package org.openobservatory.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * OONISession contains shared state for running experiments and/or other
 * related task (e.g. geolocation). Note that a OONISession is not meant to
 * be a long running instance. The expected usage is that you create a new
 * session, use it immediately, and then forget about it.
 */
public interface OONISession {
    /** newContext creates a new OONIContext instance. */
    OONIContext newContext();

    /**
     * newContextWithTimeout creates a new OONIContext instance that times
     * out after the specified number of seconds. A zero or negative timeout
     * is equivalent to create a OONIContext without a timeout.
     */
    OONIContext newContextWithTimeout(long timeout);

    /** submit submits a measurement and returns the submission results. */
    OONISubmitResults submit(OONIContext ctx, String measurement) throws Exception;

    /** checkIn function is called by probes asking if there are tests to be run. */
    OONICheckInResults checkIn(OONIContext ctx, OONICheckInConfig config) throws Exception;

    /**
     * Fetches a specific ooni run descriptor.
     *
     * @param ctx OONIContext instance
     * @param id ooni run id
     * @return [OONIRunFetchResponse] with the contents of the ooni run descriptor.
     */
    OONIRunDescriptor getLatestOONIRunLink(OONIContext ctx, String probeServicesURL, long id) throws Exception;

    OONIRunRevisions getOONIRunLinkRevisions(@Nullable OONIContext ooniContext, @NotNull String probeServicesURL, long runId) throws Exception ;
    
    /** close closes the session along with any running circumvention tunnels */
    void close() throws Exception;
}
