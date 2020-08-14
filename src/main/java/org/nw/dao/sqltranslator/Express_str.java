package org.nw.dao.sqltranslator;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 此处插入类型说明。 创建日期：(2001-3-15 19:27:54)
 * 
 */
public class Express_str {
	private static final Log log = LogFactory.getLog(Express_str.class);
	String[] m_definitie = null; // 公式表达式
	Expresion_str m_Arbore = null; // 根接点
	private int pozitie = 0; // 指针位置
	private int code = 0; // 返回的错误代码
	private int state = 0; // 状态，运算、关系、逻辑
	private static int DIVISION_BY_0 = 1; // 被零除
	private static int ILEGAL_OPERATION = 2; // 非法操作符
	private static int UNDEFINED_VARIABLE = 3; // 未定义的变量
	private static int INVALID_DOMAIN = 4; // 错误的取值范围
	private static int RELATION = 5; // 关系比较式
	// private java.util.Hashtable data=new java.util.Hashtable();//变量表
	// String m_pvariabile=null;

	// String m_asOperationStr[] = { "=", "!=", "<>", "<", "<=", ">", ">=" };
	// //操作符
	// String m_asOperationStr[] = { "and", "or"}; //操作符

	// 定义特殊字符串,//张森 2001。3。17 改，增加 >,!>,!<,=,<,
	/*
	 * String m_asSpecialStr[] = { "!=", "!>", "!<", "<>", "<=", ">=", "=", "<",
	 * ">", "||", m_sLineSep };
	 */
	String m_asSpecialStr[] = { "and", "or" };
	// String m_asSpecialStr_new[] = { "and", "or"," ",m_sLineSep };

	static String m_sLineSep = "\r\n";
	// System.getProperty("line.separator"); //换行符
	// String m_sSpecialChar = "-+()*=,? <>;" + m_sLineSep;

	String m_sSpecialChar = "()";

	// String m_sSpecialChar_new = "() " +m_sLineSep;
	/**
	 * Express 构造子注解。
	 */
	public Express_str() {
		super();
		log.debug("nc.bs.mw.sqltrans.Express_str Over");
	}

	/**
	 * 此处插入方法说明。 功能：去除同类项 创建日期：(2001-3-26 14:09:10)
	 * 
	 * @return java.util.Vector
	 * @param source
	 *            java.util.Vector
	 */
	public Vector dealVector(Vector source) {
		log.debug("nc.bs.mw.sqltrans.Express_str.dealVector");
		if(source == null || source.size() <= 0)
			return null;

		for(int i = 0; i < source.size() - 1; i++) {
			String st1 = source.elementAt(i).toString().trim();

			for(int j = i + 1; j < source.size(); j++) {
				String st2 = source.elementAt(j).toString().trim();

				if(st1.equalsIgnoreCase(st2)) {
					source.removeElementAt(j);
				}
			}
		}

		log.debug("nc.bs.mw.sqltrans.Express_str.dealVector Over");
		return source;
	}

	/**
	 * 此处插入方法说明。 功能：使节点为空 创建日期：(2001-3-15 19:30:11)
	 * 
	 * @log.debug("nc.bs.mw.sqltrans.Express_str.elibmem Over"); return
	 *                                                   test.Expresion
	 */
	private void elibmem(Expresion_str e) {
		log.debug("nc.bs.mw.sqltrans.Express_str.elibmem");
		e = null;

	}

