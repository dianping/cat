<%@ page session="false" language="java" pageEncoding="UTF-8" %>

<h3 class="text-error">客户端版本说明</h3>
<table class='table table-striped table-bordered table-condensed'>
	<tr class="text-success"><th width="3%">版本</th><th width="87%">说明</th><th width="10%">发布时间</th></tr>
	<tr><td>1.1.2</td><td>1、动态配置CAT的路由策略，支持统一项目名调整</td><td>2014-01-02</td></tr>
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

<h3 class="text-error">服务端功能说明</h3>
<table class='table table-striped table-bordered table-condensed'>
	<tr class="text-success"><th width="90%">最新发布功能描述</th><th width="10%">发布时间</th></tr>	
	<tr><td><strong class='text-error'>增加了运维容量规划报表，去掉ABtest心跳信息以及错误信息。</strong></td><td>2014-01-01</td></tr>
	<tr><td><strong class='text-error'>在CAT的URL后面加上参数forceDownload=xml或者forceDownload=json，可以看到当前页面的数据模型</strong></td><td>2013-12-16</td></tr>
	<tr><td><strong>优化了主页面的UI，新增了常用domain的切换功能，常用是最近使用的10个项目</strong></td><td>2013-09-06</td></tr>
	<tr><td><strong>增加了业务监控以及业务监控大盘</strong></td><td>2013-08-14</td></tr>
	<tr><td><strong>Dependency报表，包含实时依赖曲线图、依赖拓扑、产品线监控、监控仪表盘</strong></td><td>2013-06-14</td></tr>
	<tr><td>支持FrontEnd的错误js的合并</td><td>2013-06-14</td></tr>
	<tr><td>Problem小时报表支持一个小时内的错误趋势图</td><td>2013-03-13</td></tr>
	<tr><td>Query报表支持按照天或者小时查询Transaction,Event,Problem数据</td><td>2013-03-11</td></tr>
	<tr><td>Cross报表支持根据方法名称查询是哪些客户端调用此方法</td><td>2013-03-11</td></tr>
	<tr><td>Top报表,根据分钟级别实时展现线上异常最多、访问最慢(URL\Service\SQL\Call\Cache)的应用</td><td>2013-03-11</td></tr>
	<tr><td>项目信息修改，请项目负责人到Config标签下，修改项目所在分组的基本信息（仅修改线上环境）</<td><td>2013-01-21</td></tr>
	<tr><td>Transaction\Event月报表支持每天的趋势图，以天为单位</td><td>2013-01-21</td></tr>
	<tr><td>Transaction\Event报表日报表、周报表支持趋势图对比,时间精度为5分钟</td><td>2013-01-01</td></tr>
	<tr><td>默认告警，邮件订阅（修改线上环境即可)，请项目负责人到Alarm标签下，订阅相关异常告警、服务调用失败告警、日常邮件，Hawk会逐步下线中。</td><td>2012-09-01</td></tr>
</table>