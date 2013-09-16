<%@ page session="false" language="java" pageEncoding="UTF-8" %>

<h3 class="text-error">集成帮助文档</h3>

<dl>
  <dt><h5 class="text-success">1.Web.xml中新增filter</h5></dt>
  <dd><p class="detailContent">注：如果项目是对外不提供URL访问，比如GroupService，仅仅提供Pigeon服务，则不需要。</p>
	<p class="text-error detailContent"><strong>Filter放在url-rewrite-filter 之后的第一个。</strong></p>
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
	
  <dt><h5 class="text-success">2.Pom.xml中更新jar包(或者更新platform包)</h5></dt>
  <dd>
  <xmp class="well">
       <dependency>
             <groupId>com.dianping.zebra</groupId>
             <artifactId>zebra-ds-monitor-client</artifactId>
             <version>0.0.7</version>
       </dependency>
       <dependency>
             <groupId>com.dianping.cat</groupId>
             <artifactId>cat-core</artifactId>   
             <version>0.6.1</version>
             </dependency>
       <dependency>
              <groupId>com.dianping</groupId>
              <artifactId>avatar-dao</artifactId>
             <version>2.1.7</version>
       </dependency>
       <dependency>
              <groupId>com.dianping.dpsf</groupId>
              <artifactId>dpsf-net</artifactId>
             <version>1.7.3</version>
       </dependency>
       <dependency>
              <groupId>com.dianping.hawk</groupId>
              <artifactId>hawk-client</artifactId>
             <version>0.6.7</version>
       </dependency>
       <dependency>
              <groupId>com.dianping</groupId>
              <artifactId>avatar-cache</artifactId>
             <version>2.2.4</version>
       </dependency> 
  </xmp>
  <p class="detailContent">      1、更新了这些JAR，默认就会URL，Pigeon，SQL的调用情况，业务可以根据项目需要选择是否升级这些JAR。  </p>
  <p  class="detailContent text-error"> 2、SQL调用依赖需要加载一个配置文件 /config/spring/common/appcontext-ds-monitor.xml，这个文件是在zebra-ds-monitor-client这个jar包下。
            web.xml 加载是需要加入classpath*:config/spring/common/appcontext-ds-monitor.xml</p>
  </dd>
  <dt><h5 class="text-success">3.在资源文件中新建client.xml文件</h5></dt>
  <dd><p class="detailContent">在resources资源文件META-INF下，新建cat文件夹，注意是<span class="text-error">src/main/resources/META-INF/cat/client.xml</span>文件，
  而不是<span class="text-error">webapps下的那个META-INF</span>,domain id表示项目名称<span class="text-error">此处不能为中文，仅支持英文（不能有特殊符号）</span></p>
  <xmp class="well">
     <config mode="client">
         <domain id="TuanGouApi"/>
     </config>
  </xmp>
  </dd>
  <dt><h5 class="text-success">4./data/appdatas/cat/目录下，新建一个client.xml文件(线上环境是OP配置)</h5></dt>
  <dd>
  <p class="detailContent">如果系统是windows环境，则在eclipse运行的盘，比如D盘，新建/data/appdatas/cat/目录，新建client.xml文件</p>
	
  <p>项目文件中srouce中的client.xml,此文件代表了这个项目我是谁,比如项目的名字Cat。</p>
  <p>/data/appdatas/cat/client.xml,此文件有OP控制,这里的Domain名字用来做开关，如果一台机器上部署了多个应用，可以指定把一个应用的监控关闭。</p>
  
  <xmp class="well">
      <config mode="client">
          <servers>
             <server ip="192.168.7.70" port="2280" />
         </servers>
      </config>
  </xmp>
  <p class="text-error">alpha、beta这个配置需要自己在此目录添加</p>
  <p class="text-error">预发以及生产环境这个配置需要通知到对应OP团队，让他们统一添加，自己上线时候做下检查即可</p>
  <p>a、192.168.7.70:2280端口是指向测试环境的cat地址</p>
  <p>b、<span class="text-error">配置可以加入CAT的开关，用于关闭CAT消息发送,将enabled改为false，如下表示将MobileApi这个项目关闭</span></p>
  <xmp>
  	<config mode="client">
          <servers>
             <server ip="192.168.7.70" port="2280" />
         </servers>
         <domain id="MobileApi" enabled="true"/>
      </config>
  </xmp>
  </dd>
  <dt><h5 class="text-success">5.CAT的Log4j集成</h5></dt>
  <dd><p class="detailContent text-error">业务程序的所有异常都通过记录到CAT中，方便看到业务程序的问题，建议在Root节点中添加次appendar</p>
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
  <p class="text-error">c）注意有一些Log的是不继承root的，需要如下配置</p>
  <xmp class="well">
      <logger name="com.dianping.api.location" additivity="false">
        <level value="INFO"/>
        <appender-ref ref="locationAppender"/>
        <appender-ref ref="catAppender"/>
      </logger>
  </xmp>
  </dd>
</dl>

