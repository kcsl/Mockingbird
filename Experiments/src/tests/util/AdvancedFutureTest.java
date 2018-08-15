package util;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Derrick Lockwood
 * @created 8/14/18.
 */
public class AdvancedFutureTest {

    private static ExecutorService executorService;

    @BeforeClass
    public static void before() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterClass
    public static void after() {
        executorService.shutdownNow();
    }

    @Test
    public void testCompletedFuture() throws ExecutionException, InterruptedException {
        AdvancedFuture<Integer> future = AdvancedFuture.completedFuture(20);
        Assert.assertEquals(20, (int) future.get());
    }

    @Test
    public void testWaitFuture() throws ExecutionException, InterruptedException {
        AdvancedFuture<Integer> future = AdvancedFuture.submit(executorService, () -> {
            Thread.sleep(500);
            return 20;
        });
        Assert.assertEquals(20, (int) future.get());
    }

    @Test
    public void testMapFuture() throws ExecutionException, InterruptedException {
        AdvancedFuture<Integer> future = AdvancedFuture.submit(executorService, () -> 22);
        String actual = future.map(Objects::toString).get();
        Assert.assertEquals("22", actual);
    }

    @Test
    public void testLinkFuture() throws ExecutionException, InterruptedException {
        List<Integer> list = new ArrayList<>();
        AdvancedFuture<Integer> future = AdvancedFuture.submit(executorService, waitCallable(300, 300))
                .setConsumer(list::add)
                .link(AdvancedFuture.submit(executorService, waitCallable(200, 200)))
                .link(AdvancedFuture.submit(executorService, waitCallable(700, 700)));
        future.consume();
        Assert.assertEquals(Arrays.asList(300, 200, 700), list);
    }

    @Test
    public void testLinkLastFuture() throws ExecutionException, InterruptedException {
        List<Integer> list = new ArrayList<>();
        AdvancedFuture<Integer> future = AdvancedFuture.submit(executorService, waitCallable(300, 300))
                .link(AdvancedFuture.submit(executorService, waitCallable(200, 200)))
                .link(AdvancedFuture.submit(executorService, waitCallable(700, 700)))
                .setConsumer(list::add);
        future.consume();
        Assert.assertEquals(Collections.singletonList(700), list);
    }

    @Test
    public void testFinishFirstFuture() throws ExecutionException, InterruptedException {
        List<AdvancedFuture<Integer>> list = new ArrayList<>();
        list.add(AdvancedFuture.submit(executorService, waitCallable(300, 300)));
        list.add(AdvancedFuture.submit(executorService, waitCallable(200, 200)));
        list.add(AdvancedFuture.submit(executorService, waitCallable(700, 700)));
        Assert.assertEquals(Arrays.asList(200, 300, 700), AdvancedFuture.finishOrderFuture(list).get());
    }

    @Test
    public void testFinishFirstTimeoutFuture() throws ExecutionException, InterruptedException, TimeoutException {
        List<AdvancedFuture<Integer>> list = new ArrayList<>();
        list.add(AdvancedFuture.submit(executorService, waitCallable(300, 300)));
        list.add(AdvancedFuture.submit(executorService, waitCallable(200, 200)));
        list.add(AdvancedFuture.submit(executorService, waitCallable(700, 700)));
        Assert.assertEquals(Arrays.asList(200, 300, 700),
                AdvancedFuture.finishOrderFuture(list).get(800, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testFinishFirstFailFuture() throws ExecutionException, InterruptedException {
        List<AdvancedFuture<Integer>> list = new ArrayList<>();
        list.add(AdvancedFuture.submit(executorService, waitCallable(300, 300)));
        list.add(AdvancedFuture.submit(executorService, waitCallable(200, 200)));
        list.add(AdvancedFuture.submit(executorService, waitCallable(700, 700)));
        try {
            AdvancedFuture.finishOrderFuture(list).consume(500, TimeUnit.MILLISECONDS);
            Assert.fail();
        } catch (TimeoutException ignored) {

        }
    }

    private <V> Callable<V> waitCallable(long length, V returnValue) {
        return () -> {
            Thread.sleep(length);
            return returnValue;
        };
    }

    private <V> void assertList(List<V> expected, List<V> actual) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

}