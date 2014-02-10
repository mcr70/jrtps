package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class DefaultUnicastLocator extends LocatorParameter {

    public DefaultUnicastLocator(Locator locator) {
        super(ParameterEnum.PID_DEFAULT_UNICAST_LOCATOR, locator);
    }

    DefaultUnicastLocator() {
        super(ParameterEnum.PID_DEFAULT_UNICAST_LOCATOR);
    }
}