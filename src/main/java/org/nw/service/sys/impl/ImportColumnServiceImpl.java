package org.nw.service.sys.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.ImportColumnService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.ImportColumnVO;
import org.nw.vo.sys.ImportConfigBVO;
import org.nw.vo.sys.ImportConfigVO;
import org.springframework.stereotype.Service;

import com.tms.constants.TabcodeConst;

/**
 * 导入字段操作类
 * 
 * @author xuqc
 * 
 */
@Service
public class ImportColumnServiceImpl extends AbsToftServiceImpl implements ImportColumnService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, ImportConfigVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, ImportConfigVO.PK_IMPORT_CONFIG);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ImportConfigBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ImportConfigBVO.PK_IMPORT_CONFIG);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_import_config_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_import_config_b");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(paramVO.isBody()) {
			// 如果是表体的查询
			if(StringUtils.isBlank(orderBy)) {
				orderBy = " order by display_order";
			}
		}
		return orderBy;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("templet_fun_code")) {
					fieldVO.setUserdefine3("selected_fun_code:${funTree.getSelectedNode().attributes['code']}");
				}
			}
		}
		return templetVO;
	}
	

	/**
	 * 多次引用该比较类，使用内部类处理
	 * 
	 * @author xuqc
	 */
	class ImportConfigBComparator implements Comparator<ImportConfigBVO> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(ImportConfigBVO fvo1, ImportConfigBVO fvo2) {
			final Integer x1 = fvo1.getDisplay_order();
			final Integer x2 = fvo2.getDisplay_order();
			if(x1 == null) {
				return 0;
			}
			return x1.compareTo(x2);
		}
	}

	public AggregatedValueObject queryBillVO(ParamVO paramVO) {
		AggregatedValueObject aggVO = super.queryBillVO(paramVO);
		if(aggVO != null) {
			CircularlyAccessibleValueObject[] childVOs = aggVO.getChildrenVO();
			if(childVOs != null && childVOs.length > 0) {
				Arrays.sort((ImportConfigBVO[]) childVOs, new ImportConfigBComparator());
			}
		}
		return aggVO;
	}

	public void copyColumn(String pk_fun, String pk_corp, String dest_pk_corp) {
		if(StringUtils.isBlank(pk_fun) || StringUtils.isBlank(dest_pk_corp)) {
			return;
		}
		ImportColumnVO[] vos = null;
		if(StringUtils.isBlank(pk_corp)) {
			vos = NWDao.getInstance().queryForSuperVOArrayByCondition(ImportColumnVO.class,
					"isnull(dr,0)=0 and fun_code=? and (pk_corp is null || pk_corp=?)", pk_fun, Constants.SYSTEM_CODE);
		} else {
			vos = NWDao.getInstance().queryForSuperVOArrayByCondition(ImportColumnVO.class,
					"isnull(dr,0)=0 and pk_fun=? and pk_corp=?", pk_fun, pk_corp);
		}
		if(vos == null || vos.length == 0) {
			throw new BusiException("数据已经被删除，无法完成复制！");
		}
		for(ImportColumnVO vo : vos) {
			vo.setPk_import_column(null);
			vo.setTs(new UFDateTime(new Date()));
			vo.setPk_corp(dest_pk_corp);
			vo.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(vo);
			NWDao.getInstance().saveOrUpdate(vo);
		}
	}
}
