package org.openobservatory.ooniprobe.activity.runtests

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.openobservatory.engine.BaseNettest
import org.openobservatory.ooniprobe.BuildConfig
import org.openobservatory.ooniprobe.R
import org.openobservatory.ooniprobe.activity.AbstractActivity
import org.openobservatory.ooniprobe.activity.RunningActivity
import org.openobservatory.ooniprobe.activity.runtests.RunTestsViewModel.Companion.SELECT_ALL
import org.openobservatory.ooniprobe.activity.runtests.RunTestsViewModel.Companion.SELECT_NONE
import org.openobservatory.ooniprobe.activity.runtests.RunTestsViewModel.Companion.SELECT_SOME
import org.openobservatory.ooniprobe.activity.runtests.adapter.RunTestsExpandableListViewAdapter
import org.openobservatory.ooniprobe.activity.runtests.models.ChildItem
import org.openobservatory.ooniprobe.activity.runtests.models.GroupItem
import org.openobservatory.ooniprobe.common.AbstractDescriptor
import org.openobservatory.ooniprobe.common.OONIDescriptor
import org.openobservatory.ooniprobe.common.OONITests
import org.openobservatory.ooniprobe.common.PreferenceManager
import org.openobservatory.ooniprobe.common.disableTest
import org.openobservatory.ooniprobe.common.enableTest
import org.openobservatory.ooniprobe.databinding.ActivityRunTestsBinding
import org.openobservatory.ooniprobe.model.database.InstalledDescriptor
import java.io.Serializable
import javax.inject.Inject

class RunTestsActivity : AbstractActivity() {
    lateinit var binding: ActivityRunTestsBinding

    private lateinit var adapter: RunTestsExpandableListViewAdapter

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var viewModel: RunTestsViewModel

