package io.misterfix.snowflake;

class Snowflake {
    private int datacenterId;     //Datacenter ID
    private int instanceId;       //Instance ID
    private int sequence = 0;     //Serial number
    private long lastStamp = -1L; //Last timestamp
    private long epoch;

    Snowflake(int datacenterId, int instanceId, long epoch) {
        if (datacenterId > 31 || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than 31 or less than 0");
        }
        if (instanceId > 31 || instanceId < 0) {
            throw new IllegalArgumentException("instanceId can't be greater than 31 or less than 0");
        }
        this.datacenterId = datacenterId;
        this.instanceId = instanceId;
        this.epoch = epoch;
    }

    synchronized long nextId() {
        long currStamp = System.currentTimeMillis();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Time moved backwards. Refusing to generate Snowflake");
        }

        if (currStamp == lastStamp) {
            sequence = (sequence + 1) & 4095;
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            sequence = 0;
        }
        lastStamp = currStamp;

        return (currStamp - epoch) << 22 | datacenterId << 17 | instanceId << 12 | sequence;
    }

    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }
}