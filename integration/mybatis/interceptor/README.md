
# mybatis 配置插件 

```
     @Value("${jdbc-url}")
     private String jdbcUrl;
         
     @Bean
     public SqlSessionFactory mysqlSessionFactory(DataSource mysqlDataSource) throws Exception {
         SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
         sqlSessionFactoryBean.setDataSource(mysqlDataSource);
         PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
         sqlSessionFactoryBean.setMapperLocations(resolver.getResources(mapperLocations));
         sqlSessionFactoryBean.setPlugins(new Interceptor[]{new CatMybatisInterceptor(jdbcUrl)});
         return sqlSessionFactoryBean.getObject();
     }
```