package net.sf.jrtps.rpc;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

/**
 * This annotation can be used to set characteristics of service interface 
 * to non-default values. The use of this annotation is not mandatory.
 * If not used, defaults will be used.
 */
@Documented
@Target(TYPE)
public @interface RPCConfiguration {
    /**
     * Name of the the request topic. Clients will write invocation data into this topic
     * and Services will read invocation data from this topic. If not given or the name
     * is an empty String, the topic name will be <i>&lt;serviceName&gt;_Service_Request</i>.
     * <p>
     * It is IllegalArgumentException is thrown, if the requestTopic and replyTopic is set
     * to same name.
     * 
     * @return Name of the the request topic.
     */
    String requestTopic();
    
    /**
     * Name of the reply topic. Services will write responses to to this topic and 
     * Clients will read responses from this topic. If not given or the name is an empty String, 
     * the topic will be <i>&lt;serviceName&gt;_Service_Reply</i>.
     * <p>
     * It is IllegalArgumentException is thrown, if the requestTopic and replyTopic is set
     * to same name.
     * 
     * @return Name of the reply topic
     */
    String replyTopic();
    
    /**
     * Name of the service. If not given, name of the service is the simple name of 
     * service Class
     * @see java.lang.Class#getSimpleName()
     * @return Name of the service
     */
    String serviceName() default "";
    
    /**
     * Name of the instance. If not given, instance name of the service is the simple name of 
     * service Class
     * @see java.lang.Class#getSimpleName()
     * @return Name of the instance
     */
    String instanceName() default "";
}
