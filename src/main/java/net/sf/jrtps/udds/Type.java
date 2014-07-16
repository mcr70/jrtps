package net.sf.jrtps.udds;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to set the topic name and type name of the DDS object/topic.
 *
 * If a class does not have a Topic annotation, topic name is set to
 * simple name of class and type name is set to fully qualified class name.
 * 
 * @author mcr70
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Type {
    /**
     * Topic name. Optional. If not given, determine topic name from the class name
     * @return name of the topic
     */
    String topicName() default "";
    /**
     * Type name. 
     * @return name of the type
     */
    String typeName();
}
