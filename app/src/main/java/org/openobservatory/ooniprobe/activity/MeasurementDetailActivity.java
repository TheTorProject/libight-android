package org.openobservatory.ooniprobe.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.apache.commons.io.FileUtils;
import org.openobservatory.ooniprobe.R;
import org.openobservatory.ooniprobe.client.callback.GetMeasurementJsonCallback;
import org.openobservatory.ooniprobe.client.callback.GetMeasurementsCallback;
import org.openobservatory.ooniprobe.common.OrchestraTask;
import org.openobservatory.ooniprobe.common.ReachabilityManager;
import org.openobservatory.ooniprobe.common.ResubmitTask;
import org.openobservatory.ooniprobe.fragment.measurement.DashFragment;
import org.openobservatory.ooniprobe.fragment.measurement.FacebookMessengerFragment;
import org.openobservatory.ooniprobe.fragment.measurement.FailedFragment;
import org.openobservatory.ooniprobe.fragment.measurement.HeaderNdtFragment;
import org.openobservatory.ooniprobe.fragment.measurement.HeaderOutcomeFragment;
import org.openobservatory.ooniprobe.fragment.measurement.HttpHeaderFieldManipulationFragment;
import org.openobservatory.ooniprobe.fragment.measurement.HttpInvalidRequestLineFragment;
import org.openobservatory.ooniprobe.fragment.measurement.NdtFragment;
import org.openobservatory.ooniprobe.fragment.measurement.TelegramFragment;
import org.openobservatory.ooniprobe.fragment.measurement.WebConnectivityFragment;
import org.openobservatory.ooniprobe.fragment.measurement.WhatsappFragment;
import org.openobservatory.ooniprobe.fragment.resultHeader.ResultHeaderDetailFragment;
import org.openobservatory.ooniprobe.model.api.ApiMeasurement;
import org.openobservatory.ooniprobe.model.database.Measurement;
import org.openobservatory.ooniprobe.model.database.Measurement_Table;
import org.openobservatory.ooniprobe.test.suite.PerformanceSuite;
import org.openobservatory.ooniprobe.test.test.Dash;
import org.openobservatory.ooniprobe.test.test.FacebookMessenger;
import org.openobservatory.ooniprobe.test.test.HttpHeaderFieldManipulation;
import org.openobservatory.ooniprobe.test.test.HttpInvalidRequestLine;
import org.openobservatory.ooniprobe.test.test.Ndt;
import org.openobservatory.ooniprobe.test.test.Telegram;
import org.openobservatory.ooniprobe.test.test.WebConnectivity;
import org.openobservatory.ooniprobe.test.test.Whatsapp;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import localhost.toolkit.app.fragment.ConfirmDialogFragment;
import localhost.toolkit.app.fragment.MessageDialogFragment;
import okhttp3.Request;

public class MeasurementDetailActivity extends AbstractActivity implements ConfirmDialogFragment.OnConfirmedListener {
    private static final String ID = "id";
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private Measurement measurement;
    private Snackbar snackbar;
    private Boolean isInExplorer;

