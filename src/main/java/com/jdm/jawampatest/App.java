package com.jdm.jawampatest;

import com.jdm.jwamp.JWampFactory;
import com.jdm.jwamp.JWampProxy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;

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

    private Observable<JWampProxy> wamp;

    App(String uri, String realm)
    {
        wamp = JWampFactory.jawampa(uri, realm);
    }

    public void test()
    {
        wamp
            .map(v -> "Success!")
            .onErrorResumeNext(e -> Observable.just("Failed! " + e.toString()))
            .take(1)
            .toBlocking() // toBlocking() is bad! Quick and dirty synchronous way to wait for success or failed.
            .forEach(s -> log(1, s)); // forEach() called only once due to take(1)
    }

    public void run() throws IOException, InterruptedException
    {
        Observable<JWampProxy> wamp = this.wamp
            .retryWhen(e -> e.delay(3, TimeUnit.SECONDS))
            .repeatWhen(e -> e.delay(3, TimeUnit.SECONDS))
            .replay(1).refCount();
        Observable<JWampProxy> toolbarRoot = wamp
            .map(p -> p.makeProxy("com.peterconnects.toolbar.", false))
            .replay(1).refCount();
        Observable<JWampProxy> toolbar = toolbarRoot
            .switchMap(p -> p.call("Login"))
            .retryWhen(e -> e.delay(2, TimeUnit.SECONDS))
            .switchMap(v -> toolbarRoot.map(p -> p.makeProxy(String.format("%s.", v.asText()), true)))
            .switchMap(tb -> tb.lifetime$()
                .map(ok -> tb))
            .replay(1).refCount();

        Observable<String> log$ = Observable.merge(
            wamp.map(p -> "Connected!"),
            toolbar.map(p -> "Logged in!"));

        //log$ = wamp.map(v -> "Now then?");// Observable.just("Do I work?");

        Subscription sub = log$
            .subscribe(s -> log(1, s));
/*        while(true) {
            if ((char)System.in.read() == 'q')
                break;
        }
*/      Thread.sleep(300000);
        sub.unsubscribe();
    }

    public static void main( String[] args )
    {
        log(1, "Program started!" );
        try {
            App app = new App("ws://pcs02.otap.local:9001/wamp", "realm1");
            // app.test();
            app.run();
            log(0, "Wait a little while...");
            Thread.sleep(500);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
        log(1, "Program ended." );
    }
}
