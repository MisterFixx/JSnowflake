package io.misterfix.snowflake;

/**
 * Twitter's snowflake algorithm -- java implementation
 *
 * @author Mister_Fix
 */
class Snowflake {

    /**
     * Starting timestamp
     */
    private final static long START_STAMP = 1420070400000L; //Using Discord's epoch for now

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
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    private long getNewStamp() {
        return System.currentTimeMillis();
    }

    Snowflake(long datacenter, long instanceId) {
        if (datacenter > MAX_DATACENTER_NUM || datacenter < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (instanceId > MAX_INSTANCE_NUM || instanceId < 0) {
            throw new IllegalArgumentException("instanceId can't be greater than MAX_INSTANCE_NUM or less than 0");
        }
        this.datacenterId = datacenter;
        this.instanceId = instanceId;
    }

    /**
     * Generate the next ID
     *
     * @return long the Snowflake object
     */
    synchronized long nextId() {
        long currStmp = getNewStamp();
        if (currStmp < lastStamp) {
            throw new RuntimeException("Time moved backwards. Refusing to generate Snowflake");
        }

        if (currStmp == lastStamp) {
            //In the same millisecond, the serial number is incremented.
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //The number of sequences in the same millisecond has reached the maximum
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //The serial number is set to 0 in different milliseconds.
            sequence = 0L;
        }

        lastStamp = currStmp;

        return (currStmp - START_STAMP) << TIMESTAMP_LEFT //Timestamp part
                | datacenterId << DATACENTER_LEFT         //Datacenter ID part
                | instanceId << INSTANCE_LEFT             //Instance ID part
                | sequence;                               //Serial number part
    }
}