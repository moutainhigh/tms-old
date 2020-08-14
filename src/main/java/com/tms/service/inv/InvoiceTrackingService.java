package com.tms.service.inv;

import com.tms.service.job.lbs.TrackVO;
import org.nw.service.IBillService;

import java.util.List;
import java.util.Map;

/**
 * Created by XIA on 2016/5/26.
 */
public interface InvoiceTrackingService  extends IBillService{


    public Map<String,Object> getInvAndTrackInfo (String billno);

    public List<TrackVO> loadTrailsByVbillno(String vbillno);

}