	/**
	 * 此处插入方法说明。 功能：从指针的位置开始解析表达里的常数和变量， 采用递归的方法建立运算表达式 创建日期：(2001-3-15
	 * 19:30:11)
	 * 
	 * @return test.Expresion_str 函数调用顺序： expresize(+ -)->termen(* /)->putere(^
	 *         乘方) ->logicalOP(《 》=)->sgOp(！) ->factor(- （） ||
	 *         )>-<identificator(函数、变量)
	 */
	private Expresion_str expresie_or() {
		log.debug("nc.bs.mw.sqltrans.Express_str.expresie_or");
		Expresion_str nod;
		Expresion_str arb1 = logicalOp_and(); // and
		Expresion_str arb2;
		if(arb1 == null)
			return null; // In caz de eroare terminate

		while(pozitie < m_definitie.length - 1 && m_definitie[pozitie].equalsIgnoreCase("or")) {
			nod = new Expresion_str();
			nod.left = arb1;
			// nod.operatie = m_definitie[pozitie];
			nod.operatie = '|';
			pozitie++;
			arb2 = logicalOp_and(); // and
			nod.right = arb2;
			if(arb2 == null) {
				elibmem(nod);
				return null; // In caz de eroare terminate
			}
			arb1 = nod;
		}
		log.debug("nc.bs.mw.sqltrans.Express_str.expresie_or Over");
		return arb1;
	}

	/**
	 * 此处插入方法说明。 功能：处理函数后的表达式 创建日期：(2001-3-15 19:30:11)
	 * 
	 * @return test.Expresion_str
	 */
	private Expresion_str factor() {
		log.debug("nc.bs.mw.sqltrans.Express_str.factor");
		Expresion_str nod = null, nod2 = null, left = null;

		if(m_definitie[pozitie].equals("(")) // 如果遇到左括号,说明是函数
		{
			pozitie++;
			nod = expresie_or(); // 继续处理公式里的常数和变量
			if(nod == null)
				return null;
			while(pozitie < m_definitie.length && !m_definitie[pozitie].equals(")")) // 如果遇到逗号,说明下一个仍然是函数的参数
			{
				nod2 = new Expresion_str();
				pozitie++;
				nod2.right = expresie_or(); // 继续处理公式里的常数和变量
				nod2.left = nod;
				nod = new Expresion_str();
				nod = nod2;
				nod.operatie = ',';
			}
			/*
			 * if (m_definitie[pozitie].equals(")") )//如果没有遇到右括号,说明 该函数非法 {
			 * elibmem(nod);//去掉该表达式 return null; }
			 */
			if(pozitie >= m_definitie.length)
				return null;
			pozitie++;
			return nod;
		} else
			// 单常量 参数 函数
			log.debug("nc.bs.mw.sqltrans.Express_str.factor Over");
		return identificator(); // 识别函数或变量

	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 16:39:28)
	 * 
	 * @return int
	 * @param stArray
	 *            java.lang.String[]
	 * @param i
	 *            int
	 */
	public int getEndIndex(String[] stArray, int index) {
		log.debug("nc.bs.mw.sqltrans.Express_str.getEndIndex");
		int i = 0;
		int left = 0;
		int right = 0;

		for(i = index; i < stArray.length; i++) {
			if(stArray[i].trim().equals("(")) {
				left++;
			}
			if(stArray[i].trim().equals(")")) {
				right++;
			}
			// if(left==right && (stArray[i].trim().equalsIgnoreCase("or") ||
			// stArray[i].trim().equalsIgnoreCase("and")))
			if(left == right) {
				break;
			}
		}
		// i--;
		log.debug("nc.bs.mw.sqltrans.Express_str.getEndIndex Over");
		return i;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 16:39:28)
	 * 
	 * @return int
	 * @param stArray
	 *            java.lang.String[]
	 * @param i
	 *            int
	 */
	public int getKuoEndIndex(String[] stArray, int index) {
		log.debug("nc.bs.mw.sqltrans.Express_str.getKuoEndIndex");
		int i = 0;
		int left = 0;
		int right = 0;

		for(i = index; i < stArray.length; i++) {
			if(stArray[i].trim().equals("(")) {
				left++;
			}
			if(stArray[i].trim().equals(")")) {
				right++;
			}
			if(right > left
					|| (right == left && (stArray[i].trim().equalsIgnoreCase("or") || stArray[i].trim()
							.equalsIgnoreCase("and")))) {
				break;
			}
		}
		i--;
		log.debug("nc.bs.mw.sqltrans.Express_str.getKuoEndIndex Over");
		return i;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-15 21:34:19)
	 * 
	 * @return double
	 */
	public String getValue() {
		log.debug("nc.bs.mw.sqltrans.Express_str.getValue");
		Vector vec = vexp(m_Arbore);

		if(vec == null || vec.size() <= 0)
			return "";

		String re = "";

		for(int i = 0; i < vec.size(); i++) {
			String st = vec.elementAt(i).toString().trim();

			if(i < vec.size() - 1) {
				// re+="("+st+") "+"or"+" ";
				re += st + " or" + " ";
			} else {
				// re+="("+st+") ";
				re += st;
			}
		}

		log.debug("nc.bs.mw.sqltrans.Express_str.getValue Over");
		return re;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-15 21:34:19)
	 * 
	 * @return double
	 */
	public String getValue(String sql) {
		log.debug("nc.bs.mw.sqltrans.Express_str.getValue");
		setExpress(sql);

		log.debug("nc.bs.mw.sqltrans.Express_str.getValue Over");
		return getValue();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-15 21:34:19)
	 * 
	 * @return double
	 */
	public Vector getValue_v() {
		log.debug("nc.bs.mw.sqltrans.Express_str.getValue_v");
		log.debug("nc.bs.mw.sqltrans.Express_str.getValue_v Over");
		return vexp(m_Arbore);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-26 14:53:36)
	 * 
	 * @return boolean
	 * @param sou
	 *            java.lang.String[]
	 * @param de
	 *            java.lang.String
	 */
	public boolean having(String[] sou, String de) {
		log.debug("nc.bs.mw.sqltrans.Express_str.having");
		if(sou == null || de == null)
			return false;

		for(int i = 0; i < sou.length; i++) {
			if(sou[i].trim().equalsIgnoreCase(de.trim())) {
				return true;
			}
		}

		log.debug("nc.bs.mw.sqltrans.Express_str.having Over");
		return false;
	}

