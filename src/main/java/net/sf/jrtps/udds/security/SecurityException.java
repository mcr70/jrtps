package net.sf.jrtps.udds.security;

/**
 * An exception thrown by security plugins
 * @author mcr70
 */
public class SecurityException extends Exception {
	private static final long serialVersionUID = 1L;
	private String msg;

	public SecurityException() {
		super();
	}
	public SecurityException(String msg) {
		super(msg);
	}
	public SecurityException(Throwable t) {
		super(t);
	}
	public SecurityException(String msg, Throwable t) {
		super(t);
		this.msg = msg;
	}
	
	@Override
	public String getMessage() {
		return msg;
	}
}
