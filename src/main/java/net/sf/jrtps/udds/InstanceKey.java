package net.sf.jrtps.udds;

import java.util.Arrays;

class InstanceKey {
    private byte[] key;

    InstanceKey(byte[] key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InstanceKey) {
            InstanceKey other = (InstanceKey) o;

            return Arrays.equals(key, other.key);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    public String toString() {
        return "Key: " + Arrays.toString(key);
    }
}

