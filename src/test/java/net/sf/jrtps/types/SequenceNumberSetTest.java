package net.sf.jrtps.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.junit.Test;

public class SequenceNumberSetTest {
	@Test
	public void testReadOneArrayElement() {
		// SNS: 					  Base               numbits   array 
		byte[] snsBytes = new byte[] {0,0,0,0, 0,0,0,1,  0,0,0,32, -1,-1,-1,-1};
		RTPSByteBuffer bb = new RTPSByteBuffer(snsBytes);
		
		SequenceNumberSet sns = new SequenceNumberSet(bb);
		assertEquals(1, sns.getBitmaps().length);
	}

	@Test
	public void testReadTwoArrayElements() {
		// SNS: 					  Base               numbits   array 
		byte[] snsBytes = new byte[] {0,0,0,0, 0,0,0,1,  0,0,0,33, -1,-1,-1,-1, 0,0,0,0};
		RTPSByteBuffer bb = new RTPSByteBuffer(snsBytes);
		
		SequenceNumberSet sns = new SequenceNumberSet(bb);
		assertEquals(2, sns.getBitmaps().length);
	}
	
	@Test
	public void test1234() {
		// Tests that example in ch. 9.4.2.6 SequenceNumberSet works correctly
		SequenceNumberSet sns = new SequenceNumberSet(1234, new int[] {0x30000000});
		assertFalse(sns.isSet(1234));
		assertFalse(sns.isSet(1235));
		assertTrue(sns.isSet(1236));
		assertTrue(sns.isSet(1237));
		assertFalse(sns.isSet(1238)); // rest seqnums are false
	}
}
