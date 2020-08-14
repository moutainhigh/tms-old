package com.tms.service.te.impl;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.dao.helper.DaoHelper;
import org.nw.exception.BusiException;
import org.nw.jf.UiConstants;
import org.nw.jf.vo.BillTempletBVO;
import org.nw.jf.vo.UiBillTempletVO;
import org.nw.json.JacksonUtils;
import org.nw.utils.BillnoHelper;
import org.nw.utils.NWUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.pub.lang.UFDouble;
import org.nw.vo.sys.DataDictBVO;
import org.nw.vo.trade.pub.IExAggVO;
import org.nw.web.utils.ServletContextHolder;
import org.nw.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Service;

import com.tms.BillStatus;
import com.tms.constants.BillTypeConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.TMSAbsBillServiceImpl;
import com.tms.service.cm.PayDetailService;
import com.tms.service.te.VehicleBatchService;
import com.tms.services.peripheral.WebServicesUtils;
import com.tms.vo.base.ETCBVO;
import com.tms.vo.base.ETCVO;
import com.tms.vo.base.FuelCardBVO;
import com.tms.vo.base.FuelCardVO;
import com.tms.vo.cm.PayDetailBVO;
import com.tms.vo.cm.PayDetailVO;
import com.tms.vo.fleet.RefuelVO;
import com.tms.vo.fleet.RemiBVO;
import com.tms.vo.fleet.RemiVO;
import com.tms.vo.fleet.RepairBVO;
import com.tms.vo.fleet.RepairVO;
import com.tms.vo.fleet.TollBVO;
import com.tms.vo.fleet.TollVO;
import com.tms.vo.te.EntLotLineViewVO;
import com.tms.vo.te.EntLotPayDetailViewVO;
import com.tms.vo.te.EntLotTrackingViewVO;
import com.tms.vo.te.EntLotViewVO;
import com.tms.vo.te.EntTransbilityBVO;
import com.tms.vo.te.EntrustVO;
import com.tms.vo.te.ExAggVehicleBatchVO;
import com.tms.vo.te.VehiclePayVO;

@Service
public class VehicleBatchServiceImpl extends TMSAbsBillServiceImpl implements VehicleBatchService {
	
	private final String REFUEL_PK = "5c5baff594564fb38e64800bbbbd9c3c";
	private final String TOLL_PK = "d8ce5f58036c43f29ea292611ca4c70f";
	private final String REP_PK = "d8ce5f58036c43f29ea292611ca4c70f";
	
	@Autowired
	private PayDetailService payDetailService;

	public String getBillType() {
		return "";
	}
	
