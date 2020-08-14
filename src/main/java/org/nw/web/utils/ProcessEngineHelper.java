package org.nw.web.utils;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;

/**
 * 
 * @author xuqc
 *
 */
public class ProcessEngineHelper {

	private static ProcessEngine processEngine = null;

	public static ProcessEngine getProcessEngine() {
		if(processEngine == null) {
			processEngine = ProcessEngines.getDefaultProcessEngine();
		}
		return processEngine;
	}

}
