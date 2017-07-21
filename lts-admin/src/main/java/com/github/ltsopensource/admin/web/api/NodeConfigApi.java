package com.github.ltsopensource.admin.web.api;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.ltsopensource.admin.cluster.BackendAppContext;
import com.github.ltsopensource.admin.request.NodeConfig;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.admin.web.vo.RestfulResponse;
import com.github.ltsopensource.core.cluster.NodeType;
import com.github.ltsopensource.core.commons.utils.TypeUtil;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.core.registry.Registry;

@RestController
@RequestMapping("node")
public class NodeConfigApi {

	private Logger logger = LoggerFactory.getLogger(NodeConfigApi.class);
	@Autowired
	private BackendAppContext appContext;
	
	@RequestMapping("node-config-add")
	public RestfulResponse addNodeConfig(@RequestBody NodeConfig nodeConfig) {
		
		RestfulResponse response = new RestfulResponse();
		
		try {
			Registry registry = getRegistry();
			registry.addConfig(getAbsolutePath(nodeConfig), TypeUtil.getTypeValue(nodeConfig.getConfigValue()));
			
			appContext.getBackendNodeConfigAccess().insert(Collections.singletonList(nodeConfig));
			response.setSuccess(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			response.setMsg("新增节点配置失败");
			logger.error("新增节点配置失败"+nodeConfig, e);
		}
		
		return response;
		
	}
	
	@RequestMapping("node-config-delete")
	public RestfulResponse deleteNodeConfig(NodeConfig nodeConfig) {
		
		RestfulResponse response = new RestfulResponse();
		
		try {
			Registry registry = getRegistry();
			registry.deleteConfig(getAbsolutePath(nodeConfig));
			
			appContext.getBackendNodeConfigAccess().delete(nodeConfig);

			response.setSuccess(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			response.setMsg("删除节点配置失败");
			logger.error("删除节点配置失败"+nodeConfig, e);
		}
		
		return response;
	}
	
	
	@RequestMapping("node-config-update")
	public RestfulResponse updateNodeConfig(@RequestBody Map<String, NodeConfig> nodeConfigs) {
		
		RestfulResponse response = new RestfulResponse();
		
		try {
			Registry registry = getRegistry();
			
			NodeConfig newConfig = nodeConfigs.get("newConfig");
			NodeConfig oldConfig = nodeConfigs.get("oldConfig");
			
			String newPath = getAbsolutePath(newConfig);
			String oldPath = getAbsolutePath(oldConfig);
			
			if(oldPath.equals(newPath)) {
				registry.updateConfig(oldPath, TypeUtil.getTypeValue(newConfig.getConfigValue()));
			} else {
				registry.deleteConfig(oldPath);
				registry.addConfig(newPath, TypeUtil.getTypeValue(newConfig.getConfigValue()));
			}
			
			appContext.getBackendNodeConfigAccess().update(newConfig);
			response.setSuccess(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			response.setMsg("更新节点配置失败");
			logger.error("更新节点配置失败"+nodeConfigs, e);
		}

		return response;
	}
	
	@RequestMapping("node-config-get")
	public RestfulResponse queryNodeConfig(NodeConfig nodeConfig) {
		
		RestfulResponse response = new RestfulResponse();
		
		try {
			PaginationRsp<NodeConfig> result = appContext.getBackendNodeConfigAccess().query(nodeConfig);
			response.setSuccess(true);
			response.setRows(result.getRows());
			response.setResults(result.getResults());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			response.setMsg("节点配置查询失败");
			logger.error("节点配置查询失败"+nodeConfig, e);
		}
		
		return response;
		
		
	}
	
	
	private Registry getRegistry() {
		return appContext.getBackendRegistrySrv().getRegistry();
	}
	
	private String getAbsolutePath(NodeConfig nodeConfig) {
		
		String path = "/LTS/config/" + appContext.getConfig().getClusterName()+"/"+nodeConfig.getNodeType();
		
		if(NodeType.TASK_TRACKER.name().equals(nodeConfig.getNodeType())) {
			path+= "/"+nodeConfig.getNodeGroup();
		}
		
		path+= "/"+nodeConfig.getConfigName();
		
		return path;
		
	}
	
}
