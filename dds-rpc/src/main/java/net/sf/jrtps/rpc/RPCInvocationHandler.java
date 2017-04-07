package net.sf.jrtps.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.rpc.Reply.ReplyHeader;
import net.sf.jrtps.rpc.Request.Call;
import net.sf.jrtps.rpc.Request.RequestHeader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.udds.CommunicationListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

class RPCInvocationHandler implements InvocationHandler, SampleListener<Reply>, CommunicationListener<SubscriptionData> {
   private static final Logger logger = LoggerFactory.getLogger(RPCInvocationHandler.class);

   private final Map<Class<?>, Serializer> serializers;
   private final Map<String, Integer> hashMap = new HashMap<>();
   private final DataWriter<Request> requestWriter;
   private final DataReader<Reply> responseReader;

   long seqNum = 1;
   
   RPCInvocationHandler(DataWriter<Request> requestWriter, DataReader<Reply> responseReader,
         Map<Class<?>, Serializer> serializers) {
      this.requestWriter = requestWriter;
      this.responseReader = responseReader;
      this.serializers = serializers;
      
      responseReader.addSampleListener(this);
      requestWriter.addCommunicationListener(this);
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String mName = method.getName();
      logger.debug("Invoking {}", mName); 

      RequestHeader header = new Request.RequestHeader(requestWriter.getGuid(), 
            new SequenceNumber(seqNum++), "serviceName", "instanceName");
      
      Integer discriminator = hashMap.get(mName);
      if (discriminator == null) {
         discriminator = hash(mName);
         hashMap.put(mName, discriminator);
      }
      
      RTPSByteBuffer bb = new RTPSByteBuffer(new byte[1024]); // TODO Hardcoded
      for (int i = 0; i < args.length; i++) {
         Serializer serializer = serializers.get(args[i].getClass());
         serializer.serialize(args[i], bb);
      }
      byte[] requestParameters = bb.toArray();
      Call call = new Request.Call(discriminator, requestParameters);
      
      Request request = new Request(header, call);
      requestWriter.write(request);
   
      // TODO: Synchronization should be based on Request.header.sequenceNumber
      // TODO: hardcoded
      Reply reply = queue.poll(1000, TimeUnit.MILLISECONDS);
      if (reply == null) {
         throw new TimeoutException("Timeout invoking " + method.getName());
      }

      switch(reply.header.remoteExceptionCode) {
      case ReplyHeader.REMOTE_EX_INVALID_ARGUMENT: throw new RemoteException("Invalid argument");
      case ReplyHeader.REMOTE_EX_OUT_OF_RESOURCES: throw new RemoteException("Out of resources");
      case ReplyHeader.REMOTE_EX_UNKNOWN_EXCEPTION: throw new RemoteException("Unknown exception");
      case ReplyHeader.REMOTE_EX_UNKNOWN_OPERATION: throw new RemoteException("Unknown operation");
      case ReplyHeader.REMOTE_EX_UNSUPPORTED: throw new RemoteException("Unsupported");
         
      }

      if (void.class.equals(method.getReturnType())) {
         return null;
      }

      Serializer serializer = serializers.get(method.getReturnType());
      return serializer.deSerialize(method.getReturnType(), new RTPSByteBuffer(reply.reply.result));
   }

   private BlockingQueue<Reply> queue = new ArrayBlockingQueue<>(1);
   
   @Override
   public void onSamples(List<Sample<Reply>> samples) {
      logger.debug("Got {} replies from service", samples.size());
      for (Sample<Reply> sample: samples) {
         try {
            queue.put(sample.getData());
         } catch (InterruptedException e) {
            logger.error("Got interrupted while putting reply to queue");
         }
      }
      
      // Clear responses, so that they won't start consuming memory
      responseReader.clear(samples);
   }

   @Override
   public void deadlineMissed(KeyHash instanceKey) {
      // TODO Auto-generated method stub
   }

   @Override
   public void entityMatched(SubscriptionData ed) {
      logger.debug("Reader matched: {}", ed);
   }

   @Override
   public void inconsistentQoS(SubscriptionData ed) {
      // TODO Auto-generated method stub
   }
   
   private Integer hash(String methodName) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] digest = md.digest(methodName.getBytes());
         return digest[0] + (digest[1] << 8) + (digest[2] << 16) + (digest[3] << 24); 
      } catch (NoSuchAlgorithmException e) {
         // Ignore, every Java implementation has this algorithm
      }
      
      throw new RuntimeException("Internal error; java did not have 'MD5' MessageDigest");
   }

}
