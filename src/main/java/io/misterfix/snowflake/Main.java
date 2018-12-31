package io.misterfix.snowflake;


import com.beust.jcommander.Parameter;
import simplenet.Client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static Snowflake snowflake;
    private static AtomicInteger ids_served = new AtomicInteger();
    private static Map<Client, Long> clientMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(5);

    @Parameter(names = {"--port"}, description = "The port on which the snowflake service will run. Default 9098")
    private static final int port = 9098;
    @Parameter(names = {"--admin-port"}, description = "Admin service port. Default 9099")
    private static final int adminPort = 9099;
    @Parameter(names = {"--datacenter-id"}, description = "Numeric datacenter ID on which this instance of snowflake will run (0 - 31). Default 1")
    private static final int datacenterId = 1;
    @Parameter(names = {"--instance-id"}, description = "Numeric instance ID which runs of this datacenter (0 - 31). Default 1")
    private static final int instanceId = 1;
    @Parameter(names = {"--epoch"}, description = "Snowflake epoch")
    private static final long epoch = 1436077819000L;

    public static void main(String[] args) {
        snowflake = new Snowflake(datacenterId, instanceId, epoch);
        new AdminService(adminPort, instanceId, datacenterId, epoch);
        new SnowflakeService(port);

        threadPool.scheduleAtFixedRate(()-> clientMap.forEach((client, lastActivity) -> {
            if((System.currentTimeMillis() - lastActivity) > 10000){
                client.close();
            }
        }), 20, 1, TimeUnit.SECONDS);

        System.out.println("Snowflake service started on port "+port+".");
        System.out.println("Snowflake Admin service started on port "+adminPort+".");
    }

    static AtomicInteger getIdsServed(){
        return ids_served;
    }
    static Map<Client, Long> getClientMap() {return clientMap;}
    static Snowflake getSnowflake(){return snowflake;}
}
