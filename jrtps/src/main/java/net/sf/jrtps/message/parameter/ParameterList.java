package net.sf.jrtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterList {
	Logger log = LoggerFactory.getLogger(ParameterList.class);

	private List<Parameter> params = new LinkedList<Parameter>(); 

	public ParameterList() {
	}

	public ParameterList(RTPSByteBuffer bb) {
		log.trace("Reading ParameterList from buffer");
		while (true) {
			int pos1 = bb.getBuffer().position();

			Parameter param = ParameterFactory.readParameter(bb);		
			params.add(param);
			int length = bb.getBuffer().position() - pos1;

			log.trace("Read Parameter {}, length {} from position {}", param, length, pos1);

			if (param.getParameterId() == ParameterEnum.PID_SENTINEL) {
				break; // TODO: Add some control token to CDRInputStream that counts bytes read and 
				//       fails if expected_read_count+1 is reached 
			}
		}
	}


	public void add(Parameter param) {
		params.add(param);
	}

	public void writeTo(RTPSByteBuffer buffer) {
		buffer.align(4); // @see 9.4.2.11

		params.add(new Sentinel()); // Sentinel must be the last Parameter
		for (Parameter param: params) {
			buffer.write_short(param.getParameterId().kind());
			buffer.write_short(0); // length will be calculated

			int pos = buffer.position();
			param.writeTo(buffer); 

			buffer.align(4); // Make sure length is multiple of 4 & align for next param

			int paramLength = buffer.position() - pos;
			buffer.getBuffer().putShort(pos - 2, (short) paramLength);
		}

		// TODO: last Parameter must be PID_SENTINEL
	}

	public List<Parameter> getParameters() {
		return params;
	}

	public int size() {
		return params.size();
	}

	public Parameter getParameter(ParameterEnum pid) {
		for (Parameter p : params) {
			if (pid.equals(p.getParameterId())) {
				return p;
			}
		}
		return null;
	}
}
