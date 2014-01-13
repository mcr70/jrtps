package net.sf.jrtps;

/**
 * InconsistentPolicy exception. This exception gets thrown by setPolicy method,
 * if the policy being set is somehow inconsistent.
 * 
 * @see net.sf.jrtps.QualityOfService#setPolicy
 * @author mcr70
 */
public class InconsistentPolicy extends Exception {
	private static final long serialVersionUID = 1L;

	public InconsistentPolicy(String msg) {
		super(msg);
	}
}