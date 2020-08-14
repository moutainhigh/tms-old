package org.nw.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.nw.vo.pub.lang.UFDate;

/**
 * UFDate^J?null
 * 
 * @author xuqc
 * @date 2011-7-13
 */
class UFDateDeserializer extends StdDeserializer<UFDate> {
	protected UFDateDeserializer(Class<?> vc) {
		super(vc);
	}

	public UFDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String date = jp.getText();
		return UFDate.getDate(date);
	}
}