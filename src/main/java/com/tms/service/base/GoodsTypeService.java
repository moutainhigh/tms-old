package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.GoodsTypeVO;

/**
 * 货品类型接口
 * 
 * @author xuqc
 * @date 2012-7-23 下午08:40:40
 */
public interface GoodsTypeService extends IToftService {

	public GoodsTypeVO getByCode(String code);
}
