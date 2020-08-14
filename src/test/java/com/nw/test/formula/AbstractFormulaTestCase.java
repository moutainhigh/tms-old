package com.nw.test.formula;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.junit.After;
import org.junit.Before;

abstract public class AbstractFormulaTestCase {
	protected DataSource ds = null;

	@Before
	public void setUp() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("driverClassName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		properties.setProperty("url", "jdbc:sqlserver://121.40.174.233:1433;databaseName=luc;");
		properties.setProperty("username", "tms");
		properties.setProperty("password", "123456");
		properties.setProperty("maxActive", "10");
		properties.setProperty("maxIdle", "5");
		properties.setProperty("minIdle", "1");
		properties.setProperty("initialSize", "1");

		ds = BasicDataSourceFactory.createDataSource(properties);
	}

	@After
	public void tearDown() throws Exception {
	}

}
