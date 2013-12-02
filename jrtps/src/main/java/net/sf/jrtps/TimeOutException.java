package net.sf.jrtps;

public class TimeOutException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TimeOutException(String message) {
		super(message);
	}
}
