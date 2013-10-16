package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator_t;

public class DefaultMulticastLocator extends LocatorParameter {

	public DefaultMulticastLocator(Locator_t locator) {
		super(ParameterEnum.PID_DEFAULT_MULTICAST_LOCATOR, locator);
	}

	DefaultMulticastLocator() {
		super(ParameterEnum.PID_DEFAULT_MULTICAST_LOCATOR);
	}
}