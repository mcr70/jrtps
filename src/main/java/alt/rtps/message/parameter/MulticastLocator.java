package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class MulticastLocator extends LocatorParameter {
	MulticastLocator() {
		super(ParameterEnum.PID_MULTICAST_LOCATOR);
	}

	public MulticastLocator(Locator_t locator) {
		super(ParameterEnum.PID_MULTICAST_LOCATOR, locator);
	}
}