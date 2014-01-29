package net.sf.jrtps;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Gap;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.InfoDestination;
import net.sf.jrtps.message.InfoReply;
import net.sf.jrtps.message.InfoReplyIp4;
import net.sf.jrtps.message.InfoSource;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.SubMessage;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.LocatorUDPv4_t;
import net.sf.jrtps.types.Time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSMessageHandler is a consumer to BlockingQueue.
 * Queue contains byte[] entries, which is parsed to RTPS Messages.
 * Successfully parsed messages are split into submessages, which are passed
 * to corresponding RTPS reader entities.
 * 
 * @author mcr70
 */
class RTPSMessageHandler implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RTPSMessageHandler.class);
	
	private final RTPSParticipant participant;
	private final BlockingQueue<byte[]> queue;

	private Set<GuidPrefix> ignoredParticipants = new HashSet<>();
	
	private boolean running = true;

	RTPSMessageHandler(RTPSParticipant p, BlockingQueue<byte[]> queue) {
		this.participant = p;
		this.queue = queue;
	}


	@Override
	public void run() {
		while(running) {
			try {
				// NOTE: We can have only one MessageHandler. pending samples concept relies on it.
				byte[] bytes = queue.take();
				Message msg = new Message(new RTPSByteBuffer(bytes));
				logger.debug("Parsed RTPS message {}", msg);

				handleMessage(msg);
			} catch (InterruptedException e) {
				running = false;
			}
		}
		
		logger.debug("RTPSMessageHandler exiting");
	}
	
	
	/**
	 * Handles incoming Message. Each sub message is transferred to corresponding
	 * reader.
	 * @param msg
	 */
	private void handleMessage(Message msg) {
		Time timestamp = null;
		GuidPrefix destGuidPrefix = GuidPrefix.GUIDPREFIX_UNKNOWN;
		GuidPrefix sourceGuidPrefix = msg.getHeader().getGuidPrefix();
		
		if (participant.getGuid().getPrefix().equals(sourceGuidPrefix)) {
			logger.debug("Discarding message originating from this participant");
			return;
		}

		logger.debug("Got Message from {}", sourceGuidPrefix);	
		
		Set<RTPSReader<?>> dataReceivers = new HashSet<>();
		List<SubMessage> subMessages = msg.getSubMessages();
		
		for (SubMessage subMsg : subMessages) {
			switch (subMsg.getKind()) {
			case ACKNACK:
				if (ignoredParticipants.contains(sourceGuidPrefix)) {
					continue;
				}
		
				handleAckNack(sourceGuidPrefix, (AckNack)subMsg);
				break;
			case DATA:
				if (ignoredParticipants.contains(sourceGuidPrefix)) {
					continue;
				}

				try {
					RTPSReader<?> r = handleData(sourceGuidPrefix, timestamp, (Data)subMsg);
					if (r != null) {
						dataReceivers.add(r);
					}
				}
				catch(IOException ioe) {
					logger.warn("Failed to handle data", ioe);
				}
				break;
			case HEARTBEAT:
				if (ignoredParticipants.contains(sourceGuidPrefix)) {
					continue;
				}

				handleHeartbeat(sourceGuidPrefix, (Heartbeat)subMsg);
				break;
			case INFODESTINATION: 
				destGuidPrefix = ((InfoDestination)subMsg).getGuidPrefix(); 
				break;
			case INFOSOURCE: 
				sourceGuidPrefix = ((InfoSource)subMsg).getGuidPrefix(); 
				break;
			case INFOTIMESTAMP: 
				timestamp = ((InfoTimestamp)subMsg).getTimeStamp(); 
				break;
			case INFOREPLY: // TODO: HB, AC & DATA needs to use replyLocators, if present 
				InfoReply ir = (InfoReply) subMsg;
				List<Locator> replyLocators = ir.getUnicastLocatorList();
				if (ir.multicastFlag()) {
					replyLocators.addAll(ir.getMulticastLocatorList());
				}
				logger.warn("InfoReply not handled");
				break;
			case INFOREPLYIP4: // TODO: HB, AC & DATA needs to use these Locators, if present
				InfoReplyIp4 ir4 = (InfoReplyIp4) subMsg;
				LocatorUDPv4_t unicastLocator = ir4.getUnicastLocator();
				if (ir4.multicastFlag()) {
					LocatorUDPv4_t multicastLocator = ir4.getMulticastLocator();
				}
				logger.warn("InfoReplyIp4 not handled");
				break;
			case GAP:
				handleGap(sourceGuidPrefix, (Gap)subMsg);
				break;
			default: 
				logger.warn("SubMessage not handled: {}", subMsg);
			}
		}
		
		logger.trace("Releasing samples for {} readers", dataReceivers.size());
		for (RTPSReader<?> reader : dataReceivers) {
			reader.releasePendingSamples();
		}
	}



	private void handleAckNack(GuidPrefix sourceGuidPrefix, AckNack ackNack) {
		RTPSWriter<?> writer = participant.getWriter(ackNack.getWriterId(), ackNack.getReaderId());

		if (writer != null) {
			writer.onAckNack(sourceGuidPrefix, ackNack);
		}
		else {
			logger.debug("No Writer({}) to handle AckNack from {}", ackNack.getWriterId(), ackNack.getReaderId());
		}
	}

	private void handleGap(GuidPrefix sourceGuidPrefix, Gap gap) {
		RTPSReader<?> reader = participant.getReader(gap.getReaderId(), gap.getWriterId());
		reader.handleGap(sourceGuidPrefix, gap);
	}

	private RTPSReader<?> handleData(GuidPrefix sourcePrefix, Time timestamp, Data data) throws IOException {
		RTPSReader<?> reader = participant.getReader(data.getReaderId(), data.getWriterId());

		if (reader != null) {
			reader.createSample(sourcePrefix, data, timestamp);
			return reader;
		}
		else {
			logger.warn("No Reader({}) to handle Data from {}", data.getReaderId(), data.getWriterId());
		}
		
		return null;
	}

	private void handleHeartbeat(GuidPrefix senderGuidPrefix, Heartbeat hb) {		
		RTPSReader<?> reader = participant.getReader(hb.getReaderId(), hb.getWriterId());
	
		if (reader != null) {
			reader.onHeartbeat(senderGuidPrefix, hb);
		}
		else {
			logger.debug("No Reader({}) to handle Heartbeat from {}", hb.getReaderId(), hb.getWriterId());
		}
	}


	void ignoreParticipant(GuidPrefix prefix) {
		ignoredParticipants.add(prefix);
	}
}
