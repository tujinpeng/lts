package com.github.ltsopensource.admin.request;

public class NodeConfig extends PaginationReq {

	private Integer id;
	
	private String configName;
	
	private String configValue;
	
	private String nodeType;
	
	private String nodeGroup;

	private String remark;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "name:"+this.getConfigName()+",value="+this.getConfigValue()
				+",nodeType:"+this.getNodeType()+",nodeGroup:"+this.nodeGroup;
	}
	



	
	
}
