package net.sf.jrtps.rpc;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.Participant;

public class ServiceManager {
   private Participant participant;

   /**
    * Creates a ServiceManager with default participant.
    */
   public ServiceManager() {
      this.participant = new Participant();
   }
   
   /**
    * Registers a new Service to this ServiceManager. A Succesfull registration of
    * a Service will create all the needed internal DDS Entities. Failure to 
    * create a service will clean up all the related DDS Entities.
    * 
    * @param service a Service to create 
    */
   public void registerService(Service service) {
      createEndpoints(service);
   }
   
   /**
    * Unregistering a Service will cleanup all the related DDS resources.
    * @param service a Service to unregister
    */
   public void unregisterService(Service service) {
      closeEndpoints(service);      
   }
   
   private void createEndpoints(Service service) {
      List<DataReader<?>> requestReaders = new LinkedList<>();
      List<DataWriter<?>> responseWriters = new LinkedList<>();
      
      Method[] methods = service.getClass().getMethods();
      for (Method m: methods) {
         
      }
   }

   private void closeEndpoints(Service service) {
      Method[] methods = service.getClass().getMethods();
      for (Method m: methods) {
         
      }
   }
}
