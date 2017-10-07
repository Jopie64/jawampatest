package com.jdm.jawampatest;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

interface IJWampProxy
{
    Observable<JsonNode> call(String name, Object... args);
}

class JWamp
{
    static class JawampaProxy implements IJWampProxy
    {
        private WampClient wamp;

        public JawampaProxy(WampClient wamp)
        {
            this.wamp = wamp;
        }

        public Observable<JsonNode> call(String name, Object... args)
        {
            return wamp.call(name, args)
                .map(r -> r.arguments().get(0));
        }
    }

    public static Observable<IJWampProxy> fromJawampa(String uri, String realm)
    {
        return Observable.create(observer -> {
            IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
            WampClientBuilder builder = new WampClientBuilder();
            builder.withConnectorProvider(connectorProvider)
                    .withUri(uri)
                    .withRealm(realm);
            try {
                final WampClient client = builder.build();
                AtomicInteger connectionState = new AtomicInteger(0);
                observer.add(client.statusChanged().subscribe(
                    state -> {
                        if (state instanceof WampClient.ConnectingState) {
                            connectionState.set(1);
                            return;
                        }
                        else if (state instanceof WampClient.ConnectedState) {
                            connectionState.set(2);
                            observer.onNext(new JWamp.JawampaProxy(client));
                        }
                        else if (state instanceof WampClient.DisconnectedState) {
                            switch (connectionState.get()) {
                                case 0: return;
                                case 1: observer.onError(((WampClient.DisconnectedState)state).disconnectReason()); return;
                                case 2: observer.onCompleted(); return;
                            }
                        }
                    }
                ));

                client.open();
                
                observer.add(new Subscription() {
                    boolean isSubscribed = true;
                    // This is called when the observable is unsubscribed
                    public void unsubscribe() {
                        isSubscribed = false;
                        client.close();
                    }
                    public boolean isUnsubscribed() { return !isSubscribed; }
                });
            } catch (Exception e) {
                observer.onError(e);
            }
        });
    }
}
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

    public void run() throws Exception
    {
        JWamp.fromJawampa("ws://pcs02.otap.local:9001/wamp", "realm1")
            .switchMap(p -> p.call("com.peterconnects.toolbar.Login"))
            .map(v -> "Success! " + v.asInt())
            .onErrorResumeNext(e -> Observable.just("Failed! " + e.toString()))
            .take(1)
            .toBlocking() // toBlocking() is bad! Quick and dirty synchronous way to wait for success or failed.
            .forEach(s -> log(1, s)); // forEach() called only once due to take(1)
    }

    public static void main( String[] args )
    {
        log(1, "Program started!" );
        try {
            new App().run();
            log(0, "Wait a little while...");
            Thread.sleep(500);
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
