package org.nw.exp;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.service.IToftService;
import org.nw.vo.pub.SuperVO;

/**
 * 导入时的编码验证，一个默认的实现，如果还需要进行其他字段的验证，那么可以继承该类
 * 
 * @author xuqc
 * @date 2013-8-23 下午09:25:53
 */
public class CodeImportChecker implements ImportChecker {

	IToftService service;

	public void setService(IToftService service) {
		this.service = service;
	}

	public String check(Object value, BillTempletBVO fieldVO, int rowNum) {
		if(value == null || StringUtils.isBlank(value.toString())) {
			return "编码不能为空！";
		}
		if(this.service != null) {
			if(fieldVO.getItemkey().equals(this.service.getCodeFieldCode())) {
				// 编码进行唯一性检查
				SuperVO superVO = this.service.getByCode(value.toString());
				if(superVO != null) {
					return "数据库中已存在编码为:" + value + "的记录。";
				}
			}
		}
		return null;
	}

}
