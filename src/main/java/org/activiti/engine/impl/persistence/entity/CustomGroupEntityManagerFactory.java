package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

public class CustomGroupEntityManagerFactory implements SessionFactory {

	public Class<?> getSessionType() {
		return GroupEntityManager.class;
	}

	public Session openSession() {
		return new CustomGroupEntityManager();
	}
}
