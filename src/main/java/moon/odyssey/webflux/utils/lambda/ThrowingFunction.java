package moon.odyssey.webflux.utils.lambda;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    R apply(T t) throws E;

    static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
        return t -> {
            R r = null;
            try {
                r = f.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            finally {
                return r;
            }
        };
    }

    static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f, Consumer<Throwable> c) {
        return t -> {
            R r = null;
            try {
                r = f.apply(t);
            } catch (Throwable e) {
                c.accept(e);
            }
            finally {
                return r;
            }
        };
    }

    static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f, Consumer<Throwable> c, R fallback) {
        return t -> {
            R r = null;
            try {
                r = f.apply(t);
            } catch (Throwable e) {
                c.accept(e);
                r = fallback;
            }
            finally {
                return r;
            }
        };
    }
}
