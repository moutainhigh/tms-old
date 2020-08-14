package com.tms.web.inv;

import com.tms.service.inv.InvoiceTrackingService;
import com.tms.service.job.lbs.TrackVO;
import org.nw.basic.util.StringUtils;
import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by XIA on 2016/5/25.
 */
@Controller
@RequestMapping(value = "/inv/it")
public class InvoiceTrackingController extends AbsBillController{

    @Autowired
    private InvoiceTrackingService trackingService;

    @Override
    public InvoiceTrackingService getService() {

        return trackingService;
    }

    /**
     * 进入首页
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/toIndex.json")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {

        //获取订单信息
        String vbillno = request.getParameter("vbillno");

        if (StringUtils.isBlank(vbillno)){
            return new ModelAndView("/busi/inv/it.jsp");
        }
        Map<String,Object> result = this.getService().getInvAndTrackInfo(vbillno);
        if(result == null){
            return new ModelAndView("/busi/inv/it.jsp");
        }
        request.setAttribute("invoice",result.get("invoice"));
        request.setAttribute("trackings",result.get("trackings"));
        return new ModelAndView("/busi/inv/it.jsp");
    }


    /**
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/loadTrailsByVbillno.json")
    @ResponseBody
    public List<TrackVO> loadTrailsByVbillno(HttpServletRequest request, HttpServletResponse response){
        String vbillno = request.getParameter("vbillno");
        if(StringUtils.isBlank(vbillno)){
            return null;
        }
        return this.getService().loadTrailsByVbillno(vbillno);

    }



}
