package org.nw.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.dao.PaginationVO;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.QueryConditionVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiPrintTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.jf.vo.UiReportTempletVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.FunVO;
import org.nw.vo.sys.RefInfoVO;
import org.nw.web.utils.WebUtils;

/**
 * 所有service的接口,定义了service的一些公共的方法
 * 
 * @author xuqc
 * @date 2012-6-10 下午04:26:42
 */
public interface IToftService {

	/**
	 * 返回单据类型信息
	 * 
	 * @return
	 */
	public abstract AggregatedValueObject getBillInfo();

	/**
	 * 根据code查询billId（主表pk）,对于单据，code就是vbillno
	 * 
	 * @param code
	 * @return
	 */
	public String getPKByCode(String code);

	/**
	 * 根据主键查询
	 * 
	 * @param primaryKey
	 * @return
	 */
	public <T extends SuperVO> T getByPrimaryKey(Class<? extends SuperVO> clazz, String primaryKey);

	/**
	 * 根据主表的id，返回整张单据的vo,包含其他的参数，如表体是否翻页等
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject queryBillVO(ParamVO paramVO);

	/**
	 * 更新保存SuperVO
	 * 
	 * @param superVO
	 * @return
	 */
	public int saveOrUpdate(SuperVO superVO) throws Exception;

	/**
	 * 更新保存SuperVO
	 * 
	 * @param superVOs
	 * @return
	 */
	public int saveOrUpdate(List<SuperVO> superVOs) throws Exception;

	/**
	 * 添加一个对象
	 * 
	 * @param superVO
	 * @return
	 */
	public int addSuperVO(SuperVO superVO);

	/**
	 * 根据主键删除对象,逻辑删除,将dr设置为1
	 * 
	 * @param primaryKey
	 * @return
	 */
	public int deleteByPrimaryKey(Class<? extends SuperVO> clazz, String primaryKey) throws Exception;

	/**
	 * 批量删除，在一个事务中
	 * 
	 * @param clazz
	 * @param primaryKeys
	 * @return
	 */
	public int batchDelete(Class<? extends SuperVO> clazz, String[] primaryKeys) throws Exception;

	/**
	 * 根据主键删除对象,逻辑删除,将dr设置为1
	 * 
	 * @param superVO
	 * @return
	 */
	public int deleteSuperVO(SuperVO superVO) throws Exception;

	/**
	 * 根据主键更新对象
	 * 
	 * @param superVO
	 * @return
	 */
	public int updateByPrimaryKey(SuperVO superVO);

	/**
	 * 返回默认值
	 * 
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> getDefaultValue(ParamVO paramVO);

	/**
	 * 返回单据模板ID,当前可以直接返回一个ID值，以后可以做一个简单的分配页面 FIXME
	 * 
	 * @return
	 */
	public String getBillTemplateID(ParamVO paramVO);

	/**
	 * 返回查询模板ID，当前可以直接返回一个ID值，以后可以做一个简单的分配页面 FIXME
	 * 
	 * @return
	 */
	public String getQueryTemplateID(ParamVO paramVO);

	/**
	 * 返回单据模板VO
	 * 
	 * @param templateID
	 * @return
	 */
	public UiBillTempletVO getBillTempletVO(String templateID);

	/**
	 * 某些单据需要读取另外一个功能节点的单据模板数据
	 * 
	 * @param paramVO
	 * @return
	 */
	public UiBillTempletVO getBillTempletVOByFunCode(ParamVO paramVO);

	/**
	 * 根据模板编码返回模板信息
	 * 
	 * @param pk_billtypecode
	 * @return
	 */
	public UiBillTempletVO getBillTempletVOByBilltypecode(String pk_billtypecode);

	/**
	 * 返回查询模板VO
	 * 
	 * @param templateID
	 * @return
	 */
	public UiQueryTempletVO getQueryTempletVO(String templateID);

	/**
	 * 返回报表模板ID
	 * 
	 * @param paramVO
	 * @return
	 */
	public String getReportTempletID(ParamVO paramVO);

