package net.sf.jrtps;

/**
 * TimeOutException.
 * 
 * @author mcr70
 */
public class TimeOutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TimeOutException(String message) {
        super(message);
    }
}
