package org.nw.web.sys;

import org.apache.commons.lang.StringUtils;
import org.nw.service.sys.PsndocService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.BusinessException;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.sys.PsndocVO;
import org.nw.web.AbsToftController;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 用户操作类
 * 
 * @author xuqc
 * @date 2012-6-10 下午03:38:27
 */
@Controller
@RequestMapping(value = "/psndoc")
public class PsndocController extends AbsToftController {

	@Autowired
	private PsndocService psndocService;

	public PsndocService getService() {
		// 这里事务使用spring自动管理，service必须从springbean管理器中取得
		return psndocService;
	}

	protected void checkBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		CircularlyAccessibleValueObject parentVO = billVO.getParentVO();
		Object psncode = parentVO.getAttributeValue("psncode");
		if(psncode != null && StringUtils.isNotBlank(psncode.toString())) {
			try {
				PsndocVO psndocVO = psndocService.getByPsncode(psncode.toString());
				if(StringUtils.isBlank(parentVO.getPrimaryKey())) {
					// 新增的情况
					if(psndocVO != null) {
						if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
							throw new RuntimeException("编码已经存在！");
						}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
							throw new RuntimeException("Code already exists！");
						}
						throw new RuntimeException("编码已经存在！");
					}
				} else {
					// 修改的情况
					if(psndocVO != null) {
						if(!parentVO.getPrimaryKey().equals(psndocVO.getPk_psndoc())) {
							if(WebUtils.getLoginInfo() == null || WebUtils.getLoginInfo().getLanguage().equals("zh_CN")){
								throw new RuntimeException("编码已经存在！");
							}else if(WebUtils.getLoginInfo().getLanguage().equals("en_US")){
								throw new RuntimeException("Code already exists！");
							}
							throw new RuntimeException("编码已经存在！");
						}
					}
				}
			} catch(BusinessException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getUploadField() {
		return "photo";
	}
}
