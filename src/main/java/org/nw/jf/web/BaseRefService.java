package org.nw.jf.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.basic.util.SecurityUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.utils.FormulaHelper;
import org.nw.vo.pub.SuperVO;

/**
 * 参照服务基类<br>
 * 继承自AbstractBaseService
 * 暂时注释掉@service，采用直接new类的实例，也可以修改为用getInstance获取单例实例，service注解暂时去除--fangw.
 * 
 * @author xuqc
 * @date 2011-1-24
 */
public class BaseRefService {
	private static final Log logger = LogFactory.getLog(BaseRefService.class);
	private BaseRefModel refModel;
	private boolean queryByCodeWithMnecode = true;

	public static final int REF_GRID_PAGESIZE = 10;

	public final boolean isQueryByCodeWithMnecode() {
		return queryByCodeWithMnecode;
	}

	public final void setQueryByCodeWithMnecode(boolean queryByCodeWithMnecode) {
		this.queryByCodeWithMnecode = queryByCodeWithMnecode;
	}

	public BaseRefService(BaseRefModel refModel) {
		this.refModel = refModel;
	}

	/**
	 * 分页查询，返回分页对象 已自动加入dr=0的条件
	 * 
	 * @param voClass
	 * @param condition
	 * @param offset
	 * @param pageSize
	 * @param params
	 * @return
	 */
	public PaginationVO queryForPagination(Class<? extends SuperVO> voClass, int offset, int pageSize,
			String condition, Object... params) {
		PaginationVO page = NWDao.getInstance()
				.queryByConditionWithPaging(voClass, offset, pageSize, condition, params);
		return page;
	}

	/**
	 * 分页查询，返回分页对象
	 * 
	 * @param voClass
	 * @param sql
	 * @param offset
	 * @param pageSize
	 * @param params
	 * @return
	 */
	public PaginationVO queryBySqlForPagination(Class<?> voClass, String sql, int offset, int pageSize,
			Object... params) {
		PaginationVO page = NWDao.getInstance().queryBySqlWithPaging(sql, voClass, offset, pageSize, params);
		return page;
	}

	/**
	 * 不包括额外条件的查询 ，这中情况必须保证code是唯一的<br/>
	 * XXX 这里返回Object，因为可能返回的是map，或者list，或者superVO
	 * 
	 * 
	 * @param <T>
	 * @param voClass
	 * @param code
	 * @return
	 * @author xuqc
	 * @date 2011-10-20
	 */
	public Object getVOByCode(Class<? extends SuperVO> voClass, String code) {
		return getVOByCode(voClass, code, null);
	}

