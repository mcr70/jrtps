package net.sf.jrtps.udds;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to set the type name of the DDS object/topic.
 * If a class does not have a type name annotation, type name is set to
 * fully qualified class name.
 * 
 * @author mcr70
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface TypeName {
    String typeName();
}
