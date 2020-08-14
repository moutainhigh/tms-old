package org.nw.jf.ulw;

import java.util.List;

import org.nw.jf.utils.UiTempletUtils;
import org.nw.redis.RedisDao;


/**
 * 处理ULW中的下拉框
 * 
 * @author xuqc
 * @date 2012-5-11
 */
public class ComboRenderer implements IRenderer {

	/*
	 * 下拉框类型，根据value返回text值
	 * 
	 * @see com.uft.webnc.service.IULWRenderer#render(java.lang.Object)
	 */
	public Object render(Object value, int datatype, String reftype) {
		if(value == null){
			return "";
		}
		//List<String[]> values = UiTempletUtils.getSelectValues(reftype);
		List<String[]> values =  RedisDao.getInstance().getSelectValues(reftype);
		//下拉类型支持多选显示。
		String[] valueArr = null;
		if(value.toString().indexOf(",") > 0){
			valueArr = value.toString().split(",");
		}else if(value.toString().indexOf("\\|") > 0){
			valueArr = value.toString().split("\\|");
		}else{
			valueArr = new String[]{value.toString()};
		}
		if(values != null){
			String retStr = "";
			for(int i = 0; i < values.size(); i++) {
				String[] arr = values.get(i); // 这里的arr的长度肯定是2,第一个值是text，第二个值是value
				//如果传入"|"则按照"|"拼接，否则都是","
				for(String valueUnit : valueArr){
					if(valueUnit.trim().equals(arr[1].trim())){
						if(value.toString().indexOf("\\|") > 0){
							retStr += arr[0].trim() +"\\|";
						}else{
							retStr += arr[0].trim() +",";
						}
					}
				}
			}
			if(retStr.length() > 0){
				return retStr.substring(0, retStr.length()-1);
			}
		}
		//FIXME
//		for(String[] arr : values) {
//			if(arr[1].equals(String.valueOf(value))) {
//				return arr[0];
//			}
//		}
		return value;
	}

}
