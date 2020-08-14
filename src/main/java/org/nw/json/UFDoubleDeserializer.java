package org.nw.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.nw.vo.pub.lang.UFDouble;

/**
 * UFDouble^J
 * ?jsonее_-_null
 * 
 * @author wangxf
 * @date 2011-7-14
 */
public class UFDoubleDeserializer extends StdDeserializer<UFDouble> {

	protected UFDoubleDeserializer(Class<?> vc) {
		super(vc);
	}

	public UFDouble deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		Double d = super._parseDouble(jp, ctxt);
		if(d == null) {
			return null;
		} else if(d.compareTo(Double.valueOf(1)) == 0) {
			return UFDouble.ONE_DBL;
		} else if(d.compareTo(Double.valueOf(0)) == 0) {
			return UFDouble.ZERO_DBL;
		}
		return new UFDouble(d);
	}

}
