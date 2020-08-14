package org.nw.json;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.nw.basic.util.DateUtils;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFTime;

/**
 * spring′jacksonjsonUFDateTime,UFDate,UFTime,UFBoolean
 *  οspringmvc
 * 
 * @author xuqc
 * @date 2010-12-7
 */
public class UftObjectMapper extends ObjectMapper {
	private CustomSerializerFactory sf = new CustomSerializerFactory();

	public UftObjectMapper() {
		this.setSerializerFactory(sf);
		this.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		sf.addGenericMapping(UFDateTime.class, new JsonSerializer<UFDateTime>() {
					public void serialize(UFDateTime value, JsonGenerator jgen, SerializerProvider provider)
					throws IOException, JsonProcessingException {
				jgen.writeObject(value.toString());
			}
		});
		sf.addGenericMapping(UFDate.class, new JsonSerializer<UFDate>() {
					public void serialize(UFDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
					JsonProcessingException {
				jgen.writeObject(value.toString());
			}
		});
		sf.addGenericMapping(UFTime.class, new JsonSerializer<UFTime>() {
					public void serialize(UFTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
					JsonProcessingException {
				jgen.writeObject(value.toString());
			}
		});
		sf.addGenericMapping(Date.class, new JsonSerializer<Date>() {
					public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
					JsonProcessingException {
				SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.DATETIME_FORMAT_HORIZONTAL);
				jgen.writeObject(sdf.format(value));
			}
		});
		sf.addGenericMapping(UFBoolean.class, new JsonSerializer<UFBoolean>() {
					public void serialize(UFBoolean value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
					JsonProcessingException {
				jgen.writeObject(value.toString());
			}
		});
	}

	public CustomSerializerFactory getSerializerFactory() {
		return sf;
	}
}
