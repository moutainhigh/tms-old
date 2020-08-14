package com.tms.service.wh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.basic.util.SecurityUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.ServiceHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.QueryHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.web.utils.ServletContextHolder;
import org.springframework.stereotype.Service;

import com.tms.constants.BillTypeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.vo.wh.StorageAjustBVO;
import com.tms.vo.wh.StorageAjustVO;

/**
 * 新增调整单，是参照库存生成的
 * 
 * @author xuqc
 * @date 2014-3-30 下午02:08:16
 */
@Service
public class AjustRefService extends TMSAbsBillServiceImpl {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, StorageAjustVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, StorageAjustVO.PK_STORAGE_AJUST);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, StorageAjustBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, StorageAjustBVO.PK_STORAGE_AJUST);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_storage_ajust_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_storage_ajust_b");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String getBillType() {
		return BillTypeConst.AJUST;
	}

	/**
	 * 返回lot表中的字段，查询时使用，包含了前缀
	 * 
	 * @return
	 */
	private String[] getLotFieldAry() {
		return new String[] { "ts_lot.lot_attr1", "ts_lot.lot_attr2", "ts_lot.lot_attr3", "ts_lot.lot_attr4",
				"ts_lot.lot_attr5", "ts_lot.lot_attr6", "ts_lot.lot_attr7", "ts_lot.lot_attr8", "ts_lot.lot_attr9",
				"ts_lot.lot_attr10", "ts_lot.lot_attr11", "ts_lot.produce_date", "ts_lot.expire_date" };
	}

	/**
	 * 库内移动的查询比较特殊，需要关联ts_lot_qty和ts_lot表
	 * 
	 * @return
	 */
	public String getBaseSql() {
		String[] lotAry = getLotFieldAry();
		String s = NWUtils.join(lotAry, ",");
		String sql = "select ts_lot_qty.pk_lot_qty,ts_lot_qty.lot,ts_lot_qty.pk_goods_allocation,ts_lot_qty.lpn,"
				+ "ts_lot_qty.pk_customer,ts_lot_qty.pk_goods,ts_lot_qty.stock_num,ts_lot_qty.available_num,"
				+ "ts_lot_qty.located_num,ts_lot_qty.picked_num,ts_lot_qty.choosed_num,"
				+ "ts_lot_qty.instorage_vbillno," + s + " from ts_lot_qty inner join ts_lot "
				+ "on ts_lot_qty.lot=ts_lot.lot where isnull(ts_lot_qty.dr,0)=0 and isnull(ts_lot.dr,0)=0 ";
		return sql;
	}

	@SuppressWarnings("unchecked")
	public PaginationVO loadData(ParamVO paramVO, int offset, int pageSize, String[] pks, String orderBy,
			String extendCond, Object... values) {
		String params = ServletContextHolder.getRequest().getParameter(Constants.PUB_QUERY_PARAMETER);
		/**
		 * 1.首先获取对应的superVO
		 */
		AggregatedValueObject billInfo = this.getBillInfo();
		Class<? extends SuperVO> voClass = ServiceHelper.getVOClass(billInfo, paramVO);
		UiQueryTempletVO queryTempletVO = this.getQueryTempletVO(this.getQueryTemplateID(paramVO));
		/**
		 * 2.组装查询条件-sql where
		 */
		StringBuilder where = new StringBuilder();
		if(paramVO.isBody()) {
			/**
			 * 2.1.组装前台查询条件
			 */
			String condition = null;
			condition = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
			if(StringUtils.isNotBlank(condition)) {
				if(where.length() > 0) {
					where.append(" and ");
				}
				where.append(condition);
			}

			// FIXME XUQC 2012-03-29 这里如果是单表体的情况，不需要加入这个条件
			if(billInfo.getChildrenVO() != null && billInfo.getChildrenVO().length > 0) {
				if(billInfo.getParentVO() != null) {
					// 如果是子表，则增加主表pk条件
					// String pkFieldName =
					// billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString().trim();
					// String pkValue =
					// ServletContextHolder.getRequest().getParameter(pkFieldName);
					// if(StringUtils.isNotBlank(pkValue)) {
					// if(where.length() > 0) {
					// where.append(" and ");
					// }
					// where.append(getParentPkFieldInChild(paramVO)).append("='");
					// where.append(pkValue);
					// where.append("'");
					// }
				} else {
					// 如果是单表体模式,那么可能需要加入默认条件和锁定条件
					if(StringUtils.isBlank(params)) {
						// 没有经过查询窗口的查询
						String defaultCond = QueryHelper.getDefaultCond(queryTempletVO);
						if(StringUtils.isNotBlank(defaultCond)) {
							if(where.length() > 0) {
								where.append(" and ");
							}
							where.append(defaultCond);
						}
					}
					// 固定条件是必须加上去的，不管前台有没有传入，就算传入了，相同的条件也没关系
					String immobilityCond = QueryHelper.getImmobilityCond(queryTempletVO);
					if(StringUtils.isNotBlank(immobilityCond)) {
						if(where.length() > 0) {
							where.append(" and ");
						}
						where.append(immobilityCond);
					}
				}
			} else {
				// 单表头模式
			}
		} else {
			String billId = ServletContextHolder.getRequest().getParameter("billId");
			if(StringUtils.isNotBlank(billId)) {
				// 如果是主表，但传入了billId（通过待办事务打开的场景）
				if(where.length() > 0) {
					where.append(" and ");
				}
				where.append(getParentPkFieldInChild(paramVO)).append("='");
				where.append(billId);
				where.append("'");// 这里不再组装前台查询条件
			} else {
				// 组装前台查询条件
				String dataWhere = null;
				dataWhere = this.buildLoadDataCondition(params, paramVO, queryTempletVO);
				if(StringUtils.isNotBlank(dataWhere)) {
					if(where.length() > 0) {
						where.append(" and ");
					}
					where.append(dataWhere);
				}

				/**
				 * 查询模板的相关信息
				 */
				// 这个条件都只是表头的，表体不需要查询模板的条件
				if(StringUtils.isBlank(params)) {
					// 没有经过查询窗口的查询
					String defaultCond = QueryHelper.getDefaultCond(queryTempletVO);
					if(StringUtils.isNotBlank(defaultCond)) {
						if(where.length() > 0) {
							where.append(" and ");
						}
						where.append(defaultCond);
					}
				}
				// 固定条件是必须加上去的，不管前台有没有传入，就算传入了，相同的条件也没关系
				String immobilityCond = QueryHelper.getImmobilityCond(queryTempletVO);
				if(StringUtils.isNotBlank(immobilityCond)) {
					if(where.length() > 0) {
						where.append(" and ");
					}
					where.append(immobilityCond);
				}
			}
		}

		/**
		 * 处理扩展参数 如左边树的参数
		 */
		if(StringUtils.isNotBlank(extendCond)) {
			if(where.length() > 0) {
				where.append(" and ");
			}
			where.append(extendCond);
		}

		/**
		 * 2.2.处理排序
		 */

		orderBy = this.buildLoadDataOrderBy(paramVO, voClass, orderBy);
		if(orderBy != null && orderBy.length() > 0) {
			orderBy = orderBy.toLowerCase();
			if(orderBy.indexOf(Constants.ORDER_BY) != -1) {
				if(this.isCompatibleMode() || paramVO.isBody()) {
					where.append(" ");
					where.append(orderBy);
				}
			}
		}

		/**
		 * 3.进行查询
		 */
		PaginationVO paginationVO = getPaginationVO(billInfo, paramVO, voClass, offset, pageSize, where.toString(),
				orderBy, values);
		// 在执行公式之前做一些操作
		processBeforeExecFormula(paginationVO.getItems(), paramVO);

		UiBillTempletVO uiBillTempletVO = this.getBillTempletVO(paramVO.getTemplateID());
		// 统计行的数据
		paginationVO.setSummaryRowMap(buildSummaryRowMap(uiBillTempletVO, voClass, where.toString()));
		// 表头时，有表头表尾等多个tableCode；表体时，只会有当前表体的tableCode
		String[] tableCodes = this.getTableCodes(paramVO.getTabCode(), paramVO.isBody());
		/**
		 * 4.转换数据格式，准备执行公式
		 */
		// 首先转换为Map的list，因为执行公式需要使用这种数据结构
		List<?> itemList = paginationVO.getItems();
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
		List<Map<String, Object>> list = this.execFormula4Templet(mapList, uiBillTempletVO, paramVO.isBody(),
				tableCodes, null);
		processAfterExecFormula(list, paramVO, orderBy);
		paginationVO.setItems(list);
		return paginationVO;
	}

	public PaginationVO getPaginationVO(AggregatedValueObject billInfo, ParamVO paramVO,
			Class<? extends SuperVO> voClass, int offset, int pageSize, String where, String orderBy, Object... values) {
		String sql = getBaseSql();
		if(StringUtils.isNotBlank(where)) {
			sql += " and " + where;
		}
		if(orderBy != null && orderBy.length() > 0 && orderBy.indexOf(Constants.ORDER_BY) != -1) {
			sql += orderBy;
		}
		NWDao dao = NWDao.getInstance();
		PaginationVO paginationVO = dao.queryBySqlWithPaging(sql, HashMap.class, offset, pageSize, values);
		return paginationVO;
	}

	/**
	 * 将新录入的数据生成调整单。
	 */
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		CircularlyAccessibleValueObject[] cvos = billVO.getChildrenVO();
		if(cvos == null || cvos.length == 0) {
			throw new BusiException("请先选择需要调整的库存记录！");
		}
	}
}
