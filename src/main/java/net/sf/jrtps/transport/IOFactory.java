package net.sf.jrtps.transport;

import java.util.concurrent.BlockingQueue;

import net.sf.jrtps.types.Locator;

class IOFactory {
    Receiver createReceiver(Locator locator, BlockingQueue<byte[]> queue, int bufferSize) {
        
        return null;
    }
    Transmitter createTransmitter(Locator locator, int bufferSize) {
        return null;
    }
}
