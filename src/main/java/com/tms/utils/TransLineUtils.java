package com.tms.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.utils.AreaHelper;
import org.nw.utils.NWUtils;

import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.LineNodeVO;
import com.tms.vo.base.TransLineVO;
import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.tp.SegmentVO;

/**
 * 线路工具类
 * @author XIA
 * @date 2016 8 5
 */
public class TransLineUtils {
	
	
	private static String addrSql = "SELECT ts_trans_line.* FROM ts_trans_line WITH(NOLOCK) "
			+ " LEFT JOIN ts_line_node WITH(NOLOCK) "
			+ " ON ts_trans_line.pk_trans_line = ts_line_node.pk_trans_line "
			+ " WHERE ("
			+ " (ts_trans_line.start_addr =? AND (ts_trans_line.end_addr=? OR ts_line_node.pk_address =?)) "
			+ " OR "
			+ " (ts_trans_line.end_addr=? AND (ts_trans_line.start_addr=? OR ts_line_node.pk_address =?))"
			+ " ) "
			+ " AND ts_trans_line.pk_corp =? "
			+ " AND line_type =? AND pk_trans_type =? AND isnull(ts_trans_line.dr,0)=0 AND isnull(ts_line_node.dr,0)=0  "
			+ " AND isnull(ts_trans_line.locked_flag,'N')='N'AND isnull(ts_line_node.locked_flag,'N')='N'";
	private static String areaSql = "SELECT ts_trans_line.* FROM ts_trans_line WITH(NOLOCK) "
			+ " WHERE ts_trans_line.start_addr =? AND ts_trans_line.end_addr=? "
			+ " AND line_type =? AND pk_trans_type =? AND ts_trans_line.pk_corp =? "
			+ " AND isnull(ts_trans_line.dr,0)=0 AND isnull(ts_trans_line.locked_flag,'N')='N'";
	/**
	 * 根据发货单，匹配线路
	 * @param targetLineType
	 * @param invoiceVO
	 * @return
	 */
	public static List<TransLineVO> matchTransLine(Integer targetLineType , InvoiceVO invoiceVO ){
		if(targetLineType == null || invoiceVO == null ){
			return null;
		}
		//先根据地址到地址直接匹配，匹配不到的时候，再向上匹配
		List<TransLineVO> transLineVOs = NWDao.getInstance().queryForList(addrSql, TransLineVO.class, 
				invoiceVO.getPk_delivery(),invoiceVO.getPk_arrival(),invoiceVO.getPk_arrival(),
				invoiceVO.getPk_delivery(),invoiceVO.getPk_arrival(),invoiceVO.getPk_arrival(),
				invoiceVO.getPk_corp(),targetLineType,invoiceVO.getPk_trans_type());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		//区域到区域的直接匹配
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, invoiceVO.getDeli_city(),invoiceVO.getArri_city(),targetLineType,invoiceVO.getPk_trans_type(),invoiceVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		String startAreaParent = getParentPkArea(invoiceVO.getDeli_city());
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startAreaParent,invoiceVO.getArri_city(),targetLineType,invoiceVO.getPk_trans_type(),invoiceVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		String endAreaParent = getParentPkArea(invoiceVO.getArri_city());
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, invoiceVO.getDeli_city(),endAreaParent,targetLineType,invoiceVO.getPk_trans_type(),invoiceVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startAreaParent,endAreaParent,targetLineType,invoiceVO.getPk_trans_type(),invoiceVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		//如果都没匹配到，返回空。
		return null;
	}
	
