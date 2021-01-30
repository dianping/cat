package async;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 封装异步线程池 支持异步调用链路
 * 实现了ExecutorService
 * 对原线程池的一层代理回调 业务逻辑仍在原线程池中
 * 主线程生成消息上下文传递给子线程,子线程上报cat,并回调
 *
 * @author lianghm343
 * @date 2020/6/3
 */
public class CatExecutorService implements ExecutorService {

    private static final String DEFAULT_ASYNC_TYPE = "async";

    private ExecutorService executorService;

    /**
     * 构造函数
     * 对原线程池的一层代理回调
     */
    public CatExecutorService(ExecutorService tmpExecutorService) {
        executorService = tmpExecutorService;
    }

    /**
     * 对Executor.execute(Runnable command) 的代理
     */
    @Override
    public void execute(final Runnable runner) {
        final Cat.Context context = new ExecutorCatContext();
        Cat.logRemoteCallClient(context);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                subThreadRun(context, runner);
            }
        });
    }

    /**
     * 对executorService.submit(Runnable task)的代理
     */
    @Override
    public Future<?> submit(final Runnable runner) {
        final Cat.Context context = new ExecutorCatContext();
        Cat.logRemoteCallClient(context);
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                subThreadRun(context, runner);
            }
        });
    }

    /**
     * 对executorService.submit(Callable<T> task)的代理
     * 返回原Callable结果
     */
    @Override
    public <T> Future<T> submit(final Callable<T> caller) {
        final Cat.Context context = new ExecutorCatContext();
        Cat.logRemoteCallClient(context);
        Future<T> future = executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                Transaction t = Cat.newTransaction(DEFAULT_ASYNC_TYPE, "caller");
                T result = null;
                try {
                    Cat.logRemoteCallServer(context);
                    result = caller.call();
                } catch (Exception e) {
                    Cat.logError(e);
                    throw e;
                } finally {
                    t.setStatus(Transaction.SUCCESS);
                    t.complete();
                }
                return result;
            }
        });
        return future;
    }

    /**
     * 抽取Runnable子线程共用逻辑
     */
    private void subThreadRun(Cat.Context context, Runnable runner) {
        Transaction t = Cat.newTransaction(DEFAULT_ASYNC_TYPE, "runner");
        try {
            Cat.logRemoteCallServer(context);
            runner.run();
        } finally {
            t.setStatus(Transaction.SUCCESS);
            t.complete();
        }
    }


    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }


    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        executorService.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
        return executorService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    static class ExecutorCatContext implements Cat.Context {

        private Map<String, String> properties = new HashMap<String, String>();

        @Override
        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return properties.get(key);
        }
    }
}
