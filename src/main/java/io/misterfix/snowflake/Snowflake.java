package io.misterfix.snowflake;

/**
 * Twitter's snowflake algorithm -- java implementation
 * @author Some random chinese guy from some random gitlab setup
 * Comments and code translated and fine tuned by Mister_Fix
 *
 * From tests iv'e made this class is completley useless on it's own and need to be ran on a server to cetralize it
 * If you create a new Snowflake object everytime you need a snowflake you will instantly get duplicates which
 * defeats the purpose, and that's fine because that's how it's supposed to be.
 */
class Snowflake {
    /**
     * Number of bits occupied by each part
     */
    private final static long SEQUENCE_BIT = 12;  //Number of bits occupied by the serial number
    private final static long DATACENTER_BIT = 5; //Number of bits occupied by the PID
    private final static long INSTANCE_BIT = 5;   //Number of bits occupied by the thread ID

    /**
     * Maximum value of each part
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << INSTANCE_BIT);
    private final static long MAX_INSTANCE_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * Displacement of each part to the left
     */
    private final static long INSTANCE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + DATACENTER_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + INSTANCE_BIT;

    private long datacenterId;    //Datacenter ID
    private long instanceId;      //Instance ID
    private long sequence = 0L;   //Serial number
    private long lastStamp = -1L; //Last timestamp

    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }

    Snowflake(long datacenterId, long instanceId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (instanceId > MAX_INSTANCE_NUM || instanceId < 0) {
            throw new IllegalArgumentException("instanceId can't be greater than MAX_INSTANCE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.instanceId = instanceId;
    }

    /**
     * Generate the next ID
     *
     * @return long the Snowflake object
     */
    synchronized long nextId() {
        long currStamp = System.currentTimeMillis();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Time moved backwards. Refusing to generate Snowflake");
        }

        if (currStamp == lastStamp) {
            //In the same millisecond, the serial number is incremented.
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //The number of sequences in the same millisecond has reached the maximum
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //The serial number is set to 0 in different milliseconds.
            sequence = 0L;
        }

        lastStamp = currStamp;

        return currStamp << TIMESTAMP_LEFT                //Timestamp part
                | datacenterId << DATACENTER_LEFT         //Datacenter ID part
                | instanceId << INSTANCE_LEFT             //Instance ID part
                | sequence;                               //Serial number part
    }
}