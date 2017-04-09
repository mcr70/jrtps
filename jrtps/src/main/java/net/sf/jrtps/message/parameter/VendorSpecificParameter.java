package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class VendorSpecificParameter extends Parameter {
    private short vendorParamId;

    VendorSpecificParameter(short paramId) {
        super(ParameterId.PID_VENDOR_SPECIFIC);

        this.vendorParamId = paramId;
    }

    public short getVendorParameterId() {
        return vendorParamId;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb);
    }

    public String toString() {
        byte[] bytes = getBytes();
        if (bytes != null) {
            return "VendorSpecifiParameter(" + String.format("0x%04x", vendorParamId) + ")" + Arrays.toString(bytes);
        }

        return "VendorSpecifiParameter(" + String.format("0x%04x", vendorParamId) + ")";
    }
}