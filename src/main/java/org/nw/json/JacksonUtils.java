package org.nw.json;

import java.io.IOException;
import java.io.Writer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;

/**
 * jackson�Ĺ����࣬��Ҫ���ڸ���ObjectMapper
 * 
 * @author xuqc
 * @date 2011-7-12
 */
public class JacksonUtils {

	/**
	 * ����mapper����
	 */
	private static ObjectMapper mapper = new UftObjectMapper();

	static {

		SimpleModule ufdateModule = new SimpleModule("UFDateDeserializer", Version.unknownVersion());
		ufdateModule.addDeserializer(UFDate.class, new UFDateDeserializer(UFDate.class));

		SimpleModule ufdatetimeModule = new SimpleModule("UFDateTimeDeserializer", Version.unknownVersion());
		ufdatetimeModule.addDeserializer(UFDateTime.class, new UFDateTimeDeserializer(UFDateTime.class));

		SimpleModule ufdoubleModule = new SimpleModule("UFDoubleDeserializer", Version.unknownVersion());
		ufdoubleModule.addDeserializer(UFDouble.class, new UFDoubleDeserializer(UFDouble.class));

		SimpleModule ufbooleanModule = new SimpleModule("UFBooleanDeserializer", Version.unknownVersion());
		ufbooleanModule.addDeserializer(UFBoolean.class, new UFBooleanDeserializer(UFBoolean.class));

		SimpleModule integerModule = new SimpleModule("IntegerDeserializer", Version.unknownVersion());
		integerModule.addDeserializer(Integer.class, new IntegerDeserializer(Integer.class));

		// ���map���
		// mapper.getSerializationConfig().set(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES,
		// false);
		// ���javabean
		// mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// �������ֻ����࣬��Map�в�������
		// mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
		// true);

		mapper.withModule(ufdateModule);
		mapper.withModule(ufdatetimeModule);
		mapper.withModule(ufdoubleModule);
		mapper.withModule(ufbooleanModule);
		mapper.withModule(integerModule);

		// ֻ���л���Ա����
		mapper.setVisibilityChecker(mapper.getVisibilityChecker().withFieldVisibility(Visibility.ANY));
		mapper.setVisibilityChecker(mapper.getVisibilityChecker().withGetterVisibility(Visibility.NONE));
		mapper.setVisibilityChecker(mapper.getVisibilityChecker().withIsGetterVisibility(Visibility.NONE));
	}

	/**
	 * ��ȡmapperʵ��
	 * 
	 * @return
	 */
	public static ObjectMapper getInstance() {
		return mapper;
	}

	/**
	 * ��obj����ת��Ϊjson��
	 * 
	 * @param obj
	 * @return
	 */
	public static String writeValueAsString(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * ��TreeModel��ȡ
	 * 
	 * @param json
	 * @return
	 */
	public static JsonNode readTree(String json) {
		JsonNode jn = null;
		try {
			jn = mapper.readTree(json);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return jn;
	}

	/**
	 * JsonNodeת������
	 * 
	 * @param <T>
	 * @param jn
	 * @param valueType
	 * @return
	 */
	public static <T> T readValue(JsonNode jn, Class<T> valueType) {
		try {
			return mapper.readValue(jn, valueType);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * json��ת��Ϊ����
	 * 
	 * @param <T>
	 * @param json
	 * @param valueType
	 * @return
	 */
	public static <T> T readValue(String json, Class<T> valueType) {
		try {
			return mapper.readValue(json, valueType);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���������д��writer
	 * 
	 * @param <T>
	 * @param w
	 * @param value
	 * 
	 */
	public static <T> void writeValue(Writer w, Object value) {
		try {
			mapper.writeValue(w, value);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
