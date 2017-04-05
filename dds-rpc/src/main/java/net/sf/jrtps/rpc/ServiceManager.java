package net.sf.jrtps.rpc;

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

   private final Map<Service, DataReader<Request>> requestReaders = new HashMap<>();
   private final Map<Service, DataWriter<Reply>> replyWriters = new HashMap<>();
   private final Set<Service> services = new HashSet<>();

   private final Participant participant;
   private final QualityOfService serviceQos = new QualityOfService();
   /**
    * Creates a ServiceManager with default participant.
    */
   public ServiceManager() {
      this.participant = new Participant();
      // TODO: Check serviceQos
      serviceQos.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, Duration.INFINITE));
      serviceQos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_ALL, 1));
      serviceQos.setPolicy(new QosDurability(QosDurability.Kind.VOLATILE));
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

      services.add(service);
      createEndpoints(service);
   }


   private void createEndpoints(Service service) {      
      logger.debug("Creating reader and writer for {}", service.getClass().getSimpleName());

      DataReader<Request> dr = 
            participant.createDataReader(service.getClass().getSimpleName() + "_Service_Request",
                  Request.class, Request.class.getName(), serviceQos);
      requestReaders.put(service, dr);
      
      DataWriter<Reply> dw = 
            participant.createDataWriter(service.getClass().getSimpleName() + "_Service_Reply",
                  Reply.class, Reply.class.getName(), serviceQos);
      replyWriters.put(service, dw);

      dr.addSampleListener(new ServiceInvoker(dr, dw, service));
   }
}
