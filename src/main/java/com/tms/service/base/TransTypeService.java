package com.tms.service.base;

import java.util.Map;

import org.nw.service.IToftService;
import org.nw.vo.pub.lang.UFDouble;

import com.tms.vo.base.TransTypeVO;

/**
 * 运输方式操作接口
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:53:12
 */
public interface TransTypeService extends IToftService {

	public TransTypeVO getByCode(String code);

	/**
	 * 返回所有的运输方式
	 * 
	 * @return
	 */
	public TransTypeVO[] getAllTransTypeVOs();


}
