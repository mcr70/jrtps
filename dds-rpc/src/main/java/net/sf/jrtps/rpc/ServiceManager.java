package net.sf.jrtps.rpc;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class ServiceManager {
   private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

   private final Map<Class<?>, Serializer> serializers = new HashMap<>();   
   private final Set<Service> services = new HashSet<>();

   // For services:
   private final Map<Class<?>, DataReader<Request>> requestReaders = new HashMap<>();
   private final Map<Class<?>, DataWriter<Reply>> replyWriters = new HashMap<>();

   // For clients:
   private final Map<Class<?>, DataWriter<Request>> requestWriters = new HashMap<>();
   private final Map<Class<?>, DataReader<Reply>> replyReaders = new HashMap<>();
   
   private final Participant participant;
   private final QualityOfService serviceQos = new QualityOfService();
   /**
    * Creates a ServiceManager with default participant.
    */
   public ServiceManager() {
      this(0, -1);
   }

   public ServiceManager(int domainId, int participantId) {
      this.participant = new Participant(domainId, participantId);
      this.participant.setMarshaller(Request.class, new RequestMarshaller());
      this.participant.setMarshaller(Reply.class, new ReplyMarshaller());
      
      // TODO: Check serviceQos
      serviceQos.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, Duration.INFINITE));
      serviceQos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_ALL, 1));
      serviceQos.setPolicy(new QosDurability(QosDurability.Kind.VOLATILE));
      
      initializeSerializers();
   }

   /**
    * Initialize serializers for the primitive Java types.
    */
   private void initializeSerializers() {      
      Class<?>[] primitiveClasses = new Class[] {
            int.class, Integer.class, short.class, Short.class, long.class, Long.class,
            float.class, Float.class, double.class, Double.class, char.class, Character.class,
            byte.class, Byte.class, boolean.class, Boolean.class, String.class
            };
      
      JavaPrimitiveSerializer js = new JavaPrimitiveSerializer();
      for (Class<?> c: primitiveClasses) {
         serializers.put(c, js);
      }
   }

   public <T extends Service> T createClient(Class<T> service) throws Exception {
      String reqTopic = service.getSimpleName() + "_Service_Request";
      String repTopic = service.getSimpleName() + "_Service_Reply";
      
      logger.debug("Creating writer({}) and reader({}) for client {}", 
            reqTopic, repTopic, service.getSimpleName());

      DataWriter<Request> dw = 
            participant.createDataWriter(reqTopic,
                  Request.class, Request.class.getName(), serviceQos);
      requestWriters.put(service, dw);
      
      DataReader<Reply> dr = 
            participant.createDataReader(repTopic,
                  Reply.class, Reply.class.getName(), serviceQos);
      replyReaders.put(service, dr);

      
      Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {service}, 
            new RPCInvocationHandler(dw, dr, serializers));
      
      return (T) proxy;
   }
   
   /**
    * Registers a new Service to this ServiceManager. A Succesfull registration of
    * a Service will create all the needed internal DDS Entities. Failure to 
    * create a service will clean up all the related DDS Entities.
    * 
    * @param service a Service to create 
    */
   public void registerService(Service service) {
      logger.debug("Registering service: {}", service);
      Class<?>[] interfaces = service.getClass().getInterfaces();
      
      for (Class<?> i: interfaces) {
         if (Service.class.isAssignableFrom(i)) {
            //services.add();
            createEndpoints(i, service);
         }
      }
   }

   /**
    * Registers a Serializer for given type. Serializers are used to serialize
    * Service call input parameters and return values to wire during remote
    * invocation process. By default, only Serializers for primitive types
    * (int.class, Integer.class, .... , String.class) is defined.<p>
    * 
    * If a Serializer is registered twice, first registration is overriden.
    * 
    * @param type Type 
    * @param serializer Serializer for type
    */
   public void registerSerializer(Class<?> type, Serializer serializer) {
      serializers.put(type, serializer);
   }
   
   private void createEndpoints(Class<?> serviceClass, Service service) {      
      String reqTopic = serviceClass.getSimpleName() + "_Service_Request";
      String repTopic = serviceClass.getSimpleName() + "_Service_Reply";
      
      logger.debug("Creating reader({}) and writer({}) for service {}", 
            reqTopic, repTopic, serviceClass.getSimpleName());

      DataReader<Request> dr = 
            participant.createDataReader(reqTopic,
                  Request.class, Request.class.getName(), serviceQos);
      requestReaders.put(serviceClass, dr);
      
      DataWriter<Reply> dw = 
            participant.createDataWriter(repTopic,
                  Reply.class, Reply.class.getName(), serviceQos);
      replyWriters.put(serviceClass, dw);

      dr.addSampleListener(new ServiceInvoker(serializers, dr, dw, service));
   }
}
