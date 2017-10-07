package com.jdm.jwamp;

import com.fasterxml.jackson.databind.JsonNode;
import rx.Observable;

public interface IJWampProxy
{
    Observable<JsonNode> call(String name, Object... args);
    Observable<JsonNode> subscribe(String name);
}
