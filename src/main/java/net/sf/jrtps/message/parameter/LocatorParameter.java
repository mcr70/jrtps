package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Locator_t;

public abstract class LocatorParameter extends Parameter {
	private Locator_t locator;

	/**
	 * 
	 * @param pe Must be one to define a locator. No check is made.
	 */
	protected LocatorParameter(ParameterEnum pe) {
		super(pe);
	}
	/**
	 * 
	 * @param pe Must be one to define a locator. No check is made.
	 */
	protected LocatorParameter(ParameterEnum pe, Locator_t locator) {
		super(pe);
		this.locator = locator;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.locator = new Locator_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		locator.writeTo(bb);
	}
	
	public Locator_t getLocator() {
		return locator;
	}

	public String toString() {
		return super.toString() + ": " + getLocator();
	}		
}
