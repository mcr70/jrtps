package net.sf.jrtps.transport;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.sf.jrtps.types.Locator;

public class UDPLocator extends Locator {

    public UDPLocator(int kind, int port, byte[] address) {
        super(kind, port, address);
    }

    public UDPLocator(Locator locator) {
        super(locator.getKind(), locator.getPort(), locator.getAddress());
    }

    @Override
    public boolean isMulticastLocator() {
        return getInetAddress().isMulticastAddress();
    }

    /**
     * Get the remote socket address
     * 
     * @return SocketAddress
     */
    SocketAddress getSocketAddress() {
        return new InetSocketAddress(getInetAddress(), getPort());
    }    

    private InetAddress getInetAddress() {
        InetAddress inetAddress = null;
        int kind = getKind();
        byte[] address = getAddress();
        try {
            switch (kind) {
            case Locator.LOCATOR_KIND_UDPv4:
                byte[] addr = new byte[4];
                addr[0] = address[12];
                addr[1] = address[13];
                addr[2] = address[14];
                addr[3] = address[15];
                inetAddress = InetAddress.getByAddress(addr);
                break;
            case Locator.LOCATOR_KIND_UDPv6:
                inetAddress = InetAddress.getByAddress(address);
                break;
            default:
                throw new IllegalArgumentException("Internal error: Unknown Locator kind: 0x"
                        + String.format("%04x", kind) + ", port: " + String.format("%04x", getPort()));
            }
        } catch (UnknownHostException uhe) {
            // Not Possible. InetAddress.getByAddress throws this exception if
            // byte[] length is not
            // 4 or 16. We have ensured this in constructor & switch-case
        }

        return inetAddress;
    }
}
