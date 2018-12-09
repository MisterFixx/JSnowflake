package io.misterfix.snowflake;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private static ServerSocket listener;

    public static void main(String[] args){
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSpec<Integer> port = parser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(9098);
        OptionSpec<Integer> datacenterId = parser.accepts("datacenter-id").withRequiredArg().ofType(Integer.class).defaultsTo(1);
        OptionSpec<Integer> instanceId = parser.accepts("instance-id").withRequiredArg().ofType(Integer.class).defaultsTo(1);
        OptionSet set = parser.parse(args);
        Snowflake snowflake = new Snowflake(set.valueOf(datacenterId), set.valueOf(instanceId));

        try {
            listener = new ServerSocket(set.valueOf(port));
            System.out.println("Snowflake server started on port "+set.valueOf(port)+".");
            while (!listener.isClosed()) {
                Socket socket = listener.accept();
                new PrintWriter(socket.getOutputStream(), true).println(snowflake.nextId());
            }
        } catch(IOException e){
            if(e.getClass() == BindException.class){
                System.out.println("Cannot start Snowflake server - Address is already in use!");
                System.exit(1);
            }
            else {
                e.printStackTrace();
                System.exit(2);
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            try {
                listener.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }));
    }
}
