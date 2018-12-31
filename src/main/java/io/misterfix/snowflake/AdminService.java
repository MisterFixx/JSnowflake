package io.misterfix.snowflake;

import spark.Spark;

import java.text.SimpleDateFormat;
import java.util.Date;

class AdminService {
    AdminService(int port, int instanceId, int datacenterId, long epoch){
        Spark.port(port);
        Spark.get("/status", (req, res) ->
                "<html>\n" +
                "    <head>\n" +
                "        <title>Snowflake Report</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <table>\n" +
                "            <tr><td>Datacenter Id</td><td> "+datacenterId+"</td></tr>\n" +
                "            <tr><td>Instance Id</td><td> "+instanceId+"</td></tr>\n" +
                "            <tr><td>Timestamp</td><td> "+System.currentTimeMillis()+"</td></tr>\n" +
                "            <tr><td>Epoch</td><td> "+epoch+"</td></tr>\n" +
                "            <tr><td>Time</td><td> "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())+"</td></tr>\n" +
                "            <tr><td>IDs Generated</td><td> "+ Main.getIdsServed().get()+"</td></tr>\n" +
                "        </table>\n" +
                "    </body>\n" +
                "</html>"
        );
    }
}
