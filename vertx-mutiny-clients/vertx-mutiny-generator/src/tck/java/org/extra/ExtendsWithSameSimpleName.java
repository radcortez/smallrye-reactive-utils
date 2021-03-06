package org.extra;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import org.extra.sub.UseVertxGenNameDeclarationsWithSameSimpleName;

import java.util.function.Function;

@VertxGen
public interface ExtendsWithSameSimpleName extends
        UseVertxGenNameDeclarationsWithSameSimpleName,
        Handler<UseVertxGenNameDeclarationsWithSameSimpleName>,
        ReadStream<UseVertxGenNameDeclarationsWithSameSimpleName> {

    @CacheReturn
    UseVertxGenNameDeclarationsWithSameSimpleName foo(UseVertxGenNameDeclarationsWithSameSimpleName arg);

    void function(
            Function<UseVertxGenNameDeclarationsWithSameSimpleName, UseVertxGenNameDeclarationsWithSameSimpleName> function);

    void asyncResult(Handler<AsyncResult<UseVertxGenNameDeclarationsWithSameSimpleName>> handler);

}
