package examples.ospl.hello;

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
