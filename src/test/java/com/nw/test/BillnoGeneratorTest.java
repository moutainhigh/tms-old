package com.nw.test;

import org.junit.Test;
import org.nw.utils.BillnoHelper;

/**
 * 
 * @author xuqc
 * @date 2014-5-11 下午10:49:00
 */
public class BillnoGeneratorTest extends BaseTestCase {

	class GenerateBillnoThread extends Thread {
		public void run() {
			BillnoHelper.generateBillnoByDefault("INSTO");
		}
	}

	@Test
	public void testThread() {
		for(int i = 0; i < 1000; i++) {
			new GenerateBillnoThread().start();
		}
		// 主线程需要挂起，否则子线程无法执行了
		try {
			Thread.currentThread().sleep(100000000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
