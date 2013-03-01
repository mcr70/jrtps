package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class DefaultMulticastLocator extends LocatorParameter {

	public DefaultMulticastLocator(Locator_t locator) {
		super(ParameterEnum.PID_DEFAULT_MULTICAST_LOCATOR, locator);
	}

	DefaultMulticastLocator() {
		super(ParameterEnum.PID_DEFAULT_MULTICAST_LOCATOR);
	}
}