	/**
	 * 此处插入方法说明。 功能：//识别函数或变量,处理常数、变量、函数 创建日期：(2001-3-15 19:30:11)
	 * 
	 * @return test.Expresion_str
	 */
	private Expresion_str identificator() {
		log.debug("nc.bs.mw.sqltrans.Express_str.identificator");

		Expresion_str nod = null, nod2 = null;
		Expresion_str result = null;

		String newSt = m_definitie[pozitie];

		if(pozitie < m_definitie.length - 1 && m_definitie[pozitie + 1].equalsIgnoreCase("(")) {
			pozitie++; // 记录当前位置
			int leftInclude = 0;
			int rightInclude = 0;
			String sub = "";
			while(pozitie < m_definitie.length
					&& (leftInclude != rightInclude || (!m_definitie[pozitie].equalsIgnoreCase("and") && !m_definitie[pozitie]
							.equalsIgnoreCase("or")))) {
				if(m_definitie[pozitie].equals("(")) {
					leftInclude++;
				} else if(m_definitie[pozitie].equals(")")) {
					rightInclude++;
				}
				newSt += " " + m_definitie[pozitie];
				pozitie++;
			}
			pozitie--;
		}

		nod = new Expresion_str();
		nod.left = null;
		nod.right = null;
		nod.operatie = '@';
		Vector v = new Vector();
		// v.addElement(m_definitie[pozitie]);
		v.addElement(newSt);
		nod.valoare = v;
		result = nod;

		pozitie++;

		log.debug("nc.bs.mw.sqltrans.Express_str.identificator Over");
		return result;

	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-29 11:18:23)
	 * 
	 * @return boolean
	 * @param st
	 *            java.lang.String
	 */
	public boolean isBiJiaoFu(String st) {
		log.debug("nc.bs.mw.sqltrans.Express_str.isBiJiaoFu");
		boolean isOprater = false;

		// 没有遇到as， 也没有遇到on,别名
		if(st.equals("=") || st.startsWith("=") || st.equals("<=") || st.startsWith("<=") || st.equals(">=")
				|| st.startsWith(">=") || st.equals("<") || st.startsWith("<") || st.equals(">") || st.startsWith(">")
				|| st.equals("<>") || st.startsWith("<>") || st.equals("!=") || st.startsWith("!=")
				|| st.equalsIgnoreCase("is") || st.startsWith("is ") || st.equalsIgnoreCase("+") || st.startsWith("+")
				|| st.equalsIgnoreCase("-") || st.startsWith("-") || st.equalsIgnoreCase("*") || st.startsWith("*")
				|| st.equalsIgnoreCase("/") || st.startsWith("/") || st.equalsIgnoreCase("!") || st.startsWith("!")) {
			isOprater = true;
		}
		log.debug("nc.bs.mw.sqltrans.Express_str.isBiJiaoFu Over");
		return isOprater;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 18:37:16)
	 * 
	 * @return boolean
	 * @param c
	 *            char
	 */
	public boolean isSpecialChar(char c) {
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialChar");
		boolean isSpecial = false;

		for(int i = 0; i < m_sSpecialChar.length(); i++) {
			if(c == m_sSpecialChar.charAt(i)) {
				isSpecial = true;
			}
		}
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialChar Over");
		return isSpecial;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 18:37:16)
	 * 
	 * @return boolean
	 * @param c
	 *            char
	 */
	public boolean isSpecialChar_all(char c) {
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialChar_all");
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialChar_all Over");
		return (c == ' ') || isSpecialChar(c);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 18:37:16)
	 * 
	 * @return boolean
	 * @param c
	 *            char
	 */
	public boolean isSpecialSt(String st) {
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialSt");
		boolean isSpecial = false;

		for(int i = 0; i < m_asSpecialStr.length; i++) {
			if(st.equalsIgnoreCase(m_asSpecialStr[i])) {
				isSpecial = true;
			}
		}
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialSt Over");
		return isSpecial;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 18:37:16)
	 * 
	 * @return boolean
	 * @param c
	 *            char
	 */
	public boolean isSpecialSt(String st, int index, String source) {
		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialSt");
		boolean isSpecial = false;

		if(isSpecialSt(st)) {
			if(index <= 0 || (isSpecialChar_all(source.charAt(index - 1)))) {
				if(index + st.length() >= source.length() - 1
						|| (isSpecialChar_all(source.charAt(index + st.length())))) {
					isSpecial = true;
				}
			}
		}

		log.debug("nc.bs.mw.sqltrans.Express_str.isSpecialSt Over");
		return isSpecial;
	}

