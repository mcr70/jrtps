package net.sf.jrtps.udds;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides means to tag fields as being part of a RTPS(DDS)
 * key. A key uniquely identifies an instance of an Object in domain/topic.<br>
 * 
 * A default index is set to 0, which allows fields to be annotated with a
 * simple Key annotation:
 * 
 * <pre>
 * public class HelloMessage {
 *     private @Key
 *     int id;
 *     private String message;
 * }
 * </pre>
 * 
 * An example with multiple Key elements:
 * 
 * <pre>
 * public class HelloMessage {
 *     private @Key(index = 0)
 *     int id;
 *     private @Key(index = 1)
 *     long foreignId;
 *     private String message;
 * }
 * </pre>
 * 
 * @author mcr70
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Key {
    /**
     * Get the index of the key. A key may be composed of multiple fields. This
     * annotation method provides means to set those fields in a known order.
     * Key fields are ordered according to their index.
     * <p>
     * 
     * It is an error to provide two fields with same index.
     * 
     * @return index of the key
     */
    int index() default 0;
}
