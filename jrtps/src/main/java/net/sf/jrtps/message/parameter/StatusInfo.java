package net.sf.jrtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.ChangeKind;
import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * StatusInfo parameter. 
 * See 9.6.3.4 StatusInfo_t (PID_STATUS_INFO) for detailed description.
 * 
 * @author mcr70
 *
 */
public class StatusInfo extends Parameter {
	private static final Logger log = LoggerFactory.getLogger(StatusInfo.class);

	private byte[] flags;

	public StatusInfo(ChangeKind ... changeKinds) {
		super(ParameterEnum.PID_STATUS_INFO);
		this.flags = new byte[4];

		for (ChangeKind kind : changeKinds) {
			switch(kind) {
			case DISPOSE: this.flags[3] |= 0x1; break;
			case UNREGISTER: this.flags[3] |= 0x2; break;
			case WRITE: break; // Ignore
			default:
				log.warn("{} not handled", kind);
				break;
			}
		}
	}
	
	StatusInfo() {
		super(ParameterEnum.PID_STATUS_INFO);
	}


	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
		this.flags = getBytes();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write(flags);
	}
	
	public boolean isDisposed() {
		return (flags[3] & 0x1) == 0x1;
	}
	
	public boolean isUnregistered() {
		return (flags[3] & 0x2) == 0x2;
	}

	public List<ChangeKind> getChangeKinds() {
		List<ChangeKind> ckList = new LinkedList<>();
		if (isDisposed()) {
			ckList.add(ChangeKind.DISPOSE);
		}
		if (isUnregistered()) {
			ckList.add(ChangeKind.UNREGISTER);
		}
		
		return ckList;
	}

}