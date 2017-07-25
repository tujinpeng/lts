package com.github.ltsopensource.core.cluster;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.ltsopensource.core.AppContext;
import com.github.ltsopensource.core.constant.Constants;
import com.github.ltsopensource.core.constant.ExtConfig;
import com.github.ltsopensource.core.factory.NamedThreadFactory;
import com.github.ltsopensource.core.registry.Registry;
import com.github.ltsopensource.core.registry.RegistryFactory;
import com.github.ltsopensource.core.remoting.RemotingServerDelegate;
import com.github.ltsopensource.core.spi.ServiceLoader;
import com.github.ltsopensource.remoting.RemotingProcessor;
import com.github.ltsopensource.remoting.RemotingServer;
import com.github.ltsopensource.remoting.RemotingServerConfig;
import com.github.ltsopensource.remoting.RemotingTransporter;
import com.github.ltsopensource.zookeeper.DataListener;

/**
 * @author Robert HG (254963746@qq.com) on 8/18/14.
 *         抽象服务端
 */
public abstract class AbstractServerNode<T extends Node, App extends AppContext> extends AbstractJobNode<T, App> {

    protected RemotingServerDelegate remotingServer;

    protected void remotingStart() {

        remotingServer.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            remotingServer.registerDefaultProcessor(defaultProcessor, getDefaultExecutor());            
        }
    }

    public void setListenPort(int listenPort) {
        config.setListenPort(listenPort);
    }

    protected void remotingStop() {
        remotingServer.shutdown();
    }

    @Override
    protected void beforeRemotingStart() {
        RemotingServerConfig remotingServerConfig = new RemotingServerConfig();
        // config 配置
        if (config.getListenPort() == 0) {
            config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
            node.setPort(config.getListenPort());
        }
        remotingServerConfig.setListenPort(config.getListenPort());

        remotingServer = new RemotingServerDelegate(getRemotingServer(remotingServerConfig), appContext);

        registry = RegistryFactory.getRegistry(appContext);
        appContext.setRegistry(registry);
        
        beforeStart();
    }

    private RemotingServer getRemotingServer(RemotingServerConfig remotingServerConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingServer(appContext, remotingServerConfig);
    }

    @Override
    protected void afterRemotingStart() {
        afterStart();
    }

    @Override
    protected void beforeRemotingStop() {
        beforeStop();
    }

    @Override
    protected void afterRemotingStop() {
        afterStop();
    }

    /**
     * 得到默认的处理器
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();
    
    private ExecutorService getDefaultExecutor() {
    	
    	Registry registry = appContext.getRegistry();
    	String path = registry.getAbsolutePath(config, ExtConfig.PROCESSOR_THREAD);
        
    	final int processorSize = registry.getConfig(path, 
        		config.getParameter(ExtConfig.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD));
        
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.
        		newFixedThreadPool(processorSize, new NamedThreadFactory(AbstractServerNode.class.getSimpleName(), true));
        
        registry.addListener(path, new DataListener() {
			
			@Override
			public void dataDeleted(String dataPath) throws Exception {
				// TODO Auto-generated method stub
				int processorSize = config.getParameter(ExtConfig.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
				executor.setCorePoolSize(processorSize);
				executor.setMaximumPoolSize(processorSize);
				LOGGER.info("参数调整"+ExtConfig.PROCESSOR_THREAD+"="+processorSize);
			}
			
			@Override
			public void dataChange(String dataPath, Object data) throws Exception {
				// TODO Auto-generated method stub
				executor.setCorePoolSize((int)data);
				executor.setMaximumPoolSize((int)data);
				LOGGER.info("参数调整"+ExtConfig.PROCESSOR_THREAD+"="+data);
			}
		});
        
        return executor;
        
    }


}
