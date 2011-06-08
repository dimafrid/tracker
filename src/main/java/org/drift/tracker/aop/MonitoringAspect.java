package org.drift.tracker.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.drift.tracker.MonitoredCall;
import org.drift.tracker.Tracker;
import org.drift.tracker.TrackerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Dima Frid
 */
@Aspect
public class MonitoringAspect {

    @Pointcut("@annotation(Monitored) && execution(* *(..))")
    public void monitoredPoint() {
    }

    @Around("monitoredPoint()")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        String trackerName = getTrackerName(pjp);
        Tracker tracker = TrackerFactory.getTracker(trackerName);

        MonitoredCall call = composeCall(pjp);
        tracker.registerCall(call);

        long start = System.currentTimeMillis();
        try {
            Object ret = pjp.proceed();
            long end = System.currentTimeMillis() - start;

            StringBuilder buf = new StringBuilder();
            buf.append("{monitor} " + call.getCall());
            if (call.logArgs) {
                buf.append(" with args " + Arrays.toString(call.args));
            }
            buf.append(" took " + end + " ms");
            return ret;
        } catch (Throwable t) {
            long end = System.currentTimeMillis() - start;
            StringBuilder buf = new StringBuilder();
            buf.append("Exception occurred while executing " + call.typeName + "." + call.methodName);
            if (call.logArgs) {
                buf.append(" with args [" + Arrays.toString(call.args) + "]");
            }
            buf.append("; took " + end + " ms");
            throw t;
        } finally {
            tracker.deregisterCall();
        }
    }

    private String getTrackerName(ProceedingJoinPoint pjp) {
        Method method = getMethod(pjp);
        if (method != null) {
            Monitored annotation = method.getAnnotation(Monitored.class);
            if (annotation != null) {
                return annotation.trackerName();
            }
        }

        return TrackerFactory.DEFAULT_TRACKER_NAME;
    }

    private Method getMethod(ProceedingJoinPoint pjp) {
        Signature signature = pjp.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature mSignature = (MethodSignature) signature;
            Class targetClass = pjp.getTarget().getClass();
            try {
                return targetClass.getMethod(mSignature.getMethod().getName(), mSignature.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private MonitoredCall composeCall(ProceedingJoinPoint pjp) {
        Method method = getMethod(pjp);
        MonitoredCall call;
        if (method != null) {
            String methodName = method.getName();

            call = new MonitoredCall(pjp.getTarget().getClass().getSimpleName(), methodName, pjp.getArgs());

            Monitored annotation = method.getAnnotation(Monitored.class);
            if (annotation != null) {
                call.logArgs = annotation.logArgs();
                call.setMsg(annotation.message());
            }
        } else {
            //todo:Dima need to be refined
            Signature signature = pjp.getSignature();
            call = new MonitoredCall(signature.getDeclaringType().getSimpleName(), signature.getName(), pjp.getArgs());
        }
        return call;
    }
}
