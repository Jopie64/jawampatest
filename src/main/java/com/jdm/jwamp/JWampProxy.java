package com.jdm.jwamp;

import com.fasterxml.jackson.databind.JsonNode;
import rx.Observable;
import rx.Subscription;

class SubProxy implements JWampProxy
{
    String uriBase;
    JWampProxy proxyBase;

    SubProxy(String uriBase, JWampProxy proxyBase)
    {
        this.uriBase = uriBase;
        this.proxyBase = proxyBase;
    }

    String formatUri(String uri)
    {
        return uriBase + uri;
    }

    public Observable<JsonNode> call(String name, Object... args)
    {
        return proxyBase.call(formatUri(name), args);
    }

    public Observable<JsonNode> subscribe(String name)
    {
        return proxyBase.subscribe(formatUri(name));
    }

    public Observable<Boolean> lifetime$()
    {
        return proxyBase.lifetime$();
    }
}

class SubProxyWithLifetime extends SubProxy
{
    SubProxyWithLifetime(String uriBase, JWampProxy proxyBase)
    {
        super(uriBase, proxyBase);
    }

    public Observable<Boolean> lifetime$()
    {
        return createLifetime$()
            .replay(1).refCount();
    }

    private Observable<Boolean> createLifetime$()
    {
        return Observable.create(observer -> {
            // Receiving end, ends the subscription
            observer.add(subscribe("End")
                .subscribe(v -> observer.onCompleted()));
            call("Initialize")
                .subscribe(alive -> {
                    if (alive.asBoolean()) {
                        observer.onNext(true);
                    } else {
                        observer.onCompleted();
                    }
                });
            observer.add(new Subscription() {
                boolean isSubscribed = true;
                // This is called when the observable is unsubscribed
                public void unsubscribe() {
                    isSubscribed = false;
                    call("End");
                }
                public boolean isUnsubscribed() { return !isSubscribed; }
            });
        });
    }
}

public interface JWampProxy
{
    Observable<JsonNode> call(String name, Object... args);
    Observable<JsonNode> subscribe(String name);
    default JWampProxy makeProxy(String uriBase, boolean hasOwnLifetime)
    {
        if (hasOwnLifetime)
            return new SubProxyWithLifetime(uriBase, this);
        else
            return new SubProxy(uriBase, this);
    }
    
    public Observable<Boolean> lifetime$();
}
