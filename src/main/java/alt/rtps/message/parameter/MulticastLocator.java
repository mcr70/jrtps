package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Locator_t;

public class MulticastLocator extends Parameter {
	private Locator_t locator;

	MulticastLocator() {
		super(ParameterEnum.PID_MULTICAST_LOCATOR);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.locator = new Locator_t(bb);
	}

	public Locator_t getLocator() {
		return locator;
	}

	public String toString() {
		return super.toString() + ": " + getLocator();
	}		
}