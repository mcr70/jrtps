package alt.rtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.transport.RTPSByteBuffer;

public class ParameterList {
	private List<Parameter> params = new LinkedList<Parameter>(); 

	public ParameterList() {
	}
	
	public ParameterList(RTPSByteBuffer bb) {
		while (true) {
			bb.align(4);
			Parameter param = ParameterFactory.readParameter(bb);		
			params.add(param);
			
			if (param.getParameterId() == ParameterEnum.PID_SENTINEL) {
				break; // TODO: Add some control token to CDRInputStream that counts bytes read and 
				       //       fails if expected_read_count+1 is reached 
			}
		}
	}
	
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

	public List<Parameter> getParameters() {
		return params;
	}
	
	public int size() {
		return params.size();
	}
}
