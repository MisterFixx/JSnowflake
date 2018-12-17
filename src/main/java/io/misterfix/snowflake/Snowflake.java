package io.misterfix.snowflake;

class Snowflake {
    private long datacenterId;    //Datacenter ID
    private long instanceId;      //Instance ID
    private long sequence = 0L;   //Serial number
    private long lastStamp = -1L; //Last timestamp
    //5th of july, 2015, 06:30:19 AM GMT
    private long offset = 1436077819000L; // offset timestamp, it's here to avoid creating a variable for it every time an id is generated.

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
            sequence = 0L;
        }

        lastStamp = currStamp;

        return (currStamp - offset) << 22
                | datacenterId << 17
                | instanceId << 12
                | sequence;
    }

    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }
}