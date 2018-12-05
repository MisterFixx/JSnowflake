package io.misterfix.snowflake;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

public class SocketServer {
    private static ServerSocket listener;

    public static void main(String[] args){
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        Snowflake snowflake = new Snowflake(tlr.nextInt(1, 31), tlr.nextInt(1, 31));
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSpec<Integer> threadCount = parser.accepts("thread-count").withRequiredArg().ofType(Integer.class).defaultsTo(100);
        OptionSpec<Integer> port = parser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(9098);
        OptionSet set = parser.parse(args);

        try {
            listener = new ServerSocket(set.valueOf(port));
            ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(set.valueOf(threadCount));
            threadPool.execute(()->{
                System.out.println("Started server on port "+set.valueOf(port)+" with workerThreads="+set.valueOf(threadCount));
                while (true) {
                    try (Socket socket = listener.accept()) {
                        new PrintWriter(socket.getOutputStream(), true).println(snowflake.nextId());
                    }
                    catch(IOException ignored){}
                }
            });
        }
        catch(IOException e){
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
