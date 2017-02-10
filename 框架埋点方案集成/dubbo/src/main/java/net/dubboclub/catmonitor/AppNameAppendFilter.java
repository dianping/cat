package net.dubboclub.catmonitor;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;

/**
 * Created by bieber on 2015/11/12.
 */
@Activate(group = {Constants.CONSUMER})
public class AppNameAppendFilter implements Filter {
    
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext.getContext().setAttachment(Constants.APPLICATION_KEY,invoker.getUrl().getParameter(Constants.APPLICATION_KEY));
        return invoker.invoke(invocation);
    }
}
