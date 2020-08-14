package com.tms.web.base;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nw.Global;
import org.nw.basic.util.DateUtils;
import org.nw.utils.ImageUtil;
import org.nw.utils.NWUtils;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.tms.service.base.CarService;

/**
 * 车辆管理
 * 
 * @author xuqc
 * @date 2012-7-22 下午10:51:56
 */
@Controller
@RequestMapping(value = "/base/car")
public class CarController extends AbsToftController {

	@Autowired
	private CarService carService;

	public CarService getService() {
		return carService;
	}

	protected String getUploadField() {
		return "photo";
	}
	
	protected String getOtherUseURL(){
		return getServletContext().getRealPath("/") + "\\images\\car_photo";
	}
	
	
}
