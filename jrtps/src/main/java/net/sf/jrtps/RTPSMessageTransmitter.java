package net.sf.jrtps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.transport.UDPWriter;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSMessageTransmitter is responsible for sending RTPS messages to its destination. 
 * RTPSWriters write their RTPS Messages into BlockingQueue. This class reads the queue 
 * and possibly joins multiple messages into a single RTPS message, that gets sent to
 * wire.
 *  
 * @see RTPSWriter
 * @author mcr70
 */
@Experimental("This class is not used at the moment")
class RTPSMessageTransmitter implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RTPSMessageTransmitter.class);
	private final BlockingQueue<Message> queue;
	private final UDPWriter writer;
	private final RTPSByteBuffer buffer;
	
	RTPSMessageTransmitter(Locator locator, BlockingQueue<Message> queue, int bufferSize) throws IOException {
		this.queue = queue;
		this.writer = new UDPWriter(locator, bufferSize); // TODO: remove bufferSize from constructor
		this.buffer = new RTPSByteBuffer(ByteBuffer.allocate(bufferSize));
		this.buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void run() {
		boolean running = true;		
		List<Message> mList = new LinkedList<>(); // a List to hold Messages being processed.
		
		while (running) {
			try {
				Message msg = queue.take(); // Wait for a message to arrive and take it
				queue.drainTo(mList); // If there were more than 1 message, drain them to mList

				for (Message m : mList) {
					msg.join(m);
				}
				mList.clear();
				
				sendMessage(msg);
			} catch (InterruptedException e) {
				running = false;
			}
		}

		logger.debug("RTPSMessageTransmitter exiting");
		try {
			writer.close();
		} catch (IOException e) {
			logger.warn("Exception occured while closing UDPWriter", e);
		}
	}


	private void sendMessage(Message msg) {
		int subMsgCount1 = msg.getSubMessages().size();
		buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		boolean done = msg.drainTo(buffer);
		int subMsgCount2 = msg.getSubMessages().size();
		
		logger.debug("Sending first {} subMessages from total count of {}", subMsgCount1 - subMsgCount2, subMsgCount1);
		writer.sendMessage(buffer);
		
		if (!done) {
			sendMessage(msg);
		}
	}
}
