package net.dubboclub.catmonitor;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * Created by bieber on 2015/11/12.
 */
@Activate(group = {CommonConstants.CONSUMER})
public class AppNameAppendFilter implements Filter {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext.getContext().setAttachment(CommonConstants.APPLICATION_KEY,invoker.getUrl().getParameter(CommonConstants.APPLICATION_KEY));
        return invoker.invoke(invocation);
    }
}
