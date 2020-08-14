package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.CarVO;

/**
 * 车辆操作接口
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:48:54
 */
public interface CarService extends IToftService {

	public CarVO getByCarno(String carno);
}
