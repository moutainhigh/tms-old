package org.nw.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.exception.BusiException;
import org.nw.formula.impl.Formula_;
import org.nw.formula.impl.Formula_arithmetic;
import org.nw.formula.impl.Formula_getcolvalue;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.pub.lang.UFNumberFormat;
import org.nw.vo.pub.lang.UFTime;

/**
 * WEB端公式解析器 注： 1、该解析器不依赖任何NC后台组件，全前台解析，使用了查询缓存，速度极快
 * 2、目前并不支持公式合并，因为我认为提升效率的关键在缓存，而且公式的合并，会带来糟糕的设计。 3、目前由于时间不够，仅实现了部分公式，其它以后用到时再实现
 * 
 * @author fangw
 */
public class FormulaParser {
	public static final String base_prefix = "org.nw.formula.impl.Formula_";
	private static final Log log = LogFactory.getLog(FormulaUtils.class);
	private static ConcurrentHashMap<String, Class<?>> classCacheMap = new ConcurrentHashMap<String, Class<?>>();
	private DataSource dataSource = null;
	private List<String> formulas = null;
	private List<Map<String, Object>> context = null;
	private List<Object> args = new ArrayList<Object>();

	/**
	 * 是否把输入context与公式结果合并<br>
	 * 另，如果公式间存在前后引用，必须用合并方式
	 */
	private boolean mergeContextToResult = true;// 默认合并
	private FormulaDao dao = null;

	public FormulaParser() {
		log.warn("警告：公式解析器没有传入数据源，在遇到类似getColValue这种需要依赖数据库查询的公式时将出错！");
	}

	public FormulaParser(DataSource dataSource) {
		this.setDataSource(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * XXX 通过每次创建一个dao实例，dao实例中包括了缓存，这样保证在每次执行公式时可以使用单个事务的缓存
	 * 
	 * @param dataSource
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.dao = new FormulaDao(this.getDataSource());
	}

	public boolean isMergeContextToResult() {
		return mergeContextToResult;
	}

	public void setMergeContextToResult(boolean mergeContextToResult) {
		this.mergeContextToResult = mergeContextToResult;
	}

	public List<String> getFormulas() {
		return formulas;
	}

	public void setFormulas(String[] formulas) {
		if (formulas != null) {
			if (this.formulas == null) {
				this.formulas = new ArrayList<String>();
			}
			for (String formula : formulas) {
				this.formulas.add(formula);
			}
		}
	}

	public void setFormulas(List<String> formulas) {
		this.formulas = formulas;
	}

	public void addFormula(String formula) {
		if (this.formulas == null) {
			this.formulas = new ArrayList<String>();
		}
		this.formulas.add(formula);
	}

	public List<Map<String, Object>> getContext() {
		return context;
	}
	
	public List<Object> getArgs() {
		return args;
	}
	
	public void setArgs(List<Object> args) {
		this.args = args;
	}

	/**
	 * 设置数据context context最终必须是List<Map<String,Object>>格式
	 * 
	 * @param context
	 */
	public void setContext(List<Map<String, Object>> context) {
		this.context = context;
		this.checkContext();
	}

	public void setVOContext(CircularlyAccessibleValueObject[] context) {
		if (context != null) {
			this.context = new ArrayList<Map<String, Object>>();
			for (CircularlyAccessibleValueObject vo : context) {
				// ת��ΪList<Map<String,Object>
				Map<String, Object> map = new HashMap<String, Object>();
				for (String key : vo.getAttributeNames()) {
					map.put(key, vo.getAttributeValue(key));
				}
				this.context.add(map);
			}
		}

		// У��ת��context
		this.checkContext();
	}

	public void setVOContext(
			List<? extends CircularlyAccessibleValueObject> context) {
		if (context != null) {
			this.context = new ArrayList<Map<String, Object>>();
			for (CircularlyAccessibleValueObject vo : context) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (String key : vo.getAttributeNames()) {
					map.put(key, vo.getAttributeValue(key));
				}
				this.context.add(map);
			}
		}

		this.checkContext();
	}

	private void checkContext() {
		if (this.context != null) {
			for (Map<String, Object> map : this.context) {
				for (String key : map.keySet()) {
					Object value = map.get(key);
					if (value != null) {
						if (value instanceof UFBoolean) {
							value = ((UFBoolean) value).booleanValue();
							map.put(key, value);
						} else if (value instanceof UFDate) {
							value = ((UFDate) value).toString();
							map.put(key, value);
						} else if (value instanceof UFDateTime) {
							value = ((UFDateTime) value).toString();
							map.put(key, value);
						} else if (value instanceof UFDouble) {
							value = ((UFDouble) value).doubleValue();
							map.put(key, value);
						} else if (value instanceof UFNumberFormat) {
							value = ((UFNumberFormat) value).toString();
							map.put(key, value);
						} else if (value instanceof UFTime) {
							value = ((UFTime) value).toString();
							map.put(key, value);
						}
					}
				}
			}
		}
	}