	/**
	 * 此处插入方法说明。 功能：比较大小，应扩充 = != <> <= >=的比较 创建日期：(2001-3-15 19:30:11)
	 * 
	 * @return test.Expresion_str
	 */
	private Expresion_str logicalOp_and() {
		log.debug("nc.bs.mw.sqltrans.Express_str.logicalOp_and");
		Expresion_str nod;
		Expresion_str arb1 = factor(); // 元素
		Expresion_str arb2;
		if(arb1 == null)
			return null; // In caz de eroare terminate

		// 如果遇到 > < =
		while(pozitie < m_definitie.length - 1 && m_definitie[pozitie].equalsIgnoreCase("and")) {
			nod = new Expresion_str();
			nod.left = arb1;

			nod.operatie = '&';

			pozitie++;
			arb2 = factor(); // 元素
			nod.right = arb2;
			if(arb2 == null) {
				elibmem(nod);
				return null; // In caz de eroare terminate
			}

			arb1 = nod;
		}
		log.debug("nc.bs.mw.sqltrans.Express_str.logicalOp_and Over");
		return arb1;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-25 19:33:15)
	 * 
	 * @return java.util.Vector
	 * @param lrft
	 *            java.util.Vector
	 * @param right
	 *            java.util.Vector
	 */
	public Vector op_and(Vector left, Vector right) {
		log.debug("nc.bs.mw.sqltrans.Express_str.op_and");
		if(left == null || right == null || left.size() <= 0 || right.size() <= 0) {
			return new Vector();
		}

		Vector re = new Vector();

		for(int i = 0; i < left.size(); i++) {
			String st_left = left.elementAt(i).toString().trim();

			for(int j = 0; j < right.size(); j++) {
				String st_right = right.elementAt(j).toString().trim();

				String st = "";

				// 排序
				if(st_left.compareToIgnoreCase(st_right) == 0 || having(parseSql(st_left), st_right)
						|| having(parseSql(st_right), st_left)) {
					st = st_left;

				} else {
					if(st_left.compareToIgnoreCase(st_right) < 0) {
						st = st_left + " " + "and" + " " + st_right;

					} else if(st_left.compareToIgnoreCase(st_right) > 0) {
						st = st_right + " " + "and" + " " + st_left;

					}

				}

				re.addElement(st);

			}
		}

		log.debug("nc.bs.mw.sqltrans.Express_str.op_and Over");
		return dealVector(re);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-25 19:33:15)
	 * 
	 * @return java.util.Vector
	 * @param lrft
	 *            java.util.Vector
	 * @param right
	 *            java.util.Vector
	 */
	public Vector op_or(Vector left, Vector right) {
		log.debug("nc.bs.mw.sqltrans.Express_str.op_or");
		if(left == null || right == null || left.size() <= 0 || right.size() <= 0) {
			return new Vector();
		}

		for(int i = 0; i < right.size(); i++) {
			String st = right.elementAt(i).toString().trim();
			left.addElement(st);
		}
		log.debug("nc.bs.mw.sqltrans.Express_str.op_or Over");
		return dealVector(left);
	}

