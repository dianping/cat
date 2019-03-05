<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<table class='table table-bordered table-striped table-condensed  '>
	<tr class="text-success"><th width="8%">版本</th><th width="82%">说明</th><th width="10%">发布时间</th></tr>
	<tr><td>3.0.0</td><td>国庆献礼，重大更新：1、增加多语言客户端；2、增加聚合采样，大幅提升性能效率；3、采用二进制协议通信； 4、新版消息文件存储</td><td>2018-10-01</td></tr>
	<tr><td>1.3.8</td><td>1、增加了客户端路由，去除了NullMessage。</td><td>2014-12-09</td></tr>
	<tr><td>1.2.8</td><td>1、合并单独的Event，Metric等原子消息，减少消息总量。修复了启动时候连接服务端的bug</td><td>2014-12-09</td></tr>
	<tr><td>1.1.9</td><td>1、修复了消息截断时候，统计时间的bug</td><td>2014-12-09</td></tr>
	<tr><td>1.1.5</td><td>1、修复了CAT初始化路由出错导致监控信息丢失的bug</td><td>2014-11-21</td></tr>
	<tr><td>1.1.2</td><td>1、动态配置CAT的路由策略，支持统一项目名调整</td><td>2014-08-02</td></tr>
	<tr><td>1.0.1</td><td>1、将ABtest的功能从监控中分离</td><td>2014-01-02</td></tr>
	<tr><td>1.0.0</td><td>1、修复了CAT监控初始化当服务端都出异常的状况，不会自动连接的Bug</td><td>2013-12-20</td></tr>
	<tr><td>0.6.2</td><td>1、支持java job的监控，优化了cat的API</td><td>2013-08-06</td></tr>
	<tr><td>0.6.1</td><td>1、cat客户端的消息长度设置了子消息的最大长度（500），多的消息直接丢弃，以防止内存过大的CAT消息内存泄露</td><td>2013-06-06</td></tr>
	<tr><td>0.6.0</td><td>1、增加业务监控埋点API。2、修复时间戳调整bug。3、修复classpath获取bug。4、修复CatFilter支持Forward请求</td><td>2013-03-26</td></tr>
	<tr><td>0.4.1</td><td>1、默认禁止心跳线程获取线程锁信息，以降低对业务线程的影响。</td><td>2012-09-06</td></tr>
	<tr><td>0.4.0</td><td>1、支持开关动态关闭。2、后端存储重构，支持分布式Logview的查看(关联pigeon的call)。</td><td>2012-08-20</td></tr>
	<tr><td>0.3.4</td><td>1、规范了CAT客户端的日志。2、规范了后台模块的加载顺序。3、统一服务端配置存取。4、新增心跳报表的Http线程 </td><td>2012-07-25</td></tr>
	<tr><td>0.3.3</td><td>1、修改CAT线程为后台Dameon线程。2、减少CAT的日志输出。3、修复了极端情况客户端丢失部分消息。4、支持CAT的延迟加载。5、修复了0.3.2一个getLog的bug</td><td>2012-07-17</td></tr>
	<tr><td>0.3.2</td><td>1、修复了配置单个服务器时候，服务器重启，客户端断开链接bug。2、修复了CAT不正常加载时候，内存溢出的问题。（此版本有问题，请更新至0.3.3）</td><td>2012-07-01</td></tr>
	<tr><td>0.3.1</td><td>1、修复CAT在业务testcase的使用，支持业务运行Testcase在Console上看到运行情况。</td><td>2012-06-25</td></tr>
	<tr><td>0.3.0</td><td>1、修复CAT在Transaction Name的Nullpoint异常。</td><td>2012-06-15</td></tr>
	<tr><td>0.2.5</td><td>1、心跳消息监控新增oldgc和newgc  2、更新了ThreadLocal的线程模型（修复了一些无头消息和部分错乱消息）</td><td>2012-05-01</td></tr>
</table>

