package com.tms.service.base;

import org.nw.service.IToftService;

import com.tms.vo.base.GoodsPackRelaVO;
import com.tms.vo.base.GoodsVO;

/**
 * 货品操作接口
 * 
 * @author xuqc
 * @date 2012-7-23 下午09:07:32
 */
public interface GoodsService extends IToftService {

	public GoodsVO getByCode(String code);

	/**
	 * 返回货品关联的一个包装明细
	 * 
	 * @param pk_goods
	 * @param pk_goods_packcorp
	 * @return
	 */
	public GoodsPackRelaVO getGoodsPackRelaVO(String pk_goods, String pk_goods_packcorp);
	/**
	 * 根据货品编码及客户编码返回货品
	 * @param goodsCode
	 * @param customCode
	 * @return
	 */
	public GoodsVO getByGoodsCodeCustomCode(String goodsCode,String customCode);
}
