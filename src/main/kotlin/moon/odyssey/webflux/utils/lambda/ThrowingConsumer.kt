package moon.odyssey.webflux.utils.lambda

import java.util.function.Consumer

fun interface ThrowingConsumer<T> {
    @Throws(Throwable::class)
    fun accept(t: T)

    companion object {
        @JvmStatic
        fun <T> unchecked(f: ThrowingConsumer<T>): Consumer<T> {
            return Consumer { t ->
                try {
                    f.accept(t)
                } catch (e: Throwable) {
                    throw RuntimeException(e)
                }
            }
        }

        @JvmStatic
        fun <T> unchecked(f: ThrowingConsumer<T>, c: Consumer<Throwable>): Consumer<T> {
            return Consumer { t ->
                try {
                    f.accept(t)
                } catch (e: Throwable) {
                    c.accept(e)
                }
            }
        }
    }
}
