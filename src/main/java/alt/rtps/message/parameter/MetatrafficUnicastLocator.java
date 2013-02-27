package alt.rtps.message.parameter;

import alt.rtps.types.Locator_t;

public class MetatrafficUnicastLocator extends Locator {

	public MetatrafficUnicastLocator(Locator_t locator) {
		this();
		this.locator = locator;
	}

	MetatrafficUnicastLocator() {
		super(ParameterEnum.PID_METATRAFFIC_UNICAST_LOCATOR);
	}
}