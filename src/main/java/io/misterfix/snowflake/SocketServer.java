package io.misterfix.snowflake;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.net.InetSocketAddress;

public class SocketServer extends ChannelInboundHandlerAdapter {
    private static Snowflake snowflake;
    private static long ids_served;

    public static void main(String[] args) throws InterruptedException {
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
        EventLoopGroup group = new NioEventLoopGroup(80);
        new AdminService(set.valueOf(adminServicePort), set.valueOf(instanceId), set.valueOf(datacenterId), set.valueOf(adminServiceUser), set.valueOf(adminServicePass), set.valueOf(epoch));

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress("localhost", set.valueOf(port)));
            System.out.println("Snowflake service started on port "+set.valueOf(port)+".");
            System.out.println("Snowflake Admin service started on port "+set.valueOf(adminServicePort)+".");
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                public void initChannel(SocketChannel socketChannel) {
                    socketChannel.writeAndFlush(Unpooled.copiedBuffer(String.valueOf(snowflake.nextId()), CharsetUtil.UTF_8)).addListener(ChannelFutureListener.CLOSE);
                    ids_served++;
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            group.shutdownGracefully().sync();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    static long getIdsServed(){
        return ids_served;
    }
}
