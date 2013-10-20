package net.sf.jrtps.udds.hello;
import java.io.Serializable;


/**
 * HelloMessage is used for testing purposes
 * 
 * @author mcr70
 *
 */
public class HelloMessage implements Serializable {
	// Should we have some annotation for keys, like
	// @Key
	public int userId;
	public String message;

	public HelloMessage(int userId, String message) {
		this.userId = userId;
		this.message = message;
	}
}
