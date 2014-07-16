package examples.ospl.hello;

import net.sf.jrtps.udds.Type;

@Type(topicName = "HelloWorldData_Msg", typeName = "HelloWorldData::Msg")
public class Msg {
    int userID;
    String message;

    public Msg() {
    }

    public Msg(int userID, String message) {
        this.userID = userID;
        this.message = message;
    }

    public String toString() {
        return userID + ", " + message;
    }
}
