

我是搬运工 原作者@Monocc
# mybatis 配置插件 

```
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource">
            <ref bean="dataSource" />
        </property>
        <property name="plugins">
            <array>
                <bean class="com.mybatis.CatMybatisPlugins"></bean>
            </array>
        </property>
        <property name="mapperLocations">
            <list>
                <value>classpath:com/mybatis/*.xml</value>
            </list>
        </property>
    </bean> 
```