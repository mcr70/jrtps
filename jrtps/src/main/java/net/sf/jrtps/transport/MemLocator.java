package net.sf.jrtps.transport;

import java.net.URI;
import java.nio.ByteBuffer;

import net.sf.jrtps.types.Locator;

/**
 * Locator for memory based transport
 *
 * @author mcr70
 */
class MemLocator extends Locator {
    MemLocator(Locator loc) {
        super(MemProvider.LOCATOR_KIND_MEM, loc.getPort(), ByteBuffer.wrap(new byte[16]).putInt(loc.toString().hashCode()).array());
    }   
    MemLocator(URI uri) {
        super(MemProvider.LOCATOR_KIND_MEM, uri.getPort(), ByteBuffer.wrap(new byte[16]).putInt(uri.toString().hashCode()).array());
    }   
}
