package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.DriverVO;

/**
 * 实际操作接口
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:46:14
 */
public interface DriverService extends IToftService {

	public DriverVO getByCode(String code);
}
