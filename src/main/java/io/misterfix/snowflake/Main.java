package io.misterfix.snowflake;


import com.beust.jcommander.Parameter;

public class Main {
    @Parameter(names = {"--port"}, description = "The port on which the snowflake service will run. Default 9098")
    private static final int port = 9098;
    @Parameter(names = {"--admin-port"}, description = "Admin service port. Default 9099")
    private static final int adminPort = 9099;
    @Parameter(names = {"--datacenter-id"}, description = "Numeric datacenter ID on which this instance of snowflake will run (0 - 31). Default 1")
    private static final int datacenterId = 1;
    @Parameter(names = {"--instance-id"}, description = "Numeric instance ID which runs of this datacenter (0 - 31). Default 1")
    private static final int instanceId = 1;
    @Parameter(names = {"--epoch"}, description = "Snowflake epoch. Default 1436077819000 or Sunday, July 5, 2015 6:30:19 AM")
    private static final long epoch = 1436077819000L;
    @Parameter(names = {"--max-ids-per-socket"}, description = "Maximum amount of Snowflakes that can be served in a single request. Default 131072")
    private static final int max_snowflakes_per_socket = 131072;

    private static Snowflake snowflake = new Snowflake(datacenterId, instanceId, epoch);

    public static void main(String[] args) {
        new AdminService();
        new SnowflakeService();

        System.out.println("Snowflake service started on port "+port+".");
        System.out.println("Snowflake Admin service started on port "+adminPort+".");
    }

    static Snowflake getSnowflake(){return snowflake;}
    static int getPort(){ return port; }
    static int getAdminPort(){ return adminPort; }
    static int getDatacenterId(){ return datacenterId; }
    static int getInstanceId(){ return instanceId; }
    static long getEpoch(){ return epoch; }
    static int getMaxSnowflakesPerSocket(){ return max_snowflakes_per_socket; }
}
