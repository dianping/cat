import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
public class CatAspect {

    @Around("@annotation(catTransaction)")
    public Object catTransactionProcess(ProceedingJoinPoint pjp, CatTransaction catTransaction) throws Throwable {
        String transName = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        if(StringUtils.isNotBlank(catTransaction.name())){
            transName = catTransaction.name();
        }
        Transaction t = Cat.newTransaction(catTransaction.type(), transName);
        try {
            Object result = pjp.proceed();
            t.setStatus(Transaction.SUCCESS);
            return result;
        } catch (Throwable e) {
            t.setStatus(e);
            throw e;
        }finally{
            t.complete();
        }
    }
    
    @Around("@annotation(catCacheTransaction)")
    public Object catCacheTransactionProcess(ProceedingJoinPoint pjp, CatCacheTransaction catCacheTransaction) throws Throwable {
        String transName = pjp.getSignature().getName();
        if(StringUtils.isNotBlank(catCacheTransaction.name())){
            transName = catCacheTransaction.name();
        }
        Transaction t = Cat.newTransaction("Cache.Redis",transName);
        try {
            Cat.logEvent("Cache.Server", catCacheTransaction.server());
            Object result = pjp.proceed();
            t.setStatus(Transaction.SUCCESS);
            return result;
        } catch (Throwable e) {
            Cat.logEvent("Cache.Server", catCacheTransaction.server(), "-1", null);
            t.setStatus(e);
            throw e;
        }finally{
            t.complete();
        }
    }
    
    @Around("@annotation(catDubboClientTransaction)")
    public Object catDubboServerTransactionProcess(ProceedingJoinPoint pjp, CatDubboClientTransaction catDubboClientTransaction) throws Throwable {
        String transName = pjp.getSignature().getName();
        if(StringUtils.isNotBlank(catDubboClientTransaction.name())){
            transName = catDubboClientTransaction.name();
        }
        Transaction t = Cat.newTransaction("Call",transName);
        try {
            Cat.logEvent("Call.app", catDubboClientTransaction.callApp());
            Cat.logEvent("Call.server", catDubboClientTransaction.callServer());
            Object result = pjp.proceed();
            t.setStatus(Transaction.SUCCESS);
            return result;
        } catch (Throwable e) {
            t.setStatus(e);
            throw e;
        }finally{
            t.complete();
        }
    }

    @After("@annotation(catHttpRequestTransaction)")
    public void catHttpRequestProcess(CatHttpRequestTransaction catHttpRequestTransaction) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if(StringUtils.isNotBlank(catHttpRequestTransaction.name())){
            String transName = catHttpRequestTransaction.name();
            request.setAttribute("cat-page-uri", transName);
        }
    }
}