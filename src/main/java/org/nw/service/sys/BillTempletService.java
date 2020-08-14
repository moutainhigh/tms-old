package org.nw.service.sys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.jf.vo.BillTempletVO;
import org.nw.service.IToftService;
import org.nw.vo.pub.AggregatedValueObject;

/**
 * 模板初始化
 * 
 * @author xuqc
 * @date 2012-11-21 下午07:39:30
 */
public interface BillTempletService extends IToftService {

	/**
	 * 查询用户表，根据表名称进行过滤
	 * 
	 * @param keyword
	 * @return
	 */
	public List<String> selectUserTable(String keyword);

	/**
	 * 根据单据类型编码查询所有模板，包括子表数据
	 * 
	 * @param pk_billtypecode
	 * @return
	 */
	public List<HashMap> loadTemplet(String pk_billtypecode);

	/**
	 * 返回单据模板中的所有tab
	 * 
	 * @param pk_billtemplet
	 * @return
	 */
	public Map<String, Object> loadTempletTab(String pk_billtemplet);

	/**
	 * 根据表名返回表的所有字段
	 * 
	 * @param tableName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List<HashMap> loadTableFields(String tableName);

	/**
	 * 返回模板的描述信息，格式化后的信息
	 * 
	 * @param pk_billtemplet
	 * @return
	 */
	public Map<String, Object> loadTempletDesc(String pk_billtemplet);

	/**
	 * 根据单据类型编码，返回对应的所有模板信息
	 * 
	 * @param pk_billtypecode
	 * @return
	 */
	public Map<String, Object> loadTempletData(String pk_billtypecode);

	/**
	 * 删除单据模板
	 * 
	 * @param pk_billtemplet
	 */
	public void deleteBillTemplet(String pk_billtemplet);

	/**
	 * 保存单据模板
	 */
	public Map<String, Object> saveBillTemplet(AggregatedValueObject billVO);

	/**
	 * 复制模板
	 * 
	 * @param pk_billtemplet
	 *            源模板
	 * @param bill_templetcaption
	 *            模板的名称，web使用这个字段作为模板名称，而不使用bill_templetname
	 * @return
	 */
	public BillTempletVO copyBillTemplet(String pk_billtemplet, String bill_templetcaption, String nodecode);

	/**
	 * 从单据模板中自动生成查询模板
	 * 
	 * @param pk_billtemplet
	 * @param model_name
	 * @param node_code
	 * @param bCover
	 *            是否覆盖
	 */
	public void buildQueryTemplet(String pk_billtemplet, String model_name, String node_code, boolean bCover);

	/**
	 * 从单据模板中自动生成报表模板
	 * 
	 * @param pk_billtemplet
	 * @param vtemplatename
	 * @param nodecode
	 * @param bCover
	 *            是否覆盖
	 */
	public void buildReportTemplet(String pk_billtemplet, String vtemplatename, String nodecode, boolean bCover);
}
