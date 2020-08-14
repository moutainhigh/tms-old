package org.nw.dao.sqltranslator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 模块: TranslateToSybase.java
 * 描述: 将SqlServer语句翻译到Sybase语句
 */

public class TranslateToSybase extends TranslatorObject {
	private static final Log log = LogFactory.getLog(TranslateToSybase.class);

	/**
	 * TranslateToSybase 构造子注释。
	 */
	public TranslateToSybase() {
		super(SYBASE);
		log.debug("nc.bs.mw.sqltrans.TranslateToSybase Over");
	}
}