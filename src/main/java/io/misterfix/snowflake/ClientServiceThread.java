package io.misterfix.snowflake;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

class ClientServiceThread extends Thread {
    private Socket clientSocket;
    private Snowflake snowflake;

    ClientServiceThread(Socket socket, Snowflake Snowflake) {
        clientSocket = socket;
        snowflake = Snowflake;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            out.println(snowflake.nextId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}