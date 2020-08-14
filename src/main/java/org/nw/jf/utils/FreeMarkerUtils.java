package org.nw.jf.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.nw.dao.CacheUtils;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

/**
 * freeMarkerģ����ع�����
 * 
 * @author xuqc
 * @date 2011-6-3
 */
public class FreeMarkerUtils {
	private static final String DEFAULT_TEMPLATE_FOLDER = "/template";
	private static Configuration configuration = null;

	public static Configuration getConfiguration() {
		if(configuration == null) {
			try {
				configuration = new Configuration();
				configuration.setDefaultEncoding("UTF-8");
				if(!CacheUtils.isUseCache()) {
					configuration.setTemplateUpdateDelay(0);// 如果是debug状态,不缓存模板
				} else {
					configuration.setTemplateUpdateDelay(-1);
				}
				configuration.setDirectoryForTemplateLoading(new File(FreeMarkerUtils.class.getResource(
						DEFAULT_TEMPLATE_FOLDER).getFile()));

				BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
				wrapper.setExposureLevel(BeansWrapper.EXPOSE_ALL);
				TemplateHashModel staticModels = wrapper.getStaticModels();
				TemplateModel model = staticModels.get(UiTempletUtils.class.getName());
				configuration.setSharedVariable(UiTempletUtils.class.getSimpleName(), model);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return configuration;
	}

	/**
	 * ����Ĭ�ϵ�ģ�壬��ģ�嶨����webnc-core�У�һ��ʹ�ø�ģ�������
	 * 
	 * @param tplPath
	 * @return
	 */
	public static Template getDefaultTemplate(String fileName) throws IOException, TemplateException {
		// FreeMarkerUtils.class.getResource("").getHost();
		// //��ʲô�ã���ȥ����--fangw
		String folder = FreeMarkerUtils.class.getClassLoader().getResource(DEFAULT_TEMPLATE_FOLDER).getFile();
		FreeMarkerUtils.getConfiguration().setDirectoryForTemplateLoading(new File(folder));// ����ģ�����ڵ��ļ���
		return FreeMarkerUtils.getConfiguration().getTemplate(fileName);
	}

	/**
	 * ���Ե��ø÷��������û��Զ����ģ��
	 * 
	 * @param fileName
	 * @return
	 */
	public static Template getTemplate(String fileName) throws IOException, TemplateException {
		// �����쳣������
		return FreeMarkerUtils.getConfiguration().getTemplate(fileName);
	}

	/**
	 * Process the specified FreeMarker template with the given model and write
	 * the result to the given Writer.
	 * <p>
	 * When using this method to prepare a text for a mail to be sent with
	 * Spring's mail support, consider wrapping IO/TemplateException in
	 * MailPreparationException.
	 * 
	 * @param model
	 *            the model object, typically a Map that contains model names as
	 *            keys and model objects as values
	 * @return the result as String
	 * @throws IOException
	 *             if the template wasn't found or couldn't be read
	 * @throws freemarker.template.TemplateException
	 *             if rendering failed
	 * @see org.springframework.mail.MailPreparationException
	 */
	public static String processTemplateIntoString(Template template, Object model) throws IOException,
			TemplateException {
		StringWriter result = new StringWriter();
		template.process(model, result);
		return result.toString();
	}

	/**
	 * ����ģ�壬���ؽ�������ַ�
	 * 
	 * @param fileName
	 * @param model
	 * @return
	 */
	public static String processTemplateIntoString(String fileName, Object model) throws IOException, TemplateException {
		Template template = getTemplate(fileName);
		return processTemplateIntoString(template, model);
	}

	/**
	 * ����ģ��
	 * 
	 * @param fileName
	 * @param model
	 * @param writer
	 */
	public static void processTemplate(String fileName, Object model, Writer writer) throws IOException,
			TemplateException {
		Template template = getTemplate(fileName);
		template.process(model, writer);
	}
}
