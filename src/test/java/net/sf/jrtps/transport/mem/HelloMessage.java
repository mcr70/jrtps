package net.sf.jrtps.transport.mem;

import java.io.Serializable;

import net.sf.jrtps.udds.Key;
import net.sf.jrtps.udds.Type;

/**
 * HelloMessage is used for testing purposes
 * 
 * @author mcr70
 * 
 */
@Type(topicName = "Hello", typeName = "HelloMessage")
public class HelloMessage implements Serializable {
    private static final long serialVersionUID = 5427974433060817425L;

    public @Key
    int userId;
    public String message;

    public HelloMessage(int userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String toString() {
        return userId + ": " + message;
    }
}
