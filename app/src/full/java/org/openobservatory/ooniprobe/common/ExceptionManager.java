package org.openobservatory.ooniprobe.common;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import io.sentry.Sentry;

public class ExceptionManager {
    public static void logException(Exception e){
        FirebaseCrashlytics.getInstance().recordException(e);
        Sentry.captureException(e);
    }
}
