package org.nw.formula.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * getColsValuemorewithcond公式
 * 这里的whereCondition是附加条件，不能使用变量，如果需要用变量，尽量在程序中指定，比如当前公司，在程序中拼入
 * 
 * <pre>
 * 格式示例：vadmindeptname,pk_unit->getColsValuemorewithcond("bd_deptdoc","deptname","def3","pk_deptdoc",vadmindeptid,whereCondition)
 * </pre>
 * 
 * @author fangw
 */
public class Formula_getcolsvaluemorewithcond extends AbstractFormula {

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

		int paramIndex = argList.size() - 2;
		Object paramObj = argList.get(paramIndex);
		// 处理子公式
		if(paramObj instanceof IFormula) {
			// 如果参数是子公式，则先执行子公式
			IFormula formula = (IFormula) paramObj;
			argList.set(paramIndex, formula.getResult(rowContext));
		} else {
			// 如果参数不是子公式，则把变量翻译为值
			if(paramObj != null) {
				if(paramObj.toString().startsWith("\"") && paramObj.toString().endsWith("\"")) {
					String t = paramObj.toString();
					t = t.substring(1);
					t = t.substring(0, t.length() - 1);
					argList.set(paramIndex, t);
				} else {
					argList.set(paramIndex, rowContext.get(paramObj));
				}
			}
		}

		// 判断是否使用缓存
		// XXX 现在已经不再使用这个，公式全部用带缓存的查询，但查询缓存作用域仅限于此次公式解析
		// boolean isUseCache = true;
		if(ArgName_noCache.equalsIgnoreCase(this.getArgs().get(this.getArgs().size() - 1).toString())) {
			// isUseCache = false;
			argList.remove(argList.size() - 1);

			// 处理子公式
			if(argList.get(argList.size() - 1) instanceof IFormula) {
				// 如果参数是子公式，则先执行子公式
				IFormula formula = (IFormula) argList.get(argList.size() - 1);
				argList.set(argList.size() - 1, formula.getResult(rowContext));
			} else {
				// 如果参数不是子公式，则把变量翻译为值
				argList.set(argList.size() - 1, rowContext.get(argList.get(argList.size() - 1)));
			}
		}

		/**
		 * 执行公式
		 */
		// 生成sql
		StringBuilder sb = new StringBuilder();
		sb.append("select ");

		// 查询的字段
		for(int i = 1; i < argList.size() - 3; i++) {
			if(i > 1) {
				sb.append(",");
			}
			String fieldName = argList.get(i).toString();
			if(fieldName.startsWith("\"")) {
				fieldName = fieldName.substring(1);
			}
			if(fieldName.endsWith("\"")) {
				fieldName = fieldName.substring(0, fieldName.length() - 1);
			}
			sb.append(fieldName);
		}

		sb.append(" from ");

		// 表名
		String tableName = argList.get(0).toString();
		if(tableName.startsWith("\"")) {
			tableName = tableName.substring(1);
		}
		if(tableName.endsWith("\"")) {
			tableName = tableName.substring(0, tableName.length() - 1);
		}
		sb.append(tableName);

		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sb.append(" where ");
		}else{
			sb.append(" WITH(NOLOCK) where ");
		}

		// 参数名
		String param = argList.get(argList.size() - 3).toString();
		if(param.startsWith("\"")) {
			param = param.substring(1);
		}
		if(param.endsWith("\"")) {
			param = param.substring(0, param.length() - 1);
		}
		sb.append(param);

		// 参数设值
		if(argList.get(argList.size() - 2) == null) {
			sb.append(" is null");
		} else {
			sb.append("=?");
		}

		param = argList.get(argList.size() - 1).toString();
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
			if(argList.get(argList.size() - 2) == null) {
				logger.debug("公式参数为null");
				try {
					list = this.getDao().queryForListWithCache(sb.toString());
				} catch(Exception e) {
					logger.error("公式执行错误：" + this.getFormulaStr(), e);
					return null;
				}
			} else {
				logger.debug("公式参数:" + argList.get(argList.size() - 2));
				list = this.getDao().queryForListWithCache(sb.toString(), argList.get(argList.size() - 2));
			}
		} catch(Exception e) {
			logger.error("公式执行错误，可能书写错误，公式为：" + this.getFormulaStr(), e);
		}

		if(list != null && list.size() > 0) {
			// 找到结果
			Map<String, Object> map = list.get(0);// 如果有多个，只取第一个

			Map<String, Object> result = new HashMap<String, Object>();
			String[] outArgs = this.getOutArgName().split(",");

			for(int i = 1; i < argList.size() - 3; i++) {
				String fieldName = argList.get(i).toString();
				if(fieldName.startsWith("\"")) {
					fieldName = fieldName.substring(1);
				}
				if(fieldName.endsWith("\"")) {
					fieldName = fieldName.substring(0, fieldName.length() - 1);
				}
				Object value = map.get(fieldName.toLowerCase());
				if(value != null && value instanceof BigDecimal) {
					value = ((BigDecimal) value).toPlainString();
				}
				logger.debug("公式执行结果" + i + "：" + value);
				result.put(outArgs[i - 1], value);
			}
			return result;
		} else {
			// 没有找到结果
			return null;
		}
	}

}
