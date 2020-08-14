package com.tms.web.kpi;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.redis.RedisDao;
import org.nw.service.IToftService;
import org.nw.utils.CorpHelper;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.CorpVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tms.service.kpi.LimitService;
import com.tms.vo.base.GoodsVO;
import com.tms.vo.kpi.LimitVO;

@Controller
@RequestMapping(value = "/kpi/limit")
public class LimitController extends AbsToftController {
	
	@Autowired
	private LimitService  limitService;

	@Override
	public IToftService getService() {
		return limitService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		String codeField = this.getService().getCodeFieldCode();
		if(parentVO != null) {
			if(StringUtils.isNotBlank(codeField)) {
				Object code = parentVO.getAttributeValue(this.getService().getCodeFieldCode());
				try {
					if(code != null && StringUtils.isNotBlank(code.toString())) {
						//code不为空时，需要检查code唯一性
						LimitVO limitVO = (LimitVO) this.getService().getByCode(code.toString());
						if(StringUtils.isBlank(parentVO.getPrimaryKey())) {							
							// 新增的情况
							if(limitVO != null) {
								throw new RuntimeException("编码已经存在！");
							}
						} else {
							// 修改的情况
							if(limitVO != null) {
								if(!parentVO.getPrimaryKey().equals(limitVO.getPrimaryKey())) {
									throw new RuntimeException("编码已经存在！");
								}
							}
						}
					}
					//检查数据唯一性
					LimitVO newLimitVO = (LimitVO) parentVO;
					LimitVO limitVO = limitService.getLimitVObycond(newLimitVO.getLimit_type(), 
							newLimitVO.getMatter(), newLimitVO.getExp_type(), newLimitVO.getPk_carrier(),
							newLimitVO.getPk_customer(), newLimitVO.getIf_urgent(), newLimitVO.getItem_code(), 
							newLimitVO.getGoods_type(), newLimitVO.getPk_address(), newLimitVO.getPk_corp());
					if(StringUtils.isBlank(parentVO.getPrimaryKey())){
						// 新增的情况
						if(limitVO != null) {
							throw new RuntimeException("已存在相同的数据！");
						}
					}else{
						// 修改的情况
						if(limitVO != null) {
							if(!parentVO.getPrimaryKey().equals(limitVO.getPrimaryKey())) {
								throw new RuntimeException("已存在相同的数据！！");
							}
						}
					}
				} catch (Exception e) {
					throw new RuntimeException("已存在相同的数据！！");
				}
			}
			// 子公司只能修改本公司的数据
			if(!WebUtils.getLoginInfo().getPk_corp().equals(Constants.SYSTEM_CODE)) {
				// 不是集团登陆的
				//List<CorpVO> vos = CorpHelper.getCurrentCorpVOsWithChildren();// 当前公司和子公司
				List<CorpVO> vos = RedisDao.getInstance().getCurrentCorpVOsWithChildren(WebUtils.getLoginInfo().getPk_corp());// 当前公司和子公司
				Object pk_corp = parentVO.getAttributeValue("pk_corp");
				if(pk_corp != null) {
					if(!pk_corp.toString().equals("@@@@")) {
						boolean exist = false;
						for(CorpVO vo : vos) {
							if(pk_corp.toString().equals(vo.getPk_corp())) {
								exist = true;// 可编辑的范围
								break;
							}
						}
						if(!exist) {
							throw new RuntimeException("您不能修改父级公司的数据");
						}
					}
				}
			}
		}
	}
}
