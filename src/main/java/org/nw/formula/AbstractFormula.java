package org.nw.formula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.dao.NWDao;
import org.nw.dao.AbstractDao.DB_TYPE;
import org.nw.exception.BusiException;
import org.nw.utils.NWUtils;

import com.tms.constants.TransTypeConst;

/**
 * 公式抽象类 所有公式都从该类继承，
 * 
 * @author fangw
 */
public abstract class AbstractFormula implements IFormula {
	protected Log logger = LogFactory.getLog(getClass());
	private FormulaDao dao = null;
	private List<Object> args = new ArrayList<Object>();
	private String outArgName;
	private String formulaStr;
	public static String ArgName_noCache = "__nocache";

	public FormulaDao getDao() {
		return dao;
	}

	public void setDao(FormulaDao dao) {
		this.dao = dao;
	}

	public void addArgs(Object args) {
		this.args.add(args);
	}

	public List<Object> getArgs() {
		return args;
	}

	public String getFormulaStr() {
		return formulaStr;
	}

	public String getOutArgName() {
		return outArgName;
	}

	/**
	 * 获取公式执行结果 context必须是List<Map<String,Object>>
	 * 
	 * @param rowContext
	 *            行上下文
	 * @return
	 */
	public abstract Object getResult(Map<String, Object> rowContext);

	public void setArgs(List<Object> args) {
		this.args = args;
	}

	public void setFormulaStr(String formulaStr) {
		this.formulaStr = formulaStr;
	}

	public void setOutArgName(String outArgName) {
		this.outArgName = outArgName;
	}

	/**
	 * 获取公式名称 注:该方法并不对公式解析和执行造成影响，只是为了让公式对象描述更加清晰
	 */
	public String getFormulaName() {
		String name = this.getClass().getName();
		String p = "Formula_";
		return name.substring(name.indexOf(p) + p.length());
	}
	
	//yaojiie 2015 12 08 一次性将所有结果获取到，避免多次访问数据库。
	public List<Map<String,Object>> getColValueResult(List<Map<String,Object>> contexts){
		logger.debug("开始执行公式：" + this.getFormulaStr());
		List<Object> argList = new ArrayList<Object>(this.getArgs().size());
		Set<Object> params = new HashSet<Object>();

		argList.addAll(this.getArgs());
		for (Map<String, Object> rowContext : contexts) {
			if (argList.get(3) instanceof IFormula) {
				// 如果第4个参数是子公式，则先执行子公式(此处其实不执行，不支持子公式类型。)
				throw new BusiException("不支持[?]含有子公式类型显示公式，请正确填写！",this.getFormulaStr());
			} else {
				// 如果第4个参数不是子公式，则把变量翻译为值
				if (argList.get(3) != null) {
					if (argList.get(3).toString().startsWith("\"") && argList.get(3).toString().endsWith("\"")) {
						String t = argList.get(3).toString();
						t = t.substring(1, t.length() - 1);
							params.add(t);
						
					} else {
							params.add(rowContext.get(argList.get(3)));
					}
				}
			}
		}
		if (ArgName_noCache.equalsIgnoreCase(this.getArgs().get(this.getArgs().size() - 1).toString())) {
			// isUseCache = false;
			argList.remove(argList.size() - 1);
		}

		// 执行公式
		StringBuilder sb = new StringBuilder();
		sb.append("select ");

		// 查询的字段
		String fieldName = argList.get(1).toString();
		String fieldName1 = argList.get(2).toString();
		if (fieldName.startsWith("\"")) {
			fieldName = fieldName.substring(1);
		}
		if (fieldName.endsWith("\"")) {
			fieldName = fieldName.substring(0, fieldName.length() - 1);
		}
		sb.append(fieldName);
		sb.append(",").append(fieldName1);

		sb.append(" from ");

		// 表名
		String tableName = argList.get(0).toString();
		if (tableName.startsWith("\"")) {
			tableName = tableName.substring(1);
		}
		if (tableName.endsWith("\"")) {
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
		if (param.startsWith("\"")) {
			param = param.substring(1);
		}
		if (param.endsWith("\"")) {
			param = param.substring(0, param.length() - 1);
		}
		sb.append(param);

		if (argList.get(3) == null) {
			sb.append(" is null");
		} else {
			sb.append(" in ");
		}
		logger.debug("公式sql=" + sb.toString());

		List<Map<String, Object>> list = null;

		try {
			if (params == null || params.size() == 0) {
				logger.debug("公式参数为null");
				list = this.getDao().queryForListWithCache(sb.toString());
			} else {
				logger.debug("公式参数=" + params);
				String cond = NWUtils.buildConditionString(params.toArray(new String[params.size()]));
				String sql = sb.toString() + cond;
				list = this.getDao().queryForListWithCache(sql);
			}
		} catch (Exception e) {
			logger.error("公式执行错误，可能书写错误，公式为：" + this.getFormulaStr(), e);
		}
		return list;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getAllResults(List<Map<String, Object>> contexts, IFormula formula) {
		List<Map<String, Object>> list = null;

		List<Object> argList = new ArrayList<Object>(this.getArgs().size());
		argList.addAll(this.getArgs());
		this.getArgs();
		list = getColValueResult(contexts);
		if (list != null && list.size() > 0) {
			for (Map<String, Object> map : list) {
				for (Map<String, Object> rowMap : contexts) {
					if (rowMap.containsKey(argList.get(3)) && rowMap.get(argList.get(3)) != null
							&& rowMap.get(argList.get(3)).equals(map.get(argList.get(2)))) {
						Object result = map.get(argList.get(1));
						if (result instanceof Map
								|| (formula.getOutArgName() != null && formula.getOutArgName().contains(","))) {
							// 多个返回值（如:getColsValue）
							if (result == null) {
								String[] arr = formula.getOutArgName().split(",");
								for (String argName : arr) {
									rowMap.put(argName.trim(), null);
								}
							} else {
								if (formula.getOutArgName() != null) {
									rowMap.putAll((Map) result);
								}
							}
						} else {
							if (formula.getOutArgName() != null) {
								// 如果有输出参数，把相应数据填回行数据context（如果没有？那就没必要执行公式了！）
								rowMap.put(formula.getOutArgName().trim(), result);
							} else {
								// log.warn("公式输出参数名称为空，该公式结果将被忽略，公式为："
								// + formula.getFormulaStr());
							}
						}
					}
				}
			}

		}

	}

}
