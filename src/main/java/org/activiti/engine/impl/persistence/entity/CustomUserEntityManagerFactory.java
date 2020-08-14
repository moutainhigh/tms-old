package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

public class CustomUserEntityManagerFactory implements SessionFactory {
	public Class<?> getSessionType() {
		return UserEntityManager.class;
	}

	public Session openSession() {
		return new CustomUserEntityManager();
	}
}