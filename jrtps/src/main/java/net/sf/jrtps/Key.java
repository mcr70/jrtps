package net.sf.jrtps;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides means to tag fields as being part of a RTPS(DDS) key.
 * A RTPS(DDS) key uniquely identifies an instance of an Object id RTPS(DDS) domain.
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
	 * Get the index of the key. A RTPS(DDS) key may composed of multiple fields.
	 * This annotation method provides means to set those fields in known order.
	 * Indexing for the starts from 0, and it is an error to provide gaps between indices. <p>
	 * 
	 * For example, it is correct to provide indices 0,1,2 with this annotation, but 
	 * incorrect to provide indices 0,2,3, or 2,3,4 (starts from 0)
	 *  
	 * @return index of the key
	 */
	int index() default 0;
}
