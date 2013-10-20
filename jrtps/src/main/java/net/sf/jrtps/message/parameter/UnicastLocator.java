package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator_t;

public class UnicastLocator extends LocatorParameter {

	public UnicastLocator(Locator_t locator) {
		super(ParameterEnum.PID_UNICAST_LOCATOR, locator);
	}
	
	UnicastLocator() {
		super(ParameterEnum.PID_UNICAST_LOCATOR);
	}
}