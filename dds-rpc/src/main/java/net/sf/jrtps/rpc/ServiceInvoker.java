package net.sf.jrtps.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.BufferUnderflowException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.rpc.Reply.ReplyHeader;
import net.sf.jrtps.rpc.Reply.Return;
import net.sf.jrtps.rpc.Request.Call;
import net.sf.jrtps.rpc.Request.RequestHeader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.transport.RTPSByteBuffer;
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

   private final Map<Integer, Method> discriminatorMap = new HashMap<>();
   private final Service service;
   private final DataReader<Request> requestReader;
   private final DataWriter<Reply> replyWriter;

   private Map<Class<?>, Serializer> serializers;

   private final Configuration cfg;

   ServiceInvoker(Configuration cfg, Map<Class<?>, Serializer> serializers, DataReader<Request> requestReader, 
         DataWriter<Reply> replyWriter, Service service) {
      this.cfg = cfg;
      this.serializers = serializers;
      this.requestReader = requestReader;
      this.replyWriter = replyWriter;
      this.service = service;
      
      Class<?> srvClass = service.getClass();
      Class<?>[] interfaces = srvClass.getInterfaces();
      
      // dds-rpc spec does not allow overloading of methods
      Set<String> methodNames = new HashSet<>();
      for (Class<?> i: interfaces) {
         if (Service.class.isAssignableFrom(i)) {
            logger.debug("Adding declared methods of {} to service {}", i.getName(), srvClass.getName());
            for (Method m: i.getDeclaredMethods()) {
               Method prev = discriminatorMap.put(hash(m.getName()), m);
               if (prev != null) {
                  logger.warn("DDS-RPC specification does not allow method overloading, discarding {}", prev);
               }
               
               methodNames.add(m.getName());
            }            
         }
         else {
            logger.debug("Skipping methods of {}, as it is not derived from {}", 
                  i.getName(), Service.class);
         }
      }
      
      logger.debug("{} is serving methods: {}", srvClass.getName(), methodNames);
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

   @Override
   public void onSamples(List<Sample<Request>> samples) {      
      for (Sample<Request> sample: samples) {
         logger.debug("Got request {}", sample.getData());
         invoke(sample.getData());
      }
      
      // Clear samples from the DataReader, so that they won't
      // grow infinitely
      requestReader.clear(samples);
   }

   private void invoke(Request data) {
      RequestHeader header = data.getHeader();
      Call call = data.getCall();
      Method m = discriminatorMap.get(call.discriminator);

      byte[] result;
      int status = ReplyHeader.REMOTE_EX_OK;
      if (m == null) {
         logger.warn("Did not find a method for discriminator {}", call.discriminator);
         result = new byte[0];
         status = ReplyHeader.REMOTE_EX_UNKNOWN_OPERATION;
      }
      else {
         try {
            Object[] args = getArguments(m, call.request);
            Object o = m.invoke(service, args);

            result = toBytes(m, o);
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Failed to invoke {}", m.getName(), e);
            result = new byte[0];
            status = ReplyHeader.REMOTE_EX_UNKNOWN_EXCEPTION;
         } catch (NoSuchSerializer | SerializationException | BufferUnderflowException e) {
            logger.error("Failed to serialize arguments or return type for {}", m.getName(), e);
            result = new byte[0];
            status = ReplyHeader.REMOTE_EX_INVALID_ARGUMENT;
         }
      }
      
      writeReply(call.discriminator, status, header, result);
   }

   private void writeReply(int discriminator, int status, RequestHeader reqHdr, byte[] result) {
      ReplyHeader hdr = new ReplyHeader(reqHdr, status);
      Return ret = new Return(discriminator, result);

      Reply sample = new Reply(hdr, ret);
      replyWriter.write(sample);
   }

   private byte[] toBytes(Method m, Object result) throws NoSuchSerializer, SerializationException {
      Class<?> returnType = m.getReturnType();
      
      if (void.class.equals(returnType)) {
         return new byte[0];
      }
      
      Serializer serializer = serializers.get(returnType);
      if (serializer == null) {
         logger.error("No Serializer found for {}", returnType.getName());
         throw new NoSuchSerializer(returnType);
      }
      
      byte[] buffer = new byte[cfg.getRPCBufferSize()];
      RTPSByteBuffer bb = new RTPSByteBuffer(buffer);
      serializer.serialize(result, bb);
      
      return bb.toArray();
   }

   private Object[] getArguments(Method method, byte[] bytes) throws SerializationException, NoSuchSerializer {
      Parameter[] params = method.getParameters();
      Object[] args = new Object[params.length];
      RTPSByteBuffer bb = new RTPSByteBuffer(bytes);
      
      for (int i = 0; i < args.length; i++) {
         Class<?> type = params[i].getType();
         Serializer serializer = serializers.get(type);
         if (serializer == null) {
            throw new NoSuchSerializer(type);
         }
         args[i] = serializer.deSerialize(type, bb);
      }
      
      return args;
   }
}
