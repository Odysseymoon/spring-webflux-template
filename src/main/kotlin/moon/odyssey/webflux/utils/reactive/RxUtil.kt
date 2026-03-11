package moon.odyssey.webflux.utils.reactive

import org.reactivestreams.Publisher
import org.springframework.cache.CacheManager
import reactor.cache.CacheMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers

object RxUtil {

    fun <T> elasticMono(f: () -> Mono<out T>): Mono<T> {
        return ofMono(f, Schedulers.boundedElastic())
    }

    fun <T> elasticFlux(f: () -> Publisher<T>): Flux<T> {
        return ofFlux(f, Schedulers.boundedElastic())
    }

    fun <T> ofMono(f: () -> Mono<out T>, scheduler: Scheduler): Mono<T> {
        return Mono.defer(f).subscribeOn(scheduler)
    }

    fun <T> ofFlux(f: () -> Publisher<T>, scheduler: Scheduler): Flux<T> {
        return Flux.defer(f).subscribeOn(scheduler)
    }

    fun <T : Any> cacheMono(
        cacheManager: CacheManager,
        cacheName: String,
        key: String,
        retriever: Mono<T>,
        klass: Class<T>
    ): Mono<T> {
        val cache = cacheManager.getCache(cacheName)
        return CacheMono.lookup(
            { k ->
                elasticMono {
                    Mono.justOrEmpty(cache?.get(k, klass))
                        .map { Signal.next(it) }
                }
            },
            key
        )
            .onCacheMissResume(retriever)
            .andWriteWith { k, sig ->
                elasticMono {
                    val value = sig.get()
                    if (value != null) {
                        Mono.just(value)
                            .doOnNext { cache?.put(k, it) }
                            .then()
                    } else {
                        Mono.empty()
                    }
                }
            }
    }
}