	/**
	 * 返回报表模板VO
	 * 
	 * @param templateID
	 * @return
	 */
	public UiReportTempletVO getReportTempletVO(String templateID);

	/**
	 * 返回打印模板ID
	 * 
	 * @param paramVO
	 * @return
	 */
	public String getPrintTempletID(ParamVO paramVO);

	/**
	 * 返回打印模板VO
	 * 
	 * @param templateID
	 * @return
	 */
	public UiPrintTempletVO getPrintTempletVO(String templateID);

	/**
	 * 处理表头的编码，如果是档案类型的那么就是编码，如果是单据类型，则就是设置单据号
	 * 
	 * @param parentVO
	 * @param paramVO
	 */
	public void setCodeField(CircularlyAccessibleValueObject parentVO, ParamVO paramVO);

	/**
	 * 返回code字段的字段名，目前用于设置编码的默认值，编码的默认值一般使用规则生成，对于单据类型，就是单据号
	 * 
	 * @return
	 */
	public String getCodeFieldCode();

	/**
	 * 根据code返回vo，在校验code唯一性时需要调用
	 * 
	 * @return
	 */
	public SuperVO getByCode(String code);

	/**
	 * 根据code返回vo，在校验code唯一性时需要调用
	 * 
	 * @return
	 */
	public SuperVO getByCodeWithNoDr(String code);

	/**
	 * 模式化页面的保存方法,页面发送json数据,这里进行解析,保存
	 * 
	 * @param json
	 */
	public AggregatedValueObject save(AggregatedValueObject billVO, ParamVO paramVO);

	/**
	 * 显示一张单据
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject show(ParamVO paramVO);

	/**
	 * 表格加载数据
	 * 
	 * @param paramVO
	 * @param offset
	 * @param pageSize
	 * @param pks
	 * @param orderBy
	 * @param extendCond
	 * @param values
	 * @return
	 */
	public PaginationVO loadData(ParamVO paramVO, int offset, int pageSize, String[] pks, String orderBy,
			String extendCond, Object... values);

	/**
	 * 复制单据
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject copy(ParamVO paramVO);

	/**
	 * 这个方法每个service都需要调用
	 * 
	 * @param fun_code
	 * @return
	 */
	public FunVO getFunVOByFunCode(String fun_code);

	/**
	 * 执行模板中某个字段的编辑公式
	 * 
	 * @param billTempletBVO
	 * @param context
	 * @return
	 * @author xuqc
	 * @date 2012-2-10
	 */
	public Map<String, Object> execEditFormula(BillTempletBVO billTempletBVO, Map<String, Object> context);

	/**
	 * 导出数据，根据条件查询后导出
	 * 
	 * @param paramVO
	 * @param offset
	 * @param pageSize
	 * @param orderBy
	 * @param extendCond
	 * @param values
	 * @return
	 */
	public HSSFWorkbook export(ParamVO paramVO, int offset, int pageSize, String orderBy, String extendCond,
			Object... values);

	/**
	 * 
	 * 显示上一张单据,如果是代办事项页面，则显示上一条代办事项
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject doPrev(ParamVO paramVO, String orderBy, String extendCond, Object... values);

	/**
	 * 显示下一张单据,如果是代办事项页面，则显示下一条代办事项
	 * 
	 * @param paramVO
	 * @return
	 */
	public AggregatedValueObject doNext(ParamVO paramVO, String orderBy, String extendCond, Object... values);

	/**
	 * 
	 * @param datas
	 * @param paramVO
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<List<Object>> processULWData(List datas, ParamVO paramVO);

	/**
	 * 
	 * @param data
	 * @param paramVO
	 * @return
	 */
	public List<Object> processULWData(Object data, ParamVO paramVO);

	/**
	 * 返回ulw页面需要的字段,这些字段可能在模板中配置成隐藏的,但是页面上是必须的
	 * 
	 * @param paramVO
	 * @return
	 */
	public List<String> getULWImmobilityFields(ParamVO paramVO);

