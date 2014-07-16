package examples.opendds.messenger;

import net.sf.jrtps.udds.Type;

@Type(topicName = "Movie Discussion List", typeName = "IDL:Messenger/MessageTypeSupport:1.0")
public class Message {
    String from;
    String subject;
    int subject_id;
    String text;
    int count;

    public Message() {
    }

    public Message(String from, String subject, int subject_id, String text, int count) {
        this.from = from;
        this.subject = subject;
        this.subject_id = subject_id;
        this.text = text;
        this.count = count;
    }

    public String toString() {
        return from + ", " + subject + ", " + subject_id + ", " + text + ", " + count;
    }
}
