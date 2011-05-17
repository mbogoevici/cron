package org.jboss.seam.cron.async.context;

import javax.interceptor.InvocationContext;

/**
 * Abstraction of an {@link javax.interceptor.InvocationContext} cloning operation
 * for use with the {@link org.jboss.seam.cron.async.AsynchronousInterceptor}
 * To be implemented separately for various CDI containers
 *
 * @author Marius Bogoevici
 */
public interface InvocationContextReplicator {

     InvocationContext replicate(InvocationContext invocationContext) throws Exception;
}
