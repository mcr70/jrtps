package net.sf.jrtps.rpc;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;

public class Request {
   private RequestHeader header;
   private Call call;

   public Request(RequestHeader header, Call call) {
      this.header = header;
      this.call = call;
   }

   Request(RTPSByteBuffer bb) {
      this.header = new RequestHeader(bb);
      this.call = new Call(bb);
   }

   public void writeTo(RTPSByteBuffer bb) {
      header.writeTo(bb);
      call.writeTo(bb);
   }

   static class RequestHeader {
      // SampleIdentity is made of Guid and sequenceNumber
      Guid guid;
      SequenceNumber seqeunceNumber; 
      
      String serviceName;
      String instanceName;
      
      RequestHeader(Guid guid, SequenceNumber seqNum, String serviceName, String instanceName) {
         this.guid = guid;
         this.seqeunceNumber = seqNum;
         this.serviceName = serviceName;
         this.instanceName = instanceName;
      }

      RequestHeader(RTPSByteBuffer bb) {
         this.guid = new Guid(bb);
         this.seqeunceNumber = new SequenceNumber(bb);
         this.serviceName = bb.read_string();
         this.instanceName = bb.read_string();
      }

      public void writeTo(RTPSByteBuffer bb) {
         guid.writeTo(bb);
         seqeunceNumber.writeTo(bb);
         bb.write_string(serviceName);
         bb.write_string(instanceName);
      }
   }
   
   static class Call {
      int discriminator; // Represents Method
      byte[] request; // Parameters marshalled
      
      Call(int discriminator, byte[] requestParameters) {
         this.discriminator = discriminator;
         this.request = requestParameters;
      }

      public Call(RTPSByteBuffer bb) {
         this.discriminator = bb.read_long();
         this.request = new byte[bb.read_long()];
         
         for (int i = 0; i < request.length; i++) {
            request[i] = bb.read_octet();
         }
      }

      public void writeTo(RTPSByteBuffer bb) {
         bb.write_long(discriminator);
         bb.write_long(request.length);
         
         for (int i = 0; i < request.length; i++) {
            bb.write_octet(request[i]);
         }
      }
   }

}
