package net.sf.jrtps.rpc;

import net.sf.jrtps.types.Guid;

public class Request {
   RequestHeader header;
   Call call;

   static class RequestHeader {
      // SampleIdentity is made of Guid and sequenceNumber
      Guid guid;
      long seqeunceNumber; // SequenceNumber

      String serviceName;
      String instanceName;
   }
   
   static class Call {
      int discriminator; // Represents Method
      byte[] request; // Parameters marshalled
   }
}
