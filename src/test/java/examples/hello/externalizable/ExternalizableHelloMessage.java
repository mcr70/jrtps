package examples.hello.externalizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.sf.jrtps.udds.Key;

/**
 * HelloMessage is used for testing purposes
 * 
 * @author mcr70
 * 
 */
public class ExternalizableHelloMessage implements Externalizable {
    private static final long serialVersionUID = 5427974433060817425L;

    public @Key
    int userId;
    public String message;

    /**
     * Constructor used with externalization
     */
    public ExternalizableHelloMessage() {
    }

    public ExternalizableHelloMessage(int userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.userId = in.readInt();
        this.message = in.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(userId);
        out.writeUTF(message);
    }

    public String toString() {
        return userId + ": " + message;
    }
}
