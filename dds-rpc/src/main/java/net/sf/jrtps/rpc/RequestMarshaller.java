package net.sf.jrtps.rpc;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

class RequestMarshaller implements Marshaller<Request> {
   private final int bufferSize = 1024; // TODO: configurable
   private final Service service;

   public RequestMarshaller(Service service) {
      this.service = service;
   }
   
   @Override
   public boolean hasKey() {
      return false;
   }

   @Override
   public byte[] extractKey(Request data) {
      return null;
   }

   @Override
   public Request unmarshall(DataEncapsulation dEnc) throws IOException {
      CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
      RTPSByteBuffer bb = cdrEnc.getBuffer();

      return new Request(bb);
   }

   @Override
   public DataEncapsulation marshall(Request data) throws IOException {
      CDREncapsulation cdrEnc = new CDREncapsulation(bufferSize);
      RTPSByteBuffer bb = cdrEnc.getBuffer();

      data.writeTo(bb);
      
      return cdrEnc;
   }
}
