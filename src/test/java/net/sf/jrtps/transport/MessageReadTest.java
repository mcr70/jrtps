package net.sf.jrtps.transport;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;

import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.junit.Test;

public class MessageReadTest {
	@Test
	public void testHistoryCacheMessage() throws IOException {
		FileInputStream fis = new FileInputStream("src/test/resources/SEDPbuiltinPublicationsWriter-hc.rtps");
		RTPSByteBuffer bb = new RTPSByteBuffer(fis.getChannel().map(MapMode.READ_ONLY, 0, fis.available()));
		
		Message m = new Message(bb);
		//System.out.println("Read message: " + m);
		
		assertTrue(m.getSubMessages().size() == 4);
	}
}
