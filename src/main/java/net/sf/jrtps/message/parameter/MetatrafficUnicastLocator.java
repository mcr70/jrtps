package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class MetatrafficUnicastLocator extends LocatorParameter {

    public MetatrafficUnicastLocator(Locator locator) {
        super(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR, locator);
    }

    MetatrafficUnicastLocator() {
        super(ParameterId.PID_METATRAFFIC_UNICAST_LOCATOR);
    }
}