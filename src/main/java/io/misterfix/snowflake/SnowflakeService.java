package io.misterfix.snowflake;

import simplenet.Client;
import simplenet.Server;
import simplenet.packet.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class SnowflakeService {
    private static final Snowflake snowflake = Main.getSnowflake();
    private static final Map<Client, Long> clientMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(5);
    private static AtomicInteger ids_served = new AtomicInteger();

    SnowflakeService(){
        Server server = new Server();
        server.onConnect(client -> {
            clientMap.putIfAbsent(client, System.currentTimeMillis());
            client.readIntAlways(value -> {
                if(value <= 100000){
                    Packet packet = Packet.builder();

                    for(int i = 0; i < value; i++) {
                        packet.putLong(snowflake.nextId());
                        if(packet.getSize() >= 4096){
                            packet.writeAndFlush(client);
                            packet = Packet.builder();
                        }
                    }

                    packet.writeAndFlush(client);
                    clientMap.replace(client, System.currentTimeMillis());
                    ids_served.getAndAdd(value);
                }
            });

            client.preDisconnect(()-> clientMap.remove(client));
        });
        server.bind("localhost", Main.getPort());


        threadPool.scheduleAtFixedRate(()-> clientMap.forEach((client, lastActivity) -> {
            if((System.currentTimeMillis() - lastActivity) > 10000){
                client.close();
            }
        }), 20, 1, TimeUnit.SECONDS);
    }

    static AtomicInteger getIdsServed(){ return ids_served; }
}
