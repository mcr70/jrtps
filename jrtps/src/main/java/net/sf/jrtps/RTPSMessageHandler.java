package net.sf.jrtps;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.InfoDestination;
import net.sf.jrtps.message.InfoSource;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.SubMessage;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.Time_t;

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
	private static final Logger log = LoggerFactory.getLogger(RTPSMessageHandler.class);

	private final RTPSParticipant participant;
	private final BlockingQueue<byte[]> queue;

	private boolean running = true;

	RTPSMessageHandler(RTPSParticipant p, BlockingQueue<byte[]> queue) {
		this.participant = p;
		this.queue = queue;
	}

	/**
	 * Handles incoming Message. Each sub message is transferred to corresponding
	 * reader.
	 * @param msg
	 */
	private void handleMessage(Message msg) {
		Time_t timestamp = null;
		GuidPrefix_t destGuidPrefix = GuidPrefix_t.GUIDPREFIX_UNKNOWN;
		GuidPrefix_t sourceGuidPrefix = msg.getHeader().getGuidPrefix();
		
		log.debug("Got Message from {}", sourceGuidPrefix);
		if (participant.getGuid().prefix.equals(sourceGuidPrefix)) {
			log.debug("Discarding message originating from this participant");
			return;
		}
			
		List<SubMessage> subMessages = msg.getSubMessages();

		Set<RTPSReader<?>> dataReceivers = new HashSet<>();
		
		for (SubMessage subMsg : subMessages) {
			switch (subMsg.getKind()) {
			case ACKNACK:
				handleAckNack(sourceGuidPrefix, (AckNack)subMsg);
				break;
			case DATA:
				try {
					RTPSReader<?> r = handleData(sourceGuidPrefix, timestamp, (Data)subMsg);
					dataReceivers.add(r);
				}
				catch(IOException ioe) {
					log.warn("Failed to handle data", ioe);
				}
				break;
			case HEARTBEAT:
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
			default: 
				log.warn("SubMessage not handled: {}", subMsg);
			}
		}
		
		for (RTPSReader<?> reader : dataReceivers) {
			reader.releasePendingSamples();
		}
	}

	private void handleAckNack(GuidPrefix_t sourceGuidPrefix, AckNack ackNack) {
		RTPSWriter<?> writer = participant.getWriter(ackNack.getWriterId(), ackNack.getReaderId());

		if (writer != null) {
			writer.onAckNack(sourceGuidPrefix, ackNack);
		}
		else {
			log.debug("No Writer({}) to handle AckNack from {}", ackNack.getWriterId(), ackNack.getReaderId());
		}
	}

	private RTPSReader<?> handleData(GuidPrefix_t sourcePrefix, Time_t timestamp, Data data) throws IOException {
		RTPSReader<?> reader = participant.getReader(data.getReaderId(), data.getWriterId());

		if (reader != null) {
			reader.createSample(sourcePrefix, data, timestamp);
			return reader;
		}
		else {
			log.debug("No Reader({}) to handle Data from {}", data.getReaderId(), data.getWriterId());
		}
		
		return null;
	}

	private void handleHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb) {		
		RTPSReader<?> reader = participant.getReader(hb.getReaderId(), hb.getWriterId());

		if (reader != null) {
			reader.onHeartbeat(senderGuidPrefix, hb);
		}
		else {
			log.debug("No Reader({}) to handle Heartbeat from {}", hb.getReaderId(), hb.getWriterId());
		}
	}

	@Override
	public void run() {
		while(running) {
			try {
				byte[] bytes = queue.take();
				Message msg = new Message(new RTPSByteBuffer(bytes));
				log.debug("Parsed RTPS message {}", msg);

				handleMessage(msg);
			} catch (InterruptedException e) {
				running = false;
			}
		}
		
		log.debug("Handler exiting");
	}
}
