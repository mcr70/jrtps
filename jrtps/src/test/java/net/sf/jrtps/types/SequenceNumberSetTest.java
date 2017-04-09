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
		final int BASE = 1234;

		// Tests that example in ch. 9.4.2.6 SequenceNumberSet works correctly
		SequenceNumberSet sns = new SequenceNumberSet(BASE, new int[] {0x30000000});
		assertFalse(sns.containsSeqNum(1234));
		assertFalse(sns.containsSeqNum(1235));
		assertTrue(sns.containsSeqNum(1236));
		assertTrue(sns.containsSeqNum(1237));
		for (int i = 4; i < sns.getNumBits(); i++) {
			assertFalse(sns.containsSeqNum(BASE + i)); // rest seqnums are false
		}	

		assertTrue(sns.getNumBits() == 32);
		assertTrue(sns.getSequenceNumbers().size() == 2);
		assertTrue(sns.getMissingSequenceNumbers().size() == 30);
		

		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[16]);		
		bb.write_longlong(BASE);   // Base
		bb.write_long(12);         // numBits
		bb.write_long(0x30000000); // bitmap
		bb.getBuffer().flip();
		
		sns = new SequenceNumberSet(bb);

		assertEquals(1, sns.getBitmaps().length);
		assertFalse(sns.containsSeqNum(1234));
		assertFalse(sns.containsSeqNum(1235));
		assertTrue(sns.containsSeqNum(1236));
		assertTrue(sns.containsSeqNum(1237));
		 
		for (int i = 4; i < sns.getNumBits(); i++) {
			assertFalse(sns.containsSeqNum(BASE + i)); // rest seqnums are false
		}	

		assertTrue(sns.getNumBits() == 12);
		assertTrue(sns.getSequenceNumbers().size() == 2);
		assertTrue(sns.getMissingSequenceNumbers().size() == 10);	
	}
}
