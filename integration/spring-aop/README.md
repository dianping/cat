## Spring AOP ANNOTATION 监控方法执行时间
- 源码中提供了CatAnnotation 以及 CatAopService 这两个类
- spring的xml中添加com.dianping.cat.aop.CatAopService 以及 aop:aspectj-autoproxy
- 在监控的bean的方法上添加CatAnnotation
- <font color=#FF4500>cat不推荐使用aop方法埋点，会有少量性能损耗，推荐使用api，这样type和name以及成功状态可以更加灵活</font>
- <font color=#FF4500>业务也可以将此jar两个文件copy到自己的项目工程，这样可以少引入一个jar</font>具体的可以参考如下源码


### spring的配置文件

```
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans  
           http://www.springframework.org/schema/beans/spring-beans.xsd  
           http://www.springframework.org/schema/aop  
           http://www.springframework.org/schema/aop/spring-aop.xsd">

	<!-- 配置通知所在的bean，实际是aspectjs自动扫描所有bean，去找有切面的bean ，请在配置文件添加-->
	<bean id="adviceService" class="com.dianping.cat.aop.CatAopService" />

	<!-- 使用aspectjs配置自动代理，请在配置文件添加 -->
	<aop:aspectj-autoproxy />

	<!-- 这行是使用的一个demo，不需要添加 -->
	<bean id="userServiceImpl" class="com.dianping.cat.aop.UserServiceImpl" />

</beans>  

```
#### 其他参考源码

```
package com.dianping.cat.aop;


public interface UserService {

	public void delete(Object entity);
	
	public void getAllObjects();
	
	public void save(Object entity);
	
	public void update(Object entity);
}
```

```
package com.dianping.cat.aop;

public class UserServiceImpl implements UserService {

	@Override
	@CatAnnotation //此处增加annotation
	public void delete(Object entity) {
		sleep(30);
		System.out.println("UserServiceImpl---删除方法:delete()---");
	}

	@Override
	@CatAnnotation //此处增加annotation
	public void getAllObjects() {
		sleep(40);
		System.out.println("UserServiceImpl---查找所有方法:getAllObjects()---");
	}

	@Override
	@CatAnnotation //此处增加annotation
	public void save(Object entity) {
		sleep(10);
		System.out.println("UserServiceImpl---保存方法:save()---");
	}

	@Override
	@CatAnnotation //此处增加annotation
	public void update(Object entity) {
		sleep(20);
		System.out.println("UserServiceImpl---更新方法:update()---");
	}
	
	private void sleep(int time) {
    	try {
    			Thread.sleep(time);
    	} catch (Exception e) {
    		
    	}
    }

}
```

```
package com.dianping.cat.aop;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AdviseTest {

	@SuppressWarnings("resource")
	@Test
	public void testUser() throws InterruptedException {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-aspectjs.xml");
		UserService userService = context.getBean("userServiceImpl", UserService.class);

		for (int i = 0; i < 1000; i++) {
			userService.save(null);
			userService.update(null);
			userService.delete(null);
			userService.getAllObjects();
		}

		Thread.sleep(1000);
	}

}
```
