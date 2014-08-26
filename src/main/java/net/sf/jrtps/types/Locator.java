package net.sf.jrtps.types;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
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
     * @param kind
     * @param port
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

    public InetAddress getInetAddress() {
        InetAddress inetAddress = null;

        try {
            switch (kind) {
            case LOCATOR_KIND_UDPv4:
                byte[] addr = new byte[4];
                addr[0] = address[12];
                addr[1] = address[13];
                addr[2] = address[14];
                addr[3] = address[15];
                inetAddress = InetAddress.getByAddress(addr);
                break;
            case LOCATOR_KIND_UDPv6:
                inetAddress = InetAddress.getByAddress(address);
                break;
            default:
                throw new IllegalArgumentException("Internal error: Unknown Locator kind: 0x"
                        + String.format("%04x", kind) + ", port: " + String.format("%04x", port));
            }
        } catch (UnknownHostException uhe) {
            // Not Possible. InetAddress.getByAddress throws this exception if
            // byte[] length is not
            // 4 or 16. We have ensured this in constructor & switch-case
        }

        return inetAddress;
    }

    /**
     * Get the remote socket address
     * 
     * @return SocketAddress
     */
    public SocketAddress getSocketAddress() {
        return new InetSocketAddress(getInetAddress(), port);
    }

    /**
     * Checks, if this Locator represents a multicast locator.
     * @return true, if multicast locator.
     */
    public boolean isMulticastLocator() {
        return getInetAddress().isMulticastAddress();
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
    
    public int getPort() {
        // TODO: check this. port is ulong (32 bits), but in practice, 16 last
        // bits (0-65535) is our port number
        // For large port numbers (> 32767), we need to mask out negativeness
        return port & 0xffff;
    }

    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(kind);
        buffer.write_long(port);
        buffer.write(address);
    }


    public String toString() {
        return kind + "::" + Arrays.toString(address) + ":" + getPort();
    }
}
