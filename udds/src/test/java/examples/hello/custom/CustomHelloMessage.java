package examples.hello.custom;

/**
 * HelloMessage is used for testing purposes
 * 
 * @author mcr70
 * 
 */
public class CustomHelloMessage {
    public int userID;
    public String message;

    public CustomHelloMessage() {
    }

    public CustomHelloMessage(int userId, String message) {
        this.userID = userId;
        this.message = message;
    }

    public String toString() {
        return userID + ": " + message;
    }
}
