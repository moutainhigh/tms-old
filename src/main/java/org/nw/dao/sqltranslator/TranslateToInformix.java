package org.nw.dao.sqltranslator;

/**
 * 此处插入类型说明。
 * 创建日期：(2004-10-9 14:38:56)
 * @author：杨志强
 */
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 模块: TranslateToInformix.java
 * 描述: 将SqlServer语句翻译到Informix语句
 */

public class TranslateToInformix extends TranslatorObject {
	private static final Log log = LogFactory.getLog(TranslateToInformix.class);

	public String getSql() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.getSql");
		//翻译SQL语句
		translateSql();
		//若翻译结果为空，则返回空
		if(m_sbDestinationSql == null)
			return null;
		String sResult = m_sbDestinationSql.toString();
		//若翻译结果以分号结尾，则去掉分号
		if(sResult.endsWith(";"))
			sResult = sResult.substring(0, sResult.length() - 1);
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.getSql Over");
		return sResult;
	}

	/**
	 * 转换Create语句
	 */

	private void translateCreate() {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateCreate");
		m_sbDestinationSql = new StringBuffer(m_sResorceSQL);
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateCreate Over");
	}

	/**
	 * 转换Delete语句
	 */

	private StringBuffer translateDelete(String[] sqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateDelete");

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
					//					translateFunPatindex(parseSql(sSql));
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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateDelete Over");
		return m_sbDestinationSql;

	}

	/**
	 * 转换Drop语句
	 */
	private void translateDrop() {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateDrop");
		m_sbDestinationSql = new StringBuffer(m_sResorceSQL);
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateDrop Over");
	}

	/**
	 * 根据函数对照表进行函数转换
	 */
	private void translateFunction() {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunction");

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
				m_asSqlWords[iOffSet] = "datetime";
				iOffSet++;
			}
			if(m_asSqlWords[iOffSet + 1].equals("(")) {
				m_asSqlWords[iOffSet] = getFunction(sWord);
				iOffSet++;
			}
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunction Over");
	}

	/**
	 * 转换Insert语句
	 */
	private StringBuffer translateInsert(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateInsert");

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
					//					translateFunPatindex(parseSql(sSql));
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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateInsert Over");
		return m_sbDestinationSql;

	}

	/**
	 * 处理连接更新
	 * 创建日期：(2004-11-05 8:38:35)
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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateJoinUpdate");

		int iOffSet = 0; //偏移量
		int iOffSet1 = 1; //取表名的偏移量
		String[] asWords = asSqlWords;
		//String sSql = ""; //返回值
		boolean bFind = false;
		String sTableName = ""; //表名
		String sTableAlias = ""; //表的别名
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
								} else if(iOffSet >= 2
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
			log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateJoinUpdate Over");
			return translateJoinUpdate(asSqlWords, sTableName, sTableAlias);
		}
	}

	/**
	 * 翻译Sql语句,进行:
	 * 函数转换
	 */
	private StringBuffer translateSelect(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateSelect");

		int iOffSet = 0;
		String sSql = new String();
		String sWord = "";
		String sPreWord = "";
		int iLBracket;
		int iRBracket;
		String topNum = "";
		String replaceTop = "";
		boolean hasTop = false;
		boolean hasWhere = false;
		boolean isUpdate = false;
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
			//处理oracle优化关键字
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

			//处理count(1)->count(*)
			if(iOffSet + 3 < asSqlWords.length && asSqlWords[iOffSet].equalsIgnoreCase("count")
					&& asSqlWords[iOffSet + 1].equals("(")
					&& (asSqlWords[iOffSet + 2].equals("1") || asSqlWords[iOffSet + 2].equals("0"))
					&& asSqlWords[iOffSet + 3].equals(")")) {
				m_sbDestinationSql.append(" count(*) ");
				iOffSet += 4;
			}

			//处理取模%
			if(iOffSet + 2 < asSqlWords.length && asSqlWords[iOffSet + 1].equals("%")) {
				m_sbDestinationSql.append(" mod(" + sWord + "," + asSqlWords[iOffSet + 2] + ")");
				iOffSet += 3;
			}

			//处理模式匹配符
			if(sWord.equalsIgnoreCase("like")) {
				String s = new String();
				s = asSqlWords[iOffSet + 1];
				if(s.indexOf("[^") > 0 && s.indexOf("]") > 0) {
					sSql = "";
					sSql = sSql + s.substring(0, s.indexOf("["));
					sSql = sSql + s.substring(s.indexOf("^") + 1, s.indexOf("]"));
					sSql = sSql + s.substring(s.indexOf("]") + 1);
					m_sbDestinationSql.append(" not like " + sSql);
					iOffSet += 2;
				} else if(s.indexOf("[") > 0 && s.indexOf("]") > 0) {
					sSql = "";
					sSql = sSql + s.substring(0, s.indexOf("["));
					sSql = sSql + s.substring(s.indexOf("[") + 1, s.indexOf("]"));
					sSql = sSql + s.substring(s.indexOf("]") + 1);
					m_sbDestinationSql.append(" " + sSql);
					iOffSet++;
				}
			}
			//处理子查询
			if(iOffSet < asSqlWords.length && iOffSet > 0 && asSqlWords[iOffSet].equalsIgnoreCase("select")
					&& asSqlWords[iOffSet - 1].equals("(")) {
				aTransUnit = dealSelect(asSqlWords, sWord, iOffSet);
				iOffSet = aTransUnit.getIOffSet();

			}
			//处理top关键字
			if(asSqlWords[iOffSet].equalsIgnoreCase("top")) {
				m_sbDestinationSql.append(" first");
				iOffSet++;
			}
			//处理columnname+columnname
			if(iOffSet < asSqlWords.length - 1 && asSqlWords[iOffSet].equalsIgnoreCase("+")
					&& (asSqlWords[iOffSet + 1].indexOf("'") > -1 || asSqlWords[iOffSet - 1].indexOf("'") > -1)) {
				m_sbDestinationSql.append("||");
				iOffSet++;

			}
			if(asSqlWords[iOffSet].equalsIgnoreCase("where")) {
				hasWhere = true;
			}
			//处理null
			if(asSqlWords[iOffSet].equalsIgnoreCase("null") && iOffSet > 0 && iOffSet < 2
					&& !asSqlWords[iOffSet - 1].equals("=") && !asSqlWords[iOffSet - 1].equals("(")) {
				m_sbDestinationSql.append(" null::char");
				iOffSet++;
			} else if(asSqlWords[iOffSet].equalsIgnoreCase("null")
					&& iOffSet < asSqlWords.length
					&& iOffSet > 1
					&& !asSqlWords[iOffSet - 1].equals("=")
					&& !asSqlWords[iOffSet - 1].equalsIgnoreCase("is")
					&& (!asSqlWords[iOffSet - 2].equalsIgnoreCase("is") && !asSqlWords[iOffSet - 1]
							.equalsIgnoreCase("not"))
					&& !((asSqlWords[iOffSet - 2].equalsIgnoreCase("cast")) && (asSqlWords[iOffSet - 1]
							.equalsIgnoreCase("(")))) {
				m_sbDestinationSql.append(" null::char");
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

		if(m_sbDestinationSql != null) {
			m_sbDestinationSql.replace(0, m_sbDestinationSql.toString().length(), m_sbDestinationSql.toString().trim());
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateSelect Over");
		return m_sbDestinationSql;

	}

	/**
	 * 根据语句类型进行转换
	 */

	private void translateSql() throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateSql");

		//分析sql语句,得到sql单词序列
		m_sbDestinationSql = new StringBuffer();
		if(m_asSqlWords == null) {
			m_sbDestinationSql = null;
			log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateSql Over");
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
			translateSelect(m_asSqlWords);
			break;
		case SQL_UPDATE:
			//translateUpdate(m_asSqlWords);
			translateUpdate(m_asSqlWords);
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
	 * 创建日期：(2004-11-03 19:50:50)
	 */
	private String appendString(String[] asSqlWords, int start, int end) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.appendString");
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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.appendString Over");
		return result;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-11-03 19:50:50)
	 */
	public TransUnit dealFunction(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.dealFunction");

		Vector vec = new Vector();
		vec.addElement(asSqlWords[iOffSet]);
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
			//translateFunCast(newFuncSql);
		} else if(sWord.equalsIgnoreCase("coalesce")) {
			translateFunCoalesce(newFuncSql);
		} else if(sWord.equalsIgnoreCase("ltrim")) {
			//translateFunLtrim(newFuncSql);
		} else if(sWord.equalsIgnoreCase("rtrim")) {
			//translateFunRtrim(newFuncSql);
		} else if(sWord.equalsIgnoreCase("patindex")) {
			//translateFunPatindex(newFuncSql);
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

		}

		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.dealFunction Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-11-03 19:50:50)
	 */
	public TransUnit dealSelect(String[] asSqlWords, String sWord, int iOffSet) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.dealSelect");

		if(iOffSet < asSqlWords.length) {

			TransUnit aTransUnit = getSubSql(asSqlWords, "(", ")", iOffSet);
			String[] newCaseSql = aTransUnit.getSqlArray();
			iOffSet = aTransUnit.getIOffSet();

			String newSql[] = new String[newCaseSql.length - 1];

			for(int i = 0; i < newSql.length; i++) {
				newSql[i] = newCaseSql[i];
			}

			//处理子查询
			TranslateToInformix newTranslateToInformix = new TranslateToInformix();

			newTranslateToInformix.setSqlArray(newSql);

			m_sbDestinationSql.append(newTranslateToInformix.getSql());
		}

		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.dealSelect Over");
		return new TransUnit(null, null, iOffSet);
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-11-03 19:50:50)
	 */
	private String dropTable(String[] asSqlWords, int index) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.dropTable");
		String s = "";
		if(asSqlWords[asSqlWords.length - 1].equalsIgnoreCase("go")) {
			asSqlWords[asSqlWords.length - 1] = ";";
		}
		s = appendString(asSqlWords, index, asSqlWords.length - 1);
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.dropTable Over");
		return s;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-11-03 19:50:50)
	 */
	public boolean isFunctionName(String sWord, String nextWord) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.isFunctionName");
		boolean isFunc = false;

		if((sWord.equalsIgnoreCase("square") || sWord.equalsIgnoreCase("coalesce") || sWord.equalsIgnoreCase("convert")
				|| sWord.equalsIgnoreCase("dateadd") || sWord.equalsIgnoreCase("round") || sWord
					.equalsIgnoreCase("datediff")) && nextWord.equals("(")) {
			isFunc = true;
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.isFunctionName Over");
		return isFunc;
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-11-03 19:50:50)
	 */
	private int isHasWord(String asSqlWords[], String s) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.isHasWord");
		if(asSqlWords == null || asSqlWords.length < 1)
			return -1;
		int pos = -1;
		int i = 0;
		while(i < asSqlWords.length && (!asSqlWords[i].equalsIgnoreCase(s))) {
			i++;
		}
		if(i < asSqlWords.length)
			pos = i;
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.isHasWord Over");
		return pos;
	}

	/**
	 * 转换Convert函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * Convert(datatype,col) -> cast(col as datatype)
	 */

	private void translateFunConvert(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunConvert");
		String sRes = "";
		int iOff = 2;
		String dataType = "", theData = "", theLen = "";
		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		dataType = params[0].trim();

		theData = params[1].trim();
		if(params.length > 2) {
			theLen = "," + params[2].trim();
		}
		TranslateToInformix newTranslateToInformix = new TranslateToInformix();
		try {
			newTranslateToInformix.setSql(theData);

			theData = newTranslateToInformix.getSql();
		} catch(Exception e) {
			e.printStackTrace();
		}
		sRes = "cast(" + theData + " as " + dataType + ")";
		m_sbDestinationSql.append(" " + sRes);
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunConvert Over");
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->power(f,2)
	 */

	private void translateFunDateAdd(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunDateAdd");

		int iOff = 2;

		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		String dateType = params[0].trim();

		String theNumber = params[1].trim();

		String theDate = params[2].trim();

		TranslateToInformix newTranslateToInformix = new TranslateToInformix();
		try {
			newTranslateToInformix.setSql(theDate);

			theDate = newTranslateToInformix.getSql();

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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunDateAdd Over");
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->power(f,2)
	 */

	private void translateFunDateDiff(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunDateDiff");

		int iOff = 2;

		String params[] = getFunParam(asWords, iOff, asWords.length - 1);

		String dateType = params[0].trim();

		String theStart = params[1].trim();

		String theEnd = params[2].trim();

		TranslateToInformix newTranslateToInformix = new TranslateToInformix();
		try {
			newTranslateToInformix.setSql(theStart);
			theStart = newTranslateToInformix.getSql();

			newTranslateToInformix.setSql(theEnd);
			theEnd = newTranslateToInformix.getSql();

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

					theStart = "month(current date)";
				} else {
					theStart = "month(" + theStart + ")";
				}
				if(theEnd.toLowerCase().startsWith("getdate(") || theEnd.toLowerCase().startsWith("getdate (")
						|| theEnd.toLowerCase().startsWith("getdate  (")
						|| theEnd.toLowerCase().startsWith("getdate   (")) {

					theEnd = "month(current date)";
				} else {
					theEnd = "month(" + theEnd + ")";
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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunDateDiff Over");
	}

	/*
	 * round(参数1,参数2,参数3)
	 * 规则:
	 * round(参数1,参数2,参数3)->trunc(参数1,参数2)
	 * 创建日期：(2004-11-30 15:06:37)
	 */
	private void translateFunRound(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunRound");
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

		TranslateToInformix newTranslateToInformix = null;

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
						if(newTranslateToInformix == null) {
							newTranslateToInformix = new TranslateToInformix();
						}

						newTranslateToInformix.setSql(theNumber);

						theNumber = newTranslateToInformix.getSql();
					}
				} else {
					secondCommaIndex = iOff;

					for(int i = firstCommaIndex + 1; i < iOff; i++) {
						theLength += " " + asSqlWords[i];
					}

					if(iOff - (firstCommaIndex + 1) > 1) {
						if(newTranslateToInformix == null) {
							newTranslateToInformix = new TranslateToInformix();
						}

						newTranslateToInformix.setSql(theLength);

						theLength = newTranslateToInformix.getSql();
					}
				}
			}
			iOff++;
		}

		int fromIndex = 0;
		String s = "";

		if(commaCount == 1) {
			for(int i = firstCommaIndex + 1; i < asSqlWords.length - 1; i++) {
				theLength += " " + asSqlWords[i];
			}

			if((asSqlWords.length - 1) - (firstCommaIndex + 1) > 1) {
				if(newTranslateToInformix == null) {
					newTranslateToInformix = new TranslateToInformix();
				}

				newTranslateToInformix.setSql(theLength);

				theLength = newTranslateToInformix.getSql();
			}

			s = " round(" + theNumber + ", " + theLength + ") ";
		} else {
			for(int i = secondCommaIndex + 1; i < asSqlWords.length - 1; i++) {
				theTyle += " " + asSqlWords[i];
			}

			if((asSqlWords.length - 1) - (secondCommaIndex + 1) > 1) {
				if(newTranslateToInformix == null) {
					newTranslateToInformix = new TranslateToInformix();
				}

				newTranslateToInformix.setSql(theTyle);

				theTyle = newTranslateToInformix.getSql();
			}

			int tyle = Integer.valueOf(theTyle.trim()).intValue();

			if(tyle == 0) {
				s = " round(" + theNumber + ", " + theLength + ") ";
			} else {
				s = " trunc(" + theNumber + ", " + theLength + ") ";
			}
		}
		m_sbDestinationSql.append(s);
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2.TranslateToInformix Over");
	}

	/**
	 * 转换Square函数
	 * 参数:
	 * asWords 函数子句
	 * 规则:
	 * square(f)->f*f
	 */

	private void translateFunSquare(String[] asWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunSquare");
		String s = new String();
		int iOff = 2;
		int iLBracket = 1;
		int iRBracket = 0;

		while(iOff < asWords.length) {
			if(asWords[iOff].equals("("))
				iLBracket++;
			if(asWords[iOff].equals(")"))
				iRBracket++;
			if(iLBracket != iRBracket)
				s += asWords[iOff];
			iOff++;
		}
		try {
			translateSelect(parseSql(s + "*" + s));
		} catch(Exception e) {
			System.out.println(e);
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunSquare Over");
	}

	/**
	 * 此处插入方法说明。
	 * 创建日期：(2002-4-3 19:50:50)
	 */
	private void translateIFExists(String[] asSqlWords) {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateIFExists");
		int index = -1;
		String result = "";
		if((index = isHasWord(asSqlWords, "drop")) > -1 && asSqlWords[index + 1].equalsIgnoreCase("table")) {
			result = dropTable(asSqlWords, index);
		}
		m_sbDestinationSql.append(result);
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateIFExists Over");
	}

	/**
	 * 处理连接更新
	 * 创建日期：(2004-11-05 8:38:35)
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
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateJoinUpdate");

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
		boolean bExist = false;
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

				sSql += " set";
				//vSql.addElement("set");
				iOffSet++;

				int leftCount = 0; //左括号数
				int rightCount = 0; //右括号数

				//若当前字符串不是“from”，则循环
				while(iOffSet < asWords.length && !asWords[iOffSet].equalsIgnoreCase("from")) {

					//把表的别名替换为表的名字
					if(sTableAlias != null && sTableAlias.trim().length() > 0
							&& asWords[iOffSet].startsWith(sTableAlias + ".")) {
						int i = asWords[iOffSet].indexOf(".");
						asWords[iOffSet] = sTableName + asWords[iOffSet].substring(i);
					}

					if(asWords[iOffSet].equalsIgnoreCase("(")) //左括号数
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
						sSql += "(" + sLeftField + ")=((select " + sRightField; //将暂时保存起来的语句，拼接到sql语句
					else if(iJoinCount == 1)
						sSql += "" + sLeftField + "=((select " + sRightField;
				} else {
					if(iJoinCount > 1)
						sSql += setsql + ",(" + sLeftField + ")=((select " + sRightField;
					else if(iJoinCount == 1)
						sSql += setsql + "," + sLeftField + "=((select " + sRightField;
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

			}

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
					//把表的别名替换为表的名字
					if(sTableAlias != null && sTableAlias.trim().length() > 0
							&& asWords[iOffSet].startsWith(sTableAlias + ".")) {
						int i = asWords[iOffSet].indexOf(".");
						asWords[iOffSet] = sTableName + asWords[iOffSet].substring(i);
					}

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
			} else if(iOffSet < asWords.length) {
				if(asWords[iOffSet].equalsIgnoreCase(sTableName) || asWords[iOffSet].equalsIgnoreCase(sTableAlias)) {
					sSql += " " + sTableName;
				} else {
					sSql += " " + asWords[iOffSet];
				}

				//sSql += " " + asWords[iOffSet];
			}
		} //while (iOffSet < asWords.length - 1)结束处

		m_Sql += "))";
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

		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateJoinUpdate Over");
		return asWords;
	}

	private int[][] err_inf = { { -204, 208 }, //表或视图不存在
			{ -104, 2715 }, //函数不存在
			{ -206, 207 }, //无效的列名
			{ -421, 205 }, //使用union的语句和目标列表应具有相同数目的表达式
			{ -408, 213 }, //插入数据和表数据类型不一致
			{ -803, 2627 }, //不能插入相同主键的记录
			{ -407, 515 }, //列值不能为空
			{ -433, 8152 } //插入的值对于列过大
	};

	//函数对照表,列出sqlServer函数与Informix函数的对应关系,
	private String[][] fun_inf = { { "len", "length" }, { "substring", "substr" }, { "lower", "lower" },
			{ "upper", "upper" }, { "isnull", "nvl" }, { "rtrim", "trim" }, { "ltrim", "trim" }

	};

	/**
	 * TransDB2 构造子注释。
	 */
	public TranslateToInformix() {
		super(INFORMIX);
		m_apsFunList = fun_inf;
		m_apiErrorList = err_inf;
		log.debug("nc.bs.mw.sqltrans.TranslateToDB2 Over");
	}

	/**
	 * 转换 coalesce 函数
	 * 参数:
	 * asSqlWords coalesce函数子句
	 * 规则:
	 * coalesce(exp1,exp2,exp3,exp4)->nvl(nvl(nvl(exp1,exp2),exp3),exp4)
	 */

	private void translateFunCoalesce(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunCoalesce");

		int iOffSet = 0;
		int iLBracket = 0;
		int iRBracket = 0;
		//标识每个参数的起始位置
		int iStart = 0;
		java.util.Vector v = new java.util.Vector();
		String sWord = asSqlWords[iOffSet];

		TranslateToInformix newTranslateToInformix = new TranslateToInformix();

		//若当前字符串为“coalesce”，且下一个字符串为左括号
		if(sWord.equalsIgnoreCase("coalesce") && asSqlWords[iOffSet + 1].equals("(")) {
			m_bFinded = true;
			m_sbDestinationSql.append(" ");
			//标识加1
			iOffSet++;
			//起始位置加2
			iStart += 2;
			//若标志小于字符串集长度，则循环
			while(iOffSet < asSqlWords.length) {
				iOffSet++;
				//若当前字符串为左括号，则左标志加1
				if(asSqlWords[iOffSet].equals("("))
					iLBracket++;
				//若当前字符串为右括号，则右标志加1
				if(asSqlWords[iOffSet].equals(")"))
					iRBracket++;
				//若左右标识相等，且当前字符串为逗号
				if(iLBracket == iRBracket && asSqlWords[iOffSet].equals(",")) {
					String str = new String();
					//保存从开始位置到标识位置的字符串
					for(int i = iStart; i < iOffSet; i++) {
						str += " " + asSqlWords[i];
					}

					if(str.indexOf("(") >= 0) {
						newTranslateToInformix.setSql(str);
						str = newTranslateToInformix.getSql();
					}
					v.addElement(str);
					//开始位置为标识位置加1
					iStart = iOffSet + 1;
				}
				//若左右标识相等，且下一个字符串为右括号
				if((iLBracket == iRBracket) && asSqlWords[iOffSet + 1].equals(")")) {
					String str = new String();
					//保存从开始位置到标识位置加1的字符串
					for(int i = iStart; i < iOffSet + 1; i++) {
						str += " " + asSqlWords[i];
					}

					if(str.indexOf("(") >= 0) {
						newTranslateToInformix.setSql(str);
						str = newTranslateToInformix.getSql();
					}

					v.addElement(str);
					String s = new String();

					//增加nvl
					for(int i = 1; i < v.size(); i++)
						m_sbDestinationSql.append("nvl(");
					m_sbDestinationSql.append(v.elementAt(0));

					//格式整理
					for(int j = 1; j < v.size(); j++) {
						s += " " + ",";
						s += " " + v.elementAt(j);
						s += " " + ")";
					}
					m_sbDestinationSql.append(s);
					//translateSelect(parseSql(s));
					break;
				}
			}
		}
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateFunCoalesce Over");
	}

	/**
	 * 转换Update语句
	 */
	private StringBuffer translateUpdate(String[] asSqlWords) throws Exception {
		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateUpdateII");

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

		log.debug("nc.bs.mw.sqltrans.TranslateToInformix.translateUpdateII Over");
		return translateSelect(asWords);

	}
}