	/**
	 * 根据运段，匹配线路，先匹配地址，再匹配线路
	 * @param targetLineType
	 * @param segmentVO
	 * @return
	 */
	public static List<TransLineVO> matchTransLine(Integer targetLineType , SegmentVO segmentVO ){
		if(targetLineType == null || segmentVO == null ){
			return null;
		}
		//先根据地址到地址直接匹配，匹配不到的时候，再向上匹配
		List<TransLineVO> transLineVOs = NWDao.getInstance().queryForList(addrSql, TransLineVO.class, 
				segmentVO.getPk_delivery(),segmentVO.getPk_arrival(),segmentVO.getPk_arrival(),segmentVO.getPk_delivery(),segmentVO.getPk_delivery(),
				segmentVO.getPk_arrival(),segmentVO.getPk_corp(),targetLineType,segmentVO.getPk_trans_type());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		//区域到区域的直接匹配
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, segmentVO.getDeli_city(),segmentVO.getArri_city(),targetLineType,segmentVO.getPk_trans_type(),segmentVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		String startAreaParent = getParentPkArea(segmentVO.getDeli_city());
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startAreaParent,segmentVO.getArri_city(),targetLineType,segmentVO.getPk_trans_type(),segmentVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		String endAreaParent = getParentPkArea(segmentVO.getArri_city());
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, segmentVO.getDeli_city(),endAreaParent,targetLineType,segmentVO.getPk_trans_type(),segmentVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startAreaParent,endAreaParent,targetLineType,segmentVO.getPk_trans_type(),segmentVO.getPk_corp());
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		//如果都没匹配到，返回空。
		return null;
	}
	
	/**
	 * 根据起始地址和目的地址匹配线路
	 * 根据地址类型不同，支持地址到地址，和区域到区域匹配
	 * @param startAddrType
	 * @param stratAddr
	 * @param endAddrType
	 * @param endAddr
	 * @return
	 */
	public static List<TransLineVO> matchTransLine(Integer targetLineType ,String pk_corp, String pk_trans_type,Integer startAddrType , String stratAddr , Integer endAddrType , String endAddr){
		if(StringUtils.isBlank(pk_trans_type)
				||startAddrType == null || StringUtils.isBlank(stratAddr)
				|| endAddrType == null || StringUtils.isBlank(endAddr)
				|| !startAddrType.equals(endAddrType)){//当地址类型不同的时候，必然匹配不到线路
			return null;
		}
		//先根据地址到地址直接匹配，匹配不到的时候，再向上匹配
		List<TransLineVO> transLineVOs = NWDao.getInstance().queryForList(addrSql, TransLineVO.class, stratAddr,endAddr,endAddr,stratAddr,stratAddr,endAddr,pk_corp,targetLineType,pk_trans_type);
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		String[] addrs = new String[]{stratAddr,endAddr};
		AddressVO[] addressVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, "pk_address in " + NWUtils.buildConditionString(addrs));
		if(addressVOs == null || addressVOs.length != 2){
			return null;//地址信息有误
		}
		String startArea = "";
		String endArea = "";
		for(AddressVO addressVO : addressVOs){
			if(addressVO.getPk_address().equals(stratAddr)){
				startArea = addressVO.getPk_area();
			}else if(addressVO.getPk_address().equals(endAddr)){
				endArea = addressVO.getPk_area();
			}
		}
		//区域到区域的直接匹配
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startArea,endArea,targetLineType,pk_trans_type,pk_corp);
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		//获取区域到区域的父级进行匹配 这里默认都能找到父级区域，所以不对区域进行判断。
		String startAreaParent = getParentPkArea(startArea);
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startAreaParent,endArea,targetLineType,pk_trans_type,pk_corp);
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		String endAreaParent = getParentPkArea(endArea);
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startArea,endAreaParent,targetLineType,pk_trans_type,pk_corp);
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		transLineVOs = NWDao.getInstance().queryForList(areaSql, TransLineVO.class, startAreaParent,endAreaParent,targetLineType,pk_trans_type,pk_corp);
		if(transLineVOs != null && transLineVOs.size() > 0){
			return transLineVOs;
		}
		//如果都没匹配到，返回空。
		return null;
	}
	
	
	/**
	 * 根据发货单，匹配线路，只根据线路里的城市信息进行匹配，并且向上匹配直到匹配不到为止
	 * @param targetLineType
	 * @param segmentVO
	 * @return
	 */
	public static TransLineVO matchTransLineByArea(Integer targetLineType , InvoiceVO invoiceVO ){
		if(targetLineType == null || invoiceVO == null ){
			return null;
		}
		return matchTransLineByArea(targetLineType, invoiceVO.getDeli_city(), invoiceVO.getArri_city(), invoiceVO.getPk_corp(), invoiceVO.getPk_trans_type());
	}
	
	/**
	 * 根据运段，匹配线路，只根据线路里的城市信息进行匹配，并且向上匹配直到匹配不到为止
	 * @param targetLineType
	 * @param segmentVO
	 * @return
	 */
	public static TransLineVO matchTransLineByArea(Integer targetLineType , SegmentVO segmentVO ){
		if(targetLineType == null || segmentVO == null ){
			return null;
		}
		return matchTransLineByArea(targetLineType, segmentVO.getDeli_city(), segmentVO.getArri_city(), segmentVO.getPk_corp(), segmentVO.getPk_trans_type());
	}
	
	/**
	 * 根据城市，匹配线路，只根据线路里的城市信息进行匹配，并且向上匹配直到匹配不到为止
	 * @param targetLineType 匹配的目标线路类型
	 * @param strat_area 起始区域
	 * @param end_area 目的区域
	 * @param pk_corp 线路公司
	 * @param pk_trans_type 运输方式
	 * @return
	 */
	public static TransLineVO matchTransLineByArea(Integer targetLineType , String strat_area, String end_area ,String pk_corp,String pk_trans_type){
		if(targetLineType == null || StringUtils.isBlank(strat_area) || StringUtils.isBlank(end_area) || StringUtils.isBlank(pk_corp)){
			return null;
		}
		List<AreaVO> startAreaVOs = AreaHelper.getCurrentAreaVOWithParents(strat_area);
		String startAreas = "";
		if(startAreaVOs == null || startAreaVOs.size() == 0){
			return null;
		}else{
			for(AreaVO areaVO : startAreaVOs) {
				startAreas += "'";
				startAreas += areaVO.getPk_area();
				startAreas += "',";
			}
			if(startAreas.length() > 0) {
				startAreas = "(" + startAreas.substring(0, startAreas.length() - 1) + ")";
			}
		}
		List<AreaVO> endAreaVOs = AreaHelper.getCurrentAreaVOWithParents(end_area);
		String endAreas = "";
		if(endAreaVOs == null || endAreaVOs.size() == 0){
			return null;
		}else{
			for(AreaVO areaVO : endAreaVOs) {
				endAreas += "'";
				endAreas += areaVO.getPk_area();
				endAreas += "',";
			}
			if(endAreas.length() > 0) {
				endAreas = "(" + endAreas.substring(0, endAreas.length() - 1) + ")";
			}
		}
		String sql =  "SELECT ts_trans_line.* FROM ts_trans_line WITH(NOLOCK) "
				+ " LEFT JOIN ts_line_node WITH(NOLOCK) "
				+ " ON ts_trans_line.pk_trans_line = ts_line_node.pk_trans_line  AND isnull(ts_line_node.dr, 0) = 0 AND isnull(ts_line_node.locked_flag,'N')='N' "
				+ " WHERE ts_trans_line.start_addr in "+ startAreas
				+ " AND (ts_trans_line.end_addr in " + endAreas + " OR ts_line_node.pk_area in " + endAreas + ") "
				+ " AND ts_trans_line.pk_corp =? "
				+ " AND line_type =? AND pk_trans_type =? AND isnull(ts_trans_line.dr,0)=0   "
				+ " AND isnull(ts_trans_line.locked_flag,'N')='N' ";
		List<TransLineVO> transLineVOs = NWDao.getInstance().queryForList(sql, TransLineVO.class, pk_corp,targetLineType,pk_trans_type);
		if(transLineVOs == null || transLineVOs.size() == 0){
			return null;
		}
		//取出优先级最高的那个线路
		//1，找出起始区域等级最高的线路
		List<TransLineVO> stratAreaLeveLHighest = new ArrayList<TransLineVO>();
		for(AreaVO area : startAreaVOs){
			for(TransLineVO line : transLineVOs){
				if(area.getPk_area().equals(line.getStart_addr())){
					stratAreaLeveLHighest.add(line);
				}
			}
			if(stratAreaLeveLHighest.size() > 0){
				break;//如果匹配到了，更高层级的地址就不在匹配了。
			}
		}
		if(stratAreaLeveLHighest.size() ==1){
			return stratAreaLeveLHighest.get(0);
		}
		//2，可能存在多个相同起点的线路，这事找出目的或者中间节点点级别最高的线路
		List<String> pk_lines = new ArrayList<String>();
		for(TransLineVO line : stratAreaLeveLHighest){
			pk_lines.add(line.getPk_trans_line());
		}
		//中间节点的区域PK值
		LineNodeVO[] lineNodes = NWDao.getInstance().queryForSuperVOArrayByCondition(LineNodeVO.class,
				" pk_trans_line IN " + NWUtils.buildConditionString(pk_lines));
		//key 是线路 value 是这个线路的节点和终点的pk_area
		Map<TransLineVO,List<String>> lineAndNodeAndEndaddr = new HashMap<TransLineVO, List<String>>();
		for(TransLineVO line : stratAreaLeveLHighest){
			List<String> areas = new ArrayList<String>();
			//线路的终点
			areas.add(line.getEnd_addr());
			//线路的节点
			for(LineNodeVO node : lineNodes){
				if(node.getPk_trans_line().equals(line.getPk_trans_line())
						&& StringUtils.isNotBlank(node.getPk_area())){
					areas.add(node.getPk_area());
				}
			}
			lineAndNodeAndEndaddr.put(line, areas);
		}
		//对订单的终点所得到的区域，包括父辈区域，进行排序area_level最大的房子最前面，优先匹配(这个排序在AreaHelper里已经做了)
		//对于终点，按层级去匹配线路的终点和中间节点，当前层级如果没有匹配到，则向上匹配到可以匹配到为止
		for(AreaVO endArea : endAreaVOs){
			for(TransLineVO line : lineAndNodeAndEndaddr.keySet()){
				//这个线路对应的终点和中间节点的合集
				List<String> areas = lineAndNodeAndEndaddr.get(line);
				if(areas != null && areas.size() > 0){
					for(String pk_area : areas){
						//终点的pk和节点的PK是否一致 ,一致就不在查找，直接返回即可。
						if(pk_area.equals(endArea.getPk_area())){
							return line;
						}
					}
				}
			}
		}
		return stratAreaLeveLHighest.get(0);
	}
	

	public static String getParentPkArea(String pk_area){
		String sql = "SELECT  parent.pk_area FROM ts_area parent  "
				+ " LEFT JOIN ts_area children ON parent.pk_area=children.parent_id  "
				+ " WHERE "
				+ " isnull(parent.dr,0)=0 AND isnull(children.dr,0)=0 "
				+ " AND isnull(parent.locked_flag,'N') = 'N'  "
				+ " AND isnull(children.locked_flag,'N') = 'N' "
				+ " AND children.pk_area=? ";
		String parent_area = NWDao.getInstance().queryForObject(sql, String.class, pk_area);
		if(StringUtils.isBlank(parent_area)){
			parent_area = "";
		}
		return parent_area;
	}
	
	
	

}
