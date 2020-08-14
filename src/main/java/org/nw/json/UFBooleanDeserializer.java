package org.nw.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.nw.vo.pub.lang.UFBoolean;

/**
 * UFBoolean巴л
 * 
 * @author Wangxf
 * @date 2011-8-17
 */
public class UFBooleanDeserializer extends StdDeserializer<UFBoolean> {

	protected UFBooleanDeserializer(Class<?> vc) {
		super(vc);
	}

	public UFBoolean deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		String b = jp.getText();
		if("1".equals(b)) {
			b = "Y";
		} else if("0".equals(b)) {
			b = "N";
		}
		return UFBoolean.valueOf(b);
	}

}
