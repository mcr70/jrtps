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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.rpc.Reply.ReplyHeader;
import net.sf.jrtps.rpc.Request.Call;
import net.sf.jrtps.rpc.Request.RequestHeader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.SampleListener;

class RPCInvocationHandler implements InvocationHandler, SampleListener<Reply> {
   private static final Logger logger = LoggerFactory.getLogger(RPCInvocationHandler.class);

   private final Map<Class<?>, Serializer> serializers;
   private final Map<String, Integer> hashMap = new HashMap<>();
   private final DataWriter<Request> requestWriter;
   private final DataReader<Reply> responseReader;

   private final Map<Long, Object> exchangeMap = new ConcurrentHashMap<>();
   private final MessageDigest md5;
   private final Configuration cfg;
   
   long seqNum = 1;

   RPCInvocationHandler(Configuration cfg, DataWriter<Request> requestWriter, DataReader<Reply> responseReader,
         Map<Class<?>, Serializer> serializers) {
      this.cfg = cfg;
      this.requestWriter = requestWriter;
      this.responseReader = responseReader;
      this.serializers = serializers;
      
      responseReader.addSampleListener(this);

      try {
         md5 = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {  
         throw new RuntimeException(e); // Should not happen, every java has MD5
      }
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
      
      byte[] buffer = new byte[cfg.getRPCBufferSize()];
      RTPSByteBuffer bb = new RTPSByteBuffer(buffer); 
      
      for (int i = 0; i < args.length; i++) {
         Serializer serializer = serializers.get(args[i].getClass());
         serializer.serialize(args[i], bb);
      }
      byte[] requestParameters = bb.toArray();
      Call call = new Request.Call(discriminator, requestParameters);
      
      Request request = new Request(header, call);
      requestWriter.write(request);
   
      // TODO: Synchronization should be based on Request.header.sequenceNumber
      
      CountDownLatch cdl = new CountDownLatch(1);      
      int timeout = cfg.getRPCInvocationTimeout();
      exchangeMap.put(header.seqeunceNumber.getAsLong(), cdl); // Store cb to exchange map

      boolean await = cdl.await(timeout, TimeUnit.MILLISECONDS);
      Reply reply = (Reply) exchangeMap.remove(header.seqeunceNumber.getAsLong());
      if (!await) {
         throw new TimeoutException("Timeout waiting for service response");
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
         Reply reply = sample.getData();
         long seqNum = reply.header.seqeunceNumber.getAsLong();
         CountDownLatch cdl = (CountDownLatch) exchangeMap.remove(seqNum);
         if (cdl != null) { // if cdl is null, client is no longer waiting for response
            exchangeMap.put(seqNum, reply);
            cdl.countDown();
         }
      }
      
      // Clear responses, so that they won't start consuming memory
      responseReader.clear(samples);
   }

   private Integer hash(String methodName) {
         byte[] digest = md5.digest(methodName.getBytes());
         md5.reset();
         return digest[0] + (digest[1] << 8) + (digest[2] << 16) + (digest[3] << 24); 
   }
}
