package org.openobservatory.ooniprobe.test.suite;

import androidx.annotation.Nullable;

import org.openobservatory.ooniprobe.R;
import org.openobservatory.ooniprobe.common.PreferenceManager;
import org.openobservatory.ooniprobe.test.test.AbstractTest;
import org.openobservatory.ooniprobe.test.test.Experimental;

import java.util.ArrayList;

public class ExperimentalSuite extends AbstractSuite {
    public static final String NAME = "experimental";

    public ExperimentalSuite() {
        super(NAME,
                R.string.Test_Experimental_Fullname,
                R.string.Dashboard_Experimental_Card_Description,
                R.drawable.test_experimental,
                R.drawable.test_experimental_24,
                R.color.color_gray7,
                R.style.Theme_MaterialComponents_Light_DarkActionBar_App_NoActionBar_Experimental,
                R.style.Theme_MaterialComponents_NoActionBar_App_Experimental,
                R.string.Dashboard_Experimental_Overview_Paragraph,
                "anim/experimental.json",
                R.string.TestResults_NotAvailable);
    }

    @Override public AbstractTest[] getTestList(@Nullable PreferenceManager pm) {
        if (super.getTestList(pm) == null) {
            ArrayList<AbstractTest> list = new ArrayList<>();
            list.add(new Experimental("stunreachability"));
            super.setTestList(list.toArray(new AbstractTest[0]));
        }
        return super.getTestList(pm);
    }}
