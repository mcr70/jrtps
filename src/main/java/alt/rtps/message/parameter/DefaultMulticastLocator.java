package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class DefaultMulticastLocator extends Locator {

	public DefaultMulticastLocator(Locator_t locator) {
		this();
		this.locator = locator;
	}

	DefaultMulticastLocator() {
		super(ParameterEnum.PID_DEFAULT_MULTICAST_LOCATOR);
	}
}