package org.openobservatory.engine;

import oonimkall.Oonimkall;
import oonimkall.Session;

final class PESession implements OONISession {
    private Session session;

    public PESession(OONISessionConfig config) throws Exception {
        session = Oonimkall.newSession(config.toOonimkallSessionConfig());
    }

    public OONIGeolocateResults geolocate(OONIContext ctx) throws Exception {
        return new OONIGeolocateResults(session.geolocate(ctx.ctx));
    }

    public void maybeUpdateResources(OONIContext ctx) throws Exception {
        session.maybeUpdateResources(ctx.ctx);
    }

    public OONIContext newContext() {
        return newContextWithTimeout(-1);
    }

    public OONIContext newContextWithTimeout(long timeout) {
        return new OONIContext(session.newContextWithTimeout(timeout));
    }

    public OONISubmitResults submit(OONIContext ctx, String measurement) throws Exception {
        return new OONISubmitResults(session.submit(ctx.ctx, measurement));
    }
}
