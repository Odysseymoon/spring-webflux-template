package moon.odyssey.webflux.utils.lambda

import java.util.function.Consumer
import java.util.function.Function

fun interface ThrowingFunction<T, R> {
    @Throws(Throwable::class)
    fun apply(t: T): R

    companion object {
        @JvmStatic
        fun <T, R> unchecked(f: ThrowingFunction<T, R>): Function<T, R> {
            return Function { t ->
                try {
                    f.apply(t)
                } catch (e: Throwable) {
                    throw RuntimeException(e)
                }
            }
        }

        @JvmStatic
        fun <T, R> unchecked(f: ThrowingFunction<T, R>, c: Consumer<Throwable>): Function<T, R> {
            return Function { t ->
                try {
                    f.apply(t)
                } catch (e: Throwable) {
                    c.accept(e)
                    throw RuntimeException(e)
                }
            }
        }

        @JvmStatic
        fun <T, R> unchecked(f: ThrowingFunction<T, R>, c: Consumer<Throwable>, fallback: R): Function<T, R> {
            return Function { t ->
                try {
                    f.apply(t)
                } catch (e: Throwable) {
                    c.accept(e)
                    fallback
                }
            }
        }
    }
}
