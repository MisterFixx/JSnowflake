package io.misterfix.snowflake;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import simplenet.Client;
import simplenet.Server;
import simplenet.packet.Packet;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketServer{
    private static Snowflake snowflake;
    private static AtomicInteger ids_served = new AtomicInteger();
    private static final Map<Client, Long> clientMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(5);
    private static final int maxSnowflakesPerRequest = 500000;

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSpec<Integer> port = parser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(9098);
        OptionSpec<Integer> datacenterId = parser.accepts("datacenter-id").withRequiredArg().ofType(Integer.class).defaultsTo(1);
        OptionSpec<Integer> instanceId = parser.accepts("instance-id").withRequiredArg().ofType(Integer.class).defaultsTo(1);
        OptionSpec<String> adminServiceUser = parser.accepts("admin-username").withRequiredArg().ofType(String.class).defaultsTo("user");
        OptionSpec<String> adminServicePass = parser.accepts("admin-pass").withRequiredArg().ofType(String.class).defaultsTo("pass");
        OptionSpec<Integer> adminServicePort = parser.accepts("admin-port").withRequiredArg().ofType(Integer.class).defaultsTo(9099);
        OptionSpec<Long> epoch = parser.accepts("epoch").withRequiredArg().ofType(Long.class).defaultsTo(1436077819000L);
        OptionSet set = parser.parse(args);

        snowflake = new Snowflake(set.valueOf(datacenterId), set.valueOf(instanceId), set.valueOf(epoch));
        new AdminService(set.valueOf(adminServicePort), set.valueOf(instanceId), set.valueOf(datacenterId), set.valueOf(adminServiceUser), set.valueOf(adminServicePass), set.valueOf(epoch));

        Server server = new Server((maxSnowflakesPerRequest*18)+500);
        server.onConnect(client -> {
            clientMap.putIfAbsent(client, System.currentTimeMillis());
            client.readIntAlways(value -> {
                if(value < maxSnowflakesPerRequest){
                    String[] snowflakes = new String[value];
                    for(int i = 0; i < value; i++) {
                        snowflakes[i] = Long.toString(snowflake.nextId());
                        ids_served.getAndIncrement();
                    }
                    Packet.builder().putBytes(String.join("\n", snowflakes).getBytes(StandardCharsets.UTF_8)).writeAndFlush(client);
                    clientMap.replace(client, System.currentTimeMillis());
                }
            });
            client.preDisconnect(()-> clientMap.remove(client));
        });
        server.bind("localhost", set.valueOf(port));

        threadPool.scheduleAtFixedRate(()-> clientMap.forEach((client, lastActivity) -> {
            if((System.currentTimeMillis() - lastActivity) > 10000){
                client.close();
            }
        }), 20, 1, TimeUnit.SECONDS);

        System.out.println("Snowflake service started on port "+set.valueOf(port)+".");
        System.out.println("Snowflake Admin service started on port "+set.valueOf(adminServicePort)+".");

    }

    static AtomicInteger getIdsServed(){
        return ids_served;
    }
}
