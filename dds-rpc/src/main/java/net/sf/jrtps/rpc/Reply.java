package net.sf.jrtps.rpc;

import net.sf.jrtps.rpc.Request.RequestHeader;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;

class Reply {
   ReplyHeader header;
   Return reply;

   Reply(RTPSByteBuffer bb) {
      this.header = new ReplyHeader(bb);
      this.reply = new Return(bb);
   }
   
   Reply(ReplyHeader hdr, Return reply) {
      this.header = hdr;
      this.reply = reply;
   }

   void writeTo(RTPSByteBuffer bb) {
      header.writeTo(bb);
      reply.writeTo(bb);
   }

   static class ReplyHeader {
      static final transient int REMOTE_EX_OK = 0;
      static final transient int REMOTE_EX_UNSUPPORTED = 1;
      static final transient int REMOTE_EX_INVALID_ARGUMENT = 2;
      static final transient int REMOTE_EX_OUT_OF_RESOURCES = 3;
      static final transient int REMOTE_EX_UNKNOWN_OPERATION = 4;
      static final transient int REMOTE_EX_UNKNOWN_EXCEPTION = 5;
      
      // SampleIdentity is made of Guid and sequenceNumber
      Guid guid;
      SequenceNumber seqeunceNumber; 

      int remoteExceptionCode;

      ReplyHeader(RTPSByteBuffer bb) {
         this.guid = new Guid(bb);
         this.seqeunceNumber = new SequenceNumber(bb);
         this.remoteExceptionCode = bb.read_long();
      }

      ReplyHeader(RequestHeader reqHdr, int exceptionCode) {
         this.guid = reqHdr.guid;
         this.seqeunceNumber = reqHdr.seqeunceNumber;
         this.remoteExceptionCode = exceptionCode;
      }

      void writeTo(RTPSByteBuffer bb) {
         guid.writeTo(bb);
         seqeunceNumber.writeTo(bb);
         bb.write_long(remoteExceptionCode);
      }
   }

   static class Return {
      int discriminator; // Represents Method
      byte[] result; // Result marshalled

      Return(RTPSByteBuffer bb) {
         this.discriminator = bb.read_long();
         this.result = new byte[bb.read_long()];
         
         for (int i = 0; i < result.length; i++) {
            result[i] = bb.read_octet();
         }
      }

      Return(int discriminator, byte[] result) {
         this.discriminator = discriminator;
         this.result = result;
      }

      void writeTo(RTPSByteBuffer bb) {
         bb.write_long(discriminator);
         bb.write_long(result.length);
         
         for (int i = 0; i < result.length; i++) {
            bb.write_octet(result[i]);
         }
      }
   }
}
