package net.sf.jrtps;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on constructors, methods or classes that are experimental in nature.
 * @author mcr70
 */
@Documented
@Retention(value=RetentionPolicy.SOURCE)
@Target(value= {ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface Experimental {
	String value() default "";
}
