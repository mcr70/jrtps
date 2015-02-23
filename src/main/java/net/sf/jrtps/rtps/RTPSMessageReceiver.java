package net.sf.jrtps.rtps;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Gap;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.IllegalMessageException;
import net.sf.jrtps.message.InfoDestination;
import net.sf.jrtps.message.InfoReply;
import net.sf.jrtps.message.InfoReplyIp4;
import net.sf.jrtps.message.InfoSource;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.SecureSubMessage;
import net.sf.jrtps.message.SubMessage;
import net.sf.jrtps.message.SubMessage.Kind;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.LocatorUDPv4_t;
import net.sf.jrtps.types.Time;
import net.sf.jrtps.udds.security.CryptoPlugin;
import net.sf.jrtps.udds.security.SecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSMessageReceiver is a consumer to BlockingQueue<byte[]>. A network
 * receiver produces byte arrays into this queue. These byte[] are parsed into
 * RTPS messages by this class.
 * <p>
 * 
 * Successfully parsed messages are split into submessages, which are passed to
 * corresponding RTPS reader entities.
 * 
 * @see RTPSReader
 * @author mcr70
 */
class RTPSMessageReceiver implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RTPSMessageReceiver.class);

    private final RTPSParticipant participant;
    private final BlockingQueue<byte[]> queue;
	private final CryptoPlugin cryptoPlugin;

    private Set<GuidPrefix> ignoredParticipants = new HashSet<>();
    private boolean running = true;

    RTPSMessageReceiver(CryptoPlugin cryptoPlugin, RTPSParticipant p, BlockingQueue<byte[]> queue, Configuration config) {
        this.participant = p;
        this.queue = queue;
        this.cryptoPlugin = cryptoPlugin;
    }

    @Override
    public void run() {
        while (running) {
        	byte[] bytes = null;
        	try {
                // NOTE: We can have only one MessageReceiver. pending samples
                // concept relies on it.
                // NOTE2: pending samples concept has changed. Check this.
                bytes = queue.take();
                if (running) {
                    Message msg = new Message(new RTPSByteBuffer(bytes));
                    logger.debug("Parsed RTPS message {}", msg);

                    handleMessage(msg);
                }
            } catch(InterruptedException e) {
                running = false;
            } catch(IllegalMessageException ime) {
            	logger.warn("Got Illegal message: {}, enable trace to see stacktrace", ime.getMessage());
            	logger.trace("Illegal message", ime);
            } catch(Exception e) {
                logger.warn("Got unexpected exception during Message handling", e);
            }
        }

        logger.debug("RTPSMessageReceiver exiting");
    }

    /**
     * Handles incoming Message. Each sub message is transferred to
     * corresponding reader.
     * 
     * @param msg
     */
    private void handleMessage(Message msg) {
        int msgId = msg.hashCode();
        Time timestamp = null;
        GuidPrefix destGuidPrefix = participant.getGuid().getPrefix();
        boolean destinationThisParticipant = true;

        GuidPrefix sourceGuidPrefix = msg.getHeader().getGuidPrefix();
        GuidPrefix myPrefix = participant.getGuid().getPrefix(); 

        if (myPrefix.equals(sourceGuidPrefix)) {
            logger.debug("Discarding message originating from this participant");
            return;
        }

        Set<RTPSReader<?>> dataReceivers = new HashSet<>();
        List<SubMessage> subMessages = msg.getSubMessages();

        for (SubMessage subMsg : subMessages) {
        	if (subMsg.getKind() == Kind.SECURESUBMSG) {
        		SecureSubMessage ssm = (SecureSubMessage) subMsg;
        		if (ssm.singleSubMessageFlag()) {
        			//subMsg = cryptoPlugin.decodeSubMessage(ssm);
        			logger.warn("Decoding of submessage not implemented. Discarding it.");
        			continue;
        		}
        		else {
            		try {
            			handleMessage(cryptoPlugin.decodeMessage(sourceGuidPrefix, ssm));
					} catch (SecurityException e) {
						logger.error("Failed to decode message", e);
					}

            		continue;
        		}
        	}
        	
        	switch (subMsg.getKind()) {
            case ACKNACK:
                if (!destinationThisParticipant) {
                    continue;
                }

                if (ignoredParticipants.contains(sourceGuidPrefix)) {
                    continue;
                }

                handleAckNack(sourceGuidPrefix, (AckNack) subMsg);
                break;
            case DATA:
                if (!destinationThisParticipant) {
                    continue;
                }

                if (ignoredParticipants.contains(sourceGuidPrefix)) {
                    continue;
                }

                try {
                    Data data = (Data) subMsg;
                    RTPSReader<?> r = participant.getReader(data.getReaderId(), sourceGuidPrefix, data.getWriterId());

                    if (r != null) {
                        if (dataReceivers.add(r)) {
                            r.startMessageProcessing(msgId);
                        }
                        r.onData(msgId, sourceGuidPrefix, data, timestamp);
                    }
                    else {
                        logger.warn("No reader({}) was matched with {} to handle Data message from {}", 
                                data.getReaderId(), new Guid(sourceGuidPrefix, data.getWriterId()),
                                participant.getReaders());
                    }
                } catch (IOException ioe) {
                    logger.warn("Failed to handle data", ioe);
                }
                break;
            case HEARTBEAT:
                if (!destinationThisParticipant) {
                    continue;
                }

                if (ignoredParticipants.contains(sourceGuidPrefix)) {
                    continue;
                }

                handleHeartbeat(sourceGuidPrefix, (Heartbeat) subMsg);
                break;
            case INFODESTINATION:
                destGuidPrefix = ((InfoDestination) subMsg).getGuidPrefix();
                destinationThisParticipant = participant.getGuid().getPrefix().equals(destGuidPrefix) 
                        || GuidPrefix.GUIDPREFIX_UNKNOWN.equals(destGuidPrefix);

                break;
            case INFOSOURCE:
                sourceGuidPrefix = ((InfoSource) subMsg).getGuidPrefix();
                break;
            case INFOTIMESTAMP:
                timestamp = ((InfoTimestamp) subMsg).getTimeStamp();
                break;
            case INFOREPLY: // TODO: HB, AC & DATA needs to use replyLocators,
                // if present
                InfoReply ir = (InfoReply) subMsg;
                List<Locator> replyLocators = ir.getUnicastLocatorList();
                if (ir.multicastFlag()) {
                    replyLocators.addAll(ir.getMulticastLocatorList());
                }
                logger.warn("InfoReply not handled");
                break;
            case INFOREPLYIP4: // TODO: HB, AC & DATA needs to use these
                // Locators, if present
                InfoReplyIp4 ir4 = (InfoReplyIp4) subMsg;
                LocatorUDPv4_t unicastLocator = ir4.getUnicastLocator();
                if (ir4.multicastFlag()) {
                    LocatorUDPv4_t multicastLocator = ir4.getMulticastLocator();
                }
                logger.warn("InfoReplyIp4 not handled");
                break;
            case GAP:
                if (!destinationThisParticipant) {
                    continue;
                }

                handleGap(sourceGuidPrefix, (Gap) subMsg);
                break;

            default:
                logger.warn("SubMessage not handled: {}", subMsg);
            }
        }

        logger.trace("Releasing samples for {} readers", dataReceivers.size());
        for (RTPSReader<?> reader : dataReceivers) {
            reader.stopMessageProcessing(msgId);
        }
    }


	private void handleAckNack(GuidPrefix sourceGuidPrefix, AckNack ackNack) {
        RTPSWriter<?> writer = participant.getWriter(ackNack.getWriterId(), sourceGuidPrefix, ackNack.getReaderId());

        if (writer != null) {
            writer.onAckNack(sourceGuidPrefix, ackNack);
        } else {
            logger.debug("No Writer({}) to handle AckNack from {}", ackNack.getWriterId(), ackNack.getReaderId());
        }
    }

    private void handleGap(GuidPrefix sourceGuidPrefix, Gap gap) {
        RTPSReader<?> reader = participant.getReader(gap.getReaderId(), sourceGuidPrefix, gap.getWriterId());
        if (reader != null) {
        	reader.onGap(sourceGuidPrefix, gap);
        } else {
            logger.debug("No Reader({}) to handle Gap from {}", gap.getReaderId(), gap.getWriterId());
        }
    }

    private void handleHeartbeat(GuidPrefix sourceGuidPrefix, Heartbeat hb) {
        RTPSReader<?> reader = participant.getReader(hb.getReaderId(), sourceGuidPrefix, hb.getWriterId());

        if (reader != null) {
            reader.onHeartbeat(sourceGuidPrefix, hb);
        } else {
            logger.debug("No Reader({}) to handle Heartbeat from {}", hb.getReaderId(), hb.getWriterId());
        }
    }

	private SubMessage extractSubMessage(SecureSubMessage subMsg) {
    	logger.warn("Secure subMessage -> SubMessage not handled");
		return null;
	}

    void ignoreParticipant(GuidPrefix prefix) {
        ignoredParticipants.add(prefix);
    }

    void close() {
        // Trying to close RTPSMessageReceiver gracefully, by setting running flag to false
        // and putting a dummy byte[] into receiver queue to wake up waiting thread
        running = false;
        queue.offer(new byte[0]); // Put a dummy array
    }
}
