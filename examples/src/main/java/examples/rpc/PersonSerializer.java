package examples.rpc;

import net.sf.jrtps.rpc.SerializationException;
import net.sf.jrtps.rpc.Serializer;
import net.sf.jrtps.transport.RTPSByteBuffer;

public class PersonSerializer implements Serializer<Person> {
   @Override
   public void serialize(Person value, RTPSByteBuffer bb) throws SerializationException {
      bb.write_string(value.name);
      bb.write_string(value.address);
   }

   @Override
   public Person deSerialize(Class<Person> type, RTPSByteBuffer bb) throws SerializationException {
      Person p = new Person();
      p.name = bb.read_string();
      p.address = bb.read_string();
      
      return p;
   }
}
