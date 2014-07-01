package net.sf.jrtps.message.parameter;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.rtps.ChangeKind;
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
public class StatusInfo extends Parameter implements InlineQoS {
    private static final Logger log = LoggerFactory.getLogger(StatusInfo.class);

    private byte[] flags;

    public StatusInfo(ChangeKind... changeKinds) {
        super(ParameterEnum.PID_STATUS_INFO);
        this.flags = new byte[4];

        for (ChangeKind kind : changeKinds) {
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
        readBytes(bb, length);
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

    /**
     * Gets the Kind of change represented by this StatusInfo. 
     * Note, that only one kind is returned, even if there are multiple flags set on
     * StatusInfo. Most significant change is DISPOSE, then UNREGISTER, and finally WRITE.
     * 
     * @return change kind
     */
    public ChangeKind getKind() {
        if (isDisposed()) {
            return ChangeKind.DISPOSE;
        }
        else if (isUnregistered()) {
            return ChangeKind.UNREGISTER;
        }
        else {
            return ChangeKind.WRITE;
        }
    }
}