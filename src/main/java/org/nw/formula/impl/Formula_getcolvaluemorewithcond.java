package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * getcolvaluemorewithcond公式
 * 
 * <pre>
 * 格式：getColValueMoreWithCond(tablename,selectfield,field1,value1,field2,value2...,whereCondition)
 * 示例：pk_accbank->getcolvaluemorewithcond(\"bd_custbank\", \"pk_accbank\", \"pk_cubasdoc\", vendorbasid, \"pk_corp\", pk_corp,\"isnull(defflag,'N')='Y'and pk_corp = '0001'\")
 * </pre>
 * 
 * @author fangw
 */
public class Formula_getcolvaluemorewithcond extends AbstractFormula {

	/**
	 * 获取公式执行结果
	 * 
	 * @param rowContext
	 * @return
	 */
	public Object getResult(Map<String, Object> rowContext) {
		logger.debug("开始执行公式：" + this.getFormulaStr());

		List<Object> argList = new ArrayList<Object>(this.getArgs().size());
		argList.addAll(this.getArgs());

		for(int i = 3; i < argList.size(); i++) {
			if((i % 2) != 0) {
				if(argList.get(i) instanceof IFormula) {
					// 如果第i个参数是子公式，则先执行子公式
					IFormula formula = (IFormula) argList.get(i);
					argList.set(i, formula.getResult(rowContext));
				} else {
					// 如果第i个参数不是子公式，则把变量翻译为值
					if(argList.get(i) != null) {
						if(argList.get(i).toString().startsWith("\"") && argList.get(i).toString().endsWith("\"")) {
							String t = argList.get(i).toString();
							t = t.substring(1);
							t = t.substring(0, t.length() - 1);
							argList.set(i, t);
						} else {
							argList.set(i, rowContext.get(argList.get(i)));
						}
					}
				}
			}
		}

		// 判断是否使用缓存
		// XXX 现在已经不再使用这个，公式全部用带缓存的查询，但查询缓存作用域仅限于此次公式解析
		// boolean isUseCache = true;
		if(ArgName_noCache.equalsIgnoreCase(this.getArgs().get(this.getArgs().size() - 1).toString())) {
			// isUseCache = false;
			argList.remove(argList.size() - 1);
		}

		// 执行公式
		StringBuilder sb = new StringBuilder();
		sb.append("select ");

		// 查询的字段
		String fieldName = argList.get(1).toString();
		if(fieldName.startsWith("\"")) {
			fieldName = fieldName.substring(1);
		}
		if(fieldName.endsWith("\"")) {
			fieldName = fieldName.substring(0, fieldName.length() - 1);
		}
		sb.append(fieldName);

		sb.append(" from ");

		// 表名
		String tableName = argList.get(0).toString();
		if(tableName.startsWith("\"")) {
			tableName = tableName.substring(1);
		}
		if(tableName.endsWith("\"")) {
			tableName = tableName.substring(0, tableName.length() - 1);
		}
		//yaojiie 2015 12 08添加 WITH (NOLOCK)
		sb.append(tableName);if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sb.append(" where ");
		}else{
			sb.append(" WITH(NOLOCK) where ");
		}

		// 条件处理
		List<Object> sqlArg = new ArrayList<Object>();
		boolean isFirst = true;
		for(int i = 2; i < (argList.size() - 1); i++) {
			if((i % 2) == 0) {
				if(isFirst) {
					isFirst = false;
				} else {
					sb.append(" and ");
				}
				// 参数名称
				String param = argList.get(i).toString();
				if(param.startsWith("\"") && param.endsWith("\"")) {
					param = param.substring(1, param.length() - 1);
				}
				sb.append(param);

				if(argList.get(i + 1) == null) {
					sb.append(" is null");
				} else {
					sb.append("=?");
				}
			} else {
				// 参数值
				if(argList.get(i) != null) {
					sqlArg.add(argList.get(i));
				}
			}
		}

		// 附加条件(最后一个)
		String param = argList.get(argList.size() - 1).toString();
		if(param.startsWith("\"") && param.endsWith("\"")) {
			param = param.substring(1, param.length() - 1);
		}
		if(param.trim().length() > 0) {
			sb.append(" and ");
		}
		sb.append(param);
		logger.debug("公式sql=" + sb.toString());

		List<Map<String, Object>> list = null;
		try {
			if(sqlArg.size() == 0) {
				list = this.getDao().queryForListWithCache(sb.toString());
			} else {
				list = this.getDao().queryForListWithCache(sb.toString(), sqlArg.toArray());
			}
		} catch(Exception e) {
			logger.error("公式执行错误，可能书写错误，公式为：" + this.getFormulaStr(), e);
		}

		if(list != null && list.size() > 0) {
			// 找到结果
			Map<String, Object> map = list.get(0);// 如果有多个，只取第一个
			logger.debug("公式执行结果：" + map.get(fieldName.toLowerCase()));
			return map.get(fieldName.toLowerCase());
		} else {
			// 没有找到结果
			return null;
		}
	}
}
