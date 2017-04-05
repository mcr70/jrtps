package net.sf.jrtps.rpc;

import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.SequenceNumber;

class Reply {
   ReplyHeader header;
   Return reply;

   
   static class ReplyHeader {
      public static final transient int REMOTE_EX_OK = 0;
      public static final transient int REMOTE_EX_UNSUPPORTED = 1;
      public static final transient int REMOTE_EX_INVALID_ARGUMENT = 2;
      public static final transient int REMOTE_EX_OUT_OF_RESOURCES = 3;
      public static final transient int REMOTE_EX_UNKNOWN_OPERATION = 4;
      public static final transient int REMOTE_EX_UNKNOWN_EXCEPTION = 5;
      
      // SampleIdentity is made of Guid and sequenceNumber
      Guid guid;
      SequenceNumber seqeunceNumber; 

      int remoteExceptionCode;
   }

   static class Return {
      int discriminator; // Represents Method
      byte[] result; // Result marshalled
   }
}
