package org.nw.service.index.impl;

import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.index.IndexService;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;


/**
 * 后台首页的处理类
 * 
 * @author xuqc
 * @date 2012-6-16 下午02:14:26
 */
@Service
public class IndexServiceImpl extends AbsToftServiceImpl implements IndexService {

	public AggregatedValueObject getBillInfo() {
		return null;
	}
}
