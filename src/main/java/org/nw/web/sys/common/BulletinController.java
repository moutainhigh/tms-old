package org.nw.web.sys.common;

import org.nw.service.sys.BulletinService;
import org.nw.web.AbsToftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 公告
 * 
 * @author xuqc
 * @date 2013-7-3 上午09:28:07
 */
@Controller
@RequestMapping(value = "/common/bulletin")
public class BulletinController extends AbsToftController {

	@Autowired
	private BulletinService bulletinService;

	public BulletinService getService() {
		return bulletinService;
	}

}
