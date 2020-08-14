package org.nw.dao.helper;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.annotation.NotDBField;
import org.nw.basic.util.DateUtils;
import org.nw.basic.util.ReflectionUtils;
import org.nw.basic.util.VOUtils;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.dao.BaseDao;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.pub.lang.UFTime;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;

/**
 * dao操作的处理类
 * 
 * @author fangw
 * @date 2011-4-12
 */
public class DaoHelper {
	private static final Log logger = LogFactory.getLog(DaoHelper.class);
	private static SimpleDateFormat format_datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// private static SimpleDateFormat format_date = new
	// SimpleDateFormat("yyyy-MM-dd");
	private static ConcurrentHashMap<String, Set<String>> tableColumnNamesCacheMap = new ConcurrentHashMap<String, Set<String>>();

	/**
	 * 转换resultSet到vo属性 2011-4-12
	 */
	@SuppressWarnings("unchecked")
	public static void convertResultSet(Object owner, ResultSet rs) {
		try {
			ResultSetMetaData md = rs.getMetaData();
			int columnCount = md.getColumnCount();
			if(owner instanceof Map) {
				// map类型，返回Map
				@SuppressWarnings("rawtypes")
				Map map = (Map) owner;
				for(int i = 1; i <= columnCount; i++) {
					// 对于mysql，getColumnName返回的是数据库实际的列名，如果要返回as的列名，需要使用getColumnLabel
					String colName;
					if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
						colName = md.getColumnLabel(i).toLowerCase(); // 数据库字段是大写的
					} else {
						colName = md.getColumnName(i).toLowerCase(); // 数据库字段是大写的
					}
					Object value = getResultObject(rs, i);
					if(value != null) {
						map.put(colName, value);
					} else {
						map.put(colName, null);
					}
				}
			} else {
				for(int i = 1; i <= columnCount; i++) {
					String colName;
					if(DB_TYPE.MYSQL.equals(NWDao.getCurrentDBType())) {
						colName = md.getColumnLabel(i).toLowerCase(); // 数据库字段是大写的
					} else {
						colName = md.getColumnName(i).toLowerCase(); // 数据库字段是大写的
					}
					String methodName = VOUtils.getNCSetterMethodName(colName);
					Object value = getResultObject(rs, i);
					if(value != null) {
						Field field = ReflectionUtils.getDeclaredField(owner, colName.toLowerCase());
						if(field == null) {
							continue;
						}
						if(field.getType() == UFBoolean.class) {
							UFBoolean b = UFBoolean.valueOf(String.valueOf(value));
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { UFBoolean.class },
									new Object[] { b });
						} else if(field.getType() == UFDouble.class) {
							UFDouble d = new UFDouble(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { UFDouble.class },
									new Object[] { d });
						} else if(field.getType() == UFDateTime.class) {
							String str = value.toString();// 其他系统中来的数据，可能不是规范的
							if(str.length() > 19) {
								str = str.substring(0, 19);
							}
							UFDateTime dt = new UFDateTime(str);
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { UFDateTime.class },
									new Object[] { dt });
						} else if(field.getType() == UFDate.class) {
							UFDate d = new UFDate(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { UFDate.class },
									new Object[] { d });
						} else if(field.getType() == UFTime.class) {
							UFTime t = new UFTime(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { UFTime.class },
									new Object[] { t });
						} else if(field.getType() == Date.class) {
							Date t = DateFormat.getInstance().parse(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { Date.class },
									new Object[] { t });
						} else if(field.getType() == Boolean.class) {
							Boolean t = new Boolean(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { Boolean.class },
									new Object[] { t });
						} else if(field.getType() == BigDecimal.class) {
							BigDecimal t = new BigDecimal(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { BigDecimal.class },
									new Object[] { t });
						} else if(field.getType() == Integer.class) {
							Integer t = new Integer(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { Integer.class },
									new Object[] { t });
						} else if(field.getType() == Long.class) {
							Long t = new Long(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { Long.class },
									new Object[] { t });
						} else if(field.getType() == Double.class) {
							Double t = new Double(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { Double.class },
									new Object[] { t });
						} else if(field.getType() == Float.class) {
							Float t = new Float(value.toString());
							ReflectionUtils.invokeMethod(owner, methodName, new Class[] { Float.class },
									new Object[] { t });
						} else {
							try {
								ReflectionUtils.invokeMethod(owner, methodName, new Class[] { value.getClass() },
										new Object[] { value });
							} catch(Exception e) {
								// 如果是主键，有些vo可能没有相应的set方法，只能用setPrimaryKey
								if(owner instanceof SuperVO) {
									String pkFieldName = ReflectionUtils.invokeMethod(owner, "getPKFieldName")
											.toString();
									if(pkFieldName.equalsIgnoreCase(colName)) {
										// 确认是主键，进行设置
										ReflectionUtils.invokeMethod(owner, "setPrimaryKey",
												new Class[] { String.class }, new Object[] { value });
									} else {
										// 放弃吧
										logger.warn("警告：VO " + owner.getClass() + "中不存在与字段" + colName
												+ "对应的属性,或vo字段类型不匹配！");
										try {
											logger.warn("字段类型应该是：" + value.getClass());
										} catch(Exception e1) {
											e1.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("convertResultSet error!", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 返回从数据库读取的某个字段的值，支持CLOB,BLOB类型
	 * 
	 * @param rs
	 * @param index
	 * @return
	 * @throws Exception
	 * @author xuqc
	 * @date 2012-10-12
	 * 
	 */
	private static Object getResultObject(ResultSet rs, int index) throws Exception {
		Object value = null;
		ResultSetMetaData md = rs.getMetaData();
		String columnTypeName = md.getColumnTypeName(index);
		if(columnTypeName.equalsIgnoreCase("CLOB")) {
			Clob clob = rs.getClob(index);
			if(clob != null && clob.length() > 0) {
				value = clob.getSubString(1, (int) clob.length());
			}
		} else if(columnTypeName.equalsIgnoreCase("BLOB") || columnTypeName.equalsIgnoreCase("image")) {
			Blob blob = rs.getBlob(index);
			if(blob != null) {
				try {
					value = blob.getBinaryStream();
				} catch(Exception e) {
					logger.error("BLOB字段读取失败!", e);
				}
			}
		} else {
			value = rs.getObject(index);
		}
		return value;
	}

	/**
	 * 设置主键-用UUID算法
	 * 
	 * @param superVO
	 */
	public static void setUuidPrimaryKey(SuperVO superVO) {
		if(superVO.getStatus() == VOStatus.NEW) {
			if(superVO.getPrimaryKey() == null)
				superVO.setPrimaryKey(UUID.randomUUID().toString().replace("-", ""));
		}
	}

	/**
	 * 设置主键-用UUID算法<br>
	 * 2011-4-9
	 */
	public static void setUuidPrimaryKey(AggregatedValueObject billVO) {
		if(billVO.getParentVO() != null) {
			SuperVO parentVO = (SuperVO) billVO.getParentVO();
			// 设置主表
			if(parentVO.getStatus() == VOStatus.NEW) {
				if(parentVO.getPrimaryKey() == null)
					parentVO.setPrimaryKey(UUID.randomUUID().toString().replace("-", ""));
			}
			CircularlyAccessibleValueObject[] cvos = null;
			if(billVO instanceof IExAggVO) {
				// 多子表
				cvos = ((IExAggVO) billVO).getAllChildrenVO();
			} else {
				// 单子表
				cvos = billVO.getChildrenVO();
			}

			// 设置子表
			if(cvos != null && cvos.length > 0) {
				for(CircularlyAccessibleValueObject vo : cvos) {
					SuperVO childSuperVO = (SuperVO) vo;
					if(childSuperVO.getStatus() == VOStatus.NEW) {
						if(childSuperVO.getPrimaryKey() == null)
							childSuperVO.setPrimaryKey(UUID.randomUUID().toString().replace("-", ""));
						if(StringUtils.isBlank(childSuperVO.getParentPKFieldName())) {
							// 这里不要抛出错误，因为有些情况下，表体只是挂在这个单据上，但是跟表头可能没有直接的联系
							logger.warn("表体的VO类没有定义getParentPKFieldName的返回值！");
						}
						childSuperVO.setAttributeValue(childSuperVO.getParentPKFieldName(), parentVO.getPrimaryKey());
					}
				}
			}
		} else {
			// 单表体的时候
			if(billVO.getChildrenVO() != null && billVO.getChildrenVO().length > 0) {
				for(CircularlyAccessibleValueObject vo : billVO.getChildrenVO()) {
					SuperVO superVO = (SuperVO) vo;
					if(superVO.getStatus() == VOStatus.NEW) {
						if(superVO.getPrimaryKey() == null) {
							superVO.setPrimaryKey(UUID.randomUUID().toString().replace("-", ""));
						}
					}
				}
			}
		}
	}

	/**
	 * 产生20位的随机数 作为主键， 8位时间+4位sessionid+4为pk_corp
	 * 
	 * @return
	 */
	public static String new20PosPrimaryKey() {
		String str = DateUtils.getCurrentDate("yyyyMMddHHmmssSSS");// 17位
		StringBuilder sb = new StringBuilder(str.substring(8));// 减去前面8位，剩余9位,在同一时间，这9位一般是相同的
		String id = ServletContextHolder.getRequest().getSession(true).getId();
		if(id.length() > 2) {
			sb.append(id.substring(0, 3));// 加上session的前3位，同一个session，这3为一般也是相同的
		}
		if(WebUtils.getLoginInfo() != null) {
			String pk_corp = WebUtils.getLoginInfo().getPk_corp();
			if(pk_corp.length() > 3) {
				sb.append(pk_corp.substring(0, 4));// 加上公司的前4位，公司一般也是相同的
			}
		}
		// 3位随机字母， 一位0-9随机数
		return sb.append(getRandomChar()).append(getRandomChar()).append(getRandomChar())
				.append(String.valueOf(getRandomInt())).toString();
	}

	/**
	 * 返回随机字母，包括大小写
	 * 
	 * @return
	 */
	private static char getRandomChar() {
		int choice = new Random().nextInt(2) % 2 == 0 ? 65 : 97;
		return (char) (choice + new Random().nextInt(26));
	}

	/**
	 * 0-9随机数
	 * 
	 * @return
	 */
	private static int getRandomInt() {
		double d = Math.floor(Math.random() * 10);
		return new Double(d).intValue();
	}

	public static void main(String[] args) {
		// for(int i = 0; i < 100; i++) {
		// double d = Math.floor(Math.random() * 10);
		// System.out.println(new Double(d).intValue());
		// }
	}

	/**
	 * 设置主键-用UUID算法
	 * 
	 * @param superVO
	 */
	public static void setNWPrimaryKey(SuperVO superVO) {
		if(superVO.getStatus() == VOStatus.NEW) {
			if(superVO.getPrimaryKey() == null)
				superVO.setPrimaryKey(new20PosPrimaryKey());
		}
	}

	/**
	 * 设置主键-用UUID算法<br>
	 * 2011-4-9
	 */
	public static void setNWPrimaryKey(AggregatedValueObject billVO) {
		if(billVO.getParentVO() != null) {
			SuperVO parentVO = (SuperVO) billVO.getParentVO();
			// 设置主表
			if(parentVO.getStatus() == VOStatus.NEW) {
				if(parentVO.getPrimaryKey() == null)
					parentVO.setPrimaryKey(new20PosPrimaryKey());
			}
			CircularlyAccessibleValueObject[] cvos = null;
			if(billVO instanceof IExAggVO) {
				// 多子表
				cvos = ((IExAggVO) billVO).getAllChildrenVO();
			} else {
				// 单子表
				cvos = billVO.getChildrenVO();
			}

			// 设置子表
			if(cvos != null && cvos.length > 0) {
				for(CircularlyAccessibleValueObject vo : cvos) {
					SuperVO childSuperVO = (SuperVO) vo;
					if(childSuperVO.getStatus() == VOStatus.NEW) {
						if(childSuperVO.getPrimaryKey() == null)
							childSuperVO.setPrimaryKey(new20PosPrimaryKey());
						if(StringUtils.isBlank(childSuperVO.getParentPKFieldName())) {
							// 这里不要抛出错误，因为有些情况下，表体只是挂在这个单据上，但是跟表头可能没有直接的联系
							logger.warn("表体的VO类没有定义getParentPKFieldName的返回值！");
						}
						childSuperVO.setAttributeValue(childSuperVO.getParentPKFieldName(), parentVO.getPrimaryKey());
					}
				}
			}
		} else {
			// 单表体的时候
			if(billVO.getChildrenVO() != null && billVO.getChildrenVO().length > 0) {
				for(CircularlyAccessibleValueObject vo : billVO.getChildrenVO()) {
					SuperVO superVO = (SuperVO) vo;
					if(superVO.getStatus() == VOStatus.NEW) {
						if(superVO.getPrimaryKey() == null)
							superVO.setPrimaryKey(new20PosPrimaryKey());
					}
				}
			}
		}
	}

	/**
	 * 生成count查询语句，用于得到统计行数据，这里只会查询voClass中包括的字段，其他字段（公式生成），
	 * 
	 * @param clazz
	 *            查询的vo类
	 * @param where
	 *            查询的条件
	 * @param fields
	 *            要进行统计的行
	 * @return
	 * @author xuqc
	 * @date 2012-10-19
	 * 
	 */
	public static String buildSumSelectSql(Class<? extends SuperVO> clazz, String where, List<String[]> fieldAry) {
		if(fieldAry == null || fieldAry.size() == 0) {
			return null;
		}
		SuperVO superVO = null;
		try {
			superVO = clazz.newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		StringBuffer buffer = new StringBuffer();
		String tableName = superVO.getTableName();
		boolean flag = false;// 是否存在需要查询的字段，如果都不存在，那么不需要查询了
		buffer.append("SELECT ");
		for(int i = 0; i < fieldAry.size(); i++) {
			// 判断字段是否存在
			String[] fieldNameAry = fieldAry.get(i);
			Field field = ReflectionUtils.getDeclaredField(superVO, fieldNameAry[0]);
			if(field != null) {
				NotDBField notDBField = field.getAnnotation(NotDBField.class);// 判断是否是数据库的字段
				if(notDBField == null) {
					flag = true;
					buffer.append("sum(").append(tableName).append(".").append(fieldNameAry[0]).append(") as ")
							.append(fieldNameAry[1]).append(",");
				}
			}
		}
		if(!flag) {
			return null;
		}
		buffer.setLength(buffer.length() - 1);
		buffer.append(" FROM ").append(tableName).append(" WITH(NOLOCK) ");

		where = DaoHelper.getWhereClause(clazz, where);
		if(where != null && where.length() != 0) {
			if(where.toUpperCase().trim().startsWith("ORDER ")) {
				buffer.append(" ").append(where);
			} else {
				buffer.append(" WHERE ").append(where);
			}
		}

		logger.debug("buildCountSelectSql=" + buffer.toString());
		return buffer.toString();
	}

	/**
	 * 根据vo生成select语句
	 * 
	 * @param voClass
	 * @param condition
	 * @param fields
	 * @param distinct
	 *            是否加入distinct关键字
	 * @return
	 * @author xuqc
	 * @date 2012-10-19
	 * 
	 */
	public static String buildSelectSql(Class<? extends SuperVO> voClass, String condition, String[] fields,
			boolean distinct) {
		SuperVO vo = null;
		try {
			vo = voClass.newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		String pkName = vo.getPKFieldName();
		boolean hasPKField = false;
		StringBuffer buffer = new StringBuffer();
		String tableName = vo.getTableName();

		buffer.append("SELECT ");
		if(distinct) {
			buffer.append("DISTINCT ");
		}
		if(fields == null || fields.length == 0) {
			buffer.append(tableName).append(".* FROM ").append(tableName).append(" WITH(NOLOCK) ");
		} else {
			for(int i = 0; i < fields.length; i++) {
				buffer.append(fields[i]).append(",");
				if(fields[i].equalsIgnoreCase(pkName)) {
					hasPKField = true;
				}
			}
			if(!hasPKField) {
				buffer.append(pkName).append(",");
			}
			buffer.setLength(buffer.length() - 1);
			buffer.append(" FROM ").append(tableName).append(" WITH(NOLOCK) ");
		}

		if(condition != null && condition.length() != 0) {
			if(condition.toUpperCase().trim().startsWith("ORDER ")) {
				buffer.append(" ").append(condition);
			} else {
				buffer.append(" WHERE ").append(condition);
			}
		}

		logger.debug("buildSelectSql=" + buffer.toString());
		return buffer.toString();
	}

	/**
	 * 生成select查询语句<br>
	 * 2011-4-9
	 */
	public static String buildSelectSql(Class<? extends SuperVO> voClass, String condition, String[] fields) {
		return buildSelectSql(voClass, condition, fields, false);
	}

	/**
	 * 生成删除<br>
	 * 2011-4-9
	 */
	public static String getDeleteSQL(SuperVO superVO, boolean isLogicalDelete) {
		if(superVO == null) {
			return null;
		}
		if(StringUtils.isBlank(superVO.getPrimaryKey())) {
			throw new BusiException("删除时主键不能为空！");
		}

		superVO.setStatus(VOStatus.DELETED);

		// 如果有dr字段，则标记删除，否则进行物理删除
		Field field = ReflectionUtils.getDeclaredField(superVO, "dr");
		if(!isLogicalDelete || field == null) {
			// 物理删除
			StringBuilder sb = new StringBuilder("delete from ");
			sb.append(superVO.getTableName());
			sb.append(" where ");
			sb.append(superVO.getPKFieldName());
			sb.append("=");

			field = ReflectionUtils.getDeclaredField(superVO, superVO.getPKFieldName());
			if(field.getType().equals(String.class) || field.getType().equals(UFDate.class)
					|| field.getType().equals(UFTime.class) || field.getType().equals(UFDateTime.class)
					|| field.getType().equals(UFBoolean.class)) {
				if(superVO.getPrimaryKey() != null) {
					sb.append("'");
					sb.append(superVO.getPrimaryKey());
					sb.append("'");
				} else {
					sb.append("null");
				}
			} else {
				sb.append(superVO.getPrimaryKey());
			}
			return sb.toString();
		} else {
			// 标记删除
			StringBuilder sb = new StringBuilder("update ");
			sb.append(superVO.getTableName());
			sb.append(" set dr=1");

			Field field_ts = ReflectionUtils.getDeclaredField(superVO, "ts");
			if(field_ts != null) {
				if(field_ts.getType().equals(UFDateTime.class)) {
					superVO.setAttributeValue("ts", new UFDateTime(format_datetime.format(new Date())));
				} else {
					superVO.setAttributeValue("ts", format_datetime.format(new Date()));
				}
				sb.append(",ts=");
				if(superVO.getAttributeValue("ts") != null) {
					sb.append("'");
					sb.append(superVO.getAttributeValue("ts"));
					sb.append("'");
				} else {
					sb.append("null");
				}
			}
			Field field_modify_time = ReflectionUtils.getDeclaredField(superVO, "modify_time");
			if(field_modify_time != null) {
				if(field_modify_time.getType().equals(UFDateTime.class)) {
					superVO.setAttributeValue("modify_time", new UFDateTime(format_datetime.format(new Date())));
				} else {
					superVO.setAttributeValue("modify_time", format_datetime.format(new Date()));
				}
				sb.append(",modify_time=");
				sb.append("'");
				sb.append(superVO.getAttributeValue("modify_time"));
				sb.append("'");
			}

			Field field_modify_user = ReflectionUtils.getDeclaredField(superVO, "modify_user");
			if(field_modify_user != null) {
				if(WebUtils.getLoginInfo() != null) {
					superVO.setAttributeValue("modify_user", WebUtils.getLoginInfo().getPk_user());
					sb.append(",modify_user=");
					sb.append("'");
					sb.append(superVO.getAttributeValue("modify_user"));
					sb.append("'");
				}
			}

			sb.append(" where ");
			sb.append(superVO.getPKFieldName());
			sb.append("=");

			field = ReflectionUtils.getDeclaredField(superVO, superVO.getPKFieldName());
			if(field.getType().equals(String.class) || field.getType().equals(UFDate.class)
					|| field.getType().equals(UFTime.class) || field.getType().equals(UFDateTime.class)
					|| field.getType().equals(UFBoolean.class)) {
				if(superVO.getPrimaryKey() != null) {
					sb.append("'");
					sb.append(superVO.getPrimaryKey());
					sb.append("'");
				} else {
					sb.append("null");
				}
			} else {
				sb.append(superVO.getPrimaryKey());
			}
			return sb.toString();
		}
	}

	/**
	 * 生成删除脚本<br>
	 * 2011-4-9
	 */
	public static List<String> getDeleteSQL(AggregatedValueObject billVO, boolean isLogicalDelete) {
		if(billVO == null) {
			return null;
		}

		List<String> sqlList = new ArrayList<String>();

		// 解析主表
		if(billVO.getParentVO() != null) {
			String sql = DaoHelper.getDeleteSQL((SuperVO) billVO.getParentVO(), isLogicalDelete);
			if(sql != null) {
				sqlList.add(sql);
			}
		}

		// 解析子表
		if(billVO instanceof IExAggVO) {
			// 多子表
			SuperVO[] items = (SuperVO[]) ((IExAggVO) billVO).getAllChildrenVO();
			if(items != null && items.length > 0) {
				for(SuperVO vo : items) {
					String sql = DaoHelper.getDeleteSQL(vo, isLogicalDelete);
					if(sql != null) {
						sqlList.add(sql);
					}
				}
			}
		} else {
			// 单子表
			if(billVO.getChildrenVO() != null && billVO.getChildrenVO().length > 0) {
				// 解析子表
				for(CircularlyAccessibleValueObject vo : billVO.getChildrenVO()) {
					String sql = DaoHelper.getDeleteSQL((SuperVO) vo, isLogicalDelete);
					if(sql != null) {
						sqlList.add(sql);
					}
				}
			}
		}

		return sqlList;
	}

	public static List<String> getDeleteSQL(SuperVO[] superVOs, boolean isLogicalDelete) {
		if(superVOs == null) {
			return null;
		}

		List<String> sqlList = new ArrayList<String>();
		for(SuperVO vo : superVOs) {
			String sql = DaoHelper.getDeleteSQL(vo, isLogicalDelete);
			if(sql != null) {
				sqlList.add(sql);
			}
		}

		return sqlList;
	}

	public static List<String> getDeleteSQL(List<SuperVO> superVOList, boolean isLogicalDelete) {
		if(superVOList == null) {
			return null;
		}

		List<String> sqlList = new ArrayList<String>();
		for(SuperVO vo : superVOList) {
			String sql = DaoHelper.getDeleteSQL(vo, isLogicalDelete);
			if(sql != null) {
				sqlList.add(sql);
			}
		}

		return sqlList;
	}

	/**
	 * 获取superVO相应的更新sql
	 * 
	 * @param vo
	 * @param isLogicalDelete
	 * @return
	 */
	public static String getUpdateSQL(BaseDao baseDao, SuperVO vo, boolean isLogicalDelete) {
		return getUpdateSQL(baseDao, vo, isLogicalDelete, true);
	}
	
	public static String getUpdateSQL(BaseDao baseDao, SuperVO vo, boolean isLogicalDelete, boolean isChecked) {
		if(vo == null || vo.getStatus() == VOStatus.UNCHANGED) {
			return null;
		} else if(vo.getStatus() == VOStatus.NEW) {
			if(vo.getPKFieldName() != null && StringUtils.isBlank(vo.getPrimaryKey())) {
				throw new BusiException("新增时主键不能为空！");
			} else {
				// 没有主键
			}
			
			// 设置默认值
			Field field_ts = ReflectionUtils.getDeclaredField(vo, "ts");
			if(field_ts != null) {
				if(field_ts.getType().equals(UFDateTime.class)) {
					vo.setAttributeValue("ts", new UFDateTime(format_datetime.format(new Date())));
				} else {
					vo.setAttributeValue("ts", format_datetime.format(new Date()));
				}
			}
			if(ReflectionUtils.getDeclaredField(vo, "dr") != null) {
				vo.setAttributeValue("dr", 0);
			}
			/**
			 * 获取数据库表信息
			 */
			Set<String> tableColumnNames;
			if(tableColumnNamesCacheMap.get(vo.getTableName()) != null) {
				tableColumnNames = tableColumnNamesCacheMap.get(vo.getTableName());
			} else {
				tableColumnNames = getTableColumnNames(baseDao, vo.getTableName());
				tableColumnNamesCacheMap.put(vo.getTableName(), tableColumnNames);
			}

			/**
			 * 拼接sql
			 */
			StringBuilder colNames = new StringBuilder();
			StringBuilder values = new StringBuilder();
			for(String key : vo.getAttributeNames()) {
				Field field = ReflectionUtils.getDeclaredField(vo, key);
				// 这里要判断该字段在vo中存在，而且在数据库表中也存在
				if(field != null && tableColumnNames.contains(key)) {
					// FIXME XUQC 2013-3-19,插入时，如果是null值，则不加入
					Object val = vo.getAttributeValue(key);
					if(val != null) {
						if(colNames.length() > 0) {
							colNames.append(",");
							values.append(",");
						}

						colNames.append(key);
						if(field.getType().equals(String.class) || field.getType().equals(UFDate.class)
								|| field.getType().equals(UFTime.class) || field.getType().equals(UFDateTime.class)
								|| field.getType().equals(UFBoolean.class)) {
							values.append("'");
							// 如果有单引号，要转成两个单引号，以进行转义
							// mysql好像要用\'转义，暂时先不考虑
							values.append(val.toString().replace("'", "''"));
							values.append("'");
						} else {
							values.append(val);
						}
					}
				}
			}

			StringBuilder sb = new StringBuilder("insert into ");
			sb.append(vo.getTableName());
			sb.append(" (");
			sb.append(colNames);
			sb.append(") values (");
			sb.append(values);
			sb.append(")");
			return sb.toString();
		} else if(vo.getStatus() == VOStatus.DELETED) {
			return DaoHelper.getDeleteSQL(vo, isLogicalDelete);
		} else if(vo.getStatus() == VOStatus.UPDATED) {
			if(StringUtils.isBlank(vo.getPrimaryKey())) {
				throw new BusiException("更新时主键不能为空！");
			}
			
			// 设置默认值
			Field field_ts = ReflectionUtils.getDeclaredField(vo, "ts");
			if(field_ts != null) {
				//验证数据库的ts和将要保存的数据的ts是否一致，如果不一致，则说明这条数据被修改过了，不允许改动。应对并发问题。
				if(isChecked){
					String ts_value = String.valueOf(vo.getAttributeValue("ts"));
					String ts_db = new String();
					//获取数据库里的ts
					if(DB_TYPE.SQLSERVER.equals(NWDao.getCurrentDBType())){
						String sql = "select ts from " + vo.getTableName() +" WITH(NOLOCK) where "
									+ vo.getPKFieldName() +" =?";
						ts_db = NWDao.getInstance().queryForObject(sql, String.class,  vo.getPrimaryKey());
					}
					if(!ts_db.equals(ts_value)){
						throw new BusiException("数据[?]已被改动，请重新操作！",vo.getTableName());
					}
				}else{
					logger.info("没有TS校验," + vo.getTableName() +  ":" + vo.getPKFieldName());
				}
				if(field_ts.getType().equals(UFDateTime.class)) {
					vo.setAttributeValue("ts", new UFDateTime(format_datetime.format(new Date())));
				} else {
					vo.setAttributeValue("ts", format_datetime.format(new Date()));
				}
			}
			Field field_modify_time = ReflectionUtils.getDeclaredField(vo, "modify_time");
			if(field_modify_time != null) {
				if(field_modify_time.getType().equals(UFDateTime.class)) {
					vo.setAttributeValue("modify_time", new UFDateTime(format_datetime.format(new Date())));
				} else {
					vo.setAttributeValue("modify_time", format_datetime.format(new Date()));
				}
			}
			Field field_modify_user = ReflectionUtils.getDeclaredField(vo, "modify_user");
			if(field_modify_user != null) {
				if(WebUtils.getLoginInfo() != null) {
					vo.setAttributeValue("modify_user", WebUtils.getLoginInfo().getPk_user());
				}
			}

			StringBuilder sb = new StringBuilder("update ");
			sb.append(vo.getTableName());
			sb.append(" set ");

			/**
			 * 获取数据库表信息
			 */
			Set<String> tableColumnNames;
			if(tableColumnNamesCacheMap.get(vo.getTableName()) != null) {
				tableColumnNames = tableColumnNamesCacheMap.get(vo.getTableName());
			} else {
				tableColumnNames = getTableColumnNames(baseDao, vo.getTableName());
				tableColumnNamesCacheMap.put(vo.getTableName(), tableColumnNames);
			}

			boolean isFirst = true;
			for(String key : vo.getAttributeNames()) {
				Field field = ReflectionUtils.getDeclaredField(vo, key);
				if(field != null && tableColumnNames.contains(key)) {
					if(!isFirst) {
						sb.append(",");
					} else {
						isFirst = false;
					}
					sb.append(key);
					sb.append("=");
					if(field.getType().equals(String.class) || field.getType().equals(UFDate.class)
							|| field.getType().equals(UFTime.class) || field.getType().equals(UFDateTime.class)
							|| field.getType().equals(UFBoolean.class)) {
						if(vo.getAttributeValue(key) != null) {
							sb.append("'");
							// 如果有单引号，要转成两个单引号，以进行转义
							// mysql好像要用\'转义，暂时先不考虑
							sb.append(vo.getAttributeValue(key).toString().replace("'", "''"));
							sb.append("'");
						} else {
							sb.append("null");
						}
					} else {
						sb.append(vo.getAttributeValue(key));
					}
				}
			}

			sb.append(" where ");
			sb.append(vo.getPKFieldName());
			sb.append("=");

			Field field = ReflectionUtils.getDeclaredField(vo, vo.getPKFieldName());
			if(field.getType().equals(String.class) || field.getType().equals(UFDate.class)
					|| field.getType().equals(UFTime.class) || field.getType().equals(UFDateTime.class)
					|| field.getType().equals(UFBoolean.class)) {
				if(vo.getPrimaryKey() != null) {
					sb.append("'");
					sb.append(vo.getPrimaryKey());
					sb.append("'");
				} else {
					sb.append("null");
				}
			} else {
				sb.append(vo.getPrimaryKey());
			}
			return sb.toString();
		}
		return null;
	}

	private static Set<String> getTableColumnNames(BaseDao baseDao, String tableName) {
		Set<String> tableColumnNames = new HashSet<String>();

		String sql = "select * from " + tableName + " WITH(NOLOCK) where 1<>1";
		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sql = "select * from " + tableName + " where 1<>1";
		}
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = baseDao.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);
			ResultSetMetaData rsmd = ps.executeQuery().getMetaData();
			for(int i = 0; i < rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnName(i + 1);
				if(columnName != null) {
					tableColumnNames.add(columnName.toLowerCase());
				}
			}
			return tableColumnNames;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if(ps != null) {
				try {
					ps.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
			if(conn != null) {
				try {
					conn.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取聚合vo相应的更新sql
	 * 
	 * @param billVO
	 *            聚合vo
	 * @param isLogicalDelete
	 * @return
	 */
	public static List<String> getUpdateSQL(BaseDao baseDao, AggregatedValueObject billVO, boolean isLogicalDelete) {
		if(billVO == null) {
			return null;
		}

		List<String> sqlList = new ArrayList<String>();

		/**
		 * 解析主表
		 */
		if(billVO.getParentVO() != null) {
			String sql = DaoHelper.getUpdateSQL(baseDao, (SuperVO) billVO.getParentVO(), isLogicalDelete);
			if(sql != null) {
				sqlList.add(sql);
			}
		}

		/**
		 * 解析子表
		 */
		if(billVO instanceof IExAggVO) {
			// 多子表
			SuperVO[] items = (SuperVO[]) ((IExAggVO) billVO).getAllChildrenVO();
			if(items != null && items.length > 0) {
				for(SuperVO vo : items) {
					String sql = DaoHelper.getUpdateSQL(baseDao, vo, isLogicalDelete);
					if(sql != null) {
						sqlList.add(sql);
					}
				}
			}
		} else {
			// 单子表
			if(billVO.getChildrenVO() != null && billVO.getChildrenVO().length > 0) {
				// 解析子表
				for(CircularlyAccessibleValueObject vo : billVO.getChildrenVO()) {
					String sql = DaoHelper.getUpdateSQL(baseDao, (SuperVO) vo, isLogicalDelete);
					if(sql != null) {
						sqlList.add(sql);
					}
				}
			}
		}

		return sqlList;
	}

	/**
	 * 获取聚合vo相应的更新sql
	 * 
	 * @param superVOArray
	 * @param isLogicalDelete
	 * @return
	 */
	public static List<String> getUpdateSQL(BaseDao baseDao, SuperVO[] superVOArray, boolean isLogicalDelete) {
		if(superVOArray == null) {
			return null;
		}

		List<String> sqlList = new ArrayList<String>();
		for(SuperVO superVO : superVOArray) {
			String sql = DaoHelper.getUpdateSQL(baseDao, superVO, isLogicalDelete);
			if(sql != null && sql.length() > 0) {
				sqlList.add(sql);
			}
		}
		return sqlList;
	}

	public static List<String> getUpdateSQL(BaseDao baseDao, List<SuperVO> superVOArray, boolean isLogicalDelete) {
		return getUpdateSQL(baseDao, superVOArray, isLogicalDelete, true);
	}
	
	public static List<String> getUpdateSQL(BaseDao baseDao, List<SuperVO> superVOArray, boolean isLogicalDelete, boolean isCkeck) {
		if(superVOArray == null) {
			return null;
		}

		List<String> sqlList = new ArrayList<String>();
		for(SuperVO superVO : superVOArray) {
			String sql = DaoHelper.getUpdateSQL(baseDao, superVO, isLogicalDelete,isCkeck);
			if(sql != null && sql.length() > 0) {
				sqlList.add(sql);
			}
		}
		return sqlList;
	}

	/**
	 * 自动处理DR和封存字段
	 * 
	 * @param voClass
	 * @param strWhere
	 * @return
	 */
	public static String getWhereClause(Class<? extends SuperVO> voClass, String strWhere) {
		// 增加判断是否删除
		Object obj = ReflectionUtils.findField(voClass, "dr");
		if(obj != null) {
			// 如果有dr字段

			// 获取表名，所有字段名都要加上表前缀，避免关联表字段相同的情况
			SuperVO vo = null;
			try {
				vo = voClass.newInstance();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}

			if(strWhere != null && strWhere.length() != 0) {
				if(strWhere.trim().toUpperCase().startsWith("ORDER ")) {
					strWhere = "isnull(" + vo.getTableName() + ".dr,0)=0 " + strWhere;
				} else {
					strWhere = "isnull(" + vo.getTableName() + ".dr,0)=0 and " + strWhere;
				}
			} else {
				strWhere = "isnull(" + vo.getTableName() + ".dr,0)=0";
			}
		}

		return strWhere;
	}

	/**
	 * 返回存储过程的调用字符串
	 * 
	 * @param procName
	 *            存储过程名称
	 * @param prametCount
	 *            参数个数
	 * @return
	 * @throws Exception
	 */
	public static String getProcedureCallName(String procName, int prametCount) {
		if(prametCount == 0){
			return "{call " + procName + "}";
		}
		String procedureCallName = "{call " + procName;
		for(int i = 0; i < prametCount; i++) {
			if(0 == i) {
				procedureCallName = procedureCallName + "(?";
			}
			if(0 != i) {
				procedureCallName = procedureCallName + ",?";
			}
		}
		procedureCallName = procedureCallName + ")}";
		return procedureCallName;
	}
}
