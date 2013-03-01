package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class DefaultUnicastLocator extends LocatorParameter {
	
	public DefaultUnicastLocator(Locator_t locator) {
		super(ParameterEnum.PID_DEFAULT_UNICAST_LOCATOR, locator);
	}

	DefaultUnicastLocator() {
		super(ParameterEnum.PID_DEFAULT_UNICAST_LOCATOR);
	}
}