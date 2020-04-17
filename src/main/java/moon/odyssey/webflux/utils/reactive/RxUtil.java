package moon.odyssey.webflux.utils.reactive;

import org.reactivestreams.Publisher;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.function.Supplier;

import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class RxUtil {

    public static <T> Mono<T> elasticMono(Supplier<? extends Mono<? extends T>> f) {
        return RxUtil.ofMono(f, Schedulers.elastic());
    }

    public static <T> Flux<T> elasticFlux(Supplier<? extends Publisher<T>> f) {
        return RxUtil.ofFlux(f, Schedulers.elastic())
            ;
    }

    public static <T> Mono<T> ofMono(Supplier<? extends Mono<? extends T>> f, Scheduler scheduler) {
        return Mono.defer(f)
                   .subscribeOn(scheduler)
            ;
    }

    public static <T> Flux<T> ofFlux(Supplier<? extends Publisher<T>> f, Scheduler scheduler) {
        return Flux.defer(f)
                   .subscribeOn(scheduler)
            ;
    }

    public static <T> Mono<T> cacheMono(CacheManager cacheManager, String cacheName, String key, Mono<T> retriever, Class<T> klass) {
        return
            CacheMono
                .lookup(
                    k -> RxUtil.elasticMono(() -> Mono.justOrEmpty(cacheManager.getCache(cacheName).get(k, klass)).map(Signal::next) )
                    , key
                )
                .onCacheMissResume(retriever)
                .andWriteWith((k, sig) ->
                    RxUtil.elasticMono(() -> Optional.ofNullable(sig.get()).map(Mono::just).orElseGet(Mono::empty)
                                                     .doOnNext(o -> cacheManager.getCache(cacheName).put(k, o))
                                                     .then()
                    )
                )
            ;
    }

}
