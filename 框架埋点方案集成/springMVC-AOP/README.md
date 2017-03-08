切面注解方式埋点 实现以切面注解方式快速对系统进行埋点，在需要埋点的类或方法加上注解即可（会有性能影响） 具体操作如下: 

1,web.xml中增加filter,url-pattern配置需要埋点的restful接口,防止静态资源乱入 

cat-filter com.dianping.cat.servlet.CatFilter cat-filter /test1/ /test2/ REQUEST FORWARD

2,CatCacheTransaction注解示例

@CatCacheTransaction
public V get(K key) {

}

@CatCacheTransaction
public void put(K key, V value) {

}

@CatCacheTransaction
public void delete(K key) {  

}

3,CatHttpRequestTransaction注解示例,URL聚合注解

@RequestMapping(value = "/orders/{userId}/{orderStatus}")
@ResponseBody
@CatHttpRequestTransaction(type = "URL", name = "/orders")
public String userOrders() {

}

4,CatDubboClientTransaction注解示例

@CatDubboClientTransaction(callApp="orders",callServer = "orderServer")
public List<Long> getOrdersByUser() {

}
