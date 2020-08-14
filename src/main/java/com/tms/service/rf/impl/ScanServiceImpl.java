package com.tms.service.rf.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.ExpAccidentConst;
import com.tms.constants.OperateTypeConst;
import com.tms.constants.TrackingConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.rf.ScanService;
import com.tms.service.te.TrackingService;
import com.tms.vo.base.AddressVO;
import com.tms.vo.rf.EntLineBarBVO;
import com.tms.vo.rf.RfTempBVO;
import com.tms.vo.rf.ScanVO;
import com.tms.vo.te.EntLineBVO;
import com.tms.vo.te.EntTrackingVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExpAccidentVO;

@Service
public class ScanServiceImpl extends TMSAbsBillServiceImpl implements ScanService {
	
	@Autowired
	private TrackingService trackingService;

	public List<ScanVO> scan(String lot, Integer operate_type) {
		if(StringUtils.isBlank(lot)){
			return null;
		}
		String sql = new String();
		if(operate_type != null){
			sql = "SELECT tel.lot,tetb.carno,tetb.pk_driver,telb.arrival_flag,telb.pk_ent_line_b,tc.carr_code,tc.carr_name ,ta.addr_code,ta.addr_name,SUM(telpb.NUM) AS num "
					+ " FROM ts_ent_line_b telb with(nolock) " + " LEFT JOIN ts_address  ta ON telb.pk_address=ta.pk_address  "
					+ " LEFT JOIN ts_ent_line_pack_b  telpb with(nolock)  ON telpb.pk_ent_line_b=telb.pk_ent_line_b  "
					+ " LEFT JOIN ts_entrust te with(nolock)  ON telb.pk_entrust=te.pk_entrust  "
					+ " LEFT JOIN ts_carrier  tc with(nolock)  ON tc.pk_carrier=te.pk_carrier  "
					+ " LEFT JOIN ts_entrust_lot tel with(nolock)  ON tel.lot=te.lot  "
					+ " LEFT JOIN (SELECT  ts_entrust.lot,ts_entrust.vbillno,ts_ent_transbility_b.pk_entrust,ts_ent_transbility_b.carno,ts_ent_transbility_b.pk_driver, "
					+ " Row_Number() OVER (partition by ts_entrust.lot ORDER BY ts_ent_transbility_b.carno asc) rank FROM ts_ent_transbility_b  with(nolock)  "
					+ " LEFT JOIN  ts_entrust with(nolock) ON ts_entrust.pk_entrust= ts_ent_transbility_b.pk_entrust  "
					+ " WHERE isnull(ts_ent_transbility_b.dr,0)=0 ) tetb ON tetb.lot=tel.lot  " + " and tetb.rank =1   "
					+ " WHERE telb.operate_type="+operate_type+"  " + " AND isnull(telb.dr,0)=0 " + " AND isnull(ta.dr,0)=0 "
					+ " AND isnull(telpb.dr,0)=0 " + " AND isnull(te.dr,0)=0 " + " AND isnull(tc.dr,0)=0 "
					+ " AND isnull(tel.dr,0)=0 " + " AND te.vbillstatus <>'24'  AND tel.lot = ? "
					+ " GROUP BY  telb.arrival_flag,tel.lot,tetb.carno,tetb.pk_driver,telb.pk_ent_line_b,ta.addr_code,ta.addr_name,tc.carr_code,tc.carr_name, telb.req_arri_date "
					+ " ORDER BY  telb.req_arri_date desc ";
		}else{
			sql = "SELECT tel.lot,tetb.carno,tetb.pk_driver,telb.arrival_flag,telb.pk_ent_line_b,tc.carr_code,tc.carr_name ,ta.addr_code,ta.addr_name,SUM(telpb.NUM) AS num "
					+ " FROM ts_ent_line_b telb with(nolock) " + " LEFT JOIN ts_address  ta ON telb.pk_address=ta.pk_address  "
					+ " LEFT JOIN ts_ent_line_pack_b  telpb with(nolock)  ON telpb.pk_ent_line_b=telb.pk_ent_line_b  "
					+ " LEFT JOIN ts_entrust te with(nolock)  ON telb.pk_entrust=te.pk_entrust  "
					+ " LEFT JOIN ts_carrier  tc with(nolock)  ON tc.pk_carrier=te.pk_carrier  "
					+ " LEFT JOIN ts_entrust_lot tel with(nolock)  ON tel.lot=te.lot  "
					+ " LEFT JOIN (SELECT  ts_entrust.lot,ts_entrust.vbillno,ts_ent_transbility_b.pk_entrust,ts_ent_transbility_b.carno,ts_ent_transbility_b.pk_driver, "
					+ " Row_Number() OVER (partition by ts_entrust.lot ORDER BY ts_ent_transbility_b.carno asc) rank FROM ts_ent_transbility_b  with(nolock)  "
					+ " LEFT JOIN  ts_entrust with(nolock) ON ts_entrust.pk_entrust= ts_ent_transbility_b.pk_entrust  "
					+ " WHERE isnull(ts_ent_transbility_b.dr,0)=0 ) tetb ON tetb.lot=tel.lot  " + " and tetb.rank =1   "
					+ " WHERE isnull(telb.dr,0)=0 " + " AND isnull(ta.dr,0)=0 "
					+ " AND isnull(telpb.dr,0)=0 " + " AND isnull(te.dr,0)=0 " + " AND isnull(tc.dr,0)=0 "
					+ " AND isnull(tel.dr,0)=0 " + " AND te.vbillstatus <>'24'  AND tel.lot = ? "
					+ " GROUP BY  telb.arrival_flag,tel.lot,tetb.carno,tetb.pk_driver,telb.pk_ent_line_b,ta.addr_code,ta.addr_name,tc.carr_code,tc.carr_name, telb.req_arri_date "
					+ " ORDER BY  telb.req_arri_date desc ";
		}
		//查询这个批次下，所有的提货信息
		
		List<ScanVO> results = NWDao.getInstance().queryForList(sql, ScanVO.class, lot);
		//将查询到的结果按地址分组，并将pk_ent_line_b用逗号合并起来
		List<ScanVO> returnResults = new ArrayList<ScanVO>();
		Map<String, List<ScanVO>> groupMap = new HashMap<String, List<ScanVO>>();
		for (ScanVO result : results) {
			String key = result.getAddr_code();
			List<ScanVO> voList = groupMap.get(key);
			if (voList == null) {
				voList = new ArrayList<ScanVO>();
				groupMap.put(key, voList);
			}
			voList.add(result);
		}
		for(String key : groupMap.keySet()){
			List<ScanVO> scanVOs = groupMap.get(key);
			Integer allNum = 0;
			String pk_ent_line_bs = new String();
			for(ScanVO scanVO : scanVOs){
				allNum = allNum + (scanVO.getNum() == null ? 0 : scanVO.getNum());
				if(StringUtils.isNotBlank(pk_ent_line_bs)){
					pk_ent_line_bs = pk_ent_line_bs + "," + scanVO.getPk_ent_line_b();
				}else{
					pk_ent_line_bs = scanVO.getPk_ent_line_b();
				}
			}
			ScanVO scanVO = new ScanVO();
			//取第1个scanVO，合并数量和PK，并且将处理后的结果放到返回结果集里。
			scanVO = scanVOs.get(0);
			scanVO.setNum(allNum);
			scanVO.setPk_ent_line_b(pk_ent_line_bs);
			returnResults.add(scanVO);
		}
		return returnResults;
	}

