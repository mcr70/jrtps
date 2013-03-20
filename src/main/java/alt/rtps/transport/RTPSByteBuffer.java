package alt.rtps.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * @author mcr70
 *
 */
public class RTPSByteBuffer /* extends org.omg.CORBA.portable.InputStream */ {
	// TODO: consider extending from org.omg.CORBA.portable.InputStream
	//       This would allow to use idl compiler for dds.idl & rtps.idl
	private ByteBuffer buffer;
	

	public RTPSByteBuffer(byte[] bytes) {
		this(ByteBuffer.wrap(bytes));
	}
	
	public RTPSByteBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public RTPSByteBuffer(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = is.read()) != -1) {
			baos.write(b);
		}
		
		this.buffer = ByteBuffer.wrap(baos.toByteArray());
	}
	
	public int position() {
		return buffer.position();
	}

	public byte read_octet() {
		return buffer.get();
	}
	
	public void write_octet(byte an_octet) {
		buffer.put(an_octet);
	}
	
	public int read_short() {
		return buffer.getShort();
	}
	
	public void write_short(int a_short) {
		//align(2);
		buffer.putShort((short) a_short);
	}
	

	public int read_long() {
		return buffer.getInt();
	}
	
	public void write_long(int a_long) {
		//align(4);
		buffer.putInt(a_long);
	}

	public boolean read_boolean() {
		int b = read_octet();
		
		// '1' should represent true, but it is known fact that some ORBs 
		// do not honor this, representing true value for anything other than '0'
		return b != 0;  
	}

	public void write_boolean(boolean b) {
		if (b) {
			write_octet((byte) 1);
		}
		else {
			write_octet((byte) 0);
		}
	}
	
	public void write_string(String s) {
		// @see 9.3.2.7 Strings and Wide Strings, CORBA 3.2 spec, formal/2011-11-02
		// TODO: character encoding
		
		write_long(s.length() + 1); // +1 for adding terminating NUL character
		write(s.getBytes());
		write_octet((byte) 0); // terminating NUL character 
	}
	
	public String read_string() {
		// @see 9.3.2.7 Strings and Wide Strings, CORBA 3.2 spec, formal/2011-11-02
		// TODO: character encoding

		int length = read_long() - 1; // ignore trailing NUL character
		byte[] bytes = new byte[length];
		read(bytes);
		read_octet(); // Read terminating NUL character. ignore it.
		
		return new String(bytes);
	}
	

	public void read(byte[] bytes) {
		buffer.get(bytes);
	}
	
	public void write(byte[] bytes) {
		buffer.put(bytes);
	}

	public void write(int[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			write_long(bytes[i]);
		}
	}

	public void align(int byteBoundary) {
		int position = buffer.position();
		int adv = (position % byteBoundary);
		
		if (adv != 0 && true) {
			buffer.position(position + (byteBoundary - adv));
		}
		else {
			buffer.position(position + adv);
		}
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}

	/**
	 * 
	 * @param endianessFlag
	 * @see 9.4.5.1.2 flags
	 */
	public void setEndianess(boolean endianessFlag) {
		if (endianessFlag) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		else {
			buffer.order(ByteOrder.BIG_ENDIAN);
		}
	}
}
