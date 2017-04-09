package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class DefaultMulticastLocator extends LocatorParameter {

    public DefaultMulticastLocator(Locator locator) {
        super(ParameterId.PID_DEFAULT_MULTICAST_LOCATOR, locator);
    }

    DefaultMulticastLocator() {
        super(ParameterId.PID_DEFAULT_MULTICAST_LOCATOR);
    }
}