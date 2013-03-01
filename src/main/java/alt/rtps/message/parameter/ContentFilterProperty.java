package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class ContentFilterProperty extends Parameter {
	ContentFilterProperty() {
		super(ParameterEnum.PID_CONTENT_FILTER_PROPERTY);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}