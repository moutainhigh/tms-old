package org.nw.jf.ulw;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.jf.ext.ref.userdefine.IMultiSelect;
import org.nw.jf.utils.UIUtils;
import org.nw.jf.utils.UiTempletUtils;
import org.nw.redis.RedisDao;


/**
 * 处理ulw中参照的数据，只处理多选下拉的数据，其他类型不需要处理
 * 
 * @author xuqc
 * @date 2013-8-4 下午04:59:08
 */
public class RefRenderer implements IRenderer {

	public Object render(Object value, int datatype, String reftype) {
		if(value == null || "".equals(value.toString())) {
			return "";
		}
		if(UIUtils.isMultiSelect(reftype, datatype)) {
			// 多选下拉
			Object obj = UIUtils.getReftypeObj(reftype, datatype);
			IMultiSelect multiSelect = (IMultiSelect) obj;
			String consult_code = multiSelect.getConsult_code();
			if(StringUtils.isNotBlank(consult_code)) {
				//List<String[]> selectValues = UiTempletUtils.getSelectValues(consult_code);
				List<String[]> selectValues =  RedisDao.getInstance().getSelectValues(consult_code);
				StringBuffer sb = new StringBuffer();
				String[] valueArr = value.toString().split(",");
				for(String one : valueArr) {
					for(String[] option : selectValues) {
						if(one.equals(option[1])) {
							sb.append("[");
							sb.append(option[0]);
							sb.append("]");
							sb.append(",");
						}
					}
				}
				if(sb.length() > 0) {
					return sb.substring(0, sb.length() - 1);
				}
			}
		}
		return value;
	}

}
