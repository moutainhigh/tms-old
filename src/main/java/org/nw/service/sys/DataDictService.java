package org.nw.service.sys;

import org.nw.service.IToftService;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.DataDictVO;


/**
 * 数据字典操作接口
 * 
 * @author xuqc
 * @date 2012-7-15 下午05:32:25
 */
public interface DataDictService extends IToftService {

	/**
	 * 根据数据类型编码返回表头VO
	 * 
	 * @param datatype_code
	 * @return
	 */
	public DataDictVO getByDatatypeCode(String datatype_code);

	/**
	 * 根据数据类型编码返回AggVO
	 * 
	 * @param datatype_code
	 * @return
	 */
	public AggregatedValueObject getAggVOByDatatypeCode(String datatype_code);
}
