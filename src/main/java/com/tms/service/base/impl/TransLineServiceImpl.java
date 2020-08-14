package com.tms.service.base.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import org.nw.utils.RefUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.RefVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.springframework.stereotype.Service;

import com.tms.constants.TabcodeConst;
import com.tms.constants.TransLineConst;
import com.tms.constants.DataDictConst.ADDR_TYPE;
import com.tms.service.base.TransLineService;
import com.tms.vo.attach.AttachmentVO;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.LineNodeVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.cm.ReceiveDetailVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.web.ref.AddressAndAreaDefaultRefModel;

/**
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:56:33
 */
@Service
public class TransLineServiceImpl extends AbsBaseDataServiceImpl implements TransLineService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, TransLineVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, TransLineVO.PK_TRANS_LINE);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, LineNodeVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, LineNodeVO.PK_TRANS_LINE);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_line_node");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_line_node");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if(fieldVO.getItemkey().equals(TransLineVO.START_ADDR)) {
					// 起始地
					fieldVO.setUserdefine3("addr_type:${uft.Utils.getField('start_addr_type').getValue()}");
				} else if(fieldVO.getItemkey().equals(TransLineVO.END_ADDR)) {
					// 目的地
					fieldVO.setUserdefine3("addr_type:${uft.Utils.getField('end_addr_type').getValue()}");
				} else if(fieldVO.getItemkey().equals(TransLineVO.START_ADDR_TYPE)) {
					fieldVO.setUserdefine1("afterChangeStartAddrType(field,value,originalValue)");
				} else if(fieldVO.getItemkey().equals(TransLineVO.END_ADDR_TYPE)) {
					fieldVO.setUserdefine1("afterChangeEndAddrType(field,value,originalValue)");
				}
			}

		}
		return templetVO;
	}

	protected void convertRefOject(Map<String, Object> resultMap, Map<String, BillTempletBVO> refMap) {
		// 对“城市地址档案(并集)”需要做特殊处理
		// 返回的值需要对参照值多特殊处理
		for(String key : resultMap.keySet()) {
			BillTempletBVO fieldVO = refMap.get(key);
			if(fieldVO != null) {
				// 这个是参照对象,返回已经构建的参照对象
				BaseRefModel refModel = (BaseRefModel) fieldVO.genRefItem(false);
				// 调用该参照对象的getByPk方法，返回VO值，并转化成参照VO，因为需要3个必须值，id,code,name
				if(resultMap.get(key) != null) {
					Map<String, Object> voMap = null;
					if(refModel instanceof AddressAndAreaDefaultRefModel) {
						AddressAndAreaDefaultRefModel aaaRefModel = (AddressAndAreaDefaultRefModel) refModel;
						String addr_type = "";
						if("start_addr".equals(key)) {
							addr_type = resultMap.get("start_addr_type").toString();
						} else if("end_addr".equals(key)) {
							addr_type = resultMap.get("end_addr_type").toString();
						}
						voMap = (Map<String, Object>) aaaRefModel.getByPk(addr_type, resultMap.get(key).toString());
					} else {
						// 可能本身就为null值
						voMap = refModel.getByPk(resultMap.get(key).toString());
					}
					if(voMap != null && voMap.get("data") != null) {
						// 可能返回的值也是为null，如参照压根就没有实现
						RefVO refVO = null;
						// 有些getByPk是返回SuperVO，也可能返回Map
						if(voMap.get("data") instanceof SuperVO) {
							SuperVO superVO = (SuperVO) voMap.get("data");// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
							refVO = RefUtils.convert(refModel, superVO);
						} else {
							Map map = (Map) voMap.get("data");// 这个data属于关键字，在AbstractBaseController.genAjaxResponse中定义
							// 若参照为管理档案，显示值为基本档案的name和code，则在参照中不再关联管理档案表取管理档案的pk值，直接在这边取值赋给参照的pkfieldcode
							// wpp 2012-04-11
							map.put(refModel.getPkFieldCode(), resultMap.get(key));
							refVO = RefUtils.convert(refModel, map);
						}
						// 将返回的值重新设置到该resultMap中
						resultMap.put(key, refVO);
					} else {
						// 查询不到记录，可能该档案已经被删除，这个数据也失去意义了
						resultMap.put(key, null);
					}
				}
			}
		}
	}

	public String getCodeFieldCode() {
		return TransLineVO.LINE_CODE;
	}

	public TransLineVO getByCode(String code) {
		return dao.queryByCondition(TransLineVO.class,
				"isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and line_code=?", code);
	}

	public TransLineVO getByObject(TransLineVO lineVO) {
		StringBuffer cond = new StringBuffer("1=1");
		if(StringUtils.isNotBlank(lineVO.getStart_addr())) {
			cond.append(" and start_addr='");
			cond.append(lineVO.getStart_addr());
			cond.append("'");
		}
		if(StringUtils.isNotBlank(lineVO.getEnd_addr())) {
			cond.append(" and end_addr='");
			cond.append(lineVO.getEnd_addr());
			cond.append("'");
		}
		if(lineVO.getLine_type() != null) {
			cond.append(" and line_type=");
			cond.append(lineVO.getLine_type());
		}
		if(StringUtils.isNotBlank(lineVO.getPk_trans_type())) {
			cond.append(" and pk_trans_type='");
			cond.append(lineVO.getPk_trans_type());
			cond.append("'");
		}
		return dao.queryByCondition(TransLineVO.class, cond.toString());
	}
	
	protected void processCopyVO(AggregatedValueObject copyVO, ParamVO paramVO) {
		super.processCopyVO(copyVO, paramVO);
		CircularlyAccessibleValueObject parentVO = copyVO.getParentVO();
		parentVO.setAttributeValue("line_code", null);
		parentVO.setAttributeValue("line_name", null); 
	}
}