	public EntLineBarBVO[] getBarCodeVOS(String lot,  String pk_ent_line_b) {
		if(StringUtils.isBlank(lot) || StringUtils.isBlank(pk_ent_line_b)){
			return null;
		}
		//将pk_ent_line_bs按逗号分隔开，形成数组。
		String[] pkArrs = pk_ent_line_b.split("\\" + Constants.SPLIT_CHAR);
		//获取这个节点下的条码信息
		EntLineBVO entLineBVO = NWDao.getInstance().queryByPK(EntLineBVO.class, pkArrs[0]);
		EntLineBarBVO[] barBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntLineBarBVO.class, "lot =? and pk_address=? ",
				 lot,entLineBVO.getPk_address());
		return barBVOs;
	}
	
	public EntLineBVO getNextEntLineBVO(EntLineBVO entLineBVO){
		//找到这个节点的下一个节点 并且下一个点一定要是delivery，把多出的条码记录到下个卸货点，如果分段了，还要记到对应的提货点。
		EntLineBVO nextEntLineBVO = NWDao.getInstance().queryByCondition(EntLineBVO.class, "pk_entrust=? and operate_type =? and serialno =?", 
				entLineBVO.getPk_entrust(),OperateTypeConst.DELIVERY,entLineBVO.getSerialno()+10);
		//同一个委托单的下一个节点
		if(nextEntLineBVO != null){
			return nextEntLineBVO;
		}
		String sql = "SELECT * FROM ts_ent_line_b WITH(nolock) "
				+ "LEFT JOIN ts_entrust WITH(nolock) ON ts_ent_line_b.pk_entrust=ts_entrust.pk_entrust "
				+ "LEFT JOIN ts_ent_inv_b WITH(nolock) ON ts_ent_line_b.pk_entrust = ts_ent_inv_b.pk_entrust "
				+ "WHERE ts_entrust.dr=0  AND ts_entrust.vbillstatus <>24 AND ts_ent_inv_b.pk_invoice = "
				+ "(SELECT ts_ent_inv_b.pk_invoice FROM ts_ent_inv_b WITH(nolock) "
				+ "LEFT JOIN ts_ent_line_b WITH(nolock) ON ts_ent_inv_b.pk_entrust = ts_ent_line_b.pk_entrust "
				+ "WHERE ts_ent_line_b.pk_ent_line_b = ? AND isnull(ts_ent_inv_b.dr,0) = 0 AND isnull(ts_ent_line_b.dr,0) = 0) "
				+ "AND ts_ent_line_b.operate_type = ? AND isnull(ts_ent_inv_b.dr,0)=0 AND isnull(ts_ent_line_b.dr,0)=0 ";
		List<EntLineBVO> sameTypeEntLineBVOs = NWDao.getInstance().queryForList(sql, EntLineBVO.class,  entLineBVO.getPk_ent_line_b(),OperateTypeConst.PICKUP);
		if(sameTypeEntLineBVOs != null && sameTypeEntLineBVOs.size() > 0){
			for(EntLineBVO sameTypeEntLineBVO : sameTypeEntLineBVOs){
				if(sameTypeEntLineBVO.getPk_address().equals(entLineBVO.getPk_address())){
					//发货单分段产生的下一个节点
					return sameTypeEntLineBVO;
				}
			}
		}
		return null;
	}
	
	public List<EntLineBarBVO> saveBarCodes(EntLineBarBVO[] barBVOs,List<String> scanBarcodes, String pk_ent_line_b ,String lot){
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		List<EntLineBarBVO> EntLineBarBVOs = new ArrayList<EntLineBarBVO>();
		EntLineBVO entLineBVO = NWDao.getInstance().queryByPK(EntLineBVO.class, pk_ent_line_b);
				
		for(String barcode : scanBarcodes){
			boolean flag = true;
			for(EntLineBarBVO barBVO : barBVOs){
				if(barcode.substring(5).equals(barBVO.getBar_code())){
					barBVO.setStatus(VOStatus.UPDATED);
					flag = false;
				}
			}
			//这个条码在这个节点并不存在。 随机分配到某一个节点
			if(flag){
				EntLineBarBVO barBVO = new EntLineBarBVO();
				barBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(barBVO);
				barBVO.setPk_entrust(entLineBVO.getPk_entrust());
				barBVO.setPk_ent_line_b(entLineBVO.getPk_ent_line_b());
				barBVO.setBar_code(barcode.substring(5));
				barBVO.setLot(lot);
				barBVO.setExp_flag(UFBoolean.TRUE);
				barBVO.setPk_address(entLineBVO.getPk_address());
				toBeUpdate.add(barBVO);
				//多出的条码
				EntLineBarBVOs.add(barBVO);
				
				RfTempBVO rfTempBVO = new RfTempBVO();
				rfTempBVO.setStatus(VOStatus.NEW);
				rfTempBVO.setPk_address(entLineBVO.getPk_address());
				NWDao.setUuidPrimaryKey(rfTempBVO);
				rfTempBVO.setLot(lot);
				rfTempBVO.setIf_exp(UFBoolean.TRUE);
				
				toBeUpdate.add(rfTempBVO);
			}
		}
		List<ExpAccidentVO> expAccidentVOs = new ArrayList<ExpAccidentVO>();
		List<String> pk_entrusts = new ArrayList<String>();
		for(EntLineBarBVO barBVO : barBVOs){
			if(barBVO.getStatus() != VOStatus.UPDATED){
				//减少的条码
				barBVO.setStatus(VOStatus.DELETED);
				ExpAccidentVO accidentVO = new ExpAccidentVO();
				expAccidentVOs.add(accidentVO);
				accidentVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(accidentVO);
				//这里现将PK放在Vbillno的位置，下面会改掉
				accidentVO.setEntrust_vbillno(barBVO.getPk_entrust());
				accidentVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.YCSG));
				accidentVO.setStatus(BillStatus.NEW);
				accidentVO.setOrigin(ExpAccidentConst.ExpAccidentOrgin.TRACKING.toString());
				accidentVO.setCreate_time(new UFDateTime(new Date()));
				accidentVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
				accidentVO.setReason_memo(barBVO.getBar_code());
				accidentVO.setReason_type("7");
				accidentVO.setExp_type("10");
				toBeUpdate.add(barBVO);
				
				RfTempBVO rfTempBVO = new RfTempBVO();
				rfTempBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(rfTempBVO);
				rfTempBVO.setPk_entrust(barBVO.getPk_entrust());
				rfTempBVO.setPk_ent_line_b(barBVO.getPk_ent_line_b());
				rfTempBVO.setIf_exp(UFBoolean.TRUE);
				
				toBeUpdate.add(rfTempBVO);
			}else{
				//原有的条码
				EntLineBarBVOs.add(barBVO);
			}
		}
		if(pk_entrusts != null && pk_entrusts.size() > 0){
			EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, 
					"pk_entrust =" +NWUtils.buildConditionString(pk_entrusts.toArray(new String[pk_entrusts.size()])));
			if(entrustVOs != null && entrustVOs.length > 0){
				for(EntrustVO entrustVO : entrustVOs){
					for(ExpAccidentVO accidentVO : expAccidentVOs){
						if(entrustVO.getPk_entrust().equals(accidentVO.getEntrust_vbillno())){
							accidentVO.setEntrust_vbillno(entrustVO.getVbillno());
							accidentVO.setInvoice_vbillno(entrustVO.getInvoice_vbillno());
							accidentVO.setPk_carrier(entrustVO.getPk_carrier());
							accidentVO.setReason_memo("承运商："+entrustVO.getPk_carrier()+" 操作员："+WebUtils.getLoginInfo().getPk_user()
									+" 扫描条码："+accidentVO.getReason_memo()+"丢失");
							toBeUpdate.add(accidentVO);
						}
					}
				}
			}
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
		return EntLineBarBVOs;
	}
	
	public void updateNextEntLineBarBVO(Map<String, List<EntLineBarBVO>> groupMap, EntLineBVO[] entLineBVOs){
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		//1，查出下一个节点的地址
		List<String> pk_ent_line_bs = new ArrayList<String>();
		for(EntLineBVO entLineBVO :entLineBVOs){
			EntLineBVO nextEntLineBVO = this.getNextEntLineBVO(entLineBVO);
			if(nextEntLineBVO != null){
				//查找这个EntLineBVO 对应的lot。
				String sql = "select lot from ts_entrust with(nolock) where isnull(dr,0) = 0 and  pk_entrust = ?";
				String lot = NWDao.getInstance().queryForObject(sql, String.class, nextEntLineBVO.getPk_entrust());
				pk_ent_line_bs.add(nextEntLineBVO.getPk_ent_line_b());
				List<EntLineBarBVO> voList = groupMap.get(entLineBVO.getPk_ent_line_b());
				if(voList != null && voList.size() > 0){
					for(EntLineBarBVO entLineBarBVO : voList){
						EntLineBarBVO barBVO = new EntLineBarBVO();
						barBVO.setStatus(VOStatus.NEW);
						NWDao.setUuidPrimaryKey(barBVO);
						barBVO.setPk_entrust(nextEntLineBVO.getPk_entrust());
						barBVO.setPk_ent_line_b(nextEntLineBVO.getPk_ent_line_b());
						barBVO.setBar_code(entLineBarBVO.getBar_code());
						barBVO.setLot(lot);
						barBVO.setPk_address(nextEntLineBVO.getPk_address());
						barBVO.setPk_goods(entLineBarBVO.getPk_goods());
						barBVO.setGoods_code(entLineBarBVO.getGoods_code());
						barBVO.setGoods_name(entLineBarBVO.getGoods_name());
						
						barBVO.setDef1(entLineBarBVO.getDef1());
						barBVO.setDef2(entLineBarBVO.getDef2());
						barBVO.setDef3(entLineBarBVO.getDef3());
						barBVO.setDef4(entLineBarBVO.getDef4());
						barBVO.setDef5(entLineBarBVO.getDef5());
						barBVO.setDef6(entLineBarBVO.getDef6());
						barBVO.setDef7(entLineBarBVO.getDef7());
						barBVO.setDef8(entLineBarBVO.getDef8());
						barBVO.setDef9(entLineBarBVO.getDef9());
						barBVO.setDef10(entLineBarBVO.getDef10());
						barBVO.setDef11(entLineBarBVO.getDef11());
						barBVO.setDef12(entLineBarBVO.getDef12());
						
						toBeUpdate.add(barBVO);
					}
				}
			}
		}
		if(pk_ent_line_bs.size() > 0){
			String sql = "UPDATE ts_ent_line_bar_b SET dr = 1 WHERE dr=0 and  pk_ent_line_b IN " 
					+ NWUtils.buildConditionString(pk_ent_line_bs.toArray(new String[pk_ent_line_bs.size()]));
			NWDao.getInstance().getJdbcTemplate().execute(sql);
		}
		NWDao.getInstance().saveOrUpdate(toBeUpdate);
	}
	
	
	public Map<String, Object> insertTracking(ScanVO result,String type,AddressVO addrVO){
		String[] pkArrs = result.getPk_ent_line_b().split("\\" + Constants.SPLIT_CHAR);
		//通过pk_ent_line_bs 查找对应委托单
		String sql = "select distinct ts_entrust.* from ts_entrust with(nolock) left join ts_ent_line_b with(nolock) "
				+ "on ts_ent_line_b.pk_entrust = ts_entrust.pk_entrust "
				+ "where isnull(ts_entrust.dr,0) = 0 and isnull(ts_ent_line_b.dr,0) = 0 "
				+ "and ts_ent_line_b.pk_ent_line_b in " + NWUtils.buildConditionString(pkArrs);
		List<EntrustVO> entrustVOs = NWDao.getInstance().queryForList(sql, EntrustVO.class);
		String[] entVbillnoAry = new String[entrustVOs.size()];
		for(int i = 0; i < entrustVOs.size();i++){
			entVbillnoAry[i] = entrustVOs.get(i).getVbillno();
		}
		
		EntTrackingVO entTrackingVO = new EntTrackingVO();
		entTrackingVO.setStatus(VOStatus.NEW);
		entTrackingVO.setTracking_origin(DataDictConst.TRACK_ORIG.RF.intValue());
		NWDao.setNCPrimaryKey(entTrackingVO);
		entTrackingVO.setTracking_time(new UFDateTime(new Date()));
		entTrackingVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		entTrackingVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
		entTrackingVO.setCreate_time(new UFDateTime(new Date()));
		if("ar".equals(type)){
			//到达
			entTrackingVO.setTracking_status(TrackingConst.NODE_ARRI);
			entTrackingVO.setTracking_memo("RF到达"+addrVO.getAddr_name());
		}else if("le".equals(type)){
			//离开
			entTrackingVO.setTracking_status(TrackingConst.NODE_LEAV);
			entTrackingVO.setTracking_memo("RF离开"+addrVO.getAddr_name());
		}
			
		//调用跟踪信息方法
		return trackingService.batchSaveEntTracking(entTrackingVO, entVbillnoAry);
	}

	public String getBillType() {
		// TODO Auto-generated method stub
		return null;
	}

	public AggregatedValueObject getBillInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
