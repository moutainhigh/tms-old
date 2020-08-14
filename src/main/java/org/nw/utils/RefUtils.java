package org.nw.utils;

import java.util.Map;

import org.nw.jf.ext.ref.BaseRefModel;
import org.nw.vo.RefVO;
import org.nw.vo.pub.SuperVO;


/**
 * 将BaseRefModel对象转换成RefVO对象
 * 
 * @author xuqc
 * @date 2012-2-7
 */
public class RefUtils {

	/**
	 * 
	 * @param refModel
	 * @param obj
	 * @author xuqc
	 * @date 2012-2-7
	 * 
	 */
	public static RefVO convert(BaseRefModel refModel, SuperVO superVO) {
		if(refModel == null) {
			return null;
		}
		if(superVO == null) {
			return null;
		}
		RefVO refVO = new RefVO();
		Object pk = superVO.getAttributeValue(refModel.getPkFieldCode());
		refVO.setPk(pk == null ? null : pk.toString());
		Object code = superVO.getAttributeValue(refModel.getCodeFieldCode());
		refVO.setCode(code == null ? null : code.toString());
		Object name = superVO.getAttributeValue(refModel.getNameFieldCode());
		refVO.setName(name == null ? null : name.toString());
		return refVO;
	}

	@SuppressWarnings("rawtypes")
	public static RefVO convert(BaseRefModel refModel, Map map) {
		if(refModel == null) {
			return null;
		}
		if(map == null) {
			return null;
		}
		RefVO refVO = new RefVO();
		Object pk = map.get(refModel.getPkFieldCode());
		refVO.setPk(pk == null ? null : pk.toString());
		Object code = map.get(refModel.getCodeFieldCode());
		refVO.setCode(code == null ? null : code.toString());
		Object name = map.get(refModel.getNameFieldCode());
		refVO.setName(name == null ? null : name.toString());
		return refVO;
	}
}
