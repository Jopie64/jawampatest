package com.jdm.jwamp;

import com.fasterxml.jackson.databind.JsonNode;
import rx.Observable;

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
}

public interface JWampProxy
{
    Observable<JsonNode> call(String name, Object... args);
    Observable<JsonNode> subscribe(String name);
    default JWampProxy makeProxy(String uriBase)
    {
        return new SubProxy(uriBase, this);
    }
}
