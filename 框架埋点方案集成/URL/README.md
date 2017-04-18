## URL监控埋点方案

### 作用
1. 一个http请求来了之后，会自动打点，能够记录每个url的访问情况，并将以此请求后续的调用链路串起来，可以在cat上查看logview
2. 可以在cat Transaction及Event 页面上都看到URL和URL.Forward（如果有Forward请求的话）两类数据；Transaction数据中URL点进去的数据就是被访问的具体URL（去掉参数的前缀部分）
3. 请将catFilter存放filter的第一个，这样可以保证最大可能性监控所有的请求

```

<filter>
    <filter-name>cat-filter</filter-name>
    <filter-class>com.dianping.cat.servlet.CatFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>cat-filter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>FORWARD</dispatcher>
</filter-mapping>
```

filter-mapping可以支持匹配规则，比如如下匹配了/r/开头以及/s/开头的path，这样可以让cat-flter仅仅监控部分URL，而不是所有的URL，不过建议是全量URL。

```
	<filter-mapping>
		<filter-name>cat-filter</filter-name>
		<url-pattern>/r/*</url-pattern>
		<dispatcher>REQUEST</dispatcher>
	</filter-mapping>
	<filter-mapping>
		<filter-name>cat-filter</filter-name>
		<url-pattern>/s/*</url-pattern>
		<dispatcher>REQUEST</dispatcher>
	</filter-mapping>

	
```
    
### RestFull 监控配置
	
- 比如shop/1 shop/2 都会自动归类到 /shop/{num}
- 注意shop/v1 shop/v2 等不会自动到/shop/v{num}，cat仅仅替换两个/之间，如果为数字，替换为{num}
- 对于一些特别的url参数，CAT提供了客户端自定义的URL的name功能，只要在HttpServletRequest的设置一个Attribute，就可以解决URL基本一样，但仅仅参数不同导致分开统计的问题。比如restfull的url。
- 在业务运行代码中加入如下code可以自定义URL下name，这样可以进行自动聚合，代码如下


```	
	HttpServletRequest req = ctx.getHttpServletRequest();
	req.setAttribute("cat-page-uri", "myPageName");
```
	