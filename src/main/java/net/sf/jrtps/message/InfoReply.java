package net.sf.jrtps.message;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This message is sent from an RTPS Reader to an RTPS Writer. It contains
 * explicit information on where to send a reply to the Submessages that follow
 * it within the same message.
 * 
 * see 9.4.5.9 InfoReply Submessage, 8.3.7.8 InfoReply
 * 
 * @author mcr70
 * 
 */
public class InfoReply extends SubMessage {
    private static final Logger log = LoggerFactory.getLogger(InfoReply.class);

    public static final int KIND = 0x0f;

    private List<Locator> unicastLocatorList = new LinkedList<Locator>();
    private List<Locator> multicastLocatorList = new LinkedList<Locator>();

    public InfoReply(List<Locator> unicastLocators, List<Locator> multicastLocators) {
        super(new SubMessageHeader(KIND));

        this.unicastLocatorList = unicastLocators;
        this.multicastLocatorList = multicastLocators;

        if (multicastLocatorList != null && multicastLocatorList.size() > 0) {
            header.flags |= 0x2;
        }
    }

    InfoReply(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        long numLocators = bb.read_long(); // ulong
        log.trace("Reading {}(0x{}) locators", numLocators, String.format("%08x", numLocators));
        for (int i = 0; i < numLocators; i++) {
            Locator loc = new Locator(bb);

            unicastLocatorList.add(loc);
        }

        if (multicastFlag()) {
            numLocators = bb.read_long(); // ulong
            for (int i = 0; i < numLocators; i++) {
                Locator loc = new Locator(bb);

                multicastLocatorList.add(loc);
            }
        }
    }

    /**
     * Returns the MulticastFlag. If true, message contains MulticastLocatorList
     * 
     * @return true, if message contains multicast locator
     */
    public boolean multicastFlag() {
        return (header.flags & 0x2) != 0;
    }

    /**
     * Indicates an alternative set of unicast addresses that the Writer should
     * use to reach the Readers when replying to the Submessages that follow.
     */
    public List<Locator> getUnicastLocatorList() {
        return unicastLocatorList;
    }

    /**
     * Indicates an alternative set of multicast addresses that the Writer
     * should use to reach the Readers when replying to the Submessages that
     * follow. Only present when the MulticastFlag is set.
     */
    public List<Locator> getMulticastLocatorList() {
        return multicastLocatorList;
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(unicastLocatorList.size());
        for (Locator loc : unicastLocatorList) {
            loc.writeTo(bb);
        }

        if (multicastFlag()) {
            bb.write_long(multicastLocatorList.size());
            for (Locator loc : multicastLocatorList) {
                loc.writeTo(bb);
            }
        }
    }

    public String toString() {
        return super.toString() + ", " + unicastLocatorList + ", " + multicastLocatorList;
    }
}
