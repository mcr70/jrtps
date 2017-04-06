package net.sf.jrtps.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.rpc.Request.Call;
import net.sf.jrtps.rpc.Request.RequestHeader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

class RPCInvocationHandler implements InvocationHandler, SampleListener<Reply> {
   private static final Logger logger = LoggerFactory.getLogger(RPCInvocationHandler.class);
   private DataWriter<Request> requestWriter;
   private DataReader<Reply> responseReader;

   long seqNum = 1;
   
   public RPCInvocationHandler(DataWriter<Request> requestWriter, DataReader<Reply> responseReader) {
      this.requestWriter = requestWriter;
      this.responseReader = responseReader;
      
      responseReader.addSampleListener(this);
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      logger.debug("Invoking {}", method.getName()); 

      RequestHeader header = new Request.RequestHeader(requestWriter.getGuid(), 
            new SequenceNumber(seqNum++), "serviceName", "instanceName");
      
      // TODO: Create discriminator
      int discriminator = 0;
      // TODO: Create requestParameters
      byte[] requestParameters = new byte[0];
      Call call = new Request.Call(discriminator, requestParameters);
      
      Request request = new Request(header, call);
      requestWriter.write(request);
      
      // TODO: synchronize and wait for reponse to arrive at onSamples(...) method
      
      return null;
   }

   @Override
   public void onSamples(List<Sample<Reply>> samples) {
      // TODO: Release locks on invoke(...) and handle response there
   }
}
