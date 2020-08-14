package org.nw.dao.sqltranslator;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 模块: TranslateToDB2.java
 * 描述: 将SqlServer语句翻译到DB2语句
 * 作者: cf
 */

public class TranslateToDB2 extends TranslatorObject {
	private static final Log log = LogFactory.getLog(TranslateToDB2.class);

	//函数对照表,列出sqlServer函数与DB2函数的对应关系,
	private String[][] fun_db2 = { { "len", "length" }, { "substring", "substr" }, { "lower", "lcase" },
			{ "upper", "ucase" }, { "isnull", "coalesce" }
	//{"square","power"}		//square(f)->power(f,2)
	};

	//ErrorCode对照表,列出sqlServer ErrorCode与DB2 ErrorCode的对应关系 格式:{db2,SqlServer}
	private int[][] err_db2 = { { -204, 208 }, //表或视图不存在
			{ -104, 2715 }, //函数不存在
			{ -206, 207 }, //无效的列名
			{ -421, 205 }, //使用union的语句和目标列表应具有相同数目的表达式
			{ -408, 213 }, //插入数据和表数据类型不一致
			{ -803, 2627 }, //不能插入相同主键的记录
			{ -407, 515 }, //列值不能为空
			{ -433, 8152 } //插入的值对于列过大
	};

	/**
	 * TransDB2 构造子注释。
	 */
	public TranslateToDB2() {
		super(DB2);
		m_apsFunList = fun_db2;
		m_apiErrorList = err_db2;
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2 Over");
	}

