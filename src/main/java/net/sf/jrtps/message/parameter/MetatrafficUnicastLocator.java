package net.sf.jrtps.message.parameter;

import net.sf.jrtps.types.Locator;

public class MetatrafficUnicastLocator extends LocatorParameter {

    public MetatrafficUnicastLocator(Locator locator) {
        super(ParameterEnum.PID_METATRAFFIC_UNICAST_LOCATOR, locator);
    }

    MetatrafficUnicastLocator() {
        super(ParameterEnum.PID_METATRAFFIC_UNICAST_LOCATOR);
    }
}