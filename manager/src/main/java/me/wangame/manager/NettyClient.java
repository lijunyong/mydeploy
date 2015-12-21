package me.wangame.manager;

import com.sun.tools.attach.VirtualMachine;
import me.wangame.manager.po.NettyRequest;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * Created by zzy
 */
public class NettyClient {

    public static void main(String... args) {

        ChannelFactory factory =
                new NioClientSocketChannelFactory(
                        Executors.newSingleThreadExecutor(),
                        Executors.newSingleThreadExecutor());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(1024*1000, Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast("handler", new SimpleChannelHandler() {
                    @Override
                    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                        System.out.println("message:" + e.getMessage());
                    }
                });
                return pipeline;
            }
        });
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        String host = "127.0.0.1";
        int port = 28888;
        if (args.length >= 2) {
            host = args[0];
            port = Integer.valueOf(args[1]);
        }
        Channel channel = bootstrap.connect(new InetSocketAddress(host, port)).getChannel();
        Scanner sc = new Scanner(System.in);
        String line = "";
        do {
            line = sc.nextLine();
            if (line.equals("help")) {
                help();
                continue;
            }
            NettyRequest request = new NettyRequest();
            request.setUid("system");
            if (line.startsWith("loadAgent,")) {
                try {
                    String targetVmPid = "";
                    String[]ss = line.split(",");

                    List<String> cmds = new ArrayList<String>();
                    cmds.add("sh");
                    cmds.add("-c");
                    cmds.add("jps|grep NettyServerBootstrap|awk '{print $1}'");
                    ProcessBuilder pb=new ProcessBuilder(cmds);
                    Process process = pb.start();
                    InputStream input = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    targetVmPid = reader.readLine().trim();
                    System.out.println("---->pid:"+targetVmPid);
                    VirtualMachine virtualmachine = VirtualMachine.attach(targetVmPid);
                    String agent = ss[1];
                    String params = ss[2];
                    virtualmachine.loadAgent(agent,params);
                    System.out.println("load agent finish");
                }catch(Exception ex) {
                    System.out.println("error:"+ex.getMessage());
                    ex.printStackTrace();
                }
                continue;
            }

            String[] strs = line.split(",");
            String cmd = strs[0];
            request.setCmd(cmd);
            if (strs.length > 1) {
                for (int i = 1; i < strs.length; i++) {
                    request.getParams().add(strs[i]);
                }
            }
            System.out.println("request:" + request.toString());
            channel.write(request.toString() + "\r\n");
        } while (!line.equals("exit"));
        sc.close();
        channel.close();
    }

    private static void help() {
        StringBuffer sb = new StringBuffer();
        sb.append("loadAgent,/home/cokserver/program/agent.jar,1_com.elex.cok.actor.crosskingdom.WatchActor_/tmp/WatchActor.class\n" +
                "loadAgent,/root/server/signupserver/agent.jar,1_com.elex.cok.actor.crosskingdom.WatchActor_/tmp/WatchActor.class").append("\r\n");
        System.out.println(sb.toString());
    }
}
