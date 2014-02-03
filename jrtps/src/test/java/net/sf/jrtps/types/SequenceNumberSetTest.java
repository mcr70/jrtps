package net.sf.jrtps.types;

import static org.junit.Assert.assertEquals;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.junit.Test;

public class SequenceNumberSetTest {
	@Test
	public void testReadOneArrayElement() {
		// SNS: 					  Base               numbits   array 
		byte[] snsBytes = new byte[] {0,0,0,0, 0,0,0,1,  0,0,0,32, -1,-1,-1,-1};
		RTPSByteBuffer bb = new RTPSByteBuffer(snsBytes);
		
		SequenceNumberSet sns = new SequenceNumberSet(bb);
		assertEquals(1, sns.getBitmapBase());
		assertEquals(1, sns.getBitmaps().length);
	}

	@Test
	public void testReadTwoArrayElements() {
		// SNS: 					  Base               numbits   array 
		byte[] snsBytes = new byte[] {0,0,0,0, 0,0,0,1,  0,0,0,33, -1,-1,-1,-1, 0,0,0,0};
		RTPSByteBuffer bb = new RTPSByteBuffer(snsBytes);
		
		SequenceNumberSet sns = new SequenceNumberSet(bb);
		assertEquals(1, sns.getBitmapBase());
		assertEquals(2, sns.getBitmaps().length);
	}
}
