package com.jdm.jwamp;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Observable;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

class JawampaProxy implements JWampProxy
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

    public Observable<JsonNode> subscribe(String name)
    {
        return wamp.makeSubscription(name)
            .map(v -> v.arguments().get(0));
    }

    public Observable<Boolean> lifetime$()
    {
        return wamp.statusChanged()
            .takeWhile(v -> !(v instanceof WampClient.DisconnectedState))
            .filter(v -> v instanceof WampClient.ConnectedState)
            .map(v -> true);
    }
}



public class JWampFactory
{
    public static Observable<JWampProxy> jawampa(String uri, String realm)
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
                            observer.onNext(new JawampaProxy(client));
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
