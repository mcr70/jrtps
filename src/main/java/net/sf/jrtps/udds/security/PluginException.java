package net.sf.jrtps.udds.security;

public class PluginException extends Exception {
	private static final long serialVersionUID = 1L;

	public PluginException(String message, Exception e) {
		super(message, e);
	}

	public PluginException(String message) {
		super(message);
	}
}
