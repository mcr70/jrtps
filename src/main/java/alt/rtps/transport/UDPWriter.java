package alt.rtps.transport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.Message;
import alt.rtps.types.Locator_t;

public class UDPWriter {
	private static final Logger log = LoggerFactory.getLogger(UDPWriter.class);
	private final Locator_t locator;
	private DatagramChannel channel;
	
	public UDPWriter(Locator_t locator) throws IOException {
		this.locator = locator;		
		channel = DatagramChannel.open();
		channel.connect(locator.getSocketAddress());
	}
	
	public boolean sendMessage(Message m) {
		return sendMessage(m, null);
	}
	public boolean sendMessage(Message m, String fileName) {
		RTPSByteBuffer buffer = new RTPSByteBuffer(ByteBuffer.allocate(1024)); // TODO: hardcoded
		buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		boolean overFlowed = m.writeTo(buffer);
		buffer.getBuffer().flip();

		try {
			channel.write(buffer.getBuffer());
		} 
		catch (IOException e) {
			log.error("Failed to send message to " + locator, e);
		}
		
		if (fileName != null) {
			writeToFile(buffer.getBuffer().rewind(), fileName);
		}
		
		return overFlowed;
	}
	
	private void writeToFile(Buffer buffer, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.getChannel().write((ByteBuffer) buffer);
			fos.close();
		} catch (Exception e) {
			log.warn("", e);
		}
	}

	public void close() throws IOException {
		channel.close();		
	}
}
