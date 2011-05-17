package org.jboss.seam.cron.async.context;

import javax.interceptor.InvocationContext;

/**
 * Trivial implementation of {@link InvocationContextReplicator}. Returns the same {@link InvocationContext}
 * instance. To be used when there is no suitable cloning strategy for the current container
 *
 * @author Marius Bogoevici
 */
public class EchoInvocationContextReplicator implements InvocationContextReplicator {

    public InvocationContext replicate(InvocationContext invocationContext) throws Exception {
        return invocationContext;
    }

}