	/**
	 * 根据code获取vo
	 * 自动加入dr条件，若不维护dr字段，则在controller中使用queryForSuperVOByWhereClauseWithCache
	 * 可能会返回多条记录，以list返回
	 * 
	 * @param <T>
	 * @param voClass
	 * @param code
	 * @param wherePart
	 *            额外的参数条件，不包括code的条件，code条件已经会自动加入了
	 * @return
	 * @author xuqc
	 * @date 2011-10-20
	 */
	public Object getVOByCode(Class<? extends SuperVO> voClass, String code, String wherePart) {
		try {
			String orginalCond;
			if(refModel.isShowCodeOnFocus() != null && refModel.isShowCodeOnFocus()) {
				orginalCond = refModel.getCodeFieldCode() + "=?";
			} else {
				orginalCond = "(" + refModel.getCodeFieldCode() + "=? or " + refModel.getNameFieldCode() + "='" + code
						+ "')";
			}
			if(StringUtils.isNotBlank(wherePart)) {
				orginalCond += " and " + wherePart;
			}
			StringBuffer cond = new StringBuffer();
			if(this.queryByCodeWithMnecode) {
				// 助记码查询
				String[] mnecode = refModel.getMnecode();
				String[] mencodeSearchType = refModel.getMnecodeSearchType();
				if(mnecode != null && mnecode.length > 0) {
					if(mencodeSearchType == null) {
						mencodeSearchType = new String[mnecode.length];
					}
					cond.append("(");
					// cond += "(";
					for(int i = 0; i < mnecode.length; i++) {
						if(i > 0) {
							// cond += " or ";
							cond.append(" or ");
						}
						if(mencodeSearchType[i] == null) {
							mencodeSearchType[i] = "="; // 默认是等号查询
						}
						cond.append(mnecode[i]);
						cond.append(" ");
						cond.append(mencodeSearchType[i]);
						// cond += mnecode[i] + " " + mencodeSearchType[i];
						if("=".equals(mencodeSearchType[i].trim())) {
							// cond += " '" + code + "'";
							cond.append(" '");
							cond.append(code);
							cond.append("'");
						} else {
							cond.append(" '%");
							cond.append(code);
							cond.append("%'");
							// cond += " '%" + code + "%'";
						}
					}
					// cond += ")";
					cond.append(")");
				}
			}
			if(StringUtils.isNotBlank(cond.toString())) {
				if(StringUtils.isNotBlank(wherePart)) {
					// cond += " and " + wherePart;
					cond.append(" and ");
					cond.append(wherePart);
				}
				SuperVO[] superVOs = NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(voClass,
						cond.toString());
				if(superVOs == null || superVOs.length == 0) {
					return NWDao.getInstance().queryByConditionWithCache(voClass, orginalCond, code);
				} else {
					if(superVOs.length == 1) {
						// 如果只有一条数据，直接返回
						return superVOs[0];
					}
					if(superVOs.length > REF_GRID_PAGESIZE) {
						// 当返回的条数大于50条时，只取前50条，避免数据量太大，这个快速查询就是为了快速定位，本来就不应该返回这么多条。否则就失去了它原来的意义
						SuperVO[] newSuperVOs = new SuperVO[REF_GRID_PAGESIZE];
						for(int i = 0; i < REF_GRID_PAGESIZE; i++) {
							newSuperVOs[i] = superVOs[i];
						}
						superVOs = null;
						return newSuperVOs;
					}
					return superVOs;
				}
			} else {
				return NWDao.getInstance().queryByConditionWithCache(voClass, orginalCond, code);
			}
		} catch(Exception e) {
			// 不再抛出执行期异常
			logger.error(e);
			return null;
		}
	}

	/**
	 * 根据pk获取VO,支持多个pk以逗号分隔
	 * 
	 * @param <T>
	 * @param voClass
	 * @param condition
	 * @param pk
	 * @return
	 */
	public Object getVOByPk(Class<? extends SuperVO> voClass, String pk) {
		try {
			if(pk.indexOf(",") == -1) {
				return NWDao.getInstance().queryByConditionWithCache(voClass, refModel.getPkFieldCode() + "=?", pk);
			} else {
				// pk有多个,并以逗号分隔
				String[] pkAry = pk.split(",");
				StringBuffer sb = new StringBuffer();
				for(String onePK : pkAry) {
					sb.append("'");
					sb.append(onePK);
					sb.append("'");
					sb.append(",");
				}
				String pkStr = sb.toString().substring(0, sb.length() - 1);
				return NWDao.getInstance().queryForSuperVOArrayByConditionWithCache(voClass,
						refModel.getPkFieldCode() + " in (" + pkStr + ")");
			}
		} catch(Exception e) {
			// 不再抛出执行期异常
			logger.error(e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void execFormula(PaginationVO paginationVO, String[] formulas) {
		if(paginationVO == null || formulas == null || formulas.length == 0) {
			return;
		}
		List<?> itemList = paginationVO.getItems();
		if(itemList == null || itemList.size() == 0) {
			return;
		}
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>(itemList.size());
		for(Object item : itemList) {
			Map<String, Object> map = new HashMap<String, Object>();
			if(item instanceof SuperVO) {
				SuperVO vo = (SuperVO) item;
				String[] attrs = vo.getAttributeNames();
				for(String key : attrs) {
					Object obj = vo.getAttributeValue(key);
					if(obj != null) {
						map.put(key, SecurityUtils.escape(obj.toString()));
					} else {
						map.put(key, obj);
					}
				}
			} else {
				// map结构
				map = (Map<String, Object>) item;
				for(String key : map.keySet()) {
					Object obj = map.get(key);
					if(obj != null) {
						obj = SecurityUtils.escape(obj.toString());
					}
				}
			}
			mapList.add(map);
		}

		/**
		 * 6.执行公式
		 */
		List<Map<String, Object>> list = FormulaHelper.execFormula(mapList, formulas, true);
		paginationVO.setItems(list);
	}

	public BaseRefModel getRefModel() {
		return refModel;
	}

	public void setRefModel(BaseRefModel refModel) {
		this.refModel = refModel;
	}
}
