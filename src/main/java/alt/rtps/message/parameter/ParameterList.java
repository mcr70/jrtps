package alt.rtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.transport.RTPSByteBuffer;

public class ParameterList {
	private List<Parameter> params = new LinkedList<Parameter>(); 

	public ParameterList(List<Parameter> params) {
		this.params = params;
	}
	
	public void add(Parameter param) {
		params.add(param);
	}
	
	public void writeTo(RTPSByteBuffer buffer) {
		for (Parameter param: params) {
			buffer.align(4); // @see 9.4.2.11
			buffer.write_short(param.getParameterId().kind());
			
			if (true) { // TODO: Sentinel handling
				buffer.write_short(0); // length will be calculated
			
				int pos = buffer.position();
				param.writeTo(buffer); 
				int paramLength = buffer.position() - pos;
				paramLength += (paramLength % 4); // Make sure length is multiple of 4
				
				buffer.getBuffer().putShort(pos - 2, (short) paramLength);
			}
		}
		
		// TODO: last Parameter must be PID_SENTINEL
	}

	public int size() {
		return params.size();
	}
}
