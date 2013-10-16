package alt.rtps.transport;

import static org.junit.Assert.*;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.junit.Test;

public class RTPSByteBufferTest {
	@Test
	public void testAlign() {
		System.out.println("0 % 4 == " + (0%4));
		System.out.println("1 % 4 == " + (1%4));
		System.out.println("2 % 4 == " + (2%4));
		System.out.println("3 % 4 == " + (3%4));
		System.out.println("4 % 4 == " + (4%4));
		
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
