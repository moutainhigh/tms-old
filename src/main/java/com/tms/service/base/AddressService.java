package com.tms.service.base;

import java.util.List;
import java.util.Map;

import org.nw.service.IToftService;

import com.tms.vo.base.AddressVO;

/**
 * 地址档案接口
 * 
 * @author xuqc
 * @date 2012-7-25 上午11:46:14
 */
public interface AddressService extends IToftService {

	public AddressVO getByCode(String code);

	/**
	 * 根据地址名称匹配地址，目前用于合同明细导入
	 * 
	 * @param name
	 * @return
	 */
	public AddressVO getByName(String name);
	
	//yaojiie 2015 11 19 添加获取地址经纬度接口 并返回不能获取到经纬度地址的提示信息。
	public String addLongitude(String[] pk_addressS);
}
