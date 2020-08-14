package org.activiti.engine;

import javax.sql.DataSource;

import org.nw.dao.NWDao;

public class ActivitiDao extends NWDao {

	public ActivitiDao(DataSource datasource) {
		super(datasource);
	}

}
