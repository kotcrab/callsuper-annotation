package com.kotcrab.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main annotation used by CallSuper library. If this annotation is used on method then all overrides of this method must call it's super method.
 * Annotation processing must be enabled in order to support this functionality.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CallSuper {
}
