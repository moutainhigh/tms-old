package org.nw.service.sys.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.PaginationVO;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.TPL_STYLE_NAME;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.BtnTempletDistService;
import org.nw.vo.HYBillVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.BtnTempletDistVO;
import org.nw.vo.sys.FunVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

import com.tms.constants.FunConst;

/**
 * 按钮模板分配处理类
 * 
 * @author xuqc
 * @date 2012-7-8 上午11:38:52
 */
@Service
public class BtnTempletDistServiceImpl extends AbsToftServiceImpl implements BtnTempletDistService {

	public AggregatedValueObject getBillInfo() {
		AggregatedValueObject billInfo = new HYBillVO();
		VOTableVO vo = new VOTableVO();
		vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
		vo.setAttributeValue(VOTableVO.HEADITEMVO, BtnTempletDistVO.class.getName());
		vo.setAttributeValue(VOTableVO.PKFIELD, BtnTempletDistVO.PK_BTN_TEMPLET_DIST);
		billInfo.setParentVO(vo);
		return billInfo;
	}

	public List<BtnTempletDistVO> loadTempletDist(String pk_fun) {
		String sql = "select * from nw_btn_templet_dist WITH(NOLOCK) where isnull(dr,0)=0 and pk_fun=?";
		// 已分配的模板
		List<BtnTempletDistVO> distVOs = NWDao.getInstance().queryForList(sql, BtnTempletDistVO.class, pk_fun);
		return distVOs;
	}

	/**
	 */
	@SuppressWarnings("rawtypes")
	public PaginationVO loadTemplet(String pk_fun) {
		FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, "pk_fun=?", pk_fun);
		// 读取可分配的单据模板
		String sql = "select pk_billtemplet as pk_templet,pk_billtypecode as templet_code,bill_templetcaption as templet_name,"
				+ UiConstants.TPL_STYLE.BILL.intValue()
				+ " as tempstyle,'"
				+ TPL_STYLE_NAME.BILL.toString()
				+ "' as tempstyle_name " + "from nw_billtemplet WITH(NOLOCK) where isnull(dr,0)=0 and nodecode=? ";
		List<HashMap> list = (List<HashMap>) NWDao.getInstance().queryForList(sql, HashMap.class, funVO.getFun_code());
		PaginationVO paginationVO = new PaginationVO();
		paginationVO.setItems(list);
		return paginationVO;
	}

	public int saveTempletDist(List<BtnTempletDistVO> distVOs, String pk_fun) {
		if(StringUtils.isBlank(pk_fun)) {
			return 0;
		}
		FunVO funVO = NWDao.getInstance().queryByCondition(FunVO.class, "pk_fun=?", pk_fun);
		if(funVO == null) {
			throw new BusiException("菜单已经被删除！");
		}
		if(funVO.getFun_property().intValue() != FunConst.POWERFUL_BUTTON
				&& funVO.getFun_property().intValue() != FunConst.POWERLESS_BUTTON) {
			throw new BusiException("请选择按钮节点！");
		}

		// 先删除旧的数据
		BtnTempletDistVO[] toBeDel = dao.queryForSuperVOArrayByCondition(BtnTempletDistVO.class, "pk_fun=?", pk_fun);
		dao.delete(toBeDel, false);// 这里使用物理删除
		if(distVOs == null || distVOs.size() == 0) {
			return 0;
		}
		for(BtnTempletDistVO distVO : distVOs) {
			distVO.setCreate_time(new UFDateTime(new Date()));
			distVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			distVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(distVO);
		}
		dao.saveOrUpdate(distVOs.toArray(new BtnTempletDistVO[distVOs.size()]));
		return distVOs.size();
	}

}
