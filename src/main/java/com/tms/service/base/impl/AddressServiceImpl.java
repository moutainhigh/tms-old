package com.tms.service.base.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.service.impl.AbsBaseDataServiceImpl;
import org.nw.utils.NWUtils;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFDouble;
import org.springframework.stereotype.Service;

import com.tms.service.base.AddressService;
import com.tms.vo.base.AddrContactVO;
import com.tms.vo.base.AddressCalWinVO;
import com.tms.vo.base.AddressCapabilityVO;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.ExAggAddrVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-25 上午11:46:45
 */
@Service
public class AddressServiceImpl extends AbsBaseDataServiceImpl implements AddressService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggAddrVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, ExAggAddrVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AddressVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AddressVO.PK_ADDRESS);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggAddrVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, AddrContactVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, AddrContactVO.PK_ADDRESS);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_addr_contact");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_addr_contact");
			//yaojiie 2015 11 17 作业时间窗
			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggAddrVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, AddressCalWinVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, AddressCalWinVO.PK_ADDRESS);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, "ts_address_cal_win");
			childVO1.setAttributeValue(VOTableVO.VOTABLE, "ts_address_cal_win");
			//yaojiie 2015 11 17 地址能力
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggAddrVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, AddressCapabilityVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, AddressCapabilityVO.PK_ADDRESS);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, "ts_address_capability");
			childVO2.setAttributeValue(VOTableVO.VOTABLE, "ts_address_capability");
			CircularlyAccessibleValueObject[] childrenVO = { childVO,childVO1,childVO2};
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	public AddressVO getByCode(String code) {
		return dao.queryByCondition(AddressVO.class, "addr_code=? and isnull(locked_flag,'N')='N'", code);
	}

	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for(BillTempletBVO fieldVO : fieldVOs) {
			if(fieldVO.getPos().intValue() == UiConstants.POS[0] && fieldVO.getItemkey().equals("pk_city")) {
				fieldVO.setUserdefine3("area_level=5");
			}
		}
		return templetVO;
	}

	public String getCodeFieldCode() {
		return AddressVO.ADDR_CODE;
	}

	public AddressVO getByName(String name) {
		String sql = "select * from ts_address where WITH(NOLOCK) isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and addr_name=?";
		List<AddressVO> list = dao.queryForList(sql, AddressVO.class, name);
		if(list != null && list.size() > 0) {// 如果有多条记录，只返回第一条，通常不会
			return list.get(0);
		}
		return null;
	}
	//yaojiie 2015 11 19通过传入地址PK值，将解析好的经纬度信息添加到相应的地址VO中并存储到数据库中 并返回不能获取到经纬度地址的提示信息。
	public Map<String,String> getLongitude(String address){
		String key = "EMfamWxMfh0n812MGeVBImXn";
		BufferedReader bufferedReader = null;
		InputStreamReader streamReader = null;
		try {
			if(StringUtils.isNotBlanks(address)){
				address = URLEncoder.encode(address, "UTF-8");
			}else{
				return null;
			}
			java.net.URL url = new URL("http://api.map.baidu.com/geocoder/v2/?address="+ address +"&output=json&ak="+ key + "&callback=showLocation");
			streamReader = new InputStreamReader(url.openStream(), "UTF-8");
			bufferedReader = new BufferedReader(streamReader);
			String res;  
            StringBuilder sb = new StringBuilder("");  
            while((res = bufferedReader.readLine())!=null){  
                sb.append(res.trim());  
            }  
            String str = sb.toString();  
            if(StringUtils.isNotBlank(str)){  
                int lngStart = str.indexOf("lng\":");  
                int lngEnd = str.indexOf(",\"lat");  
                int latEnd = str.indexOf("},\"precise");  
                if(lngStart > 0 && lngEnd > 0 && latEnd > 0){  
                	Map<String,String> map = new HashMap<String, String>(); 
                    String lng = str.substring(lngStart+5, lngEnd);  
                    String lat = str.substring(lngEnd+7, latEnd);  
                    map.put("lng", lng);  
                    map.put("lat", lat);  
                    return map;  
                }  
            }
		} catch (IOException e) {
			throw new BusiException("IOException");
		}finally{
			try {
				if(bufferedReader != null){
					bufferedReader.close();//关闭流
				}
				if(streamReader != null){
					streamReader.close();
				}
			} catch (IOException e) {
				throw new BusiException("IOException");
			}
		}	
		return null;
	}

	//yaojiie 2015 11 19通过传入地址PK值，将解析好的经纬度信息添加到相应的地址VO中并存储到数据库中 并返回不能获取到经纬度地址的提示信息。
	public String addLongitude(String[] pk_addressS) {
		List<AddressVO> AddressVOs = new ArrayList<AddressVO>();
		String errorMesage = new String();
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class,
				"pk_address in " + NWUtils.buildConditionString(pk_addressS));
		for(AddressVO addressVO : addressVOs){
			Map<String,String> map = getLongitude(addressVO.getDetail_addr());
			if(map == null || map.size() == 0){
				errorMesage += addressVO.getAddr_code();
				continue;
			}else{
				addressVO.setLongitude(new UFDouble(map.get("lng")));
				addressVO.setLatitude(new UFDouble(map.get("lat")));
				addressVO.setStatus(VOStatus.UPDATED);
				AddressVOs.add(addressVO);
			}
		}
		if(AddressVOs !=null && AddressVOs.size() != 0){
			NWDao.getInstance().saveOrUpdate(AddressVOs.toArray(new AddressVO[AddressVOs.size()]));
		}
		if(StringUtils.isNotBlanks(errorMesage)){
			return errorMesage += "没有能获取到经纬度，请重新录入详细地址！";
		}
		return null;
	}
}
