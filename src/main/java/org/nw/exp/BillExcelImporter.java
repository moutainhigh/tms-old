package org.nw.exp;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.nw.BillStatus;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.NWDao;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.sys.ImportConfigBVO;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.WebUtils;

/**
 * excel导入器，用于导入包括主子表的单据或者档案,目前只支持一个子表
 * 
 * @author xuqc
 * @date 2014-4-8 下午09:41:03
 */
public class BillExcelImporter extends ExcelImporter {

	public BillExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service,configVO);
	}

	public BillExcelImporter(ParamVO paramVO, IToftService service,ImportConfigVO configVO) {
		super(paramVO, service,configVO);
	}

	public static final String VBILLNO = "vbillno";
	public static final String VBILLSTATUS = "vbillstatus";

	/**
	 * 返回要导入的子表的VO实例,对于多子表，这里只返回第一个子表
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected SuperVO getChildVO() {
		try {
			CircularlyAccessibleValueObject[] childrenVOTableArray = billInfo.getChildrenVO();
			if(childrenVOTableArray == null || childrenVOTableArray.length == 0){
				return null;
			}
			Class<? extends SuperVO> childrenVOClass = (Class<? extends SuperVO>) Class.forName(childrenVOTableArray[0]
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			return childrenVOClass.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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

	/**
	 * 返回要导入的子表的VO实例,对于多子表，这里只返回第一个子表
	 * 
	 * @return
	 */
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

	/**
	 * 返回主表在子表中的主键字段
	 * 
	 * @return
	 */
	protected String getParentPkInChild() {
		CircularlyAccessibleValueObject[] childrenVOTableArray = billInfo.getChildrenVO();
		return childrenVOTableArray[0].getAttributeValue(VOTableVO.PKFIELD).toString().trim();
	}

	/**
	 * 导入一个sheet的数据，返回vo集合
	 * 
	 * @param sheet
	 * @param itemMap
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
			SuperVO parentVO = getParentVO();// 表头的VO
			SuperVO childVO = getChildVO();// 表体的VO
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
				String fieldCode = importConfigBVO.getField_code();// 导入字段中定义的字段编码，可能是表头，也可能是表体
				BillTempletBVO fieldVO = itemMap.get(fieldCode);// 读取对应的模板的字段配置VO
				int index = fieldCode.indexOf(".");
				if(index != -1) {
					// 表体的字段
					// String tabcode = fieldCode.substring(0, index);// 所在的页签编码
					fieldCode = fieldCode.substring(index + 1);// 这个才是模板中的字段名称，即itemkey,使用点号说明是表体的字段
					superVO = childVO;
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
				setDefaultValue(parentVO, childVO, i);
				processAfterResolveOneRow(parentVO, childVO, aggVOs, i + 1);
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

	
	
	// 为每一行设置默认值
	public void setDefaultValue(SuperVO parentVO, SuperVO childVO, int rowNum) {
	}

	/**
	 * 识别一行数据后，对VO进行处理,对于单据，这一行数据包括表头和表体的vo，但是需要处理这个可能需要合并到其他行数据
	 * 
	 * @param parentVO
	 * @param childVO
	 * @param aggVOs
	 * @param rowNum
	 */
	public void processAfterResolveOneRow(SuperVO parentVO, SuperVO childVO, List<AggregatedValueObject> aggVOs,
			int rowNum) {
		Object oVbillno = parentVO.getAttributeValue(VBILLNO);
		AggregatedValueObject aggVO = null;
		SuperVO currentVO = null;
		if(oVbillno != null) {// 存在单据号,检查当前是否已经存在该单据号了
			String vbillno = oVbillno.toString();
			for(AggregatedValueObject billVO : aggVOs) {
				SuperVO superVO = (SuperVO) billVO.getParentVO();
				if(vbillno.equals(superVO.getAttributeValue(VBILLNO).toString())) {
					aggVO = billVO;
					currentVO = superVO;
					break;
				}
			}
		}
		if(aggVO != null) {
			// 导入的数据中已经包含了单据号，而且单据号已经存在，那么这里只使用其表体的数据
			// 对导入的这一行数据的表头和表体进行关联处理
			childVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(childVO);
			childVO.setAttributeValue(getParentPkInChild(), currentVO.getPrimaryKey());
			CircularlyAccessibleValueObject[] childVOs = null;
			if(aggVO instanceof IExAggVO) {
				// 多子表
				childVOs = ((IExAggVO) aggVO).getTableVO(childVO.getTableName());// table_name务必和tabcode一致
			} else {
				childVOs = aggVO.getChildrenVO();
			}
			if(childVOs != null && childVOs.length > 0) {
				CircularlyAccessibleValueObject[] newChildVOs = (CircularlyAccessibleValueObject[]) Array.newInstance(
						childVOs[0].getClass(), childVOs.length + 1);
				for(int i = 0; i < childVOs.length; i++) {
					newChildVOs[i] = childVOs[i];
				}
				newChildVOs[newChildVOs.length - 1] = childVO;
				if(aggVO instanceof IExAggVO) {
					// 多子表
					((IExAggVO) aggVO).setTableVO(childVO.getTableName(), newChildVOs);
				} else {
					aggVO.setChildrenVO(newChildVOs);
				}
			}
		} else {
			AggregatedValueObject billVO = getAggVO();
			parentVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(parentVO);
			billVO.setParentVO(parentVO);

			// 对导入的这一行数据的表头和表体进行关联处理
			if(childVO != null){
				childVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(childVO);
				childVO.setAttributeValue(getParentPkInChild(), parentVO.getPrimaryKey());
				CircularlyAccessibleValueObject[] vos = (CircularlyAccessibleValueObject[]) Array.newInstance(
						childVO.getClass(), 1);
				vos[0] = childVO;
				if(billVO instanceof IExAggVO) {
					// 多子表
					((IExAggVO) billVO).setTableVO(childVO.getTableName(), vos);
				} else {
					billVO.setChildrenVO(vos);
				}
			}
			aggVOs.add(billVO);
		}
	}
	
	
	
	public void processAfterResolveOneRowForMultiTable(SuperVO parentVO, List<SuperVO> childrenVO , List<AggregatedValueObject> aggVOs,
			int rowNum) {
		Object oVbillno = parentVO.getAttributeValue(VBILLNO);
		AggregatedValueObject aggVO = null;
		SuperVO currentVO = null;
		if(oVbillno != null) {// 存在单据号,检查当前是否已经存在该单据号了
			String vbillno = oVbillno.toString();
			for(AggregatedValueObject billVO : aggVOs) {
				SuperVO superVO = (SuperVO) billVO.getParentVO();
				if(vbillno.equals(superVO.getAttributeValue(VBILLNO).toString())) {
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
			billVO.setParentVO(parentVO);
			for(SuperVO childVO : childrenVO){
				parentVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(parentVO);
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
}
