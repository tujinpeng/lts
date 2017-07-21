package com.github.ltsopensource.admin.access.face;

import java.util.List;

import com.github.ltsopensource.admin.request.NodeConfig;
import com.github.ltsopensource.admin.response.PaginationRsp;

public interface BackendNodeConfigAccess {

	void insert(List<NodeConfig> nodeConfigs);
	
	int update(NodeConfig nodeConfig);
	
	int delete(NodeConfig filter);
	
	Long count(NodeConfig filter);
	 
	PaginationRsp<NodeConfig> query(NodeConfig filter);

}
