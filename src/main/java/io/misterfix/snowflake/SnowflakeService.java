package io.misterfix.snowflake;

import simplenet.Client;
import simplenet.Server;
import simplenet.packet.Packet;

import java.nio.charset.StandardCharsets;
import java.util.Map;

class SnowflakeService {
    private static final Snowflake snowflake = Main.getSnowflake();
    private static final Map<Client, Long> clientMap = Main.getClientMap();

    SnowflakeService(int port){
        Server server = new Server();
        server.onConnect(client -> {
            clientMap.putIfAbsent(client, System.currentTimeMillis());
            client.readIntAlways(value -> {
                if(value <= 100000){
                    Packet packet = Packet.builder();

                    for(int i = 0; i < value; i++) {
                        String out = Long.toString(snowflake.nextId())+"\n";
                        packet.putBytes(out.getBytes(StandardCharsets.UTF_8));

                        if(packet.getSize() >= 4000){
                            packet.writeAndFlush(client);
                            packet = Packet.builder();
                        }
                    }

                    packet.writeAndFlush(client);
                    clientMap.replace(client, System.currentTimeMillis());
                    Main.getIdsServed().getAndAdd(value);
                }
            });

            client.preDisconnect(()-> clientMap.remove(client));
        });
        server.bind("localhost", port);
    }
}
