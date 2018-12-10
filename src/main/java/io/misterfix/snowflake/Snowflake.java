package io.misterfix.snowflake;

/**
 * Twitter's snowflake algorithm -- java implementation
 * @author Some random chinese guy from some random gitlab setup
 * Comments and code translated and fine tuned by Mister_Fix
 *
 * From tests iv'e done this class is completley useless on it's own and needs to be ran on a server to centralize it
 * If you create a new Snowflake object everytime you need a snowflake you will instantly get duplicates which
 * defeats the purpose, and that's fine because that's how it's supposed to be.
 */
class Snowflake {
    /**
     * INFO OF VARIABLES FOR FUTURE REFERENCE
     *
     * Number of bits occupied by each part
     *  SEQUENCE_BIT = 12;  //the serial number
     *  DATACENTER_BIT = 5; //the datacenter ID
     *  INSTANCE_BIT = 5;   //the instance ID
     *
     * Maximum value of each part
     *  MAX_DATACENTER_NUM = 31;
     *  MAX_INSTANCE_NUM = 31;
     *  MAX_SEQUENCE = 4095;
     *
     * Displacement of each part to the left
     *  INSTANCE_LEFT = 12;
     *  DATACENTER_LEFT = 17;
     *  TIMESTAMP_LEFT = 22;
     */

    private long datacenterId;    //Datacenter ID
    private long instanceId;      //Instance ID
    private long sequence = 0L;   //Serial number
    private long lastStamp = -1L; //Last timestamp
    private long offset = 1436077819000L; //5th of july, 2015, 06:30:19 AM GMT

    Snowflake(long datacenterId, long instanceId) {
        if (datacenterId > 31 || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than 31 or less than 0");
        }
        if (instanceId > 31 || instanceId < 0) {
            throw new IllegalArgumentException("instanceId can't be greater than 31 or less than 0");
        }
        this.datacenterId = datacenterId;
        this.instanceId = instanceId;
    }

    /**
     * Generates the next snowflake on the object.
     * @return long the the snowflake
     */
    synchronized long nextId() {
        long currStamp = System.currentTimeMillis();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Time moved backwards. Refusing to generate Snowflake");
        }

        if (currStamp == lastStamp) {
            //In the same millisecond, the serial number is incremented.
            sequence = (sequence + 1) & 4095;
            //The number of sequences in the same millisecond has reached the maximum
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //The serial number is set to 0 in different milliseconds.
            sequence = 0L;
        }

        lastStamp = currStamp;

        return (currStamp - offset) << 22 //Timestamp part
                | datacenterId << 17      //Datacenter ID part
                | instanceId << 12        //Instance ID part
                | sequence;               //Serial number part
    }
    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }
}