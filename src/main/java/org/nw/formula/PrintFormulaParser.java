package org.nw.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.formula.impl.Formula_;
import org.nw.formula.impl.Formula_arithmetic;

/**
 * 打印公式解析器
 * 
 * @author fangw
 */
public class PrintFormulaParser {
	private static final Log log = LogFactory.getLog(PrintFormulaParser.class);
	private static ConcurrentHashMap<String, Class<?>> classCacheMap = new ConcurrentHashMap<String, Class<?>>();
	private DataSource dataSource = null;
	private List<String> formulas;
	private Map<String, Object> context;
	private FormulaDao dao = null;

	public PrintFormulaParser() {
		log.warn("警告：公式解析器没有传入数据源，在遇到类似getColValue这种需要依赖数据库查询的公式时将出错！");
	}

	public PrintFormulaParser(DataSource dataSource) {
		this.setDataSource(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.dao = new FormulaDao(this.getDataSource());
	}

	public List<String> getFormulas() {
		return formulas;
	}

	public void setFormulas(String[] formulas) {
		if(formulas != null) {
			if(this.formulas == null) {
				this.formulas = new ArrayList<String>();
			}
			for(String formula : formulas) {
				this.formulas.add(formula);
			}
		}
	}

	public void setFormulas(List<String> formulas) {
		this.formulas = formulas;
	}

	public void addFormula(String formula) {
		if(this.formulas == null) {
			this.formulas = new ArrayList<String>();
		}
		this.formulas.add(formula);
	}

	public Map<String, Object> getContext() {
		return context;
	}

	/**
	 * �������context context���ձ�����List<Map<String,Object>>��ʽ
	 * 
	 * @param context
	 */
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}

