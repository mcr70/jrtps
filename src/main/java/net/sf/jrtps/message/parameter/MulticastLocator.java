package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator_t;

public class MulticastLocator extends LocatorParameter {
	MulticastLocator() {
		super(ParameterEnum.PID_MULTICAST_LOCATOR);
	}

	public MulticastLocator(Locator_t locator) {
		super(ParameterEnum.PID_MULTICAST_LOCATOR, locator);
	}
}