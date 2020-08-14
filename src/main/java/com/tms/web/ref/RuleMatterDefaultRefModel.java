package com.tms.web.ref;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.nw.basic.util.ReflectionUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.AbstractGridRefModel;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.sys.DeptVO;
import org.nw.vo.sys.PsndocVO;
import org.nw.web.utils.ControllerHelper;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.constants.DataDictConst;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CustomerVO;
import com.tms.vo.base.GoodsTypeVO;
import com.tms.vo.base.TransTypeVO;

/**
 * 
 * 
 * @author xuqc
 * @date 2012-7-2 下午06:03:15
 */
@Controller
@RequestMapping(value = "/ref/common/bbb")
public class RuleMatterDefaultRefModel extends AbstractGridRefModel {
	@JsonProperty
	protected Boolean isMulti = true; // 是否多选
	@JsonProperty
	protected Boolean showCodeOnBlur = false;

	private static String cust_sql = "select pk_customer as pk_bbb,cust_code as code,cust_name as name,memo from ts_customer WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
	private static String psndoc_sql = "select pk_psndoc as pk_bbb,psncode as code,psnname as name,photo as memo from nw_psndoc WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
	private static String dept_sql = "select pk_dept as pk_bbb,dept_code as code,dept_name as name,dept_name as memo from nw_dept WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
	private static String addr_sql = "select pk_address as pk_bbb,addr_code as code,addr_name as name,memo from ts_address WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
	private static String goods_type_sql = "select pk_goods_type as pk_bbb,code as code,name as name,memo from ts_goods_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
	private static String trans_type_sql = "select pk_trans_type as pk_bbb,code as code,name as name,memo from ts_trans_type WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' ";
	private static String area_sql = "select pk_area as pk_bbb,code as code,name as name,memo from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and area_level=3 ";
	private static String priv_sql = "select pk_area as pk_bbb,code as code,name as name,memo from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and area_level=4 ";
	private static String city_sql = "select pk_area as pk_bbb,code as code,name as name,memo from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and (area_level=5 or area_level=6) ";
	private static final long serialVersionUID = -4697550166289979740L;

	protected String[] getFieldCode() {
		return new String[] { "code", "name", "memo" };
	}

	protected String[] getFieldName() {
		return new String[] { "编码", "名称", "备注" };
	}

