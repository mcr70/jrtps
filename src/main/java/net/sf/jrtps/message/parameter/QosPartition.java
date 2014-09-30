package net.sf.jrtps.message.parameter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class QosPartition extends Parameter implements SubscriberPolicy<QosPartition>, PublisherPolicy<QosPartition>,
        InlineQoS {
    private String[] partitions;

    QosPartition() {
        super(ParameterEnum.PID_PARTITION);
    }

    public QosPartition(String[] partitions) {
        super(ParameterEnum.PID_PARTITION);
        this.partitions = partitions;

        if (this.partitions == null) {
            this.partitions = new String[0];
        }
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        int len = bb.read_long();
        this.partitions = new String[len];
        for (int i = 0; i < len; i++) {
            partitions[i] = bb.read_string();
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(partitions.length);
        for (int i = 0; i < partitions.length; i++) {
            bb.write_string(partitions[i]);
        }
    }

    /**
     * Checks whether or not two QosPartition policies are compatible or not.
     * For the two to be compatible, at least one partition must be shared.
     * 
     * Note, that regular expression comparison is done first from 'this'
     * policys partitions. If no matching partitions are found that way,
     * 'other' policys partitions are used as regular expression and compared
     * to 'this' policys partitions.
     * 
     * Note, that this yields to implementation that is not strictly comaptible
     * with DDS specification, as spec says that two partitions cannot be matched
     * if both contain regular expressions. I.e. this check is not made.
     */
    @Override
    public boolean isCompatible(QosPartition other) {
        
        if (partitions.length == 0 && other.partitions.length == 0) {
            return true;
        }
        
        // Try to match with regular expression from 'this' partitions
        for (String partition : partitions) {
            Pattern p = Pattern.compile(partition);
            for (String otherPartition : other.partitions) {
                Matcher matcher = p.matcher(otherPartition);
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        
        // Try to match with regular expression from 'other' partitions
        for (String otherPartition : other.partitions) {
            Pattern p = Pattern.compile(otherPartition);
            for (String partition : partitions) {
                Matcher matcher = p.matcher(partition);
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        
        return false; // TODO: Partition matching is done differently
    }

    /**
     * Default partition.
     * 
     * @return QosPartition
     */
    public static QosPartition defaultPartition() {
        return new QosPartition(new String[0]);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < partitions.length; i++) {
            sb.append(partitions[i]);
            if (i < partitions.length - 1) {
                sb.append(",");
            }
        }
        return super.toString() + "([" + sb + "])";
    }
}