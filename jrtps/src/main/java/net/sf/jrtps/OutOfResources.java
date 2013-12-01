package net.sf.jrtps;

public class OutOfResources extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OutOfResources(String msg) {
		super(msg);
	}
}
