package net.sf.jrtps.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.rpc.Request.Call;
import net.sf.jrtps.rpc.Request.RequestHeader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

/**
 * This class listens for incoming service requests, and invokes 
 * corresponding service method.
 * 
 * @author mcr70
 */
class ServiceInvoker implements SampleListener<Request> {
   private static final Logger logger = LoggerFactory.getLogger(ServiceInvoker.class);

   private Service service;
   private DataReader<Request> requestReader;
   private DataWriter<Reply> replyWriter;

   ServiceInvoker(DataReader<Request> requestReader, DataWriter<Reply> replyWriter, Service service) {
      this.requestReader = requestReader;
      this.replyWriter = replyWriter;
      this.service = service;
   }

   @Override
   public void onSamples(List<Sample<Request>> samples) {
      for (Sample<Request> sample: samples) {
         invoke(sample.getData());
      }
      
      // Clear samples from the DataReader, so that they won't
      // grow infinitely
      requestReader.clear(samples);
   }

   private void invoke(Request data) {
      RequestHeader header = data.getHeader();
      Call call = data.getCall();
      Method m = getMethod(call.discriminator);
      Object[] args = getArguments(call);
      try {
         Object result = m.invoke(service, args);
         writeReply(result);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         logger.error("Failed to invoke {}", m.getName(), e);
      }
   }

   private void writeReply(Object result) {
      // TODO Auto-generated method stub
      
      Reply sample = createReply(result);
      replyWriter.write(sample);
   }

   private Reply createReply(Object result) {
      // TODO Auto-generated method stub
      return null;
   }

   private Object[] getArguments(Call call) {
      // TODO Auto-generated method stub
      return null;
   }

   private Method getMethod(int discriminator) {
      // TODO Auto-generated method stub
      return null;
   }
}
