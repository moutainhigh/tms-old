package org.nw.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;

/**
 * Integer巴л
 * 
 * @author fangw
 */
public class IntegerDeserializer extends StdDeserializer<Integer> {

	protected IntegerDeserializer(Class<?> vc) {
		super(vc);
	}

	public Integer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String b = jp.getText();
		if(b != null && b.length() > 0) {
			if(b.equalsIgnoreCase("N") || b.equalsIgnoreCase("false")) {
				b = "0";
			} else if(b.equalsIgnoreCase("Y") || b.equalsIgnoreCase("true")) {
				b = "1";
			}
		} else {
			return null;
		}
		return new Integer(b);
	}
}
