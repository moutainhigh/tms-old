package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.CarTypeVO;

/**
 * 车辆类型操作接口
 * 
 * @author xuqc
 * @date 2012-7-22 下午02:47:25
 */
public interface CarTypeService extends IToftService {

	public CarTypeVO getByCode(String code);

	/**
	 * 根据车辆类型的名称查询，目前使用在合同导入的时候
	 * 
	 * @param name
	 * @return
	 */
	public CarTypeVO getByName(String name);
}
