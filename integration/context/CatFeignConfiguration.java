package com.dianping.cat.context.context;

import com.dianping.cat.Cat;
import com.dianping.cat.context.CatConstantsExt;
import com.dianping.cat.context.CatContextImpl;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * 适用于使用feign调用其他SpringCloud微服务的调用链上下文传递场景
 * 作用：在使用feign请求其他微服务时，自动生成context上下文，并将相应参数rootId、parentId、childId放入header
 * 使用方法：在需要添加catcontext的feign service接口中，@FeignClient注解添加此类的configuration配置，
 *      如：@FeignClient(name="account-manage", configuration = CatFeignConfiguration.class)
 *
 * @author soar
 * @date 2019-01-10
 */
public class CatFeignConfiguration implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        com.dianping.cat.context.CatContextImpl catContext = new CatContextImpl();
        Cat.logRemoteCallClient(catContext,Cat.getManager().getDomain());
        requestTemplate.header(CatConstantsExt.CAT_HTTP_HEADER_ROOT_MESSAGE_ID,catContext.getProperty(Cat.Context.ROOT));
        requestTemplate.header(CatConstantsExt.CAT_HTTP_HEADER_PARENT_MESSAGE_ID,catContext.getProperty(Cat.Context.PARENT));
        requestTemplate.header(CatConstantsExt.CAT_HTTP_HEADER_CHILD_MESSAGE_ID,catContext.getProperty(Cat.Context.CHILD));

    }
}
