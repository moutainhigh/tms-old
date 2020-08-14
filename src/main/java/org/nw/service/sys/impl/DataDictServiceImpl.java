package org.nw.service.sys.impl;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.DataDictService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.DataDictBVO;
import org.nw.vo.sys.DataDictVO;
import org.nw.web.utils.WebUtils;
import org.springframework.stereotype.Service;

/**
 * 
 * @author xuqc
 * @date 2012-7-15 下午05:32:54
 */
@Service
public class DataDictServiceImpl extends AbsToftServiceImpl implements DataDictService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, DataDictVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, DataDictVO.PK_DATA_DICT);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, DataDictBVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, DataDictBVO.PK_DATA_DICT);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "nw_data_dict_b");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "nw_data_dict_b");
			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public AggregatedValueObject getAggVOByDatatypeCode(String datatype_code) {
		AggregatedValueObject billVO = new HYBillVO();
		DataDictVO ddVO = getByDatatypeCode(datatype_code);
		if(ddVO == null) {
			logger.warn("数据类型编码不存在，datatype_code:" + datatype_code);
			return null;
		}
		billVO.setParentVO(ddVO);
		String where = "pk_data_dict=? order by display_order";
		SuperVO[] superVOArr = NWDao.getInstance().queryForSuperVOArrayByCondition(DataDictBVO.class, where,
				ddVO.getPk_data_dict());
		billVO.setChildrenVO(superVOArr);
		return billVO;
	}

	public DataDictVO getByDatatypeCode(String datatype_code) {
		DataDictVO vo = NWDao.getInstance().queryByCondition(DataDictVO.class,
				"locked_flag='N' and datatype_code=? and pk_corp=?", datatype_code,
				WebUtils.getLoginInfo().getPk_corp());
		if(vo == null) {
			logger.info("当前登录的公司没有在数据字典中定义该数据,数据类型编码：" + datatype_code + ",将从集团中读取！");
			vo = NWDao.getInstance().queryByCondition(DataDictVO.class,
					"locked_flag='N' and datatype_code=? and pk_corp=?", datatype_code, Constants.SYSTEM_CODE);
		}
		if(vo == null) {
			logger.warn("没有在数据字典中定义该数据,数据类型编码：" + datatype_code);
		}
		return vo;
	}

	/**
	 * 可以看到当前公司及集团的数据
	 */
	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and " + cond;
		}
		if(!paramVO.isBody()) {
			String corpCond = CorpHelper.getCurrentCorpWithGroup();
			if(StringUtils.isNotBlank(corpCond)) {
				fCond += " and " + corpCond;
			}
		}
		return fCond;
	}

	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(paramVO.isBody()) {
			// 表体的查询
			orderBy = " order by display_order";
		} else {
			if(StringUtils.isBlank(orderBy)) {
				orderBy = " order by ts";
			}
		}
		return orderBy;
	}
}
