package org.nw.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.constants.FunRegisterConst;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.ext.Field;
import org.nw.jf.ext.ListColumn;
import org.nw.jf.ext.RecordType;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.utils.FormulaHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.sys.FunVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.WebUtils;

public class ServiceHelper {
	private static final Log logger = LogFactory.getLog(ServiceHelper.class);

	public static AggregatedValueObject queryBillVO(AggregatedValueObject billInfo, String billId) {
		ParamVO paramVO = new ParamVO();
		paramVO.setBillId(billId);
		return queryBillVO(billInfo, paramVO, null);
	}

	public static AggregatedValueObject queryBillVO(AggregatedValueObject billInfo, ParamVO paramVO) {
		return queryBillVO(billInfo, paramVO, null);
	}

	/**
	 * 根据主表PK获取整个单据VO,如果只想获取主表的VO，可以使用queryParentVO
	 * 
	 * @param billId
	 *            主表PK
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AggregatedValueObject queryBillVO(AggregatedValueObject billInfo, ParamVO paramVO,
			String[] childrenOrderBy) {
		AggregatedValueObject billVO = null;

		try {
			NWDao dao = NWDao.getInstance();

			// 单据聚合VO类
			Class<?> aggVOClass = Class.forName(billInfo.getParentVO().getAttributeValue(VOTableVO.BILLVO).toString()
					.trim());
			// 单据VO实例
			billVO = (AggregatedValueObject) aggVOClass.newInstance();

			// 主表VO类
			Class<? extends SuperVO> parentVOClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());

			// where条件
			String where = billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString().trim();
			where += "=?";
			// 解决了单据并发操作的问题（NC和WEB同时操作）
			CircularlyAccessibleValueObject parentVO = dao.queryByCondition(parentVOClass, where, paramVO.getBillId());
			if(parentVO == null) {
				throw new BusiException("单据已被改变或已被他人删除，请重新查询，类名称[?]，单据ID[?]！",parentVOClass.toString(),paramVO.getBillId());
			}
			// billVO.setParentVO(dao.queryByCondition(parentVOClass, where,
			// billId));
			billVO.setParentVO(parentVO);
			// 子表的VO对照信息（VOTable信息）
			CircularlyAccessibleValueObject[] childrenVOTableArray = billInfo.getChildrenVO();
			// 可能没有子表
			if(childrenVOTableArray != null) {
				if(billVO instanceof IExAggVO) {
					// 多子表
					int index = 0;
					for(CircularlyAccessibleValueObject cavo : childrenVOTableArray) {
						String tableCode = cavo.getAttributeValue(VOTableVO.VOTABLE).toString().trim();
						if(paramVO != null) {
							// 如果这个页签使用分页，那么不需要查询，使用页签自己的加载数据
							Boolean isPagination = paramVO.getBodyPaginationMap().get(tableCode);
							if(isPagination != null && isPagination.booleanValue()) {
								continue;
							}
						}
						Class<? extends SuperVO> childrenVOClass = (Class<? extends SuperVO>) Class.forName(cavo
								.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
						where = cavo.getAttributeValue(VOTableVO.PKFIELD).toString().trim() + "=? ";
						if(childrenOrderBy != null) {
							String orderBy = null;
							try {
								// 这里捕获数组越界异常，避免模板增加tab后需要改动程序
								orderBy = childrenOrderBy[index];
							} catch(ArrayIndexOutOfBoundsException e) {

							}
							if(orderBy != null) {
								where += orderBy;
							}
						}
//						SuperVO[] superVOArr = dao.queryForSuperVOArrayByCondition(childrenVOClass, where,
//								paramVO.getBillId());
						SuperVO[] superVOArr = dao.queryForSuperVOArrayByCondition(childrenVOClass, where,
								parentVO.getAttributeValue(childrenVOTableArray[0].getAttributeValue(VOTableVO.PKFIELD).toString().trim()));
						((IExAggVO) billVO).setTableVO(tableCode, superVOArr);
						index++;
					}
				} else {
					// 单子表
					if(childrenVOTableArray.length > 1) {
						throw new BusiException("单据类型的VOTABLE子表配置了多个VO信息，但单据VO并没有实现IExAggVO接口！");
					}

					if(childrenVOTableArray.length == 1) {
						String tableCode = childrenVOTableArray[0].getAttributeValue(VOTableVO.VOTABLE).toString()
								.trim();
						boolean isPagination = false;
						if(paramVO != null) {
							// 如果这个页签使用分页，那么不需要查询，使用页签自己的加载数据
							Boolean pagination = paramVO.getBodyPaginationMap().get(tableCode);
							if(pagination != null) {
								isPagination = pagination.booleanValue();
							}
						}
						if(!isPagination) {
							Class<? extends SuperVO> childrenVOClass = (Class<? extends SuperVO>) Class
									.forName(childrenVOTableArray[0].getAttributeValue(VOTableVO.HEADITEMVO).toString()
											.trim());
							where = childrenVOTableArray[0].getAttributeValue(VOTableVO.PKFIELD).toString().trim()
									+ "=? ";// 主表在子表中的外键字段名
							if(childrenOrderBy != null) {
								where += childrenOrderBy[0];
							}
//							SuperVO[] superVOArr = dao.queryForSuperVOArrayByCondition(childrenVOClass, where,
//									paramVO.getBillId());
							SuperVO[] superVOArr = dao.queryForSuperVOArrayByCondition(childrenVOClass, where,
									parentVO.getAttributeValue(childrenVOTableArray[0].getAttributeValue(VOTableVO.PKFIELD).toString().trim()));
							billVO.setChildrenVO(superVOArr);
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("获取单据VO失败！", e);
			throw new RuntimeException(e);
		}

		return billVO;
	}

	/**
	 * 根据billId获取主表VO
	 * 
	 * @param paramVO
	 *            参数条件
	 * @param fieldNames
	 *            查询字段，若为null或者length=0，则查询所有字段
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static SuperVO queryParentVO(AggregatedValueObject billInfo, String billId) {
		// 主表VO类
		Class<? extends SuperVO> parentVOClass = null;
		try {
			parentVOClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
					.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		// where条件
		String where = billInfo.getParentVO().getAttributeValue(VOTableVO.PKFIELD).toString().trim();
		where += "=?";

		// 主表SuperVO
		return NWDao.getInstance().queryByCondition(parentVOClass, where, billId);
	}

	/**
	 * 执行表头的某些字段的编辑公式
	 * 
	 * @param pkBilltemplet
	 * @param itemKeys
	 * @param model
	 *            执行公式所需要的上下文
	 * @return
	 * @author xuqc
	 * @date 2012-3-15
	 */
	public static void execEditFormula4HeadField(UiBillTempletVO templetVO, String[] itemKeys, Map<String, Object> model) {
		if(itemKeys == null || itemKeys.length == 0) {
			return;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() != UiConstants.POS[0]) // 过滤非表头字段
				continue;
			for(String itemKey : itemKeys) {
				if(fieldVO.getItemkey().equals(itemKey)) {
					// 定位到该字段
					if(fieldVO.getEditformula() == null || fieldVO.getEditformula().trim().length() == 0) {
						// 如果没有编辑公式，则不执行
						continue;
					} else {
						// 必须逐个执行，因为编辑公式可能存在冲突，批量解析可能会出问题
						List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
						context.add(model);
						Map<String, Object> retMap = FormulaHelper.execFormula(context,
								new String[] { fieldVO.getEditformula() }, false).get(0);
						model.putAll(retMap);
					}
				}
			}
		}

	}

	/**
	 * 执行表体的某些字段的编辑公式
	 * 
	 * @param pkBilltemplet
	 * @param itemKeys
	 * @param tabCode
	 * @param model
	 *            执行公式所需要的上下文
	 * @return
	 * @author xuqc
	 * @date 2012-3-15
	 */
	public static void execEditFormula4BodyField(UiBillTempletVO templetVO, String[] itemKeys, String tabCode,
			Map<String, Object> model) {
		if(itemKeys == null || itemKeys.length == 0) {
			return;
		}
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() != UiConstants.POS[1] || !fieldVO.getTable_code().equals(tabCode)) // 过滤非表体字段
				continue;
			for(String itemKey : itemKeys) {
				if(fieldVO.getItemkey().equals(itemKey)) {
					// 定位到该字段
					if(fieldVO.getEditformula() == null || fieldVO.getEditformula().trim().length() == 0) {
						// 如果没有编辑公式，则不执行
						continue;
					} else {
						// 必须逐个执行，因为编辑公式可能存在冲突，批量解析可能会出问题
						List<Map<String, Object>> context = new ArrayList<Map<String, Object>>();
						context.add(model);
						Map<String, Object> retMap = FormulaHelper.execFormula(context,
								new String[] { fieldVO.getEditformula() }, false).get(0);
						model.putAll(retMap);
					}
				}
			}
		}
	}

	/**
	 * 返回VO class<br/>
	 * 根据paramVO中的isBody判定是否是表体,若是表体，返回第一个子表的VO Class
	 * 
	 * @param billInfo
	 * @param paramVO
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends SuperVO> getVOClass(AggregatedValueObject billInfo, ParamVO paramVO) {
		if(billInfo == null) {
			return null;
		}
		// 主表VO类
		Class<? extends SuperVO> voClass = null;
		try {
			if(!paramVO.isBody()) {
				// 如果是主表
				voClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
						.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
			} else {
				// 如果是子表
				CircularlyAccessibleValueObject[] voArr = billInfo.getChildrenVO();
				if(voArr != null && voArr.length > 0) {
					if(voArr.length == 1) {
						// 有一个子表
						voClass = (Class<? extends SuperVO>) Class.forName(voArr[0]
								.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
					} else {
						// 多子表
						VOTableVO[] voTables = (VOTableVO[]) billInfo.getChildrenVO();
						for(VOTableVO voTable : voTables) {
							if(paramVO.getTabCode().equals(voTable.getVotable())) {
								// 多子表时，voTable中的votable字段作为子表与tabCode对应关系的信息（单子表时就不需要了）
								voClass = (Class<? extends SuperVO>) Class.forName(voTable.getHeaditemvo());
								break;
							}
						}
					}
				} else {
					// 如果表体时候没有表体vo，则可能是单列表模式
					try {
						voClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
								.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
					} catch(Exception e) {
						logger.error(
								"没有找到对应该表体的VO：tabCode=" + paramVO.getTabCode() + ",funCode=" + paramVO.getFunCode()
										+ ",billType=" + paramVO.getBillType() + ",登录用户是："
										+ WebUtils.getLoginInfo().getUser_code(), e);
						throw new BusiException("没有找到对应该表体的VO[?]！", e,paramVO.getTabCode());
					}

					if(voClass == null) {
						// 如果还是没有找到VO，就不应该发送该请求!!!
						throw new BusiException("没有找到对应该表体的VO[?]！",paramVO.getTabCode());
					}
				}
			}
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return voClass;
	}

	/**
	 * 从前台传入的数据不会为null，只会是空串，而对于数据库来说，这些值应该是null值
	 * 
	 * @param billVO
	 * @return
	 */
	public static AggregatedValueObject convertEmptyStringToNull(AggregatedValueObject billVO) {
		SuperVO parentVO = (SuperVO) billVO.getParentVO();
		if(parentVO != null) {
			for(String att : parentVO.getAttributeNames()) {
				if(parentVO.getAttributeValue(att) != null && parentVO.getAttributeValue(att) instanceof String
						&& ((String) parentVO.getAttributeValue(att)).trim().length() == 0) {
					parentVO.setAttributeValue(att, null);
				}
			}
		}

		if(billVO instanceof IExAggVO) {
			String[] keys = ((IExAggVO) billVO).getTableCodes();
			for(String key : keys) {
				CircularlyAccessibleValueObject[] childrenVO = ((IExAggVO) billVO).getTableVO(key);
				if(childrenVO != null) {
					for(CircularlyAccessibleValueObject c : childrenVO) {
						for(String att : c.getAttributeNames()) {
							if(c.getAttributeValue(att) != null && c.getAttributeValue(att) instanceof String
									&& ((String) c.getAttributeValue(att)).trim().length() == 0) {
								c.setAttributeValue(att, null);
							}
						}
					}
				}
			}

		} else {
			CircularlyAccessibleValueObject[] childrenVO = billVO.getChildrenVO();
			if(childrenVO != null && childrenVO.length > 0)
				for(CircularlyAccessibleValueObject c : childrenVO) {
					for(String att : c.getAttributeNames()) {
						if(c.getAttributeValue(att) != null && c.getAttributeValue(att) instanceof String
								&& ((String) c.getAttributeValue(att)).trim().length() == 0) {
							c.setAttributeValue(att, null);
						}
					}
				}
		}
		return billVO;
	}

	public static AggregatedValueObject removeUnchangedChildrenVO(AggregatedValueObject billVO) {
		if(billVO instanceof IExAggVO) {
			if(((IExAggVO) billVO).getAllChildrenVO() != null) {
				String[] keys = ((IExAggVO) billVO).getTableCodes();
				for(String key : keys) {
					CircularlyAccessibleValueObject[] childrenVO = ((IExAggVO) billVO).getTableVO(key);
					if(childrenVO != null) {
						List<CircularlyAccessibleValueObject> list = new ArrayList<CircularlyAccessibleValueObject>();
						for(CircularlyAccessibleValueObject c : childrenVO) {
							if(c.getStatus() != VOStatus.UNCHANGED) {
								list.add(c);
							}
						}
						if(list.size() == 0) {
							((IExAggVO) billVO).setTableVO(key, null);
						} else {
							((IExAggVO) billVO).setTableVO(
									key,
									list.toArray((CircularlyAccessibleValueObject[]) Array.newInstance(
											childrenVO[0].getClass(), 0)));
						}
					}
				}
			}
		} else {
			if(billVO.getChildrenVO() != null) {
				CircularlyAccessibleValueObject[] childrenVO = billVO.getChildrenVO();
				if(childrenVO != null) {
					List<CircularlyAccessibleValueObject> list = new ArrayList<CircularlyAccessibleValueObject>();
					for(CircularlyAccessibleValueObject c : childrenVO) {
						if(c.getStatus() != VOStatus.UNCHANGED) {
							list.add(c);
						}
					}
					if(list.size() == 0) {
						billVO.setChildrenVO(null);
					} else {
						billVO.setChildrenVO(list.toArray((CircularlyAccessibleValueObject[]) Array.newInstance(
								childrenVO[0].getClass(), 0)));
					}
				}
			}
		}
		return billVO;
	}

	/**
	 * 返回VO class<br/>
	 * 根据paramVO中的isBody判定是否是表体,若是表体，返回第一个子表的VO Class
	 * 
	 * @param billInfo
	 * @param paramVO
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends SuperVO>[] getVOClassAry(AggregatedValueObject billInfo) {
		List<Class<? extends SuperVO>> clazzAry = new ArrayList<Class<? extends SuperVO>>();
		// 表头
		try {
			if(billInfo.getParentVO() != null) {
				Class<? extends SuperVO> parentClass = (Class<? extends SuperVO>) Class.forName(billInfo.getParentVO()
						.getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
				clazzAry.add(parentClass);
			}
			if(billInfo.getChildrenVO() != null && billInfo.getChildrenVO().length > 0) {
				for(int i = 0; i < billInfo.getChildrenVO().length; i++) {
					Class<? extends SuperVO> childVOClass = (Class<? extends SuperVO>) Class.forName(billInfo
							.getChildrenVO()[i].getAttributeValue(VOTableVO.HEADITEMVO).toString().trim());
					clazzAry.add(childVOClass);
				}
			}
		} catch(Exception e) {
			logger.error(e);
		}
		return clazzAry.toArray(new Class[clazzAry.size()]);
	}

	public static String buildSelectSql(AggregatedValueObject billInfo, ParamVO paramVO, String where, String[] fields) {
		Class<? extends SuperVO>[] clazzAry = ServiceHelper.getVOClassAry(billInfo);
		if(clazzAry == null || clazzAry.length == 0) {
			throw new BusiException("VOTABLE配置不正确，请检查！");
		}
		SuperVO parentVO = null;
		try {
			parentVO = clazzAry[0].newInstance();
		} catch(Exception e) {
			logger.error("无法实例化表头VO，" + e);
			return null;
		}
		String sql = DaoHelper.buildSelectSql(clazzAry[0], null, fields, false);
		where = DaoHelper.getWhereClause(clazzAry[0], where); // 加入dr处理
		StringBuffer sqlBuffer = new StringBuffer(sql);
		StringBuffer autoAppendWhereSql = new StringBuffer();
		if(billInfo.getChildrenVO() != null && billInfo.getChildrenVO().length > 0) {
			// 如果存在子表，则拼接子表的条件，使用left join 子表，来提高效率
			for(int i = 0; i < billInfo.getChildrenVO().length; i++) {
				Class<? extends SuperVO> bodyVOClazz = clazzAry[i + 1];// 第一个voclass是表头的voclass
				// 加校验。如果子表的VO和主表VO是一样的。条件left join链接 edit by wangpp 20121209
				if(bodyVOClazz != null && bodyVOClazz.getName().equals(clazzAry[0].getName())) {
					continue;
				}
				SuperVO bodyVO = null;
				try {
					bodyVO = bodyVOClazz.newInstance();
				} catch(Exception e) {
					logger.error("无法实例化表体VO，" + e);
					continue;
				}
				String appCond = findHappenAndAdjustSql(sqlBuffer, parentVO, bodyVO, where);
				if(appCond != null) {
					autoAppendWhereSql.append(" AND ").append(appCond);
				}
			}
		}
		if(StringUtils.isNotBlank(where) || StringUtils.isNotBlank(autoAppendWhereSql.toString())) {
			sqlBuffer.append(" where ");
		}
		if(StringUtils.isNotBlank(where)) {
			sqlBuffer.append(where);
		}
		if(StringUtils.isNotBlank(autoAppendWhereSql.toString())) {
			sqlBuffer.append(autoAppendWhereSql);
		}
		return sqlBuffer.toString();
	}

	/**
	 * 根据billInfo返回单据的查询sql，这里肯定是表头的查询，表体不会left
	 * join表体，如果存在子表条件，则使用合并子表查询的方式来提高效率
	 * 
	 * @param billInfo
	 * @param paramVO
	 * @param where
	 * @return
	 * @author xuqc
	 * @date 2012-7-20
	 * 
	 */
	public static String buildSelectSql(AggregatedValueObject billInfo, ParamVO paramVO, String where) {
		return buildSelectSql(billInfo, paramVO, where, null);
	}

	/**
	 * 合并子表查询
	 * 
	 * @param qrySqlBuffer
	 * @param headVoInst
	 *            表头VO的实例
	 * @param bodyVo
	 *            表体VO的实例
	 * @param strWhere
	 * @return
	 * @author xuqc
	 * @date 2012-7-20
	 * 
	 */
	private static String findHappenAndAdjustSql(StringBuffer qrySqlBuffer, SuperVO headVoInst, SuperVO bodyVo,
			String strWhere) {
		String tableName = bodyVo.getTableName();
		StringBuffer autoAppendWhereSql = new StringBuffer();
		// 如果查询条件中包含该表的信息，那么增加关联信息 left join bodyTable on
		// bodyTable.pk_Parent=headTable.pk
		if(strWhere.toUpperCase().contains(tableName.toUpperCase())
				&& !qrySqlBuffer.toString().contains(" left join " + tableName)) {// 关联过的就无需再关联
			if(StringUtils.isBlank(bodyVo.getParentPKFieldName())) {
				throw new BusiException("表体的VO类没有定义getParentPKFieldName的返回值！");
			}
			//yaojiie 2015 12 08添加 WITH (NOLOCK)
			String joinSql = " left join " + tableName + " WITH(NOLOCK) " + " on " + tableName + "." + bodyVo.getParentPKFieldName() + "="
					+ headVoInst.getTableName() + "." + headVoInst.getPKFieldName();
			qrySqlBuffer.append(joinSql);
			autoAppendWhereSql.append("isnull(" + tableName + ".dr,0)=0");
			return autoAppendWhereSql.toString();
		} else {
			logger.debug("[HZG]Not happend for: " + tableName);
			return null;
		}
	}

	/**
	 * 返回功能节点下的按钮对象,不是锁定的按钮. 只需要fun_name,help_name,locked_flag等字段 XXX
	 * 如果用户有权限，那么加上auth:'y',
	 * 
	 * @param paramVO
	 * @return
	 */
	public static List<FunVO> getBtnRegisterAry(ParamVO paramVO) {
		String sql = "select pk_fun,fun_name,help_name,locked_flag,bill_type,class_name from nw_fun WITH(NOLOCK) where isnull(dr,0)=0 and fun_code LIKE '"
				+ paramVO.getFunCode() + "%' AND fun_property=?";
		List<FunVO> funVOs = NWDao.getInstance().queryForList(sql, FunVO.class, FunRegisterConst.POWERFUL_BUTTON);
		sql = "select pk_fun from nw_fun WITH(NOLOCK) where fun_code like '" + paramVO.getFunCode() + "%' and fun_property=? and "
				+ "pk_fun in (select pk_fun from nw_power_fun WITH(NOLOCK) where isnull(dr,0)=0 "
				+ " and pk_role in (select pk_role from nw_user_role WITH(NOLOCK) where pk_user=? and isnull(dr,0)=0))";
		List<String> pkFunAry = NWDao.getInstance().queryForList(sql, String.class, FunRegisterConst.POWERFUL_BUTTON,
				WebUtils.getLoginInfo().getPk_user());
		if(funVOs != null && funVOs.size() > 0 && pkFunAry != null && pkFunAry.size() > 0) {
			for(FunVO funVO : funVOs) {
				if(pkFunAry.contains(funVO.getPk_fun())) {
					// 有权限
					funVO.setAuth(UFBoolean.TRUE);
				} else {
					funVO.setAuth(UFBoolean.FALSE);
				}
			}
		}
		return funVOs;
	}

	/**
	 * 构建表格可以直接使用的模板
	 * 
	 * @param paramVO
	 * @param templetVO
	 * @param tabCode
	 * @return
	 */
	public static Map<String, Object> buildTempletMap4Grid(ParamVO paramVO, UiBillTempletVO templetVO, String tabCode) {
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		List<RecordType> recordAry = new ArrayList<RecordType>();
		List<ListColumn> columnAry = new ArrayList<ListColumn>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("templateID", templetVO.getTemplateID());
		paramMap.put("funCode", paramVO.getFunCode());
		paramMap.put("nodeKey", paramVO.getNodeKey());
		paramMap.put("tabCode", tabCode);
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(StringUtils.isBlank(tabCode)) {
				recordAry.add(fieldVO.buildRecordType());
				columnAry.add(fieldVO.buildListColumn());
			} else {
				if(fieldVO.getTable_code().equals(tabCode)) {
					recordAry.add(fieldVO.buildRecordType());
					columnAry.add(fieldVO.buildListColumn());
				}
			}
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("params", paramMap);
		retMap.put("records", recordAry);
		retMap.put("columns", columnAry);
		return retMap;
	}

	/**
	 * 构建表单可以使用的模板
	 * 
	 * @param paramVO
	 * @param templetVO
	 * @param tabCode
	 * @return
	 */
	public static Map<String, Object> buildTempletMap4Form(ParamVO paramVO, UiBillTempletVO templetVO, String tabCode) {
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		List<Object> itemAry = new ArrayList<Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("templateID", templetVO.getTemplateID());
		paramMap.put("funCode", paramVO.getFunCode());
		paramMap.put("nodeKey", paramVO.getNodeKey());
		paramMap.put("tabCode", tabCode);
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(StringUtils.isNotBlank(tabCode)) {
				if(fieldVO.getTable_code().equals(tabCode)) {
					Object item = fieldVO.buildFormItem();
					if(item instanceof Field) {
						((Field) item).setId(null);// 避免和卡片页冲突
					} else if(item instanceof BaseRefModel) {
						((BaseRefModel) item).setId(null);// 避免和卡片页冲突
					}
					itemAry.add(item);
				}
			} else {
				Object item = fieldVO.buildFormItem();
				if(item instanceof Field) {
					((Field) item).setId(null);// 避免和卡片页冲突
				} else if(item instanceof BaseRefModel) {
					((BaseRefModel) item).setId(null);// 避免和卡片页冲突
				}
				itemAry.add(item);
			}
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("params", paramMap);
		retMap.put("items", itemAry);
		return retMap;
	}
}
