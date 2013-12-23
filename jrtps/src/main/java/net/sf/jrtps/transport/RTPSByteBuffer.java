package net.sf.jrtps.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is used to marshall and unmarshall data.
 * 
 * @author mcr70
 *
 */
public class RTPSByteBuffer /* extends org.omg.CORBA.portable.InputStream */ {
	// TODO: consider extending from org.omg.CORBA.portable.InputStream
	//       This would allow to use idl compiler for dds.idl & rtps.idl
	private ByteBuffer buffer;
	
	/**
	 * Constructs RTPSByteBuffer.
	 * @param bytes
	 */
	public RTPSByteBuffer(byte[] bytes) {
		this(ByteBuffer.wrap(bytes));
	}
	
	/**
	 * Constructs RTPSByteBuffer.
	 * @param buffer
	 * @see java.nio.ByteBuffer
	 */
	public RTPSByteBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}
	/**
	 * Constructs RTPSByteBuffer.
	 * @param is
	 * @see java.io.InputStream
	 */
	public RTPSByteBuffer(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = is.read()) != -1) {
			baos.write(b);
		}
		
		this.buffer = ByteBuffer.wrap(baos.toByteArray());
	}
	
	/**
	 * Get the position in underlying buffer.
	 * @return position
	 */
	public int position() {
		return buffer.position();
	}

	/**
	 * Reads an octet from underlying buffer.
	 * @return octet
	 */
	public byte read_octet() {
		return buffer.get();
	}
	
	/**
	 * Writes an octet to underlying buffer.
	 * @param an_octet
	 */
	public void write_octet(byte an_octet) {
		buffer.put(an_octet);
	}
	
	/**
	 * Reads a short from underlying buffer
	 * @return short
	 */
	public int read_short() {
		return buffer.getShort();
	}
	
	/**
	 * Qrites a short to underlying buffer
	 * @param a_short
	 */
	public void write_short(int a_short) {
		//align(2);
		buffer.putShort((short) a_short);
	}
	
	/**
	 * Reads a long from underlying buffer
	 * @return long
	 */
	public int read_long() {
		return buffer.getInt();
	}
	
	/**
	 * Writes a long to underlying buffer
	 * @param a_long
	 */
	public void write_long(int a_long) {
		//align(4);
		buffer.putInt(a_long);
	}

	/**
	 * Reads a boolean from underlying buffer. Internally, an octet is read.
	 * @return boolean
	 */
	public boolean read_boolean() {
		int b = read_octet();
		
		// '1' should represent true, but it is known fact that some ORBs 
		// do not honor this, representing true value for anything other than '0'
		return b != 0;  
	}

	/**
	 * Writes a boolean to underlying buffer
	 * @param b
	 */
	public void write_boolean(boolean b) {
		if (b) {
			write_octet((byte) 1);
		}
		else {
			write_octet((byte) 0);
		}
	}
	
	
	/**
	 * Reads a String from underlying buffer
	 * @return String
	 */	
	public String read_string() {
		// @see 9.3.2.7 Strings and Wide Strings, CORBA 3.2 spec, formal/2011-11-02
		// TODO: character encoding

		int length = read_long() - 1; // ignore trailing NUL character
		byte[] bytes = new byte[length];
		read(bytes);
		read_octet(); // Read terminating NUL character. ignore it.
		
		return new String(bytes);
	}
	
	/**
	 * Writes a string to underlying buffer
	 * @param s
	 */
	public void write_string(String s) {
		// @see 9.3.2.7 Strings and Wide Strings, CORBA 3.2 spec, formal/2011-11-02
		// TODO: character encoding
		
		write_long(s.length() + 1); // +1 for adding terminating NUL character
		write(s.getBytes());
		write_octet((byte) 0); // terminating NUL character 
	}

	/**
	 * Reads in bytes(octets) from underlying buffer.
	 * @param bytes
	 */
	public void read(byte[] bytes) {
		buffer.get(bytes);
	}
	
	/**
	 * Writes bytes(octets).
	 * @param bytes
	 */
	public void write(byte[] bytes) {
		buffer.put(bytes);
	}

	/**
	 * Writes an array of ints(longs)
	 * @param ints
	 */
	public void write(int[] ints) {
		for (int i = 0; i < ints.length; i++) {
			write_long(ints[i]);
		}
	}

	/**
	 * Aligns this buffer to given byteBoundary.
	 * @param byteBoundary
	 */
	public void align(int byteBoundary) {
		int position = buffer.position();
		int adv = (position % byteBoundary);
		
		if (adv != 0) {
			buffer.position(position + (byteBoundary - adv));
		}
	}
	
	/**
	 * Get the underlying ByteBuffer.
	 * @return ByteBuffer
	 */
	public ByteBuffer getBuffer() {
		return buffer;
	}

	/**
	 * Sets the endianness used. 
	 * see 9.4.5.1.2 flags
	 * 
	 * @param endianessFlag If true, little endian is used. If false, big endian is used.
	 * 
	 */
	public void setEndianess(boolean endianessFlag) {
		if (endianessFlag) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		else {
			buffer.order(ByteOrder.BIG_ENDIAN);
		}
	}

	/**
	 * Gets an InputStream reading from the backing ByteBuffer.
	 * InputStream will start reading from current position of the ByteBuffer.
	 * 
	 * @return InputStream
	 */
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				if (!buffer.hasRemaining()) {
					return -1;
				}

				return buffer.get() & 0xff;
			}
		};
	}
	
	/**
	 * Gets an OutputStream that writes to this RTPSByteBuffer.
	 * Writing starts at current position.
	 * 
	 * @return OutputStream
	 */
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
				buffer.put((byte) arg0);
			}
		};
	}
}
