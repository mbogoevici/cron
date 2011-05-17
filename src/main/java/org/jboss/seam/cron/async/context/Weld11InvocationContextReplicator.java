package org.jboss.seam.cron.async.context;

import org.jboss.seam.solder.core.Veto;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link InvocationContextReplicator} implementation for Weld 1.1
 *
 * @author Marius Bogoevici
 */
public class Weld11InvocationContextReplicator implements InvocationContextReplicator {

    private static Class<?> INTERCEPTOR_INVOCATION_CONTEXT_CLASS;
    private static Class<?> INTERCEPTION_CHAIN_CLASS;
    private static Class<?> SIMPLE_INTERCEPTION_CHAIN_CLASS;
    private static Class<?> INTERCEPTION_TYPE_CLASS;

    private static Field INTERCEPTION_CHAIN_FIELD;
    private static Field INTERCEPTOR_METHOD_INVOCATIONS_FIELD;
    private static Field POSITION_FIELD;
    private static Field CONTEXT_DATA_FIELD;
    private static Field TIMER_FIELD;
    private static Field PARAMETERS_FIELD;

    private static Constructor<?> INTERCEPTION_CHAIN_CONSTRUCTOR;
    private static Constructor<InvocationContext> INVOCATION_CONTEXT_CONSTRUCTOR;

    private static boolean initializedSuccessfully = false;

    static {
        try {
            INTERCEPTOR_INVOCATION_CONTEXT_CLASS
                    = getClassLoader().loadClass("org.jboss.interceptor.proxy.InterceptorInvocationContext");
            INTERCEPTION_CHAIN_CLASS
                    = getClassLoader().loadClass("org.jboss.interceptor.spi.context.InterceptionChain");
            SIMPLE_INTERCEPTION_CHAIN_CLASS
                    = getClassLoader().loadClass("org.jboss.interceptor.proxy.SimpleInterceptionChain");
            INTERCEPTION_TYPE_CLASS
                    = getClassLoader().loadClass("org.jboss.interceptor.spi.model.InterceptionType");


            INTERCEPTION_CHAIN_FIELD = getField(INTERCEPTOR_INVOCATION_CONTEXT_CLASS, "interceptionChain");
            CONTEXT_DATA_FIELD = getField(INTERCEPTOR_INVOCATION_CONTEXT_CLASS, "contextData");
            TIMER_FIELD = getField(INTERCEPTOR_INVOCATION_CONTEXT_CLASS, "timer");
            PARAMETERS_FIELD = getField(INTERCEPTOR_INVOCATION_CONTEXT_CLASS, "parameters");
            INTERCEPTOR_METHOD_INVOCATIONS_FIELD = getField(SIMPLE_INTERCEPTION_CHAIN_CLASS, "interceptorMethodInvocations");
            POSITION_FIELD = getField(SIMPLE_INTERCEPTION_CHAIN_CLASS, "currentPosition");

            INTERCEPTION_CHAIN_CONSTRUCTOR = SIMPLE_INTERCEPTION_CHAIN_CLASS.getConstructor(Collection.class, INTERCEPTION_TYPE_CLASS, Object.class, Method.class);
            INVOCATION_CONTEXT_CONSTRUCTOR = (Constructor<InvocationContext>) INTERCEPTOR_INVOCATION_CONTEXT_CLASS.getConstructor(INTERCEPTION_CHAIN_CLASS, Object.class, Method.class, Object.class);

            initializedSuccessfully = true;
        } catch (Exception e) {

        }
    }

    private static ClassLoader getClassLoader() {
        return Weld11InvocationContextReplicator.class.getClassLoader();
    }

    public InvocationContext replicate(InvocationContext context) throws Exception {
        if (!initializedSuccessfully) {
            throw new IllegalStateException ("Critical Weld classes not found, replication is not supported");
        }
        if (!(context.getClass().getName().startsWith("org.jboss.interceptor"))) {
            throw new IllegalArgumentException("Cannot clone: invocationContext not an InterceptorInvocationContext");
        }
        Object interceptionChain = INTERCEPTION_CHAIN_FIELD.get(context);

        Object newInterceptionChain = INTERCEPTION_CHAIN_CONSTRUCTOR.newInstance(Collections.emptyList(), null, context.getTarget(), context.getMethod());

        copyField(INTERCEPTOR_METHOD_INVOCATIONS_FIELD, newInterceptionChain, interceptionChain);
        copyField(POSITION_FIELD, newInterceptionChain, interceptionChain);

        InvocationContext newContext = INVOCATION_CONTEXT_CONSTRUCTOR.newInstance(newInterceptionChain, context.getTarget(), context.getMethod(), null);
        copyField(CONTEXT_DATA_FIELD, newContext, context);
        copyField(TIMER_FIELD, newContext, context);
        copyField(PARAMETERS_FIELD, newContext, context);
        return newContext;
    }

    private static void copyField(Field field, Object destination, Object source) throws IllegalAccessException {
        field.set(destination, field.get(source));
    }

    private static Field getField(final Class<?> clazz, final String fieldName) {
        return doSecurely(new PrivilegedAction<Field>() {
            public Field run() {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static <O> O doSecurely(final PrivilegedAction<O> action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            return AccessController.doPrivileged(action);
        } else {
            return action.run();
        }
    }
}
