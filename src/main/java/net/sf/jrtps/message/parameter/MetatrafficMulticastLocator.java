package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class MetatrafficMulticastLocator extends LocatorParameter {

    public MetatrafficMulticastLocator(Locator locator) {
        super(ParameterId.PID_METATRAFFIC_MULTICAST_LOCATOR, locator);
    }

    MetatrafficMulticastLocator() {
        super(ParameterId.PID_METATRAFFIC_MULTICAST_LOCATOR);
    }
}