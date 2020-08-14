package org.nw.deploy;

import java.io.File;
import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.nw.web.utils.WebUtils;

/**
 * 调用存储过程，导出sql，包括定义语句和数据 XXX 对于mysql，导出数据没有使用存储过程
 * 
 * @author xuqc
 * @date 2014-5-19 下午04:57:52
 */
public class SQLExporter {
	private static Logger logger = Logger.getLogger(SQLExporter.class.getName());
	public static String DB_FOLDER = "DB";
	public static String ENCODING = "UTF-8";
	String ddl_folder;
	String data_folder;
	// 导出表数据的sql语句的存储过程名称
	public static String export_data_procedure = "usp_GenInsertSql";

	// 1、初始化系统时导出，使用export-init.xml配置文件
	// 2、系统更新时导出，使用export.xml配置文件
	boolean ifInit = false;// 是否是初始化系统时导出

	String ddl_file = "ddl.sql";// 如果是导出到同一个文件，导出到这个文件
	String data_file = "data.sql";// 如果是导出到同一个文件，导出到这个文件
	boolean ifOneFile = true;// 是否导出到同一个文件

	List<ExportVO> eVOs;

	public SQLExporter() {
		this(false);
	}

	public SQLExporter(boolean ifInit) {
		this.ifInit = ifInit;
		System.out.println("ifInit:" + ifInit);

		String projectPath = System.getProperty("user.dir");
		ddl_folder = projectPath + File.separator + "target" + File.separator + DB_FOLDER + File.separator + "ddl";
		File ddl_file = new File(ddl_folder);
		if(!ddl_file.exists()) {
			ddl_file.mkdirs();
		}
		data_folder = projectPath + File.separator + "target" + File.separator + DB_FOLDER + File.separator + "data";
		File data_file = new File(data_folder);
		if(!data_file.exists()) {
			data_file.mkdirs();
		}
		// 从配置文件中读取需要导出的表
		eVOs = getConfig();
		if(eVOs == null || eVOs.size() == 0) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("没有配置表信息，不需要导出！");
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("No configuration table information is not required!");
			}
			throw new RuntimeException("没有配置表信息，不需要导出！");
		}

	}

	@SuppressWarnings("rawtypes")
	private List<ExportVO> getConfig() {
		Document document = ExportConfig.getDocument(ifInit);
		if(document == null) {
			if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
				throw new RuntimeException("读取配置文件错误，配置文件不存在！");
			}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
				throw new RuntimeException("Read the configuration file error, the configuration file does not exist!");
			}
			throw new RuntimeException("读取配置文件错误，配置文件不存在！");
		}
		List<ExportVO> eVOs = new ArrayList<ExportVO>();
		Map<String, ExportVO> map = new HashMap<String, ExportVO>();
		Element root = document.getRootElement();
		List tableAry = root.selectNodes("/export/table");
		if(tableAry != null && tableAry.size() > 0) {
			for(int i = 0; i < tableAry.size(); i++) {
				Element el = (Element) tableAry.get(i);
				String name = el.selectSingleNode("name").getText();
				boolean ddl = Boolean.valueOf(el.selectSingleNode("ddl").getText());
				boolean data = Boolean.valueOf(el.selectSingleNode("data").getText());
				String where = el.selectSingleNode("where").getText();
				if(map.get(name) != null) {
					logger.info("表名称为：" + name + "的配置信息重复了！");
				}
				ExportVO eVO = new ExportVO();
				eVO.setTableName(name);
				eVO.setDdl(ddl);
				eVO.setData(data);
				if(StringUtils.isBlank(where)) {
					where = "1=1";
				}
				eVO.setWhere(where);
				eVOs.add(eVO);

				map.put(name, eVO);
			}
		}
		map.clear();
		return eVOs;
	}

	/**
	 * 导出表的数据
	 */
	public void exportData() {
		if(eVOs == null || eVOs.size() == 0) {
			return;
		}
		logger.info("开始导出INSERT语句...");
		DB db = new DB(ifInit);
		try {
			StringBuffer dataString = new StringBuffer();
			for(ExportVO eVO : eVOs) {
				if(eVO.isData()) {
					if(StringUtils.isBlank(eVO.getTableName())) {
						continue;
					}
					if(StringUtils.isBlank(eVO.getWhere())) {
						eVO.setWhere("1=1");
					}

					StringBuffer sqlBuf = new StringBuffer();
					if(db.isMysql()) {
						String selectSql = "select * from " + eVO.getTableName() + " WITH(NOLOCK) where " + eVO.getWhere();
						selectSql = selectSql.replaceAll("isnull", "ifnull");// mysql函数
						Statement sm = db.getConn().createStatement();
						List<String> sqlList = MysqlGenerator.getColumnNameAndColumeValue(sm, selectSql,
								eVO.getTableName());
						for(String insertSql : sqlList) {
							sqlBuf.append(insertSql);
							sqlBuf.append("\r");// 换行符
						}
					} else {
						ArrayList<HashMap<Object, Object>> list = db.executeProcedureQuery(export_data_procedure,
								new Object[] { eVO.getTableName(), eVO.getWhere() });
						if(list != null && list.size() > 0) {
							logger.info("从表[" + eVO.getTableName() + "]查询到" + list.size() + "条记录！");
							for(HashMap<Object, Object> map : list) {
								Object obj = map.values().iterator().next();
								if(obj != null) {
									String insertSql = obj.toString();
									sqlBuf.append(insertSql);
									sqlBuf.append("\r");// 换行符
									sqlBuf.append(" ; ");
									sqlBuf.append("\r");// 换行符
								}
							}
						}
					}

					if(!ifOneFile) {
						// 生成文件
						String filePath = data_folder + File.separator + eVO.getTableName() + ".sql";
						File dataFile = new File(filePath);
						if(dataFile.exists()) {
							logger.info("数据文件【" + filePath + "】已经存在,先删除！");
							dataFile.delete();
						}
						dataFile = new File(filePath);
						logger.info("生成文件【" + filePath + "】！");
						// 使用gbk编码，因为sql通常包括中文，避免在使用工具osql进行批量执行的时候出现乱码
						FileUtils.writeStringToFile(dataFile, sqlBuf.toString(), ENCODING);
					} else {
						dataString.append(sqlBuf.toString());
						dataString.append("\r");
					}
				}
			}
			if(ifOneFile) {
				String filePath = data_folder + File.separator + data_file;
				File dataFile = new File(filePath);
				if(dataFile.exists()) {
					logger.info("数据文件【" + filePath + "】已经存在,先删除！");
					dataFile.delete();
				}
				dataFile = new File(filePath);
				logger.info("生成文件【" + filePath + "】！");
				FileUtils.writeStringToFile(dataFile, dataString.toString(), ENCODING);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			db.closeConn();
		}
		logger.info("导出INSERT语句结束...");
	}

	/**
	 * 导出表的结构
	 */
	public void exportDdl() {
		if(eVOs == null || eVOs.size() == 0) {
			return;
		}
		logger.info("开始导出DDL...");
		DB db = new DB();
		try {
			StringBuffer ddlBuf = new StringBuffer();
			for(ExportVO eVO : eVOs) {
				if(eVO.isDdl()) {
					if(StringUtils.isBlank(eVO.getTableName())) {
						continue;
					}
					String ddl = db.generateDdl(eVO.getTableName());
					if(StringUtils.isNotBlank(ddl)) {
						if(!ifOneFile) {
							// 生成文件
							String filePath = ddl_folder + File.separator + eVO.getTableName() + ".sql";
							File ddlFile = new File(filePath);
							if(ddlFile.exists()) {
								logger.info("DDL文件【" + filePath + "】已经存在,先删除！");
								ddlFile.delete();
							}
							ddlFile = new File(filePath);
							logger.info("生成文件【" + filePath + "】！");
							FileUtils.writeStringToFile(ddlFile, ddl, ENCODING);
						} else {
							ddlBuf.append(ddl);
							ddlBuf.append("\r");
						}
					}
				}
			}
			if(ifOneFile) {
				String filePath = ddl_folder + File.separator + ddl_file;
				File ddlFile = new File(filePath);
				if(ddlFile.exists()) {
					logger.info("DDL文件【" + filePath + "】已经存在,先删除！");
					ddlFile.delete();
				}
				ddlFile = new File(filePath);
				logger.info("生成文件【" + filePath + "】！");
				FileUtils.writeStringToFile(ddlFile, ddlBuf.toString(), ENCODING);
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			db.closeConn();
		}
		logger.info("导出DDL结束...");
	}

	/**
	 * 导出数据库中的表和表中的数据
	 */
	public void export() {
		logger.info("/********************导出数据库脚本开始*************************/");
		Calendar start = Calendar.getInstance();
		exportDdl();
		exportData();
		logger.info("导出数据库脚本耗时：" + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) + "ms！");
		logger.info("/********************导出数据库脚本结束*************************/");
	}

	public boolean isIfInit() {
		return ifInit;
	}

	public void setIfInit(boolean ifInit) {
		this.ifInit = ifInit;
	}

	public static void main(String[] args) {
		SQLExporter exporter = null;
		if(args.length > 0) {
			exporter = new SQLExporter(new Boolean(args[0]));
		} else {
			exporter = new SQLExporter();
		}
		exporter.export();
	}
}
