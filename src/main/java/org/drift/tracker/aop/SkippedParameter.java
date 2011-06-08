package org.drift.tracker.aop;

import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dima Frid
 */
@Target({PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkippedParameter {
}
