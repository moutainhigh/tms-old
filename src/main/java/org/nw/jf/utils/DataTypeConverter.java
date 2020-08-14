package org.nw.jf.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nw.dao.RefinfoDao;
import org.nw.jf.UiConstants;
import org.nw.jf.UiConstants.DATATYPE;
import org.nw.jf.ulw.CheckboxRenderer;
import org.nw.jf.ulw.ComboRenderer;
import org.nw.jf.ulw.DateTimeRenderer;
import org.nw.jf.ulw.IRenderer;
import org.nw.jf.ulw.NumberRenderer;
import org.nw.jf.ulw.RefRenderer;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.vo.sys.RefInfoVO;

public class DataTypeConverter {

	private static final Log log = LogFactory.getLog(DataTypeConverter.class);

	// NC ģ�������õ���ݸ�ʽ��Ext����ݸ�ʽ�Ķ��չ�ϵ
	public static Map<Integer, String> datatype_converter = new HashMap<Integer, String>();

	/**
	 * ULW��������ͼ���renderer���Map
	 */
	public static Map<Integer, IRenderer> ULWRendererMap = new HashMap<Integer, IRenderer>();

	static {
		// �ַ�����С�����ڣ��߼������գ��������Զ��嵵����ʱ�䣬���ı���ͼƬ������ռλ�죬�����
		// form�е�type��list�е�xtype�᲻һ��
		datatype_converter.put(UiConstants.DATATYPE.TEXT.intValue(), UiConstants.FORM_XTYPE.TEXTFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.INTEGER.intValue(), UiConstants.FORM_XTYPE.NUMBERFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.DECIMAL.intValue(), UiConstants.FORM_XTYPE.NUMBERFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.DATE.intValue(), UiConstants.FORM_XTYPE.UFTDATEFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.CHECKBOX.intValue(), UiConstants.FORM_XTYPE.UFTCHECKBOX.toString());
		datatype_converter.put(UiConstants.DATATYPE.REF.intValue(), UiConstants.FORM_XTYPE.HEADERREFFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.SELECT.intValue(), UiConstants.FORM_XTYPE.COMBO.toString());
		// �Զ��嵵��Ҳ�ǲ���
		datatype_converter.put(UiConstants.DATATYPE.USERDEFINE.intValue(),
				UiConstants.FORM_XTYPE.HEADERREFFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.TIME.intValue(), UiConstants.FORM_XTYPE.TIMEFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.TEXTAREA.intValue(), UiConstants.FORM_XTYPE.TEXTAREA.toString());
		// TODO ͼƬ
		datatype_converter.put(UiConstants.DATATYPE.PHOTO.intValue(), UiConstants.FORM_XTYPE.IMAGEFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.IDENTITY.intValue(), UiConstants.FORM_XTYPE.IMAGEFIELD.toString());
		/**
		 * �������Ϊ����ʱ��ʹ�ò��������ֶ�
		 */
		// TODO ����
		// datatype_converter.put(11,OBJECT);
		// TODOռλ��
		datatype_converter.put(UiConstants.DATATYPE.BLOCK.intValue(), UiConstants.FORM_XTYPE.DISPLAYFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.PASSWORD.intValue(), UiConstants.FORM_XTYPE.TEXTFIELD.toString());

		// NC56������ֶ����ͣ�����������Ӧ�����ⱨ�?ʵ����ʹ��ʲô�ռ���Ҫ��ȷ��
		datatype_converter.put(UiConstants.DATATYPE.EMAIL.intValue(), UiConstants.FORM_XTYPE.TEXTFIELD.toString());
		datatype_converter.put(UiConstants.DATATYPE.TIMESTAMP.intValue(),
				UiConstants.FORM_XTYPE.DATETIMEFIELD.toString());// 日期和时间类型
		datatype_converter.put(UiConstants.DATATYPE.CUSTOM.intValue(), UiConstants.FORM_XTYPE.TEXTFIELD.toString());

		ULWRendererMap.put(UiConstants.DATATYPE.INTEGER.intValue(), new NumberRenderer());
		ULWRendererMap.put(UiConstants.DATATYPE.DECIMAL.intValue(), new NumberRenderer());
		ULWRendererMap.put(UiConstants.DATATYPE.SELECT.intValue(), new ComboRenderer());
		ULWRendererMap.put(UiConstants.DATATYPE.CHECKBOX.intValue(), new CheckboxRenderer());
		ULWRendererMap.put(UiConstants.DATATYPE.TIMESTAMP.intValue(), new DateTimeRenderer());
		ULWRendererMap.put(UiConstants.DATATYPE.REF.intValue(), new RefRenderer());
	}

	/**
	 * �����������
	 * 
	 * @param vo
	 * @return
	 */
	public static String getFieldType(BillTempletBVO vo) {
		if(DATATYPE.OBJECT.intValue() == vo.getDatatype().intValue()) {
			// ���������Ϊ"����"
			// ��ʹ�á��������á��������
			return vo.getReftype();
		}
		String result = datatype_converter.get(vo.getDatatype());
		if(StringUtils.isNotBlank(result)) {
			return result;
		} else {
			throw new RuntimeException("�ֶΡ�" + vo.getItemkey() + "�����õ����͡�" + vo.getDatatype()
					+ "����δ����ת�����͡�");
		}
	}

	/**
	 * ���ز��վ�����
	 * 
	 * @param reftype
	 * @return
	 */
	public static String getRefClazz(String reftype, int datatype) {
		if(StringUtils.isBlank(reftype)) {
			return null;
		}
		if(reftype.indexOf(",") > -1) {
			// nc�еĲ�����Щ������������ڲ�������ϣ��粿�ŵ���,code=Y,nl=N
			reftype = reftype.substring(0, reftype.indexOf(","));
		}

		String refclass = null;
		if(DATATYPE.REF.intValue() == datatype) {
			// ����
			if(reftype.startsWith("<")) {
				// �Զ�����գ���ʽ��<com.uft.webnc.jf.ext.ref.SSRefModel>
				refclass = reftype.substring(1, reftype.length() - 1);
			} else {
				RefinfoDao dao = new RefinfoDao();
				RefInfoVO vo = dao.getRefinfoVO(reftype);
				refclass = vo.getRefclass();
			}
			if(StringUtils.isBlank(refclass)) {
				refclass = reftype;
			}
		} else {
			throw new RuntimeException("���Ͳ�����ȷ:reftype=" + reftype);
		}
		return refclass.trim();
	}

	/**
	 * ���ز�����ƣ�һ��������
	 * 
	 * @param reftype
	 * @return
	 */
	public static String getRefName(String reftype, int datatype) {
		if(reftype.indexOf(",") > -1) {
			// nc�еĲ�����Щ������������ڲ�������ϣ��粿�ŵ���,code=Y,nl=N
			reftype = reftype.substring(0, reftype.indexOf(","));
		}
		String name = null;
		if(DATATYPE.REF.intValue() == datatype) {
			if(reftype.startsWith("<")) {
				// ������Զ������,����������ͷ�Ļ��ϲ���Ҫ���õ�getRefName
				log.warn(reftype + "û�м̳�getRefTitle������ʹ��������Ϊ������ƣ�");
				name = reftype;
			} else {
				RefinfoDao dao = new RefinfoDao();
				RefInfoVO vo = dao.getRefinfoVO(reftype);
				name = vo.getName();
			}
		} else {
			throw new RuntimeException("���Ͳ�����ȷ:reftype=" + reftype);
		}
		return name;
	}

}
