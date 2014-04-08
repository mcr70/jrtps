package net.sf.jrtps.transport;

import net.sf.jrtps.message.Message;

interface Transmitter {
    public void sendMessage(Message msg);
    public void close();
}
