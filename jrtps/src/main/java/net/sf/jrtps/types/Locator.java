package net.sf.jrtps.types;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Locator is used to tell remote participants how a participant can be reached.
 * 
 * @author mcr70
 */
public class Locator {
    /**
     * This kind is used for communication with UDPv4
     */
    public static final int LOCATOR_KIND_UDPv4 = 1;
    /**
     * This kind is used for communication with UDPv6
     */
    public static final int LOCATOR_KIND_UDPv6 = 2;
    /**
     * An invalid Locator kind 
     */
    public static final int LOCATOR_KIND_INVALID = -1;

    private int kind;
    private int port;
    private byte[] address;

    public Locator(InetAddress addr, int port) {
        this.port = port;
        
        byte[] bytes = addr.getAddress();

        if (bytes.length == 4) {
            this.kind = LOCATOR_KIND_UDPv4;

            this.address = new byte[16];
            this.address[12] = bytes[0];
            this.address[13] = bytes[1];
            this.address[14] = bytes[2];
            this.address[15] = bytes[3];
        } else if (bytes.length == 16) {
            this.kind = LOCATOR_KIND_UDPv6;
            this.address = bytes;
        } else {
            throw new IllegalArgumentException("Invalid length for InetAddress.getAddress: " + bytes.length);
        }
    }

    public Locator(RTPSByteBuffer is) {
        this.kind = is.read_long();
        this.port = is.read_long();
        this.address = new byte[16];

        is.read(address);
    }

    /**
     * Create new Locator.
     * @param kind Kind
     * @param port port
     * @param address address must be an array of length 16
     * @throws IllegalArgumentException if address is not of length 16
     */
    public Locator(int kind, int port, byte[] address) {
        this.kind = kind;
        this.port = port;
        this.address = address;
        
        if (address.length != 16) {
            throw new IllegalArgumentException("address must be an array of length 16");
        }
    }

    
    /** 
     * Gets the kind of this Locator. Each Locator has a kind associated with it to distinguish different types 
     * of communication mechanisms from each other. For example, kind can be used to distinguish UDP from TCP. 
     * RTPS specification defines only two mandatory kinds: for UDPv4 and UDPv6
     *  
     * @return kind 
     */
    public int getKind() {
        return kind;
    }
    
    /**
     * Gets the port of this Locator.
     * @return port
     */
    public int getPort() {
        // TODO: check this. port is ulong (32 bits), but in practice, 16 last
        // bits (0-65535) is our port number
        // For large port numbers (> 32767), we need to mask out negativeness
        return port & 0xffff;
    }
    
    /**
     * Gets the address of this Locator.
     * @return address
     */
    public byte[] getAddress() {
        return address;
    }
    
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(kind);
        buffer.write_long(port);
        buffer.write(address);
    }


    public String toString() {
        return "Locator(" + kind + ", " + Arrays.toString(address) + ":" + getPort() + ")";
    }
    
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Locator) {
            Locator loc = (Locator) other;
            return kind == loc.kind && port == loc.port && Arrays.equals(address, loc.address);
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        byte[] locBytes = new byte[24];
        ByteBuffer bb = ByteBuffer.wrap(locBytes);
        bb.putInt(kind).putInt(port).put(address);
        
        return Arrays.hashCode(locBytes);
    }

    public boolean isMulticastLocator() {
        return false;
    }
}
