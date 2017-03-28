package com.wanda.cat.sample.plugins;

import com.alibaba.druid.pool.DruidDataSource;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;


/**
 * 对MyBatis进行拦截，添加Cat监控
 * 目前仅支持RoutingDataSource和Druid组合配置的数据源
 *
 * @author Steven
 */

@Intercepts({
        @Signature(method = "query", type = Executor.class, args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class }),
        @Signature(method = "update", type = Executor.class, args = { MappedStatement.class, Object.class })
})
public class CatMybatisPlugin implements Interceptor {

    private static Log logger = LogFactory.getLog(CatMybatisPlugin.class);

    //缓存，提高性能
    private static final Map<String, String> sqlURLCache = new ConcurrentHashMap<String, String>(256);

    private static final String EMPTY_CONNECTION = "jdbc:mysql://unknown:3306/%s?useUnicode=true";

    private Executor target;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        //得到类名，方法
        String[] strArr = mappedStatement.getId().split("\\.");
        String methodName = strArr[strArr.length - 2] + "." + strArr[strArr.length - 1];

        Transaction t = Cat.newTransaction("SQL", methodName);

        //得到sql语句
        Object parameter = null;
        if(invocation.getArgs().length > 1){
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = showSql(configuration, boundSql);

        //获取SQL类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Cat.logEvent("SQL.Method", sqlCommandType.name().toLowerCase(), Message.SUCCESS, sql);

        String s = this.getSQLDatabase();
        Cat.logEvent("SQL.Database", s);

        Object returnObj = null;
        try {
            returnObj = invocation.proceed();
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.logError(e);
        } finally {
            t.complete();
        }

        return returnObj;
    }

    private javax.sql.DataSource getDataSource() {
        org.apache.ibatis.transaction.Transaction transaction = this.target.getTransaction();
        if (transaction == null) {
            logger.error(String.format("Could not find transaction on target [%s]", this.target));
            return null;
        }
        if (transaction instanceof SpringManagedTransaction) {
            String fieldName = "dataSource";
            Field field = ReflectionUtils.findField(transaction.getClass(), fieldName, javax.sql.DataSource.class);

            if (field == null) {
                logger.error(String.format("Could not find field [%s] of type [%s] on target [%s]",
                        fieldName, javax.sql.DataSource.class, this.target));
                return null;
            }

            ReflectionUtils.makeAccessible(field);
            javax.sql.DataSource dataSource = (javax.sql.DataSource) ReflectionUtils.getField(field, transaction);
            return dataSource;
        }

        logger.error(String.format("---the transaction is not SpringManagedTransaction:%s", transaction.getClass().toString()));

        return null;
    }

    private String getSqlURL() {
        javax.sql.DataSource dataSource = this.getDataSource();

        if (dataSource == null) {
            return null;
        }

        if (dataSource instanceof AbstractRoutingDataSource) {
            String methodName = "determineTargetDataSource";
            Method method = ReflectionUtils.findMethod(AbstractRoutingDataSource.class, methodName);

            if (method == null) {
                logger.error(String.format("---Could not find method [%s] on target [%s]",
                        methodName,  dataSource));
                return null;
            }

            ReflectionUtils.makeAccessible(method);
            javax.sql.DataSource dataSource1 = (javax.sql.DataSource) ReflectionUtils.invokeMethod(method, dataSource);
            if (dataSource1 instanceof DruidDataSource) {
                DruidDataSource druidDataSource = (DruidDataSource) dataSource1;
                return druidDataSource.getUrl();
            } else {
                logger.error("---only surpport DruidDataSource:" + dataSource1.getClass().toString());
            }
        } else if(dataSource instanceof BasicDataSource){
            return ((BasicDataSource) dataSource).getUrl();
        }
        return null;
    }

    private String getSQLDatabase() {
//        String dbName = RouteDataSourceContext.getRouteKey();
        String dbName = null; //根据设置的多数据源修改此处,获取dbname
        if (dbName == null) {
            dbName = "DEFAULT";
        }
        String url = CatMybatisPlugin.sqlURLCache.get(dbName);
        if (url != null) {
            return url;
        }

        url = this.getSqlURL();//目前监控只支持mysql ,其余数据库需要各自修改监控服务端
        if (url == null) {
            url = String.format(EMPTY_CONNECTION, dbName);
        }
        CatMybatisPlugin.sqlURLCache.put(dbName, url);
        return url;
    }
    /**
     * 解析sql语句
     * @param configuration
     * @param boundSql
     * @return
     */
    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));

            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    }
                }
            }
        }
        return sql;
    }

    /**
     * 参数解析
     * @param obj
     * @return
     */
    private String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            this.target = (Executor) target;
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
