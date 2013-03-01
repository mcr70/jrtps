package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class MetatrafficUnicastLocator extends LocatorParameter {

	public MetatrafficUnicastLocator(Locator_t locator) {
		super(ParameterEnum.PID_METATRAFFIC_UNICAST_LOCATOR, locator);
	}

	MetatrafficUnicastLocator() {
		super(ParameterEnum.PID_METATRAFFIC_UNICAST_LOCATOR);
	}
}