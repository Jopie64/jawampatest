package com.jdm.jawampatest;

import com.jdm.jwamp.JWampFactory;
import rx.Observable;

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

    public void run()
    {
        JWampFactory.jawampa("ws://pcs02.otap.local:9001/wamp", "realm1")
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
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
        log(1, "Program ended." );
    }
}
