package org.openobservatory.ooniprobe.common;


public class ThirdPartyServices {
    public static void reloadConsents(Application app) {
    }

    public static void logException(Exception e){
        // does nothing
    }

    public static void acceptCrash(Application app) {
        //no need to set false, it's already the default value
    }
}
