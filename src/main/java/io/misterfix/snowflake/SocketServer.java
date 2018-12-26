package io.misterfix.snowflake;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import simplenet.Server;
import simplenet.packet.Packet;

public class SocketServer{
    private static Snowflake snowflake;
    private static long ids_served;

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

        Server server = new Server();
        server.onConnect(client -> client.readByteAlways(value -> {
            System.out.println(value);
            Packet.builder().putLong(snowflake.nextId()).writeAndFlush(client);
            ids_served++;
        }));
        server.bind("localhost", set.valueOf(port));

        System.out.println("Snowflake service started on port "+set.valueOf(port)+".");
        System.out.println("Snowflake Admin service started on port "+set.valueOf(adminServicePort)+".");

    }

    static long getIdsServed(){
        return ids_served;
    }
}