	/**
	 * ����ʽ
	 * 
	 * <pre>
	 * ��ʽʾ��
	 * pk_billtype_name->getColValue(bd_billtype,billtypename,pk_billtypecode,pk_billtype)
	 * 
	 * ���ӹ�ʽ��
	 * remaindercavmny->floanmny-iif(pk_contrajectloan==null,temp_cavmny,temp_cavmny2)-iif(pk_contrajectloan==null,temp_fhandbackmny,temp_fhandbackmny2)
	 * </pre>
	 * 
	 * @return
	 */
	private IFormula parseFormula(String formulaStr) {
		log.debug("����ʽ��" + formulaStr);
		// ����ʽ���(��ʽȫ����Сд)
		String formulaName = formulaStr.trim().toLowerCase();
		String outArgName = null;
		if(formulaName.indexOf(";") != -1) {
			// ���ʽ
			throw new RuntimeException("��ʽ������󣬲�Ӧ�û��зֺţ�");
		} else {
			// ����ʽ
			// ����������
			if(formulaName.indexOf("->") != -1) {
				outArgName = formulaName.substring(0, formulaName.indexOf("->"));
				formulaName = formulaName.substring(formulaName.indexOf("->") + 2);
			}
			// eg:iif(indcode==null, null, indcode+" "+indname)
			formulaName = FormulaUtils.getFormulaName(formulaName);
		}
		formulaName = formulaName.trim();

		// ������ʽʵ��
		IFormula oFormula = null;
		try {
			String className = "com.uft.webnc.formula.impl.Formula_" + formulaName;
			log.debug("׼��ʵ��ʽ��" + className);

			// Class.forName���Ի���
			Class<?> clazz = null;
			if(classCacheMap.containsKey(className)) {
				clazz = classCacheMap.get(className);
			} else {
				clazz = Class.forName(className);
				classCacheMap.put(className, clazz);
			}

			oFormula = (IFormula) clazz.newInstance();
			oFormula.setFormulaStr(formulaStr.trim());
			// oFormula.setDataSource(this.getDataSource());
			oFormula.setDao(this.dao);
		} catch(ClassNotFoundException e) {
			log.error("��֧�ָù�ʽ��" + formulaStr.trim());
			throw new RuntimeException("��֧�ָù�ʽ��" + formulaStr.trim());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		// ������
		if(outArgName != null) {
			oFormula.setOutArgName(outArgName);
		}

		// �������
		String args = formulaStr.trim();
		if(args.indexOf("->") != -1) {
			args = args.substring(args.indexOf("->") + 2);
		} else {
			// ����Ҫ���?һ�����ӹ�ʽ���Ѿ�û��->�����
		}

		if(oFormula instanceof Formula_) {
			args = args.trim();
			log.debug("�������" + args);

			do {
				if(args.contains("+") || args.contains("-") || args.contains("*") || args.contains("/")
						|| args.contains("(") || args.contains(")") || args.contains("<") || args.contains(">")
						|| args.contains("<=") || args.contains(">=") || args.contains("==") || args.contains("!=")
						|| args.contains("!") || args.contains("||") || args.contains("&&")) {
					for(int i = 0; i < args.length(); i++) {
						char t = args.charAt(i);
						if(t == '+' || t == '-' || t == '*' || t == '/' || t == '(' || t == ')' || t == '<' || t == '>'
								|| t == '=' || t == '!' || t == '|' || t == '&') {
							if(t == '(' && i != 0) {
								// ����һ����(����ô��������ӹ�ʽ
								// 1.��ȡ�ӹ�ʽ
								// 1.1.��ȡ��Ӧ��(��
								int _start = i;// ��ʼλ��
								int _end = -1;// ��λ
								int count = 1;// ��(�ŵĸ����ʼΪ1������Ϊ��һ����(��

								// ��λ��(�Ž���λ��
								for(int index = _start + 1; index < args.length(); index++) {
									if(args.charAt(index) == '(') {
										count++;// ��(�ż�һ
									} else if(args.charAt(index) == ')') {
										count--;// �ҵ�ƥ����(�ţ���(�ż�һ
										_end = index;// ����ָ��
									}
									if(count == 0) {
										// ��0������Ϊ�ҵ�ƥ�����(��
										break;
									}
								}

								if(count != 0) {
									throw new RuntimeException("��ʽ����" + formulaStr);
								}

								// ��ȡ�ӹ�ʽ
								String subFormula = args.substring(0, _end + 1).trim();
								log.debug("�����ӹ�ʽ��" + subFormula);
								log.debug("��ʼ����...");
								try {
									oFormula.addArgs(this.parseFormula(subFormula));
								} catch(Exception e) {
									log.error("�ӹ�ʽ�����?��ʶ��Ϊ�ַ�");
									oFormula.addArgs(subFormula);
								}
								log.debug("�ӹ�ʽ�������.");

								args = args.substring(_end + 1).trim();
								i = -1;
							} else {
								if(i != 0) {
									oFormula.addArgs(args.substring(0, i).trim());
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
			} while(args.trim().length() > 0);
		} else if(oFormula instanceof Formula_arithmetic) {
			args = args.trim();
			log.debug("�������" + args);

			// a+b-c*d/e
			do {
				if(args.contains("+") || args.contains("-") || args.contains("*") || args.contains("/")
						|| args.contains("(") || args.contains(")")) {
					for(int i = 0; i < args.length(); i++) {
						char t = args.charAt(i);
						if(t == '+' || t == '-' || t == '*' || t == '/' || t == '(' || t == ')') {
							if(t == '(' && i != 0) {
								// ����һ����(����ô��������ӹ�ʽ
								// 1.��ȡ�ӹ�ʽ
								// 1.1.��ȡ��Ӧ��(��
								int _start = i;// ��ʼλ��
								int _end = -1;// ��λ
								int count = 1;// ��(�ŵĸ����ʼΪ1������Ϊ��һ����(��

								// ��λ��(�Ž���λ��
								for(int index = _start + 1; index < args.length(); index++) {
									if(args.charAt(index) == '(') {
										count++;// ��(�ż�һ
									} else if(args.charAt(index) == ')') {
										count--;// �ҵ�ƥ����(�ţ���(�ż�һ
										_end = index;// ����ָ��
									}
									if(count == 0) {
										// ��0������Ϊ�ҵ�ƥ�����(��
										break;
									}
								}

								if(count != 0) {
									throw new RuntimeException("��ʽ����" + formulaStr);
								}

								// ��ȡ�ӹ�ʽ
								String subFormula = args.substring(0, _end + 1).trim();
								log.debug("�����ӹ�ʽ��" + subFormula);
								log.debug("��ʼ����...");
								try {
									oFormula.addArgs(this.parseFormula(subFormula));
								} catch(Exception e) {
									log.error("�ӹ�ʽ�����?��ʶ��Ϊ�ַ�");
									oFormula.addArgs(subFormula);
								}
								log.debug("�ӹ�ʽ�������.");

								args = args.substring(_end + 1).trim();
								i = -1;
							} else {
								if(i != 0) {
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
			} while(args.trim().length() > 0);
		} else if(formulaName.equals("_get")) {
			args = args.trim();
			oFormula.addArgs(args);
			args = "";
		} else {
			args = args.substring(args.indexOf("(") + 1, args.lastIndexOf(")")).trim();
			log.debug("�������" + args);

			// a,b,c,d
			do {
				args = args.trim();
				if(args.indexOf(",") == -1 && args.indexOf("(") == -1) {
					// ֻ��һ�����
					oFormula.addArgs(args);
					args = "";
				} else if(args.indexOf(",") == -1 && args.indexOf("(") != -1) {
					// ֻ��һ����������ӹ�ʽ(�ӹ�ʽҲֻ��һ�����)���磺zeroifnull(nnum)

					// ��ȡ�ӹ�ʽ
					String subFormula = args.trim();
					log.debug("�����ӹ�ʽ��" + subFormula);
					log.debug("��ʼ����...");
					try {
						oFormula.addArgs(this.parseFormula(subFormula));
					} catch(Exception e) {
						log.error("�ӹ�ʽ�����?��ʶ��Ϊ�ַ�");
						oFormula.addArgs(subFormula);
					}
					log.debug("�ӹ�ʽ�������.");
					args = "";
				} else if(args.indexOf("(") == -1 || args.indexOf(",") < args.indexOf("(")) {
					// ���ǰ�����(�ţ��򲻴����ӹ�ʽ
					oFormula.addArgs(args.substring(0, args.indexOf(",")).trim());
					args = args.substring(args.indexOf(",") + 1).trim();
				} else {
					// ������(�ţ�����Ϊ���ӹ�ʽ
					int _start = args.indexOf("(");// ��ʼλ��
					int _end = -1;// ��λ
					int count = 1;// ��(�ŵĸ����ʼΪ1������Ϊ��һ����(��

					// ��λ��(�Ž���λ��
					for(int index = _start + 1; index < args.length(); index++) {
						if(args.charAt(index) == '(') {
							count++;// ��(�ż�һ
						} else if(args.charAt(index) == ')') {
							count--;// �ҵ�ƥ����(�ţ���(�ż�һ
							_end = index;// ����ָ��
						}
						if(count == 0) {
							// ��0������Ϊ�ҵ�ƥ�����(��
							break;
						}
					}

					if(count != 0) {
						throw new RuntimeException("��ʽ����" + formulaStr);
					}

					// ��ȡ�ӹ�ʽ
					String subFormula = args.substring(0, _end + 1).trim();
					log.debug("�����ӹ�ʽ��" + subFormula);
					log.debug("��ʼ����...");
					try {
						oFormula.addArgs(this.parseFormula(subFormula));
					} catch(Exception e) {
						log.error("�ӹ�ʽ�����?��ʶ��Ϊ�ַ�");
						oFormula.addArgs(subFormula);
					}
					log.debug("�ӹ�ʽ�������.");

					args = args.substring(_end + 1).trim();
					if(args.length() > 0 && args.startsWith(",")) {
						args = args.substring(1);// �����滹�в�������
					}
				}
			} while(args.length() > 0);
		}

		return oFormula;
	}

	/**
	 * ����ʽ�б�
	 * 
	 * @return
	 */
	private List<IFormula> parseFormulas() {
		List<IFormula> formulaList = new ArrayList<IFormula>();
		for(int i = 0; this.formulas != null && i < this.formulas.size(); i++) {
			if(this.formulas.get(i) == null || this.formulas.get(i).trim().length() == 0) {
				continue;
			}
			String formulaStr = this.formulas.get(i);
			if(formulaStr.indexOf(";") != -1) {
				String[] formulaArray = formulaStr.split(";");// ���
				for(String formula : formulaArray) {
					if(formula != null && formula.trim().length() > 0) {
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
	 * ��ȡ��ʽִ�н��
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResult() {
		// ����ʽ
		List<IFormula> formulaList = this.parseFormulas();

		// ���ִ��ÿһ��ʽ
		Map<String, Object> resultRowMap = new HashMap<String, Object>();

		// ����ж��ʽ������ִ�е����һ��֮ǰ
		for(int i = 0; formulaList != null && i < formulaList.size() - 1; i++) {
			IFormula formula = formulaList.get(i);
			Object result = formula.getResult(this.context);
			if(result instanceof Map || (formula.getOutArgName() != null && formula.getOutArgName().contains(","))) {
				// ����ֵ����:getColsValue��
				if(result == null) {
					String[] arr = formula.getOutArgName().split(",");
					for(String argName : arr) {
						resultRowMap.put(argName, null);
						this.context.put(argName, null);// Ϊ��֧�ֱ����ݣ�����ͬʱҲ����rowMap
					}
				} else {
					if(formula.getOutArgName() != null) {
						// ��������������Ӧ�����������context�����û�У��Ǿ�û��Ҫִ�й�ʽ�ˣ���
						resultRowMap.putAll((Map) result);
						this.context.putAll((Map) result);// Ϊ��֧�ֱ����ݣ�����ͬʱҲ����rowMap
					}
				}
			} else {
				// �����ֵ
				if(formula.getOutArgName() != null) {
					// ��������������Ӧ�����������context�����û�У��Ǿ�û��Ҫִ�й�ʽ�ˣ���
					resultRowMap.put(formula.getOutArgName(), result);
					this.context.put(formula.getOutArgName(), result);// Ϊ��֧�ֱ����ݣ�����ͬʱҲ����rowMap
				} else {
					log.warn("��ʽ���������Ϊ�գ��ù�ʽ������ԣ���ʽΪ��" + formula.getFormulaStr());
				}
			}
		}

		// ���һ��ʽ����ӡ��ʽ�Ľ��Ϳ����ʽ��
		IFormula formula = formulaList.get(formulaList.size() - 1);
		Object result = formula.getResult(this.context);
		if(result instanceof Map || (formula.getOutArgName() != null && formula.getOutArgName().contains(","))) {
			// ����ֵ����:getColsValue��
			throw new RuntimeException("��ӡ��ʽ��֧�ֶ���ֵ�����鹫ʽ��ʽ��");
		} else {
			// �����ֵ
			if(formula.getOutArgName() != null) {
				// �����������
				// throw new RuntimeException("��ӡ��ʽ��֧�����������鹫ʽ��ʽ��");
				log.warn("��ӡ��ʽ��֧�����������鹫ʽ:" + formula.getFormulaStr());
				return result;
			} else {
				return result;
			}
		}
	}
}
