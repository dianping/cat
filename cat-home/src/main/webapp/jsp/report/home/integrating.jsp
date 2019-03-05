<%@ page session="false" language="java" pageEncoding="UTF-8" %>


<dl>
  <dt><h5 class="text-success">1.Web.xml中新增filter</h5></dt>
  <dd><p class="detailContent">注：如果项目是对外不提供URL访问，比如GroupService，仅仅提供Pigeon服务，则不需要。</p>
	<h5 class="text-danger detailContent"><strong>Filter放在url-rewrite-filter 之后的第一个，如果不是会导致URL的个数无限多，比如search/1/2,search/2/3等等，无法监控，后端存储压力也变大。</strong></h5>
	<xmp class="well">
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
	</xmp>
  </dd>
	  <h5 class="text-danger">struts会吃掉URL中的ERROR信息，请在配置中加
	  	<xmp class="well"> <constant name="struts.handle.exception" value="false" /> 
	  	</xmp>
	  </h5>
	  <h5 class="text-danger">解决URL中很多重复的问题，比如restfull的url</h5>
	    <xmp class="well">
	    	CAT 提供了自定义的URL的name功能，只要在HttpServletRequest的设置一个Attribute，
	    	在业务运行代码中加入如下code可以自定义URL下name，这样可以进行自动聚合。
	    	HttpServletRequest req ;
	    	req.setAttribute("cat-page-uri", "myPageName");
	    </xmp>
	
  <dt><h5 class="text-success">2.Pom.xml中更新jar包(点评内部公共组件，外部公司可以忽略)</h5></dt>
  <dd>
  <xmp class="well">
       <dependency>
            <groupId>com.dianping.cat</groupId>
            <artifactId>cat-client</artifactId>   
            <version>1.4.4</version>
       </dependency>
  </xmp>
  </dd>
  <dt><h5 class="text-success">3、配置domain (cat-client 1.1.3之后版本，优先读取A配置)</h5></dt>
   <p class="text-danger">A) 在资源文件中新建app.properties文件</p>
	   <dd><p class="detailContent">在resources资源文件META-INF下，注意是<span class="text-danger">src/main/resources/META-INF/</span>文件夹，
	  而不是<span class="text-danger">webapps下的那个META-INF</span>,添加<span class="text-danger">app.properties</span>，加上domain配置，如：<span class="text-danger">app.name=tuangou-web</span></p>
	  </dd>
  <dt><h5 class="text-success">4./data/appdatas/cat/目录下，新建一个client.xml文件(线上环境是OP配置)</h5></dt>
  <dd>
  <p class="detailContent">如果系统是windows环境，则在eclipse运行的盘，比如D盘，新建/data/appdatas/cat/目录，新建client.xml文件</p>
	
  <p>项目文件中srouce中的app.properties,此文件代表了这个项目我是谁,比如项目的名字tuangou-web。</p>
  <p>/data/appdatas/cat/client.xml,此文件有OP控制,这里的Domain名字用来做开关，如果一台机器上部署了多个应用，可以指定把一个应用的监控关闭。</p>
  
  <xmp class="well">
      <config mode="client">
          <servers>
             <server ip="10.66.13.115" port="2280" />
         </servers>
      </config>
  </xmp>
  <p class="text-danger">alpha、beta这个配置需要自己在此目录添加，预发以及生产环境这个配置需要通知到对应OP团队，让他们统一添加，自己上线时候做下检查即可</p>
  <p>10.66.13.115:2280端口是指向测试环境的cat地址</p>
  </dd>
  <dt><h5 class="text-success">5.CAT的Log4j集成 【建议所有Log都打到CAT，这样才能更快发现问题】</h5></dt>
  <dd><p class="detailContent text-danger">业务程序的所有异常都通过记录到CAT中，方便看到业务程序的问题，建议在Root节点中添加次appendar</p>
  <p>a）在Log4j的xml中，加入Cat的Appender></p>
  <xmp class="well">
    <appender name="catAppender" class="com.dianping.cat.log4j.CatAppender"></appender>
  </xmp>
  <p>b）在Root的节点中加入catAppender</p>
  <xmp class="well">
     <root>
       <level value="error" />
       <appender-ref ref="catAppender" />
     </root>
  </xmp>
  <p class="text-danger">c）注意有一些Log的是不继承root的，需要如下配置</p>
  <xmp class="well">
      <logger name="com.dianping.api.location" additivity="false">
        <level value="INFO"/>
        <appender-ref ref="locationAppender"/>
        <appender-ref ref="catAppender"/>
      </logger>
  </xmp>
  </dd>
  
  <dd>
  	<h5 class="text-success">6.Java代码一份埋点的样例</strong></span></h5></dt>
    <p>Transaction用来记录一段程序响应时间</p>
    <p>Event用来记录一行code的执行次数</p>
    <p>Metric用来记录一个业务指标</p>
    <p class="text-danger">这些指标都是独立的，可以单独使用Event，单独使用Metric或者Transaction，主要看业务场景。</p>
    <p class="text-danger">Transaction的埋点一定要complete，切记放在finally里面。</p>
    <p class="text-danger">下面的埋点代码里面表示需要记录一个页面的响应时间，并且记录一个代码执行次数，以及记录两个业务指标,所有用了一个Transaction，一个Event，两个Metric</strong></span></p>
	
	<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/develop05.png"/>
  </dd>
</dl>

