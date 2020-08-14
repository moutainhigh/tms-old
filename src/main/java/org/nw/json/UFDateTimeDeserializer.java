package org.nw.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.nw.vo.pub.lang.UFDateTime;

/**
 * UFDateTime^J?null
 * 
 * @author wangxf
 * @date 2011-7-14
 */
class UFDateTimeDeserializer extends StdDeserializer<UFDateTime> {

	protected UFDateTimeDeserializer(Class<?> vc) {
		super(vc);
	}

	public UFDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
			JsonProcessingException {
		String date = jp.getText();
		if(date == null || date.length() == 0) {
			return null;
		}
		return new UFDateTime(date);
	}
}
