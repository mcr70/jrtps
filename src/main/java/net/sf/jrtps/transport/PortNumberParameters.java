package net.sf.jrtps.transport;

/**
 * This class encapsulates parameters used in calculation of port numbers.
 * <table border='1'>
 * <tr><th>Traffic type</th> <th>Port number expression</th></tr>
 * <tr><td>Discovery multicast</td> <td>PB + DG * domainId + d0</td></tr>
 * <tr><td>Discovery unicast</td> <td>PB + DG * domainId + d1 + PG * participantId</td></tr>
 * <tr><td>User traffic multicast</td> <td>PB + DG * domainId + d2</td></tr>
 * <tr><td>User traffic unicast</td> <td>PB + DG * domainId + d3 + PG * participantId</td></tr>
 * </table>
 * For more details, see RTPS spec, ch. 9.6.1.1 and 9.6.1.2.
 * 
 * @author mcr70
 */
public class PortNumberParameters {
    private final int pb;
    private final int dg;
    private final int pg;
    private final int d0;
    private final int d1;
    private final int d2;
    private final int d3;

    public PortNumberParameters(int pb, int dg, int pg, int d0, int d1, int d2, int d3) {
        this.pb = pb > 0 ? pb : 7400;
        this.dg = dg > 0 ? dg : 250;
        this.pg = pg > 0 ? pg : 2;
        this.d0 = d0 > 0 ? d0 : 0;
        this.d1 = d1 > 0 ? d1 : 10;
        this.d2 = d2 > 0 ? d2 : 1;
        this.d3 = d3 > 0 ? d3 : 11;
    }
    
    /**
     * Gets the port base number (PB)-
     * @return Port Base, defaults to 7400
     */
    public int getPortBase() {
        return pb;
    }
    
    /**
     * Gets the DomainId Gain (DG). 
     * @return DomainId Gain, defaults to 250
     */
    public int getDomainIdGain() {
        return dg;
    }

    /**
     * Gets the ParticipantId Gain (PG).
     * @return ParticipantId Gain, defaults to 2
     */
    public int getParticipantIdGain() {
        return pg;
    }
    
    /**
     * d0 offset
     * @return d0 offset, defaults to 0
     */
    public int getD0() {
        return d0;
    }    

    /**
     * d1 offset
     * @return d1 offset, defaults to 10
     */
    public int getD1() {
        return d1;
    }
    
    /**
     * d2 offset
     * @return d2 offset, defaults to 1
     */    
    public int getD2() {
        return d2;
    }
    
    /**
     * d3 offset
     * @return d3 offset, defaults to 11
     */    
    public int getD3() {
        return d3;
    }
    
    
    public int getDiscoveryMulticastPort(int domainId) {
        return pb + dg * domainId + d0;
    }

    public int getDiscoveryUnicastPort(int domainId, int participantId) {
        return pb + dg * domainId + d1 + pg * participantId;
    }
    
    public int getUserdataMulticastPort(int domainId) {
        return pb + dg * domainId + d2;
    }
    
    public int getUserdataUnicastPort(int domainId, int participantId) {
        return pb + dg * domainId + d3 + pg * participantId;
    }
    
    public String toString() {
        return "PB=" + pb + ",DG=" + dg + ",PG=" + pg + ",d0=" + d0 + ",d1=" + d1 + ",d2=" + d2 + ",d3=" + d3;
    }
}
