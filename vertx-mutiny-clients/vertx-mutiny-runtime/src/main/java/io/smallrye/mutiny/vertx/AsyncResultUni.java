package io.smallrye.mutiny.vertx;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.AbstractUni;
import io.smallrye.mutiny.operators.UniSerializedSubscriber;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class AsyncResultUni<T> extends AbstractUni<T> implements Uni<T> {
    private final Consumer<Handler<AsyncResult<T>>> subscriptionConsumer;

    public static <T> Uni<T> toUni(Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
        return new AsyncResultUni<>(subscriptionConsumer);
    }

    public AsyncResultUni(Consumer<Handler<AsyncResult<T>>> subscriptionConsumer) {
        this.subscriptionConsumer = subscriptionConsumer;
    }

    @Override
    protected void subscribing(UniSerializedSubscriber<? super T> downstream) {
        AtomicBoolean terminated = new AtomicBoolean();
        downstream.onSubscribe(() -> terminated.set(true));

        if (!terminated.get()) {
            try {
                subscriptionConsumer.accept(ar -> {
                    if (!terminated.getAndSet(true)) {
                        if (ar.succeeded()) {
                            T val = ar.result();
                            downstream.onItem(val);
                        } else if (ar.failed()) {
                            downstream.onFailure(ar.cause());
                        }
                    }
                });
            } catch (Exception e) {
                if (!terminated.getAndSet(true)) {
                    downstream.onFailure(e);
                }
            }
        }
    }
}
