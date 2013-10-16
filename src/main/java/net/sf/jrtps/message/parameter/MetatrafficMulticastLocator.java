package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator_t;

public class MetatrafficMulticastLocator extends LocatorParameter {

	public MetatrafficMulticastLocator(Locator_t locator) {
		super(ParameterEnum.PID_METATRAFFIC_MULTICAST_LOCATOR, locator);
	}

	MetatrafficMulticastLocator() {
		super(ParameterEnum.PID_METATRAFFIC_MULTICAST_LOCATOR);
	}
}