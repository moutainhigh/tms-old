package org.nw.exp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.ext.ref.userdefine.IMultiSelect;
import org.nw.jf.utils.DataTypeConverter;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.IToftService;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.RefUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.ImportConfigBVO;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.WebUtils;

import com.tms.service.base.GoodsService;
import com.tms.vo.base.GoodsVO;

/**
 * excel导入器，只适合导入单表头的档案
 * 
 * @author xuqc
 * @date 2013-8-15 下午10:43:50
 */
public class ExcelImporter {

	protected Logger logger = Logger.getLogger(ExcelImporter.class);

	// 参数
	public ParamVO paramVO;
	

	public void setParamVO(ParamVO paramVO) {
		this.paramVO = paramVO;
	}

	public ExcelImporter() {

	}

	public ExcelImporter(ParamVO paramVO, IToftService service,ImportConfigVO configVO) {
		this.paramVO = paramVO;
		this.service = service;
		this.initColumnDefineMap(configVO);// 初始化列对照关系
		billInfo = this.service.getBillInfo();
	}

	// 当数据整理完成后，需要调用service类进行保存
	public IToftService service;

	public AggregatedValueObject billInfo;

	public void setService(IToftService service) {
		this.service = service;
		billInfo = this.service.getBillInfo();
	}

	// 导入过程中产生的警告信息
	protected StringBuffer logBuf = new StringBuffer();

	protected int importNum = 0;// 导入的个数

	/**
	 * 返回导入过程所产生的日志
	 * 
	 * @return
	 */
	public String getLog() {
		return logBuf.toString();
	}

	public ImportChecker checker;

	/**
	 * 列对照关系
	 */
	public Map<String, ImportConfigBVO> importColumnMap = new HashMap<String, ImportConfigBVO>();

	public String templetFile;

