package net.sf.jrtps.rpc;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.DataEncapsulation;

class RequestMarshaller implements Marshaller<Request> {
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
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DataEncapsulation marshall(Request data) throws IOException {
      // TODO Auto-generated method stub
      return null;
   }
}
