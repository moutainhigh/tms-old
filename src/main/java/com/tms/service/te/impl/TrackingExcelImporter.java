package com.tms.service.te.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.exp.ExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.ImportConfigVO;
import org.nw.web.utils.SpringContextHolder;

import com.tms.service.te.EntrustService;
import com.tms.service.te.TrackingService;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;

/**
 * 异常跟踪导入
 * 
 * @author xuqc
 * @date 2013-11-26 下午04:14:16
 */
public class TrackingExcelImporter extends BillExcelImporter {
	
	private TrackingService trackingService = (TrackingService)SpringContextHolder.getBean("trackingService");

	public TrackingExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	protected SuperVO getParentVO() {
		return new EntTrackingVO();
	}
	
	public void _import(File file) throws Exception {
		List<AggregatedValueObject> aggVOs = resolve(file);
		for(AggregatedValueObject aggVO : aggVOs) {
			EntTrackingVO etVO = (EntTrackingVO) aggVO.getParentVO();
			if(StringUtils.isBlank(etVO.getEntrust_vbillno())) {
				continue;
			}
			EntrustVO entVO = NWDao.getInstance().queryByCondition(EntrustVO.class, "vbillno=?", etVO.getEntrust_vbillno());
			if(etVO.getNode_num() == null){
				trackingService.saveEntTracking(etVO, entVO, null, null, null);
			}else{
				String sql = "select * from ts_ent_line_b with(nolock) where pk_entrust=? and isnull(dr,0)=0 order by serialno";
				List<EntLineBVO> entLineBVOs = NWDao.getInstance().queryForList(sql,EntLineBVO.class, entVO.getPk_entrust());
				if(etVO.getNode_num() <= entLineBVOs.size() && etVO.getNode_num() > 0){
					for(int i=0;i<entLineBVOs.size();i++){
						if(i == etVO.getNode_num()-1){
							entLineBVOs.get(i).setAct_arri_date(etVO.getTracking_time().toString());
							trackingService.confirmArrival(entLineBVOs.get(i),0);
							break;
						}
					}
				}else{
					throw new BusiException("委托单[?]输入的节点号[?]超出范围！",entVO.getVbillno(),etVO.getNode_num()+"");
				}
			}
		}
	}
}
