package net.sf.jrtps.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is used to marshall and unmarshall data.
 * 
 * @author mcr70
 * 
 */
public class RTPSByteBuffer {
    private ByteBuffer buffer;

    /**
     * Constructs RTPSByteBuffer.
     * 
     * @param bytes
     */
    public RTPSByteBuffer(byte[] bytes) {
        this(ByteBuffer.wrap(bytes));
    }

    /**
     * Constructs RTPSByteBuffer.
     * 
     * @param buffer
     * @see java.nio.ByteBuffer
     */
    public RTPSByteBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Constructs RTPSByteBuffer.
     * 
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
     * 
     * @return position
     */
    public int position() {
        return buffer.position();
    }

    /**
     * Reads an octet from underlying buffer.
     * 
     * @return octet
     */
    public byte read_octet() {
        return buffer.get();
    }

    /**
     * Writes an octet to underlying buffer.
     * 
     * @param an_octet
     */
    public void write_octet(byte an_octet) {
        buffer.put(an_octet);
    }

    /**
     * Reads a short from underlying buffer
     * 
     * @return short
     */
    public int read_short() {
        return buffer.getShort();
    }

    /**
     * Qrites a short to underlying buffer
     * 
     * @param a_short
     */
    public void write_short(int a_short) {
        // align(2);
        buffer.putShort((short) a_short);
    }

    /**
     * Reads a long from underlying buffer
     * 
     * @return long
     */
    public int read_long() {
        return buffer.getInt();
    }

    /**
     * Writes a long to underlying buffer
     * 
     * @param a_long
     */
    public void write_long(int a_long) {
        // align(4);
        buffer.putInt(a_long);
    }

    /**
     * Reads a boolean from underlying buffer. Internally, an octet is read.
     * 
     * @return boolean
     */
    public boolean read_boolean() {
        int b = read_octet();

        // '1' should represent true, but it is known fact that some ORBs
        // do not honor this, representing true value for anything other than
        // '0'
        return b != 0;
    }

    /**
     * Writes a boolean to underlying buffer
     * 
     * @param b
     */
    public void write_boolean(boolean b) {
        if (b) {
            write_octet((byte) 1);
        } else {
            write_octet((byte) 0);
        }
    }

    /**
     * Reads a String from underlying buffer
     * 
     * @return String
     */
    public String read_string() {
        // @see 9.3.2.7 Strings and Wide Strings, CORBA 3.2 spec,
        // formal/2011-11-02
        // TODO: character encoding

        int length = read_long() - 1; // ignore trailing NUL character
        byte[] bytes = new byte[length];
        read(bytes);
        read_octet(); // Read terminating NUL character. ignore it.

        return new String(bytes);
    }

    /**
     * Writes a string to underlying buffer
     * 
     * @param s
     */
    public void write_string(String s) {
        // @see 9.3.2.7 Strings and Wide Strings, CORBA 3.2 spec,
        // formal/2011-11-02
        // TODO: character encoding

        write_long(s.length() + 1); // +1 for adding terminating NUL character
        write(s.getBytes());
        write_octet((byte) 0); // terminating NUL character
    }

    // --- Reads and writes for java primitives --------------
    /**
     * Reads a java primitive int.
     * @return an int
     */
    public int readInt() {
        return buffer.getInt();
    }
    
    /**
     * Writes a java primitive int.
     * @param i
     */
    public void writeInt(int i) {
        buffer.putInt(i);
    }
    
    /**
     * Reads a java primitive short.
     * @return a short
     */
    public short readShort() {
        return buffer.getShort();
    }
    
    /**
     * Writes a java primitive short.
     * @param s
     */
    public void writeShort(short s) {
        buffer.putShort(s);
    }
    
    /**
     * Reads a java primitive long.
     * @return a long
     */
    public long readLong() {
        return buffer.getLong();
    }
    
    /**
     * Writes a java primitive long.
     * @param l
     */
    public void writeLong(long l) {
        buffer.putLong(l);
    }

    /**
     * Reads a java primitive float.
     * @return a float
     */
    public float readFloat() {
        return buffer.getFloat();
    }
    
    /**
     * Writes a java primitive float.
     * @param f
     */
    public void writeFloat(float f) {
        buffer.putFloat(f);
    }
    
    /**
     * Reads a java primitive double.
     * @return a double
     */
    public double readDouble() {
        return buffer.getDouble();
    }
    
    /**
     * Writes a java primitive double.
     * @param d
     */
    public void writeDouble(double d) {
        buffer.putDouble(d);
    }
    
    /**
     * Reads a java primitive char.
     * @return a char
     */
    public char readChar() {
        return buffer.getChar();
    }
    
    /**
     * Writes a java primitive char.
     * @param c
     */
    public void writeChar(char c) {
        buffer.putChar(c);
    }
    
    /**
     * Reads a java primitive byte.
     * @return a byte
     */
    public byte readByte() {
        return buffer.get();
    }
    
    /**
     * Writes a java primitive byte.
     * @param b
     */
    public void writeByte(byte b) {
        buffer.put(b);
    }
    
    /**
     * Reads a java primitive boolean.
     * @return a boolean
     */
    public boolean readBoolean() {
        return read_boolean();
    }
    
    /**
     * Writes a java primitive boolean.
     * @param b
     */
    public void writeBoolean(boolean b) {
        write_boolean(b);
    }
    
    
    
    /**
     * Reads in bytes(octets) from underlying buffer.
     * 
     * @param bytes
     */
    public void read(byte[] bytes) {
        buffer.get(bytes);
    }

    /**
     * Writes bytes(octets).
     * 
     * @param bytes
     */
    public void write(byte[] bytes) {
        buffer.put(bytes);
    }

    /**
     * Writes a String into this buffer. This method calls writeString(s, "ISO-8859-1") 
     * @param s String to write
     */
    public void writeString(String s) {
        writeString(s, "ISO-8859-1");
    }
    
    /**
     * Writes a String into this buffer with given charsetName. charsetName is used when getting the bytes of the
     * String. 
     * @param s String to write
     * @param charsetName Name of the charset
     * @throws RuntimeException that wraps UnsupportedEncodingException
     */
    public void writeString(String s, String charsetName) {
        buffer.putInt(s.length());
        
        try {
            buffer.put(s.getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Reads a String from the buffer. This method calls readString("ISO-8859-1")
     * @return a String
     */
    public String readString() {
        return readString("ISO-8859-1");
    }
    
    /**
     * Reads a String from the buffer. 
     * @param charsetName Name of of charset
     * @return a String
     * @throws RuntimeException that wraps UnsupportedEncodingException
     */
    public String readString(String charsetName) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Aligns this buffer to given byteBoundary.
     * 
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
     * 
     * @return ByteBuffer
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Sets the endianness used. see 9.4.5.1.2 flags
     * 
     * @param endianessFlag
     *            If true, little endian is used. If false, big endian is used.
     * 
     */
    public void setEndianess(boolean endianessFlag) {
        if (endianessFlag) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buffer.order(ByteOrder.BIG_ENDIAN);
        }
    }

    /**
     * Gets an InputStream reading from the backing ByteBuffer. InputStream will
     * start reading from current position of the ByteBuffer.
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
     * Gets an OutputStream that writes to this RTPSByteBuffer. Writing starts
     * at current position.
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
