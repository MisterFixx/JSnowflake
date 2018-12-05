package io.misterfix.snowflake;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SocketServer {
    private static int generatedIds = 0;
    private static long pid = ProcessHandle.current().pid();

    public static void main(String[] args){
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSpec<Integer> threadCount = parser.accepts("thread-count").withRequiredArg().ofType(Integer.class).defaultsTo(60);
        OptionSpec<Integer> port = parser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(9090);
        OptionSet set = parser.parse(args);
        try {
            final ServerSocket listener = new ServerSocket(set.valueOf(port));
            ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(set.valueOf(threadCount));
            threadPool.execute(()->{
                System.out.println("Started server on port "+set.valueOf(port)+" with workerThreads="+set.valueOf(threadCount));
                while (true) {
                    try (Socket socket = listener.accept()) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        long time = System.currentTimeMillis();
                        long threadId = Thread.currentThread().getId();
                        out.println(time+""+threadId+""+pid+""+generatedIds);
                        generatedIds++;
                    }
                    catch(IOException ignored){}
                }
            });
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
