package net.sf.jrtps.message;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class OSPLTest {
    public static void main(String[] args) throws IllegalMessageException {
        String s1 = "52545053 0201cafe 93610200 0e0f0a0c 0e0b0a0b @0x14 07020000 c7040000 c2040000 00000000 00000000 00000000 00000000 01000000";
        //String s2 = "52545053 0201cafe 93610200 0e0f0a0c 0e0b0a0b @0x14 07020000 c7040000 c2040000 00000000 00000000 00000000 00000000 01000000";

        // Description : malformed packet received from vendor 202.254 state parse:heartbeat <52545053 0201cafe 93610200 0e0f0a0c 0
        //  e0b0a0b @0x14 07020000 c7040000 c2040000 00000000 00000000 00000000 00000000 01000000> (note: maybe partially bswap'd) {
        //  {7,2,0},4c7,4c2,0,0}

        String[] sa = s1.split(" ");
        System.out.println(Arrays.toString(sa));
        
        RTPSByteBuffer bb = new RTPSByteBuffer(new byte[sa.length * 8 - 3]);
        consumeOSPLDump(sa, bb);
        Message msg = new Message(bb);
        
        System.out.println(msg);
    }

    private static void consumeOSPLDump(String[] sa, RTPSByteBuffer bb) {
        
        for (int i = 0; i < sa.length; i++) {
            if (sa[i].charAt(0) == '@') {
                continue;
            }
            
            String[] byteStrings = sa[i].split("(?<=\\G..)");
            System.out.println(Arrays.toString(byteStrings));

            for (String byteStr : byteStrings) {
                bb.write_octet((byte) (Integer.parseInt(byteStr, 16) & 0xff));
            }
        }
        
        bb.getBuffer().flip();
    }
}
