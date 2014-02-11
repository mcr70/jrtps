package examples.opendds.messenger;

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
