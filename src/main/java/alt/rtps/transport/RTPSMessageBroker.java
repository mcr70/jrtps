package alt.rtps.transport;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.RTPSParticipant;
import alt.rtps.RTPSReader;
import alt.rtps.RTPSWriter;
import alt.rtps.message.AckNack;
import alt.rtps.message.Data;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.InfoDestination;
import alt.rtps.message.InfoSource;
import alt.rtps.message.InfoTimestamp;
import alt.rtps.message.Message;
import alt.rtps.message.SubMessage;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Time_t;

public class RTPSMessageBroker {
	private static final Logger log = LoggerFactory.getLogger(RTPSMessageBroker.class);

	private final RTPSParticipant participant;

	public RTPSMessageBroker(RTPSParticipant p) {
		this.participant = p;
	}

	public void handleMessage(Message msg) {
		Time_t timestamp = null;
		GuidPrefix_t destGuidPrefix = GuidPrefix_t.GUIDPREFIX_UNKNOWN;
		GuidPrefix_t sourceGuidPrefix = msg.getHeader().getGuidPrefix();
		List<SubMessage> subMessages = msg.getSubMessages();

		for (SubMessage subMsg : subMessages) {
			switch (subMsg.getKind()) {
			case ACKNACK:
				handleAckNack(sourceGuidPrefix, (AckNack)subMsg);
				break;
			case DATA:
				try {
					handleData(sourceGuidPrefix, timestamp, (Data)subMsg);
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
	}

	private void handleAckNack(GuidPrefix_t sourceGuidPrefix, AckNack ackNack) {
		RTPSWriter writer = participant.getWriter(ackNack.getWriterId(), ackNack.getReaderId());

		if (writer != null) {
			//log.debug("Got AckNack for {}", writer.getGuid().entityId);
			writer.onAckNack(sourceGuidPrefix, ackNack);
		}
		else {
			log.debug("No Writer to handle AckNack from {}", ackNack.getReaderId());
		}
	}

	private void handleData(GuidPrefix_t sourcePrefix, Time_t timestamp, Data data) throws IOException {
		RTPSReader reader = null;
		if (data.getReaderId().equals(EntityId_t.UNKNOWN_ENTITY)) {
			reader = participant.getMatchingReader(data.getWriterId());
		}
		else {
			reader = participant.getReader(data.getReaderId());
		}

		if (reader != null) {
			reader.onData(sourcePrefix, data, timestamp);
		}
		else {
			log.debug("No Reader ({}) to handle Data from {}", data.getReaderId(), data.getWriterId());
		}
	}

	private void handleHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb) {		
		RTPSReader reader = participant.getReader(hb.getReaderId(), hb.getWriterId());

		if (reader != null) {
			reader.onHeartbeat(senderGuidPrefix, hb);
		}
		else {
			log.debug("No Reader to handle Heartbeat from {}", hb.getWriterId());
		}
	}
}
