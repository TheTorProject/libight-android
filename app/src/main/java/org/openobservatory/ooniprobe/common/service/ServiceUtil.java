package org.openobservatory.ooniprobe.common.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.openobservatory.engine.OONICheckInConfig;
import org.openobservatory.ooniprobe.common.Application;
import org.openobservatory.ooniprobe.common.DefaultDescriptors;
import org.openobservatory.ooniprobe.common.PreferenceManager;
import org.openobservatory.ooniprobe.common.ReachabilityManager;
import org.openobservatory.ooniprobe.common.TestDescriptorManager;
import org.openobservatory.ooniprobe.domain.GenerateAutoRunServiceSuite;
import org.openobservatory.ooniprobe.test.suite.AbstractSuite;
import org.openobservatory.ooniprobe.test.suite.DynamicTestSuite;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class ServiceUtil {
    private static final int id = 100;

    static Dependencies d = new Dependencies();

    public static void scheduleJob(Context context) {
        Application app = ((Application) context.getApplicationContext());
        app.getServiceComponent().inject(d);

        ComponentName serviceComponent = new ComponentName(context, RunTestJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(id, serviceComponent);

        //Options explication https://www.coderzheaven.com/2016/11/22/how-to-create-a-simple-repeating-job-using-jobscheduler-in-android/
        int networkConstraint = d.preferenceManager.testWifiOnly() ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY;
        builder.setRequiredNetworkType(networkConstraint);
        builder.setRequiresCharging(d.preferenceManager.testChargingOnly());

        /*
         * Specify that this job should recur with the provided interval, not more than once per period.
         * You have no control over when within this interval
         * this job will be executed, only the guarantee that it will be executed at most once within this interval.
         */
        builder.setPeriodic(60 * 60 * 1000);
        builder.setPersisted(true); //Job scheduled to work after reboot

        //JobScheduler is specifically designed for inexact timing, so it can combine jobs from multiple apps, to try to reduce power consumption.
        JobScheduler jobScheduler = ContextCompat.getSystemService(context, JobScheduler.class);
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    public static void stopJob(Context context) {
        JobScheduler jobScheduler = ContextCompat.getSystemService(context, JobScheduler.class);
        if (jobScheduler != null) {
            jobScheduler.cancel(id);
        }
    }

    public static void startRunTestServiceUnattended(Application app) {
        app.getServiceComponent().inject(d);

        boolean isVPNInUse = ReachabilityManager.isVPNinUse(app);

        OONICheckInConfig config = app.getOONICheckInConfig();

        if (!d.generateAutoRunServiceSuite.shouldStart(config.isOnWiFi(), config.isCharging(), isVPNInUse)) {
            return;
        }
        List<DynamicTestSuite> testSuites = DefaultDescriptors.autoRunTests(app, d.preferenceManager, d.testDescriptorManager);
        if (!testSuites.isEmpty()){
            ServiceUtil.startRunTestServiceCommon(app, new ArrayList<>(testSuites), false, true);
            d.generateAutoRunServiceSuite.markAsRan();
        }

    }


    public static void startRunTestServiceManual(Context context, ArrayList<AbstractSuite> iTestSuites, boolean storeDB) {
        startRunTestServiceCommon(context, iTestSuites, storeDB, false);
    }

    private static void startRunTestServiceCommon(Context context, ArrayList<AbstractSuite> iTestSuites, boolean storeDB, boolean unattended) {
        ArrayList<AbstractSuite> testSuites = Lists.newArrayList(
                Iterables.filter(Iterables.filter(iTestSuites, item -> item != null), testSuite -> !testSuite.isTestEmpty(d.preferenceManager))
        );

        Intent serviceIntent = new Intent(context, RunTestService.class);
        serviceIntent.putExtra("testSuites", testSuites);
        serviceIntent.putExtra("storeDB", storeDB);
        serviceIntent.putExtra("unattended", unattended);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void launchNotificationSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //for Android 5-7
        intent.putExtra("app_package", context.getPackageName());
        intent.putExtra("app_uid", context.getApplicationInfo().uid);

        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

        context.startActivity(intent);
    }

    public static class Dependencies {
        @Inject
        GenerateAutoRunServiceSuite generateAutoRunServiceSuite;

        @Inject
        PreferenceManager preferenceManager;

        @Inject
        TestDescriptorManager testDescriptorManager;
    }
}
