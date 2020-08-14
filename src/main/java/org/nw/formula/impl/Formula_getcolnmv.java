package org.nw.formula.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nw.dao.NWDao;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.formula.AbstractFormula;
import org.nw.formula.IFormula;

/**
 * getColNmv(tablename,fieldname,pkfield,pkvalue)
 * 根据主键从数据库查询特定字段的值,其返回的值将直接作为数字使用。 其功能类似SQL语句:select fieldname from tablename
 * where pkfield = pkvalue 从这条SQL语句可以看出各个参数的含义.
 * 
 * @author fangw
 */
public class Formula_getcolnmv extends AbstractFormula {

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

		if(argList.get(3) instanceof IFormula) {
			// 如果第4个参数是子公式，则先执行子公式
			IFormula formula = (IFormula) argList.get(3);
			argList.set(3, formula.getResult(rowContext));
		} else {
			// 如果第4个参数不是子公式，则把变量翻译为值
			if(argList.get(3) != null) {
				if(argList.get(3).toString().startsWith("\"") && argList.get(3).toString().endsWith("\"")) {
					String t = argList.get(3).toString();
					t = t.substring(1);
					t = t.substring(0, t.length() - 1);
					argList.set(3, t);
				} else {
					argList.set(3, rowContext.get(argList.get(3)));
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
		sb.append(tableName);

		if(DB_TYPE.ORACLE.equals(NWDao.getCurrentDBType())){
			sb.append(" where ");
		}else{
			sb.append(" WITH(NOLOCK) where ");
		}

		// 参数名
		String param = argList.get(2).toString();
		if(param.startsWith("\"")) {
			param = param.substring(1);
		}
		if(param.endsWith("\"")) {
			param = param.substring(0, param.length() - 1);
		}
		sb.append(param);

		if(argList.get(3) == null) {
			sb.append(" is null");
		} else {
			sb.append("=?");
		}
		logger.debug("公式sql=" + sb.toString());

		List<Map<String, Object>> list = null;
		try {
			if(argList.get(3) == null) {
				logger.debug("公式参数为null");
				list = this.getDao().queryForListWithCache(sb.toString());
			} else {
				logger.debug("公式参数" + argList.get(3) + "=" + argList.get(3));
				list = this.getDao().queryForListWithCache(sb.toString(), argList.get(3));
			}
		} catch(Exception e) {
			logger.error("公式执行错误，可能书写错误，公式为：" + this.getFormulaStr(), e);
		}

		if(list != null && list.size() > 0) {
			// 找到结果
			Map<String, Object> map = list.get(0);// 如果有多个，只取第一个
			Object value = map.get(fieldName.toLowerCase());
			logger.debug("公式执行结果：" + value);
			if(value == null) {
				return value;
			} else {
				if(value.toString().contains(".")) {
					return Double.valueOf(value.toString());
				} else {
					return Integer.valueOf(value.toString());
				}
			}
		} else {
			// 没有找到结果
			return null;
		}
	}

}