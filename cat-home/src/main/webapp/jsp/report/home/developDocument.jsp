<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<dl>
  <dt><h5 class="text-success">1.CAT消息协议</h5></dt>
  <dd>CAT客户端可以向服务端发送Transaction, Event, Heartbeat三种消息. 消息的传输格式如下:
  	  <xmp class="well">Class Timestamp Type Name Status  Duration  Data</xmp>
  	  <p>下例是某个实际传输的heartbeat消息:</p>
  	  <xmp>H2012-04-26 16:00:42.775        Heartbeat       192.168.63.141  0  <os name="Mac OS X" arch="x86_64" version="Mac OS X"/>
  	  </xmp>
  	  <table class="table table-striped table-condensed ">
  	  	<tr><td class="text-info">Timestamp</td><td>记录消息产生的时刻, 格式"yyyy-mm-dd HH:MM:SS.sss".</td></tr>
  	  	<tr><td class="text-info">Type</td><td>大小写敏感的字符串. 常见的Transaction type有 "URL", "SQL", "Email", "Exec"等. 常见的Event type有 "Info", "Warn", "Error", 还有"Cat"用来表示Cat内部的消息.</td></tr>
  	  	<tr><td class="text-info">Name</td><td>大小写敏感的字符串. type和name的组合要满足全局唯一性. 常见的URL transaction type的name如 "ViewItem", "MakeBid", "SignIn"等. SQL transaction type的name如 "AddFeedback", "GetAccountDetailUnit4", "IncrementFeedbackAndTotalScore"等.</td></tr>
  	  	<tr><td class="text-info">Status</td><td>大小写敏感的字符串. 0表示成功, 非零表示失败. 建议不要使用太长的字符串. Transaction start没有status字段.</td></tr>
  	  	<tr><td class="text-info">Duration</td><td>精确到0.1毫秒. 表示transaction start和transaction end之间的时间长度. 仅出现在Transaction end或者Atomic Transaction. Event和Heartbeat没有duration字段.</td></tr>
  	  	<tr><td class="text-info">Data</td><td>建议使用以&字符分割的name=value对组成的字符串列表. Transaction start没有data字段.</td></tr>
  	  </table>
  </dd>
  
  <dt><h5 class="text-success">2.Transaction</h5></dt>
  <dd>
  	  <p class="text-danger">a).transaction适合记录跨越系统边界的程序访问行为，比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控</p>
  	  <p>b).某些运行期单元要花费一定时间完成工作, 内部需要其他处理逻辑协助, 我们定义为Transaction.</p>
      <p>c).Transaction可以嵌套(如http请求过程中嵌套了sql处理). </p>
      <p>d).大部分的Transaction可能会失败, 因此需要一个结果状态码. </p>
      <p>e).如果Transaction开始和结束之间没有其他消息产生, 那它就是Atomic Transaction(合并了起始标记).</p>

	  <imgages src="../imgages/transactionGuide.jpg"/>
	  <br/>
	  <p><strong class="text-info">Transaction API</strong></p>
	  <code>
	  	com.dianping.cat.message.MessageProducer:<br/>
        Transaction newTransaction(String type, String name);<br/>
		com.dianping.cat.message.Transaction:<br/>
        void addData(String keyValuePairs);<br/>
        void addData(String key, Object value);<br/>
        void setStatus(String status);<br/>
        void complete();<br/>
	  </code>  
	  <p><strong class="text-info">代码示例</strong></p>
	  
	  <xmp class="well">
     Transaction t = Cat.getProducer().newTransaction("your transaction type", "your transaction name");
     try {
                 yourBusinessOperation();
                Cat.getProducer().logEvent("your event type", "your event name", Event.SUCCESS, "keyValuePairs")
                t.setStatus(Transaction.SUCCESS);
     } catch (Exception e) {
            Cat.getProducer().logError(e);//用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
            throw e; 
            	  (CAT所有的API都可以单独使用，也可以组合使用，比如Transaction中嵌套Event或者Metric。)
                  (注意如果这里希望异常继续向上抛，需要继续向上抛出，往往需要抛出异常，让上层应用知道。)
                  (如果认为这个异常在这边可以被吃掉，则不需要在抛出异常。)
     } finally {
           t.complete();
     }
	  </xmp>
	  
  </dd>
  <dt><h5 class="text-success">3.Event</h5></dt>
  <dd>Event用来记录次数，表名单位时间内消息发生次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小</dd>
  <dt><h5 class="text-success">4.Metric</h5></dt>
  <h5 class='text-danger'> Metric一共有三个API，分别用来记录次数、平均、总和，统一粒度为一分钟</h5>
  <p>1).logMetricForCount用于记录一个指标值出现的次数</p>
  <p>2).logMetricForDuration用于记录一个指标出现的平均值</p>
  <p>3).logMetricForSum用于记录一个指标出现的总和</p>
  <p>4).修改指标一些图形描述信息，请到<a href="/cat/s/config?op=metricConfigList">Config进行配置</a></p>
  <dt><h5 class="text-success">5.Heartbeat<span class="text-danger"><strong>  这个是系统CAT客户端使用，应用程序不使用此API.</strong></span></h5></dt>
  <dd>Heartbeta表示程序内定期产生的统计信息, 如CPU%, MEM%, 连接池状态, 系统负载等。</dd>
  <dt><h5 class="text-success">6.一份埋点的样例</strong></span></h5></dt>
  
    <h5>Transaction用来记录一段程序响应时间</h5>
    <h5>Event用来记录一行code的执行次数</h5>
    <h5>Metric用来记录一个业务指标</h5>
    <h5 class="text-danger">这些指标都是独立的，可以单独使用，主要看业务场景。</h5>
    <h5 class="text-success">下面的埋点代码里面表示需要记录一个页面的响应时间，并且记录一个代码执行次数，以及记录两个业务指标,所有用了一个Transaction，一个Event，两个Metric</strong></span></h5>
    <h5 class="text-danger">Transaction的埋点一定要complete，切记放在finally里面。</h5>
	
	<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/develop05.png"/>
</dl>