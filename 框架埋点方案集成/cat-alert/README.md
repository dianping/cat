#CAT异常告警
##1、部署
###1.1、CAT服务端
###1.1.1、修改告警模板
CAT告警的模板（主要是邮件使用）中，链接到CAT管理页面的url是写死的，而我们公司内部署CAT后都会指定一个host域名，没有域名的一般也有ip:port
将文件cat/cat-home/src/main/resources/freemaker/exceptionAlert.ftl的内容中<br/>
<pre><code>
[CAT异常告警] [项目: ${domain}] : ${content}[时间: ${date}] 
<a href='http://cat-url/cat/r/p?domain=${domain}&date=${linkDate}'>点击此处查看详情</a><br/>
${contactInfo}<br/>
</code></pre>
把其中的cat-url修改为你部署的cat的域名或ip:port，比如：
<pre><code>
[CAT异常告警] [项目: ${domain}] : ${content}[时间: ${date}] 
<a href='http://127.0.0.1:2281/cat/r/p?domain=${domain}&date=${linkDate}'>点击此处查看详情</a><br/>
${contactInfo}<br/>
</code></pre>
###1.1.2、添加微信渠道支持
* 修改代码生成配置文件
cat/cat-core/src/main/resources/META-INF/dal/jdbc/report-codegen.xml，在project表的phone字段后添加
<pre><code>
<member name="weixin" field="weixin" value-type="String" length="200" />
</code></pre>
* 修改jsp添加微信配置输入框
修改/Users/xuyanhua/IdeaProjects/cat/cat-home/src/main/webapp/jsp/system/project/project.jsp，在项目组号码后添加
<pre><code>
<tr>
   <td>项目组微信</td>
   <td><input type="text" name="project.weixin" class="input-xxlarge" value="${model.project.weixin}"/></td>
   <td>字段(多个，逗号分割)<span  style="color:red">【此字段会和CMDB信息同步】</span></td>
</tr>
</code></pre>
* 添加联系人获取微信方式
修改cat/cat-home/src/main/java/com/dianping/cat/report/alert/sender/receiver/ProjectContactor.java，修改方法queryWeiXinContactors
<pre><code>
@Override
public List<String> queryWeiXinContactors(String id) {
   List<String> weixinReceivers = new ArrayList<String>();
   Receiver receiver = m_configManager.queryReceiverById(getId());

   if (receiver != null && !receiver.isEnable()) {
      return weixinReceivers;
   } else {
      weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));

      if (StringUtils.isNotEmpty(id)) {
         Project project = m_projectService.findByDomain(id);

         if (project != null) {
            weixinReceivers.addAll(split(project.getWeixin()));//由getMail() --> getWeixin()
         }
      }
      return weixinReceivers;
   }
}
</code></pre>

###1.2、告警服务端
###1.3、数据库脚本
###1.4、依赖支持
##2、配置
###2.1、全局告警配置
###2.2、项目告警配置
##3、告警效果一览
###3.1、邮件告警效果
###3.2、微信告警效果