	/**
	 * 下载模板文件
	 * 
	 * @param request
	 * @param response
	 * @param templetFile
	 * @throws IOException
	 */
	public void downladTemplet(HttpServletRequest request, HttpServletResponse response, ParamVO paramVO) {

		this.paramVO = paramVO;
		String pk_import_config = request.getParameter("pk_import_config");
		ImportConfigVO importConfigVO = NWDao.getInstance().queryByCondition(ImportConfigVO.class,
				"pk_import_config=?", pk_import_config);
		if(importConfigVO == null){
			throw new BusiException("模板不存在！");
		}
		this.templetFile = importConfigVO.getTemplet_file();
		if(StringUtils.isBlank(this.templetFile)) {
			throw new BusiException("模板文件的名称不能为空！");
		}
		try {
			response.setContentType("application/octet-stream");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + new String(this.templetFile.getBytes("GBK"), "ISO-8859-1") + "\"");
			String basePath = request.getSession().getServletContext().getRealPath("/");
			basePath += File.separator + "WEB-INF" + File.separator + "resources" + File.separator + "import_template";
			File destFile = new File(basePath + File.separator + this.templetFile);
			if(!destFile.exists()) {
				throw new BusiException("文件不存在,目录[?]！",basePath);
			}
			/* 创建输出流 */
			OutputStream servletOS = response.getOutputStream();
			FileInputStream inputStream = new FileInputStream(destFile);
			byte[] buf = new byte[1024];
			int readLength;
			while(((readLength = inputStream.read(buf)) != -1)) {
				servletOS.write(buf, 0, readLength);
			}
			inputStream.close();
			servletOS.flush();
			servletOS.close();
		} catch(Exception e) {
			throw new BusiException("文件下载出错！", e);
		}
	}

	/**
	 * 读取导入字段的配置信息
	 * 
	 * @return
	 */
	protected AggregatedValueObject getImportConfigVO(ImportConfigVO configVO) {
		ImportConfigBVO[] childVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ImportConfigBVO.class,
				"pk_import_config=? order by display_order", configVO.getPk_import_config());
		AggregatedValueObject billVO = new HYBillVO();
		billVO.setParentVO(configVO);
		billVO.setChildrenVO(childVOs);
		return billVO;
	}

	/**
	 * 返回导入的列定义的map
	 * 
	 * @param paramVO
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected void initColumnDefineMap(ImportConfigVO configVO) {
		AggregatedValueObject aggVO = getImportConfigVO(configVO);
		ImportConfigVO parentVO = (ImportConfigVO) aggVO.getParentVO();
		String sChecker = parentVO.getChecker();
		if(StringUtils.isNotBlank(sChecker)) {
			try {
				Class clazz = Class.forName(sChecker);
				checker = (ImportChecker) clazz.newInstance();
			} catch(Exception e) {
				logger.warn("导入的校验类实例化失败，类名称：" + checker);
				throw new BusiException("导入的校验类实例化失败，类名称:[?]！",checker.getClass().toString());
			}
		}
		CircularlyAccessibleValueObject[] childVOs = aggVO.getChildrenVO();
		for(CircularlyAccessibleValueObject childVO : childVOs) {
			ImportConfigBVO vo = (ImportConfigBVO) childVO;
			importColumnMap.put(vo.getField_name(), vo);
		}
	}

	/**
	 * 导入一个sheet的数据，返回vo集合
	 * 
	 * @param sheet
	 * @param templetVO
	 * @return
	 */
	protected List<AggregatedValueObject> resolve(Sheet sheet, Map<String, BillTempletBVO> itemMap) {
		List<AggregatedValueObject> aggVOs = new ArrayList<AggregatedValueObject>();// excel文件的数据转换成的vo集合
		int rowNum = sheet.getLastRowNum() + 1; // 得到总行数,这里的总行数是从0开始的,+1
		Row headerRow = sheet.getRow(0); // 标题行
		int colNum = headerRow.getPhysicalNumberOfCells();// 列数。
		for(int i = 1; i < rowNum; i++) {
			Row row = sheet.getRow(i); // 正文行
			if(row == null) {
				continue;
			}
			SuperVO parentVO = getParentVO();// 每一行对应一个VO
			int j = 0;
			while(j < colNum) {
				String title = getStringCellValue(headerRow.getCell(j), null);// excel文件的标题栏
				if(StringUtils.isBlank(title)) {
					// 如果标题栏为空，忽略该列，比如是序号列
					j++;
					continue;
				}

				ImportConfigBVO importConfigBVO = importColumnMap.get(title);
				if(importConfigBVO == null) {
					j++;
					continue;
				}

				String fieldCode = importConfigBVO.getField_code();// 导入字段中定义的字段编码
				BillTempletBVO fieldVO = itemMap.get(fieldCode);
				String value = getStringCellValue(row.getCell(j), importConfigBVO.getCell_type());// excel文件中的字段的值
				if(StringUtils.isBlank(value)) {// 如果配置为是非空字段，此时value为空的花，不导入改行
					if(importConfigBVO.getNotnull_flag() != null && importConfigBVO.getNotnull_flag().booleanValue()) {
						j++;
						parentVO = null;// 将superVO设置成null,以便不加入
						//不在跳过，直接报错。
						throw new BusiException("第[?]行的[?:?]数据不能为空\n",(i + 1 + ""),title,value);
						//logBuf.append("第" + (i + 1) + "行的[" + title + ":" + value + "]数据不能为空\n");
						//continue;
					}
				}
				Object realValue;
				if(fieldVO == null) {
					// 模板中没有对应的字段，那么看看这个字段是否是ＶＯ的一个成员变量
					Field field = ReflectionUtils.findField(parentVO.getClass(), fieldCode);
					if(field == null) {
						j++;
						continue;
					} else {
						// 是成员变量，那么根据变量类型去设置值，对于成员变量，不需要调用检测接口
						realValue = getRealValueByField(value, field);
					}
				} else {
					realValue = getRealValueByTemplet(value, fieldVO, parentVO);// 根据数据类型转换后的值
					if(checker != null) {
						checker.setService(this.service);
						String warnMsg = checker.check(realValue, fieldVO, i + 1);
						if(StringUtils.isNotBlank(warnMsg)) {
							// 该数据不是有效的数据,这里要区分,如果是关键数据,那么整行都不导入,否则该字段不导入即可
							if(importConfigBVO.getKeyfield_flag() != null
									&& importConfigBVO.getKeyfield_flag().booleanValue()) {
								// 关键字段，忽略该行
								j++;
								parentVO = null;// 将superVO设置成null,以便不加入
								//不在跳过，直接报错。
								throw new BusiException("第[?]行的[?:?]数据有误,错误信息[?]\n",(i + 1 + ""),title,value,warnMsg);
//								logBuf.append("第" + (i + 1) + "行的[" + title + ":" + value + "]数据有误,错误信息：" + warnMsg
//										+ "\n");
//								break;
							} else {
								// 不是关键字段，设置为null值
								realValue = null;
							}
						}
					}
				}
				setValueToSuperVO(fieldVO, parentVO, fieldCode, realValue);
				j++;
			}
			if(parentVO != null) {
				setDefaultValue(parentVO, i + 1);
				processAfterResolveOneRow(parentVO, aggVOs, i + 1);
			}
		}
		importNum = aggVOs.size();
		logBuf.append("共识别到" + importNum + "条记录！\n");
		return aggVOs;
	}

	protected List<AggregatedValueObject> resolveForMultiTable(Sheet sheet, Map<String, BillTempletBVO> itemMap) {
		List<AggregatedValueObject> aggVOs = new ArrayList<AggregatedValueObject>();// excel文件的数据转换成的vo集合
		int rowNum = sheet.getLastRowNum() + 1; // 得到总行数,这里的总行数是从0开始的,+1
		Row headerRow = sheet.getRow(0); // 标题行
		int colNum = headerRow.getPhysicalNumberOfCells();// 列数。
		for(int i = 1; i < rowNum; i++) {
			Row row = sheet.getRow(i); // 正文行
			if(row == null) {
				continue;
			}
			
			SuperVO parentVO = getParentVO();// 表头的VO
			List<SuperVO> childrenVO = getChildVOForMultiTable();// 表体的VO
			List<SuperVO> reallyChildrenVO = new ArrayList<SuperVO>();// 表体的VO
			SuperVO superVO = parentVO;// 默认是表头的VO
			
			int j = 0;
			while(j < colNum) {
				String title = getStringCellValue(headerRow.getCell(j), null);// excel文件的标题栏
				if(StringUtils.isBlank(title)) {
					// 如果标题栏为空，忽略该列，比如是序号列
					j++;
					continue;
				}
				ImportConfigBVO importConfigBVO = importColumnMap.get(title);
				if(importConfigBVO == null) {
					j++;
					continue;
				}
				String fieldCodeAndVOName = importConfigBVO.getField_code();// 导入字段中定义的字段编码，可能是表头，也可能是表体
				String fieldCode = new String();
				String tableName = new String();
				BillTempletBVO fieldVO = itemMap.get(fieldCodeAndVOName);// 读取对应的模板的字段配置VO
				int index = fieldCodeAndVOName.indexOf(".");
				fieldCode = fieldCodeAndVOName.substring(index + 1);
				if(index != -1) {
					// 表体的字段
					// String tabcode = fieldCode.substring(0, index);// 所在的页签编码
					// 这个才是模板中的字段名称，即itemkey,使用点号说明是表体的字段
					tableName = fieldCodeAndVOName.substring(0, index);
					for(SuperVO childVO : childrenVO){
						if(tableName.equals(childVO.getTableName())){
							superVO = childVO;
							if(!reallyChildrenVO.contains(childVO)){
								reallyChildrenVO.add(childVO);
							}
							break;
						}
					}
					
				} else {
					// 表头的字段
					superVO = parentVO;
				}

				String value = getStringCellValue(row.getCell(j), importConfigBVO.getCell_type());// excel文件中的字段的值
				if(StringUtils.isBlank(value)) {// 如果配置为是非空字段，此时value为空的花，不导入改行
					if(importConfigBVO.getNotnull_flag() != null && importConfigBVO.getNotnull_flag().booleanValue()) {
						j++;
						superVO = null;// 将superVO设置成null,以便不加入
						logBuf.append("第" + i + "行的[" + title + ":" + value + "]数据不能为空\n");
						continue;
					}
				}
				Object realValue;
				if(fieldVO == null) {
					// 模板中没有对应的字段，那么看看这个字段是否是ＶＯ的一个成员变量
					Field field = ReflectionUtils.findField(superVO.getClass(), fieldCode);
					if(field == null) {
						j++;
						continue;
					} else {
						// 是成员变量，那么根据变量类型去设置值，对于成员变量，不需要调用检测接口
						realValue = getRealValueByField(value, field);
					}
				} else {
					realValue = getRealValueByTemplet(value, fieldVO, superVO);// 根据数据类型转换后的值
					if(checker != null) {
						checker.setService(this.service);
						String warnMsg = checker.check(realValue, fieldVO, i + 1);
						if(StringUtils.isNotBlank(warnMsg)) {
							// 该数据不是有效的数据,这里要区分,如果是关键数据,那么整行都不导入,否则该字段不导入即可
							if(importConfigBVO.getKeyfield_flag() != null
									&& importConfigBVO.getKeyfield_flag().booleanValue()) {
								// 关键字段，忽略该行
								j++;
								superVO = null;// 将superVO设置成null,以便不加入
								logBuf.append("第" + i + "行的[" + title + ":" + value + "]数据有误,错误信息：" + warnMsg + "\n");
								break;
							} else {
								// 不是关键字段，设置为null值
								realValue = null;
							}
						}
					}
				}
				setValueToSuperVO(fieldVO, superVO, fieldCode, realValue);
				j++;
			}
			if(superVO != null) {
				processAfterResolveOneRowForMultiTable(parentVO, reallyChildrenVO, aggVOs, i + 1);
					
			}
		}
		importNum = aggVOs.size();
		logBuf.append("共识别到" + importNum + "条记录！\n");
		return aggVOs;
	}
	
	/*protected List<AggregatedValueObject> resolveForMultiTable(Sheet sheet, Map<String, BillTempletBVO> itemMap) {
		List<AggregatedValueObject> aggVOs = new ArrayList<AggregatedValueObject>();// excel文件的数据转换成的vo集合
		int rowNum = sheet.getLastRowNum() + 1; // 得到总行数,这里的总行数是从0开始的,+1
		Row headerRow = sheet.getRow(0); // 标题行
		int colNum = headerRow.getPhysicalNumberOfCells();// 列数。
		for(int i = 1; i < rowNum; i++) {
			Row row = sheet.getRow(i); // 正文行
			if(row == null) {
				continue;
			}
			SuperVO parentVO = getParentVO();// 每一行对应一个VO
			int j = 0;
			while(j < colNum) {
				String title = getStringCellValue(headerRow.getCell(j), null);// excel文件的标题栏
				if(StringUtils.isBlank(title)) {
					// 如果标题栏为空，忽略该列，比如是序号列
					j++;
					continue;
				}

				ImportConfigBVO importConfigBVO = importColumnMap.get(title);
				if(importConfigBVO == null) {
					j++;
					continue;
				}

				String fieldCode = importConfigBVO.getField_code();// 导入字段中定义的字段编码
				BillTempletBVO fieldVO = itemMap.get(fieldCode);
				String value = getStringCellValue(row.getCell(j), importConfigBVO.getCell_type());// excel文件中的字段的值
				if(StringUtils.isBlank(value)) {// 如果配置为是非空字段，此时value为空的花，不导入改行
					if(importConfigBVO.getNotnull_flag() != null && importConfigBVO.getNotnull_flag().booleanValue()) {
						j++;
						parentVO = null;// 将superVO设置成null,以便不加入
						logBuf.append("第" + (i + 1) + "行的[" + title + ":" + value + "]数据不能为空\n");
						continue;
					}
				}
				Object realValue;
				if(fieldVO == null) {
					// 模板中没有对应的字段，那么看看这个字段是否是ＶＯ的一个成员变量
					Field field = ReflectionUtils.findField(parentVO.getClass(), fieldCode);
					if(field == null) {
						j++;
						continue;
					} else {
						// 是成员变量，那么根据变量类型去设置值，对于成员变量，不需要调用检测接口
						realValue = getRealValueByField(value, field);
					}
				} else {
					realValue = getRealValueByTemplet(value, fieldVO, parentVO);// 根据数据类型转换后的值
					if(checker != null) {
						checker.setService(this.service);
						String warnMsg = checker.check(realValue, fieldVO, i + 1);
						if(StringUtils.isNotBlank(warnMsg)) {
							// 该数据不是有效的数据,这里要区分,如果是关键数据,那么整行都不导入,否则该字段不导入即可
							if(importConfigBVO.getKeyfield_flag() != null
									&& importConfigBVO.getKeyfield_flag().booleanValue()) {
								// 关键字段，忽略该行
								j++;
								parentVO = null;// 将superVO设置成null,以便不加入
								logBuf.append("第" + (i + 1) + "行的[" + title + ":" + value + "]数据有误,错误信息：" + warnMsg
										+ "\n");
								break;
							} else {
								// 不是关键字段，设置为null值
								realValue = null;
							}
						}
					}
				}
				setValueToSuperVO(fieldVO, parentVO, fieldCode, realValue);
				j++;
			}
			if(parentVO != null) {
				setDefaultValue(parentVO, i + 1);
				processAfterResolveOneRow(parentVO, aggVOs, i + 1);
			}
		}
		importNum = aggVOs.size();
		logBuf.append("共识别到" + importNum + "条记录！\n");
		return aggVOs;
	}*/

	
	
	// 为每一行设置默认值
	public void setDefaultValue(SuperVO parentVO, int rowNum) {
	}

	/**
	 * 识别一行后执行的动作
	 * 
	 * @param parentVO
	 * @param aggVOs
	 */
	public static final String CODE = "cust_code";
	public void processAfterResolveOneRowForMultiTable(SuperVO parentVO, List<SuperVO> childrenVO , List<AggregatedValueObject> aggVOs,
			int rowNum) {
		Object code = parentVO.getAttributeValue(CODE);
		AggregatedValueObject aggVO = null;
		SuperVO currentVO = null;
		if(code != null) {// 存在单据号,检查当前是否已经存在该单据号了
			String vbillno = code.toString();
			for(AggregatedValueObject billVO : aggVOs) {
				SuperVO superVO = (SuperVO) billVO.getParentVO();
				if(vbillno.equals(superVO.getAttributeValue(CODE).toString())) {
					aggVO = billVO;
					currentVO = superVO;
					break;
				}
			}
		}
		if(aggVO != null){
			for(SuperVO childVO : childrenVO){
				// 导入的数据中已经包含了单据号，而且单据号已经存在，那么这里只使用其表体的数据
				// 对导入的这一行数据的表头和表体进行关联处理
				childVO.setStatus(VOStatus.NEW);
				childVO.setPrimaryKey(null);
				NWDao.setUuidPrimaryKey(childVO);
				childVO.setAttributeValue(getParentPkInChild(), currentVO.getPrimaryKey());
				CircularlyAccessibleValueObject[] childVOs = null;
					// 多子表
				childVOs = ((IExAggVO) aggVO).getTableVO(childVO.getTableName());// table_name务必和tabcode一致
				if(childVOs != null && childVOs.length > 0) {
					CircularlyAccessibleValueObject[] newChildVOs = (CircularlyAccessibleValueObject[]) Array.newInstance(
							childVOs[0].getClass(), childVOs.length + 1);
					for(int i = 0; i < childVOs.length; i++) {
						newChildVOs[i] = childVOs[i];
					}
				newChildVOs[newChildVOs.length - 1] = childVO;
				// 多子表
				((IExAggVO) aggVO).setTableVO(childVO.getTableName(), newChildVOs);
				}
			}
		}else{
			AggregatedValueObject billVO = getAggVO();
			for(SuperVO childVO : childrenVO){
				parentVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(parentVO);
				billVO.setParentVO(parentVO);
				// 对导入的这一行数据的表头和表体进行关联处理
				childVO.setStatus(VOStatus.NEW);
				childVO.setPrimaryKey(null);
				NWDao.setUuidPrimaryKey(childVO);
				childVO.setAttributeValue(getParentPkInChild(), parentVO.getPrimaryKey());
				CircularlyAccessibleValueObject[] vos = (CircularlyAccessibleValueObject[]) Array.newInstance(
						childVO.getClass(), 1);
				vos[0] = childVO;
				// 多子表
				((IExAggVO) billVO).setTableVO(childVO.getTableName(), vos);
			}
			aggVOs.add(billVO);
		}
	}
	
	
	public void processAfterResolveOneRow(SuperVO parentVO, List<AggregatedValueObject> aggVOs, int rowNum) {
		if(parentVO != null) {
			parentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(parentVO);
			AggregatedValueObject aggVO = new HYBillVO();
			aggVO.setParentVO(parentVO);
			aggVOs.add(aggVO);
		}
	}

	/**
	 * 为属性设置值，如果是参照需要做点特殊处理
	 * 
	 * @param fieldVO
	 * @param superVO
	 * @param fieldCode
	 * @param realValue
	 */
	protected void setValueToSuperVO(BillTempletBVO fieldVO, SuperVO superVO, String fieldCode, Object realValue) {
		if(realValue == null) {
			return;
		}
		if(realValue instanceof RefVO) {
			RefVO refVO = (RefVO) realValue;
			superVO.setAttributeValue(fieldVO.getItemkey() == null ? fieldCode : fieldVO.getItemkey(),
					refVO.getPk());
		} else {
			superVO.setAttributeValue(fieldCode, realValue);
		}
	}

	/**
	 * 设置值到superVO中，根据字段类型进行转换
	 * 
	 * @param superVO
	 * @param fieldName
	 * @param value
	 */
	protected void setValueToSuperVO(SuperVO superVO, String fieldName, Object value) {
		Field field = ReflectionUtils.findField(superVO.getClass(), fieldName);
		if(field != null) {
			Object realValue = null;
			if(value != null) {
				realValue = getRealValueByField(value.toString(), field);
			}
			superVO.setAttributeValue(fieldName, realValue);
		}
	}

	/**
	 * 解析excel文件，返回superVO
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public List<AggregatedValueObject> resolve(File file) throws Exception {
		if(file == null) {
			throw new BusiException("没有选择要导入的文件！");
		}
		if(service == null) {
			throw new BusiException("导入模板时需要注入Service类！");
		}
		InputStream input = null;
		Workbook wb = null;
		try {
			UiBillTempletVO templetVO = this.service.getBillTempletVOByFunCode(paramVO);
			Map<String, BillTempletBVO> itemMap = getBillTempletVOMap(templetVO);
			input = new FileInputStream(file);
			wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(0);
			return resolve(sheet, itemMap);
		} finally {
			try {
				input.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	public List<AggregatedValueObject> resolveForMultiTable(File file) throws Exception {
		if(file == null) {
			throw new BusiException("没有选择要导入的文件！");
		}
		if(service == null) {
			throw new BusiException("导入模板时需要注入Service类！");
		}
		InputStream input = null;
		Workbook wb = null;
		try {
			UiBillTempletVO templetVO = this.service.getBillTempletVOByFunCode(paramVO);
			Map<String, BillTempletBVO> itemMap = getBillTempletVOMap(templetVO);
			input = new FileInputStream(file);
			wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(0);
			return resolveForMultiTable(sheet, itemMap);
		} finally {
			try {
				input.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 导入excel数据
	 * 
	 * @param file
	 * @return
	 */
	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		List<String> codeFieldValues  = new ArrayList<String>();
		if(aggVOs != null && aggVOs.size() > 0) {
			for(AggregatedValueObject billVO : aggVOs) {
				//验证编码是否一致
				String codeField = this.service.getCodeFieldCode();
				String codeFieldValue = billVO.getParentVO().getAttributeValue(codeField) == null? "" : billVO.getParentVO().getAttributeValue(codeField).toString();
				if(StringUtils.isNotBlank(codeField) && 
						StringUtils.isNotBlank(codeFieldValue)){
					//货品导入特殊处理
					if(codeField.equals(GoodsVO.GOODS_CODE)){
						//货品编码需要单独与数据库进行比较
						GoodsVO goodsVO = (GoodsVO) billVO.getParentVO();
						GoodsService goodsService = (GoodsService) this.service;
						GoodsVO old_goodsVO = goodsService.getByGoodsCodeCustomCode(goodsVO.getGoods_code(), goodsVO.getPk_customer());
						if(old_goodsVO != null){
							throw new BusiException("数据库中已存在编码为[?]的记录！",codeFieldValue);
						}
						String custFieldValue = billVO.getParentVO().getAttributeValue(GoodsVO.PK_CUSTOMER) == null ? "" : billVO.getParentVO().getAttributeValue(GoodsVO.PK_CUSTOMER).toString();
						codeFieldValue += custFieldValue;
					}
					if(codeFieldValues.contains(codeFieldValue)){
						throw new BusiException("导入编码不允许重复[?]！",codeFieldValue);
					}else{
						codeFieldValues.add(codeFieldValue);
					}
					
				}
				
				processBeforeImport(billVO, paramVO);
				execFormula(billVO);
				this.service.save(billVO, paramVO);
				processAfterImport(billVO, paramVO);
			}
			logBuf.append("共导入" + importNum + "记录");
			logger.info(logBuf.toString());
		}
	}

	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		if(billVO == null) {
			throw new BusiException("没有需要保存的数据！");
		}
		AbsToftServiceImpl service = (AbsToftServiceImpl) this.service;
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		if(parentVO != null) {
			service.setCommonField(parentVO);
			service.setCodeField(parentVO, paramVO);
		}

		if(billVO instanceof IExAggVO) {
			// 多表体
			IExAggVO exAggVO = ((IExAggVO) billVO);
			String[] tableCodes = exAggVO.getTableCodes();
			for(String tableCode : tableCodes) {
				CircularlyAccessibleValueObject[] cvos = exAggVO.getTableVO(tableCode);
				if(cvos != null) {
					for(CircularlyAccessibleValueObject cvo : cvos) {
						service.setCommonField(cvo);
					}
				}
			}
		} else {
			CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
			if(cvos != null && cvos.length > 0) {
				if(parentVO == null) {
					// 单表体
					service.setCodeField(cvos[0], paramVO);
				}
				for(CircularlyAccessibleValueObject cvo : cvos) {
					service.setCommonField(cvo);
				}
			}
		}
	}

	/**
	 * 导入时候的保存前动作
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	protected void processBeforeImport(AggregatedValueObject billVO, ParamVO paramVO) {
		// 设置公司字段
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		if(parentVO != null) {
			Field field = ReflectionUtils.findField(parentVO.getClass(), "pk_corp");
			if(field != null && parentVO.getAttributeValue("pk_corp") == null) {
				parentVO.setAttributeValue("pk_corp", WebUtils.getLoginInfo().getPk_corp());
			}
		}
	}

	/**
	 * 导入时候的数据保存以后执行的动作
	 * 
	 * @param billVO
	 * @param paramVO
	 */
	protected void processAfterImport(AggregatedValueObject billVO, ParamVO paramVO) {

	}

	/**
	 * 返回vo的一个实例
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected SuperVO getParentVO() {
		try {
			Class<? extends SuperVO> voClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			return voClass.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回单据模板中字段的map对象，以itemkey作为key。如果是表体，那么tabcode作为前缀，如ts_inv_pack_b.
	 * goods_code
	 * 
	 * @param paramVO
	 * @return
	 */
	protected Map<String, BillTempletBVO> getBillTempletVOMap(UiBillTempletVO templetVO) {
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		Map<String, BillTempletBVO> itemMap = new HashMap<String, BillTempletBVO>();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				itemMap.put(fieldVO.getItemkey(), fieldVO);
			} else {
				String table_code = fieldVO.getTable_code();
				itemMap.put(table_code + "." + fieldVO.getItemkey(), fieldVO);
			}
		}
		return itemMap;
	}

	/**
	 * 返回模板的编辑公式，导入时需要同时执行编辑公式
	 * 
	 * @param templetVO
	 * @return
	 */
	protected String[] getFormulaAry(Map<String, BillTempletBVO> itemMap, int pos, String tabcode) {
		List<String> formulaAry = new ArrayList<String>();
		Iterator<BillTempletBVO> it = itemMap.values().iterator();
		while(it.hasNext()) {
			BillTempletBVO fieldVO = it.next();
			if(StringUtils.isNotBlank(tabcode)) {
				if(!fieldVO.getTable_code().equals(tabcode)) {
					continue;
				}
			}
			if(fieldVO.getPos().intValue() == pos) {
				if(StringUtils.isNotBlank(fieldVO.getEditformula())) {
					formulaAry.add(fieldVO.getEditformula());
				}
			}
		}
		return formulaAry.toArray(new String[formulaAry.size()]);
	}

	@SuppressWarnings("unchecked")
	protected void execFormula(AggregatedValueObject billVO) {
		if(billVO == null) {
			return;
		}
		// 执行公式，并将执行公式后的值设置到vo中，保存vo
		Map<String, Object> retMap = this.service.execFormula4Templet(billVO, paramVO);
		SuperVO parentVO = (SuperVO) billVO.getParentVO();
		if(parentVO != null) {
			// 存在表头
			Map<String, Object> headerMap = (Map<String, Object>) retMap.get(Constants.HEADER);
			for(String key : headerMap.keySet()) {
				Object obj = headerMap.get(key);
				if(obj instanceof RefVO) {
					RefVO refVO = (RefVO) obj;
					parentVO.setAttributeValue(key, refVO.getPk());
				} else {
					setValueToSuperVO(parentVO, key, headerMap.get(key));
				}
			}
		}
		CircularlyAccessibleValueObject[] childVOs = null;
		if(billVO instanceof IExAggVO) {
			// 多子表
			String[] tableCodes = ((IExAggVO) billVO).getTableCodes();
			for(String tableCode : tableCodes) {
				CircularlyAccessibleValueObject[] cvos = ((IExAggVO) billVO).getTableVO(tableCode);
				if(cvos != null && cvos.length > 0) {
					childVOs = cvos;
				}
			}
		} else {
			childVOs = billVO.getChildrenVO();
		}
		if(childVOs != null && childVOs.length > 0) {
			Map<String, Object> bodyMap = (Map<String, Object>) retMap.get(Constants.BODY);
			String table_name = ((SuperVO) childVOs[0]).getTableName();// 这里的table_name必须和模板中tabcode对应
			List<Map<String, Object>> bodyList = (List<Map<String, Object>>) bodyMap.get(table_name);
			for(int i = 0; i < childVOs.length; i++) {
				SuperVO childVO = (SuperVO) childVOs[i];
				Map<String, Object> rowMap = bodyList.get(i);
				for(String key : rowMap.keySet()) {
					Object obj = rowMap.get(key);
					if(obj instanceof RefVO) {
						RefVO refVO = (RefVO) obj;
						childVO.setAttributeValue(key, refVO.getPk());
					} else {
						setValueToSuperVO(childVO, key, rowMap.get(key));
					}
				}
			}
		}
	}

	@Deprecated
	protected void execFormula(List<AggregatedValueObject> aggVOs) {
		if(aggVOs != null && aggVOs.size() > 0) {
			// 执行公式，并将执行公式后的值设置到vo中，保存vo
			for(AggregatedValueObject billVO : aggVOs) {
				execFormula(billVO);
			}
		}
	}

	/**
	 * 返回根据ＶＯ中成员变量的类型的值
	 * 
	 * @param value
	 * @param field
	 * @return
	 */
	protected Object getRealValueByField(String value, Field field) {
		if(StringUtils.isBlank(value)) {
			return null;
		}
		Object realValue = value;
		if(field.getType() == Integer.class) {
			if(StringUtils.isBlank(value)) {
				value = "0";
			}
			if(value.indexOf(".") != -1) {
				// 包括小数点，直接去掉小数点
				value = value.substring(0, value.indexOf("."));
			}
			try {
				realValue = Integer.parseInt(value);
			} catch (Exception e) {
				realValue = value;
			}
			
		} else if(field.getType() == UFDouble.class) {
			if(StringUtils.isBlank(value)) {
				value = "0";
			}
			realValue = new UFDouble(value);
		} else if(field.getType() == UFDate.class) {
			realValue = new UFDate(value);
		} else if(field.getType() == UFDateTime.class) {
			realValue = new UFDateTime(value);
		} else if(field.getType() == UFBoolean.class) {
			if(Constants.Y.equalsIgnoreCase(value) || Constants.TRUE.equalsIgnoreCase(value)) {
				realValue = UFBoolean.TRUE;
			} else {
				realValue = UFBoolean.FALSE;
			}
		}
		return realValue;
	}

	/**
	 * 根据模板中的培训，将excel中设置的值转成实际可以设置到数据库中的值
	 * 
	 * @param value
	 * @param fieldVO
	 * @param superVO
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Object getRealValueByTemplet(String value, BillTempletBVO fieldVO, SuperVO superVO) {
		if(StringUtils.isBlank(value)) {
			return null;
		}
		Object realValue = value;
		try {
			if(DATATYPE.INTEGER.equals(fieldVO.getDatatype())) {
				if(StringUtils.isBlank(value)) {
					value = "0";
				}
				if(value.indexOf(".") != -1) {
					// 包括小数点，直接去掉小数点
					value = value.substring(0, value.indexOf("."));
				}
				realValue = Integer.parseInt(value);
			} else if(DATATYPE.DECIMAL.equals(fieldVO.getDatatype())) {
				if(StringUtils.isBlank(value)) {
					value = "0";
				}
				realValue = new UFDouble(value);
			} else if(DATATYPE.CHECKBOX.equals(fieldVO.getDatatype())) {
				// 逻辑框
				realValue = getCheckboxValue(value);
			} else if(DATATYPE.SELECT.equals(fieldVO.getDatatype())) {
				// 下拉
				realValue = UiTempletUtils.getSelectValue(value, fieldVO.getReftype());
				if(realValue != null) {
					// 对于下拉，再根据字段属性去判断,经常下拉返回的数据是Integer
					Field field = ReflectionUtils.findField(superVO.getClass(), fieldVO.getItemkey());
					if(field != null) {
						realValue = this.getRealValueByField(realValue.toString(), field);
					}
				}
			} else if(DATATYPE.DATE.equals(fieldVO.getDatatype())) {
				realValue = new UFDate(value);
				Field field = ReflectionUtils.findField(superVO.getClass(), fieldVO.getItemkey());
				if(field != null) {
					realValue = this.getRealValueByField(realValue.toString(), field);
				}
			} else if(DATATYPE.TIMESTAMP.equals(fieldVO.getDatatype())) {
				realValue = new UFDateTime(value);
				// 有些情况下可能UFDateTime类型，但是实际的字段类型是string，如发货单的“要求提货日期”，“要求到货日期”
				Field field = ReflectionUtils.findField(superVO.getClass(), fieldVO.getItemkey());
				if(field != null) {
					realValue = this.getRealValueByField(realValue.toString(), field);
				}
			} else if(DATATYPE.REF.equals(fieldVO.getDatatype())) {
				// 参照
				String refclass = DataTypeConverter.getRefClazz(fieldVO.getReftype(), fieldVO.getDatatype());
				BaseRefModel refModel = null;
				Class<?> clazz = Class.forName(refclass);
				Object obj = clazz.newInstance();
				if(obj instanceof IMultiSelect) {
					// 如果是多选类型
					IMultiSelect multiSelect = (IMultiSelect) obj;
					String consult_code = multiSelect.getConsult_code();
					if(StringUtils.isNotBlank(consult_code)) {
						if(value.startsWith("[") && value.endsWith("]")) {
							value = value.substring(1, value.length() - 1);
						}
						String[] valueAry = value.split(",");
						if(valueAry.length > 0) {
							StringBuffer buf = new StringBuffer();
							buf.append("[");
							for(int i = 0; i < valueAry.length; i++) {
								buf.append(UiTempletUtils.getSelectValue(valueAry[i], consult_code));
								if(i != valueAry.length - 1) {
									buf.append("]");
								}
							}
							realValue = buf.toString();
						}
					}
				} else {
					// 参照
					refModel = (BaseRefModel) obj;
					// 这里会根据code以及name去查询,如果返回多个值，不处理
					Map<String, Object> dataMap = refModel.getByCode(value);
					Object dataObj = dataMap.get("data");
					if(dataObj == null) {
						//return null;
						throw new BusiException("参照不存在对应的值！");
					} else if(dataObj instanceof List) {// 如果返回多条记录，这里只取第一条
						List list = (List) dataObj;
						dataObj = list.get(0);
					} else if(dataObj instanceof SuperVO) {
						SuperVO vo = (SuperVO) dataObj;
						realValue = RefUtils.convert(refModel, vo);// 此时的realValue是一个refVO
					} else if(dataObj instanceof Map){
						SuperVO vo = (SuperVO) dataObj;
						realValue = RefUtils.convert(refModel, vo);// 此时的realValue是一个refVO
					}else {
//						Map map = (Map) dataObj;
//						realValue = RefUtils.convert(refModel, map);// 此时的realValue是一个refVO
					}
				}
			}
		} catch(Exception e) {
			String error = "Excel中单元格的数据值解析错误，值：[?]，错误信息：[?]";
			logger.error(error);
			throw new BusiException(error,value, e.getMessage());
		}
		return realValue;
	}

	/**
	 * 逻辑框的值，页面上一般写的是“是、Y、1”等
	 * 
	 * @param value
	 * @return
	 */
	protected UFBoolean getCheckboxValue(String value) {
		if("是".equals(value) || "Y".equalsIgnoreCase(value) || "1".equals(value)) {
			return UFBoolean.TRUE;
		}
		return UFBoolean.FALSE;
	}

	/**
	 * 获取单元格数据,根据模板中定义的字段的类型
	 * 
	 * @param cell
	 * @return
	 */
	protected String getStringCellValue(Cell cell, Integer cellType) {
		if(cell == null) {
			return "";
		}
		if(cellType == null) {
			cellType = Cell.CELL_TYPE_STRING;// 默认字符型
		}
		String strCell = "";
		switch(cellType){
		case Cell.CELL_TYPE_STRING:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			strCell = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			try {
				DecimalFormat df = new DecimalFormat("0.00000000");
				strCell = df.format(cell.getNumericCellValue());
			} catch(Exception e) {
				strCell = cell.getStringCellValue();
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			strCell = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_BLANK:
			strCell = "";
			break;
		case CellTypeConst.DATE:
			try {
				Date date = cell.getDateCellValue();
				strCell = DateUtils.formatDate(date, DateUtils.DATEFORMAT_HORIZONTAL);
			} catch(Exception e) {
				// 如果异常，使用string类型读取一次
				strCell = cell.getStringCellValue();
			}
			break;
		case CellTypeConst.DATETIME:
			try {
				Date date1 = cell.getDateCellValue();
				strCell = DateUtils.formatDate(date1, DateUtils.DATETIME_FORMAT_HORIZONTAL);
			} catch(Exception e) {
				// 如果异常，使用string类型读取一次
				strCell = cell.getStringCellValue();
			}
			break;
		default:
			strCell = "";
			break;
		}
		return strCell == null ? null : strCell.trim();
	}

	public static void main(String[] args) {
		String str = "[132]";
		System.out.println(str.substring(1, str.length() - 1));
	}
	
	protected String getParentPkInChild() {
		CircularlyAccessibleValueObject[] childrenVOTableArray = billInfo.getChildrenVO();
		return childrenVOTableArray[0].getAttributeValue(VOTableVO.PKFIELD).toString().trim();
	}
	
	@SuppressWarnings("unchecked")
	protected List<SuperVO> getChildVOForMultiTable() {
		try {
			CircularlyAccessibleValueObject[] childrenVOTableArray = billInfo.getChildrenVO();
			List<SuperVO> childrenVO = new ArrayList<SuperVO>();
			for(CircularlyAccessibleValueObject circularlyAccessibleValueObject : childrenVOTableArray){
				Class<? extends SuperVO> childrenVOClass = (Class<? extends SuperVO>) Class.forName(circularlyAccessibleValueObject
						.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
				childrenVO.add(childrenVOClass.newInstance());
			}
			return childrenVO;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected AggregatedValueObject getAggVO() {
		try {
			Class<?> voClass = (Class<?>) Class.forName(billInfo.getParentVO().getAttributeValue(VOTableVO.BILLVO)
					.toString().trim());
			return (AggregatedValueObject) voClass.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
