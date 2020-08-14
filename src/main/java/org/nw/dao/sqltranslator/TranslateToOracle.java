package org.nw.dao.sqltranslator;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TranslateToOracle extends TranslatorObject {
	private static final Log log = LogFactory.getLog(TranslateToOracle.class);

	/**
	 * TransToOracle 构造子注释。
	 */
	public TranslateToOracle() {
		// 定义目标数据库为Oracle
		super(ORACLE);
		// 定义函数列表
		m_apsFunList = m_apsOracleFunctions;
		// 定义异常列表
		m_apiErrorList = m_apiOracleError;
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle Over");
	}

	/**
	 * 处理带有空值的数组 创建日期：(00-5-19 15:47:31) Author:ljq
	 * 
	 * @param asInput
	 *            java.lang.String[]
	 */
	private String[] dealArray(String[] asInput) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealArray");
		Vector v_re = new Vector(); // 与返回结果对应的Vector

		// 记录出现空值的位置

		if(asInput == null || asInput.length <= 0) // 如果为空或长度为零
			return null;

		for(int i = 0; i < asInput.length; i++) {
			// 如果asInput[i]不为空 且不全为空格或换行符
			if(asInput[i] != null && asInput[i].trim().length() > 0) {
				v_re.addElement(asInput[i].trim());
			}
		}

		if(v_re == null || v_re.size() <= 0)
			return null;

		String[] as = new String[v_re.size()];

		v_re.copyInto(as);

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealArray Over");
		return as;
	}

	/**
	 * 获取转换后的sql语句 Author:ljq Date:
	 */
	public String getSql() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.getSql");
		// 翻译SQL语句
		translateSql();

		String sResult = "";

		if(isTrigger) {
			sResult = m_sResorceSQL;

			// /StringUtils.re
			org.nw.vo.pub.util.StringOperator stOp = new org.nw.vo.pub.util.StringOperator(sResult);

			stOp.replaceAllString("\r\n", "\n");

			sResult = stOp.toString();
			// stOp.replaceAllString("\t", "\n");

			// if (!sResult.endsWith(";"))
			// {
			// sResult += ";";
			// }
		} else {
			// 若翻译结果为空，则返回空
			if(m_sbDestinationSql == null)
				return null;

			sResult = m_sbDestinationSql.toString();

			// 若翻译结果以分号结尾，则去掉分号
			if(sResult.endsWith(";")) {
				sResult = sResult.substring(0, sResult.length() - 1);
			}
			sResult = sResult.replaceAll("with\\(nolock\\)", "");

			org.nw.vo.pub.util.StringOperator stOp = new org.nw.vo.pub.util.StringOperator(sResult);
			/*
			 * stOp.replaceAllString(" '' "," ' ' "); stOp.replaceAllString(",
			 * '',",", ' ',"); stOp.replaceAllString("( '',","( ' ',");
			 * stOp.replaceAllString("('',","(' ',"); stOp.replaceAllString(",
			 * '')",", ' ')");
			 */
			if(sResult.indexOf("ltrim_case") >= 0) {
				stOp.replaceAllString("ltrim_case", "ltrim");
			}
			if(sResult.indexOf("rtrim_case") >= 0) {
				stOp.replaceAllString("rtrim_case", "rtrim");
			}

			sResult = stOp.toString();

		}

		// System.out.println("ORACLE SQL语句:");
		// System.out.println(sResult);
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.getSql Over");
		return sResult;
	}

	/**
	 * 转换Create语句
	 */
	private void translateCreate() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateCreate");
		// m_sbDestinationSql = new StringBuffer(m_sResorceSQL);

		m_sbDestinationSql = new StringBuffer("");
		for(int i = 0; i < m_asSqlWords.length; i++) {
			if(m_asSqlWords[i].equalsIgnoreCase("varchar")) {
				m_asSqlWords[i] = "varchar2";
			} else if(m_asSqlWords[i].equalsIgnoreCase("nvarchar")) {
				m_asSqlWords[i] = "varchar2";
			} else if(m_asSqlWords[i].equalsIgnoreCase("nchar")) {
				m_asSqlWords[i] = "char";
			} else if(m_asSqlWords[i].equalsIgnoreCase("float")) {
				m_asSqlWords[i] = "number";
			} else if(m_asSqlWords[i].equalsIgnoreCase("datetime") || m_asSqlWords[i].equalsIgnoreCase("smalldatetime")) {
				m_asSqlWords[i] = "date";
			}
			m_sbDestinationSql.append(" " + m_asSqlWords[i]);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateCreate Over");
	}

	/**
	 * 转换Delete语句
	 */
	private StringBuffer translateDelete(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateDelete");
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateDelete Over");
		return translateSelect(asSqlWords);
	}

	/**
	 * 转换Drop语句
	 */
	private void translateDrop() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateDrop");
		m_sbDestinationSql = new StringBuffer(m_sResorceSQL);
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateDrop Over");
	}

	/**
	 * 根据函数对照表进行函数转换
	 */
	private void translateFunction() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunction");
		// 初始临时字符串
		String sWord = null;
		// 初始计数器
		int iOffSet = -1;
		// 若计数器小于分析结果的长度
		while(iOffSet < m_asSqlWords.length) {
			// 计数器加1
			iOffSet++;
			// 取得计数位置的字符串
			sWord = m_asSqlWords[iOffSet];
			// 若为最后一个字符串，则退出
			if((iOffSet + 1) >= m_asSqlWords.length)
				break;
			// 找到函数
			// 若下一个字符是左括弧，则取得并保存对应的函数
			if(m_asSqlWords[iOffSet + 1].equals("(")) {
				m_asSqlWords[iOffSet] = getFunction(sWord);
				// 计数器加1
				iOffSet++;
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunction Over");
	}

	/**
	 * 转换"||" 参数: off 偏移量 返回: 偏移量 规则: 在Oracle中无需转换
	 */
	private int translateII(int off) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateII");
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateII Over");
		return off;
	}

	/**
	 * 转换Insert语句
	 */
	private StringBuffer translateInsert(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateInsert");
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateInsert Over");
		return translateSelect(asSqlWords);
	}

	/**
	 * 转换连接语句 参数: String[] Join语句,包括左右连接
	 * 规则: select select_exp from t1 left outer join t2 on t1.f1=t2.f1 and
	 * t1.f2=t2.f2 right outer join t3 on t1.f1=t3.f1 and t1.f2=t3.f2 where
	 * t1.f3=t2.f3; -> select select_exp from t1,t2,t3 where t1.f1=t2.f1(+) and
	 * t1.f2=t2.f2(+) and t1.f1(+)=t3.f1 and t1.f2(+)=t3.f2 and t1.f3=t2.f3;
	 */
	private void translateJoin(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateJoin");
		TransUnit aTransUnit = null;

		try {
			if(m_sLeftWhere == null || m_sLeftWhere.trim().length() < 1) {
				m_sLeftWhere = "where";
			}

			if(m_sLeftTable == null || m_sLeftTable.trim().length() < 1) {
				m_sLeftTable = "";
			}

			// int joinType = 0;

			int iOffSet = 0;
			String s0 = new String();
			String sWord = new String();

			while(iOffSet < asSqlWords.length) {
				// 取得当前字符串
				sWord = asSqlWords[iOffSet];
				// 若为连接 或 ，
				if(isOuterJoin(sWord, asSqlWords[iOffSet + 1], asSqlWords[iOffSet + 2])
						|| isInnerJoin(sWord, asSqlWords[iOffSet + 1]) || sWord.equalsIgnoreCase(",")) {
					int addOffset = 1;

					if(sWord.equalsIgnoreCase("left") || sWord.equalsIgnoreCase("right")) {
						addOffset = 3;
					} else if(sWord.equalsIgnoreCase("inner")) {
						addOffset = 2;
					}

					iOffSet += addOffset;

					// 处理子查询

					if(iOffSet < asSqlWords.length - 1 && asSqlWords[iOffSet].trim().equalsIgnoreCase("(")
							&& asSqlWords[iOffSet + 1].trim().equalsIgnoreCase("select")) {
						aTransUnit = dealJoinSelect(asSqlWords, sWord, iOffSet);
						iOffSet = aTransUnit.getIOffSet();
						// if(asSqlWords[iOffSet+2].trim().equalsIgnoreCase("on")){
						// m_sLeftTable+=" "+asSqlWords[iOffSet+1];
						// iOffSet+=2;
						// }
					} else {
						asSqlWords = trimKuohao(asSqlWords, iOffSet);
						aTransUnit = dealJoinTable(asSqlWords, asSqlWords[iOffSet], iOffSet);
						iOffSet = aTransUnit.getIOffSet();
					} /*
					 * //确定位置，记录字符串 if (asSqlWords[iOffSet +
					 * 1].equalsIgnoreCase("as")) //遇到as,别名 { m_sLeftTable +=
					 * " " +
					 * asSqlWords[iOffSet + 2];
					 * htJoinTableName.put(""+(iOffSet+2),asSqlWords[iOffSet+2]);
					 * iOffSet += 3; } else { //没有遇到as， 也没有遇到on,别名 if
					 * (isTableOtherName(asSqlWords[iOffSet + 1])) {
					 * m_sLeftTable += " " + asSqlWords[iOffSet + 1];
					 * //左表+','+右表+右表别名
					 * htJoinTableName.put(""+(iOffSet+1),asSqlWords[iOffSet+1]);
					 * iOffSet += 2; } else iOffSet += 1; //没有遇到as， 但遇到on,
					 * 没有别名 }
					 */
				}
				// 若当前字符串为“on”
				else if(sWord.equalsIgnoreCase("on")) // 连接条件开始
				{
					// aTransUnit = dealOn(asSqlWords, sWord, iOffSet,
					// joinType);
					aTransUnit = dealOn(asSqlWords, sWord, iOffSet);
					iOffSet = aTransUnit.getIOffSet();
				}

				// 若计数小于输入字符串集，且当前字符串为“where”
				else if(sWord.equalsIgnoreCase("where")) {
					iOffSet++;
					if(!m_sLeftWhere.trim().equalsIgnoreCase("where")) {
						m_sLeftWhere += " and ";
					}
					m_sLeftWhere += " (";
					while(iOffSet < asSqlWords.length) {
						m_sLeftWhere += " " + asSqlWords[iOffSet]; // 拼接连接条件
						iOffSet++;
					}
					m_sLeftWhere += " ) ";
				}

				// 处理CASE WHEN
				else if(sWord.equalsIgnoreCase("case")) {
					aTransUnit = dealCaseWhen(asSqlWords, sWord, iOffSet);

					iOffSet = aTransUnit.getIOffSet();

					String sSql = aTransUnit.getSql();

					translateJoin(parseSql(sSql));
				}

			}
		} catch(Exception e) {
			throw e;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateJoin Over");
	}

	/**
	 * 处理连接更新 创建日期：(00-6-9 8:38:35)
	 * 
	 * @return java.lang.String[]
	 * @param asSqlWords
	 *            java.lang.String[]
	 *            格式： 第一种情况： update table1 set col1=b.col2 from table1 a, table2
	 *            b where
	 *            a.col3=b.col3 -> update table1 a set col1=(select b2.col2 from
	 *            table2 b
	 *            where a.col3=b.col3)
	 *            第二种情况 update table1 set col1=col1+常量a, a.col2=b.col2+a.col2
	 *            from table1
	 *            a,table2 b
	 *            where a.col3=b.col3 and a.col2=常量 -> update table1 a set
	 *            col1=col1+常量a,col2=(select b2.col2+a.col2 from table2 b
	 *            where a.col3=b.col3 and a.col2=常量b)
	 *            where a.col2=常量b
	 */
	public String[] translateJoinUpdate(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateJoinUpdate");

		int iOffSet = 0; // 偏移量
		int iOffSet1 = 1; // 取表名的偏移量
		String[] asWords = asSqlWords;
		// String sSql = ""; //返回值
		boolean bFind = false;
		// String sLeftField = "";
		// String sRightField = "";
		// java.util.Vector vSetList = new java.util.Vector(); //存放set语句
		String sTableName = ""; // 表名
		String sTableAlias = ""; // 表的别名
		// String s = ""; //中间变量
		// Vector vecTable = new Vector();

		// 是否存在连接更新，并取得更新表的表名和别名
		if(iOffSet1 < asSqlWords.length && iOffSet1 + 5 < asSqlWords.length && asSqlWords[iOffSet1].equals("/")
				&& asSqlWords[iOffSet1 + 1].equals("*") && asSqlWords[iOffSet1 + 2].equals("+")) {
			iOffSet1 += 3;
			while(!asSqlWords[iOffSet1].equals("*") && !asSqlWords[iOffSet1 + 1].equals("/")) {
				iOffSet1 += 1;
			}
			iOffSet1 += 2;
		}
		sTableName = asWords[iOffSet1];

		int iSelectNum = 0;
		int iFromNum = 0;

		// 计数小于输入字符串集的长度
		while(iOffSet < asWords.length) {
			// 若当前字符串是“from”
			if(asWords[iOffSet].equalsIgnoreCase("from")) {
				iFromNum++;

				if(iFromNum > iSelectNum) {
					// 记录当前位置

					iOffSet++;
					// 计数小于输入字符串集的长度
					while(iOffSet < asWords.length) {
						// 若当前字符串等于表名
						if(asWords[iOffSet].equalsIgnoreCase(sTableName)) {
							// 当前位置加1
							// iOffSet++;
							// 若当前字符串为逗号，则跳出循环
							// if (iOffSet < asWords.length
							// && (asWords[iOffSet].equalsIgnoreCase(",")
							// || asWords[iOffSet].equalsIgnoreCase("where")))
							{
								if(iOffSet >= 1
										&& (asWords[iOffSet - 1].equalsIgnoreCase(",") || asWords[iOffSet - 1]
												.equalsIgnoreCase("from"))) {
									// sTableAlias = asWords[iOffSet - 1];

									if(iOffSet + 1 < asWords.length) {
										if(asWords[iOffSet + 1].equalsIgnoreCase("as")) {
											if(iOffSet + 2 < asWords.length) {
												sTableAlias = asWords[iOffSet + 2];
											}
										} else {
											if(!asWords[iOffSet + 1].equals(",")
													&& !asWords[iOffSet + 1].equalsIgnoreCase("where")
													&& !asWords[iOffSet + 1].equalsIgnoreCase("left")
													&& !asWords[iOffSet + 1].equalsIgnoreCase("right")
													&& !asWords[iOffSet + 1].equals("(")
													&& !asWords[iOffSet + 1].equals(")")) {
												sTableAlias = asWords[iOffSet + 1];
											}
										}
									}
								} /*
								 * else if (iOffSet >= 2 && (asWords[iOffSet -
								 * 2].equalsIgnoreCase(","))) { sTableAlias =
								 * ""; }
								 */
								else if(iOffSet >= 2
										&& (asWords[iOffSet - 2].equalsIgnoreCase(",") || asWords[iOffSet - 2]
												.equalsIgnoreCase("from"))) {
									sTableName = asWords[iOffSet - 1];
									sTableAlias = asWords[iOffSet];
								} else if(iOffSet >= 3
										&& (asWords[iOffSet - 3].equalsIgnoreCase(",") || asWords[iOffSet - 3]
												.equalsIgnoreCase("from"))) {
									sTableName = asWords[iOffSet - 2];
									sTableAlias = asWords[iOffSet];
								} /*
								 * else { //sTableName=asWords[iOffSet-2];
								 * sTableAlias = sTableName; }
								 */
								break;
							}

						} else {
							iOffSet++;
						}
					}
					break;
				}
			}

			if(asWords[iOffSet].equalsIgnoreCase("select")) {
				iSelectNum++;
			}

			iOffSet++;

		} // while结束处

		if(iFromNum > iSelectNum) {
			bFind = true;
		}
		if(!bFind) // 没有发现连接更新或子查询
		{
			return asWords;
		} else {
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateJoinUpdate Over");
			return translateJoinUpdate(asSqlWords, sTableName, sTableAlias);
		}
	}

	/**
	 * 翻译Select语句,进行: 函数转换 连接转换(左连接) 转换CASE WHEN 语句 模式匹配
	 */
	private StringBuffer translateSelect(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateSelect");
		int iOffSet = 0;
		String sSql = new String();
		String sWord = "";
		String sPreWord;
		int iLBracket;
		int iRBracket;

		boolean dontHaveWhere = true;

		//added by hegy
		int orderbyFrom = -1;
		int orderbyTo = -1;

		boolean ifTop = false;
		// boolean ifWhere = false;

		TransUnit aTransUnit = null;

		String rowNum = "";

		while(iOffSet < asSqlWords.length) {
			sPreWord = sWord;
			// 取得当前单词
			sWord = asSqlWords[iOffSet];

			// 在此对函数进行处理
			// 如果当前单词为函数名称
			if((iOffSet + 1 < asSqlWords.length) && isFunctionName(sWord, asSqlWords[iOffSet + 1])) {
				aTransUnit = dealFunction(asSqlWords, sWord, iOffSet);

				iOffSet = aTransUnit.getIOffSet();

				if(iOffSet > asSqlWords.length - 1) {
					// 函数嵌套
					return null;
				}
			}
			// 处理Oracle优化关键字
			else if(iOffSet < asSqlWords.length && iOffSet + 5 < asSqlWords.length && asSqlWords[iOffSet].equals("/")
					&& asSqlWords[iOffSet + 1].equals("*") && asSqlWords[iOffSet + 2].equals("+")) {
				iOffSet += 3;
				m_sbDestinationSql.append(" /*+ ");
				while(!asSqlWords[iOffSet].equals("*") && !asSqlWords[iOffSet + 1].equals("/")) {
					m_sbDestinationSql.append(asSqlWords[iOffSet] + " ");
					iOffSet += 1;
				}
				iOffSet += 2;
				m_sbDestinationSql.append(" */ ");
			}
			// (字段1,字段2) in (…)的支持
			else if(sWord.equalsIgnoreCase("&&")) {
				m_sbDestinationSql.append(",");
				iOffSet += 1;
			}
			// 处理PI()
			else if(iOffSet < asSqlWords.length && sWord.equalsIgnoreCase("PI") && asSqlWords[iOffSet + 1].equals("(")
					&& asSqlWords[iOffSet + 2].equals(")")) {
				m_sbDestinationSql.append(" 3.1415926535897931");
				iOffSet += 3;
			}
			// 处理取模%
			else if(iOffSet + 2 < asSqlWords.length && asSqlWords[iOffSet + 1].equals("%")) {
				m_sbDestinationSql.append(" mod(" + sWord + "," + asSqlWords[iOffSet + 2] + ")");
				iOffSet += 3;
			}
			// 处理getdate()
			else if(iOffSet + 2 < asSqlWords.length && sWord.equalsIgnoreCase("getdate")
					&& asSqlWords[iOffSet + 1].equals("(") && asSqlWords[iOffSet + 2].equals(")")) {
				m_sbDestinationSql.append(" sysdate");
				iOffSet += 3;
			}
			// 处理模式匹配符
			else if(iOffSet < asSqlWords.length && sWord.equalsIgnoreCase("like")) {
				aTransUnit = dealLike(asSqlWords, sWord, iOffSet);
				iOffSet = aTransUnit.getIOffSet();
			}
			// 处理top
			else if(sWord.equalsIgnoreCase("top")) {
				ifTop = true;
				rowNum = asSqlWords[iOffSet + 1];
				iOffSet += 2;

				// 保存前一个单词
				sPreWord = sWord;
				// 取得当前单词
				sWord = asSqlWords[iOffSet];
			}
			// 处理CASE WHEN
			else if(getDbVersion() <= 8 && sWord.equalsIgnoreCase("case")
					&& !asSqlWords[iOffSet + 1].equalsIgnoreCase("when")) {
				// 当为oracle8.x翻译
				aTransUnit = dealCaseWhen(asSqlWords, sWord, iOffSet);
				iOffSet = aTransUnit.getIOffSet();
				sSql = aTransUnit.getSql();
				translateSelect(parseSql(sSql));
			}
			// 处理左右连接, 内连接
			else if((iOffSet + 2 < asSqlWords.length // && getDbVersion()<=8
					&& isOuterJoin(sWord, asSqlWords[iOffSet + 1], asSqlWords[iOffSet + 2]))
					|| (iOffSet + 1 < asSqlWords.length && isInnerJoin(sWord, asSqlWords[iOffSet + 1]))) {
				htJoinTableName = new Hashtable();
				m_TabName = asSqlWords[iOffSet - 1];
				aTransUnit = dealJoin(asSqlWords, sWord, iOffSet, ifTop, dontHaveWhere, rowNum);

				iOffSet = aTransUnit.getIOffSet();

				dontHaveWhere = aTransUnit.isDontHaveWhere();

				if(iOffSet >= asSqlWords.length) {
					break;
				}
			}
			// 处理表的别名,
			else if(iOffSet < asSqlWords.length && sWord.equalsIgnoreCase("as")
					&& !asSqlWords[0].equalsIgnoreCase("create")) {
				iOffSet++;
			}
			// 处理子查询
			else if(iOffSet < asSqlWords.length && sWord.equalsIgnoreCase("select") && iOffSet > 0
					&& asSqlWords[iOffSet - 1].equalsIgnoreCase("(")) {
				aTransUnit = dealSelect(asSqlWords, sWord, iOffSet);

				iOffSet = aTransUnit.getIOffSet();
			} else if(sWord.equals(";")) {
				iOffSet++;
			} else if(sWord.equalsIgnoreCase("from") && iOffSet < asSqlWords.length - 1
					&& asSqlWords[iOffSet + 1].equals("(")
					// && !asSqlWords[iOffSet + 2].equalsIgnoreCase("select")
					&& !getFirstTrueWord(asSqlWords, iOffSet).equalsIgnoreCase("select")) {
				/*
				 * aTransUnit = dealFrom(asSqlWords, sWord, iOffSet);
				 * iOffSet = aTransUnit.getIOffSet();
				 * asSqlWords = aTransUnit.getSqlArray();
				 */
				asSqlWords = trimKuohao(asSqlWords, iOffSet + 1);
			} else {
				if(iOffSet < asSqlWords.length) {

					//added by hgy
					if(sWord.equals("order") && (iOffSet + 1 < asSqlWords.length)) {
						if("by".equals(asSqlWords[iOffSet + 1])) {
							orderbyFrom = m_sbDestinationSql.length();
						}
					}
					aTransUnit = dealOther(asSqlWords, sWord, iOffSet, ifTop, dontHaveWhere, rowNum, sPreWord);

					iOffSet = aTransUnit.getIOffSet();

					dontHaveWhere = aTransUnit.isDontHaveWhere();
					//added by hegy
					if(sWord.equals("by") && "order".equals(asSqlWords[iOffSet - 1])) {
						orderbyTo = m_sbDestinationSql.length();
					}
				} else
					break;
			}
		}

		/*
		 * 原为NCv3.1修改，但在top 语句为子查询时有问题->嵌套连接有错 if (dontHaveWhere && ifTop) {
		 * String m_DestinationSql= m_sbDestinationSql.toString();
		 * m_sbDestinationSql = new StringBuffer("select * from (");
		 * m_sbDestinationSql.append(m_DestinationSql);
		 * m_sbDestinationSql.append(" ) where "); m_sbDestinationSql.append( "
		 * rownum < " + (Integer.valueOf(rowNum).intValue() + 1)); }
		 */

		if(dontHaveWhere && ifTop) {

			//modifid by hegy
			if(orderbyFrom > -1 && orderbyTo > -1) {
				//int index = m_sbDestinationSql.indexOf("order by");
				int index = orderbyFrom;
				//                if (index > -1) {
				StringBuffer first = new StringBuffer(m_sbDestinationSql.substring(0, index));
				StringBuffer second = new StringBuffer(m_sbDestinationSql.substring(index, m_sbDestinationSql.length()));
				first.append(" where ");
				first.append(" rownum<" + (Integer.valueOf(rowNum).intValue() + 1));
				first.append(" ").append(second.toString());
				m_sbDestinationSql = first;
				//                } else {

			}

			else {
				m_sbDestinationSql.append(" where ");
				m_sbDestinationSql.append(" rownum<" + (Integer.valueOf(rowNum).intValue() + 1));
			}
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateSelect Over");
		return m_sbDestinationSql;
	}

	/**
	 * 根据语句类型进行转换
	 */

	private void translateSql() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateSql");

		m_sbDestinationSql = new StringBuffer();
		m_sLeftWhere = new String();
		m_sLeftTable = new String();

		// 分析sql语句,得到sql单词序列
		// m_asSqlWords = parseSql(m_sResorceSQL);
		// 若分析结果为空，则将目标字符串至空，并返回
		if(m_asSqlWords == null) {
			m_sbDestinationSql = null;
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateSql Over");
			return;
		}
		// create , replace trriger
		if((m_asSqlWords.length >= 2
				&& (m_asSqlWords[0].equalsIgnoreCase("create") || m_asSqlWords[0].equalsIgnoreCase("replace")) && m_asSqlWords[1]
					.equalsIgnoreCase("trigger"))
				|| (m_asSqlWords.length >= 4 && m_asSqlWords[3].equalsIgnoreCase("trigger"))) // create or replace
		// trriger
		{
			isTrigger = true;
		} else {
			isTrigger = false;
		}

		if(!isTrigger) {
			// 函数转换
			translateFunction();

			// 根据sql语句类型进行转换
			switch(getStatementType()){
			case SQL_SELECT:
				translateSelect(m_asSqlWords);
				break;
			case SQL_INSERT:
				translateInsert(m_asSqlWords);
				break;
			case SQL_CREATE:
				translateCreate();
				break;
			case SQL_DROP:
				translateDrop();
				break;
			case SQL_DELETE:
				translateDelete(m_asSqlWords);
				break;
			case SQL_UPDATE:
				translateUpdate(m_asSqlWords);
				break;
			}
		}
	}

	/**
	 * 转换Update语句
	 */
	private StringBuffer translateUpdate(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateUpdate");

		int iOffSet = 0; // 偏移量
		String[] asWords = null;
		String sSql = "";
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBracket;
		String s = new String();

		// 处理连接更新
		asWords = translateJoinUpdate(asSqlWords);

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateUpdate Over");
		return translateSelect(asWords);
	}

	// ErrorCode对照表,列出sqlServer ErrorCode与Oracle
	// ErrorCode的对应关系,格式:{Oracle,SqlServer}
	private int[][] m_apiOracleError = { { 942, 208 }, // 表或视图不存在
			{ 907, 2715 }, // 函数不存在
			{ 904, 207 }, // 无效的列名
			{ 398, 205 }, // 使用union的语句和目标列表应具有相同数目的表达式
			{ 516, 213 }, // 插入数据和表数据类型不一致
			{ 2627, 1 }, // 不能插入相同主键的记录
			{ 515, 1400 }, // 列值不能为空
			{ 8152, 1401 } // 插入的值对于列过大
	};

	// 函数对照表,列出sqlServer函数与Oracle函数的对应关系,
	private String[][] m_apsOracleFunctions = {
			// {"coalesce","nvl"},
			{ "isnull", "nvl" }, { "substring", "substr" }, { "ceiling", "ceil" }
	// {"left","substr"},//left(f,n)->substr(f,1,n)
	// {"right","substr"},//right(f,n)->substr(f,length(f)-n+1,n)
	// {"square","power"}//square(f)->power(f,2)
	// {"cast","to_..."}//cast(f as type)-> to_type(f)
	};

	Express_str express = new Express_str();

	private Hashtable htJoinTableName = null;

	public int INNERJOIN = 3;

	boolean isTrigger = false;

	public int LEFTJOIN = 1;

	public int RIGHTJOIN = 2;

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealCaseWhen(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealCaseWhen");

		String sSql = "";

		if(iOffSet < asSqlWords.length) {
			TransUnit aTransUnit = getSubSql(asSqlWords, "case", "end", iOffSet);
			String[] newCaseSql = aTransUnit.getSqlArray();
			iOffSet = aTransUnit.getIOffSet();

			sSql = preTranCaseWhen(newCaseSql);

			sSql = translateCaseWhen(parseSql(sSql));

			org.nw.vo.pub.util.StringOperator stOp = new org.nw.vo.pub.util.StringOperator(sSql);
			stOp.replaceAllString("ltrim", "ltrim_case");
			stOp.replaceAllString("rtrim", "rtrim_case");

			sSql = stOp.toString();

			iOffSet++;

		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealCaseWhen Over");
		return new TransUnit(null, sSql, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealFrom(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealFrom");
		int includeIndex = iOffSet + 1;
		int includeNum = 0;
		Vector vec = new Vector();

		while(asSqlWords[includeIndex].equals("(") && !asSqlWords[includeIndex + 1].equalsIgnoreCase("select")) {
			includeNum++;
			includeIndex++;
		}

		int leftIndex = 0;
		int rightIndex = 0;

		includeIndex = iOffSet + 1;

		while(includeIndex < asSqlWords.length && includeNum > 0) {
			if(asSqlWords[includeIndex].equals("(")) {
				leftIndex++;
				if(leftIndex == 1) {
					asSqlWords[includeIndex] = "";
				}
			} else if(asSqlWords[includeIndex].equals(")")) {
				rightIndex++;
				if(rightIndex == leftIndex) {
					asSqlWords[includeIndex] = "";
					includeNum--;
					includeIndex = iOffSet + 1;
					leftIndex = 0;
					rightIndex = 0;
					break;
				}
			}
			includeIndex++;
		}

		m_sbDestinationSql.append(" " + asSqlWords[iOffSet]);

		iOffSet++;

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealFrom Over");
		return new TransUnit(asSqlWords, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealFunction(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealFunction");

		Vector vec = new Vector();
		vec.addElement(asSqlWords[iOffSet]);

		// vec.addElement(asSqlWords[iOffSet + 1]);
		// iOffSet += 2;
		iOffSet += 1;
		TransUnit aTransUnit = getSubSql(asSqlWords, "(", ")", iOffSet);
		String[] newFuncSql = aTransUnit.getSqlArray();
		iOffSet = aTransUnit.getIOffSet() + 1;

		for(int i = 0; i < newFuncSql.length; i++) {
			vec.addElement(newFuncSql[i]);
		}
		newFuncSql = new String[vec.size()];
		vec.copyInto(newFuncSql);

		if(sWord.equalsIgnoreCase("left")) {
			translateFunLeft(newFuncSql);
		} else if(sWord.equalsIgnoreCase("right")) {
			translateFunRight(newFuncSql);
		} else if(sWord.equalsIgnoreCase("square")) {
			translateFunSquare(newFuncSql);
		} else if(sWord.equalsIgnoreCase("cast")) {
			translateFunCast(newFuncSql);
		} else if(sWord.equalsIgnoreCase("coalesce")) {
			translateFunCoalesce(newFuncSql);
		} else if(sWord.equalsIgnoreCase("ltrim")) {
			translateFunLtrim(newFuncSql);
		} else if(sWord.equalsIgnoreCase("rtrim")) {
			translateFunRtrim(newFuncSql);
		} else if(sWord.equalsIgnoreCase("patindex")) {
			translateFunPatindex(newFuncSql);
		} else if(sWord.equalsIgnoreCase("len")) {
			translateFunLen(newFuncSql);
		} else if(sWord.equalsIgnoreCase("round")) {
			translateFunRound(newFuncSql);
		} else if(sWord.equalsIgnoreCase("convert")) {
			translateFunConvert(newFuncSql);
		} else if(sWord.equalsIgnoreCase("dateadd")) {
			translateFunDateAdd(newFuncSql);
		} else if(sWord.equalsIgnoreCase("datediff")) {
			translateFunDateDiff(newFuncSql);
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealFunction Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealJoin(String[] asSqlWords, String sWord, int iOffSet, boolean ifTop, boolean dontHaveWhere,
			String rowNum) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealJoin");
		try {
			String sSql = "";
			int left = 0;
			int right = 0;

			Vector vec = new Vector();

			while(iOffSet < asSqlWords.length
					&& !((asSqlWords[iOffSet].equalsIgnoreCase("group")
							|| asSqlWords[iOffSet].equalsIgnoreCase("order") || asSqlWords[iOffSet]
								.equalsIgnoreCase("union")) && left == right)) {
				if(asSqlWords[iOffSet].equals("(")) {
					left++;
				}
				if(asSqlWords[iOffSet].equals(")")) {
					right++;
				}

				// sSql += " " + asSqlWords[iOffSet];
				vec.addElement(asSqlWords[iOffSet]);
				iOffSet++;
			}

			String newCaseSql[] = new String[vec.size()];
			vec.copyInto(newCaseSql);

			translateJoin(newCaseSql);

			String s0 = m_sLeftTable;

			if(m_sLeftWhere != null && m_sLeftWhere.trim().length() > 0
					&& !m_sLeftWhere.trim().equalsIgnoreCase("where")) {
				s0 += " " + m_sLeftWhere;
			}

			if(ifTop) {
				dontHaveWhere = false;

				if(m_sLeftWhere == null || m_sLeftWhere.trim().length() < 6) {
					s0 += " where ";
				} else {
					s0 += " and ";
				}

				s0 += " rownum<" + (Integer.valueOf(rowNum).intValue() + 1);
			}

			translateSelect(parseSql(s0));

			TransUnit aTransUnit = new TransUnit(null, sSql, iOffSet);
			aTransUnit.setDontHaveWhere(dontHaveWhere);
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealJoin Over");
			return aTransUnit;
		} catch(Exception e) {
			throw e;
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealJoinSelect(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealJoinSelect");
		// 张森，2001.10.18 加，处理子查询,要求子查询前后必须有扩号
		int leftCount = 1; // (计数
		int rightCount = 0; // )技数

		Vector vec = new Vector();

		if(iOffSet < asSqlWords.length) {
			// int sqlLenth = 0;

			while((leftCount != rightCount) && (iOffSet < asSqlWords.length)) {
				iOffSet++;

				if(asSqlWords[iOffSet].equalsIgnoreCase("(")) {
					leftCount++;
				} else if(asSqlWords[iOffSet].equalsIgnoreCase(")")) {
					rightCount++;
				}

				if(leftCount != rightCount) {
					// sSql += " " + asSqlWords[iOffSet];
					vec.addElement(asSqlWords[iOffSet]);
					// sqlLenth++;
				}

			}
			// 处理子查询
			if(vec.elementAt(0).toString().trim().toLowerCase().startsWith("select")) {
				m_sLeftTable += " ,( ";
				TranslateToOracle newTranslateToOracle = new TranslateToOracle();
				/*
				 * if (sqlLenth < 3) { vec.insertElementAt("(",0);
				 * vec.addElement(")"); }
				 */
				String newCaseSql[] = new String[vec.size()];
				vec.copyInto(newCaseSql);

				newTranslateToOracle.setSqlArray(newCaseSql);

				String newSql = newTranslateToOracle.getSql();
				/*
				 * if (sqlLenth < 3) { newSql = newSql.trim(); newSql =
				 * newSql.substring(1); newSql = newSql.substring(0,
				 * newSql.length() - 1); }
				 */
				m_sLeftTable += newSql;

				m_sLeftTable += " ) ";
				// 张时栋 2002.3.13 新增修改
				iOffSet++;
				if(iOffSet < asSqlWords.length && asSqlWords[iOffSet].equalsIgnoreCase("as")) {
					iOffSet++;
				}
				if(iOffSet < asSqlWords.length) {
					// 2002-03-21 张时栋 临时添加
					// System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& In
					// dealJoinSelect() added : "+asSqlWords[iOffSet]);
					// String temps="&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& In
					// dealJoinSelect() added : "+asSqlWords[iOffSet];
					// Logger.report(temps,this);
					m_sLeftTable += " " + asSqlWords[iOffSet]; // 加上表的别名
					htJoinTableName.put("" + iOffSet, asSqlWords[iOffSet]); // 保存表的别名
					iOffSet++;
				}
				// 修改结束
			} else {
				vec.addElement(sWord);
				if(sWord.equalsIgnoreCase("left") || sWord.equalsIgnoreCase("right")) {
					vec.addElement("outer");
				}

				vec.addElement("join");

				String newCaseSql[] = new String[vec.size()];
				vec.copyInto(newCaseSql);

				translateJoin(newCaseSql);
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealJoinSelect Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealJoinTable(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealJoinTable");

		String tableOtherName = null;
		int start = iOffSet;
		if(iOffSet < asSqlWords.length - 2 && asSqlWords[iOffSet + 1].equalsIgnoreCase("as")) // 遇到as,别名
		{
			tableOtherName = asSqlWords[iOffSet + 2];
			iOffSet += 3;
		} else if(iOffSet < asSqlWords.length - 1 && isTableOtherName(asSqlWords[iOffSet + 1])) // 没有遇到as,别名
		{
			// 没有遇到as， 也没有遇到on,别名
			tableOtherName = asSqlWords[iOffSet + 1];
			iOffSet += 2;
		} else // 没有别名
		{
			iOffSet++;
		}

		if(getIndexOf(m_sLeftTable, sWord.trim()) < 0
				|| (tableOtherName != null && getIndexOf(m_sLeftTable, tableOtherName.trim()) < 0)) {
			// 没有此表
			if(!m_sLeftTable.endsWith(")") && !m_sLeftTable.endsWith("(")) {
				m_sLeftTable += ",";
			}
			m_sLeftTable += sWord; // 左表+','+右表

			// htJoinTableName.put("" + start, sWord);
			if(tableOtherName != null) {
				m_sLeftTable += " " + tableOtherName;
				htJoinTableName.put("" + (iOffSet - 1), tableOtherName);
			} else {
				htJoinTableName.put("" + start, sWord);
			}
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealJoinTable Over");
		return new TransUnit(null, null, iOffSet);

	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealLike(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealLike");
		String s = "";
		String sSql = "";

		if(iOffSet + 1 < asSqlWords.length) {
			s = asSqlWords[iOffSet + 1];
			// if (s.indexOf("^") > 0 && s.indexOf("]") > 0) {
			// sSql = "";
			// sSql = sSql + s.substring(0, s.indexOf("["));
			// sSql = sSql + s.substring(s.indexOf("^") + 1, s.indexOf("]"));
			// sSql = sSql + s.substring(s.indexOf("]") + 1);
			// m_sbDestinationSql.append(" not like " + sSql);
			// }
			// } else if (s.indexOf("[") > 0 && s.indexOf("]") > 0) {
			// sSql = "";
			// sSql = sSql + s.substring(0, s.indexOf("["));
			// sSql = sSql
			// + s.substring(sWord.indexOf("[") + 1, s
			// .indexOf("]"));
			// // sSql = sSql + s.substring(s.indexOf("]") + 1);
			// m_sbDestinationSql.append(" like " + sSql);
			//
			// }
			// else {
			m_sbDestinationSql.append(" like " + s);

			// }
			iOffSet += 2;
		} else {
			m_sbDestinationSql.append(" like ");
			iOffSet++;
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealLike Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealOn(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealOn");

		TransUnit joinInfo = getJoinInfo(asSqlWords, sWord, iOffSet);
		// join 右边的名字
		int joinType = joinInfo.getIOffSet();

		String[] joinTable = joinInfo.getSqlArray();

		String extendCondition = null;

		iOffSet++;

		if(m_sLeftWhere.trim().length() > "where".length()) // 如果where后有条件
		{
			m_sLeftWhere += " and "; // //连接条件+and
		}

		int leftKuo = 0;
		int rightKuo = 0;
		boolean inRight = false;
		// 计数小于输入字符串集的长度，且不是所列字符串之一,说明连接条件没有结束
		while(iOffSet < asSqlWords.length
				&& ((!asSqlWords[iOffSet].equals(",") && joinNotEnd(asSqlWords[iOffSet])) || (asSqlWords[iOffSet]
						.equals(",") && leftKuo != rightKuo))) {
			if(asSqlWords[iOffSet] != null && asSqlWords[iOffSet].trim().length() > 0) {
				if(asSqlWords[iOffSet].equals("(")) {
					leftKuo++;
				} else if(asSqlWords[iOffSet].equals(")")) {
					rightKuo++;
				}
				// 处理on后的子查询条件
				if(asSqlWords[iOffSet].equalsIgnoreCase("select") && iOffSet > 0
						&& asSqlWords[iOffSet - 1].equalsIgnoreCase("(")) {
					TransUnit aTransUnit = getSubSql(asSqlWords, "(", ")", iOffSet);
					String[] newSql = aTransUnit.getSqlArray();
					for(int i = 0; i < newSql.length; i++) {
						m_sLeftWhere += " " + newSql[i];
					}
					iOffSet = aTransUnit.getIOffSet() + 1;
					continue;
				}
				if(joinType == INNERJOIN) {
					m_sLeftWhere += " " + asSqlWords[iOffSet]; // 拼接连接条件
				} else {
					if(asSqlWords[iOffSet].equalsIgnoreCase("and") || asSqlWords[iOffSet].equalsIgnoreCase("or")) {
						m_sLeftWhere += " " + asSqlWords[iOffSet]; // 拼接连接条件
						inRight = false;
					} else if(isBiJiaoFu(asSqlWords[iOffSet])) {
						m_sLeftWhere += " " + asSqlWords[iOffSet]; // 拼接连接条件
						inRight = true;
					} else {
						if(needChangJoin(asSqlWords[iOffSet], joinType, joinTable)) {
							m_sLeftWhere += " " + asSqlWords[iOffSet]; // 拼接连接条件
							// if ((iOffSet+2) < asSqlWords.length &&
							// asSqlWords[iOffSet + 1].equalsIgnoreCase("is")
							// &&asSqlWords[iOffSet +
							// 2].equalsIgnoreCase("null")) {
							if((isCompareOperator(asSqlWords[iOffSet - 1]))
									|| ((iOffSet + 1) < asSqlWords.length && isCompareOperator(asSqlWords[iOffSet + 1]))
									|| haveFunction(asSqlWords, iOffSet)) {
								m_sLeftWhere += "(+)"; // 拼接连接条件

							} else {
								m_sLeftWhere += ""; // 拼接连接条件
							}

							/*
							 * if (inRight) { m_sLeftWhere += " " +
							 * asSqlWords[iOffSet]; //拼接连接条件 m_sLeftWhere +=
							 * "(+)"; //拼接连接条件 } else { int
							 * startIndex=getStartIndex(asSqlWords, iOffSet);
							 * if(startIndex<iOffSet) { for(int
							 * i=iOffSet-1;i>=startIndex;i--) {
							 * m_sLeftWhere=m_sLeftWhere.trim();
							 * m_sLeftWhere=m_sLeftWhere.substring(0,m_sLeftWhere
							 * .length()-asSqlWords[i].length()); } }
							 * TransUnit aTransUnit = getRightSql(asSqlWords,
							 * startIndex); int off = aTransUnit.getIOffSet() -
							 * 1; String rightSql = aTransUnit.getSql();
							 * if (haveAloneSt(rightSql, ".")) { m_sLeftWhere +=
							 * " " +
							 * asSqlWords[iOffSet]; //拼接连接条件 m_sLeftWhere +=
							 * "(+)"; //拼接连接条件 } else { String[] leftSql =
							 * getLeftSql(asSqlWords, iOffSet); m_sLeftWhere +=
							 * "
							 * ("; //拼接连接条件
							 * if(rightSql!=null &&
							 * !rightSql.trim().equalsIgnoreCase("null")) { for
							 * (int i = 0; i < leftSql.length; i++) {
							 * m_sLeftWhere += leftSql[i] + " is null or "; } }
							 * for (int i = startIndex; i <= off; i++) {
							 * m_sLeftWhere += " " + asSqlWords[i]; }
							 * m_sLeftWhere += ") "; iOffSet = off; } }
							 */
						} else {
							m_sLeftWhere += " " + asSqlWords[iOffSet]; // 拼接连接条件
						}
					}
				}
			}
			iOffSet++;

		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealOn Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealOther(String[] asSqlWords, String sWord, int iOffSet, boolean ifTop, boolean dontHaveWhere,
			String rowNum, String sPreWord) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealOther");
		if(iOffSet < asSqlWords.length) {
			if(!sWord.equals(",") && !(sWord.equals(")") && sPreWord.equals("("))
					&& !(sWord.equals("]") && sPreWord.equals("["))) {
				m_sbDestinationSql.append(" ");
			}
			// m_sbDestinationSql.append(" ");
			m_sbDestinationSql.append(asSqlWords[iOffSet]);

			if(asSqlWords[iOffSet].equalsIgnoreCase("where")) {
				dontHaveWhere = false;
				if(ifTop) {
					m_sbDestinationSql.append(" rownum<" + (Integer.valueOf(rowNum).intValue() + 1));
					m_sbDestinationSql.append(" and ");
				}
			}

			iOffSet++;
		}
		TransUnit aTransUnit = new TransUnit(null, null, iOffSet);
		aTransUnit.setDontHaveWhere(dontHaveWhere);
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealOther Over");
		return aTransUnit;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit dealSelect(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealSelect");
		// 张森，2001.10.18 加，处理子查询,要求子查询前后必须有扩号
		// int leftCount = 1; // (计数
		// int rightCount = 0; // )技数
		// Vector vec = new Vector();

		if(iOffSet < asSqlWords.length) {
			/*
			 * vec.addElement(asSqlWords[iOffSet]);
			 * //如果 leftCount != rightCount，说明子查询没结束 while ((leftCount !=
			 * rightCount) && (iOffSet < asSqlWords.length)) { iOffSet++;
			 * if (asSqlWords[iOffSet].equalsIgnoreCase("(")) { leftCount++; }
			 * else if (asSqlWords[iOffSet].equalsIgnoreCase(")")) {
			 * rightCount++; }
			 * if (leftCount != rightCount) { //sSql += " " +
			 * asSqlWords[iOffSet]; vec.addElement(asSqlWords[iOffSet]); } }
			 * String newCaseSql[]=new String[vec.size()];
			 * vec.copyInto(newCaseSql);
			 */

			TransUnit aTransUnit = getSubSql(asSqlWords, "(", ")", iOffSet);
			String[] newCaseSql = aTransUnit.getSqlArray();
			iOffSet = aTransUnit.getIOffSet();

			String newSql[] = new String[newCaseSql.length - 1];

			for(int i = 0; i < newSql.length; i++) {
				newSql[i] = newCaseSql[i];
			}

			// 处理子查询
			TranslateToOracle newTranslateToOracle = new TranslateToOracle();

			newTranslateToOracle.setSqlArray(newSql);

			m_sbDestinationSql.append(newTranslateToOracle.getSql());
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealSelect Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-25 11:03:38)
	 * 
	 * @return java.lang.String
	 * @param whenSql
	 *            java.lang.String[]
	 * @exception java.lang.Throwable
	 *                异常说明。
	 */
	public String dealWhenAnd(String[] whenSql) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealWhenAnd");
		try {
			int offset = 0;
			Vector vec = new Vector();

			String str = "";

			// else
			if(offset < whenSql.length && !whenSql[offset].equalsIgnoreCase("when")) {
				while(offset < whenSql.length) {
					str += whenSql[offset] + " ";
					offset++;
				}
				return str;
			}

			// 遇到 then 之前
			while(offset < whenSql.length && !whenSql[offset].equalsIgnoreCase("then")) {
				if(!whenSql[offset].equalsIgnoreCase("when") && !whenSql[offset].equalsIgnoreCase("and")) {
					str += whenSql[offset] + " ";
				} else {
					if(offset > 0) {
						vec.addElement(str);
						str = "";
					}
				}
				offset++;
			}

			// 遇到 then 之后
			vec.addElement(str); // or then 之间
			str = "";
			offset++;

			while(offset < whenSql.length) {
				str += whenSql[offset] + " ";
				offset++;
			}

			String re = "";
			for(int i = 0; i < vec.size(); i++) {
				String strin = vec.elementAt(i).toString();

				if(i < vec.size() - 1) {
					re += "when" + " " + strin + " " + "then case ";
				} else {
					re += "when" + " " + strin + " " + "then " + str;
				}
			}

			for(int i = 0; i < vec.size() - 1; i++) {

				re += " end";

			}

			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealWhenAnd Over");
			return re;
		} catch(Exception ex) {
			System.out.println(ex);
			throw ex;
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-25 11:03:38)
	 * 
	 * @return java.lang.String
	 * @param whenSql
	 *            java.lang.String[]
	 * @exception java.lang.Throwable
	 *                异常说明。
	 */
	public String dealWhenOr(String[] whenSql) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealWhenOr");
		try {
			int offset = 0;
			Vector vec = new Vector();

			String str = "";

			// else
			if(offset < whenSql.length && !whenSql[offset].equalsIgnoreCase("when")) {
				while(offset < whenSql.length) {
					str += whenSql[offset] + " ";
					offset++;
				}
				return str;
			}
			// 遇到 then 之前
			while(offset < whenSql.length && !whenSql[offset].equalsIgnoreCase("then")) {
				if(!whenSql[offset].equalsIgnoreCase("when") && !whenSql[offset].equalsIgnoreCase("or")) {
					str += whenSql[offset] + " ";
				} else {
					if(offset > 0) // 不是第一个 when
					{
						vec.addElement(str);
						str = "";
					}
				}
				offset++;
			}

			// 遇到 then 之后
			vec.addElement(str); // or then 之间
			str = "";
			offset++;

			while(offset < whenSql.length) {
				str += whenSql[offset] + " ";
				offset++;
			}

			String re = "";
			for(int i = 0; i < vec.size(); i++) {
				String strin = vec.elementAt(i).toString();

				re += "when" + " " + strin + " then " + str + " ";
			}
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.dealWhenOr Over");
			return re;
		} catch(Exception ex) {
			System.out.println(ex);
			throw ex;
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-5-25 19:42:34)
	 * 
	 * @return boolean
	 */
	public boolean haveFunction(String[] asSqlWords, int iOffSet) {
		boolean bFunction = false;
		if(asSqlWords[iOffSet - 1].equalsIgnoreCase("(")
				&& (asSqlWords[iOffSet - 2].equalsIgnoreCase("nvl")
						|| asSqlWords[iOffSet - 2].equalsIgnoreCase("upper")
						|| asSqlWords[iOffSet - 2].equalsIgnoreCase("lower") || asSqlWords[iOffSet - 2]
						.equalsIgnoreCase("substr"))) {
			if(asSqlWords[iOffSet - 3].equalsIgnoreCase("=")) {
				bFunction = true;
				return bFunction;
			}
			while(iOffSet < asSqlWords.length) {
				iOffSet++;
				if(asSqlWords[iOffSet].equalsIgnoreCase(")") && asSqlWords[iOffSet + 1].equalsIgnoreCase("=")) {
					bFunction = true;
					break;
				}
			}

		}
		return bFunction;

	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-21 21:25:47)
	 * 
	 * @return java.lang.String
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param iOffSet
	 *            int
	 */
	public String getFirstTrueWord(String[] asSqlWords, int iOffSet) {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.getFirstTrueWord");
		String trueWord = "";

		for(int i = iOffSet + 1; i < asSqlWords.length; i++) {
			if(!asSqlWords[i].equals("(")) {
				trueWord = asSqlWords[i];
				break;
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.getFirstTrueWord Over");
		return trueWord;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-28 18:38:31)
	 * 
	 * @return nc.bs.mw.sqltrans.TransUnit
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param sWord
	 *            java.lang.String
	 * @param iOffSet
	 *            int
	 */
	public TransUnit getJoinInfo(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.getJoinInfo");
		int onNum = 1;
		int joinNum = 0;

		// int left = 0;
		// int right = 0;

		// String tabaleName = null;
		// String tabaleOtherName = null;
		int joinType = 0;

		try {
			for(int i = iOffSet - 1; i >= 0; i--) {
				/*
				 * if (asSqlWords[i].equals("(")) { left++; } else if
				 * (asSqlWords[i].equals(")")) { right++; }
				 */
				if(asSqlWords[i].equalsIgnoreCase("on")) {
					onNum++;
				} else if(asSqlWords[i].equalsIgnoreCase("join")) {
					joinNum++;

					if(joinNum == onNum) {
						if(asSqlWords[i - 1].equalsIgnoreCase("inner")) {
							joinType = INNERJOIN;
						} else if(asSqlWords[i - 2].equalsIgnoreCase("left")) {
							joinType = LEFTJOIN;
						} else if(asSqlWords[i - 2].equalsIgnoreCase("right")) {
							joinType = RIGHTJOIN;
						}
						Vector vecTableNames = new Vector();

						for(int index = i + 1; index < iOffSet; index++) {
							Object obj = htJoinTableName.get("" + index);

							if(obj != null) {
								vecTableNames.addElement(obj.toString());
							}
						}
						String stTableName[] = new String[vecTableNames.size()];
						vecTableNames.copyInto(stTableName);
						return new TransUnit(stTableName, "", joinType);

					}
				}
			}
			if(joinType == 0) {
				throw new Exception("join 与 on 不匹配!");
			}
			// String[] stArray=new String[1];
			// stArray[0]=""+joinType;
			// stArray[1]=tabaleName;
			// stArray[2]=tabaleOtherName;
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.getJoinInfo Over");
			return null;
		} catch(Exception e) {
			throw new Exception("join 与 on 不匹配!");
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-21 20:16:30)
	 * 
	 * @return boolean
	 * @param sourceSql
	 *            java.lang.String
	 * @param joinType
	 *            int
	 * @param tableName
	 *            java.lang.String
	 * @param tableOtherName
	 *            java.lang.String
	 */
	public boolean needChangJoin(String sourceSql, int joinType, String[] rightTable) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.needChangJoin");

		boolean needChang = false;

		if(joinType == LEFTJOIN) {
			int dotIndex = sourceSql.indexOf(".");
			if(dotIndex >= 0) {
				String tabName = sourceSql.substring(0, dotIndex).trim().toLowerCase();
				for(int i = 0; i < rightTable.length; i++) {
					if(tabName.equalsIgnoreCase(rightTable[i].trim())) {
						needChang = true;
						break;
					}
				}
			}
		} else if(joinType == RIGHTJOIN) {
			if(haveAloneSt(sourceSql, ".")) {
				needChang = true;

				int dotIndex = sourceSql.indexOf(".");
				if(dotIndex >= 0) {
					String tabName = sourceSql.substring(0, dotIndex).trim().toLowerCase();
					for(int i = 0; i < rightTable.length; i++) {
						// if
						// (sourceSql.toLowerCase().indexOf(rightTable[i].toLowerCase()
						// + ".") == 0)
						if(tabName.equalsIgnoreCase(rightTable[i].trim())) {
							needChang = false;
							break;
						}
					}
				}
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.needChangJoin Over");
		return needChang;
	}

	/**
	 * 此处插入方法说明。 功能：预处理，格式化case and or，使其成为指定的格式,分解 and or,成为独立单元
	 * 创建日期：(2001-3-25 9:44:54)
	 * 
	 * @return java.lang.String
	 * @param sql
	 *            java.lang.String
	 */
	public String preTranCaseWhen(String[] sql) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.preTranCaseWhen");
		try {
			String re = "";
			int offset = 0;

			while(offset < sql.length) // 格式化 when then之间的语句
			{
				if(sql[offset].equalsIgnoreCase("when")) {
					re += sql[offset] + " "; // +when

					offset++;

					String whenStr = "";

					while(offset < sql.length && !sql[offset].equalsIgnoreCase("then")) {
						whenStr += sql[offset] + " ";

						offset++;
					}

					// translateSelect(parseSql(sSql)); //张森，2001。10。18
					TranslateToOracle newTranslateToOracle = new TranslateToOracle();

					newTranslateToOracle.setSql(whenStr);

					whenStr = newTranslateToOracle.getSql();

					// 预处理，格式化case and or，使其成为指定的格式
					whenStr = express.getValue(whenStr);

					re += whenStr + " ";

					re += sql[offset] + " "; // +then

					offset++;

				} else {
					if(offset < sql.length) {
						re += sql[offset] + " ";
					}

					offset++;
				}
			}

			try {
				String re_or = preTranCaseWhen_or(parseSql(re)); // 先处理 or
				// return preTranCaseWhen_and(parseSql(re_or)); //处理 and
				log.debug("nc.bs.mw.sqltrans.TranslateToOracle.preTranCaseWhen Over");
				return re_or;
			} catch(Exception ex) {
				System.out.println(ex);
				throw ex;
			}
		} catch(Exception ex) {
			System.out.println(ex);
			throw ex;
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-25 10:06:40)
	 * 
	 * @return java.lang.String//原语句，以case开头 以end结束
	 * @param sql
	 *            java.lang.String[]
	 * @exception java.lang.Throwable
	 *                异常说明。
	 */
	public String preTranCaseWhen_and(String[] sql) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.preTranCaseWhen_and");
		try {
			int offset = 0;

			String result = "";

			while(offset < sql.length) // 有问题，else后的语句没解决
			{
				if(sql[offset].equalsIgnoreCase("when") || sql[offset].equalsIgnoreCase("else")) {
					String when_str = sql[offset]; // 加 when
					offset++;

					// 未遇到下一个 when else 前
					while((offset < sql.length) && (!sql[offset].equalsIgnoreCase("when"))
							&& (!sql[offset].equalsIgnoreCase("else"))) {
						// 如果遇到嵌套CASE WHEN
						if(sql[offset].equalsIgnoreCase("case")) {
							// 张森，2001.3.23 加，改，处理嵌套 case when
							int caseCount = 1; // case计数
							int endCount = 0; // end技数

							if(offset < sql.length) {
								String sSql = "";

								sSql += " " + sql[offset];

								// 如果 case计数！= end技数，说明嵌套没结束
								while((caseCount != endCount) && (offset < sql.length - 1)) {
									offset++;

									if(sql[offset].equalsIgnoreCase("case")) {
										caseCount++;
									} else if(sql[offset].equalsIgnoreCase("end")) {
										endCount++;
									}

									sSql += " " + sql[offset];

								}
								// sSql += " " + sql[offset];
								sSql = preTranCaseWhen_and(parseSql(sSql));
								when_str += (" " + sSql);
								offset++;

							} else
								break;
						} else // 没有遇到嵌套CASE WHEN
						{
							when_str += (" " + sql[offset]);
							offset++;
						}
					} // while ends
					result += (" " + dealWhenAnd(parseSql(when_str))); // deal
					// with
					// when
				} else {
					result += (" " + sql[offset]);
					offset++;
				}

			}
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.preTranCaseWhen_and Over");
			return result;
		} catch(Exception ex) {
			System.out.println(ex);
			throw ex;
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-3-25 10:06:40)
	 * 
	 * @return java.lang.String//原语句，以case开头 以end结束
	 * @param sql
	 *            java.lang.String[]
	 * @exception java.lang.Throwable
	 *                异常说明。
	 */
	public String preTranCaseWhen_or(String[] sql) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.preTranCaseWhen_or");
		try {
			int offset = 0;

			String result = "";

			while(offset < sql.length) // 有问题，else后的语句没解决
			{
				if(sql[offset].equalsIgnoreCase("when") || sql[offset].equalsIgnoreCase("else")) {
					String when_str = sql[offset]; // 加 when
					offset++;

					// 未遇到下一个 when else end前
					while((offset < sql.length) && (!sql[offset].equalsIgnoreCase("when"))
							&& (!sql[offset].equalsIgnoreCase("else")) && (!sql[offset].equalsIgnoreCase("end"))) {
						// 如果遇到嵌套CASE WHEN
						if(sql[offset].equalsIgnoreCase("case")) {
							// 张森，2001.3.23 加，改，处理嵌套 case when
							int caseCount = 1; // case计数
							int endCount = 0; // end技数

							if(offset < sql.length) {
								String sSql = "";

								sSql += " " + sql[offset];

								// 如果 case计数！= end技数，说明嵌套没结束
								while((caseCount != endCount) && (offset < sql.length - 1)) {
									offset++;

									if(sql[offset].equalsIgnoreCase("case")) {
										caseCount++;
									} else if(sql[offset].equalsIgnoreCase("end")) {
										endCount++;
									}

									sSql += " " + sql[offset];

								}
								// sSql += " " + sql[offset];
								sSql = preTranCaseWhen_or(parseSql(sSql));
								when_str += (" " + sSql);
								offset++;

							} else
								break;
						} else // 没有遇到嵌套CASE WHEN
						{
							when_str += (" " + sql[offset]);
							offset++;
						}
					} // while ends
					result += (" " + dealWhenOr(parseSql(when_str))); // deal
					// with
					// when
				} else {
					result += (" " + sql[offset]);
					offset++;
				}

			}
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.preTranCaseWhen_or Over");
			return result;
		} catch(Exception ex) {
			System.out.println(ex);
			throw ex;
		}
	}

	/**
	 * 转换 CASE WHEN 语句 参数: asSqlWords CaseWhen语句 返回: destSql 目标语句 规则: case s0
	 * when exp1 then exp2 when exp3 then exp4 ->
	 * decode(s0,exp1,exp2,exp3,exp4,exp5) else exp5 end
	 * case when s0 is null then exp1 when s0=exp2 then exp3 when s0<=exp4 then
	 * exp5 ->
	 * decode(s0,null,exp1,exp2,exp3,least(s0,exp4),exp5,GREATEST(s0,exp6),exp7,
	 * exp8)
	 * when s0>=exp6 then exp7 else exp8 end
	 */
	private String translateCaseWhen(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateCaseWhen");
		// 若第一个字符串不等于“when”，第一种情况
		if(!asSqlWords[1].equalsIgnoreCase("when")) {
			return translateSimpleCaseWhen(asSqlWords);
		} else { // 若第一个字符串是“when”，第二种情况
			log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateCaseWhen Over");
			return translateComplexCaseWhen(asSqlWords);
		}
	}

	/**
	 * 转换 CASE WHEN 语句 参数: asSqlWords CaseWhen语句 返回: destSql 目标语句 规则: case s0
	 * when exp1 then exp2 when exp3 then exp4 ->
	 * decode(s0,exp1,exp2,exp3,exp4,exp5) else exp5 end
	 * case when s0 is null then exp1 when s0=exp2 then exp3 when s0<=exp4 then
	 * exp5 ->
	 * decode(s0,null,exp1,exp2,exp3,least(s0,exp4),exp5,GREATEST(s0,exp6),exp7,
	 * exp8)
	 * when s0>=exp6 then exp7 else exp8 end
	 */
	private String translateComplexCaseWhen(String[] asSqlWords) throws Exception {
		int iOff = 0;
		// 参数
		String s0 = "";

		String s1 = "";
		iOff++;

		int andNum = 0;

		// 第一个字符串是“when”，第二种情况

		// s1 = "nvl(decode(";
		s1 = "decode(decode(";

		int whenCount = 0;

		int elseCount = 0;

		// 若当前字符串不是“end”
		while((iOff < asSqlWords.length) && !asSqlWords[iOff].trim().equalsIgnoreCase("end")) {
			// 若当前字符串是“is”
			if(asSqlWords[iOff].trim().equalsIgnoreCase("is")) {
				// 不支持is not 语句
				if(asSqlWords[iOff + 1].trim().equalsIgnoreCase("not")) {
					throw new Exception("'is not' clause is not superted in CASE...WHEN...END statement!");
				}
				s1 += " ,";
				iOff++;
				// 当下一个字符串不是“then”
				while(!asSqlWords[iOff].trim().equalsIgnoreCase("then")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("and")) {
					// 记录字符串
					s1 += " " + asSqlWords[iOff];
					iOff++;
				}
			} else // 若当前字符串不是“is”
			// 若当前字符串是等于号
			if(asSqlWords[iOff].trim().equals("=")) {
				s1 += " ,";
				iOff++;
				// 当前字符串不是“then”
				while(!asSqlWords[iOff].trim().equalsIgnoreCase("then")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("and")) {
					s1 += " " + asSqlWords[iOff];
					iOff++;
				}
			} else
			// 若当前字符串是小于等于号
			if(asSqlWords[iOff].trim().equals("<=")) {
				s1 += " ,least(" + s0 + ",";
				iOff++;
				// 当前字符串不是“then”
				while(!asSqlWords[iOff].trim().equalsIgnoreCase("then")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("and")) {
					s1 += " " + asSqlWords[iOff];
					iOff++;
				}
				s1 += " ) ";
			} else
			// 若当前字符串是大于等于号
			if(asSqlWords[iOff].trim().equals(">=")) {
				s1 += " ,greatest(" + s0 + ",";
				iOff++;
				// 当前字符串不是“then”
				while(!asSqlWords[iOff].trim().equalsIgnoreCase("then")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("and")) {
					s1 += " " + asSqlWords[iOff];
					iOff++;
				}
				s1 += " )";
			} else
			// 当前字符串是“then”
			if(asSqlWords[iOff].trim().equalsIgnoreCase("then")) {
				// s1="decode(decode("+s1;
				// s1 += " ,";
				s1 += ",1)";
				for(int andIndex = 0; andIndex < andNum; andIndex++) {
					s1 += ")";
				}
				// new add
				s1 += ",1,";
				// new add end
				iOff++;
				// 当前字符串不是“else”、“when”、“end”
				while(!asSqlWords[iOff].trim().equalsIgnoreCase("else")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("when")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("case")
						&& !asSqlWords[iOff].trim().equalsIgnoreCase("end")) {
					s1 += " " + asSqlWords[iOff];
					iOff++;
				}
				andNum = 0;
			} else
			// 当前字符串是“when”
			if(asSqlWords[iOff].trim().equalsIgnoreCase("when")) {
				iOff++;
				whenCount++;
				s0 = "";
				// 当前位置小于
				while(iOff < asSqlWords.length - 1) {
					if(asSqlWords[iOff].trim().equalsIgnoreCase("is") || asSqlWords[iOff].trim().equals(">=")
							|| asSqlWords[iOff].trim().equals("<=") || asSqlWords[iOff].trim().equals("=")) {

						break;
					} else {

						if(whenCount > 1) {
							// 嵌套下一个 when
							// s1 += " ), nvl(decode(";
							// s1 += " ), decode(decode(";
							s1 += " , decode(decode(";
						}

						String whenSt = "";
						while(iOff < asSqlWords.length
								&& !(asSqlWords[iOff].trim().equalsIgnoreCase("is")
										|| asSqlWords[iOff].trim().equals(">=") || asSqlWords[iOff].trim().equals("<=") || asSqlWords[iOff]
										.trim().equals("="))) {
							whenSt += asSqlWords[iOff];
							iOff++;
						}

						// 运算符左边

						s1 += " " + whenSt;

						s0 += " " + whenSt;

						// iOff++;
					}
				}
			} else
			// 当前字符串是“else”
			if(asSqlWords[iOff].trim().equalsIgnoreCase("else")) {

				// 嵌套 else
				elseCount++;
				// s1 += " ),";
				s1 += " ,";
				iOff++;
			} else
			// 当前字符串是关系符
			if(asSqlWords[iOff].trim().equals("<>") || asSqlWords[iOff].trim().equals("<")
					|| asSqlWords[iOff].trim().equals(">") || asSqlWords[iOff].trim().equals("!=")) {
				// 不支持<>,!=,<,> 操作符
				throw new Exception("'<>,!=,<,>' operator is not superted in CASE...WHEN...END statement!");
			} else if(asSqlWords[iOff].trim().equalsIgnoreCase("case")) {
				// 张森，2001.3.23 加，改，处理嵌套 case when
				int caseCount = 1; // case计数
				int endCount = 0; // end技数

				if(iOff < asSqlWords.length) {
					Vector vec = new Vector();

					vec.addElement(asSqlWords[iOff]);
					// String sSql = "";

					// sSql += " " + asSqlWords[iOff];

					// 如果 case计数！= end技数，说明嵌套没结束
					while((caseCount != endCount) && (iOff < asSqlWords.length)) {
						iOff++;

						if(asSqlWords[iOff].trim().equalsIgnoreCase("case")) {
							caseCount++;
						} else if(asSqlWords[iOff].trim().equalsIgnoreCase("end")) {
							endCount++;
						}

						// sSql += " " + asSqlWords[iOff];
						vec.addElement(asSqlWords[iOff]);

					}

					String newStArray[] = new String[vec.size()];
					vec.copyInto(newStArray);

					// 解决嵌套 case when

					s1 += " " + translateCaseWhen(newStArray) + " ";

					iOff++;

				} else
					break;
			} else
			// 当前字符串是“else”
			if(asSqlWords[iOff].trim().equalsIgnoreCase("and")) {
				andNum++;
				s1 += " ,decode(";
				s0 = "";
				iOff++;
			} else {
				s1 += " " + asSqlWords[iOff];
				s0 += " " + asSqlWords[iOff];
				iOff++;
			}
			// 若标志小于输入字符串集合的长度
			if(iOff < asSqlWords.length) {
				// 若当前字符串是“end”，则结束
				if(asSqlWords[iOff].trim().equalsIgnoreCase("end")) {
					/*
					 * if (elseCount <= 0) { s1 += " ),null"; } else { s1 += "
					 * )"; }
					 */
					if(elseCount > 0) {
						s1 += " ) ";
					}
					// 张森，2001。3。23
					break;
				}
			}
		}

		int rightKuoHao = whenCount;

		if(elseCount > 0)
			rightKuoHao = whenCount - 1;

		// 补齐右括号
		for(int i = 0; i < rightKuoHao; i++) {
			s1 += " ) ";
		}

		// return m_sbDestinationSql;
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateComplexCaseWhen Over");
		return s1;
	}

	/**
	 * 转换Cast函数 参数: asWords cast函数子句
	 * 规则: cast(f as char(n))->to_char(f) cast(f as date)->to_date(f)
	 */
	private void translateFunCast(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunCast");

		String s = new String();
		s = "";
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		boolean charLenCtrl = false;
		boolean isDate = false;
		boolean isChar = false;
		String charLenth = null;
		// 未至倒数第一，则循环
		while(iOff < (asWords.length - 1)) {
			// 若当前字符串为左括号
			if(asWords[iOff].equals("("))
				iLBracket++;
			// 若当前字符串为右括号
			if(asWords[iOff].equals(")"))
				iRBracket++;
			// 若左标记等于右标记，且下一个字符串为“as”
			if(iLBracket == iRBracket && asWords[iOff + 1].equalsIgnoreCase("as")) {
				if(isDateType(asWords[iOff + 2])) {
					isDate = true;
					m_sbDestinationSql.append(" to_date(");
				} else if(isCharType(asWords[iOff + 2])) {
					isChar = true;
					if(iOff + 4 < asWords.length && asWords[iOff + 3].equals("(")) {
						charLenCtrl = true;
						charLenth = asWords[iOff + 4];
						m_sbDestinationSql.append(" substr(to_char(");
					} else {
						m_sbDestinationSql.append(" to_char(");
					}
				} else {
					m_sbDestinationSql.append(" cast(");
				}
				s += " " + asWords[iOff];
				iOff++;
				break;

			}
			s += " " + asWords[iOff];
			iOff++;
		}
		try {
			translateSelect(parseSql(s));
			if(isChar) {
				if(charLenCtrl)
					m_sbDestinationSql.append(") ,1," + charLenth + " )");
				else
					m_sbDestinationSql.append(" )");
			} else if(isDate) {
				m_sbDestinationSql.append(", 'yyyy-mm-dd hh24:mi:ss')");
			} else
				while(iOff < (asWords.length)) {
					m_sbDestinationSql.append(" " + asWords[iOff]);
					iOff++;
				}
		} catch(Exception e) {
			throw e;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunCast Over");
	}

	/**
	 * 转换 coalesce 函数 参数: asSqlWords coalesce函数子句 规则:
	 * coalesce(exp1,exp2,exp3,exp4)->nvl(nvl(nvl(exp1,exp2),exp3,exp4))
	 */

	private void translateFunCoalesce(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunCoalesce");

		int iOffSet = 0;
		int iLBracket = 0;
		int iRBracket = 0;
		// 标识每个参数的起始位置
		int iStart = 0;
		java.util.Vector v = new java.util.Vector();
		String sWord = asSqlWords[iOffSet];

		TranslateToOracle newTranslateToOracle = new TranslateToOracle();

		// newTranslateToOracle.setSql(sSql);

		// m_sbDestinationSql.append(newTranslateToOracle.getSql());

		// 若当前字符串为“coalesce”，且下一个字符串为左括号
		if(sWord.equalsIgnoreCase("coalesce") && asSqlWords[iOffSet + 1].equals("(")) {
			m_bFinded = true;
			m_sbDestinationSql.append(" ");
			// 标识加1
			iOffSet++;
			// 起始位置加2
			iStart += 2;
			// 若标志小于字符串集长度，则循环
			while(iOffSet < asSqlWords.length) {
				iOffSet++;
				// 若当前字符串为左括号，则左标志加1
				if(asSqlWords[iOffSet].equals("("))
					iLBracket++;
				// 若当前字符串为右括号，则右标志加1
				if(asSqlWords[iOffSet].equals(")"))
					iRBracket++;
				// 若左右标识相等，且当前字符串为逗号
				if(iLBracket == iRBracket && asSqlWords[iOffSet].equals(",")) {
					String str = new String();
					// 保存从开始位置到标识位置的字符串
					for(int i = iStart; i < iOffSet; i++) {
						str += " " + asSqlWords[i];
					}

					if(str.indexOf("(") >= 0) {
						newTranslateToOracle.setSql(str);
						str = newTranslateToOracle.getSql();
					}
					v.addElement(str);
					// 开始位置为标识位置加1
					iStart = iOffSet + 1;
				}
				// 若左右标识相等，且下一个字符串为右括号
				if((iLBracket == iRBracket) && asSqlWords[iOffSet + 1].equals(")")) {
					String str = new String();
					// 保存从开始位置到标识位置加1的字符串
					for(int i = iStart; i < iOffSet + 1; i++) {
						str += " " + asSqlWords[i];
					}

					if(str.indexOf("(") >= 0) {
						newTranslateToOracle.setSql(str);
						str = newTranslateToOracle.getSql();
					}

					v.addElement(str);
					String s = new String();

					// 增加nvl
					for(int i = 1; i < v.size(); i++)
						m_sbDestinationSql.append("nvl(");
					m_sbDestinationSql.append(v.elementAt(0));

					// 格式整理
					for(int j = 1; j < v.size(); j++) {
						s += " " + ",";
						s += " " + v.elementAt(j);
						s += " " + ")";
					}
					translateSelect(parseSql(s));
					break;
				}
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunCoalesce Over");
	}

	/*
	 * 转换convert函数 参数: asWords convert函数子句
	 * 规则: convert(char(n),f)->to_char(f) convert(date,f)->to_date(f)
	 */
	private void translateFunConvert(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunConvert");

		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		boolean isDateToChar = false;
		boolean charLenCtrl = false;
		boolean isDate = false;
		boolean isChar = false;
		String charLenth = null;
		String dataType = "";
		String col = "";
		// 取出函数的参数
		String params[] = getFunParam(asWords, iOff, asWords.length - 1);
		dataType = params[0];
		col = params[1];
		dataType = dataType.trim();
		String oldDataType = dataType;
		// 取出数据类型
		if(dataType.indexOf("(") > 0) {
			dataType = dataType.substring(0, dataType.indexOf("("));
		}
		// 日期数据类型
		if(isDateType(dataType)) {
			isDate = true;
			m_sbDestinationSql.append(" to_date(");
		} else if(isCharType(dataType)) {
			isChar = true;
			if(oldDataType.indexOf("(") > 0) {
				charLenCtrl = true;
				charLenth = oldDataType.substring(oldDataType.indexOf("(") + 1, oldDataType.length() - 1);
			}
			if(charLenCtrl)
				m_sbDestinationSql.append(" substr(to_char(");
			else
				m_sbDestinationSql.append(" to_char(");
			if(params.length == 3 && params[2] != null) {
				if(params[2].trim().equals("21"))
					isDateToChar = true;
			}
		} else
			m_sbDestinationSql.append(" cast(");

		try {
			translateSelect(parseSql(col));

			if(isChar) {
				if(charLenCtrl)
					m_sbDestinationSql.append(") ,1," + charLenth + " )");
				else
					m_sbDestinationSql.append(" )");
			} else if(isDate) {
				m_sbDestinationSql.append(", 'yyyy-mm-dd')");
			} else
				m_sbDestinationSql.append(" as " + oldDataType + ")");

		} catch(Exception e) {
			throw e;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunConvert Over");
	}

	/*
	 * 转换DateAdd函数 参数: asWords DateAdd函数子句 规则: DATEADD ( datepart , number, date
	 * )
	 * datepart：yy、mm、dd
	 */
	private void translateFunDateAdd(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunDateAdd");

		int iOff = 2;

		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		String dateType = params[0].trim();

		String theNumber = params[1].trim();

		String theDate = params[2].trim();

		TranslateToOracle newTranslateToOracle = new TranslateToOracle();

		newTranslateToOracle.setSql(theDate);

		theDate = newTranslateToOracle.getSql();
		theDate = theDate.trim();

		if(!(theDate.toLowerCase().startsWith("to_date(") && theDate.toLowerCase().indexOf("'yyyy-mm-dd'") > 0)) {
			if(theDate.toLowerCase().startsWith("sysdate"))
				theDate = "to_date(sysdate)";
			else
				theDate = "to_date(" + theDate + ",'yyyy-mm-dd')";
		}

		if(dateType.equalsIgnoreCase("yy") || dateType.equalsIgnoreCase("yyyy") || dateType.equalsIgnoreCase("year")) {
			theNumber = "(" + theNumber + "*12)";
			dateType = "mm";
		}

		if(dateType.equalsIgnoreCase("mm") || dateType.equalsIgnoreCase("m") || dateType.equalsIgnoreCase("month")) {
			m_sbDestinationSql.append(" add_months( " + theDate + ", " + theNumber + ") ");
		} else {
			m_sbDestinationSql.append(" " + theDate + "+" + theNumber + " ");
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunDateAdd Over");
	}

	/*
	 * 转换DateDiff函数 参数: asWords DateDiff函数子句
	 * 规则: DATEDIFF ( datepart , startdate , enddate )
	 * datepart支持：yy、mm、dd、hh、mm、ss等
	 */
	private void translateFunDateDiff(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunDateDiff");

		int iOff = 2;

		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		String dateType = params[0].trim();

		String startDate = params[1].trim();

		String endDate = params[2].trim();

		TranslateToOracle newTranslateToOracle = new TranslateToOracle();

		newTranslateToOracle.setSql(startDate);

		startDate = newTranslateToOracle.getSql().trim();

		newTranslateToOracle.setSql(endDate);

		endDate = newTranslateToOracle.getSql().trim();

		if(!(startDate.toLowerCase().startsWith("to_date(") && startDate.toLowerCase().indexOf("'yyyy-mm-dd'") > 0)) {
			if(startDate.toLowerCase().startsWith("sysdate"))
				startDate = "to_date(sysdate)";
			else
				startDate = "to_date(" + startDate + ",'yyyy-mm-dd')";
		}

		if(!(endDate.toLowerCase().startsWith("to_date(") && endDate.toLowerCase().indexOf("'yyyy-mm-dd'") > 0)) {
			if(endDate.toLowerCase().startsWith("sysdate"))
				endDate = "to_date(sysdate)";
			else
				endDate = "to_date(" + endDate + ",'yyyy-mm-dd')";
		}

		if(dateType.equalsIgnoreCase("yy") || dateType.equalsIgnoreCase("yyyy") || dateType.equalsIgnoreCase("year")) {
			m_sbDestinationSql.append(" extract(year from  " + endDate + ") - extract(year from  " + startDate + ")");
		} else if(dateType.equalsIgnoreCase("mm") || dateType.equalsIgnoreCase("m")
				|| dateType.equalsIgnoreCase("month")) {
			m_sbDestinationSql.append(" months_between( " + endDate + ", " + startDate + ") ");
		} else {
			m_sbDestinationSql.append(" (" + endDate + "-" + startDate + ") ");
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunDateDiff Over");
	}

	/**
	 * 转换left函数 参数: asWords: left函数语句 规则: left(str,n)->substr(str,1,4)
	 */
	private void translateFunLeft(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunLeft");

		java.util.Vector v = new java.util.Vector();
		String s = new String();
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;

		s = "substr(";

		while(iOff < (asWords.length)) {
			if(asWords[iOff].equals("("))
				iLBracket++;
			if(asWords[iOff].equals(")"))
				iRBracket++;
			if((iLBracket == iRBracket) && asWords[iOff + 1].equals(",")) {
				s += " " + asWords[iOff];
				iOff++;
				s += ",1";
			}
			s += " " + asWords[iOff];
			iOff++;
		}
		try {
			translateSelect(parseSql(s));
		} catch(Exception e) {
			System.out.println(e);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunLeft Over");
	}

	/**
	 * 对 len 函数进行转换 参数: asSqlWords:len函数语句 转换格式： len(str)->length(rtrim(str,'
	 * '))
	 */
	public void translateFunLen(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunLen");
		String sSql = "";
		int iOffSet = 2;
		int iLBracket = 0;
		int iRBracket = 0;

		sSql = sSql + "length(rtrim(";
		iLBracket = 1;
		while(iOffSet < asSqlWords.length) {
			if(asSqlWords[iOffSet].equals("("))
				iLBracket++;
			if(asSqlWords[iOffSet].equals(")"))
				iRBracket++;
			if((iLBracket == iRBracket + 1) && asSqlWords[iOffSet + 1].equals(")")) {
				sSql += " " + asSqlWords[iOffSet];
				break;
			}
			sSql += " " + asSqlWords[iOffSet];
			iOffSet++;
		}
		sSql += "))";
		try {
			translateSelect(parseSql(sSql));
		} catch(Exception e) {
			System.out.println(e);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunLen Over");
	}

	/**
	 * 转换Rtrim函数 参数: asSqlWords: Ltrim函数子句 规则: Ltrim(str)->Ltrim(str,' ')
	 */
	private void translateFunLtrim(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunLtrim");
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		String s = new String();

		m_sbDestinationSql.append(" ltrim(");
		while(iOff < asSqlWords.length) {
			if(asSqlWords[iOff].equals("(")) {
				iLBracket++;
			}
			if(asSqlWords[iOff].equals(")")) {
				iRBracket++;
			}
			if(iLBracket == iRBracket && asSqlWords[iOff + 1].equals(")")) {
				if(iLBracket > 0) {
					for(int i = 2; i < iOff + 1; i++) {
						s += " " + asSqlWords[i];
					}
					try {
						translateSelect(parseSql(s));
					} catch(Exception e) {
						System.out.println(e);
					}
				} else {
					for(int i = 2; i < iOff + 1; i++) {
						m_sbDestinationSql.append(" " + asSqlWords[i]);
					}
				}
				m_sbDestinationSql.append(",' ')");
				break;
			} else if(asSqlWords[asSqlWords.length - 1].equals(")")) {
				// 函数嵌套
				String[] newFunSql = new String[asSqlWords.length - (iOff + 1)];
				for(int index = 0; index < newFunSql.length; index++) {
					newFunSql[index] = asSqlWords[iOff];
					iOff++;
				}
				translateSelect(newFunSql);
				m_sbDestinationSql.append(",' ')");
				break;
			} else {
				m_sbDestinationSql.append(asSqlWords[iOff]);
				iOff++;
				throw new Exception(asSqlWords[0] + "后的参数不匹配!");
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunLtrim Over");
	}

	/**
	 * 转换patindex函数 参数: asWords: patindex函数语句 规则:
	 * patindex('%exp1%',exp2)->instr(exp2,'exp1',1,1)
	 */
	private void translateFunPatindex(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunPatindex");

		String sSql = new String();
		int iOff = 2;
		String sWord = new String();
		sSql = "instr(";

		sWord = asWords[iOff];
		if(sWord.length() > 4 && asWords[iOff + 1].equals(",")) {
			String s = "";
			s += "'" + sWord.substring(2, sWord.length() - 2) + "'";

			iOff += 2;
			while(iOff < asWords.length) {
				if(!asWords[iOff].equals(")")) {
					sSql += " " + asWords[iOff];
					iOff++;
				} else {
					sSql += "," + s + ",1,1)";
					break;
				}
			}
			try {
				translateSelect(parseSql(sSql));
			} catch(Exception e) {
				System.out.println(e);
			}
		} else {
			for(int i = 0; i < asWords.length; i++) {
				m_sbDestinationSql.append(asWords[i]);
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunPatindex Over");
	}

	/**
	 * 转换right函数 参数: asWords: right函数子句 规则:
	 * right(f,n)->substr(f,length(f)-n+1,n)
	 */
	private void translateFunRight(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunRight");
		String s = new String();
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		s = "substr(";
		while(iOff < (asWords.length)) {
			if(asWords[iOff].equals("("))
				iLBracket++;
			if(asWords[iOff].equals(")"))
				iRBracket++;
			if((iLBracket == iRBracket) && asWords[iOff + 1].equals(",")) {
				s += " " + asWords[iOff];
				iOff++;
				s += ",length(";
				for(int i = 2; i < iOff; i++) {
					s += " " + asWords[i];
				}
				s += ")-";
				for(int j = iOff + 1; j < asWords.length - 1; j++) {
					s += " " + asWords[j];
				}
				s += "+1";
			}
			s += " " + asWords[iOff];
			iOff++;
		}
		try {
			translateSelect(parseSql(s));
		} catch(Exception e) {
			System.out.println(e);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunRight Over");
	}

	/**
	 * 转换Rtrim函数 参数: asSqlWords: Ltrim函数子句 规则: Ltrim(str)->Ltrim(str,' ')
	 */
	private void translateFunRound(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunRound");
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		int commaCount = 0;
		int doubleQuotationCount = 0;
		int singleQuotationCount = 0;

		int firstCommaIndex = 0;
		int secondCommaIndex = 0;

		String theNumber = "";
		String theLength = "";
		String theTyle = "";

		TranslateToOracle newTranslateToOracle = null;

		while(iOff < asSqlWords.length - 1) {
			if(asSqlWords[iOff].equals("(")) {
				iLBracket++;
			}
			if(asSqlWords[iOff].equals(")")) {
				iRBracket++;
			}
			if(asSqlWords[iOff].equals("\'")) {
				singleQuotationCount++;
			}
			if(asSqlWords[iOff].equals("\"")) {
				doubleQuotationCount++;
			}
			if(asSqlWords[iOff].equals(",") && iRBracket == iLBracket && doubleQuotationCount % 2 == 0
					&& singleQuotationCount % 2 == 0) {
				commaCount++;
				if(commaCount == 1) {
					firstCommaIndex = iOff;

					for(int i = 2; i < iOff; i++) {
						theNumber += " " + asSqlWords[i];
					}

					if(iOff - 2 > 1) {
						if(newTranslateToOracle == null) {
							newTranslateToOracle = new TranslateToOracle();
						}

						newTranslateToOracle.setSql(theNumber);

						theNumber = newTranslateToOracle.getSql();
					}
				} else {
					secondCommaIndex = iOff;

					for(int i = firstCommaIndex + 1; i < iOff; i++) {
						theLength += " " + asSqlWords[i];
					}

					if(iOff - (firstCommaIndex + 1) > 1) {
						if(newTranslateToOracle == null) {
							newTranslateToOracle = new TranslateToOracle();
						}

						newTranslateToOracle.setSql(theLength);

						theLength = newTranslateToOracle.getSql();
					}
				}
			}
			iOff++;
		}

		int fromIndex = 0;
		String s = " ";

		if(commaCount == 0) {
			for(int i = 0; i < asSqlWords.length; i++) {
				s += asSqlWords[i];
			}
		}

		else if(commaCount == 1) {
			for(int i = firstCommaIndex + 1; i < asSqlWords.length - 1; i++) {
				theLength += " " + asSqlWords[i];
			}

			if((asSqlWords.length - 1) - (firstCommaIndex + 1) > 1) {
				if(newTranslateToOracle == null) {
					newTranslateToOracle = new TranslateToOracle();
				}

				newTranslateToOracle.setSql(theLength);

				theLength = newTranslateToOracle.getSql();
			}

			s = " round(" + theNumber + ", " + theLength + ") ";
		} else {
			for(int i = secondCommaIndex + 1; i < asSqlWords.length - 1; i++) {
				theTyle += " " + asSqlWords[i];
			}

			if((asSqlWords.length - 1) - (secondCommaIndex + 1) > 1) {
				if(newTranslateToOracle == null) {
					newTranslateToOracle = new TranslateToOracle();
				}

				newTranslateToOracle.setSql(theTyle);

				theTyle = newTranslateToOracle.getSql();
			}

			int tyle = Integer.valueOf(theTyle.trim()).intValue();

			if(tyle == 0) {
				s = " round(" + theNumber + ", " + theLength + ") ";
			} else {
				// s =" floor(" + theNumber + "*(10**" + theLength + "))/(10**"
				// + theLength + ") ";
				s = " floor(" + theNumber + "*(power(10," + theLength + ")))/(power(10," + theLength + ")) ";
			}
		}
		m_sbDestinationSql.append(s);
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunRound Over");
	}

	/**
	 * 转换Rtrim函数 参数: asSqlWords: Rtrim函数子句 规则: Rtrim(str)->Rtrim(str,' ')
	 */
	private void translateFunRtrim(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunRtrim");
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		String s = new String();

		m_sbDestinationSql.append(" rtrim(");
		while(iOff < asSqlWords.length) {
			if(asSqlWords[iOff].equals("(")) {
				iLBracket++;
			}
			if(asSqlWords[iOff].equals(")")) {
				iRBracket++;
			}
			if(iLBracket == iRBracket && asSqlWords[iOff + 1].equals(")")) {
				if(iLBracket > 0) {
					for(int i = 2; i < iOff + 1; i++) {
						s += " " + asSqlWords[i];
					}
					try {
						translateSelect(parseSql(s));
					} catch(Exception e) {
						System.out.println(e);
					}
				} else {
					for(int i = 2; i < iOff + 1; i++) {
						m_sbDestinationSql.append(" " + asSqlWords[i]);
					}
				}
				m_sbDestinationSql.append(",' ')");
				break;
			} else if(asSqlWords[asSqlWords.length - 1].equals(")")) {
				// 函数嵌套
				String[] newFunSql = new String[asSqlWords.length - (iOff + 1)];
				for(int index = 0; index < newFunSql.length; index++) {
					newFunSql[index] = asSqlWords[iOff];
					iOff++;
				}
				translateSelect(newFunSql);
				// zhangsd modify at 2002-09-01
				if(!newFunSql[newFunSql.length - 1].equalsIgnoreCase("' '")
						&& !newFunSql[newFunSql.length - 1].equalsIgnoreCase(",")) {
					m_sbDestinationSql.append(",' '");
				}
				m_sbDestinationSql.append(" )");
				break;
			} else {
				m_sbDestinationSql.append(asSqlWords[iOff]);
				iOff++;
				throw new Exception(asSqlWords[0] + "后的参数不匹配!");
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunRtrim Over");
	}

	/**
	 * 转换Square函数 参数: asWords square函数子句 规则: square(f)->power(f,2)
	 */
	private void translateFunSquare(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunSquare");
		String s = new String();
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		s += "power(";
		while(iOff < (asWords.length)) {
			if(asWords[iOff].equals("("))
				iLBracket++;
			if(asWords[iOff].equals(")"))
				iRBracket++;
			if((iLBracket == iRBracket) && asWords[iOff + 1].equals(")")) {
				s += " " + asWords[iOff];
				iOff++;
				s += ",2";
			}
			s += " " + asWords[iOff];
			iOff++;
		}
		try {
			translateSelect(parseSql(s));
		} catch(Exception e) {
			System.out.println(e);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateFunSquare Over");
	}

	/**
	 * 处理连接更新 创建日期：(00-6-9 8:38:35)
	 * 
	 * @return java.lang.String[]
	 * @param asSqlWords
	 *            java.lang.String[]
	 *            格式： 第一种情况： update table1 set col1=b.col2 from table1 a, table2
	 *            b where
	 *            a.col3=b.col3 -> update table1 a set col1=(select b2.col2 from
	 *            table2 b
	 *            where a.col3=b.col3)
	 *            第二种情况 update table1 set col1=col1+常量a, a.col2=b.col2+a.col2
	 *            from table1
	 *            a,table2 b
	 *            where a.col3=b.col3 and a.col2=常量 -> update table1 a set
	 *            col1=col1+常量a,col2=(select b2.col2+a.col2 from table2 b
	 *            where a.col3=b.col3 and a.col2=常量b)
	 *            where a.col2=常量b
	 */
	public String[] translateJoinUpdate(String[] asSqlWords, String sTableName, String sTableAlias) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateJoinUpdate");

		int iOffSet = 0; // 偏移量
		String[] asWords = asSqlWords;
		String sSql = ""; // 返回值
		String m_Sql = ""; // 临时sql
		String sLeftField = "";
		String sRightField = "";
		Vector vSetList = new Vector(); // 存放set语句
		String s = ""; // 中间变量
		Vector vecTable = new Vector();
		String whereSql = "";
		String m_whereSql = "";
		String fromSt = "";
		// Vector vSql = new Vector(); //存放sql语句

		int iJoinCount = 0; // join的个数
		int iSingleCount = 0; // 单计数个数
		String subfromSt = "";
		Vector vWhList = new Vector(); // 存放where语句
		String inSql1 = "";
		String inSql2 = "";
		String andSql = "";
		boolean bExist = true;
		String[] asTables = null;
		iOffSet = -1; // 偏移量退回
		// 取出from后的表名
		asTables = parseTable(asSqlWords, sTableName, sTableAlias);
		// 计数小于输入字符串集的长度减1
		while(iOffSet < asWords.length - 1) {
			// 计数加1
			iOffSet++;

			// 若当前字符串是“set”
			if(asWords[iOffSet].equalsIgnoreCase("set")) {
				String str = "";
				String setsql = "";
				int setcount = 0;
				// 若表别名不为空，则记录之

				if(!sTableAlias.equalsIgnoreCase("") && !sTableAlias.equalsIgnoreCase(sTableName)) {
					sSql += " " + sTableAlias;
					// vSql.addElement(sTableAlias);
				}

				sSql += " set";
				// vSql.addElement("set");
				iOffSet++;

				int leftCount = 0; // 左括号数
				int rightCount = 0; // 右括号数

				// 若当前字符串不是“from”，则循环
				while(iOffSet < asWords.length && !asWords[iOffSet].equalsIgnoreCase("from")) {
					if(asWords[iOffSet].equalsIgnoreCase("(")) {
						leftCount++;
					} else if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase(")")) {
						rightCount++;
						if(leftCount == rightCount) {
							leftCount = 0;
							rightCount = 0;
						}
					}

					// 若当前字符串是逗号
					if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase(",") && leftCount == rightCount) {
						// 记录累计数据，将累计器清空
						vSetList.addElement(str);
						str = "";
						iOffSet++;
					} else {
						// 累计当前字符串
						str += " " + asWords[iOffSet];
						iOffSet++;
					}
				}
				// 记录累计数据
				vSetList.addElement(str);

				// 只对一列进行更新
				// 若只有一行数据
				{ // 多列更新
					int i0 = 0;
					// 计数小于记录向量长度，则循环
					while(i0 < vSetList.size()) {
						// 取得当前字符串
						s = (String) vSetList.elementAt(i0);
						// 取得等号的位置
						int start = s.indexOf("=");
						if(haveTab(s.substring(start + 1), asTables)) { // 若发现表名和别名
							iJoinCount++;
							if(iJoinCount > 1) {
								sLeftField += "," + s.substring(0, start); // 暂时保存起来，最后拼接到sql语句
								sRightField += "," + s.substring(start + 1);
							} else {
								sLeftField += " " + s.substring(0, start);
								sRightField += " " + s.substring(start + 1);
							}
						} else {
							setcount++;
							if(setcount > 1) {
								setsql += "," + s;
							} else {
								setsql += " " + s;
							}
						}
						i0++;
					}
				} // //多列更新 else 结束处

				// 若单计数和联合计数均大于0
				if(iSingleCount > 0 && iJoinCount > 0) {
					sSql += ",";
					// vSql.addElement(",");
				}

				// 若单计数大于0或联合计数大于0
				// if (iSingleCount > 0 || iJoinCount > 0)
				if(setcount == 0) {
					if(iJoinCount > 1)
						sSql += "(" + sLeftField + ")=(select " + sRightField; // 将暂时保存起来的语句，拼接到sql语句
					else if(iJoinCount == 1)
						sSql += "" + sLeftField + "=(select " + sRightField;
				} else {
					if(iJoinCount > 1)
						sSql += setsql + ",(" + sLeftField + ")=(select " + sRightField;
					else if(iJoinCount == 1)
						sSql += setsql + "," + sLeftField + "=(select " + sRightField;
					else
						sSql += setsql;
				}
			} // if (asWords[iOffSet].equalsIgnoreCase("set"))结束处

			// 将更新表表名从from子句中剔除（oracle不支持）
			// 若当前字符串是“from”
			if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase("from")) {
				int f_leftCount = 1;
				int f_rightCount = 0;
				iOffSet++;
				Vector aNewVec = new Vector();
				while(iOffSet < asWords.length && !asWords[iOffSet].equalsIgnoreCase("where")) {
					// /////////////

					if(asWords[iOffSet].equalsIgnoreCase("(")) {
						subfromSt = "(";
						while((f_leftCount != f_rightCount) && (iOffSet < asWords.length)) {
							iOffSet++;

							if(asWords[iOffSet].equalsIgnoreCase("(")) {
								f_leftCount++;
							} else if(asWords[iOffSet].equalsIgnoreCase(")")) {
								f_rightCount++;
							}

							subfromSt += " " + asWords[iOffSet];

						}

						aNewVec.addElement(subfromSt);
						iOffSet++;
					}
					// ////////////
					aNewVec.addElement(asWords[iOffSet]);
					iOffSet++;
				}

				for(int newIndex = 0; newIndex < aNewVec.size(); newIndex++) {
					String othtable = aNewVec.elementAt(newIndex).toString();
					String trueName = othtable;
					boolean isOth = false;

					if(!othtable.equalsIgnoreCase(sTableName)) {
						isOth = true;
						if(vecTable.size() > 0) {
							fromSt += ",";
						}
						fromSt += " " + othtable;
						vecTable.addElement(othtable);
					}
					newIndex++;
					if(newIndex < aNewVec.size()) {
						othtable = aNewVec.elementAt(newIndex).toString();
						if(othtable.equalsIgnoreCase("as")) {
							newIndex++;
							othtable = aNewVec.elementAt(newIndex).toString();
						}

						if(!othtable.equalsIgnoreCase(",")) {
							if(isOth) {
								fromSt += " " + othtable;
								vecTable.addElement(othtable);
							} else {
								if(sTableAlias != null && sTableAlias.trim().length() > 0) {
									if(!othtable.equalsIgnoreCase(sTableAlias)) {
										if(vecTable.size() > 0) {
											fromSt += ",";
										}
										fromSt += trueName + " " + othtable;

										vecTable.addElement(trueName);
										vecTable.addElement(othtable);
									}
								}
							}
							newIndex++;
						}
					}
				}

				if(fromSt.trim().length() > 0) {
					fromSt = " from " + fromSt;
				}

				if(fromSt.endsWith(",")) {
					fromSt = fromSt.substring(0, fromSt.length() - 1);
				}
				if(iJoinCount > 0) {
					sSql += fromSt;
				}

			} // if (asWords[iOffSet].equalsIgnoreCase("from"))结束处

			// 当前字符串是“where”
			if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase("where")) {
				int w_leftCount = 0;
				int w_rightCount = 0;
				int inCount = 0;
				int andCount = 0;
				int whereCount = 0;
				int i1 = 0;
				boolean isExist = false;
				String sw = "";
				String s1 = "";
				String w_leftField = "";
				String w_rightField = "";
				iOffSet++;
				m_Sql = sSql;
				m_Sql += " where";
				m_whereSql += " where";
				while(iOffSet < asWords.length) {
					m_Sql += " " + asWords[iOffSet];
					m_whereSql += " " + asWords[iOffSet];
					if(asWords[iOffSet].equalsIgnoreCase("(")) {
						w_leftCount++;
					} else if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase(")")) {
						w_rightCount++;
						if(w_leftCount == w_rightCount) {
							w_leftCount = 0;
							w_rightCount = 0;
						}
					}

					// 分拆where后的条件
					if(iOffSet < asWords.length
							&& (asWords[iOffSet].equalsIgnoreCase("and") || asWords[iOffSet].equalsIgnoreCase("or"))
							&& w_leftCount == w_rightCount) {
						// 记录累计数据，将累计器清空
						vWhList.addElement(sw);
						vWhList.addElement(asWords[iOffSet]);
						sw = "";
						iOffSet++;
					} else {
						// 累计当前字符串
						sw += " " + asWords[iOffSet];
						iOffSet++;
					}
				}
				// 记录累计数据
				vWhList.addElement(sw);

				// 计数小于记录向量长度，则循环
				while(i1 < vWhList.size()) {
					// 取得当前字符串
					s1 = (String) vWhList.elementAt(i1);
					// 判断特殊算术运算符的存在
					if(s1.indexOf("!=") > 0 || s1.indexOf("! =") > 0 || s1.indexOf("<") > 0 || s1.indexOf(">") > 0) {
						isExist = true;
					}

					if(!s1.trim().startsWith("(") && !isExist) {
						// 取得等号的位置
						int start = s1.indexOf("=");

						if(start > 0) {
							// 若发现表名和别名
							w_leftField = s1.substring(0, start);
							w_rightField = s1.substring(start + 1);
							if((isMasterTab(w_leftField, sTableName) || isMasterTab(w_leftField, sTableAlias))
									&& isMasterTab(asTables, w_rightField)) {
								inCount++;
								if(inCount > 1) {
									inSql1 += "," + w_leftField; // 暂时保存起来，最后拼接到sql语句
									inSql2 += "," + w_rightField;
								} else {
									inSql1 += " " + w_leftField;
									inSql2 += " " + w_rightField;
								}
							} else if((isMasterTab(w_rightField, sTableName) || isMasterTab(w_rightField, sTableAlias))
									&& isMasterTab(asTables, w_leftField)) {
								inCount++;
								if(inCount > 1) {
									inSql1 += "," + w_rightField; // 暂时保存起来，最后拼接到sql语句
									inSql2 += "," + w_leftField;
								} else {
									inSql1 += " " + w_rightField;
									inSql2 += " " + w_leftField;
								}
							} else if(isMasterTab(asTables, w_leftField)) {
								whereCount++;
								if(whereCount > 1) {
									whereSql += " " + (String) vWhList.elementAt(i1 - 1) + " " + s1;
								} else {
									whereSql += " " + s1;
								}
							} else if(isMasterTab(w_leftField, sTableName) || isMasterTab(w_leftField, sTableAlias)) {
								// else{
								andCount++;
								if(andCount > 1) {
									andSql += " " + (String) vWhList.elementAt(i1 - 1) + " " + s1;
								} else {
									andSql += " " + s1;
								}
							} else {
								bExist = false;
								break;
							}
						} else { // 不是"="，可能是"is ( not ) null"等
							String firstWord = parseWord(s1);
							if(haveTab(firstWord, asTables)) {
								whereCount++;
								if(whereCount > 1) {
									whereSql += " " + (String) vWhList.elementAt(i1 - 1) + " " + s1;
								} else {
									whereSql += " " + s1;
								}
							} else if(haveTab(firstWord, sTableName) || haveTab(firstWord, sTableAlias)) {
								// else{
								andCount++;
								if(andCount > 1) {
									andSql += " " + (String) vWhList.elementAt(i1 - 1) + " " + s1;
								} else {
									andSql += " " + s1;
								}
							} else {
								bExist = false;
								break;
							}
						}
					} else { // 以"()"括起来的条件、特殊算术运算符存在的条件
						if((haveTab(s1, sTableName) || haveTab(s1, sTableAlias)) && haveTab(s1, asTables)) {
							bExist = false;
							break;
						} else if(haveTab(s1, asTables)) {
							whereCount++;
							if(whereCount > 1) {
								whereSql += " " + (String) vWhList.elementAt(i1 - 1) + " " + s1;
							} else {
								whereSql += " " + s1;
							}

						} else if(haveTab(s1, sTableName) || haveTab(s1, sTableAlias)) {
							andCount++;
							if(andCount > 1) {
								andSql += " " + (String) vWhList.elementAt(i1 - 1) + " " + s1;
							} else {
								andSql += " " + s1;
							}
						} else {
							bExist = false;
							break;
						}
					}
					i1 += 2;
				}
			}
			// sSql += " " + asWords[iOffSet];
			// iOffSet++;
			// 分离单一表条件和连接更新条件
			/*
			 * while (iOffSet < asWords.length) { sSql += " " +
			 * asWords[iOffSet]; whereSql += " " + asWords[iOffSet]; iOffSet++;
			 * }
			 * //while (iOffSet < asWords.length)结束处 } //if
			 * (asWords[iOffSet].equalsIgnoreCase("where"))结束处
			 */// 处理CASE WHEN
			if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase("case")) {
				TransUnit aTransUnit = dealCaseWhen(asWords, asWords[iOffSet], iOffSet);

				iOffSet = aTransUnit.getIOffSet();

				String sSql_new = aTransUnit.getSql();

				sSql += translateJoinUpdate(parseSql(sSql_new));
			} else if(iOffSet < asWords.length) {
				if(asWords[iOffSet].equalsIgnoreCase(sTableName) || asWords[iOffSet].equalsIgnoreCase(sTableAlias)) {
					sSql += " " + sTableName;
				} else {
					sSql += " " + asWords[iOffSet];
				}

				// sSql += " " + asWords[iOffSet];
			}
		} // while (iOffSet < asWords.length - 1)结束处

		m_Sql += ")";
		if(iJoinCount > 0) {
			sSql = m_Sql;
		}
		if(bExist) {
			if(andSql.trim() != null && andSql.trim().length() > 0 && inSql1.trim() != null
					&& inSql1.trim().length() > 0 && whereSql.trim() != null && whereSql.trim().length() > 0) {
				sSql += " where " + andSql + " and (" + inSql1 + ") in ( select " + inSql2 + " " + fromSt + " where "
						+ whereSql + " )";
			} else if(andSql.trim() != null && andSql.trim().length() > 0 && inSql1.trim() != null
					&& inSql1.trim().length() > 0) {
				sSql += " where " + andSql + " and (" + inSql1 + ") in ( select " + inSql2 + " " + fromSt + " )";
			} else if(inSql1.trim() != null && inSql1.trim().length() > 0 && whereSql.trim() != null
					&& whereSql.trim().length() > 0) {
				sSql += " where  (" + inSql1 + ") in ( select " + inSql2 + " " + fromSt + " where " + whereSql + " )";
			} else if(inSql1.trim() != null && inSql1.trim().length() > 0) {
				sSql += " where  (" + inSql1 + ") in ( select " + inSql2 + " " + fromSt + " )";
			} else if(andSql.trim() != null && andSql.trim().length() > 0) {
				sSql += " where " + andSql;
			}
		} else {
			if(m_whereSql != null && m_whereSql.trim().length() > 0) {
				sSql += " where exists( select 1 " + fromSt + " " + m_whereSql + " )";
			}
		}
		asWords = parseSql(sSql);

		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateJoinUpdate Over");
		return asWords;
	}

	/**
	 * 转换 CASE WHEN 语句 参数: asSqlWords CaseWhen语句 返回: destSql 目标语句 规则: case s0
	 * when exp1 then exp2 when exp3 then exp4 ->
	 * decode(s0,exp1,exp2,exp3,exp4,exp5) else exp5 end
	 * case when s0 is null then exp1 when s0=exp2 then exp3 when s0<=exp4 then
	 * exp5 ->
	 * decode(s0,null,exp1,exp2,exp3,least(s0,exp4),exp5,GREATEST(s0,exp6),exp7,
	 * exp8)
	 * when s0>=exp6 then exp7 else exp8 end
	 */
	private String translateSimpleCaseWhen(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateSimpleCaseWhen");
		int iOff = 0;
		// 参数
		String s0 = "";

		String s1 = "decode(";
		iOff++;

		// 第一个字符串不等于“when”，第一种情况

		// 若当前位置的字符串不是“end”
		while((iOff < asSqlWords.length) && !asSqlWords[iOff].equalsIgnoreCase("end")) {
			// 若当前字符串是“when”、“then”或“else”
			if((asSqlWords[iOff].equalsIgnoreCase("when")) || (asSqlWords[iOff].equalsIgnoreCase("then"))
					|| (asSqlWords[iOff].equalsIgnoreCase("else"))) {
				// 加上逗号
				s1 = s1 + ",";

			} else if((asSqlWords[iOff].equalsIgnoreCase("case"))) {
				// 处理嵌套 case when
				TransUnit aTransUnit = dealCaseWhen(asSqlWords, asSqlWords[iOff], iOff);

				iOff = aTransUnit.getIOffSet() - 1;
				String sSql = aTransUnit.getSql();

				// s1 += translateCaseWhen(parseSql(sSql));
				s1 += sSql;
			} else { // 否则，加上当前字符串
				s1 = s1 + " " + asSqlWords[iOff];
			}
			iOff++;
		}
		// 加上括号
		s1 = s1 + ")";

		// return m_sbDestinationSql;
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.translateSimpleCaseWhen Over");
		return s1;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-1-22 10:16:17)
	 * 
	 * @return java.lang.String[]
	 * @param asSqlWords
	 *            java.lang.String[]
	 * @param iOffSet
	 *            int
	 */
	public String[] trimKuohao(String[] asSqlWords, int iOffSet) {
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.trimKuohao");
		int start = iOffSet;
		int left = 0;
		int right = 0;
		while(asSqlWords[iOffSet].equals("(")) {
			for(int i = iOffSet; i < asSqlWords.length; i++) {
				if(asSqlWords[i].equals("(")) {
					left++;
				}
				if(asSqlWords[i].equals(")")) {
					right++;
				}
				if(left == right) {
					asSqlWords[iOffSet] = "";
					asSqlWords[i] = "";
					iOffSet++;
					break;
				}
			}
		}
		Vector vec = new Vector();

		for(int i = 0; i < start; i++) {
			vec.addElement(asSqlWords[i]);
		}

		for(int i = start; i < asSqlWords.length; i++) {
			if(asSqlWords[i] != null && asSqlWords[i].trim().length() > 0) {
				vec.addElement(asSqlWords[i]);
			}
		}
		String[] stArray = new String[vec.size()];
		vec.copyInto(stArray);
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.trimKuohao Over");
		return stArray;
	}

	String m_TabName = null;

}