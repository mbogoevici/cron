/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.cron.async.context;

import javax.interceptor.InvocationContext;

/**
 * Wrapper class for {@link InvocationContext} replication. Detects the current container and initializes an appropriate
 * {@link InvocationContextReplicator}. If no implementation is found for the current container, it falls back to an
 * implementation that returns the same {@link InvocationContext} instance.
 *
 * @author Marius Bogoevici
 */
public class InvocationContextReplicationUtils {

    private static InvocationContextReplicator invocationContextReplicator;

    static {
        try {
            InvocationContextReplicationUtils.class.getClassLoader().loadClass("org.jboss.weld.bootstrap.WeldBootstrap");
            invocationContextReplicator = Weld11InvocationContextReplicatorDelegate.getReference();
        } catch (ClassNotFoundException e) {
            //ignore, it's not the right container
        }
        if (invocationContextReplicator == null) {
            // fall back to the default, do not make copies
            invocationContextReplicator = new EchoInvocationContextReplicator();
        }
    }

    public static InvocationContext replicate(InvocationContext invocationContext) throws Exception {
       return invocationContextReplicator.replicate(invocationContext);
    }

    public static boolean canClone(InvocationContext context) {
        // Is it a Weld-based
        if (context.getClass().getName().startsWith("org.jboss.interceptor"))
            return true;
        return false;
    }

    //avoid depending on Weld11InvocationContextReplicator at runtime if not necessary
    private static class Weld11InvocationContextReplicatorDelegate {
        static InvocationContextReplicator getReference() {
            return new Weld11InvocationContextReplicator();
        }
    }
}
