package net.sf.jrtps.message;

import static org.junit.Assert.assertArrayEquals;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.LocatorUDPv4_t;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.types.SequenceNumberSet;

import org.junit.Assert;
import org.junit.Test;

public class MessageTests {
	/**
	 * Tests, that reading and writing of InfoDestination is symmetrical.
	 * @throws IllegalMessageException 
	 */
	@Test
	public void testInfoDestination() throws IllegalMessageException {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix.GUIDPREFIX_UNKNOWN);
		m1.addSubMessage(new InfoDestination(GuidPrefix.GUIDPREFIX_UNKNOWN));
		
		// Write Message to bytes1 array 
		byte[] bytes1 = write(m1);

		// Read from bytes1 array - tests reading
		Message m2 = read(bytes1);
		
		// Write the message read to bytes2
		byte[] bytes2 = write(m2);
		
		// Test, that bytes1 and bytes2 are equal
		assertArrayEquals(bytes1, bytes2);
	}
	
	/**
	 * Tests, that reading and writing of InfoReply is symmetrical.
	 * @throws IllegalMessageException 
	 */
	@Test
	public void testInfoReply() throws IllegalMessageException {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix.GUIDPREFIX_UNKNOWN);

		Locator loc1 = new Locator(InetAddress.getLoopbackAddress(), 7111);
        Locator loc2 = new Locator(InetAddress.getLoopbackAddress(), 7222);
		
		List<Locator> unicastLocators = new LinkedList<>();
		unicastLocators.add(loc1);
		List<Locator> multicastLocators = new LinkedList<>();
		multicastLocators.add(loc2);
		
		m1.addSubMessage(new InfoReply(unicastLocators, multicastLocators));
		
		// Write Message to bytes1 array 
		byte[] bytes1 = write(m1);
		
		// Read from bytes1 array - tests reading
		Message m2 = read(bytes1);

		// Write the message read to bytes2
		byte[] bytes2 = write(m2);
		
		// Test, that bytes1 and bytes2 are equal
		assertArrayEquals(bytes1, bytes2);
	}

	/**
	 * Tests, that reading and writing of InfoReplyIp4 is symmetrical.
	 * @throws IllegalMessageException 
	 */
	@Test
	public void testInfoReplyIp4() throws IllegalMessageException {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix.GUIDPREFIX_UNKNOWN);

		LocatorUDPv4_t lc1 = LocatorUDPv4_t.LOCATORUDPv4_INVALID;
		LocatorUDPv4_t lc2 = LocatorUDPv4_t.LOCATORUDPv4_INVALID;
		
		m1.addSubMessage(new InfoReplyIp4(lc1, lc2));
		
		// Write Message to bytes1 array 
		byte[] bytes1 = write(m1);
		
		// Read from bytes1 array - tests reading
		Message m2 = read(bytes1);

		// Write the message read to bytes2
		byte[] bytes2 = write(m2);
		
		// Test, that bytes1 and bytes2 are equal
		assertArrayEquals(bytes1, bytes2);
	}

	/**
	 * Tests, that reading and writing of InfoSource is symmetrical.
	 * @throws IllegalMessageException 
	 */
	@Test
	public void testInfoSource() throws IllegalMessageException {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix.GUIDPREFIX_UNKNOWN);
		m1.addSubMessage(new InfoSource(GuidPrefix.GUIDPREFIX_UNKNOWN));
		
		// Write Message to bytes1 array 
		byte[] bytes1 = write(m1);
		
		// Read from bytes1 array - tests reading
		Message m2 = read(bytes1);

		// Write the message read to bytes2
		byte[] bytes2 = write(m2);
		
		// Test, that bytes1 and bytes2 are equal
		assertArrayEquals(bytes1, bytes2);
	}

	@Test
	public void testGap() {
		// Test Gap of seqNums 0,1,2

		Gap gap = new Gap(null, null, 0, 2);
		Assert.assertTrue(0 == gap.getGapStart());
		Assert.assertTrue(2 == gap.getGapEnd());
		
		RTPSByteBuffer bb = new RTPSByteBuffer(new byte[128]);
		EntityId.UNKNOWN_ENTITY.writeTo(bb); // readerId
		EntityId.UNKNOWN_ENTITY.writeTo(bb); // writerId
		SequenceNumber sn = new SequenceNumber(0);
		sn.writeTo(bb); // gapStart
		SequenceNumberSet sns = new SequenceNumberSet(1, new int[] {0x80000000 >> 1});
		Assert.assertTrue(2 == sns.getSequenceNumbers().size());
		sns.writeTo(bb); // gapList
		
		bb.getBuffer().flip();
		
		Gap gap2 = new Gap(new SubMessageHeader(Gap.KIND), bb);		
		Assert.assertTrue(0 == gap2.getGapStart());
		Assert.assertTrue(2 == gap2.getGapEnd());

		Gap gap3 = new Gap(null, null, 1, 1);
		Assert.assertTrue(1 == gap3.getGapStart());
		Assert.assertTrue(1 == gap3.getGapEnd());
	}
	
	/**
	 * Tests, that reading and writing of InfoTimestamp is symmetrical.
	 * @throws IllegalMessageException 
	 */
	@Test
	public void testInfoTimestamp() throws IllegalMessageException {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix.GUIDPREFIX_UNKNOWN);
		m1.addSubMessage(new InfoTimestamp(123));

		// Write Message to bytes1 array 
		byte[] bytes1 = write(m1);
		
		// Read from bytes1 array - tests reading
		Message m2 = read(bytes1);

		// Write the message read to bytes2
		byte[] bytes2 = write(m2);
		
		// Test, that bytes1 and bytes2 are equal
		assertArrayEquals(bytes1, bytes2);
	}

	/**
	 * Writes a message to a byte array. 
	 * @param m
	 * @return byte[]
	 */
	private byte[] write(Message m) {
		RTPSByteBuffer bb1 = new RTPSByteBuffer(new byte[1024]);
		bb1.setEndianess(true);
		m.writeTo(bb1);
		byte[] bytes = getBytes(bb1);

		return bytes;
	}

	/**
	 * Gets bytes from biven buffer. Buffer is flipped and a new array is created.
	 * @param bb
	 * @return byte[]
	 */
	private byte[] getBytes(RTPSByteBuffer bb) {
		byte[] bytes = new byte[bb.position()];
		bb.getBuffer().flip();
		bb.read(bytes);
		
		return bytes;
	}	

	/**
	 * Read message from bytes. Array is first copied to make sure
	 * resulting Message has nothing to do with original byte array.
	 * 
	 * @param bytes
	 * @return Message
	 * @throws IllegalMessageException 
	 */
	private Message read(byte[] bytes) throws IllegalMessageException {
		byte[] __bytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, __bytes, 0, bytes.length);
		
		RTPSByteBuffer bb2 = new RTPSByteBuffer(__bytes);
		return new Message(bb2);
	}
	
	@SuppressWarnings("unused")
	private void printBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			if (i % 16 == 0) {
				System.out.println();
			}
			System.out.print("0x" + String.format("%02x", bytes[i]) + " ");
		}
		
		System.out.println();
	}
}
