package net.dubboclub.catmonitor.registry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;
import net.dubboclub.catmonitor.constants.CatConstants;

import java.util.List;

/**
 * Created by bieber on 2015/11/12.
 */
public class CatRegistryFactoryWrapper implements RegistryFactory {
    private RegistryFactory registryFactory;

    public CatRegistryFactoryWrapper(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    @Override
    public Registry getRegistry(URL url) {
        return new RegistryWrapper(registryFactory.getRegistry(url));
    }

    class RegistryWrapper implements Registry {
        private Registry originRegistry;
        private URL appendProviderAppName(URL url){
            String side = url.getParameter(CommonConstants.SIDE_KEY);
            if(CommonConstants.PROVIDER_SIDE.equals(side)){
                url=url.addParameter(CatConstants.PROVIDER_APPLICATION_NAME,url.getParameter(CommonConstants.APPLICATION_KEY));
            }
            return url;
        }

        public RegistryWrapper(Registry originRegistry) {
            this.originRegistry = originRegistry;
        }

        @Override
        public URL getUrl() {
            return originRegistry.getUrl();
        }

        @Override
        public boolean isAvailable() {
            return originRegistry.isAvailable();
        }

        @Override
        public void destroy() {
            originRegistry.destroy();
        }

        @Override
        public void register(URL url) {
            originRegistry.register(appendProviderAppName(url));
        }

        @Override
        public void unregister(URL url) {
            originRegistry.unregister(appendProviderAppName(url));
        }

        @Override
        public void subscribe(URL url, NotifyListener listener) {
            originRegistry.subscribe(url,listener);
        }

        @Override
        public void unsubscribe(URL url, NotifyListener listener) {
            originRegistry.unsubscribe(url,listener);
        }

        @Override
        public List<URL> lookup(URL url) {
            return originRegistry.lookup(appendProviderAppName(url));
        }
    }
}
