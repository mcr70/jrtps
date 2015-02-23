package examples.rti.hello;

import net.sf.jrtps.udds.Type;

@Type(topicName = "Message.java", typeName = "DDS::String")
public class Hello {
    public String message;

    public Hello() {
    }

    public Hello(String msg) {
		message = msg;
    }

    public String toString() {
        return message;
    }
}
