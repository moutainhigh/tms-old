package org.nw.json;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.BeanPropertyFilter;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.nw.vo.pub.SuperVO;

/**
 * ���ﶨ����һ��beanת����json�й���ĳЩ�ֶε�ʾ���࣬Ŀǰû���õ���
 * ע��������˵��ǳ�Ա����������Ҫ�õ�����Ҫ��Ч����ܻ���������ֻɨ���Ա�������磺<br/>
 * mapper.setVisibilityChecker(mapper.getVisibilityChecker().withFieldVisibility
 * (Visibility.ANY)); mapper.setVisibilityChecker(mapper.getVisibilityChecker().
 * withGetterVisibility(Visibility.NONE));
 * mapper.setVisibilityChecker(mapper.getVisibilityChecker().
 * withIsGetterVisibility(Visibility.NONE));
 * 
 * @author xuqc
 * @date 2012-3-15
 */
public class UftBeanPropertyFilter implements BeanPropertyFilter {

	// �������õ��ǹ��˵ĳ�Ա����������get��ͷ�ĺ���û����
	protected final static Set<String> _propertiesToExclude = new HashSet<String>();
	static {
		_propertiesToExclude.add("m_isDirty");
	}

	public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer)
			throws Exception {
		if(bean instanceof SuperVO) {
			if(!_propertiesToExclude.contains(writer.getName())) {
				writer.serializeAsField(bean, jgen, provider);
			}
		} else {
			writer.serializeAsField(bean, jgen, provider);
		}
	}

	public static void main(String[] args) {
		// ObjectMapper mapper = new UftObjectMapper();
		// FilterProvider fp = new
		// SimpleFilterProvider().addFilter("uftBeanFilter", new
		// UftBeanPropertyFilter());
		// try {
		// System.out.println(mapper.filteredWriter(fp).writeValueAsString(p));
		// System.out.println(mapper.writeValueAsString(p));
		// } catch(Exception e) {
		// e.printStackTrace();
		// }
	}
}
