package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.TransLineVO;

/**
 * 运输线路操作接口
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:55:59
 */
public interface TransLineService extends IToftService {

	public TransLineVO getByCode(String code);

	public TransLineVO getByObject(TransLineVO lineVO);

}
