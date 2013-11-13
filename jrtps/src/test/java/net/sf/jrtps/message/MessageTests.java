package net.sf.jrtps.message;

import static org.junit.Assert.assertArrayEquals;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.LocatorUDPv4_t;
import net.sf.jrtps.types.Locator_t;

import org.junit.Test;

public class MessageTests {
	/**
	 * Tests, that reading and writing of InfoDestination is symmetrical.
	 */
	@Test
	public void testInfoDestination() {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix_t.GUIDPREFIX_UNKNOWN);
		m1.addSubMessage(new InfoDestination(GuidPrefix_t.GUIDPREFIX_UNKNOWN));
		
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
	 */
	@Test
	public void testInfoReply() {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix_t.GUIDPREFIX_UNKNOWN);

		List<Locator_t> unicastLocators = new LinkedList<>();
		unicastLocators.add(Locator_t.defaultMetatrafficUnicastLocator(0, 0));
		List<Locator_t> multicastLocators = new LinkedList<>();
		multicastLocators.add(Locator_t.defaultDiscoveryMulticastLocator(0));
		
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
	 */
	@Test
	public void testInfoReplyIp4() {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix_t.GUIDPREFIX_UNKNOWN);

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
	 */
	@Test
	public void testInfoSource() {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix_t.GUIDPREFIX_UNKNOWN);
		m1.addSubMessage(new InfoSource(GuidPrefix_t.GUIDPREFIX_UNKNOWN));
		
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
	 * Tests, that reading and writing of InfoTimestamp is symmetrical.
	 */
	@Test
	public void testInfoTimestamp() {
		// Create a Message with InfoDestination
		Message m1 = new Message(GuidPrefix_t.GUIDPREFIX_UNKNOWN);
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
	 * Writes a message to internally created bytebuffer. 
	 * @param m
	 * @return
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
	 * @return
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
	 * @return
	 */
	private Message read(byte[] bytes) {
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
