package alt.rtps.structure;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import alt.rtps.discovery.ReaderData;
import alt.rtps.message.Message;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;
/**
 * 
 * @author mcr70
 * @see 8.4.7.1
 */
public abstract class Writer extends Endpoint {
	private static final Logger log = Logger.getLogger(Writer.class);

	private List<ReaderData> matchedReaders = new LinkedList<ReaderData>();
	
	/**
	 * Configures the mode in which the Writer operates. If pushMode==true, then the
	 * Writer will push changes to the reader. If pushMode==false, changes will only be 
	 * announced via heartbeats and only be sent as response to the request of a reader.
	 */
	protected boolean pushMode = true;
	
	private Duration_t heartbeatPeriod = new Duration_t(5, 0); // 5 sec, tunable
	private Duration_t nackResponseDelay = new Duration_t(0, 200000000); // 200 ms
	private Duration_t nackSuppressionDuration = new Duration_t(0, 0); // 0, tunable
	
	/**
	 * Contains the history of CacheChange changes for this Writer.
	 */
	protected HistoryCache writer_cache = new HistoryCache();
	

	/**
	 * 
	 * @param prefix prefix from the participant that creates this Writer
	 * @param entityId
	 * @param topicName 
	 */
	public Writer(GuidPrefix_t prefix, EntityId_t entityId, String topicName) {
		super(prefix, entityId, topicName);
	}
		
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to repeatedly announce the
	 * availability of data by sending a Heartbeat Message.
	 * @return
	 */
	public Duration_t heartbeatPeriod() {
		return heartbeatPeriod;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to delay
	 * the response to a request for data from a negative acknowledgment.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return
	 */
	public Duration_t nackResponseDelay() {
		return nackResponseDelay;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to ignore requests for data from
	 * negative acknowledgments that arrive ‘too soon’ after the corresponding change is sent.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return 
	 * 
	 */
	public Duration_t nackSupressionDuration() {
		return nackSuppressionDuration;
	}
	
	/**
	 * Internal counter used to assign increasing sequence number to each change made by the
	 * Writer.
	 * @return
	 */
	public long lastChangeSequenceNumber() {
		return writer_cache.getSeqNumMax();
	}


	public void sendToLocator(Message m, Locator_t locator) {
		log.debug("Sending " + m + " to " + locator.getSocketAddress());
		
		RTPSByteBuffer buffer = new RTPSByteBuffer(ByteBuffer.allocate(512));
		buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		m.writeTo(buffer);
		buffer.getBuffer().flip();
		
		writeToFile(buffer.getBuffer(), "tmp/my-spdp-message.bin");
		
		try {
			DatagramChannel channel = DatagramChannel.open();	
			channel.connect(locator.getSocketAddress());
			channel.write(buffer.getBuffer());
			channel.close();
		} 
		catch (IOException e) {
			log.error("Failed to send message to " + locator, e);
		}
	}
	
	public void sendToLocators(Message m, List<Locator_t> locators) {
		RTPSByteBuffer buffer = new RTPSByteBuffer(ByteBuffer.allocate(512));
		buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		m.writeTo(buffer);
		buffer.getBuffer().flip();
		
		writeToFile(buffer.getBuffer(), "tmp/my-spdp-message.bin");
		
		for (Locator_t locator : locators) {
			log.debug("Sending to " + locator.getSocketAddress() + ": " + m);
			
			try {
				// TODO: opening and closing can be optimized
				DatagramChannel channel = DatagramChannel.open();
				channel.connect(locator.getSocketAddress());
				channel.write(buffer.getBuffer());
				channel.close();
			} 
			catch (IOException e) {
				log.error("Failed to send message to " + locator, e);
			}
			
			buffer.getBuffer().rewind(); // Reset buffer to beginning
		}
	}
	
	
	private void writeToFile(ByteBuffer buffer, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.getChannel().write(buffer);
			fos.close();
		}  
		catch (IOException e) {
			e.printStackTrace();
		}

		buffer.rewind();
	}


	public HistoryCache getHistoryCache() {
		return writer_cache;
	}
	
}