    companion object {
        const val TESTS: String = "tests"

        /**
         * Create a new intent to start the [RunTestsActivity].
         * @param context The context from which the activity is started.
         * @param testSuites The list of test suites to run.
         * @return The intent to start the [RunTestsActivity] with unexpired descriptors.
         */
        @JvmStatic
        fun newIntent(context: Context, testSuites: List<AbstractDescriptor<BaseNettest>>): Intent {
            return Intent(context, RunTestsActivity::class.java).putExtras(Bundle().apply {
                putSerializable(TESTS, testSuites.filter {
                    if (it is InstalledDescriptor) {
                        return@filter it.descriptor?.expired() == false
                    } else {
                        return@filter true
                    }
                } as Serializable)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunTestsBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(
                ContextCompat.getDrawable(this@RunTestsActivity, R.drawable.close)
                    ?.apply {
                        DrawableCompat.setTint(
                            this,
                            ContextCompat.getColor(this@RunTestsActivity, R.color.color_black)
                        )
                    }
            )
            title = "Run tests".uppercase()
        }

        activityComponent?.inject(this)

        val descriptors: List<AbstractDescriptor<BaseNettest>>? =
            intent.extras?.getSerializable(TESTS) as List<AbstractDescriptor<BaseNettest>>?
        descriptors?.let { _descriptors ->

            val groupedItemList = mutableListOf<Any>()

            _descriptors.groupBy { it.javaClass }.forEach { (type, itemList) ->
                if (BuildConfig.FLAVOR_brand != "dw") {
                    if (type == OONIDescriptor::class.java) {
                        groupedItemList.add(getString(R.string.Dashboard_RunV2_Ooni_Title).uppercase())
                    } else {
                        groupedItemList.add(getString(R.string.Dashboard_RunV2_Title).uppercase())
                    }
                }
                groupedItemList.addAll(itemList.map { descriptor ->
                    descriptor.toRunTestsGroupItem(preferenceManager = preferenceManager)
                })
            }
            adapter = RunTestsExpandableListViewAdapter(
                groupedItemList,
                viewModel
            )

            binding.expandableListView.let {
                it.setAdapter(adapter)
                when (BuildConfig.FLAVOR_brand) {
                    "dw" -> {
                        it.expandGroup(0)
                        for (i in 1 until adapter.groupCount) {
                            it.collapseGroup(i)
                        }
                    }

                    else -> {
                        for (i in 0 until adapter.groupCount) {
                            it.expandGroup(i)
                        }
                    }
                }

                // NOTE: This listener is used to update the status indicator when the view is fully drawn.
                it.viewTreeObserver.addOnGlobalLayoutListener {
                    updateStatusIndicator()
                }
            }

            binding.selectAll.setOnClickListener { onSelectAllClickListener() }

            binding.selectNone.setOnClickListener { onSelectNoneClickListener() }

            viewModel.selectedAllBtnStatus.observe(this, this::selectAllBtnStatusObserver)

            binding.fabRunTests.setOnClickListener { onRunTestsClickListener() }
        } ?: run {
            finish()
        }

    }

    private fun onRunTestsClickListener() {
        updatePreferences()
        val selectedChildItems: List<String> = getChildItemsSelectedIdList()
        if (selectedChildItems.isNotEmpty()) {
            val testSuitesToRun = getGroupItemsAtLeastOneChildEnabled().map { groupItem ->
                return@map groupItem.getTest(this)
            }
            RunningActivity.runAsForegroundService(
                this@RunTestsActivity,
                java.util.ArrayList(testSuitesToRun),
                { finish() },
                preferenceManager
            )
        } else {
            Toast.makeText(this@RunTestsActivity, R.string.Dashboard_RunTests_RunButton_Empty, Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Update the preferences based on the selected tests.
     * This method is used to update the preferences when the user has selected the tests that they want to run.
     */
    private fun updatePreferences() {
        for (i in 0 until adapter.groupCount) {
            when (val group = adapter.getGroup(i)) {
                is GroupItem -> {
                    when (group.name) {
                        OONITests.EXPERIMENTAL.label -> {
                            val testNames = OONITests.EXPERIMENTAL.nettests.map { it.name };
                            when (group.nettests.filter { testNames.contains(it.name) }
                                .map { it.selected }.all { it }) {
                                true -> preferenceManager.enableTest(OONITests.EXPERIMENTAL.label)
                                false -> preferenceManager.disableTest(OONITests.EXPERIMENTAL.label)
                            }
                        }

                        else -> group.nettests.forEach { nettest ->
                            when (nettest.selected) {
                                true -> preferenceManager.enableTest(
                                    nettest.name,
                                    group.preferencePrefix()
                                )

                                false -> preferenceManager.disableTest(
                                    nettest.name,
                                    group.preferencePrefix()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectAllBtnStatusObserver(selectAllBtnStatus: String?) {
        if (!TextUtils.isEmpty(selectAllBtnStatus)) {
            when (selectAllBtnStatus) {
                SELECT_ALL -> {
                    binding.selectNone.isActivated = true
                    binding.selectAll.isActivated = false
                }

                SELECT_NONE -> {
                    binding.selectNone.isActivated = true
                    binding.selectAll.isActivated = false
                }

                SELECT_SOME -> {
                    binding.selectNone.isActivated = true
                    binding.selectAll.isActivated = true
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun onSelectNoneClickListener() {
        viewModel.setSelectedAllBtnStatus(SELECT_NONE)
        adapter.notifyDataSetChanged()
    }

    private fun onSelectAllClickListener() {
        viewModel.setSelectedAllBtnStatus(SELECT_ALL)
        adapter.notifyDataSetChanged()
    }


    private fun updateStatusIndicator() {
        val childCount = getChildItemsSelectedIdList().size
        binding.fabRunTests.text = getString(R.string.Dashboard_RunTests_RunButton_Label, childCount.toString())
    }

    private fun getChildItemsSelectedIdList(): List<String> {
        val childItemSelectedIdList: MutableList<String> = ArrayList()
        for (i in 0 until adapter.groupCount) {
            when (val group = adapter.getGroup(i)) {
                is GroupItem -> {
                    val secondLevelItemList: List<ChildItem> = group.nettests
                    secondLevelItemList
                        .asSequence()
                        .filter { it.selected }
                        .mapTo(childItemSelectedIdList) { it.name }
                }
            }
        }
        return childItemSelectedIdList
    }

    private fun getGroupItemsAtLeastOneChildEnabled(): List<GroupItem> {
        val items: MutableList<GroupItem> = ArrayList()
        for (i in 0 until adapter.groupCount) {
            when (val group = adapter.getGroup(i)) {
                is GroupItem -> {
                    if (group.nettests.any { it.selected }) {
                        items.add(group.apply {
                            nettests = nettests.filter { it.selected }
                        })
                    }
                }
            }
        }
        return items
    }
}
