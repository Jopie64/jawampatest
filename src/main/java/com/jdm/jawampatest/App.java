package com.jdm.jawampatest;

import java.util.concurrent.TimeUnit;
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
    public WampClient createWampClient() throws Exception
    {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();
        builder.withConnectorProvider(connectorProvider)
                .withUri("ws://pcs01.otap.local:9001/wamp")
                .withRealm("realm1");
        // Create a client through the builder. This will not immediatly start
        // a connection attempt
        return builder.build();
    }

    public void run() throws Exception
    {
        final WampClient client = createWampClient();
        client.statusChanged().subscribe(s -> System.out.println(s.toString()));
    }

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
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
        System.out.println("End");
    }
}
