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
    private final static long START_STMP = 1420070400000L;

    /**
     * Number of bits occupied by each part
     */
    private final static long SEQUENCE_BIT = 12; //Number of bits occupied by the serial number
    private final static long PID_BIT = 5;       //Number of bits occupied by the PID
    private final static long THREAD_BIT = 5;    //Number of bits occupied by the thread ID

    /**
     * Maximum value of each part
     */
    private final static long MAX_PID_NUM = ~(-1L << THREAD_BIT);
    private final static long MAX_THREAD_NUM = ~(-1L << PID_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * Displacement of each part to the left
     */
    private final static long THREAD_LEFT = SEQUENCE_BIT;
    private final static long PID_LEFT = SEQUENCE_BIT + PID_BIT;
    private final static long TIMESTMP_LEFT = PID_LEFT + THREAD_BIT;

    private long pid;            //Process ID
    private long threadId;       //Thread ID
    private long sequence = 0L;  //Serial number
    private long lastStmp = -1L; //Last timestamp

    Snowflake(long pid, long workerId) {
        if (pid > MAX_PID_NUM || pid < 0) {
            throw new IllegalArgumentException("pid can't be greater than MAX_PID_NUM or less than 0");
        }
        if (workerId > MAX_THREAD_NUM || workerId < 0) {
            throw new IllegalArgumentException("workerId can't be greater than MAX_THREAD_NUM or less than 0");
        }
        this.pid = pid;
        this.threadId = workerId;
    }

    /**
     * Generate the next ID
     *
     * @return long the Snowflake object
     */
    synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
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

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //Timestamp part
                | pid << PID_LEFT                       //PID part
                | threadId << THREAD_LEFT               //Thread ID part
                | sequence;                             //Serial number part
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }
}