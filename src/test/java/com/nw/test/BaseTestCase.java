package com.nw.test;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author aimer.xu
 * 
 */
public class BaseTestCase extends TestCase {
	protected ApplicationContext appContext;

	protected void setUp() throws Exception {
		super.setUp();
		appContext = new ClassPathXmlApplicationContext("classpath*:/spring/test-spring*.xml");
		assertNotNull("appContext为空", appContext);
	}
}
