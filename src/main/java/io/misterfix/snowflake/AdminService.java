package io.misterfix.snowflake;

import com.qmetric.spark.authentication.AuthenticationDetails;
import com.qmetric.spark.authentication.BasicAuthenticationFilter;
import spark.Spark;

import java.text.SimpleDateFormat;
import java.util.Date;

class AdminService {
    AdminService(int port, int instanceId, int datacenterId){
        Spark.port(port);
        Spark.before(new BasicAuthenticationFilter("/self_test/*", new AuthenticationDetails("Mister_Fix", "ZT2z0nrsQ8o")));

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
                "            <tr><td>Time</td><td> "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())+"</td></tr>\n" +
                "            <tr><td>IDs Generated</td><td> "+SocketServer.ids_served+"</td></tr>\n" +
                "        </table>\n" +
                "    </body>\n" +
                "</html>");

        Spark.get("/self_test", (request, response) -> {
            Snowflake snowflake = new Snowflake(31, 31);
            long gen_test_start = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                snowflake.nextId();
            }
            long gen_test_end = System.currentTimeMillis();

            return  "<html>\n" +
                    "    <head>\n" +
                    "        <title>Snowflake self test</title>\n" +
                    "    </head>\n" +
                    "    <body>\n" +
                    "        <table>\n" +
                    "            <tr><td>Time to generate 10,000 IDs:</td><td> "+(gen_test_end-gen_test_start)+"s</td></tr>\n" +
                    "            <tr><td>IDs per second:</td><td> "+(10000/(gen_test_end-gen_test_start))+"</td></tr>\n" +
                    "        </table>\n" +
                    "    </body>\n" +
                    "</html>";
        });

        Spark.awaitInitialization();
    }
}
