package org.drift.tracker.aop;

import org.drift.tracker.TrackerFactory;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * @author Dima Frid
 */
@Target({METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Monitored {
    String trackerName() default TrackerFactory.DEFAULT_TRACKER_NAME;

    String message() default "";

    boolean logArgs() default false;
}