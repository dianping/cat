## CAT异步线程监控

原理:
1、封装异步线程池，实现了ExecutorService。对原线程池的一层代理回调 业务逻辑仍在原线程池中
2、主线程生成消息上下文传递给子线程,子线程上报cat,并回调


覆盖主流三种使用场景：
1、Executor.execute(Runnable command)
2、executorService.submit(Runnable task)
3、executorService.submit(Callable<T> task)

## 如何接入
初始化catExecutor成员变量（参数为原线程池），与原线程池变量一一对应
代码中用新catExecutor代替原线程池即可（与原线程池变量一一对应）

```
//原
private ExecutorService executorService = new ThreadPoolExecutor(2 * PROCESSOR_COUNT, 100, 1,
        TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>(2048));
//新
private CatExecutorService catExecutorService = new CatExecutorService(executorService);
 
//原
private ExecutorService threadPool = Executors.newFixedThreadPool(5);
 
//新
private CatExecutorService catThreadPool = new CatExecutorService(threadPool);

```

## 
接入后，可以在logview里关联到主线程和子线程链路