    public static Intent newIntent(Context context, int id) {
        return new Intent(context, MeasurementDetailActivity.class).putExtra(ID, id);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        measurement = SQLite.select().from(Measurement.class)
                .where(Measurement_Table.id.eq(getIntent().getIntExtra(ID, 0))).querySingle();
        assert measurement != null;
        measurement.result.load();
        setTheme(measurement.is_failed ?
                R.style.Theme_MaterialComponents_Light_DarkActionBar_App_NoActionBar_Failed :
                measurement.result.test_group_name.equals(PerformanceSuite.NAME) ?
                        measurement.result.getTestSuite().getThemeLight() :
                        measurement.is_anomaly ?
                                R.style.Theme_MaterialComponents_Light_DarkActionBar_App_NoActionBar_Failure :
                                R.style.Theme_MaterialComponents_Light_DarkActionBar_App_NoActionBar_Success);
        setContentView(R.layout.activity_measurement_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(measurement.getTest().getLabelResId());
        }
        Fragment detail = null;
        Fragment head = null;
        if (measurement.is_failed) {
            head = HeaderOutcomeFragment.newInstance(R.drawable.error_48dp,
                    getString(R.string.bold,
                            getString(R.string.outcomeHeader,
                                    getString(R.string.TestResults_Details_Failed_Title),
                                    getString(R.string.TestResults_Details_Failed_Paragraph))));
            detail = FailedFragment.newInstance(measurement);
        } else {
            int iconRes = measurement.is_anomaly ? R.drawable.exclamation_white_48dp : R.drawable.tick_white_48dp;
            switch (measurement.test_name) {
                case Dash.NAME:
                    head = HeaderOutcomeFragment.newInstance(null,
                            getString(R.string.outcomeHeader,
                                    getString(measurement.getTestKeys().getVideoQuality(true)),
                                    getString(R.string.TestResults_Details_Performance_Dash_VideoWithoutBuffering,
                                            getString(measurement.getTestKeys().getVideoQuality(false)))));
                    detail = DashFragment.newInstance(measurement);
                    break;
                case FacebookMessenger.NAME:
                    head = HeaderOutcomeFragment.newInstance(iconRes, getString(R.string.bold, getString(measurement.is_anomaly ?
                            R.string.TestResults_Details_InstantMessaging_WhatsApp_Registrations_Label_Failed :
                            R.string.TestResults_Details_InstantMessaging_WhatsApp_Reachable_Hero_Title)));
                    detail = FacebookMessengerFragment.newInstance(measurement);
                    break;
                case HttpHeaderFieldManipulation.NAME:
                    head = HeaderOutcomeFragment.newInstance(iconRes, getString(R.string.bold, getString(measurement.is_anomaly ?
                            R.string.TestResults_Details_Middleboxes_HTTPHeaderFieldManipulation_Found_Hero_Title :
                            R.string.TestResults_Details_Middleboxes_HTTPHeaderFieldManipulation_NotFound_Hero_Title)));
                    detail = HttpHeaderFieldManipulationFragment.newInstance(measurement);
                    break;
                case HttpInvalidRequestLine.NAME:
                    head = HeaderOutcomeFragment.newInstance(iconRes, getString(R.string.bold, getString(measurement.is_anomaly ?
                            R.string.TestResults_Details_Middleboxes_HTTPInvalidRequestLine_Found_Hero_Title :
                            R.string.TestResults_Details_Middleboxes_HTTPInvalidRequestLine_NotFound_Hero_Title)));
                    detail = HttpInvalidRequestLineFragment.newInstance(measurement);
                    break;
                case Ndt.NAME:
                    head = HeaderNdtFragment.newInstance(measurement);
                    detail = NdtFragment.newInstance(measurement);
                    break;
                case Telegram.NAME:
                    head = HeaderOutcomeFragment.newInstance(iconRes, getString(R.string.bold, getString(measurement.is_anomaly ?
                            R.string.TestResults_Details_InstantMessaging_WhatsApp_Registrations_Label_Failed :
                            R.string.TestResults_Details_InstantMessaging_WhatsApp_Reachable_Hero_Title)));
                    detail = TelegramFragment.newInstance(measurement);
                    break;
                case WebConnectivity.NAME:
                    head = HeaderOutcomeFragment.newInstance(iconRes, getString(R.string.outcomeHeader, measurement.url.url,
                            getString(measurement.is_anomaly ?
                                    R.string.TestResults_Details_Websites_LikelyBlocked_Hero_Title :
                                    R.string.TestResults_Details_Websites_Reachable_Hero_Title)));
                    detail = WebConnectivityFragment.newInstance(measurement);
                    break;
                case Whatsapp.NAME:
                    head = HeaderOutcomeFragment.newInstance(iconRes, getString(R.string.bold, getString(measurement.is_anomaly ?
                            R.string.TestResults_Details_InstantMessaging_WhatsApp_Registrations_Label_Failed :
                            R.string.TestResults_Details_InstantMessaging_WhatsApp_Reachable_Hero_Title)));
                    detail = WhatsappFragment.newInstance(measurement);
                    break;
            }
        }
        assert detail != null && head != null;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.footer, ResultHeaderDetailFragment.newInstance(
                        true,
                        null,
                        null,
                        measurement.start_time,
                        measurement.runtime,
                        false,
                        measurement.result.network.country_code,
                        measurement.result.network))
                .replace(R.id.body, detail)
                .replace(R.id.head, head)
                .commit();
        snackbar = Snackbar.make(coordinatorLayout, R.string.Snackbar_ResultsNotUploaded_Text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.Snackbar_ResultsNotUploaded_Upload, v1 -> runAsyncTask());
        Context c = this;
        isInExplorer = !measurement.hasReportFile(c);
        if (measurement.hasReportFile(c)){
            getApiClient().getMeasurement(measurement.report_id, null).enqueue(new GetMeasurementsCallback() {
                @Override
                public void onSuccess(ApiMeasurement.Result result) {
                    measurement.deleteEntryFile(c);
                    measurement.deleteLogFile(c);
                    isInExplorer = true;
                }
                @Override
                public void onError(String msg) {
                    isInExplorer = false;
                }
            });
        }
        load();
    }

    private void runAsyncTask() {
        new ResubmitAsyncTask(this).execute(null, measurement.id);
    }

    private void load() {
        if (!measurement.is_failed && (!measurement.is_uploaded || measurement.report_id == null))
            snackbar.show();
        else
            snackbar.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measurement, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        if (!measurement.hasLogFile(this))
                menu.findItem(R.id.viewLog).setVisible(false);
        if (measurement.report_id == null || measurement.report_id.isEmpty())
            menu.findItem(R.id.copyExplorerUrl).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rawData:
                //Try to open file, if it doesn't exist dont show Error dialog immediately but try to download the json from internet
                try {
                    File entryFile = Measurement.getEntryFile(this, measurement.id, measurement.test_name);
                    String json = FileUtils.readFileToString(entryFile, Charset.forName("UTF-8"));
                    json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(new JsonParser().parse(json));
                    startActivity(TextActivity.newIntent(this, json));
                } catch (Exception e) {
                    if (ReachabilityManager.getNetworkType(this).equals(ReachabilityManager.NO_INTERNET)) {
                        new MessageDialogFragment.Builder()
                                .withTitle(getString(R.string.Modal_Error))
                                .withMessage(getString(R.string.Modal_Error_RawDataNoInternet))
                                .build().show(getSupportFragmentManager(), null);
                        return true;
                    }
                    getApiClient().getMeasurement(measurement.report_id, null).enqueue(new GetMeasurementsCallback() {
                        @Override
                        public void onSuccess(ApiMeasurement.Result result) {
                            getOkHttpClient().newCall(new Request.Builder().url(result.measurement_url).build()).enqueue(new GetMeasurementJsonCallback() {
                                @Override
                                public void onSuccess(String json) {
                                    startActivity(TextActivity.newIntent(MeasurementDetailActivity.this, json));
                                }

                                @Override
                                public void onError(String msg) {
                                    new MessageDialogFragment.Builder()
                                            .withTitle(getString(R.string.Modal_Error))
                                            .withMessage(msg)
                                            .build().show(getSupportFragmentManager(), null);
                                }
                            });
                        }
                        @Override
                        public void onError(String msg) {
                            new MessageDialogFragment.Builder()
                                    .withTitle(getString(R.string.Modal_Error))
                                    .withMessage(msg)
                                    .build().show(getSupportFragmentManager(), null);
                        }
                    });
                }
                return true;
            case R.id.viewLog:
                try {
                    File logFile = Measurement.getLogFile(this, measurement.result.id, measurement.test_name);
                    String log = FileUtils.readFileToString(logFile, Charset.forName("UTF-8"));
                    startActivity(TextActivity.newIntent(this, log));
                } catch (Exception e) {
                    e.printStackTrace();
                    new MessageDialogFragment.Builder()
                            .withTitle(getString(R.string.Modal_Error_LogNotFound))
                            .build().show(getSupportFragmentManager(), null);
                }
                return true;
            case R.id.copyExplorerUrl:
                String link = "https://explorer.ooni.io/measurement/" + measurement.report_id;
                if (measurement.test_name.equals("web_connectivity"))
                    link = link + "?input=" + measurement.url.url;
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(getString(R.string.General_AppName), link));
                if (isInExplorer)
                    Toast.makeText(this, R.string.Toast_CopiedToClipboard, Toast.LENGTH_SHORT).show();
                else {
                    String toastStr = getString(R.string.Toast_CopiedToClipboard) + "\n" + getString(R.string.Toast_WillBeAvailable);
                    Toast.makeText(this, toastStr, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.methodology)
    void methodologyClick() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(measurement.getTest().getUrlResId()))));
    }

    @Override
    public void onConfirmation(Serializable extra, int buttonClicked) {
        if (buttonClicked == DialogInterface.BUTTON_POSITIVE)
            runAsyncTask();
    }

    private static class ResubmitAsyncTask extends ResubmitTask<MeasurementDetailActivity> {
        ResubmitAsyncTask(MeasurementDetailActivity activity) {
            super(activity);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            MeasurementDetailActivity activity = getActivity();
            if (activity != null) {
                activity.measurement = SQLite.select().from(Measurement.class)
                        .where(Measurement_Table.id.eq(activity.measurement.id)).querySingle();
                activity.load();
                if (!result)
                    new ConfirmDialogFragment.Builder()
                            .withTitle(activity.getString(R.string.Modal_UploadFailed_Title))
                            .withMessage(activity.getString(R.string.Modal_UploadFailed_Paragraph, errors.toString(), totUploads.toString()))
                            .withPositiveButton(activity.getString(R.string.Modal_Retry))
                            .build().show(activity.getSupportFragmentManager(), null);
            }
        }
    }
}
