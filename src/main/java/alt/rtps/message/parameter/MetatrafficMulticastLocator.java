package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class MetatrafficMulticastLocator extends Locator {

	public MetatrafficMulticastLocator(Locator_t locator) {
		this();
		this.locator = locator;
	}

	MetatrafficMulticastLocator() {
		super(ParameterEnum.PID_METATRAFFIC_MULTICAST_LOCATOR);
	}
}