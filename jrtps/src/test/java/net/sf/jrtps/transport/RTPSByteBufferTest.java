package net.sf.jrtps.transport;

import static org.junit.Assert.*;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.junit.Test;

public class RTPSByteBufferTest {
	@Test
	public void testAlign() {
		byte[] bytes = new byte[10];
		RTPSByteBuffer bb = new RTPSByteBuffer(bytes);
		int pos1 = bb.getBuffer().position();		
		bb.align(4);
		int pos2 = bb.getBuffer().position();
		
		assertTrue(pos1 == pos2);

		bb.write_octet((byte) 1);
		pos2 = bb.getBuffer().position();
		
		assertTrue(pos2 == pos1 + 1);
		
		bb.align(4);
		pos2 = bb.getBuffer().position();
		
		assertTrue("pos 4!="+pos2, pos2 == 4);
	}
}