	/**
	 * 对SQL语句进行拆解分析,得到每一个有意义的字符
	 */
	String[] parseSql(String sql) {
		log.debug("nc.bs.mw.sqltrans.Express_str.parseSql");
		// 若输入的SQL为空，则返回空
		if(sql == null || sql.trim().length() == 0)
			return null;
		// 初始单词序列
		String asKeyWords[] = null;
		// 初始哈西表
		java.util.Hashtable table = new java.util.Hashtable();
		// 初始记数器
		int iCount = 0;
		sql = sql.trim();
		// 若单词长度大于0，则开始寻找其余单词
		while(sql.length() > 0) {
			// 找到第一个单词
			String sWord = parseWord(sql);

			sql = sql.substring(sWord.length()).trim();

			// 若单词长度大于0
			if(sWord.trim().length() > 0) {
				// 存入新单词
				String s = sWord;
				// 存入哈西表
				table.put(new Integer(iCount), s);
				// 计数加1
				iCount++;
			} else {
				break;
			}
		}
		// 初始字符串数组
		asKeyWords = new String[iCount];
		// 从哈西表中提取记录
		for(int i = 0; i < iCount; i++) {
			asKeyWords[i] = (String) table.get(new Integer(i));
		}
		// 返回字符串组
		log.debug("nc.bs.mw.sqltrans.Express_str.parseSql Over");
		return asKeyWords;
	}