	/**
	 * 解析公式 格式示例：
	 * 
	 * <pre>
	 * pk_billtype_name -&gt; getColValue(bd_billtype, billtypename, pk_billtypecode,
	 * 		pk_billtype)
	 * </pre>
	 * 
	 * 复杂公式：
	 * 
	 * <pre>
	 * remaindercavmny -&gt; floanmny
	 * 		- iif(pk_contrajectloan == null, temp_cavmny, temp_cavmny2)
	 * 		- iif(pk_contrajectloan == null, temp_fhandbackmny, temp_fhandbackmny2)
	 * </pre>
	 * 
	 * @return
	 */
	public IFormula parseFormula(String formulaStr) {
		log.debug("解析公式：" + formulaStr);
		// 解析公式名称(公式全部用小写)
		String formulaName = formulaStr.trim().toLowerCase();
		String outArgName = null;
		if (formulaName.indexOf(";") != -1) {
			// 多个公式
			throw new BusiException("公式解析错误，不应该还有分号！");
		} else {
			// 单个公式
			// 解析输出参数
			if (formulaName.indexOf("->") != -1) {
				outArgName = formulaName
						.substring(0, formulaName.indexOf("->"));
				formulaName = formulaName
						.substring(formulaName.indexOf("->") + 2);
			}
			// eg:iif(indcode==null, null, indcode+" "+indname)
			formulaName = FormulaUtils.getFormulaName(formulaName);
		}
		formulaName = formulaName.trim();

		IFormula oFormula = null;
		try {
			String className = base_prefix + formulaName;
			log.debug("准备实例化公式：" + className);

			Class<?> clazz = null;
			if (classCacheMap.containsKey(className)) {
				clazz = classCacheMap.get(className);
			} else {
				clazz = Class.forName(className);
				classCacheMap.put(className, clazz);
			}

			oFormula = (IFormula) clazz.newInstance();
			oFormula.setFormulaStr(formulaStr.trim());
			// oFormula.setDataSource(this.getDataSource());
			oFormula.setDao(this.dao);
		} catch (ClassNotFoundException e) {
			log.error("不支持该公式：" + formulaStr.trim());
			throw new BusiException("不支持该公式[?]！",formulaStr.trim());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (outArgName != null) {
			oFormula.setOutArgName(outArgName.trim());
		}

		String args = formulaStr.trim();
		if (args.indexOf("->") != -1) {
			args = args.substring(args.indexOf("->") + 2);
		} else {
			// 不需要处理，一般是子公式，已经没有->符号了
		}

		if (oFormula instanceof Formula_) {
			args = args.trim();
			log.debug("解析参数：" + args);

			do {
				if (args.contains("+") || args.contains("-")
						|| args.contains("*") || args.contains("/")
						|| args.contains("(") || args.contains(")")
						|| args.contains("<") || args.contains(">")
						|| args.contains("<=") || args.contains(">=")
						|| args.contains("==") || args.contains("!=")
						|| args.contains("!") || args.contains("||")
						|| args.contains("&&")) {
					for (int i = 0; i < args.length(); i++) {
						char t = args.charAt(i);
						if (t == '+' || t == '-' || t == '*' || t == '/'
								|| t == '(' || t == ')' || t == '<' || t == '>'
								|| t == '=' || t == '!' || t == '|' || t == '&') {
							if (t == '(' && i != 0) {
								// 如果第一个不是(，那么则可能是子公式
								// 1.截取子公式
								// 1.1.获取对应右括号
								int _start = i;// 起始位置
								int _end = -1;// 待定位
								int count = 1;// 左括号的个数，初始为1，意义为有一个左括号

								// 定位右括号结束位置
								for (int index = _start + 1; index < args
										.length(); index++) {
									if (args.charAt(index) == '(') {
										count++;
									} else if (args.charAt(index) == ')') {
										count--;
										_end = index;
									}
									if (count == 0) {
										break;
									}
								}

								if (count != 0) {
									throw new BusiException("公式错误[?]！",formulaStr);
								}

								String subFormula = args.substring(0, _end + 1)
										.trim();
								log.debug("发现子公式：" + subFormula);
								log.debug("开始解析...");
								try {
									oFormula.addArgs(this
											.parseFormula(subFormula));
								} catch (Exception e) {
									log.error("子公式解析出错，将识别为字符串！");
									oFormula.addArgs(subFormula);
								}
								log.debug("子公式解析完毕.");
								args = args.substring(_end + 1).trim();
								i = -1;
							} else {
								if (i != 0) {
									oFormula.addArgs(args.substring(0, i)
											.trim());
								}
								oFormula.addArgs(t);
								args = args.substring(i + 1).trim();
								i = -1;
							}
						}
					}
				} else {
					oFormula.addArgs(args.trim());
					args = "";
				}
			} while (args.trim().length() > 0);
		} else if (oFormula instanceof Formula_arithmetic) {
			args = args.trim();
			log.debug("解析参数：" + args);

			// a+b-c*d/e
			do {
				if (args.contains("+") || args.contains("-")
						|| args.contains("*") || args.contains("/")
						|| args.contains("(") || args.contains(")")) {
					for (int i = 0; i < args.length(); i++) {
						char t = args.charAt(i);
						if (t == '+' || t == '-' || t == '*' || t == '/'
								|| t == '(' || t == ')') {
							if (t == '(' && i != 0) {
								// 如果第一个不是(，那么则可能是子公式
								// 1.截取子公式
								// 1.1.获取对应右括号
								int _start = i;// 起始位置
								int _end = -1;// 待定位
								int count = 1;// 左括号的个数，初始为1，意义为有一个左括号

								// 定位右括号结束位置
								for (int index = _start + 1; index < args
										.length(); index++) {
									if (args.charAt(index) == '(') {
										count++;
									} else if (args.charAt(index) == ')') {
										count--;
										_end = index;
									}
									if (count == 0) {
										break;
									}
								}

								if (count != 0) {
									throw new BusiException("公式错误[?]！",formulaStr);
								}

								String subFormula = args.substring(0, _end + 1)
										.trim();
								log.debug("发现子公式：" + subFormula);
								log.debug("开始解析...");
								try {
									oFormula.addArgs(this
											.parseFormula(subFormula));
								} catch (Exception e) {
									log.error("子公式解析出错，将识别为字符串！");
									oFormula.addArgs(subFormula);
								}
								log.debug("子公式解析完毕.");

								args = args.substring(_end + 1).trim();
								i = -1;
							} else {
								if (i != 0) {
									String arg = args.substring(0, i).trim();
									oFormula.addArgs(arg);
								}
								oFormula.addArgs(t);
								args = args.substring(i + 1).trim();
								i = -1;
							}
						}
					}
				} else {
					oFormula.addArgs(args.trim());
					args = "";
				}
			} while (args.trim().length() > 0);
		} else if (formulaName.equals("_get")) {
			args = args.trim();
			oFormula.addArgs(args);
			args = "";
		} else {
			args = args.substring(args.indexOf("(") + 1, args.lastIndexOf(")")) .trim();
			log.debug("解析参数：" + args);

			// a,b,c,d
			do {
				args = args.trim();
				if (args.indexOf(",") == -1 && args.indexOf("(") == -1) {
					oFormula.addArgs(args);
					args = "";
				} else if (args.indexOf(",") == -1 && args.indexOf("(") != -1) {
					// 只有一个参数，且又子公式(子公式也只有一个参数)，如：zeroifnull(nnum)

					String subFormula = args.trim();
					log.debug("发现子公式：" + subFormula);
					log.debug("开始解析...");
					try {
						oFormula.addArgs(this.parseFormula(subFormula));
					} catch (Exception e) {
						log.error("子公式解析出错，将识别为字符串！");
						oFormula.addArgs(subFormula);
					}
					log.debug("�ӹ�ʽ�������.");
					args = "";
				} else if (args.indexOf("(") == -1
						|| args.indexOf(",") < args.indexOf("(")) {
					// 如果当前参数不包含括号，则不处理子公式
					oFormula.addArgs(args.substring(0, args.indexOf(","))
							.trim());
					args = args.substring(args.indexOf(",") + 1).trim();
				} else {
					// 如果包含左括号，则认为包含子公式
					int _start = args.indexOf("(");// 起始位置
					int _end = -1;// 待定位
					int count = 1;// 左括号的个数，初始为1，意义为有一个左括号

					// 定位右括号结束位置
					for (int index = _start + 1; index < args.length(); index++) {
						if (args.charAt(index) == '(') {
							count++;// 左括号加一
						} else if (args.charAt(index) == ')') {
							count--;// 找到匹配右括号，左括号减一
							_end = index;// 后移指针
						}
						if (count == 0) {
							// 减到0，则认为找到匹配的右括号
							break;
						}
					}

					if (count != 0) {
						throw new BusiException("公式错误[?]！",formulaStr);
					}

					String subFormula = args.substring(0, _end + 1).trim();
					log.debug("发现子公式：" + subFormula);
					log.debug("开始解析...");
					try {
						oFormula.addArgs(this.parseFormula(subFormula));
					} catch (Exception e) {
						log.error("子公式解析出错，将识别为字符串！");
						oFormula.addArgs(subFormula);
					}
					log.debug("子公式解析完毕.");

					args = args.substring(_end + 1).trim();
					if (args.length() > 0 && args.startsWith(",")) {
						args = args.substring(1);
					}
				}
			} while (args.length() > 0);
		}

		return oFormula;
	}

	/**
	 * 解析公式列表
	 * 
	 * @return
	 */
	public List<IFormula> parseFormulas() {
		List<IFormula> formulaList = new ArrayList<IFormula>();
		for (int i = 0; this.formulas != null && i < this.formulas.size(); i++) {
			if (this.formulas.get(i) == null
					|| this.formulas.get(i).trim().length() == 0) {
				continue;
			}
			String formulaStr = this.formulas.get(i);
			if (formulaStr.indexOf(";") != -1) {
				String[] formulaArray = formulaStr.split(";");// ���
				for (String formula : formulaArray) {
					if (formula != null && formula.trim().length() > 0) {
						formulaList.add(this.parseFormula(formula));
					}
				}
			} else {
				formulaList.add(this.parseFormula(this.formulas.get(i)));
			}
		}
		return formulaList;
	}

	/**
	 * 获取公式执行结果
	 * yaojiie 2015 12 09 修改原有的执行公式方法，将显示公式和编辑公式分开
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> getResult() {
		List<IFormula> formulaList = this.parseFormulas();
		//显示公式
		if (this.isMergeContextToResult()) {
			List<IFormula> batchformulaList = new ArrayList<IFormula>();
			List<IFormula> unitformulaList = new ArrayList<IFormula>();
			//将公式分组处理，getcolvalue公式，调用批量处理方法，其他的依旧调用原有的方法。
			for (int i = 0; formulaList != null && i < formulaList.size(); i++) {
				IFormula formula = formulaList.get(i);
				if (formula instanceof Formula_getcolvalue) {
					batchformulaList.add(formula);
				} else {
					unitformulaList.add(formula);
				}
			}
			//处理非getcolvalue公式
			if (unitformulaList != null && unitformulaList.size() > 0) {
				// 逐行迭代数据context
				for (Map<String, Object> rowMap : this.getContext()) {
					for (int i = 0; unitformulaList != null && i < unitformulaList.size(); i++) {
						IFormula formula = unitformulaList.get(i);
						Object result = formula.getResult(rowMap);

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
								log.warn("公式输出参数名称为空，该公式结果将被忽略，公式为：" + formula.getFormulaStr());
							}
						}

					}

				}
			}
			//处理getcolvalue公式
			if (batchformulaList != null && batchformulaList.size() > 0) {

				for (int i = 0; batchformulaList != null && i < batchformulaList.size(); i++) {
					IFormula formula = batchformulaList.get(i);
					formula.getAllResults(this.getContext(), formula);
				}
			}
			return this.getContext();

		} else {
			//编辑公式
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			// 逐行迭代数据context
			for (Map<String, Object> rowMap : this.getContext()) {
				Map<String, Object> resultRowMap = new HashMap<String, Object>();
				for (int i = 0; formulaList != null && i < formulaList.size(); i++) {
					IFormula formula = formulaList.get(i);
					Object result = formula.getResult(rowMap);

					if (result instanceof Map
							|| (formula.getOutArgName() != null && formula.getOutArgName().contains(","))) {
						// 多个返回值（如:getColsValue）
						if (result == null) {
							String[] arr = formula.getOutArgName().split(",");
							for (String argName : arr) {
								resultRowMap.put(argName.trim(), null);
								rowMap.put(argName.trim(), null);
							}
						} else {
							if (formula.getOutArgName() != null) {
								resultRowMap.putAll((Map) result);
								rowMap.putAll((Map) result);
							}
						}
					} else {
						if (formula.getOutArgName() != null) {
							// 如果有输出参数，把相应数据填回行数据context（如果没有？那就没必要执行公式了！）
							resultRowMap.put(formula.getOutArgName().trim(), result);
							rowMap.put(formula.getOutArgName().trim(), result);// Ϊ��֧�ֱ����ݣ�����ͬʱҲ����rowMap
						} else {
							log.warn("公式输出参数名称为空，该公式结果将被忽略，公式为：" + formula.getFormulaStr());
						}
					}
				}

				resultList.add(resultRowMap);
			}
			return resultList;
		}
	}
}
