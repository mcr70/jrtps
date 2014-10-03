package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class MulticastLocator extends LocatorParameter {
    MulticastLocator() {
        super(ParameterId.PID_MULTICAST_LOCATOR);
    }

    public MulticastLocator(Locator locator) {
        super(ParameterId.PID_MULTICAST_LOCATOR, locator);
    }
}