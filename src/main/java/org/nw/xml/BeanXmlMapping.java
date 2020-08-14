package org.nw.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * xml与javabean的相互转化类
 * 
 * @author aimer.xu
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BeanXmlMapping {
	public static String DATARECORD = "data";

	Reflection reflect;
	Map fields;// fields in pojo which inherit from model
	Map methods;// method in pojo which inherit from model
	Class clazz;
	Object instance; // an instance of the pojo which inherit from model
	String parentNode; // 生成的父节点的名称
	Map columnMappings;
	String[] fieldNames; // 生成子节点的节点集合

	public BeanXmlMapping() {

	}

	public BeanXmlMapping(Class clazz) throws Exception {
		this.clazz = clazz;
		instance = clazz.newInstance();
		reflect = new Reflection(clazz);
		fields = reflect.getFieldsWithSuper(clazz);
		methods = reflect.getMethods();
	}

	public BeanXmlMapping(String className) throws Exception {
		this(Class.forName(className));
	}

	public BeanXmlMapping(Object instance) throws Exception {
		this(instance.getClass());
		this.instance = instance;
	}

	/**
	 * 设置所有字段生成的节点的父节点，默认是dataRecord
	 * 
	 * @param parentNode
	 */
	public void setParentNode(String parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * 设置instance对象,该对象包含要转化为node所包含的javabean信息
	 * 
	 * @param instance
	 */
	public void setInstance(Object instance) throws Exception {
		this.instance = instance;
		this.clazz = this.instance.getClass();
		reflect = new Reflection(clazz);
		fields = reflect.getFieldsWithSuper(clazz);
		methods = reflect.getMethods();
	}

	private String getSetterMethodName(String nodeName) {
		StringBuffer sb = new StringBuffer();
		sb.append(nodeName);
		if(Character.isLowerCase(sb.charAt(0))) {
			if(sb.length() == 1 || !Character.isUpperCase(sb.charAt(1))) {
				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			}
		}
		sb.insert(0, "set"); //$NON-NLS-1$
		return sb.toString();
	}

	private String getGetterMethodName(String type, String nodeName) {
		StringBuffer sb = new StringBuffer();
		sb.append(nodeName);
		if(Character.isLowerCase(sb.charAt(0))) {
			if(sb.length() == 1 || !Character.isUpperCase(sb.charAt(1))) {
				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			}
		}
		if(type.toLowerCase().indexOf("boolean") == -1) {
			sb.insert(0, "get"); //$NON-NLS-1$
		} else {
			sb.insert(0, "is"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private Field getField(String fieldName) {
		if(fieldName == null) {
			return null;
		}
		// 有些地方并不规范,如果fieldName找不到,那么将第一个字母设置为小写,然后再找一次
		Object obj = fields.get(fieldName);
		if(obj != null) {
			return (Field) obj;
		}
		char first = fieldName.charAt(0);
		fieldName = first + fieldName.substring(1);
		return (Field) fields.get(fieldName);
	}

	private Method getMethod(String methodName) {
		if(methodName == null) {
			return null;
		}
		return (Method) methods.get(methodName);
	}

	/**
	 * read nodevalue from parent node.and set this value to javabean. the node
	 * may like: <dataRecord> <id>10000</id> <name>aaa</name>
	 * <address>ningbo</address> </dataRecord> return javabean
	 * 
	 * @param parent
	 * @return Object
	 * @throws Exception
	 */
	public Object getBeanFromNode(Element parent) throws Exception {
		Object instance = clazz.newInstance();
		List elList = parent.elements();
		for(int i = 0; i < elList.size(); i++) {
			Element child = (Element) elList.get(i);
			String nodeName = child.getName();
			String nodeValue = child.getTextTrim();
			Field field = getField(nodeName);
			if(field == null)
				continue;
			String setterMethod = getSetterMethodName(nodeName);
			Method method = getMethod(setterMethod);
			String typeName = field.getType().getName();
			Object[] obj = { ResolvedJavaType.getResolvedValue(typeName, nodeValue) };
			method.invoke(instance, obj);
		}
		return instance;
	}

	/**
	 * read nodevalue from a NodeList,this NodeList includes several dataRecord
	 * node. the node may like: <dataRecord> <id>10000</id> <name>aaa</name>
	 * <address>ningbo</address> </dataRecord> <dataRecord> <id>10000</id>
	 * <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param nodes
	 * @return List javabeans in this List
	 * @throws Exception
	 */
	public List getBeansFromNodeList(List<Element> nodes) throws Exception {
		List beans = new ArrayList();
		for(int i = 0; i < nodes.size(); i++) {
			Element node = nodes.get(i);
			beans.add(getBeanFromNode(node));
		}
		return beans;
	}

	private String getFieldName(String fieldName) {
		if(this.columnMappings == null)
			return fieldName;
		return columnMappings.get(fieldName) == null ? fieldName : (String) columnMappings.get(fieldName);
	}

	private String[] getFieldNames() {
		Collection c = fields.values();
		String[] fieldNames = new String[c.size()];
		int i = 0;
		for(Iterator it = c.iterator(); it.hasNext();) {
			fieldNames[i] = ((Field) it.next()).getName();
			i++;
		}
		return fieldNames;
	}

	/**
	 * converse javabean to a xml node. the node may like: <dataRecord>
	 * <id>10000</id> <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromBean() throws Exception {
		Element _dataRecord = DocumentHelper.createElement(parentNode == null ? DATARECORD : parentNode);
		if(this.fieldNames == null)
			this.fieldNames = getFieldNames();
		for(int i = 0; i < this.fieldNames.length; i++) {
			String fieldName = this.fieldNames[i];
			Field field = (Field) fields.get(fieldName);
			if(field == null)
				continue;
			String getterMethod = getGetterMethodName(field.getType().getName(), fieldName);
			Method method = getMethod(getterMethod);
			Element e = DocumentHelper.createElement(getFieldName(fieldName));
			Object o = method.invoke(instance, null);
			if(o == null) {
				Object obj = ResolvedJavaType.getDefaultResolvedValue(field.getType().getName());
				if(obj == null)
					obj = "";
				e.setText(String.valueOf(obj));
			} else
				e.setText(String.valueOf(o).trim());
			_dataRecord.add(e);
		}
		return _dataRecord;
	}

	/**
	 * converse javabean to a xml node. the node may like: <dataRecord>
	 * <id>10000</id> <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param fieldNames
	 *            include the fieldName you want to append to the parent node
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromBean(List fieldNames) throws Exception {
		if(fieldNames == null)
			return getNodeFromBean();
		this.fieldNames = new String[fieldNames.size()];
		for(int i = 0; i < fieldNames.size(); i++) {
			this.fieldNames[i] = (String) fieldNames.get(i);
		}
		return getNodeFromBean();
	}

	/**
	 * converse javabean to a xml node. the node may like: <dataRecord>
	 * <id>10000</id> <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param fieldNames
	 *            include the fieldName you want to append to the parent node
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromBean(String[] fieldNames) throws Exception {
		this.fieldNames = fieldNames;
		return getNodeFromBean();
	}

	/**
	 * converse the javabean to a xml node
	 * 
	 * @param o
	 *            the javabean object you want to build.
	 * @param document
	 *            the owner document of the return node
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromBean(Object o) throws Exception {
		// this.instance = o;
		setInstance(o);
		return getNodeFromBean();
	}

	/**
	 * converse javabean to a xml node. the node may like: <dataRecord>
	 * <id>10000</id> <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param columnMappings
	 *            field name in model with real node name in xml
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromBean(Map columnMappings) throws Exception {
		this.columnMappings = columnMappings;
		return getNodeFromBean();
	}

	/**
	 * converse javabean to a xml node. the node may like: <dataRecord>
	 * <id>10000</id> <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param columnMappings
	 *            nodename alias
	 * @param fieldNames
	 *            the node you want to create
	 * @return node
	 * @throws Exception
	 */
	public Node getNodeFromBean(Document document, Map columnMappings, String[] fieldNames) throws Exception {
		this.columnMappings = columnMappings;
		this.fieldNames = fieldNames;
		return getNodeFromBean(document);
	}

	/**
	 * converse javabean to a xml node. the node may like: <dataRecord>
	 * <id>10000</id> <name>aaa</name> <address>ningbo</address> </dataRecord>
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param columnMappings
	 *            nodename alias
	 * @param fieldNames
	 *            the node you want to create
	 * @return node
	 * @throws Exception
	 */
	public Node getNodeFromBean(Map columnMappings, List fieldNames) throws Exception {
		this.columnMappings = columnMappings;
		return getNodeFromBean(fieldNames);
	}

	/**
	 * 将传入的多个javabean转换为多个node。
	 * 
	 * @param list
	 *            include javabeans you want to build the nodes;
	 * @param document
	 *            用于生成节点的document
	 * @return List<Node> 返回的List，包含所有生成的dataRecord节点
	 * @throws Exception
	 */
	public List getNodesFromBeans(List beans) throws Exception {
		List nodes = new ArrayList();
		for(int i = 0; i < beans.size(); i++) {
			nodes.add(getNodeFromBean(beans.get(i)));
		}
		return nodes;
	}

	/**
	 * 将传入的多个javabean转换为多个node。
	 * 
	 * @param beans
	 *            存储多个javabean，用于生成节点
	 * @param document
	 *            用于生成节点的document
	 * @param fieldNames
	 *            定义dataRecord包含的子节点，若不定义，则生成包含所有field的子节点
	 * @return List<Node> 返回的List，包含所有生成的dataRecord节点
	 * @throws Exception
	 */
	public List getNodesFromBeans(List beans, String[] fieldNames) throws Exception {
		List nodes = new ArrayList();
		for(int i = 0; i < beans.size(); i++) {
			// this.instance = beans.get(i);
			setInstance(beans.get(i));
			nodes.add(getNodeFromBean(fieldNames));
		}
		return nodes;
	}

	/**
	 * 将传入的多个javabean转换为多个node。
	 * 
	 * @param beans
	 *            存储多个javabean，用于生成节点
	 * @param document
	 *            用于生成节点的document
	 * @param fieldNames
	 *            定义dataRecord包含的子节点，若不定义，则生成包含所有field的子节点
	 * @return List<Node> 返回的List，包含所有生成的dataRecord节点
	 * @throws Exception
	 */
	public List getNodesFromBeans(List beans, List fieldNames) throws Exception {
		List nodes = new ArrayList();
		for(int i = 0; i < beans.size(); i++) {
			// this.instance = beans.get(i);
			setInstance(beans.get(i));
			nodes.add(getNodeFromBean(fieldNames));
		}
		return nodes;
	}

	/**
	 * 将传入的多个javabean转换为多个node。
	 * 
	 * @param beans
	 *            存储多个javabean，用于生成节点
	 * @param document
	 *            用于生成节点的document
	 * @param columnMappings
	 *            定义字段名与节点名的对应关系，如字段名是AAA，但生成的节点可能是BBB
	 * @return List<Node> 返回的List，包含所有生成的dataRecord节点
	 * @throws Exception
	 */
	public List getNodesFromBeans(List beans, Map columnMappings) throws Exception {
		List nodes = new ArrayList();
		for(int i = 0; i < beans.size(); i++) {
			// this.instance = beans.get(i);
			setInstance(beans.get(i));
			nodes.add(getNodeFromBean(columnMappings));
		}
		return nodes;
	}

	/**
	 * 将传入的多个javabean转换为多个node。
	 * 
	 * @param document
	 * @param beans
	 *            存储多个javabean，用于生成节点
	 * @param fieldNames
	 *            存储要生成的节点名称
	 * @param columnMappings
	 *            存储要生成的节点名称的别名
	 * @return List<Node>
	 * @throws Exception
	 */
	public List getNodesFromBeans(List beans, String[] fieldNames, Map columnMappings) throws Exception {
		this.columnMappings = columnMappings;
		List nodes = new ArrayList();
		for(int i = 0; i < beans.size(); i++) {
			// this.instance = beans.get(i);
			setInstance(beans.get(i));
			nodes.add(getNodeFromBean(fieldNames));
		}
		return nodes;
	}

	/**
	 * 将传入的多个javabean转换为多个node。
	 * 
	 * @param document
	 * @param beans
	 *            存储多个javabean，用于生成节点
	 * @param fieldNames
	 *            存储要生成的节点名称
	 * @param columnMappings
	 *            存储要生成的节点名称的别名
	 * @return List<Node>
	 * @throws Exception
	 */
	public List getNodesFromBeans(List beans, List fieldNames, Map columnMappings) throws Exception {
		this.columnMappings = columnMappings;
		List nodes = new ArrayList();
		for(int i = 0; i < beans.size(); i++) {
			// this.instance = beans.get(i);
			setInstance(beans.get(i));
			nodes.add(getNodeFromBean(fieldNames));
		}
		return nodes;
	}

	/**
	 * 将map中的key值以数组的形式返回
	 * 
	 * @param map
	 * @return
	 */
	private String[] getFieldNames(Map map) {
		String[] fieldNames = new String[map.size()];
		Iterator it = map.keySet().iterator();
		int i = 0;
		while(it.hasNext()) {
			fieldNames[i] = (String) it.next();
			i++;
		}
		return fieldNames;
	}

	/**
	 * 将传入的一个map对象转换为一个节点,父节点默认是dataRecord,子节点名就是map的key值
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param map
	 *            从数据库中读取到的map对象,map中存储columnname-value
	 * @param fieldNames
	 *            定义返回的子节点，该定义的范围不能超过map的key值，若超过，将创建空的子节点
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromMap(Document document, Map map) throws Exception {
		Element _dataRecord = document.addElement(parentNode == null ? DATARECORD : parentNode);
		if(map == null)
			return _dataRecord;
		if(this.fieldNames == null)
			this.fieldNames = getFieldNames(map);
		for(int i = 0; i < this.fieldNames.length; i++) {
			Element e = document.addElement(this.fieldNames[i]);
			e.setText(String.valueOf(map.get(this.fieldNames[i]) == null ? "" : map.get(this.fieldNames[i])).trim());
			_dataRecord.add(e);
		}
		return _dataRecord;
	}

	/**
	 * 将传入的一个map对象转换为一个节点,父节点默认是dataRecord,子节点名就是map的key值
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param map
	 *            从数据库中读取到的map对象,map中存储columnname-value
	 * @return Node
	 * @throws Exception
	 */
	public Node getNodeFromMap(Document document, Map map, String[] fieldNames) throws Exception {
		this.fieldNames = fieldNames;
		return getNodeFromMap(document, map);
	}

	/**
	 * 将传入的List<map>对象转换为多个Node，其中节点名就是map中的key值
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param maps
	 *            从数据库中读取到的List<map>对象,map中存储columnname-value
	 * @return List<Node>
	 * @throws Exception
	 */
	public List getNodesFromMaps(Document document, List maps) throws Exception {
		List nodes = new ArrayList();
		for(int i = 0; i < maps.size(); i++) {
			Map map = (Map) maps.get(i);
			nodes.add(getNodeFromMap(document, map));
		}
		return nodes;
	}

	/**
	 * 将传入的List<map>对象转换为多个Node，其中节点名就是map中的key值
	 * 
	 * @param document
	 *            the owner document of the return node
	 * @param maps
	 *            从数据库中读取到的List<map>对象,map中存储columnname-value
	 * @param fieldNames
	 *            定义返回的子节点，该定义的范围不能超过map的key值，若超过，将创建空的子节点
	 * @return List<Node>
	 * @throws Exception
	 */
	public List getNodesFromMaps(Document document, List maps, String[] fieldNames) throws Exception {
		List nodes = new ArrayList();
		for(int i = 0; i < maps.size(); i++) {
			Map map = (Map) maps.get(i);
			nodes.add(getNodeFromMap(document, map, fieldNames));
		}
		return nodes;
	}

}