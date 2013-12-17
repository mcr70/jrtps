package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class MulticastLocator extends LocatorParameter {
	MulticastLocator() {
		super(ParameterEnum.PID_MULTICAST_LOCATOR);
	}

	public MulticastLocator(Locator locator) {
		super(ParameterEnum.PID_MULTICAST_LOCATOR, locator);
	}
}