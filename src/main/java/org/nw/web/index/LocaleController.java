package org.nw.web.index;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.web.AbsBaseController;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;


/**
 * 本地化处理类
 * 
 * @author xuqc
 * @date 2013-7-22 下午04:55:31
 */
@Controller
@RequestMapping(value = "/locale")
public class LocaleController extends AbsBaseController {

	/**
	 * 更改语言，zh,cn,设置到session中，这个是在spring中配置的
	 * 
	 * @param new_lang
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/changeLang.do", method = RequestMethod.POST)
	public ModelAndView changeLanguage(@RequestParam String new_lang, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
			if(localeResolver == null) {
				throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
			}
			LocaleEditor localeEditor = new LocaleEditor();
			localeEditor.setAsText(new_lang);
			localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return new ModelAndView("/index.html");
	}
}
