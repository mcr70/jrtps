package net.sf.jrtps.transport;

import net.sf.jrtps.types.Locator;

public class UDPLocator extends Locator {

    public UDPLocator(int kind, int port, byte[] address) {
        super(kind, port, address);
    }

}
