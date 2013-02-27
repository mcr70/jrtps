package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class DefaultUnicastLocator extends Locator {
	
	public DefaultUnicastLocator(Locator_t locator) {
		this();
		this.locator = locator;
	}

	DefaultUnicastLocator() {
		super(ParameterEnum.PID_DEFAULT_UNICAST_LOCATOR);
	}
}