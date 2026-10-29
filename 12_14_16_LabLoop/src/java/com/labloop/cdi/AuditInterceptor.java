package com.labloop.cdi;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CDI INTERCEPTOR - cross-cutting logging + performance monitoring.
 * Runs around every method of classes marked @Audited (all LabLoop services).
 * @Priority enables it for the whole app, so no beans.xml registration is needed.
 * Output appears in the NetBeans "GlassFish Server" output tab.
 */
@Audited
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AuditInterceptor {

    private static final Logger LOG = Logger.getLogger("LabLoop.Audit");
    private static final long SLOW_MS = 500;

    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {
        String where = ctx.getMethod().getDeclaringClass().getSimpleName() + "." + ctx.getMethod().getName();
        long start = System.nanoTime();
        try {
            return ctx.proceed();
        } catch (Exception e) {
            LOG.log(Level.INFO, "[LabLoop] {0} failed: {1}", new Object[]{where, e.getMessage()});
            throw e;
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            if (ms >= SLOW_MS) {
                LOG.log(Level.WARNING, "[LabLoop] SLOW {0} took {1} ms", new Object[]{where, ms});
            } else {
                LOG.log(Level.INFO, "[LabLoop] {0} took {1} ms", new Object[]{where, ms});
            }
        }
    }
}
