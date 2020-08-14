package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.GoodsPackcorpVO;

/**
 * 货品包装单位操作接口
 * 
 * @author xuqc
 * @date 2012-7-23 下午08:46:49
 */
public interface GoodsPackcorpService extends IToftService {

	public GoodsPackcorpVO getByCode(String code);
}
