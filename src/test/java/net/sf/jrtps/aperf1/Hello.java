package net.sf.jrtps.aperf1;

import net.sf.jrtps.udds.Type;

@Type(topicName = "Hello, World", typeName = "DDS::String")
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
