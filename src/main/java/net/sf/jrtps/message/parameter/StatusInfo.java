package net.sf.jrtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.rtps.CacheChange;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StatusInfo parameter. See 9.6.3.4 StatusInfo_t (PID_STATUS_INFO) for detailed
 * description.
 * 
 * @author mcr70
 * 
 */
public class StatusInfo extends Parameter implements InlineParameter {
    private static final Logger log = LoggerFactory.getLogger(StatusInfo.class);

    private byte[] flags;

    public StatusInfo(CacheChange.Kind... changeKinds) {
        super(ParameterEnum.PID_STATUS_INFO);
        this.flags = new byte[4];

        for (CacheChange.Kind kind : changeKinds) {
            switch (kind) {
            case DISPOSE:
                this.flags[3] |= 0x1;
                break;
            case UNREGISTER:
                this.flags[3] |= 0x2;
                break;
            case WRITE:
                break; // Ignore
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
        readBytes(bb, length); // TODO: default reading. just reads to byte[] in
                               // super class.
        this.flags = getBytes();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write(flags);
    }

    /**
     * Gets the value of disposeFlag.
     * 
     * @return true, if disposeFlag is set
     */
    public boolean isDisposed() {
        return (flags[3] & 0x1) == 0x1;
    }

    /**
     * Gets the value of unregisterFlag.
     * 
     * @return true, if unregisterFlag is set
     */
    public boolean isUnregistered() {
        return (flags[3] & 0x2) == 0x2;
    }

    
    
    public List<CacheChange.Kind> getChangeKinds() {
        List<CacheChange.Kind> ckList = new LinkedList<>();
        if (isDisposed()) {
            ckList.add(CacheChange.Kind.DISPOSE);
        }
        if (isUnregistered()) {
            ckList.add(CacheChange.Kind.UNREGISTER);
        }

        return ckList;
    }

}