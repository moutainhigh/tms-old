package org.nw.utils.office;

import java.io.InputStream;

import org.apache.log4j.Logger;

public class AsposeLicense {

	Logger logger = Logger.getLogger(AsposeLicense.class);

	private String licensePath = "org/nw/utils/office/Aspose.Words.lic";
	private static AsposeLicense asposeLicense = new AsposeLicense();
	private Boolean wordsok = null;
	private Boolean cellsok = null;
	private Boolean pptok = null;
	private Boolean pdfsok = null;

	private AsposeLicense() {
		super();
	}

	public static AsposeLicense getInstance() {
		return asposeLicense;
	}

	public synchronized boolean setWordsLicense() {
		if(wordsok != null)
			return wordsok.booleanValue();
		InputStream is = AsposeLicense.class.getClassLoader().getResourceAsStream(licensePath);
		com.aspose.words.License aposeLic = new com.aspose.words.License();
		try {
			aposeLic.setLicense(is);
			wordsok = true;
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return wordsok;
	}

	synchronized public boolean setCellsLicense() {
		if(cellsok != null)
			return cellsok.booleanValue();
		InputStream is = AsposeLicense.class.getClassLoader().getResourceAsStream(licensePath);
		com.aspose.words.License aposeLic = new com.aspose.words.License();
		try {
			logger.error("[NW]AsposeLicense.call setCellsLicense!");
			aposeLic.setLicense(is);
			cellsok = true;
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return cellsok;
	}

	// FIXME by sunyp 2015-03-04 ppt的转换依赖1.6的jar包，代码编译基于1.5会报错。这个方法现在也没人用注释掉。
	// public synchronized boolean setPPTLicense() {
	// if (pptok != null)
	// return pptok.booleanValue();
	// InputStream is =
	// AsposeLicense.class.getClassLoader().getResourceAsStream(licensePath);
	// com.aspose.slides.License aposeLic = new com.aspose.slides.License();
	// try {
	// Logger.error("[NC]AsposeLicense.call setPPTLicense!");
	// aposeLic.setLicense(is);
	// pptok = true;
	// } catch (Exception e) {
	// Logger.error(e.getMessage(), e);
	// }
	// return pptok;
	// }

	synchronized public boolean setPdfLicense() {
		if(pdfsok != null)
			return pdfsok.booleanValue();
		InputStream is = AsposeLicense.class.getClassLoader().getResourceAsStream(licensePath);
		com.aspose.words.License aposeLic = new com.aspose.words.License();
		try {
			logger.error("[NW]AsposeLicense.call setPdfLicense!");
			aposeLic.setLicense(is);
			pdfsok = true;
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return pdfsok;
	}

}