	public String getSql() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.getSql");
		//翻译SQL语句
		translateSql();
		//若翻译结果为空，则返回空
		if(m_sbDestinationSql == null)
			return null;
		String sResult = m_sbDestinationSql.toString();
		//若翻译结果以分号结尾，则去掉分号
		if(sResult.endsWith(";"))
			sResult = sResult.substring(0, sResult.length() - 1);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.getSql Over");
		return sResult;
	}

	/**
	 * 转换Create语句
	 */

	private void translateCreate() {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateCreate");
		m_sbDestinationSql = new StringBuffer(m_sResorceSQL);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateCreate Over");
	}

	/**
	 * 转换Delete语句
	 */

	private StringBuffer translateDelete(String[] sqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateDelete");

		int iOffSet = 0;
		String sSql = new String();
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBrack;

		while(iOffSet < sqlWords.length) {
			sPreWord = sWord;
			sWord = sqlWords[iOffSet];

			//处理函数
			if((sWord.equalsIgnoreCase("square") || sWord.equalsIgnoreCase("patindex"))
					&& sqlWords[iOffSet + 1].equals("(")) {
				iLBracket = 0;
				iRBrack = 0;
				sSql = "";
				sSql = sWord + sqlWords[iOffSet + 1];
				iLBracket++;
				iOffSet++;
				while(iOffSet < sqlWords.length) {
					iOffSet++;
					sSql += " " + sqlWords[iOffSet];
					if(sqlWords[iOffSet].equals("("))
						iLBracket++;
					if(sqlWords[iOffSet].equals(")"))
						iRBrack++;
					if(iLBracket == iRBrack) {
						iOffSet++;
						break;
					}
				}
				if(sWord.equalsIgnoreCase("square")) {
					translateFunSquare(parseSql(sSql));
				}
				if(sWord.equalsIgnoreCase("patindex")) {
					translateFunPatindex(parseSql(sSql));
				}

			}
			//处理PI()
			if(sWord.equalsIgnoreCase("PI") && sqlWords[iOffSet + 1].equals("(") && sqlWords[iOffSet + 2].equals(")")) {
				m_sbDestinationSql.append(" 3.1415926535897931");
				iOffSet += 3;
			}

			//处理模式匹配符
			if(sWord.equalsIgnoreCase("like")) {
				String s = new String();
				s = sqlWords[iOffSet + 1];
				if(s.indexOf("[^") > 0 && s.indexOf("]") > 0) {
					sSql = "";
					sSql = sSql + s.substring(0, s.indexOf("["));
					sSql = sSql + s.substring(s.indexOf("^") + 1, s.indexOf("]"));
					sSql = sSql + s.substring(s.indexOf("]") + 1);
					m_sbDestinationSql.append(" not like " + sSql);
					iOffSet += 2;
				}
			}
			if(sPreWord.equalsIgnoreCase("like")) {
				if(sWord.indexOf("[") > 0 && sWord.indexOf("]") > 0) {
					sSql = "";
					sSql = sSql + sWord.substring(0, sWord.indexOf("["));
					sSql = sSql + sWord.substring(sWord.indexOf("[") + 1, sWord.indexOf("]"));
					sSql = sSql + sWord.substring(sWord.indexOf("]") + 1);
					m_sbDestinationSql.append(" " + sSql);
					iOffSet++;
				}
			}

			//处理子查询
			if(sWord.equalsIgnoreCase("select")) {
				int i0 = 0;
				if(sPreWord.equals("("))
					i0 = sqlWords.length - 1;
				else
					i0 = sqlWords.length;
				sSql = "";
				while(iOffSet < i0) {
					sSql += " " + sqlWords[iOffSet];
					iOffSet++;
				}
				translateSelect(parseSql(sSql));
			} else {
				if(iOffSet < sqlWords.length) {
					if(!sWord.equals(",") && !sWord.equals(")") && !sWord.equals("]") && !sPreWord.equals("(")
							&& !sPreWord.equals("["))
						m_sbDestinationSql.append(" ");
					m_sbDestinationSql.append(sqlWords[iOffSet]);
					iOffSet++;
				} else
					break;
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateDelete Over");
		return m_sbDestinationSql;

	}

	/**
	 * 转换Drop语句
	 */
	private void translateDrop() {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateDrop");
		m_sbDestinationSql = new StringBuffer(m_sResorceSQL);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateDrop Over");
	}

	/**
	 * 根据函数对照表进行函数转换
	 */
	private void translateFunction() {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunction");

		String sWord = null;
		int iOffSet = -1;

		while(iOffSet < m_asSqlWords.length) {
			iOffSet++;
			sWord = m_asSqlWords[iOffSet];
			if((iOffSet + 1) >= m_asSqlWords.length)
				break;
			//找到函数
			if(iOffSet > 1 && m_asSqlWords[iOffSet - 2].equalsIgnoreCase("convert")
					&& m_asSqlWords[iOffSet - 1].equals("(") && m_asSqlWords[iOffSet].equalsIgnoreCase("datetime")) {
				m_asSqlWords[iOffSet] = "date";
				iOffSet++;
			}
			if(m_asSqlWords[iOffSet + 1].equals("(")) {
				m_asSqlWords[iOffSet] = getFunction(sWord);
				iOffSet++;
			}
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunction Over");
	}

	/**
	 * 转换"||"
	 * 参数:
	 * off 偏移量
	 * 返回:
	 * 偏移量
	 * 规则:
	 * 在Oracle中无需转换
	 */
	private int translateII(int ioff) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateII");
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateII Over");
		return ioff;
	}

	/**
	 * 转换Insert语句
	 */
	private StringBuffer translateInsert(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateInsert");

		int iOffSet = 0;
		String sSql = new String();
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBracket;

		while(iOffSet < asSqlWords.length) {
			sPreWord = sWord;
			sWord = asSqlWords[iOffSet];

			//处理函数
			if((sWord.equalsIgnoreCase("square") || sWord.equalsIgnoreCase("patindex"))
					&& asSqlWords[iOffSet + 1].equals("(")) {
				iLBracket = 0;
				iRBracket = 0;
				sSql = "";
				sSql = sWord + asSqlWords[iOffSet + 1];
				iLBracket++;
				iOffSet++;
				while(iOffSet < asSqlWords.length) {
					iOffSet++;
					sSql += " " + asSqlWords[iOffSet];
					if(asSqlWords[iOffSet].equals("("))
						iLBracket++;
					if(asSqlWords[iOffSet].equals(")"))
						iRBracket++;
					if(iLBracket == iRBracket) {
						iOffSet++;
						break;
					}
				}
				if(sWord.equalsIgnoreCase("square")) {
					translateFunSquare(parseSql(sSql));
				}
				if(sWord.equalsIgnoreCase("patindex")) {
					translateFunPatindex(parseSql(sSql));
				}

			}
			//处理PI()
			if(sWord.equalsIgnoreCase("PI") && asSqlWords[iOffSet + 1].equals("(")
					&& asSqlWords[iOffSet + 2].equals(")")) {
				m_sbDestinationSql.append(" 3.1415926535897931");
				iOffSet += 3;
			}

			//处理模式匹配符
			if(sWord.equalsIgnoreCase("like")) {
				String s = new String();
				s = asSqlWords[iOffSet + 1];
				//                if (s.indexOf("[^") > 0 && s.indexOf("]") > 0) {
				//                    sSql = "";
				//                    sSql = sSql + s.substring(0, s.indexOf("["));
				//                    sSql = sSql + s.substring(s.indexOf("^") + 1, s.indexOf("]"));
				//                    sSql = sSql + s.substring(s.indexOf("]") + 1);
				//                    m_sbDestinationSql.append(" not like " + sSql);
				//                    iOffSet += 2;
				//                }
				//                else   if (sWord.indexOf("[") > 0 && sWord.indexOf("]") > 0) {
				//                    sSql = "";
				//                    sSql = sSql + sWord.substring(0, sWord.indexOf("["));
				//                    sSql = sSql + sWord.substring(sWord.indexOf("[") + 1, sWord.indexOf("]"));
				//                    sSql = sSql + sWord.substring(sWord.indexOf("]") + 1);
				//                    m_sbDestinationSql.append(" " + sSql);
				//                    iOffSet++;
				//                }
			}

			//处理子查询
			if(sWord.equalsIgnoreCase("select")) {
				int l = 0;
				if(sPreWord.equals("("))
					l = asSqlWords.length - 1;
				else
					l = asSqlWords.length;
				sSql = "";
				while(iOffSet < l) {
					sSql += " " + asSqlWords[iOffSet];
					iOffSet++;
				}
				translateSelect(parseSql(sSql));
			} else {
				if(iOffSet < asSqlWords.length) {
					if(!sWord.equals(",") && !sWord.equals(")") && !sWord.equals("]") && !sPreWord.equals("(")
							&& !sPreWord.equals("["))
						m_sbDestinationSql.append(" ");
					m_sbDestinationSql.append(asSqlWords[iOffSet]);
					iOffSet++;
				} else
					break;
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateInsert Over");
		return m_sbDestinationSql;

	}

	/**
	 * 处理连接更新
	 * 创建日期：(00-6-9 8:38:35)
	 * 
	 * @return java.lang.String[]
	 * @param asSqlWords java.lang.String[]
	 *            格式：
	 *            第一种情况：
	 *            update table1 set col1=b.col2 from table1 a, table2 b where
	 *            a.col3=b.col3
	 *            ->
	 *            update table1 a set col1=(select b2.col2 from table2 b where
	 *            a.col3=b.col3)
	 *            第二种情况
	 *            update table1 set col1=col1+常量a, a.col2=b.col2+a.col2 from
	 *            table1 a,table2 b
	 *            where a.col3=b.col3 and a.col2=常量
	 *            ->
	 *            update table1 a set col1=col1+常量a,col2=(select b2.col2+a.col2
	 *            from table2 b
	 *            where a.col3=b.col3 and a.col2=常量b)
	 *            where a.col2=常量b
	 */
	private String[] translateJoinUpdate(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateJoinUpdate");

		int iOffSet = 0; //偏移量
		int iOffSet1 = 1; //取表名的偏移量
		String[] asWords = asSqlWords;
		//String sSql = ""; //返回值
		boolean bFind = false;
		//String sLeftField = "";
		//String sRightField = "";
		//java.util.Vector vSetList = new java.util.Vector(); //存放set语句
		String sTableName = ""; //表名
		String sTableAlias = ""; //表的别名
		//String s = ""; //中间变量
		//Vector vecTable = new Vector();

		//是否存在连接更新，并取得更新表的表名和别名
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

		//计数小于输入字符串集的长度
		while(iOffSet < asWords.length) {
			//若当前字符串是“from”
			if(asWords[iOffSet].equalsIgnoreCase("from")) {
				iFromNum++;

				if(iFromNum > iSelectNum) {
					//记录当前位置

					iOffSet++;
					//计数小于输入字符串集的长度
					while(iOffSet < asWords.length) {
						//若当前字符串等于表名
						if(asWords[iOffSet].equalsIgnoreCase(sTableName)) {
							//当前位置加1
							//iOffSet++;
							//若当前字符串为逗号，则跳出循环
							//if (iOffSet < asWords.length
							//&& (asWords[iOffSet].equalsIgnoreCase(",")
							//|| asWords[iOffSet].equalsIgnoreCase("where")))
							{
								if(iOffSet >= 1
										&& (asWords[iOffSet - 1].equalsIgnoreCase(",") || asWords[iOffSet - 1]
												.equalsIgnoreCase("from"))) {
									//sTableAlias = asWords[iOffSet - 1];

									if(iOffSet + 1 < asWords.length) {
										if(asWords[iOffSet + 1].equalsIgnoreCase("as")) {
											if(iOffSet + 2 < asWords.length) {
												sTableAlias = asWords[iOffSet + 2];
											}
										} else {
											if(!asWords[iOffSet + 1].equals(",")
													&& !asWords[iOffSet + 1].equalsIgnoreCase("where")
													&& !asWords[iOffSet + 1].equals("(")
													&& !asWords[iOffSet + 1].equals(")")) {
												sTableAlias = asWords[iOffSet + 1];
											}
										}
									}
								} /*
								 * else if (iOffSet >= 2 && (asWords[iOffSet -
								 * 2].equalsIgnoreCase(",")))
								 * {
								 * sTableAlias = "";
								 * }
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
								 * else
								 * {
								 * //sTableName=asWords[iOffSet-2];
								 * sTableAlias = sTableName;
								 * }
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

		} //while结束处

		if(iFromNum > iSelectNum) {
			bFind = true;
		}
		if(!bFind) //没有发现连接更新或子查询
		{
			return asWords;
		} else {
			log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateJoinUpdate Over");
			return translateJoinUpdate(asSqlWords, sTableName, sTableAlias);
		}
	}

	/**
	 * 翻译Sql语句,进行:
	 * 函数转换
	 */
	private StringBuffer translateSelect(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateSelect");

		int iOffSet = 0;
		String sSql = new String();
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBracket;
		//added by zhangsd
		String topNum = "";
		String replaceTop = "";
		boolean hasTop = false;
		boolean hasWhere = false; //zhangsd add 20020808
		boolean isUpdate = false; //zhangsd add 20020812
		TransUnit aTransUnit = null;

		while(iOffSet < asSqlWords.length) {
			sPreWord = sWord;
			sWord = asSqlWords[iOffSet];

			if(sWord.equalsIgnoreCase("update")) {
				isUpdate = true;
			}
			//在此对函数进行处理
			//如果当前单词为函数名称
			if((iOffSet + 1 < asSqlWords.length) && isFunctionName(sWord, asSqlWords[iOffSet + 1])) {
				aTransUnit = dealFunction(asSqlWords, sWord, iOffSet);

				iOffSet = aTransUnit.getIOffSet();

				if(iOffSet > asSqlWords.length - 1) {
					//函数嵌套
					return null;
				}
			}
			//处理优化关键字
			if(iOffSet < asSqlWords.length && iOffSet + 5 < asSqlWords.length && asSqlWords[iOffSet].equals("/")
					&& asSqlWords[iOffSet + 1].equals("*") && asSqlWords[iOffSet + 2].equals("+")) {
				iOffSet += 3;

				while(!asSqlWords[iOffSet].equals("*") && !asSqlWords[iOffSet + 1].equals("/")) {
					iOffSet += 1;
				}
				iOffSet += 2;

			}

			//处理PI()
			if(sWord.equalsIgnoreCase("PI") && asSqlWords[iOffSet + 1].equals("(")
					&& asSqlWords[iOffSet + 2].equals(")")) {
				m_sbDestinationSql.append(" 3.1415926535897931");
				iOffSet += 3;
			}
			//处理取模%
			if(iOffSet + 2 < asSqlWords.length && asSqlWords[iOffSet + 1].equals("%")) {
				m_sbDestinationSql.append(" mod(" + sWord + "," + asSqlWords[iOffSet + 2] + ")");
				iOffSet += 3;
			}

			//处理模式匹配符
			//            if (sWord.equalsIgnoreCase("like")) {
			//                String s = new String();
			//                s = asSqlWords[iOffSet + 1];
			//                if (s.indexOf("^") > 0 && s.indexOf("]") > 0) {
			//                    sSql = "";
			//                    sSql = sSql + s.substring(0, s.indexOf("["));
			//                    sSql = sSql + s.substring(s.indexOf("^") + 1, s.indexOf("]"));
			//                    sSql = sSql + s.substring(s.indexOf("]") + 1);
			//                    m_sbDestinationSql.append(" not like " + sSql);
			//                    iOffSet += 2;
			//                    
			//                } 

			//                else if (s.indexOf("[") > 0 && s.indexOf("]") > 0) {
			//                    sSql = "";
			//                    sSql = sSql + s.substring(0, s.indexOf("["));
			//                    sSql = sSql + s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			//                    sSql = sSql + s.substring(s.indexOf("]") + 1);
			//                    m_sbDestinationSql.append(" " + sSql);
			//                    iOffSet++;
			//                }
			//           }
			if(iOffSet < asSqlWords.length && iOffSet > 0 && asSqlWords[iOffSet].equalsIgnoreCase("select")
					&& asSqlWords[iOffSet - 1].equals("(")) {
				aTransUnit = dealSelect(asSqlWords, sWord, iOffSet);
				iOffSet = aTransUnit.getIOffSet();

			}
			if(asSqlWords[iOffSet].equalsIgnoreCase("top")) {
				topNum = asSqlWords[iOffSet + 1];
				replaceTop = "fetch first " + topNum + " rows only";
				hasTop = true;
				iOffSet += 2;
			}
			//判断处理columnname+'zhangsd'
			if(iOffSet < asSqlWords.length - 1 && asSqlWords[iOffSet].equalsIgnoreCase("+")
					&& (asSqlWords[iOffSet + 1].indexOf("'") > -1 || asSqlWords[iOffSet - 1].indexOf("'") > -1)) {
				m_sbDestinationSql.append("||");
				iOffSet++;

			}
			if(asSqlWords[iOffSet].equalsIgnoreCase("where")) {
				hasWhere = true;
			}
			if(asSqlWords[iOffSet].equalsIgnoreCase("null") && iOffSet > 0 && iOffSet < 2
					&& !asSqlWords[iOffSet - 1].equals("=") && !asSqlWords[iOffSet - 1].equals("(")) {
				m_sbDestinationSql.append(" nullif('1','1')");
				iOffSet++;
			} else if(asSqlWords[iOffSet].equalsIgnoreCase("null")
					&& iOffSet < asSqlWords.length
					&& iOffSet > 0
					&& !asSqlWords[iOffSet - 1].equals("=")
					&& !asSqlWords[iOffSet - 1].equalsIgnoreCase("is")
					&& iOffSet > 1
					&& !asSqlWords[iOffSet - 1].equalsIgnoreCase("then")
					&& (!asSqlWords[iOffSet - 2].equalsIgnoreCase("is") && !asSqlWords[iOffSet - 1]
							.equalsIgnoreCase("not"))
					&& iOffSet > 1
					&& !((asSqlWords[iOffSet - 2].equalsIgnoreCase("cast")) && (asSqlWords[iOffSet - 1]
							.equalsIgnoreCase("(")))) {
				m_sbDestinationSql.append(" nullif('1','1')");
				iOffSet++;
			} else if(asSqlWords[iOffSet].equalsIgnoreCase("=") && iOffSet < asSqlWords.length && iOffSet > 0
					&& hasWhere && asSqlWords[iOffSet + 1].equalsIgnoreCase("null")) {
				m_sbDestinationSql.append(" is null");
				iOffSet += 2;
			} else {
				if(iOffSet < asSqlWords.length) {
					if(!asSqlWords[iOffSet].equals(",") && !asSqlWords[iOffSet].equals(")")
							&& !asSqlWords[iOffSet].equals("]") && !sPreWord.equals("(") && !sPreWord.equals("["))
						m_sbDestinationSql.append(" ");
				}
				m_sbDestinationSql.append(asSqlWords[iOffSet]);
				//m_sbDestinationSql.append(" ");
				iOffSet++;
			}
		} //while loop end
		if(hasTop) {
			m_sbDestinationSql.append(" " + replaceTop);
		}
		if(m_sbDestinationSql != null) {
			m_sbDestinationSql.replace(0, m_sbDestinationSql.toString().length(), m_sbDestinationSql.toString().trim());
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateSelect Over");
		return m_sbDestinationSql;

	}

	/**
	 * 根据语句类型进行转换
	 */

	private void translateSql() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateSql");

		//分析sql语句,得到sql单词序列
		m_sbDestinationSql = new StringBuffer();
		//m_asSqlWords = parseSql(m_sResorceSQL);
		if(m_asSqlWords == null) {
			m_sbDestinationSql = null;
			log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateSql Over");
			return;
		}

		//先进行函数转换
		translateFunction();

		//根据sql语句类型进行转换
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
			//zhangsd modify 20020808
			//translateDelete(m_asSqlWords);
			translateSelect(m_asSqlWords);
			break;
		case SQL_UPDATE:
			//translateUpdate(m_asSqlWords);
			translateUpdateII(m_asSqlWords);
			break;
		case 8: //if exists
			translateIFExists(m_asSqlWords);
			break;
		}
	}

	boolean m_bSubSelect = false;

	boolean m_bUpdateFrom = false;

	/**
	 * 返回字符串数组asSqlWords中从start到end处的字符串。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private String appendString(String[] asSqlWords, int start, int end) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.appendString");
		if(asSqlWords == null || asSqlWords.length < 1)
			return null;
		if(start < 0 || end < 0 || start > end) {
			System.out.println("In appendString method error");
			return null;
		}
		String result = "";
		for(int i = start; i <= end; i++) {
			result += asSqlWords[i] + " ";
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.appendString Over");
		return result;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	public TransUnit dealFunction(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.dealFunction");

		Vector vec = new Vector();
		vec.addElement(asSqlWords[iOffSet]);

		//vec.addElement(asSqlWords[iOffSet + 1]);
		//iOffSet += 2;
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
			//translateFunLeft(newFuncSql);
		} else if(sWord.equalsIgnoreCase("right")) {
			//translateFunRight(newFuncSql);
		} else if(sWord.equalsIgnoreCase("square")) {
			translateFunSquare(newFuncSql);
		} else if(sWord.equalsIgnoreCase("cast")) {
			translateFunCast(newFuncSql);
		} else if(sWord.equalsIgnoreCase("coalesce")) {
			//translateFunCoalesce(newFuncSql);
		} else if(sWord.equalsIgnoreCase("ltrim")) {
			//translateFunLtrim(newFuncSql);
		} else if(sWord.equalsIgnoreCase("rtrim")) {
			//translateFunRtrim(newFuncSql);
		} else if(sWord.equalsIgnoreCase("patindex")) {
			translateFunPatindex(newFuncSql);
		} else if(sWord.equalsIgnoreCase("len")) {
			//translateFunLen(newFuncSql);
		} else if(sWord.equalsIgnoreCase("round")) {
			translateFunRound(newFuncSql);
		} else if(sWord.equalsIgnoreCase("convert")) {
			translateFunConvert(newFuncSql);
		} else if(sWord.equalsIgnoreCase("dateadd")) {
			translateFunDateAdd(newFuncSql);
		} else if(sWord.equalsIgnoreCase("datediff")) {
			translateFunDateDiff(newFuncSql);
			//iOffSet-=newFuncSql.length;
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.dealFunction Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	public TransUnit dealSelect(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.dealSelect");

		if(iOffSet < asSqlWords.length) {

			TransUnit aTransUnit = getSubSql(asSqlWords, "(", ")", iOffSet);
			String[] newCaseSql = aTransUnit.getSqlArray();
			iOffSet = aTransUnit.getIOffSet();

			String newSql[] = new String[newCaseSql.length - 1];

			for(int i = 0; i < newSql.length; i++) {
				newSql[i] = newCaseSql[i];
			}

			//处理子查询
			TranslateToDB2 newTranslateToDb2 = new TranslateToDB2();

			newTranslateToDb2.setSqlArray(newSql);

			m_sbDestinationSql.append(newTranslateToDb2.getSql());
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.dealSelect Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private String dropTable(String[] asSqlWords, int index) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.dropTable");
		String s = "";
		if(asSqlWords[asSqlWords.length - 1].equalsIgnoreCase("go")) {
			asSqlWords[asSqlWords.length - 1] = ";";
		}
		s = appendString(asSqlWords, index, asSqlWords.length - 1);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.dropTable Over");
		return s;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	public int getStatementType() {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.getStatementType");
		int iType = 0;
		//张时栋  2002.03.07修改
		//若语句长度小于2，则提示错误
		if(m_asSqlWords.length < 1) {
			return iType;
		}
		//修改结束
		//依次比较语句类型
		if(m_asSqlWords[0].equalsIgnoreCase("SELECT")) {
			iType = SQL_SELECT;
		} else if(m_asSqlWords[0].equalsIgnoreCase("INSERT")) {
			iType = SQL_INSERT;
		} else if(m_asSqlWords[0].equalsIgnoreCase("CREATE")) {
			if(m_asSqlWords.length > 1 && m_asSqlWords[1].equalsIgnoreCase("view")) {
				//create view
				iType = SQL_SELECT; //2001.11.06 张森修改，为了支持cteate view
			} else {
				//create table
				iType = SQL_CREATE;
			}
		} else if(m_asSqlWords[0].equalsIgnoreCase("DROP")) {
			iType = SQL_DROP;
		} else if(m_asSqlWords[0].equalsIgnoreCase("DELETE")) {
			iType = SQL_DELETE;
		} else if(m_asSqlWords[0].equalsIgnoreCase("UPDATE")) {
			iType = SQL_UPDATE;
		} else if(m_asSqlWords[0].equalsIgnoreCase("EXPLAIN")) {
			iType = SQL_EXPLAIN;

		} else if(m_asSqlWords[0].equalsIgnoreCase("if") && m_asSqlWords[1].equalsIgnoreCase("exists")) {
			iType = 8;
		} else {
			iType = SQL_SELECT;
		}
		//返回语句类型
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.getStatementType Over");
		return iType;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private boolean hasWhere(String[] asWords, int iOffSet) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.hasWhere");
		if(asWords == null)
			return false;
		for(int i = 0; i < asWords.length; i++) {
			if(asWords[i].equalsIgnoreCase("where"))
				return true;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.hasWhere Over");
		return false;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	public boolean isFunctionName(String sWord, String nextWord) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.isFunctionName");
		boolean isFunc = false;

		if((//(sWord.equalsIgnoreCase("left")&& !nextWord.equalsIgnoreCase("outer")) /** 1 **/
			//|| (sWord.equalsIgnoreCase("right")&& !nextWord.equalsIgnoreCase("outer")) /** 2 **/
				sWord.equalsIgnoreCase("square") //或当前单词为“square”
						|| sWord.equalsIgnoreCase("cast") //或当前单词为“cast”
						//|| sWord.equalsIgnoreCase("coalesce")
						//|| sWord.equalsIgnoreCase("ltrim")
						//|| sWord.equalsIgnoreCase("rtrim")
						|| sWord.equalsIgnoreCase("patindex") //|| sWord.equalsIgnoreCase("len")
						|| sWord.equalsIgnoreCase("round")
						|| sWord.equalsIgnoreCase("convert")
						|| sWord.equalsIgnoreCase("dateadd") || sWord.equalsIgnoreCase("datediff")) //且下一个单词是“(”
				&& nextWord.equals("(")) {
			isFunc = true;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.isFunctionName Over");
		return isFunc;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private int isHasWord(String asSqlWords[], String s) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.isHasWord");
		if(asSqlWords == null || asSqlWords.length < 1)
			return -1;
		int pos = -1;
		int i = 0;
		while(i < asSqlWords.length && (!asSqlWords[i].equalsIgnoreCase(s))) {
			i++;
		}
		if(i < asSqlWords.length)
			pos = i;
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.isHasWord Over");
		return pos;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private void newMethod() {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.newMethod");
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.newMethod Over");
	}

	/**
	 * 在表达式中等号后边寻找是否有指定的表名或表的别名以外的表名或表的别名
	 * 如果有，返回true,否则，返回false
	 * Author:ljq
	 * 
	 * @return boolean
	 * @param s java.lang.String表达式
	 * @param sTableName java.lang.String表名
	 * @param sTableAlias java.lang.String表别名
	 */
	public boolean searchHaveOther(String s, String sTableName, String sTableAlias) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.searchHaveOther");
		String sDotString = "";
		boolean bFlg = false;
		int iStart = 0;
		int iEnd = 0;
		String s0 = "";

		//在表达式中，若最后一个点号在等号的后边
		if(s.lastIndexOf(".") > s.indexOf("=")) //张森2001.3.17   改
		{
			//取得等号的位置
			iStart = s.indexOf("=");
			//截取等号后的字符串
			sDotString = s.substring(iStart);
			//取得下一个等号的位置
			iStart = sDotString.indexOf("=");
			int i0 = 0;
			//当当前位置小于截后字符串的长度减1
			while(i0 < sDotString.length() - 1) {
				//若当前位置的字符为运算符：“+”、“-”、“*”或“/”
				if(sDotString.substring(i0, i0 + 1).equals("+") || sDotString.substring(i0, i0 + 1).equals("-")
						|| sDotString.substring(i0, i0 + 1).equals("*")
						|| sDotString.substring(i0, i0 + 1).equals("/")
						|| sDotString.substring(i0, i0 + 1).equals("%") //张森2001.3.17 加
						|| (sDotString.substring(i0, i0 + 1).equals("|") && sDotString.substring(i0 + 1, i0 + 2)
								.equals("|")) //判断字符连接
						|| sDotString.substring(i0, i0 + 1).equals("(") //判断函数
						|| sDotString.substring(i0, i0 + 1).equals(")")) {
					if(sDotString.substring(i0, i0 + 1).equals("(")) //张森2001.3.17 加,如果遇到括号，则跳过
					{
						iStart = i0;
					} else {
						//截取运算符前的字符串
						s0 = sDotString.substring(iStart + 1, i0);
						//若截取字符串中包含点号
						if(s0.indexOf(".") > 0) {
							//取得点号的位置
							iEnd = s0.indexOf(".");
							//若截取字符串不等于目标表名且不等于目标表别名，则退出循环，且标志为真
							if(!s0.substring(iStart, iEnd).equalsIgnoreCase(sTableName)
									&& !s0.substring(iStart, iEnd).equalsIgnoreCase(sTableAlias)) {
								bFlg = true;
								break;

							} else {
								//若截取字符串等于目标表名且等于目标表别名，//张森2001.3.17 加
								if(sDotString.substring(i0, i0 + 1).equals("|") //如果遇到 "||"
										&& sDotString.substring(i0 + 1, i0 + 2).equals("|")) {
									iStart = i0 + 1;
								} else {
									iStart = i0;
								}
							}
						} else { //若不包含点号
							if(sDotString.substring(i0, i0 + 1).equals("|") //如果遇到 "||",//张森2001.3.17 改
									&& sDotString.substring(i0 + 1, i0 + 2).equals("|")) {
								iStart = i0 + 1;
							} else {
								iStart = i0;
							}

						}
					}
				}

				if(sDotString.substring(i0, i0 + 1).equals("|") //如果遇到 "||"
						&& sDotString.substring(i0 + 1, i0 + 2).equals("|")) {
					i0 += 2; //如果遇到 "||",//张森2001.3.17 改
				} else {
					i0++;
				}
			}

			//若未找到,则查最后一个"."
			if(!bFlg) {
				s0 = sDotString.substring(iStart + 1);
				if(s0.indexOf(".") > 0) {
					iEnd = s0.indexOf(".");
					/*
					 * if (!s0.substring(iStart + 1,
					 * iEnd).equalsIgnoreCase(sTableName)//张森2001.3.17 关掉该语句
					 * && !s0.substring(iStart + 1,
					 * iEnd).equalsIgnoreCase(sTableAlias))
					 */
					if(!s0.substring(0, iEnd).equalsIgnoreCase(sTableName) //张森2001.3.17 改
							&& !s0.substring(0, iEnd).equalsIgnoreCase(sTableAlias)) {
						bFlg = true;
					}
				}
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.searchHaveOther Over");
		return bFlg;
	}

	/**
	 * 在表达式中等号后边]寻找指定的表名或表的别名
	 * 如果有，返回true,否则，返回false
	 * Author:ljq
	 * 
	 * @return boolean
	 * @param s java.lang.String表达式
	 * @param sTableName java.lang.String表名
	 * @param sTableAlias java.lang.String表别名
	 */
	public boolean searchHaving(String s, String sTableName, String sTableAlias) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.searchHaving");
		String sDotString = "";
		boolean bFlg = false;
		int iStart = 0;
		int iEnd = 0;
		String s0 = "";

		//在表达式中，若最后一个点号在等号的后边
		if(s.lastIndexOf(".") > s.indexOf("=")) //张森2001.3.17  改
		{
			//取得等号的位置
			iStart = s.indexOf("=");
			//截取等号后的字符串
			sDotString = s.substring(iStart);
			//取得下一个等号的位置
			iStart = sDotString.indexOf("=");
			int i0 = 0;
			//当当前位置小于截后字符串的长度减1
			while(i0 < sDotString.length() - 1) {
				//若当前位置的字符为运算符：“+”、“-”、“*”或“/”
				if(sDotString.substring(i0, i0 + 1).equals("+") || sDotString.substring(i0, i0 + 1).equals("-")
						|| sDotString.substring(i0, i0 + 1).equals("*")
						|| sDotString.substring(i0, i0 + 1).equals("/")
						|| sDotString.substring(i0, i0 + 1).equals("%") //张森2001.3.17 加
						|| (sDotString.substring(i0, i0 + 1).equals("|") && sDotString.substring(i0 + 1, i0 + 2)
								.equals("|")) //判断字符连接
						|| sDotString.substring(i0, i0 + 1).equals("(") //判断函数
						|| sDotString.substring(i0, i0 + 1).equals(")")) {
					if(sDotString.substring(i0, i0 + 1).equals("(")) //张森2001.3.17 加,如果遇到括号，则跳过
					{
						iStart = i0;
					} else {
						//截取运算符前的字符串
						s0 = sDotString.substring(iStart + 1, i0);
						//若截取字符串中包含点号
						if(s0.indexOf(".") > 0) {
							//取得点号的位置
							iEnd = s0.indexOf(".");
							//若截取字符串等于目标表名或等于目标表别名，则退出循环，且标志为真
							if(s0.substring(iStart, iEnd).equalsIgnoreCase(sTableName)
									|| s0.substring(iStart, iEnd).equalsIgnoreCase(sTableAlias)) {
								bFlg = true;
								break;

							} else {
								//若截取字符串等于目标表名且等于目标表别名，//张森2001.3.17 加
								if(sDotString.substring(i0, i0 + 1).equals("|") //如果遇到 "||"
										&& sDotString.substring(i0 + 1, i0 + 2).equals("|")) {
									iStart = i0 + 1;
								} else {
									iStart = i0;
								}
							}
						} else { //若不包含点号
							if(sDotString.substring(i0, i0 + 1).equals("|") //如果遇到 "||",//张森2001.3.17 改
									&& sDotString.substring(i0 + 1, i0 + 2).equals("|")) {
								iStart = i0 + 1;
							} else {
								iStart = i0;
							}

						}
					}
				}

				if(sDotString.substring(i0, i0 + 1).equals("|") //如果遇到 "||"
						&& sDotString.substring(i0 + 1, i0 + 2).equals("|")) {
					i0 += 2; //如果遇到 "||",//张森2001.3.17 改
				} else {
					i0++;
				}
			}

			//若未找到,则查最后一个"."
			if(!bFlg) {
				s0 = sDotString.substring(iStart + 1);
				if(s0.indexOf(".") > 0) {
					iEnd = s0.indexOf(".");
					/*
					 * if (!s0.substring(iStart + 1,
					 * iEnd).equalsIgnoreCase(sTableName)//张森2001.3.17 关掉该语句
					 * && !s0.substring(iStart + 1,
					 * iEnd).equalsIgnoreCase(sTableAlias))
					 */
					if(s0.substring(0, iEnd).equalsIgnoreCase(sTableName) //张森2001.3.17 改
							|| s0.substring(0, iEnd).equalsIgnoreCase(sTableAlias)) {
						bFlg = true;
					}
				}
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.searchHaving Over");
		return bFlg;
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->power(f,2)
	 */

	private void translateFunConvert(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunConvert");

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
		//取出函数的参数
		String params[] = getFunParam(asWords, iOff, asWords.length - 1);
		dataType = params[0];
		col = params[1];
		dataType = dataType.trim();
		String oldDataType = dataType;
		//取出数据类型
		if(dataType.indexOf("(") > 0) {
			dataType = dataType.substring(0, dataType.indexOf("("));
		}
		if(col.equalsIgnoreCase("null"))
			m_sbDestinationSql.append(" cast(");
		else {
			//日期数据类型
			if(isDateType(dataType)) {
				isDate = true;
				m_sbDestinationSql.append(" timestamp(");
			} else if(isCharType(dataType)) {
				isChar = true;
				if(oldDataType.indexOf("(") > 0) {
					charLenCtrl = true;
					charLenth = oldDataType.substring(oldDataType.indexOf("(") + 1, oldDataType.length() - 1);
				}
				if(charLenCtrl)
					m_sbDestinationSql.append(" substr(char(");
				else
					m_sbDestinationSql.append(" char(");
				if(params.length == 3 && params[2] != null) {
					if(params[2].trim().equals("21"))
						isDateToChar = true;
				}
			} else
				m_sbDestinationSql.append(" cast(");
		}
		try {
			translateSelect(parseSql(col));

			if(isChar) {
				if(charLenCtrl)
					m_sbDestinationSql.append(") ,1," + charLenth + " )");
				else
					m_sbDestinationSql.append(" )");
			} else if(isDate) {
				m_sbDestinationSql.append(")");
			} else
				m_sbDestinationSql.append(" as " + oldDataType + ")");

		} catch(Exception e) {
			throw e;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunConvert Over");
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->power(f,2)
	 */

	private void translateFunDateAdd(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunDateAdd");

		int iOff = 2;

		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		String dateType = params[0].trim();

		String theNumber = params[1].trim();

		String theDate = params[2].trim();

		TranslateToDB2 newTranslateToDB2 = new TranslateToDB2();
		try {
			newTranslateToDB2.setSql(theDate);

			theDate = newTranslateToDB2.getSql();

			//张时栋修改 2002-05-22
			if(theDate.toLowerCase().startsWith("getdate(") || theDate.toLowerCase().startsWith("getdate (")
					|| theDate.toLowerCase().startsWith("getdate  (")
					|| theDate.toLowerCase().startsWith("getdate   (")) {
				theDate = "date(current date)";
			} else {
				theDate = "date(" + theDate + ")";
			}

			if(dateType.equalsIgnoreCase("yy") || dateType.equalsIgnoreCase("yyyy")
					|| dateType.equalsIgnoreCase("year")) {
				theNumber = "decimal(" + theNumber + "0000,8,0)";
			} else if(dateType.equalsIgnoreCase("mm") || dateType.equalsIgnoreCase("m")
					|| dateType.equalsIgnoreCase("month")) {
				theNumber = "decimal(" + theNumber + "00,8,0)";
			} else {
				theNumber = "decimal(" + theNumber + ",8,0)";
			}
			m_sbDestinationSql.append(" " + "date(" + theDate + "-" + theNumber + ")");
		} catch(Exception e) {
			e.printStackTrace();
		}
		//m_sbDestinationSql.append("");
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunDateAdd Over");
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->power(f,2)
	 */

	private void translateFunDateDiff(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunDateDiff");

		int iOff = 2;
		//DATEDIFF(day,  getdate(),'2002-05-15')
		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		String dateType = params[0].trim();

		String theStart = params[1].trim();

		String theEnd = params[2].trim();

		TranslateToDB2 newTranslateToDB2 = new TranslateToDB2();
		try {
			newTranslateToDB2.setSql(theStart);
			theStart = newTranslateToDB2.getSql();

			newTranslateToDB2.setSql(theEnd);
			theEnd = newTranslateToDB2.getSql();
			//张时栋修改 2002-06-12
			if(dateType != null
					&& (dateType.trim().equalsIgnoreCase("day") || dateType.trim().equalsIgnoreCase("dd") || dateType
							.trim().equalsIgnoreCase("d"))) {

				if(theStart.toLowerCase().startsWith("getdate(") || theStart.toLowerCase().startsWith("getdate (")
						|| theStart.toLowerCase().startsWith("getdate  (")
						|| theStart.toLowerCase().startsWith("getdate   (")) {

					theStart = "days(current date)";
				} else {
					theStart = "days(" + theStart + ")";
				}
				if(theEnd.toLowerCase().startsWith("getdate(") || theEnd.toLowerCase().startsWith("getdate (")
						|| theEnd.toLowerCase().startsWith("getdate  (")
						|| theEnd.toLowerCase().startsWith("getdate   (")) {

					theEnd = "days(current date)";
				} else {
					theEnd = "days(" + theEnd + ")";
				}
			} else if(dateType != null
					&& (dateType.trim().equalsIgnoreCase("month") || dateType.trim().equalsIgnoreCase("mm") || dateType
							.trim().equalsIgnoreCase("m"))) {

				if(theStart.toLowerCase().startsWith("getdate(") || theStart.toLowerCase().startsWith("getdate (")
						|| theStart.toLowerCase().startsWith("getdate  (")
						|| theStart.toLowerCase().startsWith("getdate   (")) {

					//hgy
					theStart = "(year(current date)*12 + month(current date))";
				} else {
					//hgy
					theStart = "(year(" + theStart + ")*12 + month(" + theStart + "))";
				}
				if(theEnd.toLowerCase().startsWith("getdate(") || theEnd.toLowerCase().startsWith("getdate (")
						|| theEnd.toLowerCase().startsWith("getdate  (")
						|| theEnd.toLowerCase().startsWith("getdate   (")) {

					//hgy
					theEnd = "(year(current date)*12 + month(current date))";
				} else {
					//hgy
					theEnd = "(year(" + theEnd + ")*12 + month(" + theEnd + "))";
				}
			} else if(dateType != null
					&& (dateType.trim().equalsIgnoreCase("year") || dateType.trim().equalsIgnoreCase("yyyy") || dateType
							.trim().equalsIgnoreCase("yy"))) {

				if(theStart.toLowerCase().startsWith("getdate(") || theStart.toLowerCase().startsWith("getdate (")
						|| theStart.toLowerCase().startsWith("getdate  (")
						|| theStart.toLowerCase().startsWith("getdate   (")) {

					theStart = "year(current date)";
				} else {
					theStart = "year(" + theStart + ")";
				}
				if(theEnd.toLowerCase().startsWith("getdate(") || theEnd.toLowerCase().startsWith("getdate (")
						|| theEnd.toLowerCase().startsWith("getdate  (")
						|| theEnd.toLowerCase().startsWith("getdate   (")) {

					theEnd = "year(current date)";
				} else {
					theEnd = "year(" + theEnd + ")";
				}
			}
			m_sbDestinationSql.append(" " + theEnd + "-" + theStart);
		} catch(Exception e) {
			e.printStackTrace();
		}
		//m_sbDestinationSql.append("");
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunDateDiff Over");
	}

	/**
	 * 转换patindex函数
	 * 参数:
	 * asWords: patindex函数语句
	 * 规则:
	 * patindex('%exp1%',exp2)->locate('exp1,exp2)
	 */

	private void translateFunPatindex(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunPatindex");
		int iOff = 2;
		String sSql = "locate(";

		String sWord = asWords[iOff];
		if(sWord.length() > 4 && asWords[iOff + 1].equals(",")) {
			sSql += "'" + sWord.substring(2, sWord.length() - 2) + "',";
			iOff += 2;
			while(iOff < asWords.length) {
				sSql += " " + asWords[iOff];
				iOff++;
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
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunPatindex Over");
	}

	/**
	 * 转换Rtrim函数
	 * 参数:
	 * asSqlWords: Ltrim函数子句
	 * 规则:
	 * Ltrim(str)->Ltrim(str,' ')
	 */
	private void translateFunRound(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunRound");
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

		TranslateToDB2 newTranslateToDB2 = null;

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
						if(newTranslateToDB2 == null) {
							newTranslateToDB2 = new TranslateToDB2();
						}

						newTranslateToDB2.setSql(theNumber);

						theNumber = newTranslateToDB2.getSql();
					}
				} else {
					secondCommaIndex = iOff;

					for(int i = firstCommaIndex + 1; i < iOff; i++) {
						theLength += " " + asSqlWords[i];
					}

					if(iOff - (firstCommaIndex + 1) > 1) {
						if(newTranslateToDB2 == null) {
							newTranslateToDB2 = new TranslateToDB2();
						}

						newTranslateToDB2.setSql(theLength);

						theLength = newTranslateToDB2.getSql();
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

		} else if(commaCount == 1) {
			for(int i = firstCommaIndex + 1; i < asSqlWords.length - 1; i++) {
				theLength += " " + asSqlWords[i];
			}

			if((asSqlWords.length - 1) - (firstCommaIndex + 1) > 1) {
				if(newTranslateToDB2 == null) {
					newTranslateToDB2 = new TranslateToDB2();
				}

				newTranslateToDB2.setSql(theLength);

				theLength = newTranslateToDB2.getSql();
			}

			s = " round(" + theNumber + ", " + theLength + ") ";
		} else {
			for(int i = secondCommaIndex + 1; i < asSqlWords.length - 1; i++) {
				theTyle += " " + asSqlWords[i];
			}

			if((asSqlWords.length - 1) - (secondCommaIndex + 1) > 1) {
				if(newTranslateToDB2 == null) {
					newTranslateToDB2 = new TranslateToDB2();
				}

				newTranslateToDB2.setSql(theTyle);

				theTyle = newTranslateToDB2.getSql();
			}

			int tyle = Integer.valueOf(theTyle.trim()).intValue();

			if(tyle == 0) {
				s = " round(" + theNumber + ", " + theLength + ") ";
			} else {
				//s =" floor(" + theNumber + "*(10**" + theLength + "))/(10**" + theLength + ") ";
				s = " floor(" + theNumber + "*(power(10.0," + theLength + ")))/(power(10.0," + theLength + ")) ";
			}
		}
		m_sbDestinationSql.append(s);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunRound Over");
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->power(f,2)
	 */

	private void translateFunSquare(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunSquare");
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
				s += asWords[iOff];
				iOff++;
				s += ",2";
			}
			s += asWords[iOff];
			iOff++;
		}
		try {
			translateSelect(parseSql(s));
		} catch(Exception e) {
			System.out.println(e);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunSquare Over");
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private void translateIFExists(String[] asSqlWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateIFExists");
		int index = -1;
		String result = "";
		if((index = isHasWord(asSqlWords, "drop")) > -1 && asSqlWords[index + 1].equalsIgnoreCase("table")) {
			result = dropTable(asSqlWords, index);
		}
		m_sbDestinationSql.append(result);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateIFExists Over");
	}

	/**
	 * 处理连接更新
	 * 创建日期：(00-6-9 8:38:35)
	 * 
	 * @return java.lang.String[]
	 * @param asSqlWords java.lang.String[]
	 *            格式：
	 *            第一种情况：
	 *            update table1 set col1=b.col2 from table1 a, table2 b where
	 *            a.col3=b.col3
	 *            ->
	 *            update table1 a set col1=(select b2.col2 from table2 b where
	 *            a.col3=b.col3)
	 *            第二种情况
	 *            update table1 set col1=col1+常量a, a.col2=b.col2+a.col2 from
	 *            table1 a,table2 b
	 *            where a.col3=b.col3 and a.col2=常量
	 *            ->
	 *            update table1 a set col1=col1+常量a,col2=(select b2.col2+a.col2
	 *            from table2 b
	 *            where a.col3=b.col3 and a.col2=常量b)
	 *            where a.col2=常量b
	 */
	private String[] translateJoinUpdate(String[] asSqlWords, String sTableName, String sTableAlias) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateJoinUpdate");

		int iOffSet = 0; //偏移量
		String[] asWords = asSqlWords;
		String sSql = ""; //返回值
		String m_Sql = ""; //临时sql
		String sLeftField = "";
		String sRightField = "";
		Vector vSetList = new Vector(); //存放set语句
		String s = ""; //中间变量
		Vector vecTable = new Vector();

		String whereSql = "";
		String fromSt = "";
		//Vector vSql = new Vector(); //存放sql语句

		iOffSet = -1; //偏移量退回
		String[] asTables = null;

		int iJoinCount = 0; //join的个数

		int iSingleCount = 0; //单计数个数
		String subfromSt = "";
		Vector vWhList = new Vector(); //存放where语句
		String inSql1 = "";
		String inSql2 = "";
		String andSql = "";
		boolean bExist = true;
		String m_whereSql = "";

		asTables = parseTable(asSqlWords, sTableName, sTableAlias);
		//计数小于输入字符串集的长度减1
		while(iOffSet < asWords.length - 1) {
			//计数加1
			iOffSet++;

			//若当前字符串是“set”
			if(asWords[iOffSet].equalsIgnoreCase("set")) {
				String str = "";
				String setsql = "";
				int setcount = 0;
				//若表别名不为空，则记录之

				if(!sTableAlias.equalsIgnoreCase("") && !sTableAlias.equalsIgnoreCase(sTableName)) {
					sSql += " " + sTableAlias;
					//vSql.addElement(sTableAlias);
				}

				sSql += " set";
				//vSql.addElement("set");
				iOffSet++;

				int leftCount = 0; //左括号数
				int rightCount = 0; //右括号数

				//若当前字符串不是“from”，则循环
				while(iOffSet < asWords.length && !asWords[iOffSet].equalsIgnoreCase("from")) {
					if(asWords[iOffSet].equalsIgnoreCase("(")) //左括号数，张森，2001。3。17 加
					{
						leftCount++;
					} else if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase(")")) {
						rightCount++;
						if(leftCount == rightCount) {
							leftCount = 0;
							rightCount = 0;
						}
					}

					//若当前字符串是逗号
					if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase(",") && leftCount == rightCount) {
						//记录累计数据，将累计器清空
						vSetList.addElement(str);
						str = "";
						iOffSet++;
					} else {
						//累计当前字符串
						str += " " + asWords[iOffSet];
						iOffSet++;
					}
				}
				//记录累计数据
				vSetList.addElement(str);

				//只对一列进行更新
				//若只有一行数据
				{ //多列更新
					int i0 = 0;
					//计数小于记录向量长度，则循环
					while(i0 < vSetList.size()) {
						//取得当前字符串
						s = (String) vSetList.elementAt(i0);
						//取得等号的位置
						int start = s.indexOf("=");
						if(haveTab(s.substring(start + 1), asTables)) { //若发现表名和别名
							iJoinCount++;
							if(iJoinCount > 1) {
								sLeftField += "," + s.substring(0, start); //暂时保存起来，最后拼接到sql语句
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
				} ////多列更新 else 结束处

				//若单计数和联合计数均大于0
				if(iSingleCount > 0 && iJoinCount > 0) {
					sSql += ",";
					//vSql.addElement(",");
				}

				//若单计数大于0或联合计数大于0
				//if (iSingleCount > 0 || iJoinCount > 0)
				if(setcount == 0) {
					if(iJoinCount > 1)
						sSql += "(" + sLeftField + ")=(select " + sRightField; //将暂时保存起来的语句，拼接到sql语句
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
			} //if (asWords[iOffSet].equalsIgnoreCase("set"))结束处

			//将更新表表名从from子句中剔除
			//若当前字符串是“from”
			if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase("from")) {
				iOffSet++;
				int f_leftCount = 1;
				int f_rightCount = 0;
				Vector aNewVec = new Vector();
				while(iOffSet < asWords.length && !asWords[iOffSet].equalsIgnoreCase("where")) {
					///////////////
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
					//////////////
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
				//存在连接表
				if(iJoinCount > 0) {
					sSql += fromSt;
				}

			} //if (asWords[iOffSet].equalsIgnoreCase("from"))结束处

			//当前字符串是“where”
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

					//分拆where后的条件
					if(iOffSet < asWords.length
							&& (asWords[iOffSet].equalsIgnoreCase("and") || asWords[iOffSet].equalsIgnoreCase("or"))
							&& w_leftCount == w_rightCount) {
						//记录累计数据，将累计器清空
						vWhList.addElement(sw);
						vWhList.addElement(asWords[iOffSet]);
						sw = "";
						iOffSet++;
					} else {
						//累计当前字符串
						sw += " " + asWords[iOffSet];
						iOffSet++;
					}
				}
				//记录累计数据
				vWhList.addElement(sw);

				//计数小于记录向量长度，则循环
				while(i1 < vWhList.size()) {
					//取得当前字符串
					s1 = (String) vWhList.elementAt(i1);

					//判断特殊算术运算符的存在
					if(s1.indexOf("!=") > 0 || s1.indexOf("! =") > 0 || s1.indexOf("<") > 0 || s1.indexOf(">") > 0) {
						isExist = true;
					}

					if(!s1.trim().startsWith("(") && !isExist) {
						//取得等号的位置
						int start = s1.indexOf("=");

						if(start > 0) {
							//若发现表名和别名
							w_leftField = s1.substring(0, start);
							w_rightField = s1.substring(start + 1);
							if((isMasterTab(w_leftField, sTableName) || isMasterTab(w_leftField, sTableAlias))
									&& isMasterTab(asTables, w_rightField)) {
								inCount++;
								if(inCount > 1) {
									inSql1 += "," + w_leftField; //暂时保存起来，最后拼接到sql语句
									inSql2 += "," + w_rightField;
								} else {
									inSql1 += " " + w_leftField;
									inSql2 += " " + w_rightField;
								}
							} else if((isMasterTab(w_rightField, sTableName) || isMasterTab(w_rightField, sTableAlias))
									&& isMasterTab(asTables, w_leftField)) {
								inCount++;
								if(inCount > 1) {
									inSql1 += "," + w_rightField; //暂时保存起来，最后拼接到sql语句
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
						} else { //不是"="，可能是"is ( not ) null"等
							String firstWord = parseWord(s1);
							if(haveTab(firstWord, asTables)) {
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
					} else {//以"()"括起来的条件、特殊算术运算符存在的条件
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
			//sSql += " " + asWords[iOffSet];
			//iOffSet++;
			//分离单一表条件和连接更新条件
			/*
			 * while (iOffSet < asWords.length) {
			 * sSql += " " + asWords[iOffSet];
			 * whereSql += " " + asWords[iOffSet];
			 * iOffSet++;
			 * } //while (iOffSet < asWords.length)结束处
			 * } //if (asWords[iOffSet].equalsIgnoreCase("where"))结束处
			 *///处理CASE WHEN
			if(iOffSet < asWords.length && asWords[iOffSet].equalsIgnoreCase("case")) {
				//TransUnit aTransUnit = dealCaseWhen(asWords, asWords[iOffSet], iOffSet);

				//iOffSet = aTransUnit.getIOffSet();

				//String sSql_new = aTransUnit.getSql();

				//sSql += translateJoinUpdate(parseSql(sSql_new));
			} else if(iOffSet < asWords.length) {
				if(asWords[iOffSet].equalsIgnoreCase(sTableName) || asWords[iOffSet].equalsIgnoreCase(sTableAlias)) {
					sSql += " " + sTableName;
				} else {
					sSql += " " + asWords[iOffSet];
				}

				//sSql += " " + asWords[iOffSet];
			}
		} //while (iOffSet < asWords.length - 1)结束处

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

		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateJoinUpdate Over");
		return asWords;
	}

	/**
	 * 转换Update语句
	 * 2002-07-22 暂时不用该函数
	 */
	private StringBuffer translateUpdate_old(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateUpdate_old");

		int iOffSet = 0;
		String sSql = new String();
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBracket;
		String[] asWords = null;
		asWords = asSqlWords;
		String sUpdateTable = "";
		String sUpdateAlias = "";
		boolean bInfrom = false;
		//asWords = translateJoinUpdate(asSqlWords);

		TransUnit aTransUnit = null;

		while(iOffSet < asWords.length) {
			sPreWord = sWord;
			sWord = asWords[iOffSet];

			//if(sPreWord.equalsIgnoreCase("update")){
			//if(asSqlWords[iOffSet+1].equalsIgnoreCase("set")){
			//sUpdateTable=asSqlWords[iOffSet];
			//}
			//else if(asSqlWords[iOffSet+2].equalsIgnoreCase("set")){
			//sUpdateTable=asSqlWords[iOffSet];
			//sUpdateAlias=asSqlWords[iOffSet+1];
			//}

			//}
			//处理函数
			if((iOffSet + 1 < asSqlWords.length) && isFunctionName(sWord, asSqlWords[iOffSet + 1])) {
				aTransUnit = dealFunction(asSqlWords, sWord, iOffSet);

				iOffSet = aTransUnit.getIOffSet();

				if(iOffSet > asSqlWords.length - 1) {
					//函数嵌套
					return null;
				}
			}
			//处理PI()
			if(asWords[iOffSet].equalsIgnoreCase("PI") && asWords[iOffSet + 1].equals("(")
					&& asWords[iOffSet + 2].equals(")")) {
				m_sbDestinationSql.append(" 3.1415926535897931");
				iOffSet += 3;
			}

			//处理模式匹配符
			if(asWords[iOffSet].equalsIgnoreCase("like")) {
				String s = new String();
				s = asWords[iOffSet + 1];
				if(s.indexOf("[^") > 0 && s.indexOf("]") > 0) {
					sSql = "";
					sSql = sSql + s.substring(0, s.indexOf("["));
					sSql = sSql + s.substring(s.indexOf("^") + 1, s.indexOf("]"));
					sSql = sSql + s.substring(s.indexOf("]") + 1);
					m_sbDestinationSql.append(" not like " + sSql);
					iOffSet += 2;
				}
			}
			if(sPreWord.equalsIgnoreCase("like")) {
				if(asWords[iOffSet].indexOf("[") > 0 && asWords[iOffSet].indexOf("]") > 0) {
					sSql = "";
					sSql = sSql + asWords[iOffSet].substring(0, asWords[iOffSet].indexOf("["));
					sSql = sSql
							+ asWords[iOffSet].substring(asWords[iOffSet].indexOf("[") + 1,
									asWords[iOffSet].indexOf("]"));
					sSql = sSql + asWords[iOffSet].substring(asWords[iOffSet].indexOf("]") + 1);
					m_sbDestinationSql.append(" " + sSql);
					iOffSet++;
				}
			}

			//处理子查询
			if(asWords[iOffSet].equalsIgnoreCase("select")) {
				int l = 0;
				if(sPreWord.equals("("))
					l = asWords.length - 1;
				else
					l = asWords.length;
				sSql = "";
				while(iOffSet < l) {
					sSql += " " + asWords[iOffSet];
					iOffSet++;
				}
				m_bSubSelect = true;
				translateSelect(parseSql(sSql));
				m_bSubSelect = false;
			}
			if(asWords[iOffSet].equalsIgnoreCase("from") && !m_bSubSelect) {
				//如果from 的后面有 where
				if(hasWhere(asWords, iOffSet)) {
					m_bUpdateFrom = true;
					m_sbDestinationSql.append(" where exists (select 1 ");
					m_sbDestinationSql.append(asWords[iOffSet]);
					iOffSet++;
				}
			} else {
				if(iOffSet < asWords.length) {
					if(!asWords[iOffSet].equals(",") && !asWords[iOffSet].equals(")") && !asWords[iOffSet].equals("]")
							&& !sPreWord.equals("(") && !sPreWord.equals("["))
						m_sbDestinationSql.append(" ");
					m_sbDestinationSql.append(asWords[iOffSet]);
					iOffSet++;
				} else
					break;
			}
		}
		if(m_bUpdateFrom) {
			if(m_sbDestinationSql.toString().endsWith(";")) {
				m_sbDestinationSql.replace(m_sbDestinationSql.toString().length() - 1, m_sbDestinationSql.toString()
						.length() - 1, ")");
			} else {
				m_sbDestinationSql.append(")");
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateUpdate_old Over");
		return m_sbDestinationSql;

	}

	/**
	 * 转换Update语句
	 */
	private StringBuffer translateUpdateII(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateUpdateII");

		int iOffSet = 0; //偏移量
		String[] asWords = null;
		String sSql = "";
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBracket;
		String s = new String();

		//处理连接更新
		asWords = translateJoinUpdate(asSqlWords);

		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateUpdateII Over");
		return translateSelect(asWords);

	}

	private void translateFunCast(String[] asWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.translateFunCast");

		String s = new String();
		s = "";
		int iOff = 2;
		int iLBracket = 0;
		int iRBracket = 0;
		boolean charLenCtrl = false;
		boolean isDate = false;
		boolean isChar = false;
		String charLenth = null;
		//未至倒数第一，则循环
		while(iOff < (asWords.length - 1)) {
			//若当前字符串为左括号
			if(asWords[iOff].equals("("))
				iLBracket++;
			//若当前字符串为右括号
			if(asWords[iOff].equals(")"))
				iRBracket++;
			//若左标记等于右标记，且下一个字符串为“as”
			if(iLBracket == iRBracket && asWords[iOff + 1].equalsIgnoreCase("as")) {
				if(asWords[iOff - 1].equalsIgnoreCase("(") && asWords[iOff].equalsIgnoreCase("null"))
					m_sbDestinationSql.append(" cast(");
				else {
					if(isDateType(asWords[iOff + 2])) {
						isDate = true;
						m_sbDestinationSql.append(" timestamp(");
					} else if(isCharType(asWords[iOff + 2])) {
						isChar = true;
						if(iOff + 4 < asWords.length && asWords[iOff + 3].equals("(")) {
							charLenCtrl = true;
							charLenth = asWords[iOff + 4];
							m_sbDestinationSql.append(" substr(char(");
						} else {
							m_sbDestinationSql.append(" char(");
						}
					} else {
						m_sbDestinationSql.append(" cast(");
					}
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
				m_sbDestinationSql.append(")");
			} else
				while(iOff < (asWords.length)) {
					m_sbDestinationSql.append(" " + asWords[iOff]);
					iOff++;
				}
		} catch(Exception e) {
			throw e;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToOracle.TranslateToDB2 Over");
	}
}