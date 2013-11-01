package net.sf.jrtps;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides means to tag fields as being part of a RTPS(DDS) key.
 * RTPS(DDS) key uniquely identifies an instance of an Object in domain/topic.<br>
 * 
 * A default index is set to 0, which allows fields to be annotated with a simple Key annotation:<p>
 * <pre>
 * public class HelloMessage {
 *    private @Key int id;
 *    private String message;
 * }
 * </pre>
 * An example with multiple Key elements:<p>
 * <pre>
 * public class HelloMessage {
 *    private @Key(index=0) int id;
 *    private @Key(index=1) long foreignId;
 *    private String message;
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
	 * Get the index of the key. RTPS(DDS) key may be composed of multiple fields.
	 * This annotation method provides means to set those fields in a known order.
	 * Indexing starts from 0, and it is an error to provide gaps between indices. <p>
	 * 
	 * For example, it is correct to provide indices 0,1,2 with this annotation, but 
	 * incorrect to provide indices 0,2,3, or 2,3,4.<p>
	 * 
	 * @return index of the key
	 */
	int index() default 0;
}
