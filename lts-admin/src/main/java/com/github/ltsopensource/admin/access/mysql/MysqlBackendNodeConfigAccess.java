package com.github.ltsopensource.admin.access.mysql;

import java.util.List;

import com.github.ltsopensource.admin.access.RshHandler;
import com.github.ltsopensource.admin.access.face.BackendNodeConfigAccess;
import com.github.ltsopensource.admin.request.NodeConfig;
import com.github.ltsopensource.admin.response.PaginationRsp;
import com.github.ltsopensource.core.cluster.Config;
import com.github.ltsopensource.monitor.access.mysql.MysqlAbstractJdbcAccess;
import com.github.ltsopensource.store.jdbc.builder.DeleteSql;
import com.github.ltsopensource.store.jdbc.builder.InsertSql;
import com.github.ltsopensource.store.jdbc.builder.OrderByType;
import com.github.ltsopensource.store.jdbc.builder.SelectSql;
import com.github.ltsopensource.store.jdbc.builder.UpdateSql;
import com.github.ltsopensource.store.jdbc.builder.WhereSql;

public class MysqlBackendNodeConfigAccess extends MysqlAbstractJdbcAccess implements BackendNodeConfigAccess {

	public MysqlBackendNodeConfigAccess(Config config) {
		// TODO Auto-generated constructor stub
		super(config);
	}
	
	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return "lts_admin_node_config";
	}
	
	@Override
    public void insert(List<NodeConfig> nodeConfigs) {
        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("config_name",
                        "config_value",
                        "node_type",
                        "node_group",
                        "remark");
        for (NodeConfig config : nodeConfigs) {
            insertSql.values(
            		config.getConfigName(),
            		config.getConfigValue(),
            		config.getNodeType(),
            		config.getNodeGroup(),
            		config.getRemark()
                    );
        }
        insertSql.doBatchInsert();
    }

    @Override
    public int delete(NodeConfig filter) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(filter))
                .doDelete();
    }

    @Override
    public int update(NodeConfig nodeConfig) {
    	// TODO Auto-generated method stub
    	 return new UpdateSql(getSqlTemplate())
         .update()
         .table(getTableName())
         .set("config_name", nodeConfig.getConfigName())
         .set("config_value", nodeConfig.getConfigValue())
         .set("node_Type", nodeConfig.getNodeType())
         .set("node_group", nodeConfig.getNodeGroup())
         .set("remark", nodeConfig.getRemark())
         .where("id = ?", nodeConfig.getId())
         .doUpdate();
    }
    
    @Override
    public PaginationRsp<NodeConfig> query(NodeConfig filter) {
    	
    	PaginationRsp<NodeConfig> response = new PaginationRsp<NodeConfig>();
    	
    	Long results = new SelectSql(getSqlTemplate())
        .select()
        .columns("count(1)")
        .from()
        .table(getTableName())
        .whereSql(
                new WhereSql()
                .andOnNotEmpty("node_type = ?", filter.getNodeType())
                .andOnNotEmpty("node_group = ?", filter.getNodeGroup())
        )
        .single();
    	
    	response.setResults(results.intValue());
		if (results == 0) {
		    return response;
		}

        List<NodeConfig> nodeConfigs = new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(filter))
                .orderBy()
                .column("node_group", OrderByType.DESC)
                .limit(filter.getStart(), filter.getLimit())
                .list(RshHandler.NODE_CONFIG_RSH);
        
        response.setRows(nodeConfigs);
        
        return response;
        
    }

    @Override
    public Long count(NodeConfig filter) {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(filter))
                .single();
    }
    
    private WhereSql buildWhereSql(NodeConfig filter){
        return new WhereSql()
                .andOnNotNull("id = ?", filter.getId())
                .andOnNotEmpty("node_type = ?", filter.getNodeType())
                .andOnNotEmpty("node_group = ?", filter.getNodeGroup());
    }

	
}