	/**
	 * 从sql语句 s 中提取第一个有意义的单词 word = (|)|*|=|,|?| |<|>|!=|<>|<=|>=|其他
	 */
	public String parseWord(String s) {
		log.debug("nc.bs.mw.sqltrans.Express_str.parseWord");
		// 注意此处不可用 s=s.trim();语句，否则会出错

		// 若输入单词长度为0，则返回""
		if(s.length() == 0) {
			return "";
		}
		// 标志:是否在''内,是否在""内,是否找到单词
		boolean bInSingle = false;
		boolean bInDouble = false;
		boolean bFound = false;
		// 初始计数器
		int iOffSet = 0;
		// 初始字符缓存
		char c;
		// 取得输入字符串在计数位置的字符
		c = s.charAt(iOffSet); // 第一个有效字符
		if(isSpecialChar(c)) {
			return "" + c;
		}
		if(s.length() > 1) {
			if(isSpecialSt(s.substring(0, 2), 0, s)) {
				return s.substring(0, 2);
			} else if(s.length() > 2 && isSpecialSt(s.substring(0, 3), 0, s)) {
				return s.substring(0, 3);
			}

		} else {
			return s;
		}
		// 返回特殊字符串
		// 计数器加1
		/*
		 * iOffSet++; //若计数小于输入字符串长度 if (iOffSet < s.length()) {
		 * //取得输入字符串在计数位置2位的字符串 if(isSpecialChar(s.charAt(iOffSet))) { return
		 * ""+c; } String ss = "" + c + s.charAt(iOffSet);
		 * if(iOffSet+1>=s.length()) { return ss.trim(); } String sss = ss +
		 * s.charAt(iOffSet+1); //依次比较是否为特殊字符串 if(isSpecialSt(ss)) { //返回特殊字符串
		 * return s.substring(0, iOffSet + 1); } if(isSpecialSt(sss)) {
		 * //返回特殊字符串 return s.substring(0, iOffSet + 2); } } //计数器减1 iOffSet--;
		 */
		// 查找并返回特殊字符
		if(isSpecialChar(c)) {
			return s.substring(0, iOffSet + 1);
		}

		// 若计数小于输入字符串的长度
		while(iOffSet < s.length()) {
			// 取得输入字符串在计数位置的字符
			c = s.charAt(iOffSet);
			// 若为单引号
			if(c == '\'') {
				// 若不在双引号内
				if(!bInDouble) {
					// 若在单引号内
					if(bInSingle) {
						// 解析''
						// 若计数加1小于输入字符串的长度，且输入字符串在计数加1位置的字符为单引号
						if((iOffSet + 1) < s.length() && s.charAt(iOffSet + 1) == '\'') {
							// 计数加1
							iOffSet++;
						} else {
							// 否则，计数加1，跳出循环
							iOffSet++;
							break;
						}
					}
					// 是否在单引号中设为真
					bInSingle = true;
				}
			}

			// 若为双引号
			if(c == '"') {
				// 若不在单引号中
				if(!bInSingle) {
					// 若在双引号中
					if(bInDouble) {
						// 计数加1，跳出循环
						iOffSet++;
						break;
					}
					// 是否在双引号中设为真
					bInDouble = true;
				}
			}

			// 不在单引号内且不在双引号内
			if((!bInDouble) && (!bInSingle)) {

				// 计数加1
				iOffSet++;
				// 若计数小于输入字符串的长度
				if(iOffSet < s.length()) {

					String ss = "" + c + s.charAt(iOffSet);
					String sss = null;

					if(iOffSet < s.length() - 1) {
						sss = ss + s.charAt(iOffSet + 1);
					}
					// 循环比较是否为特殊字符串//若找到，则跳出循环

					if(isSpecialSt(ss, iOffSet - 1, s) || (sss != null && isSpecialSt(sss, iOffSet - 1, s))) {
						return s.substring(0, iOffSet - 1);
					}

				}
				// 计数减1
				iOffSet--;

				if(!bFound) {
					// 循环查找是否为特殊字符
					if(isSpecialChar(c)) {
						// 若找到，则跳出循环
						bFound = true;
						break;
					}
				}

				// 若找到，则跳出循环
				if(bFound) {
					break;
				}
			}
			// 计数加1
			iOffSet++;
		}

		// 将输入字符串从0到计数位进行返回
		// System.out.println(s.substring(0, iOffSet));
		log.debug("nc.bs.mw.sqltrans.Express_str.parseWord Over");
		return s.substring(0, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-15 21:15:43)
	 * 
	 * @param param
	 *            java.lang.String
	 */
	public void setExpress(String param) {
		log.debug("nc.bs.mw.sqltrans.Express_str.setExpress");
		// this.data = data;//设置参数表
		pozitie = 0;
		m_definitie = parseSql(param); // 公式表达式.,char[]格式

		m_definitie = trim(m_definitie);

		m_Arbore = expresie_or(); // 表达式

		log.debug("nc.bs.mw.sqltrans.Express_str.setExpress Over");
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 16:32:05)
	 * 
	 * @return java.lang.String
	 * @param stArray
	 *            java.lang.String[]
	 */
	public String[] trim(String[] stArray) {
		log.debug("nc.bs.mw.sqltrans.Express_str.trim");
		String s = "";
		Vector vec = new Vector();
		for(int i = 0; i < stArray.length; i++) {
			String st = stArray[i].trim();
			if(st.equalsIgnoreCase("and") || st.equalsIgnoreCase("or") || st.equals(")")) {
				if(s.length() > 0) {
					vec.addElement(s);
				}
				vec.addElement(st);
				s = "";
			} else if(st.equals("(")) {
				int end = getEndIndex(stArray, i);

				if(end < stArray.length - 1 && !stArray[end + 1].equalsIgnoreCase("and")
						&& !stArray[end + 1].equalsIgnoreCase("or") && !stArray[end + 1].equalsIgnoreCase(")")) {
					end = getKuoEndIndex(stArray, i);
					for(int index = i; index <= end; index++) {
						s += stArray[index];
					}
					vec.addElement(s);
					s = "";
					i = end;
				} else {
					if(s.length() > 0) {
						vec.addElement(s);
					}
					vec.addElement(st);
					s = "";
				}
			} else {
				s += st;
			}
		}
		if(s.length() > 0) {
			vec.addElement(s);
		}
		String[] newArray = new String[vec.size()];
		vec.copyInto(newArray);
		log.debug("nc.bs.mw.sqltrans.Express_str.trim Over");
		return newArray;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-19 16:32:05)
	 * 
	 * @return java.lang.String
	 * @param stArray
	 *            java.lang.String[]
	 */
	public String[] trim_old(String[] stArray) {
		log.debug("nc.bs.mw.sqltrans.Express_str.trim_old");
		String s = "";
		Vector vec = new Vector();
		for(int i = 0; i < stArray.length; i++) {
			String st = stArray[i].trim();
			if(st.equalsIgnoreCase("and") || st.equalsIgnoreCase("or")) {
				if(s.length() > 0) {
					vec.addElement(s);
				}
				vec.addElement(st);
				s = "";
			} else if(st.equals("(")) {
				if(i > 0 && !stArray[i - 1].equalsIgnoreCase("and") && !stArray[i - 1].equalsIgnoreCase("or")) {
					int end = getEndIndex(stArray, i);

					for(int index = i; index <= end; index++) {
						s += stArray[index];
					}
					vec.addElement(s);
					s = "";
					i = end;
				} else {
					if(s.length() > 0) {
						vec.addElement(s);
					}
					vec.addElement(st);
					s = "";
				}
			} else {
				s += st;
			}
		}

		String[] newArray = new String[vec.size()];
		vec.copyInto(newArray);
		log.debug("nc.bs.mw.sqltrans.Express_str.trim_old Over");
		return newArray;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-15 21:15:43)
	 * 
	 * @param param
	 *            java.lang.String
	 */
	private Vector vexp(Expresion_str a) {
		log.debug("nc.bs.mw.sqltrans.Express_str.vexp");
		double v;
		// if (a.operatie=null) {code=10;return 0;}
		switch(a.operatie){

		case '|': {
			return op_or(vexp(a.left), vexp(a.right));
			/*
			 * if( vexp(a.left)>0 || vexp(a.right)>0 ) return 1; else return -1;
			 */
		}
		case '&': {
			return op_and(vexp(a.left), vexp(a.right));
			/*
			 * if( vexp(a.left)>0 && vexp(a.right)>0 ) return 1; else return -1;
			 */
		}
		case '@':
			return dealVector(a.valoare);
			// logical operations evaluation

		default: {
			/*
			 * if (a.left == null && a.right == null && a.operatie == '`') {
			 * double d = 0; if (a.valoarestr != null) { String str = (String)
			 * data.get(a.valoarestr.trim()); if (str != null && str.length() !=
			 * 0)//表明是函数里的参数 d = Double.valueOf(str.trim()).doubleValue(); else
			 * System.out.println("没有识别出:" + a.valoarestr); code =
			 * UNDEFINED_VARIABLE; } else { System.out.println("没有识别出:" +
			 * a.valoarestr); code = UNDEFINED_VARIABLE; } return d; } else
			 * return a.valoare;
			 */
			log.debug("nc.bs.mw.sqltrans.Express_str.vexp Over");
			return a.valoare;
		}
		}
	}
}