	private AggregatedValueObject billInfo;
	
	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new ExAggVehicleBatchVO();
			VOTableVO parentVO = new VOTableVO();
			parentVO.setAttributeValue(VOTableVO.BILLVO, ExAggVehicleBatchVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.HEADITEMVO, EntLotViewVO.class.getName());
			parentVO.setAttributeValue(VOTableVO.PKFIELD, EntLotViewVO.PK_ENTRUST_LOT_VIEW);
			billInfo.setParentVO(parentVO);
			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, ExAggVehicleBatchVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, EntrustVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, EntrustVO.LOT);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ts_entrust");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ts_entrust");
			
			VOTableVO childVO1 = new VOTableVO();
			childVO1.setAttributeValue(VOTableVO.BILLVO, ExAggVehicleBatchVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.HEADITEMVO, EntLotTrackingViewVO.class.getName());
			childVO1.setAttributeValue(VOTableVO.PKFIELD, EntLotTrackingViewVO.LOT);
			childVO1.setAttributeValue(VOTableVO.ITEMCODE, EntLotTrackingViewVO.TS_ENT_LOT_TRACKING_VIEW);
			childVO1.setAttributeValue(VOTableVO.VOTABLE, EntLotTrackingViewVO.TS_ENT_LOT_TRACKING_VIEW);
			
			VOTableVO childVO2 = new VOTableVO();
			childVO2.setAttributeValue(VOTableVO.BILLVO, ExAggVehicleBatchVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.HEADITEMVO, EntLotLineViewVO.class.getName());
			childVO2.setAttributeValue(VOTableVO.PKFIELD, EntLotLineViewVO.LOT);
			childVO2.setAttributeValue(VOTableVO.ITEMCODE, EntLotLineViewVO.TS_ENT_LOT_LINE_VIEW);
			childVO2.setAttributeValue(VOTableVO.VOTABLE, EntLotLineViewVO.TS_ENT_LOT_LINE_VIEW);
			
			VOTableVO childVO3 = new VOTableVO();
			childVO3.setAttributeValue(VOTableVO.BILLVO, ExAggVehicleBatchVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.HEADITEMVO, EntLotPayDetailViewVO.class.getName());
			childVO3.setAttributeValue(VOTableVO.PKFIELD, EntLotPayDetailViewVO.LOT);
			childVO3.setAttributeValue(VOTableVO.ITEMCODE, EntLotPayDetailViewVO.TS_ENT_LOT_PAY_DETAIL_VIEW);
			childVO3.setAttributeValue(VOTableVO.VOTABLE, EntLotPayDetailViewVO.TS_ENT_LOT_PAY_DETAIL_VIEW);

			CircularlyAccessibleValueObject[] childrenVO = { childVO,childVO1,childVO2,childVO3 };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}
	
	public UiBillTempletVO getBillTempletVO(String templateID) {
		UiBillTempletVO templetVO = super.getBillTempletVO(templateID);
		List<BillTempletBVO> fieldVOs = templetVO.getFieldVOs();
		for (BillTempletBVO fieldVO : fieldVOs) {
			if (fieldVO.getPos().intValue() == UiConstants.POS[0]) {
				if (fieldVO.getItemkey().equals("vbillstatus")) {
					fieldVO.setBeforeRenderer("vbillstatusBeforeRenderer");
				}
			}else if(fieldVO.getPos().intValue() == UiConstants.POS[1]){
				if(fieldVO.getTable_code().equals(TabcodeConst.TS_ENTRUST)){
					if(fieldVO.getItemkey().equals("vbillno")) {
						fieldVO.setRenderer("entrust_vbillnoRenderer");
					}
				}
			}
		}
		return templetVO;
	}
	
	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		if (paramVO.isBody() && TabcodeConst.TS_ENTRUST.equals(paramVO.getTabCode())) {
			// 根据行号排序
			return "order by req_deli_date,vbillno";
		}
		return super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
	}
	
	public AggregatedValueObject show(ParamVO paramVO) {
		AggregatedValueObject billVO = queryBillVO(paramVO);
		// 读取费用明细
		EntLotViewVO parentVO = (EntLotViewVO) billVO.getParentVO();
		EntrustVO[] entrustVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntrustVO.class, "lot=? order by req_deli_date,vbillno", parentVO.getLot());
		((IExAggVO) billVO).setTableVO(TabcodeConst.TS_ENTRUST, entrustVOs);
		return billVO;
	}

	public List<Map<String, Object>> getPackRecord(String id) {
		if(StringUtils.isBlank(id)){
			return new ArrayList<Map<String, Object>>();
		}
		String sql = "select * from ts_ent_pack_record_view where pk_entrust=?";
		List<Map<String, Object>> result = NWDao.getInstance().queryForList(sql, id);
		return result;
	}

	public Map<String,Object> authentication(String lot, String card_msg){
		if(StringUtils.isBlank(lot)){
			throw new BusiException("请选择批次！");
		}
		if(StringUtils.isBlank(card_msg)){
			throw new BusiException("没有获取到身份证信息！");
		}
		Map<String,Object> ret = new HashMap<String,Object>();
		@SuppressWarnings("unchecked")
		Map<String,String> card_Msg_map = JacksonUtils.readValue(card_msg, Map.class);
		String id = card_Msg_map.get("certNumber");
		ret.put("id", id);
		String bornDay = card_Msg_map.get("bornDay");
		ret.put("bornDay", bornDay);
		String certAddress = card_Msg_map.get("certAddress");
		ret.put("certAddress", certAddress);
		String certOrg = card_Msg_map.get("certOrg");
		ret.put("certOrg", certOrg);
		String effDate = card_Msg_map.get("effDate");
		ret.put("effDate", effDate);
		String expDate = card_Msg_map.get("expDate");
		ret.put("expDate", expDate);
		String gender = String.valueOf(card_Msg_map.get("gender"));
		if(gender.equals("1")){
			gender = "男";
		}else{
			gender = "女";
		}
		ret.put("gender", gender);
		String identityPic = card_Msg_map.get("identityPic");
		identityPic = identityPic.replaceAll(" ", "+");
		String nation = card_Msg_map.get("nation");
		ret.put("nation", nation);
		String partyName = card_Msg_map.get("partyName");
		ret.put("partyName", partyName);
		String sql = "SELECT distinct certificate_id,driver_name FROM ts_ent_transbility_b WITH(NOLOCK) WHERE isnull(certificate_id,'') <>''"
				+ " and isnull(driver_name,'') <>'' and isnull(dr,0)=0 and  lot=? ";
		List<Map<String, Object>> certificates = NWDao.getInstance().queryForList(sql,lot);
		
		if(certificates != null && certificates.size() > 0){
			List<String> certificate_ids = new ArrayList<String>();
			for(Map<String, Object> certificate : certificates){
				if(certificate != null && StringUtils.isNotBlank(String.valueOf(certificate.get("certificate_id"))) 
						&& !(String.valueOf(certificate.get("certificate_id")).equals("null"))){
					certificate_ids.add(String.valueOf(certificate.get("certificate_id")));
				}
			}
			//如果缺少身份证号，那么将整个身份证信息清空。
			if(certificate_ids.size() == 0){
				certificates = new ArrayList<Map<String, Object>>();
			}else{
				for(String certificate_id : certificate_ids){
					if(id.trim().equals(certificate_id)){
						//验证通过
						ret.put("checkUrl", "tg.png");
					}
				}
			}
		}
		ret.put("system", certificates);
		if(ret.get("checkUrl") == null){
			ret.put("checkUrl", "btg.png");
		}else if(ret.get("checkUrl").equals("tg.png")){
			//将运力信息的验证信息，记录到这个批次下的运力信息里
			EntTransbilityBVO[] transBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EntTransbilityBVO.class,
					"lot=?", lot);
			List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
			for(EntTransbilityBVO transBVO : transBVOs){
				if(transBVO.getCertificate_id() != null && transBVO.getCertificate_id().equals(id)){
					transBVO.setStatus(VOStatus.UPDATED);
					transBVO.setIf_checked(UFBoolean.TRUE);
					toBeUpdate.add(transBVO);
				}
			}
			NWDao.getInstance().saveOrUpdate(toBeUpdate);
		}
		
		//将照片信息传递到服务器
		String webappPath = ServletContextHolder.getRequest().getSession().getServletContext().getRealPath("/");
		String certificateDir = webappPath + "certificate";
		String filePath = certificateDir + File.separator + id + ".bmp";
		try {
			WebServicesUtils.convertBase64DataToImage(identityPic, filePath);
		} catch (IOException e) {
			logger.info("身份证图片上传服务器出错，ID：" + id);
		} catch (DecoderException e) {
			logger.info("身份证图片上传服务器编码解析出错，ID：" + id);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.info("身份证图片上传服务器编码解析出错，数组越界，ID：" + id + e.getMessage());
		} catch (Exception e) {
			logger.info("身份证图片出现未知异常，ID：" + id + e.getMessage());
		}
		ret.put("photoUrl", id+".bmp");
		return ret;
	}
	
	public List<Map<String,Object>> loadPayDetail(String lot) {
		if(StringUtils.isBlank(lot)){
			throw new BusiException("请选择单据！");
		}
		PayDetailVO[] payDetailVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(PayDetailVO.class, "pay_type=0 AND lot =? ",lot);
		if(payDetailVOs == null || payDetailVOs.length == 0){
			throw new BusiException("该批次缺少费用信息！");
		}
		for(PayDetailVO payDetailVO : payDetailVOs){
			if(payDetailVO.getVbillstatus() != BillStatus.NEW){
				throw new BusiException("应付明细[?]状态不是[新建]！",payDetailVO.getVbillno());
			}
		}
		String sql = "SELECT * FROM ( SELECT ts_pay_detail.lot, ts_expense_type.pk_expense_type,ts_pay_detail_b.valuation_type,ts_pay_detail_b.price_type, "
				+ "	ts_pay_detail_b.quote_type,ts_pay_detail_b.price, sum(ts_pay_detail_b.amount) AS amount,sum(ts_pay_detail_b.contract_amount) AS contract_amount,ts_pay_detail_b.system_create  "
				+ " FROM  ts_pay_detail WITH(NOLOCK) "
				+ " LEFT JOIN ts_pay_detail_b  WITH(NOLOCK) ON ts_pay_detail.pk_pay_detail=ts_pay_detail_b.pk_pay_detail AND  ts_pay_detail_b.dr=0 "
				+ " LEFT JOIN ts_expense_type WITH(NOLOCK) ON ts_pay_detail_b.pk_expense_type=ts_expense_type.pk_expense_type "
				+ " WHERE ts_pay_detail.dr=0 AND ts_pay_detail.lot IS NOT NULL "
				+ " GROUP BY   ts_pay_detail.lot,ts_expense_type.pk_expense_type,ts_pay_detail_b.valuation_type,ts_pay_detail_b.price_type,ts_pay_detail_b.quote_type,ts_pay_detail_b.price,ts_pay_detail_b.system_create "
				+ " ) TABLE_A WHERE pk_expense_type IS NOT NULL AND lot=?";
		List<Map<String,Object>> payDetailBVOs = NWDao.getInstance().queryForList(sql,lot);
		return payDetailBVOs;
	}

	public void saveVBLotPay(List<PayDetailBVO> detailBVOs, String lot) {
		if(StringUtils.isBlank(lot)){
			return;
		}
		String sql = "SELECT ts_pay_detail.pk_pay_detail FROM  ts_pay_detail WITH(NOLOCK)  "
				+ " WHERE isnull(ts_pay_detail.dr,0)=0 AND ts_pay_detail.pay_type=0 AND ts_pay_detail.lot =?";
		List<String> pkpds = NWDao.getInstance().queryForList(sql, String.class, lot);
		String deleteSql = "UPDATE ts_pay_detail_b SET dr = 1 WHERE pk_pay_detail IN " + NWUtils.buildConditionString(pkpds);
		NWDao.getInstance().update(deleteSql);
		//清除原有的费用
		payDetailService.saveLotPay(detailBVOs, pkpds.toArray(new String[pkpds.size()]));
	}

	public List<Map<String, Object>> loadVehiclePay(String lot) {
		//这个费用项是固定的，所以打开界面里也要将数据固定好，即没有的数据，我们也要补上。
		VehiclePayVO[] vehiclePayVOs = dao.queryForSuperVOArrayByCondition(VehiclePayVO.class, "lot=?", lot);
		List<VehiclePayVO> voList = new ArrayList<VehiclePayVO>();
		String sql = "SELECT * FROM nw_data_dict_b LEFT JOIN nw_data_dict ON "
				+ " nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
				+ " WHERE nw_data_dict.datatype_code =?";
		List<DataDictBVO> dataDictBVOs = dao.queryForList(sql,DataDictBVO.class, "vehicle_pay_type");
		if(dataDictBVOs == null || dataDictBVOs.size() == 0){
			throw new BusiException("vehicle_pay_type没有维护！");
		}
		for(DataDictBVO dataDictBVO : dataDictBVOs){
			boolean exist = false;
			for(VehiclePayVO vehiclePayVO : vehiclePayVOs){
				if(vehiclePayVO.getVehicle_pay_type().equals(dataDictBVO.getValue())){
					voList.add(vehiclePayVO);
					exist = true;
					break;
				}
			}
			if (!exist) {
				VehiclePayVO vehiclePayVO = new VehiclePayVO();
				vehiclePayVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(vehiclePayVO);
				vehiclePayVO.setVehicle_pay_type(dataDictBVO.getValue());
				voList.add(vehiclePayVO);
				dao.saveOrUpdate(vehiclePayVO);
			}
		}
		//转换数据格式
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		for(VehiclePayVO vo : voList){
			String[] attrs = vo.getAttributeNames();
			Map<String, Object> map = new HashMap<String, Object>();
			for(String attr : attrs){
				map.put(attr, vo.getAttributeValue(attr));
			}
			result.add(map);
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getKilometreAndDays(String lot) {
		String sql = "SELECT TOP 1 ts_vehicle_pay.kilometre,ts_vehicle_pay.days FROM ts_vehicle_pay WHERE isnull(dr,0)=0 AND lot = ?";
		return dao.queryForObject(sql, HashMap.class ,lot);
	}
	
	
	
	public void saveVehiclePay(List<VehiclePayVO> vehiclePayVOs) {
		if(vehiclePayVOs == null || vehiclePayVOs.size() == 0){
			return;
		}
		
		//查出批次信息
		String head_sql ="SELECT TOP 1 ts_ent_transbility_b.carno,ts_ent_transbility_b.pk_driver ,ts_ent_transbility_b.lot  "
				+ " FROM  ts_ent_transbility_b WITH(NOLOCK) "
				+ " WHERE ts_ent_transbility_b.dr=0 AND ts_ent_transbility_b.lot =?";
		@SuppressWarnings("unchecked")
		Map<String,String> carnoAndPk_driver = dao.queryForObject(head_sql, HashMap.class, vehiclePayVOs.get(0).getLot());
		if(carnoAndPk_driver == null || StringUtils.isBlank(carnoAndPk_driver.get("carno"))){
			throw new BusiException("缺少车辆信息!");
		}
		//插入记录，进行分摊。
		List<PayDetailBVO> detailBVOs = getSaveInfo(vehiclePayVOs.get(0).getLot(), carnoAndPk_driver.get("carno"), ""+ vehiclePayVOs.get(0).getDays(), ""+ vehiclePayVOs.get(0).getKilometre());
		for(VehiclePayVO vehiclePayVO : vehiclePayVOs){
			vehiclePayVO.setStatus(VOStatus.UPDATED);
			//1.看看有没有那条加油记录
			String sql = "SELECT nw_data_dict_b.def1 FROM nw_data_dict_b LEFT JOIN nw_data_dict ON "
					+ " nw_data_dict_b.pk_data_dict=nw_data_dict.pk_data_dict "
					+ " WHERE nw_data_dict.datatype_code ='vehicle_pay_type' and value=?";
			String pk = dao.queryForObject(sql, String.class, vehiclePayVO.getVehicle_pay_type());
			if(StringUtils.isBlank(pk)){
				continue;
			}
			if(vehiclePayVO.getAmount() == null || vehiclePayVO.getAmount().doubleValue() == 0){
				if(pk.equals(REFUEL_PK)){
					RefuelVO refuelVO = NWDao.getInstance().queryByCondition(RefuelVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
					if(refuelVO != null){
						dao.delete(refuelVO);
						FuelCardBVO fuelCardBVO = dao.queryByCondition(FuelCardBVO.class, "pk_refuel=?", refuelVO.getPk_refuel());
						if(fuelCardBVO != null){
							dao.delete(fuelCardBVO);
							FuelCardVO fuelCardVO = dao.queryByCondition(FuelCardVO.class, "pk_fuelcard=?", fuelCardBVO.getPk_fuelcard());
							if(fuelCardVO != null){
								fuelCardVO.setStatus(VOStatus.UPDATED);
								fuelCardVO.setAmount(fuelCardVO.getAmount() == null ? UFDouble.ZERO_DBL : fuelCardVO.getAmount());
								fuelCardVO.setAmount(fuelCardVO.getAmount().add(fuelCardBVO.getAmount()));
								dao.saveOrUpdate(fuelCardVO);
							}
						}
					}
				}else if(pk.equals(TOLL_PK)){
					TollBVO tollBVO = dao.queryByCondition(TollBVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
					if(tollBVO != null){
						TollVO tollVO = dao.queryByCondition(TollVO.class, "pk_toll=?", tollBVO.getPk_toll());
						if(tollVO != null){
							//看看还有没有其余的tollBVO 如果没有，直接把头表删除。
							TollBVO[] tollBVOs = dao.queryForSuperVOArrayByCondition(TollBVO.class, "pk_toll=? AND pk_toll_b <>?", tollBVO.getPk_toll(),tollBVO.getPk_toll_b());
							if(tollBVOs == null || tollBVOs.length == 0){
								tollVO.setStatus(VOStatus.DELETED);
							}else{
								tollVO.setStatus(VOStatus.UPDATED);
								tollVO.setCost_amount(tollVO.getCost_amount() == null ? UFDouble.ZERO_DBL : tollVO.getCost_amount());
								tollVO.setCost_amount(tollVO.getCost_amount().add(tollBVO.getAmount()));
							}
							dao.saveOrUpdate(tollVO);
						}
						dao.delete(tollBVO); 
						ETCBVO etcbvo = dao.queryByCondition(ETCBVO.class, "pk_toll_b=?", tollBVO.getPk_toll_b());
						if(etcbvo != null){
							dao.delete(etcbvo); 
							ETCVO etcvo = dao.queryByCondition(ETCVO.class, "pk_etc=?", etcbvo.getPk_etc());
							etcvo.setAmount(etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount());
							etcvo.setAmount(etcvo.getAmount().add(etcbvo.getAmount()));
							dao.saveOrUpdate(etcvo);
						}
					}
				}else if(pk.equals(REP_PK)){
					RepairBVO repairBVO = dao.queryByCondition(TollBVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
					if(repairBVO != null){
						RepairVO repairVO = dao.queryByCondition(RepairVO.class, "pk_repair=?", repairBVO.getPk_repair());
						if(repairVO != null){
							//看看还有没有其余的repairBVO 如果没有，直接把头表删除。
							RepairBVO[] repairBVOs = dao.queryForSuperVOArrayByCondition(RepairBVO.class, "pk_repair=? AND pk_repair_b <>?", repairBVO.getPk_repair(),repairBVO.getPk_repair_b());
							if(repairBVOs == null || repairBVOs.length == 0){
								repairVO.setStatus(VOStatus.DELETED);
							}else{
								repairVO.setStatus(VOStatus.UPDATED);
								repairVO.setCost_amount(repairVO.getCost_amount() == null ? UFDouble.ZERO_DBL : repairVO.getCost_amount());
								repairVO.setCost_amount(repairVO.getCost_amount().add(repairBVO.getAmount()));
							}
							dao.saveOrUpdate(repairVO);
						}
						dao.delete(repairBVO); 
					}
				}else{
					RemiBVO remiBVO = dao.queryByCondition(RemiBVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
					if(remiBVO != null){
						RemiVO remiVO = dao.queryByCondition(RemiVO.class, "pk_remi=?", remiBVO.getPk_remi());
						if(remiVO != null){
							RemiBVO[] remiBVOs = dao.queryForSuperVOArrayByCondition(RemiBVO.class, "pk_remi=? AND pk_remi_b <>?", remiBVO.getPk_remi(),remiBVO.getPk_remi_b());
							if(remiBVOs == null || remiBVOs.length == 0){
								remiVO.setStatus(VOStatus.DELETED);
							}else{
								remiVO.setStatus(VOStatus.UPDATED);
								remiVO.setCost_amount(remiVO.getCost_amount() == null ? UFDouble.ZERO_DBL : remiVO.getCost_amount());
								remiVO.setCost_amount(remiVO.getCost_amount().add(remiBVO.getAmount()));
							}
							dao.saveOrUpdate(remiVO);
						}
						dao.delete(remiBVO); 
					}
				}
				dao.saveOrUpdate(vehiclePayVO);
				continue;
			}
			if(pk.equals(REFUEL_PK)){
				refHandler(vehiclePayVO, carnoAndPk_driver);
			}else if(pk.equals(TOLL_PK)){
				tollHandler(vehiclePayVO, carnoAndPk_driver, pk);
			}else if(pk.equals(REP_PK)){
				repHandler(vehiclePayVO, carnoAndPk_driver, pk);
			}else{
				remiHandler(vehiclePayVO, carnoAndPk_driver, pk);
			}
			dao.saveOrUpdate(vehiclePayVO);
		}
		saveVBLotPay(detailBVOs, vehiclePayVOs.get(0).getLot());
	}
	
	public void refHandler(VehiclePayVO vehiclePayVO,Map<String,String> carnoAndPk_driver){
		//看看加油记录是否存在，如果不存在产生一条加油记录，否则更新。
		RefuelVO refuelVO = NWDao.getInstance().queryByCondition(RefuelVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
		if(refuelVO == null){
			refuelVO = new RefuelVO();
			refuelVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(refuelVO);
			refuelVO.setCreate_time(new UFDateTime(new Date()));
			refuelVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			refuelVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			refuelVO.setPk_vehicle_pay(vehiclePayVO.getPk_vehicle_pay());
			refuelVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.REF));
			refuelVO.setVbillstatus(BillStatus.NEW);
		}else{
			refuelVO.setStatus(VOStatus.UPDATED);
			refuelVO.setModify_time(new UFDateTime(new Date()));
			refuelVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		}
		refuelVO.setLot(vehiclePayVO.getLot());
		refuelVO.setPay_type(vehiclePayVO.getPay_type());
		refuelVO.setPk_fuelcard(vehiclePayVO.getPk_fuelcard());
		refuelVO.setRefuel_mileage(vehiclePayVO.getKilometre());
		refuelVO.setAmount(vehiclePayVO.getAmount());
		refuelVO.setMemo(vehiclePayVO.getMemo());
		refuelVO.setRefuel_qty(vehiclePayVO.getRefuel_qty());
		refuelVO.setCarno(carnoAndPk_driver.get("carno"));
		refuelVO.setPk_driver(carnoAndPk_driver.get("pk_driver"));
		dao.saveOrUpdate(refuelVO);
		//产生一条加油卡明细
		if(StringUtils.isBlank(vehiclePayVO.getPk_fuelcard())){
			return;
		}
		FuelCardVO fuelCardVO = dao.queryByCondition(FuelCardVO.class, "pk_fuelcard=?", vehiclePayVO.getPk_fuelcard());
		if(fuelCardVO != null){
			fuelCardVO.setStatus(VOStatus.UPDATED);
			FuelCardBVO fuelCardBVO = dao.queryByCondition(FuelCardBVO.class, "pk_refuel=?", refuelVO.getPk_refuel());
			double amount = 0;
			if(fuelCardBVO == null){
				fuelCardBVO = new FuelCardBVO();
				fuelCardBVO.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(fuelCardBVO);
				fuelCardBVO.setPk_refuel(refuelVO.getPk_refuel());
				fuelCardBVO.setPk_fuelcard(fuelCardVO.getPk_fuelcard());
				fuelCardVO.setStatus(VOStatus.UPDATED);
				amount = (fuelCardVO.getAmount() == null ? 0 : fuelCardVO.getAmount().doubleValue())
						-(vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue());
			}else{
				fuelCardBVO.setStatus(VOStatus.UPDATED);
				amount = (fuelCardVO.getAmount() == null ? 0 : fuelCardVO.getAmount().doubleValue())
						-(vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue())
						+(fuelCardBVO.getAmount() == null ? 0 : fuelCardBVO.getAmount().doubleValue());
				
			}
			fuelCardBVO.setAmount(vehiclePayVO.getAmount());
			fuelCardBVO.setMemo(vehiclePayVO.getMemo());
			fuelCardBVO.setOperat_date(new UFDateTime(new Date()));
			fuelCardBVO.setOperation_type(0);//加油 1，充值
			fuelCardBVO.setOperator(WebUtils.getLoginInfo().getPk_user());
			fuelCardBVO.setSystem_create(UFBoolean.TRUE);
			dao.saveOrUpdate(fuelCardBVO);
			fuelCardVO.setAmount(new UFDouble(amount));
			dao.saveOrUpdate(fuelCardVO);
		} 
	}
	
	public void tollHandler(VehiclePayVO vehiclePayVO,Map<String,String> carnoAndPk_driver,String pk_expense_type){
		TollVO tollVO = dao.queryByCondition(TollVO.class, "lot=?", vehiclePayVO.getLot());
		if(tollVO == null){
			tollVO = new TollVO();
			tollVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(tollVO);
			tollVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.TOLL));
			tollVO.setVbillstatus(BillStatus.NEW);
			tollVO.setCreate_time(new UFDateTime(new Date()));
			tollVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			tollVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
			tollVO.setLot(vehiclePayVO.getLot());
		}else{
			tollVO.setStatus(VOStatus.UPDATED);
			tollVO.setModify_time(new UFDateTime(new Date()));
			tollVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
			tollVO.setCost_amount(tollVO.getCost_amount() == null ? UFDouble.ZERO_DBL : tollVO.getCost_amount());
		}
		tollVO.setMemo(vehiclePayVO.getMemo());
		tollVO.setCarno(carnoAndPk_driver.get("carno"));
		tollVO.setPk_driver(carnoAndPk_driver.get("pk_driver"));
		TollBVO tollBVO = dao.queryByCondition(TollBVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
		if(tollBVO == null){
			tollVO.setCost_amount(vehiclePayVO.getAmount());
			tollBVO = new TollBVO();
			tollBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(tollBVO);
			tollBVO.setPk_toll(tollVO.getPk_toll());
		}else{
			double amount = 0;
			amount = (tollVO.getCost_amount() == null ? 0 : tollVO.getCost_amount().doubleValue())
					-(vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue())
					+(tollBVO.getAmount() == null ? 0 : tollBVO.getAmount().doubleValue());
			tollVO.setCost_amount(new UFDouble(amount));
			tollBVO.setStatus(VOStatus.UPDATED);
		}
		tollBVO.setAmount(vehiclePayVO.getAmount());
		tollBVO.setPay_type(vehiclePayVO.getPay_type());
		tollBVO.setPk_vehicle_pay(vehiclePayVO.getPk_vehicle_pay());
		tollBVO.setRegi_person(WebUtils.getLoginInfo().getPk_user());
		tollBVO.setRegi_time(new UFDateTime(new Date()));
		tollBVO.setPk_expense_type(pk_expense_type);
		tollBVO.setPayment_time(new UFDateTime(new Date()));
		tollBVO.setMemo(vehiclePayVO.getMemo());
		dao.saveOrUpdate(tollBVO);
		dao.saveOrUpdate(tollVO);
		//产生一条ETC卡明细
		if(StringUtils.isBlank(vehiclePayVO.getPk_etc())){
			return;
		}
		ETCVO etcvo = dao.queryByCondition(ETCVO.class, "pk_etc=?", vehiclePayVO.getPk_etc());
		if(etcvo != null){
			ETCBVO etcbvo = dao.queryByCondition(ETCBVO.class, "pk_toll=?", tollVO.getPk_toll());
			if(etcbvo == null){
				etcbvo = new ETCBVO();
				etcbvo.setStatus(VOStatus.NEW);
				NWDao.setUuidPrimaryKey(etcbvo);
				etcbvo.setPk_toll(tollVO.getPk_toll());
				etcvo.setAmount((etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount())
						.sub(tollVO.getCost_amount() == null ? UFDouble.ZERO_DBL : tollVO.getCost_amount()));
			}else{
				etcbvo.setStatus(VOStatus.UPDATED);
				etcvo.setAmount((etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount())
						.add(etcvo.getAmount() == null ? UFDouble.ZERO_DBL : etcvo.getAmount())
						.sub(tollVO.getCost_amount() == null ? UFDouble.ZERO_DBL : tollVO.getCost_amount()));
			}
			etcbvo.setAmount(tollVO.getCost_amount());
			etcbvo.setOperator(WebUtils.getLoginInfo().getPk_user());
			etcbvo.setOperation_type(0);
			etcbvo.setOperat_date(new UFDateTime(new Date()));
			etcbvo.setMemo(tollVO.getMemo());
			etcbvo.setSystem_create(UFBoolean.TRUE);
			dao.saveOrUpdate(etcbvo);
			etcvo.setStatus(VOStatus.UPDATED);
			dao.saveOrUpdate(etcvo);
		}
	}
	
	public void repHandler(VehiclePayVO vehiclePayVO,Map<String,String> carnoAndPk_driver,String pk_expense_type){
		//维修
		RepairVO repairVO = dao.queryByCondition(RepairVO.class, "lot=?", vehiclePayVO.getLot());
		if(repairVO == null){
			repairVO = new RepairVO();
			repairVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(repairVO);
			repairVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.REP));
			repairVO.setVbillstatus(BillStatus.NEW);
			repairVO.setLot(vehiclePayVO.getLot());
			repairVO.setCreate_time(new UFDateTime(new Date()));
			repairVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			repairVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		}else{
			repairVO.setStatus(VOStatus.UPDATED);
			repairVO.setModify_time(new UFDateTime(new Date()));
			repairVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		}
		repairVO.setCarno(carnoAndPk_driver.get("carno"));
		repairVO.setPk_driver(carnoAndPk_driver.get("pk_driver"));
		repairVO.setRepair_time(new UFDateTime(new Date()));
		double amount = 0;
		RepairBVO repairBVO = dao.queryByCondition(RepairBVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
		if(repairBVO == null){
			repairBVO = new RepairBVO();
			repairBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(repairBVO);
			repairBVO.setPk_repair(repairVO.getPk_repair());
			repairBVO.setPk_vehicle_pay(vehiclePayVO.getPk_vehicle_pay());
			amount = (repairVO.getCost_amount() == null ? 0 : repairVO.getCost_amount().doubleValue())
					+ (vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue());
		}else{
			repairBVO.setStatus(VOStatus.UPDATED);
			amount = (repairVO.getCost_amount() == null ? 0 : repairVO.getCost_amount().doubleValue())
					- (repairBVO.getAmount() == null ? 0 : repairBVO.getAmount().doubleValue())
					+ (vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue());
			
		}
		repairBVO.setAmount(vehiclePayVO.getAmount());
		repairBVO.setPk_expense_type(pk_expense_type);
		repairBVO.setMemo(vehiclePayVO.getMemo());
		dao.saveOrUpdate(repairBVO);
		repairVO.setCost_amount(new UFDouble(amount));
		dao.saveOrUpdate(repairVO);
	}
	
	public void remiHandler(VehiclePayVO vehiclePayVO,Map<String,String> carnoAndPk_driver,String pk_expense_type){
		RemiVO remiVO = dao.queryByCondition(RemiVO.class, "lot=?", vehiclePayVO.getLot());
		if(remiVO == null){
			remiVO = new RemiVO();
			remiVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(remiVO);
			remiVO.setVbillno(BillnoHelper.generateBillno(BillTypeConst.REM));
			remiVO.setVbillstatus(BillStatus.NEW);
			remiVO.setLot(vehiclePayVO.getLot());
			remiVO.setCreate_time(new UFDateTime(new Date()));
			remiVO.setCreate_user(WebUtils.getLoginInfo().getPk_user());
			remiVO.setPk_corp(WebUtils.getLoginInfo().getPk_corp());
		}else{
			remiVO.setStatus(VOStatus.UPDATED);
			remiVO.setModify_time(new UFDateTime(new Date()));
			remiVO.setModify_user(WebUtils.getLoginInfo().getPk_user());
		}
		remiVO.setCarno(carnoAndPk_driver.get("carno"));
		remiVO.setRegi_time(new UFDateTime(new Date()));
		remiVO.setRegi_person(WebUtils.getLoginInfo().getPk_user());
		double amount = 0;
		RemiBVO remiBVO = dao.queryByCondition(RemiBVO.class, "pk_vehicle_pay=?", vehiclePayVO.getPk_vehicle_pay());
		if(remiBVO == null){
			remiBVO = new RemiBVO();
			remiBVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(remiBVO);
			remiBVO.setPk_remi(remiVO.getPk_remi());
			remiBVO.setPk_vehicle_pay(vehiclePayVO.getPk_vehicle_pay());
			amount = (remiVO.getCost_amount() == null ? 0 : remiVO.getCost_amount().doubleValue())
					+ (vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue());
		}else{
			remiBVO.setStatus(VOStatus.UPDATED);
			amount = (remiVO.getCost_amount() == null ? 0 : remiVO.getCost_amount().doubleValue())
					- (remiBVO.getAmount() == null ? 0 : remiBVO.getAmount().doubleValue())
					+ (vehiclePayVO.getAmount() == null ? 0 : vehiclePayVO.getAmount().doubleValue());
			
		}
		remiBVO.setAmount(vehiclePayVO.getAmount());
		remiBVO.setPk_expense_type(pk_expense_type);
		remiBVO.setMemo(vehiclePayVO.getMemo());
		dao.saveOrUpdate(remiBVO);
		remiVO.setCost_amount(new UFDouble(amount));
		dao.saveOrUpdate(remiVO);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<PayDetailBVO> getSaveInfo(String lot,String carno,String days,String kilometre ){
		final List<PayDetailBVO> RETURN = new ArrayList<PayDetailBVO>();
		final String PROC_NAME = "ts_vehicle_pay_proc";
		final String LOT = lot;
		final String CARNO = carno;
		final String DAYS = days;
		final String KILOMETRE = kilometre;
		final String USER = WebUtils.getLoginInfo().getPk_user();
		final String EMPTY = "";
		
		try {
			NWDao.getInstance().getJdbcTemplate().execute(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					// 设置存储过程参数
					int count = 6;
					String storedProc = DaoHelper.getProcedureCallName(PROC_NAME, count);
					CallableStatement cs = conn.prepareCall(storedProc);
					cs.setString(1, LOT);
					cs.setString(2, CARNO);
					cs.setString(3, DAYS);
					cs.setString(4, KILOMETRE);
					cs.setString(5, USER);
					cs.setString(6, EMPTY);
					return cs;
				}
			}, new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
					// 查询结果集
					ResultSet rs = cs.executeQuery();
					while (rs.next()) {
						String pk_expense_type = rs.getString("pk_expense_type");
						String amount = rs.getString("amount");
						String memo = rs.getString("memo");
						String price = rs.getString("price");
						PayDetailBVO payDetailBVO = new PayDetailBVO();
						payDetailBVO.setAmount(new UFDouble(amount));
						payDetailBVO.setPk_expense_type(pk_expense_type);
						payDetailBVO.setPrice(new UFDouble(price));
						payDetailBVO.setMemo(memo);
						RETURN.add(payDetailBVO);
					}
					cs.close();
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RETURN;
	}
	
	
	public void saveRefAndToll(){
		
		
		
		
		
		
	}
	
}
