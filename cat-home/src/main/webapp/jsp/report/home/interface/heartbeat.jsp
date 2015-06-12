<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4>Heartbeat的扩展接口</h4>
<p>监控单台机器内的一些数据指标，cat按照一分钟的粒度进行上报，服务端统一做展示。</p>
<h5 class="text-success">适用场景</h5>
<p>1,监控当前一个业务线程状态</p>
<p>2,监控jvm内部一个队列长度</p>
<p>3,监控jvm当前数据库连接数等</p>
<p>4,监控业务内部一个技术指标等</p>

<h5>例子</h5>
<xmp>
package com.dianping.cat.status;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class HeartbeatExtenstion implements StatusExtension, Initializable {

	//给当前的类定义一个ID
	@Override
	public String getId() {
		return "MyTestId";
	}

	//一个简单描述
	@Override
	public String getDescription() {
		return "MyDescription";
	}

	//注意，只有valude值可以被转化double类型的才会在heartbeat做图形展示
	@Override
	public Map<String, String> getProperties() {
		Map<String, String> maps = new HashMap<String, String>();

		maps.put("key1", String.valueOf(1));
		maps.put("key2", String.valueOf(2));
		maps.put("key3", String.valueOf(3));

		return maps;
	}

	//这里是实现了初始化方法，把这个实现注册到cat上，如果你使用spring，需要在spring里面注册此bean，并实现初始化方法。
	@Override
	public void initialize() throws InitializationException {
		StatusExtensionRegister.getInstance().register(this);
	}
}

</xmp>
<h5 class="text-success">结果展示</h5>
cat服务端会将一个extension id作为一个group，然后将每个key指标都会做图形处理，结果会在cat的heartbeat中进行展示如下
<img  class="img-polaroid"  src="${model.webapp}/images/heartbeat02.png" width="100%"/>

