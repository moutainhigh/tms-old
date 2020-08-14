package org.nw.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nw.basic.util.DateUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.service.sys.CodeRuleService;
import org.nw.service.sys.impl.CodeRuleServiceImpl;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.CodeRuleVO;
import org.nw.vo.sys.CodenoVO;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 编码工具类，根据规则生成编码
 * 
 * @author xuqc
 * @date 2012-7-28 上午10:26:48
 */
public class CodenoHelper {

	static Logger logger = Logger.getLogger(CodenoHelper.class);
	private static final byte[] lock = new byte[0];

	/**
	 * 根据节点号所定义的编码生成规则生成编码
	 * 
	 * @param fun_code
	 * @return
	 */
	public static String generateCode(String fun_code) {
		if(StringUtils.isBlank(fun_code)) {
			return null;
		}
		CodeRuleService ruleService = new CodeRuleServiceImpl();
		CodeRuleVO ruleVO = ruleService.getByFunCode(fun_code);
		if(ruleVO == null) {
			// 没有定义规则
			return null;
		}
		StringBuffer codeno = new StringBuffer();
		if(StringUtils.isNotBlank(ruleVO.getPrefix())) {
			codeno.append(ruleVO.getPrefix());
		}
		if(ruleVO.getIs_year().booleanValue()) {
			codeno.append(DateUtils.getYear());
		}
		if(ruleVO.getIs_month().booleanValue()) {
			codeno.append(DateUtils.getMonthStr());
		}
		if(ruleVO.getIs_day().booleanValue()) {
			codeno.append(DateUtils.getDayStr());
		}
		if(ruleVO.getIs_serial_no().booleanValue()) {
			// 流水号位数
			codeno.append(getSerialno(fun_code, ruleVO.getSerial_no_count()));
		}
		return codeno.toString();
	}

	/**
	 * 返回可用的流水号，如果当前的单据类型已经存在流水号，则返回流水号+1，否则返回1， 返回的数值需要根据位数进行补齐，如0001
	 * 
	 * @param fun_code
	 * @param serial_no_count
	 *            //流水号的位数
	 * @return
	 */
	private static String getSerialno(String fun_code, int serial_no_count) {
		synchronized(lock) {
			logger.info("同步块，返回可用的流水号...");
			NWDao dao = NWDao.getInstance();
			// 不要根据公司进行查询，否则会出现相同单据号的情况
			String where = "isnull(dr,0)=0 and fun_code=? order by ts desc";
			CodenoVO[] codenoVOs = dao.queryForSuperVOArrayByCondition(CodenoVO.class, where, fun_code);
			CodenoVO codenoVO = null;
			int serialno; // 流水号
			if(codenoVOs == null || codenoVOs.length == 0) {
				// 此时还没有流水号，
				serialno = 1;
				codenoVO = new CodenoVO();
				codenoVO.setFun_code(fun_code);
				codenoVO.setSerial_no(serialno);
				// codenoVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				codenoVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(codenoVO);
			} else {
				codenoVO = codenoVOs[0];
				serialno = codenoVO.getSerial_no() + 1;
				codenoVO.setSerial_no(serialno);
				// codenoVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
				codenoVO.setStatus(VOStatus.UPDATED);
			}
			StringBuffer sb = new StringBuffer(serialno + "");
			int length = sb.length();
			// 如果位数不够，补0
			for(int i = 0; i < (serial_no_count - length); i++) {
				sb.insert(0, "0");
			}

			final String updateSql = DaoHelper.getUpdateSQL(dao, codenoVO, true);
			PlatformTransactionManager ptm = new DataSourceTransactionManager(NWDao.getInstance().getDataSource());
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			// DefaultTransactionDefinition def = new
			// DefaultTransactionDefinition(
			// TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			TransactionStatus status = ptm.getTransaction(def);
			try {
				NWDao.getInstance().update(updateSql);
			} catch(Throwable e) {
				ptm.rollback(status);
				throw new RuntimeException(e);
			} finally {
				if(!status.isCompleted()) {
					ptm.commit(status);
				}
			}
			logger.info("同步块结束，返回可用的流水号：" + sb.toString());
			return sb.toString();
		}
	}
}
