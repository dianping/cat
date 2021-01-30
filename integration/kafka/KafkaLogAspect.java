package com.dianping.cat.plugins;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class KafkaLogAspect {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());
  //todo 具体的kafka消费端的路径需要自行配置
  @Pointcut("execution(public * com.github.cat.kafka.MessageService.*(..)) ")

  public void KafkaLog() {
  }


  @Around("KafkaLog()")
  public void aroundMethod(ProceedingJoinPoint proceedingJoinPoint) {


    String className=proceedingJoinPoint.getTarget().getClass().getSimpleName();
    String method = proceedingJoinPoint.getTarget().getClass().getMethods()[0].getName();
    Transaction t = Cat.newTransaction("kafkaConsumer", className+"."+method);
    try {
      proceedingJoinPoint.proceed();
    }catch (Throwable e){
      logger.error("切面执行异常", e);
      t.setStatus(e);
    }finally {
      t.complete();

    }

  }


}