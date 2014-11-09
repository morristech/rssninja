package com.yairkukielka.rssninja.rx;

import rx.Subscriber;

/**
 * Base silent subscriber
 */
public class BaseSubscriber<T> extends Subscriber<T> {

    @Override
    public void onNext(T v) {
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
    }

}