	@SuppressWarnings({ })
	public Map<String, Object> load4Grid(HttpServletRequest request) {
		int offset = getOffset(request);
		int pageSize = getPageSize(request);
		StringBuffer cond = new StringBuffer("1=1");
		String whereClause = this.getExtendCond(request);
		String sql = null;
		if(StringUtils.isNotBlank(whereClause)) {
			cond.append(" and ");
			cond.append(whereClause);
		}
		String matter_type = request.getParameter("matter_type"); 
		PaginationVO page = null;
		if(StringUtils.isBlank(matter_type)) {
			sql = cust_sql + " and " +" 1=2 ";
			page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
		}else{
			if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.CUST.intValue() + "")) {
				String condition = getGridQueryCondition(request, CustomerVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = cust_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.PSNDOC.intValue() + "")) {
				String condition = getGridQueryCondition(request, PsndocVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = psndoc_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DEPT.intValue() + "")) {
				String condition = getGridQueryCondition(request, DeptVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = dept_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.IF_RETURN.intValue() + "")) {
				sql = "SELECT 'Y' AS pk_bbb,'Y' AS code ,'Y' AS name UNION ALL SELECT 'N' AS pk_bbb,'N' AS code ,'N' AS name";
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.IF_CUST.intValue() + "")) {
				sql = "SELECT 'Y' AS pk_bbb,'Y' AS code ,'Y' AS name UNION ALL SELECT 'N' AS pk_bbb,'N' AS code ,'N' AS name";
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.URGENT_LEVEL.intValue() + "")) {
				sql = "SELECT nw_data_dict_b.value AS pk_bbb,nw_data_dict_b.display_name AS code, nw_data_dict_b.display_name AS name "
						+ " FROM nw_data_dict_b LEFT JOIN nw_data_dict ON "
						+ " nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
						+ " WHERE nw_data_dict.datatype_code ='urgent_level' ";
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.INV_GOODS_TYPE.intValue() + "")) {
				sql = "SELECT nw_data_dict_b.value AS pk_bbb,nw_data_dict_b.display_name AS code, nw_data_dict_b.display_name AS name "
						+ " FROM nw_data_dict_b LEFT JOIN nw_data_dict ON "
						+ " nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
						+ " WHERE nw_data_dict.datatype_code ='invoice_goods_type' ";
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI.intValue() + "")) {
				String condition = getGridQueryCondition(request, AddressVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = addr_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI.intValue() + "")) {
				String condition = getGridQueryCondition(request, AddressVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = addr_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.GOODS_TYPE.intValue() + "")) {
				String condition = getGridQueryCondition(request, GoodsTypeVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = goods_type_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.TRANS_TYPE.intValue() + "")) {
				String condition = getGridQueryCondition(request, TransTypeVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = trans_type_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_CITY.intValue() + "")) {
				String condition = getGridQueryCondition(request, AreaVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = city_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_P.intValue() + "")) {
				String condition = getGridQueryCondition(request, AreaVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = priv_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_A.intValue() + "")) {
				String condition = getGridQueryCondition(request, AreaVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = area_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_CITY.intValue() + "")) {
				String condition = getGridQueryCondition(request, AreaVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = city_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_P.intValue() + "")) {
				String condition = getGridQueryCondition(request, AreaVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
				}
				sql = priv_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_A.intValue() + "")) {
				String condition = getGridQueryCondition(request, AreaVO.class);
				if(StringUtils.isNotBlank(condition)) {
					cond.append(" and ");
					cond.append(condition);
					cond.append(" and " + CorpHelper.getCurrentCorpWithChildrenAndGroup());
				}
				sql = area_sql + " and " + cond.toString();
				page = baseRefService.queryBySqlForPagination(HashMap.class, sql, offset, pageSize);
			}
		}

		
		return this.genAjaxResponse(true, null, page);
	}

	protected String getGridQueryCondition(HttpServletRequest request, Class<?> clazz) {
		String matter_type = request.getParameter("matter_type"); 
		if(StringUtils.isBlank(matter_type)) {
			matter_type = DataDictConst.MERGE_RULE_PARAMTER_TYPE.CUST.intValue() + ""; // 默认是客户
		}
		String fields[] = null;
		if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.CUST.intValue() + "")) {
			fields = new String[] { "cust_code", "cust_name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.PSNDOC.intValue() + "")){
			fields = new String[] { "psncode", "psn", "photo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DEPT.intValue() + "")){
			fields = new String[] { "dept_code", "dept_name", "dept_name" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI.intValue() + "")){
			fields = new String[] { "addr_code", "addr_name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI.intValue() + "")){
			fields = new String[] { "addr_code", "addr_name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.GOODS_TYPE.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.TRANS_TYPE.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_CITY.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_P.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_A.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_CITY.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_P.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_A.intValue() + "")){
			fields = new String[] { "code", "name", "memo" };
		}

		String keyword = ControllerHelper.getGridQueryKeyword(request);

		StringBuffer sb = new StringBuffer("(");
		if(fields == null || fields.length == 0 || StringUtils.isBlank(keyword))
			return "";
		if(clazz == null) {
			// 不进行field校验，直接拼装
			// 根据fieldsType判断参数类型
			String[] fieldsType = ControllerHelper.getGridQueryFieldsType(request);
			// 字段类型全部使用string类型
			for(int i = 0; i < fields.length; i++) {
				if(i > 0)
					sb.append(" or ");
				sb.append(fields[i]);
				if("int".equalsIgnoreCase(fieldsType[i]) || "float".equalsIgnoreCase(fieldsType[i])) {
					// 数字型
					sb.append("=");
					sb.append(keyword);
				} else {
					sb.append(" like '%"); // 加入完全模糊查询
					sb.append(keyword);
					sb.append("%'");
				}
			}
		} else {
			for(int i = 0; i < fields.length; i++) {
				// 处理xtsOrg.orgName的情况
				Field field;
				if(fields[i].indexOf(".") > 0) {
					String temp = fields[i].substring(0, fields[i].indexOf("."));
					Field nestedField = ReflectionUtils.findField(clazz, temp);
					String f = fields[i].substring(fields[i].indexOf(".") + 1, fields[i].length());
					field = ReflectionUtils.findField(nestedField.getType(), f);
				} else
					field = ReflectionUtils.findField(clazz, fields[i]);
				if(field == null)
					continue;
				if(Number.class.isAssignableFrom(field.getType())) { // number类型
					int value;
					try {
						value = Integer.parseInt(keyword);
					} catch(Exception e) {
						continue;
					}
					if(i > 0 && sb.length() > 1)
						sb.append(" or ");
					sb.append(fields[i]);
					sb.append(" = ");
					sb.append(value);
				} else {
					if(i > 0 && sb.length() > 1)
						sb.append(" or ");
					sb.append("( ");
					String[] keywordAry = keyword.split(" "); // 如果keyword包括空格，那么作为多个查询条件
					for(int j = 0; j < keywordAry.length; j++) {
						if(StringUtils.isNotBlank(keywordAry[j])) {
							sb.append(fields[i]);
							sb.append(" like '%");// 使用完全模糊匹配
							sb.append(keywordAry[j]);
							sb.append("%'");
							if(j < keywordAry.length - 1) {
								sb.append(" or ");
							}
						}
					}
					sb.append(" )");
				}
			}
		}
		sb.append(")");
		if(sb.length() <= 2) {// 没有查询条件，只有()
			return null;
		}
		return sb.toString();
	}

	public String getPkFieldCode() {
		return "pk_bbb";
	}

	public String getCodeFieldCode() {
		return "code";
	}

	public String getNameFieldCode() {
		return "name";
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Object> getByCode(String code) {
		String matter_type = ServletContextHolder.getRequest().getParameter("matter_type"); 
		if(StringUtils.isBlank(matter_type)) {
			String sql = cust_sql + " and (cust_code=? or cust_name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
			HashMap map = NWDao.getInstance().queryForObject(sql, HashMap.class, code, code);
			return this.genAjaxResponse(true, null, map);
		} else {
			if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.CUST.intValue() + "")) {
				String sql = cust_sql + " and (cust_code=? or cust_name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.PSNDOC.intValue() + "")) {
				String sql = psndoc_sql + " and (psncode=? or psnname=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DEPT.intValue() + "")) {
				String sql = dept_sql + " and (dept_code=? or dept_name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI.intValue() + "")) {
				String sql = addr_sql + " and (addr_code=? or addr_name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI.intValue() + "")) {
				String sql = addr_sql + " and (addr_code=? or addr_name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.GOODS_TYPE.intValue() + "")) {
				String sql = goods_type_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.TRANS_TYPE.intValue() + "")) {
				String sql = trans_type_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_CITY.intValue() + "")) {
				String sql = city_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_P.intValue() + "")) {
				String sql = priv_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_A.intValue() + "")) {
				String sql = area_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_CITY.intValue() + "")) {
				String sql = city_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_P.intValue() + "")) {
				String sql = priv_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_A.intValue() + "")) {
				String sql = area_sql + " and (code=? or name=?) and " + CorpHelper.getCurrentCorpWithChildrenAndGroup();
				return this.genAjaxResponse(true, null,
						NWDao.getInstance().queryForObject(sql, HashMap.class, code, code));
			}
		}
		return this.genAjaxResponse(true, null, null);
	}

	public Map<String, Object> getByPk(String pk) {
		String matter_type = ServletContextHolder.getRequest().getParameter("matter_type");
		getByPk(matter_type, pk);
		return null;
	}

	public Map<String, Object> getByPk(String matter_type, String pk) {
		String pkCond = NWUtils.buildConditionString(pk.split(","));
		String sql = "";
		if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.CUST.intValue() + "")) {
			sql = cust_sql + " and pk_customer in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.PSNDOC.intValue() + "")) {
			sql = psndoc_sql + " and pk_psndoc in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DEPT.intValue() + "")) {
			sql = dept_sql + " and pk_dept in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI.intValue() + "")) {
			sql = addr_sql + " and pk_address in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI.intValue() + "")) {
			sql = addr_sql + " and  pk_address in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.GOODS_TYPE.intValue() + "")) {
			sql = goods_type_sql + " and pk_goods_type in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.TRANS_TYPE.intValue() + "")) {
			sql = trans_type_sql + " and pk_trans_type in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_CITY.intValue() + "")) {
			sql = city_sql + " and pk_area in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_P.intValue() + "")) {
			sql = priv_sql + " and pk_area in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.DELI_A.intValue() + "")) {
			sql = area_sql + " and pk_area in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_CITY.intValue() + "")) {
			sql = city_sql + " and pk_area in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_P.intValue() + "")) {
			sql = priv_sql + " and pk_area in " + pkCond;
		} else if(matter_type.equals(DataDictConst.MERGE_RULE_PARAMTER_TYPE.ARRI_A.intValue() + "")) {
			sql = area_sql + " and pk_area in " + pkCond;
		}
		Map<String,Object> map = new HashMap<String, Object>();
		if(StringUtils.isNotBlank(sql)){
			List<Map<String,Object>> results = NWDao.getInstance().queryForList(sql);
			if(results != null && results.size() > 0){
				String pk_bbb = "";
				String code = "";
				String name = "";
				String memo = "";
				for(Map<String,Object> result : results){
					pk_bbb = pk_bbb + result.get("pk_bbb").toString() +",";
					code = code + result.get("code").toString() +",";
					if(result.get("name") != null){
						name  = name + result.get("name").toString() +",";
					}
					if(result.get("memo") != null){
						memo  = memo + result.get("memo").toString() +",";
					}
				}
				if(StringUtils.isNotBlank(pk_bbb)){
					map.put("pk_bbb", pk_bbb.substring(0,pk_bbb.length()-1));
				}
				if(StringUtils.isNotBlank(code)){
					map.put("code", code.substring(0,code.length()-1));
				}
				if(StringUtils.isNotBlank(name)){
					map.put("name", name.substring(0,name.length()-1));
				}
				if(StringUtils.isNotBlank(memo)){
					map.put("memo", memo.substring(0,memo.length()-1));
				}
			}
			return this.genAjaxResponse(true, null,map);
		}else{
			return this.genAjaxResponse(true, null, null);
		}
		
	}

}
