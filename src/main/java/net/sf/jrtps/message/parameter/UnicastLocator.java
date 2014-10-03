package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class UnicastLocator extends LocatorParameter {

    public UnicastLocator(Locator locator) {
        super(ParameterId.PID_UNICAST_LOCATOR, locator);
    }

    UnicastLocator() {
        super(ParameterId.PID_UNICAST_LOCATOR);
    }
}