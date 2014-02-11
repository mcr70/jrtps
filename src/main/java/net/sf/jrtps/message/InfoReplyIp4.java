package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.LocatorUDPv4_t;

/**
 * The InfoReplyIp4 Submessage is an additional Submessage introduced by the UDP
 * PSM. Its use and interpretation are identical to those of an InfoReply
 * Submessage containing a single unicast and possibly a single multicast
 * locator, both of kind LOCATOR_KIND_UDPv4. It is provided for efficiency
 * reasons and can be used instead of the InfoReply Submessage to provide a more
 * compact representation.
 * 
 */
public class InfoReplyIp4 extends SubMessage {
    public static final int KIND = 0x0d;

    private LocatorUDPv4_t unicastLocator;
    private LocatorUDPv4_t multicastLocator;

    public InfoReplyIp4(LocatorUDPv4_t unicastLocator, LocatorUDPv4_t multicastLocator) {
        super(new SubMessageHeader(KIND));

        this.unicastLocator = unicastLocator;
        this.multicastLocator = multicastLocator;

        if (multicastLocator != null) {
            header.flags |= 2;
        }
    }

    InfoReplyIp4(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        readMessage(bb);
    }

    /**
     * Returns the MulticastFlag. If true, message contains MulticastLocator
     * 
     * @return true if message contains MulticastLocator
     */
    public boolean multicastFlag() {
        return (header.flags & 0x2) != 0;
    }

    /**
     * Gets the unicast locator, if present
     * 
     * @return {@link LocatorUDPv4_t} or null if no unicast locator is present
     */
    public LocatorUDPv4_t getUnicastLocator() {
        return unicastLocator;
    }

    /**
     * Gets the multicast locator, if present.
     * 
     * @return {@link LocatorUDPv4_t} or null if no multicast locator is present
     */
    public LocatorUDPv4_t getMulticastLocator() {
        return multicastLocator;
    }

    private void readMessage(RTPSByteBuffer bb) {
        unicastLocator = new LocatorUDPv4_t(bb);

        if (multicastFlag()) {
            multicastLocator = new LocatorUDPv4_t(bb);
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        unicastLocator.writeTo(buffer);
        multicastLocator.writeTo(buffer);
    }
}
