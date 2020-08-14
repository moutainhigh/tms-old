package org.nw.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nw.web.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 重写delete和saveOrUpdate方法,因为这里不再需要事务,已经统一使用spring进行事务管理
 * 
 * @author xuqc
 * 
 */
public class NWDao extends BaseDao {

	protected static final transient Logger log = Logger.getLogger(NWDao.class);

	@Autowired
	private static DataSource dataSource;

	public NWDao(DataSource datasource) {
		super(datasource);
	}

	public static NWDao getInstance() {
		if(dataSource == null) {
			dataSource = getDs();
		}
		return new NWDao(dataSource);
	}

	private static DataSource getDs() {
		return (DataSource) SpringContextHolder.getBean("dataSource");
	}

}