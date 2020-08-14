package com.tms.service.inv.impl;

import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.inv.InvoiceTrackingService;
import com.tms.service.job.lbs.TrackVO;
import com.tms.service.te.impl.LBSUtils;
import com.tms.vo.inv.InvTrackingVO;
import com.tms.vo.inv.InvView;
import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.vo.api.RootVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.lang.UFDateTime;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Created by XIA on 2016/5/26.
 */
@Service
public class InvoiceTrackingServiceImpl extends TMSAbsBillServiceImpl implements InvoiceTrackingService {

	public String getBillType() {
		return null;
	}

	public AggregatedValueObject getBillInfo() {
		return null;
	}


    public Map<String, Object> getInvAndTrackInfo(String billno) {

        if(StringUtils.isBlank(billno)){
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();

        InvView[] invViews = NWDao.getInstance().queryForSuperVOArrayByCondition(InvView.class,"vbillno =?",billno);
        if (null == invViews || invViews.length == 0){
            return null;
        }
        List<Map<String,String>> trans = new ArrayList<Map<String,String>>();
        for(InvView invView : invViews){
            Map<String,String> tran = new HashMap<String,String>();
            if(StringUtils.isBlank(invView.getCarno())){
                continue;
            }
            tran.put("carno",invView.getCarno());
            tran.put("driver",invView.getDriver());
            tran.put("mobile",invView.getDriver_mobile());
            tran.put("carr_name",invView.getCarr_name());
            tran.put("gps_id",invView.getGps_id());
            tran.put("act_deli_date",invView.getAct_deli_date());
            tran.put("act_arri_date",invView.getAct_arri_date());
            trans.add(tran);
        }
        invViews[0].setTransbility(trans);
        if(trans.size() > 1){
            invViews[0].setCarno(trans.get(trans.size()-1).get("carno"));
        }

        result.put("invoice", invViews[0]);
        //获取跟踪信息
        InvTrackingVO[] invTrackingVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(InvTrackingVO.class,"invoice_vbillno =? and ISNULL (tracking_memo ,'')<>'' ",billno);
        //没有跟踪信息
        List<InvTrackingVO> trackings = new ArrayList<InvTrackingVO>();
        //将订单创建信息和确认信息放入跟踪信息里,这里倒着排序，界面上也会倒着排序
        if(invTrackingVOs == null || invTrackingVOs.length == 0){
            result.put("startLong",invViews[0].getDeli_longitude());
            result.put("startLat",invViews[0].getDeli_latitude());
            result.put("endLong",invViews[0].getArri_longitude());
            result.put("endLat",invViews[0].getArri_latitude());
        }else{
            //将invTrackingVOs按照时间排序
            InvTrackingVO[] sortTrackingVOs = bubble_sort(invTrackingVOs);
            trackings.addAll(Arrays.asList(sortTrackingVOs));
        }
//        if(!invViews[0].getVbillstatus().equals("新建")){
//            InvTrackingVO confirm = new InvTrackingVO();
//            confirm.setCreate_time(new UFDateTime(invViews[0].getConfirm_time()));
//            confirm.setCreate_user(invViews[0].getConfirm_user());
//            confirm.setTracking_memo(invViews[0].getConfirm_time() + "确认完成");
//            trackings.add(confirm);
//        }
//        InvTrackingVO create = new InvTrackingVO();
//        create.setCreate_time(new UFDateTime(invViews[0].getCreate_time()));
//        create.setCreate_user(invViews[0].getCreate_user());
//        create.setTracking_memo(invViews[0].getCreate_time() + "新建完成");
//        trackings.add(create);
        result.put("trackings",trackings);
        return result;
    }

    private InvTrackingVO[] bubble_sort(InvTrackingVO[] invTrackingVOs){
        int len = invTrackingVOs.length;
        //每次从后往前冒一个最小值，且每次能确定一个数在序列中的最终位置
        for (int i = 0; i < len-1; i++){         //比较n-1次
            boolean exchange = true;               //冒泡的改进，若在一趟中没有发生逆序，则该序列已有序
            for (int j = len-1; j >i; j--){    // 每次从后边冒出一个最小值
                if (invTrackingVOs[j].getTracking_time().after(invTrackingVOs[j-1].getTracking_time())){       //发生逆序，则交换
                    InvTrackingVO temp = new InvTrackingVO();
                    temp =  invTrackingVOs[j];
                    invTrackingVOs[j] = invTrackingVOs[j - 1];
                    invTrackingVOs[j - 1] =  temp;
                    exchange = false;
                }
            }
            if (exchange){
                return invTrackingVOs;
            }
        }
        return invTrackingVOs;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TrackVO> loadTrailsByVbillno(String vbillno){
        if(StringUtils.isBlank(vbillno)){
            return null;
        }
        String sql = "SELECT gps_id AS gpsid,deli_time AS startdate,arri_time AS enddate FROM ts_inv_view WHERE vbillno = ? ORDER BY deli_time";
        List<HashMap> gpsInfos = NWDao.getInstance().queryForList(sql, HashMap.class,vbillno);
        if(gpsInfos == null || gpsInfos.size() == 0){
            return null;
        }
        
        try {
       	 RootVO rootVO = LBSUtils.getCurrentTrackVO(gpsInfos);
       	 if(rootVO != null){
                return rootVO.getDataset();
            }
		} catch (Exception e) {
			logger.info("请求LBS数据出错");
		}
        return null;
    }

	

}
