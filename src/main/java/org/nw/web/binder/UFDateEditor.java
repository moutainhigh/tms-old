/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nw.web.binder;

import java.beans.PropertyEditorSupport;

import org.nw.vo.pub.lang.UFDate;
import org.springframework.util.StringUtils;

/**
 * Property editor for <code>java.util.Date</code>, supporting a custom
 * <code>java.text.DateFormat</code>.
 * <p>
 * This is not meant to be used as system PropertyEditor but rather as
 * locale-specific date editor within custom controller code, parsing
 * user-entered number strings into Date properties of beans and rendering them
 * in the UI form.
 * <p>
 * In web MVC code, this editor will typically be registered with
 * <code>binder.registerCustomEditor</code> calls in a custom
 * <code>initBinder</code> method.
 * 
 * @author Juergen Hoeller
 * @since 28.04.2003
 * @see java.util.Date
 * @see java.text.DateFormat
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder
 */
public class UFDateEditor extends PropertyEditorSupport {

	private final boolean allowEmpty = true;

	private final int exactDateLength = 10;

	/**
	 * Create a new CustomDateEditor instance, using the given DateFormat for
	 * parsing and rendering.
	 * <p>
	 * The "allowEmpty" parameter states if an empty String should be allowed
	 * for parsing, i.e. get interpreted as null value. Otherwise, an
	 * IllegalArgumentException gets thrown in that case.
	 * 
	 * @param dateFormat
	 *            DateFormat to use for parsing and rendering
	 * @param allowEmpty
	 *            if empty strings should be allowed
	 */
	public UFDateEditor() {
	}

	/**
	 * Parse the Date from the given text, using the specified DateFormat.
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if(this.allowEmpty && !StringUtils.hasText(text)) {
			// Treat empty String as null value.
			setValue(null);
		} else if(text != null && this.exactDateLength >= 0 && text.length() != this.exactDateLength) {
			throw new IllegalArgumentException("Could not parse date: it is not exactly" + this.exactDateLength
					+ "characters long");
		} else {
			setValue(UFDate.getDate(text));
		}
	}

	/**
	 * Format the Date as String, using the specified DateFormat.
	 */
	public String getAsText() {
		UFDate value = (UFDate) getValue();
		return (value != null ? value.toString() : "");
	}

}
