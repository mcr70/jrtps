package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Locator;

public abstract class LocatorParameter extends Parameter {
    private Locator locator;

    /**
     * Constructor
     * @param pe Must be one to define a locator. No check is made.
     */
    protected LocatorParameter(ParameterId pe) {
        super(pe);
    }

    /**
     * Constructor
     * @param pe Must be one to define a locator. No check is made.
     * @param locator Locator
     */
    protected LocatorParameter(ParameterId pe, Locator locator) {
        super(pe);
        this.locator = locator;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.locator = new Locator(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        locator.writeTo(bb);
    }

    public Locator getLocator() {
        return locator;
    }

    public String toString() {
        return super.toString() + ": " + getLocator();
    }
}
