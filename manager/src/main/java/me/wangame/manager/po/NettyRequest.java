package me.wangame.manager.po;

import com.google.gson.Gson;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzy on 2015/10/21.
 */
public class NettyRequest {

    private String uid;
    private String cmd;
    private List<String> params;

    private transient ChannelHandlerContext context;


    public NettyRequest(String message) {
        Gson gson = new Gson();
        NettyRequest nr = gson.fromJson(message,NettyRequest.class);
        this.uid = nr.getUid();
        this.cmd = nr.getCmd();
        this.params = new ArrayList<>();
        this.params.addAll(nr.getParams());
    }

    public NettyRequest() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<String> getParams() {
        if (null == this.params) {
            this.params = new ArrayList<>();
        }
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }
}
