/**
 * 
 */
package com.tms.service.cm;

import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;

import com.tms.vo.cm.CartypeTonnageVO;

/**
 * 车型吨位换算
 * 
 * @author xuqc
 * @Date 2015年6月1日 下午10:14:30
 *
 */
public interface CartypeTonnageService extends IToftService {

	/**
	 * 返回当前公司的车型吨位对照
	 * 
	 * @return
	 */
	public Map<String, List<CartypeTonnageVO>> getCartypeTonnageVOMap();

	/**
	 * 读取某个公司的车型吨位对照
	 * 
	 * @param pk_corp
	 * @return
	 */
	public Map<String, List<CartypeTonnageVO>> getCartypeTonnageVOMap(String pk_corp);
}