	/**
	 * 返回打印时所需要的参数,该参数在jasperreport设计器中定义，这里是一张单据的打印
	 * 
	 * @param request
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> getBillPrintParameterMap(HttpServletRequest request, ParamVO paramVO);

	/**
	 * 返回打印模板的数据源，这里是一张单据的打印
	 * 
	 * @param paramVO
	 * @param uiBillTempletVO
	 * @param uiPrintTempletVO
	 * @return
	 */
	public List<Map<String, Object>> getBillPrintDataSource(ParamVO paramVO, UiBillTempletVO uiBillTempletVO,
			UiPrintTempletVO uiPrintTempletVO);

	/**
	 * 返回打印时所需要的参数,该参数在jasperreport设计器中定义，这里是打印整个表头
	 * 
	 * @param request
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> getReportPrintParameterMap(HttpServletRequest request, ParamVO paramVO);

	/**
	 * 返回打印模板的数据源，这里是一张单据的打印，这里是打印整个表头
	 * 
	 * @param paramVO
	 * @param uiReportTempletVO
	 * @param uiPrintTempletVO
	 * @return
	 */
	public List<Map<String, Object>> getReportPrintDataSource(ParamVO paramVO, UiReportTempletVO uiReportTempletVO,
			UiPrintTempletVO uiPrintTempletVO);

	/**
	 * 返回锁定条件
	 * 
	 * @param paramVO
	 * @return
	 */
	public List<Object> getLockingItemAry(ParamVO paramVO);

	/**
	 * 返回锁定的条件，锁定的条件在模板中是以if_desc字段来定义的，锁定条件同时必须是使用的，默认的条件
	 * 
	 * @param paramVO
	 * @return
	 */
	public List<QueryConditionVO> getLockingConds(ParamVO paramVO);

	/**
	 * 返回nw_refinfo中定义的web参照
	 * 
	 * @return
	 */
	public List<RefInfoVO> loadSystemRef();

	/**
	 * 导入
	 * 
	 * @param paramVO
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public String doImport(ParamVO paramVO, File file, String pk_import_config) throws Exception;

	/**
	 * 执行模板中的公式
	 * 
	 * @param billVO
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> execFormula4Templet(AggregatedValueObject billVO, ParamVO paramVO);
	
	/**
	 * 聚合VO执行公式，返回格式如：<br/>
	 * {HEADER:{},BODY:{TABNAME1:[],TABNAME2:[]}}
	 * 
	 * 
	 * @param billVO  单据VO
	 * @param paramVO 参数
	 * @param execHead 是否执行表头
	 * @param execBody 是否执行表体
	 * @return
	 */
	public Map<String, Object> execFormula4Templet(AggregatedValueObject billVO, ParamVO paramVO, boolean execHead,
			boolean execBody);
	
	/**
	 * 执行完公式的后续操作
	 * @param result
	 */
	public void processAfterExecFormula(Map<String, Object> result);
	
	/**
	 * 只处理parentVO的模板，用在列表界面显示
	 * @param paramVO
	 * @param superVOs
	 * @return
	 */
	public List<Map<String, Object>> execFormula4Templet(ParamVO paramVO, List<SuperVO> superVOs);
	
	/**
	 * 给单据使用的公式执行（自动合并model到公式结果）<br>
	 * 注：该方法专门为单据模板相关模块调用
	 * 
	 * @param context
	 * @param templetId
	 * @param tabCodes
	 *            若是表头，此时tabCodes使用,分隔，若是表体，肯定只传一个，多表体是分开加载数据的(表头表尾可直接传null)
	 * @param extFormulas
	 * @return
	 */
	public List<Map<String, Object>> execFormula4Templet(List<Map<String, Object>> context,
			UiBillTempletVO uiBillTempletVO, boolean isBody, String[] tabCodes, String[] extFormulas);

}
