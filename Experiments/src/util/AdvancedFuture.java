package util;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Derrick Lockwood
 * @created 8/10/18.
 */
public abstract class AdvancedFuture<V> implements Future<V> {

    private Consumer<V> consumer;

    public AdvancedFuture<V> setConsumer(Consumer<V> consumer) {
        this.consumer = consumer;
        return this;
    }

    private V consume(V v) {
        if (consumer != null) {
            consumer.accept(v);
        }
        return v;
    }

    public void consume() throws ExecutionException, InterruptedException {
        get();
    }

    public void consume(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        get(timeout, unit);
    }

    @Override
    public final V get() throws InterruptedException, ExecutionException {
        return consume(getV());
    }

    @Override
    public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return consume(getV(timeout, unit));
    }

    protected abstract V getV() throws InterruptedException, ExecutionException;
    protected abstract V getV(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    public <U> AdvancedFuture<U> map(Function<V, U> function) {
        return new AdvancedFuture<U>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return AdvancedFuture.this.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return AdvancedFuture.this.isCancelled();
            }

            @Override
            public boolean isDone() {
                return AdvancedFuture.this.isDone();
            }

            @Override
            protected U getV() throws InterruptedException, ExecutionException {
                return function.apply(AdvancedFuture.this.get());
            }

            @Override
            protected U getV(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                return function.apply(AdvancedFuture.this.get(timeout, unit));
            }
        };
    }

    public AdvancedFuture<V> after(Consumer<V> consumer) {
        return new AdvancedFuture<V>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return AdvancedFuture.this.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return AdvancedFuture.this.isCancelled();
            }

            @Override
            public boolean isDone() {
                return AdvancedFuture.this.isDone();
            }

            @Override
            protected V getV() throws InterruptedException, ExecutionException {
                V v = AdvancedFuture.this.get();
                consumer.accept(v);
                return v;
            }

            @Override
            protected V getV(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                V v = AdvancedFuture.this.get(timeout, unit);
                consumer.accept(v);
                return v;
            }
        };
    }

    public AdvancedFuture<V> link(AdvancedFuture<V> future) {
        AdvancedFuture<V> vAdvancedFuture = new AdvancedFuture<V>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return AdvancedFuture.this.cancel(mayInterruptIfRunning) && future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return AdvancedFuture.this.isCancelled() && future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return AdvancedFuture.this.isDone() && future.isDone();
            }

            @Override
            protected V getV() throws InterruptedException, ExecutionException {
                AdvancedFuture.this.consume();
                return future.get();
            }

            @Override
            protected V getV(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                AdvancedFuture.this.consume(timeout, unit);
                return future.get(timeout, unit);
            }
        };
        if (this.consumer != null) {
            vAdvancedFuture.consumer = this.consumer;
        }
        return vAdvancedFuture;
    }

    public AdvancedFuture<V> link(Future<V> future) {
        if (future instanceof AdvancedFuture) {
            return link((AdvancedFuture<V>) future);
        }
        return link(newInstance(future));
    }

    public static <U> AdvancedFuture<U> newInstance(Future<U> future) {
        return new AdvancedFuture<U>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            protected U getV() throws InterruptedException, ExecutionException {
                return future.get();
            }

            @Override
            protected U getV(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                return future.get(timeout, unit);
            }
        };
    }

    public static <U> AdvancedFuture<U> completedFuture(U value) {
        return new AdvancedFuture<U>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            protected U getV() throws InterruptedException, ExecutionException {
                return value;
            }

            @Override
            protected U getV(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                return value;
            }
        };
    }

    public static <U> AdvancedFuture<U> submit(ExecutorService executorService, Callable<U> callable) {
        return newInstance(executorService.submit(callable));
    }

    public static <U> AdvancedFuture<List<U>> finishOrderFuture(Iterable<AdvancedFuture<U>> futures, int length) {
        final TimeUnit timeUnit = TimeUnit.MICROSECONDS;
        return new AdvancedFuture<List<U>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelSuccess = true;
                for (AdvancedFuture<U> future : futures) {
                    cancelSuccess = future.cancel(mayInterruptIfRunning);
                }
                return cancelSuccess;
            }

            @Override
            public boolean isCancelled() {
                boolean cancelSuccess = true;
                for (AdvancedFuture<U> future : futures) {
                    cancelSuccess = future.isCancelled();
                }
                return cancelSuccess;
            }

            @Override
            public boolean isDone() {
                boolean cancelSuccess = true;
                for (AdvancedFuture<U> future : futures) {
                    cancelSuccess = future.isDone();
                }
                return cancelSuccess;
            }

            @Override
            protected List<U> getV() throws InterruptedException, ExecutionException {
                List<U> returns = new ArrayList<>(length);
                while (returns.size() != length) {
                    Iterator<AdvancedFuture<U>> futureIterator = futures.iterator();
                    while (futureIterator.hasNext()) {
                        Future<U> future = futureIterator.next();
                        try {
                            returns.add(future.get(1, timeUnit));
                            futureIterator.remove();
                        } catch (TimeoutException ignored) {
                        }
                    }
                }
                return returns;
            }

            @Override
            protected List<U> getV(long timeout, TimeUnit unit) throws
                    InterruptedException,
                    ExecutionException,
                    TimeoutException {
                Duration duration = Duration.of(timeout, toChronoUnit(unit));
                Instant start = Instant.now();
                List<U> returns = new ArrayList<>(length);
                while (returns.size() != length) {
                    Iterator<AdvancedFuture<U>> futureIterator = futures.iterator();
                    while (futureIterator.hasNext()) {
                        Future<U> future = futureIterator.next();
                        try {
                            returns.add(future.get(1, timeUnit));
                            futureIterator.remove();
                        } catch (TimeoutException ignored) {
                        }
                        if (Duration.between(start, Instant.now()).compareTo(duration) >= 0) {
                            throw new TimeoutException();
                        }
                    }
                }
                return returns;
            }
        };
    }

    private static ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        switch (timeUnit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                return null;
        }
    }

    public static <U> AdvancedFuture<List<U>> finishOrderFuture(AdvancedFuture<U>[] futures) {
        return finishOrderFuture(Arrays.asList(futures), futures.length);
    }

    public static <U> AdvancedFuture<List<U>> finishOrderFuture(List<AdvancedFuture<U>> futures) {
        return finishOrderFuture(futures, futures.size());
    }

}
