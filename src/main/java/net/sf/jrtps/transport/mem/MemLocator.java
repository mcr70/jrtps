package net.sf.jrtps.transport.mem;

import java.net.URI;
import java.nio.ByteBuffer;

import net.sf.jrtps.types.Locator;

public class MemLocator extends Locator {
    public MemLocator(URI uri) {
        super(MemProvider.LOCATOR_KIND_MEM, uri.getPort(), ByteBuffer.wrap(new byte[16]).putInt(uri.toString().hashCode()).array());
    }   
    
    public MemLocator(URI uri, int port) {
        super(MemProvider.LOCATOR_KIND_MEM, port, ByteBuffer.wrap(new byte[16]).putInt(uri.toString().hashCode()).array());
    }   

}
