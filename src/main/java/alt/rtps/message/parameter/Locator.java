package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Locator_t;

public class Locator extends Parameter {

	protected Locator_t locator;

	protected Locator(ParameterEnum id) {
		super(id);
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.locator = new Locator_t(bb);
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		locator.writeTo(buffer);
	}

	public Locator_t getLocator() {
		return locator;
	}
	
	public String toString() {
		return super.toString() + ": " + getLocator();
	}		
}
