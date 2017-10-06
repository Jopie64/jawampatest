package com.jdm.jawampatest;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampConnectionConfig;

/**
 * Hello world!
 *
 */
public class App
{
    public static void log(int sev, String msg)
    {
        System.out.format("%d {%04X} %s", sev, Thread.currentThread().getId(), msg);
        System.out.println();
    }

    public WampClient createWampClient() throws Exception
    {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();
        builder.withConnectorProvider(connectorProvider)
                .withUri("ws://pcs02.otap.local:9001/wamp")
                .withRealm("realm1");
        // Create a client through the builder. This will not immediatly start
        // a connection attempt
        return builder.build();
    }

    public void run() throws Exception
    {
        final WampClient client = createWampClient();
        client.open();
        client.statusChanged()
            //.subscribeOn(Schedulers.newThread())
            //.observeOn(Schedulers.io())
            .toBlocking()
            .forEach(s -> log(1, s.toString()));
    }

    public static void main( String[] args )
    {
        log(1, "Program started!" );
        try {
            new App().run();
        } catch (WampError e) {
            // Catch exceptions that will be thrown in case of invalid configuration
            System.out.println(e);
            return;
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
        log(1, "Program ended." );
    }
}
