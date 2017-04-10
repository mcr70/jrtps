package net.sf.jrtps.rpc;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.CDREncapsulation;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.transport.RTPSByteBuffer;

class ReplyMarshaller implements Marshaller<Reply> {
   private final int bufferSize;
   
   public ReplyMarshaller(int bufferSize) {
      this.bufferSize = bufferSize;
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
      CDREncapsulation cdrEnc = (CDREncapsulation) dEnc;
      RTPSByteBuffer bb = cdrEnc.getBuffer();

      return new Reply(bb);
   }

   @Override
   public DataEncapsulation marshall(Reply data) throws IOException {
      CDREncapsulation cdrEnc = new CDREncapsulation(bufferSize);
      RTPSByteBuffer bb = cdrEnc.getBuffer();

      data.writeTo(bb);
      
      return cdrEnc;
   }
}
