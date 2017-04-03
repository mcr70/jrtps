package net.sf.jrtps.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This Annotation can be used by applications to mark an insterface or class
 * as a DDS service
 * @author mcr70
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DDSService {
   /**
    * Name of the service
    * @return Name of the service
    */
   String serviceName() default "Service";

   /**
    * Name of the topic used for RPC requests.
    * @return Name of the request topic
    */
   String requestTopic();

   /**
    * Name of the topic used for RPC responses.
    * @return Name of the response topic
    */
   String responseTopic();
}
