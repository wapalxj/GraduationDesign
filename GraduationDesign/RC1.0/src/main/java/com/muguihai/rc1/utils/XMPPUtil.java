package com.muguihai.rc1.utils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;

/**
 * XMPP
 * Created by vero on 2016/4/11.
 */
public class XMPPUtil {
    public static XMPPConnection getXMPPConnection(String SERVER,int PORT){
        //创建配置
        ConnectionConfiguration configuration =new ConnectionConfiguration(SERVER,PORT);
        //额外的配置:上线则改回来
        configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文传输
        configuration.setDebuggerEnabled(true);//调试模式，方便查看具体内容
        configuration.setSASLAuthenticationEnabled(false);
        configuration.setSendPresence(false);//先设置为离线:这样才能接收到离线信息
        //
        // 允许自动连接
        configuration.setReconnectionAllowed(true);
        // 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
        //
        //创建连接对象
        XMPPConnection connection=new XMPPConnection(configuration);
        return connection;
    }

}
