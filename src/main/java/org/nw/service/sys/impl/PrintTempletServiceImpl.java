package org.nw.service.sys.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.vo.PrintTempletVO;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.FunService;
import org.nw.service.sys.PrintTempletService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.FunVO;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

/**
 * 打印模板操作
 * 
 * @author xuqc
 * @date 2012-12-1 下午02:52:51
 */
@Service
public class PrintTempletServiceImpl extends AbsToftServiceImpl implements PrintTempletService {
	@Autowired
	private FunService funService;

	AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, PrintTempletVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, PrintTempletVO.PK_PRINT_TEMPLET);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	protected void processBeforeDelete(AggregatedValueObject billVO) {
		super.processBeforeDelete(billVO);
		PrintTempletVO ptVO = (PrintTempletVO) billVO.getParentVO();
		String currentCorp = WebUtils.getLoginInfo().getPk_corp();
		if((!currentCorp.equals(Constants.SYSTEM_CODE)) && ptVO.getPk_corp().equals(Constants.SYSTEM_CODE)) {
			throw new BusiException("子公司账户不能删除集团的模板！");
		}
	}

	/**
	 * 只能查询本公司的模板和集团的，不能删除集团的模板
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = parseCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and ";
			fCond += cond;
		}

		if(!paramVO.isBody()) {
			String corpCond = CorpHelper.getCurrentCorpWithGroup();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and " + corpCond;
			}
		}
		return fCond;
	}

	private List<TreeVO> convertToTreeVO(List<FunVO> funVOs) {
		if(funVOs == null || funVOs.size() == 0) {
			return null;
		}
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		for(FunVO funVO : funVOs) {
			TreeVO treeNode = new TreeVO();
			treeNode.setId(funVO.getFun_code());
			treeNode.setCode(funVO.getFun_code());
			treeNode.setText(funVO.getFun_code() + " " + funVO.getFun_name());
			treeNode.setLeaf(true);
			treeVOs.add(treeNode);
		}
		return treeVOs;
	}

	public List<TreeVO> getFunCodeTree() {
		List<FunVO> funVOs = funService.getPowerFunVO(false);
		for(int i = 0; i < funVOs.size(); i++) {
			if(StringUtils.isBlank(funVOs.get(i).getBill_type())) {
				funVOs.remove(i);
				i--;
			}
		}
		return convertToTreeVO(funVOs);
	}

	/**
	 * 因为模板的数据比较大，使用物理删除
	 */
	protected boolean isLogicalDelete() {
		return false;
	}

	public String[] getLoadDataFields() {
		return new String[] { "pk_print_templet", "dr", "ts", "nodecode", "file_name", "memo", "create_user",
				"create_time", "file_size", "pk_corp", "vtemplatename", "vtemplatecode", "head_formula", "body_formula" };
	}

	public void uploadTemplet(final PrintTempletVO attachVO, final InputStream in) {
		NWDao dao = NWDao.getInstance();
		if(attachVO.getStatus() == VOStatus.NEW) {
			// 新增一个模板
			String sql = "insert into nw_print_templet(pk_print_templet, dr,ts,nodecode,file_name,file_size,  memo,pk_corp,create_user,create_time,vtemplatecode,vtemplatename,head_formula,body_formula,contentdata) "
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			dao.getJdbcTemplate().execute(sql, new PreparedStatementCallback<PrintTempletVO>() {
				public PrintTempletVO doInPreparedStatement(PreparedStatement ps) throws SQLException {
					ps.setObject(1, attachVO.getPk_print_templet());
					ps.setObject(2, attachVO.getDr());
					ps.setObject(3, attachVO.getTs().toString());
					ps.setObject(4, attachVO.getNodecode());
					ps.setObject(5, attachVO.getFile_name());
					ps.setObject(6, attachVO.getFile_size());
					ps.setObject(7, attachVO.getMemo());
					ps.setObject(8, attachVO.getPk_corp());
					ps.setObject(9, attachVO.getCreate_user());
					ps.setObject(10, attachVO.getCreate_time().toString());
					ps.setObject(11, attachVO.getVtemplatecode());
					ps.setObject(12, attachVO.getVtemplatename());
					ps.setObject(13, attachVO.getHead_formula());
					ps.setObject(14, attachVO.getBody_formula());
					ps.setBinaryStream(15, in, attachVO.getFile_size().intValue());
					ps.execute();
					return attachVO;
				}
			});
		} else if(attachVO.getStatus() == VOStatus.UPDATED) {
			String sql = "update nw_print_templet set file_name=?,file_size=?,contentdata=? where pk_print_templet=?";
			dao.getJdbcTemplate().execute(sql, new PreparedStatementCallback<PrintTempletVO>() {
				public PrintTempletVO doInPreparedStatement(PreparedStatement ps) throws SQLException {
					ps.setObject(1, attachVO.getFile_name());
					ps.setObject(2, attachVO.getFile_size());
					ps.setBinaryStream(3, in, attachVO.getFile_size().intValue());
					ps.setObject(4, attachVO.getPk_print_templet());
					ps.execute();
					return attachVO;
				}
			});
		}
	}

	public PrintTempletVO getByPrimaryKey(String primaryKey) {
		String sql = "select pk_print_templet, dr,ts,nodecode,file_name,file_size,memo,head_formula,body_formula,pk_corp,create_user,create_time "
				+ "from nw_print_templet WITH(NOLOCK) where pk_print_templet=?";
		return NWDao.getInstance().queryForObject(sql, PrintTempletVO.class, primaryKey);
	}

	/**
	 * 下载打印模板文件
	 */
	@SuppressWarnings({ "rawtypes" })
	public void downloadTemplet(String pk_print_templet, OutputStream out) throws Exception {
		String sql = "select contentdata from nw_print_templet WITH(NOLOCK) where pk_print_templet=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, pk_print_templet);
		if(retMap == null) {
			logger.warn("您下载的文件已经不存在,pk_print_templet=" + pk_print_templet);
			throw new BusiException("您下载的文件已经不存在，请刷新页面！");
		}
		Object contentdata = retMap.get("contentdata");
		if(contentdata != null) {
			InputStream in = (InputStream) contentdata;
			IOUtils.copy(in, out);
		}
	}
}
