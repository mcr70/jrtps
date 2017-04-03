package net.sf.jrtps.rpc;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.DataEncapsulation;

class ReplyMarshaller implements Marshaller<Reply> {
   private final Service service;

   public ReplyMarshaller(Service service) {
      this.service = service;
   }
   
   @Override
   public boolean hasKey() {
      return false;
   }

   @Override
   public byte[] extractKey(Reply data) {
      return null;
   }

   @Override
   public Reply unmarshall(DataEncapsulation dEnc) throws IOException {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DataEncapsulation marshall(Reply data) throws IOException {
      // TODO Auto-generated method stub
      return null;
   }
}
