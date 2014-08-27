package net.sf.jrtps.transport.mem;

import java.net.URI;
import java.nio.ByteBuffer;

import net.sf.jrtps.types.Locator;

public class MemLocator extends Locator {
    public MemLocator(URI uri) {
        super(0x8000, uri.getPort(), ByteBuffer.wrap(new byte[16]).putInt(uri.toString().hashCode()).array());
    }   
    
    
    @Override
    public boolean isMulticastLocator() {
        return true;
    }
}
