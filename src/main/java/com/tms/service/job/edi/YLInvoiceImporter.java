/**
 * 
 */
package com.tms.service.job.edi;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nw.BillStatus;
import org.nw.constants.Constants;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.job.IJobService;
import org.nw.utils.PropertiesUtils;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.pub.lang.UFBoolean;
import org.nw.vo.pub.lang.UFDate;
import org.nw.vo.pub.lang.UFDateTime;
import org.nw.vo.sys.JobDefVO;
import org.nw.web.utils.SpringContextHolder;
import org.nw.web.utils.WebUtils;

import com.tms.constants.AddressConst;
import com.tms.constants.DataDictConst;
import com.tms.constants.FunConst;
import com.tms.constants.SegmentConst;
import com.tms.constants.TabcodeConst;
import com.tms.service.inv.InvoiceService;
import com.tms.service.inv.impl.InvoiceUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.CustAddrVO;
import com.tms.vo.edi.EdiHisInvPackBVO;
import com.tms.vo.edi.EdiHisInvoiceVO;
import com.tms.vo.edi.EdiInvPackBVO;
import com.tms.vo.edi.EdiInvoiceVO;
import com.tms.vo.inv.ExAggInvoiceVO;
import com.tms.vo.inv.InvPackBVO;
import com.tms.vo.inv.InvoiceVO;

/**
 * 裕络发货单导入，从中间表导入发货单数据
 * 
 * @author xuqc
 * @Date 2015年5月26日 下午9:08:28
 *
 */
public class YLInvoiceImporter implements IJobService {

	Logger logger = Logger.getLogger(this.getClass());

	private String YLCORP_PROPERTIES = "ylcorp.properties";
	private String YLCITY_PROPERTIES = "ylcity.properties";

	private Map<String, String> custMap = new HashMap<String, String>();// 客户名称和pk的缓冲，避免在一次任务执行中重复查询，但是任务结束后需要清空数据
	private Map<String, String> transTypeMap = new HashMap<String, String>();// 运输方式
	private Map<String, String> addrMap = new HashMap<String, String>();// 地址，提货方，到货方
	private Map<String, String> areaMap = new HashMap<String, String>();// 城市，省份，区域
	private Map<String, String> contactMap = new HashMap<String, String>();// 城市，省份，区域
	private Map<String, String> packMap = new HashMap<String, String>();// 城市，省份，区域
	private Map<String, String> corpMap = new HashMap<String, String>();// 公司

	StringBuffer mess = new StringBuffer();// 每个发货单进行导入时的错误信息
	String pk_customer_error = "客户或结算客户不能为空";
	String pk_trans_type_error = "运输方式不能为空";
	String req_deli_date_error = "要求提货日期不能为空";
	String req_arri_date_error = "要求到货日期不能为空";
	String pk_delivery_error = "提货方不能为空";
	String pk_arrival_error = "到货方不能为空";
	String goods_code_error = "货品编码不能为空";
	String goods_name_error = "货品名称不能为空";

	private InvoiceService service;
	private ParamVO paramVO;

	Properties ylcorpProp = null;
	Properties ylcityProp = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nw.job.IJobService#exec(org.nw.vo.sys.JobDefVO)
	 */
	public void exec(JobDefVO jobDefVO) {
		logger.info("-------------------开始执行导入发货单任务-------------------");
		logger.info("从中间表edi_invoice查询需要导入的发货单");
		String EdiSql = "select top 1500 * from edi_invoice WITH(NOLOCK) WHERE  isnull(dr,0)=0 and isnull(syncexp_flag,'N')='N'";
		List<EdiInvoiceVO> invVOs = NWDao.getInstance().queryForList(EdiSql, EdiInvoiceVO.class);
		if(invVOs != null && invVOs.size()> 0) {
			logger.info("共查询到" + invVOs.size() + "条发货单");
			for(EdiInvoiceVO ediVO : invVOs) {
				logger.info("开始导入第一条发货单，源单号：" + ediVO.getPk_invoice());
				mess.setLength(0);
				// 查询明细表数据
				logger.info("查询发货单对应的货品信息");
				EdiInvPackBVO[] ediPackBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(EdiInvPackBVO.class,
						"isnull(dr,0)=0 and isnull(syncexp_flag,'N')='N' and pk_invoice=?", ediVO.getPk_invoice());
				if(ediPackBVOs == null || ediPackBVOs.length == 0) {
					// 没有明细数据，属于异常情况。
					// 2015-05-28
					// 主表和子表使用两个触发器来同步，可能时间上有差别，正常情况下，99%以上都会存在子表数据的
					logger.info("没有查询到对应的货品信息");
					// ediVO.setStatus(VOStatus.UPDATED);
					// ediVO.setSyncexp_flag(UFBoolean.TRUE);
					// ediVO.setSyncexp_memo("没有货品信息，无法同步发货单：" +
					// ediVO.getPk_invoice());
					// ediVO.setSync_time(new UFDateTime(new Date()));
					// NWDao.getInstance().saveOrUpdate(ediVO);
				} else {
					logger.info("共查询到" + ediPackBVOs.length + "条货品信息");
					// 转换发货单表头
					logger.info("开始转换发货单信息");
					InvoiceVO invVO = convert(ediVO);
					if(mess.length() > 0) {
						// 存在错误信息
						logger.info("存在错误信息，无法同步发货单：" + ediVO.getPk_invoice() + ",错误信息：" + mess.toString());
						ediVO.setStatus(VOStatus.UPDATED);
						ediVO.setSyncexp_flag(UFBoolean.TRUE);
						ediVO.setSyncexp_memo(mess.toString());
						ediVO.setSync_time(new UFDateTime(new Date()));
						NWDao.getInstance().saveOrUpdate(ediVO);
					} else {
						logger.info("已将接口的发货单信息转换成发货单VO");
						logger.info("开始转换发货单货品信息");
						mess.setLength(0);
						List<InvPackBVO> packBVOs = new ArrayList<InvPackBVO>();
						for(EdiInvPackBVO ediPackVO : ediPackBVOs) {
							InvPackBVO packVO = convert(ediPackVO);
							packVO.setStatus(VOStatus.NEW);
							packBVOs.add(packVO);
						}
						if(mess.length() > 0) {
							logger.info("存在错误信息，无法同步发货单货品，发货单号：" + ediVO.getPk_invoice() + ",错误信息：" + mess.toString());
							ediVO.setStatus(VOStatus.UPDATED);
							ediVO.setSyncexp_flag(UFBoolean.TRUE);
							ediVO.setSyncexp_memo(mess.toString());
							ediVO.setSync_time(new UFDateTime(new Date()));
							NWDao.getInstance().saveOrUpdate(ediVO);
						} else {
							if(packBVOs.size() == 0) {
								logger.info("不存在合格的货品明细，无法同步发货单：" + ediVO.getPk_invoice());
								ediVO.setStatus(VOStatus.UPDATED);
								ediVO.setSyncexp_flag(UFBoolean.TRUE);
								ediVO.setSyncexp_memo(mess.toString());
								ediVO.setSync_time(new UFDateTime(new Date()));
								NWDao.getInstance().saveOrUpdate(ediVO);
							} else {
								logger.info("已将发货单货品信息转换成货品VO");

								// 校验客户订单号必须唯一
								String sql = "select 1 from ts_invoice WITH(NOLOCK) where isnull(dr,0)=0  and orderno='1' and cust_orderno=?";
								Integer count = NWDao.getInstance().queryForObject(sql, Integer.class,
										ediVO.getCust_orderno());
								if(count != null && count > 0) {
									String msg = "客户订单号已经存在，不可以再次同步";
									logger.info(msg);
									ediVO.setStatus(VOStatus.UPDATED);
									ediVO.setSync_time(new UFDateTime(new Date()));
									ediVO.setSyncexp_flag(UFBoolean.TRUE);
									ediVO.setSyncexp_memo(msg);
									NWDao.getInstance().saveOrUpdate(ediVO);
								} else {
									// 设置表头的一些基本信息
									invVO.setVbillstatus(BillStatus.NEW);
									invVO.setOrder_time(new UFDateTime(new Date())); // 接单时间
									invVO.setDbilldate(new UFDate(new Date()));
									invVO.setStatus(VOStatus.NEW);

									ExAggInvoiceVO aggVO = new ExAggInvoiceVO();
									aggVO.setParentVO(invVO);
									aggVO.setTableVO(TabcodeConst.TS_INV_PACK_B,
											packBVOs.toArray(new InvPackBVO[packBVOs.size()]));
									// 表体的数据合计到表头
									InvoiceUtils.setHeaderCount(aggVO, null, false);
									logger.info("调用发货单的服务类，执行保存！");
									service.save(aggVO, paramVO);
									logger.info("发货单已同步，发货单号：" + ediVO.getPk_invoice());
									// 删除中间表数据，并将中间表数据移到历史表中
									//logger.info("删除中间表的数据，并将中间表数据移动到历史表中，发货单号：" + ediVO.getPk_invoice());
									//NWDao.getInstance().delete(ediVO, false);
//									EdiHisInvoiceVO hisVO = convertToHis(ediVO);
//									hisVO.setStatus(VOStatus.NEW);
//									NWDao.setUuidPrimaryKey(hisVO);
//									NWDao.getInstance().saveOrUpdate(hisVO);
//									for(EdiInvPackBVO ediPackVO : ediPackBVOs) {
//										NWDao.getInstance().delete(ediPackVO, false);
//										EdiHisInvPackBVO hisPackVO = convertToHis(ediPackVO);
//										hisPackVO.setPk_invoice(hisVO.getPk_invoice());
//										hisPackVO.setStatus(VOStatus.NEW);
//										NWDao.setUuidPrimaryKey(hisPackVO);
//										NWDao.getInstance().saveOrUpdate(hisPackVO);
//									}
								}
							}
						}
					}
				}
			}
		}
		logger.info("-------------------导入发货单任务执行完毕-------------------");
	}

	/**
	 * 转换表头
	 * 
	 * @param ediVO
	 * @return
	 */
	public InvoiceVO convert(EdiInvoiceVO ediVO) {
		InvoiceVO invVO = new InvoiceVO();
		invVO.setCust_orderno(ediVO.getCust_orderno());
		invVO.setOrderno(ediVO.getOrderno());
		// 客户名称，根据客户名称查询具体的客户pk
		invVO.setPk_customer(getCustPK(ediVO.getPk_customer()));
		// 结算客户
		invVO.setBala_customer(getCustPK(ediVO.getBala_customer()));
		// 运输方式
		invVO.setPk_trans_type(getTransTypePK(ediVO.getPk_trans_type()));
		// 要求提货日期
		if(StringUtils.isBlank(ediVO.getReq_deli_date())) {
			mess.append("|");
			mess.append(req_deli_date_error);
		} else {
			invVO.setReq_deli_date(ediVO.getReq_deli_date());
		}
		// 要求到货日期
		if(StringUtils.isBlank(ediVO.getReq_arri_date())) {
			mess.append("|");
			mess.append(req_arri_date_error);
		} else {
			invVO.setReq_arri_date(ediVO.getReq_arri_date());
		}
		// 合同到货日期=要求到货日期
		invVO.setCon_arri_date(invVO.getReq_arri_date());

		// 提货城市
		invVO.setDeli_city(getDeliCityPK(ediVO));
		// 提货省份
		invVO.setDeli_province(getParentArea(invVO.getDeli_city()));
		invVO.setDeli_area(getParentArea(invVO.getDeli_province()));
		// 提货方联系人
		invVO.setDeli_contact(ediVO.getDeli_contact());
		invVO.setDeli_mobile(ediVO.getDeli_mobile());
		invVO.setDeli_phone(ediVO.getDeli_phone());
		invVO.setDeli_email(ediVO.getDeli_email());
		invVO.setDeli_detail_addr(ediVO.getDeli_detail_addr());
		// 提货方,需要使用到提货城市等，所以放到最后
		invVO.setPk_delivery(getDeliOrArriPK(invVO, ediVO.getPk_delivery(), ediVO.getDeli_code(), true));

		// 收货城市
		invVO.setArri_city(getArriCityPK(ediVO));
		// 收货省份
		invVO.setArri_province(getParentArea(invVO.getArri_city()));
		invVO.setArri_area(getParentArea(invVO.getArri_province()));
		// 收货方联系人
		invVO.setArri_contact(ediVO.getArri_contact());
		invVO.setArri_mobile(ediVO.getArri_mobile());
		invVO.setArri_phone(ediVO.getArri_phone());
		invVO.setArri_email(ediVO.getArri_email());
		invVO.setArri_detail_addr(ediVO.getArri_detail_addr());
		// 收货方,需要使用到收货城市等，所以放到最后
		invVO.setPk_arrival(getDeliOrArriPK(invVO, ediVO.getPk_arrival(), ediVO.getArri_code(), false));

		// 提货注意事项和到货注意事项
		invVO.setDeli_process(getDeliProcess(invVO.getPk_delivery()));
		invVO.setArri_process(getArriProcess(invVO.getPk_arrival()));

		invVO.setCreate_user(ediVO.getCreate_user());
		invVO.setCreate_time(new UFDateTime(new Date()));
		invVO.setInvoice_origin(DataDictConst.INVOICE_ORIGIN.BZDR.intValue());// 标准导入
		invVO.setPk_corp(getCorpPK(invVO, ediVO));
		invVO.setDef1(getSegmentType(ediVO));// 提货段，干线段，收货段
		invVO.setDef2(ediVO.getDef2());
		invVO.setDef3(ediVO.getDef3());
		invVO.setDef4(ediVO.getDef4());
		invVO.setDef5(ediVO.getDef5());
		invVO.setDef6(ediVO.getDef6());
		invVO.setDef7(ediVO.getDef7());
		invVO.setDef8(ediVO.getDef8());
		invVO.setDef9(ediVO.getDef9());
		invVO.setDef10(ediVO.getDef10());
		invVO.setDef11(ediVO.getDef11());
		invVO.setDef12(ediVO.getDef12());
		return invVO;
	}

	public InvPackBVO convert(EdiInvPackBVO ediVO) {
		InvPackBVO packVO = new InvPackBVO();
		packVO.setSerialno(ediVO.getSerialno());
		if(StringUtils.isBlank(ediVO.getGoods_code())) {
			mess.append("|");
			mess.append(goods_code_error);
		}
		packVO.setGoods_code(ediVO.getGoods_code());
		// 有些商品在接口表中只有code，没有名称
		if(StringUtils.isBlank(ediVO.getGoods_name())) {
			packVO.setGoods_name(ediVO.getGoods_code());
		} else {
			packVO.setGoods_name(ediVO.getGoods_name());
		}
		packVO.setNum(ediVO.getNum());
		packVO.setWeight(ediVO.getWeight());
		packVO.setVolume(ediVO.getVolume());
		packVO.setPack_num_count(ediVO.getPack_num_count());
		packVO.setUnit_weight(ediVO.getUnit_weight());
		packVO.setUnit_volume(ediVO.getUnit_volume());
		packVO.setPack(getPackPK(ediVO.getPack()));
		packVO.setMin_pack(getPackPK(ediVO.getMin_pack()));
		packVO.setLength(ediVO.getLength());
		packVO.setWidth(ediVO.getWidth());
		packVO.setHeight(ediVO.getHeight());
		packVO.setDef1(ediVO.getDef1());
		packVO.setDef2(ediVO.getDef2());
		packVO.setDef3(ediVO.getDef3());
		packVO.setDef4(ediVO.getDef4());
		packVO.setDef5(ediVO.getDef5());
		packVO.setDef6(ediVO.getDef6());
		packVO.setDef7(ediVO.getDef7());
		packVO.setDef8(ediVO.getDef8());
		packVO.setDef9(ediVO.getDef9());
		packVO.setDef10(ediVO.getDef10());
		packVO.setDef11(ediVO.getDef11());
		packVO.setDef12(ediVO.getDef12());
		return packVO;
	}

	/**
	 * 返回客户的PK
	 * 
	 * @param cust_name
	 * @return
	 */
	public String getCustPK(String cust_name) {
		logger.info("根据客户名称匹配客户PK，客户名称：" + cust_name);
		if(StringUtils.isBlank(cust_name)) {
			mess.append(pk_customer_error);
			return null;
		}
		String pk = custMap.get(cust_name);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select pk_customer from ts_customer WITH(NOLOCK) where cust_name=? and isnull(dr,0)=0";
		pk = NWDao.getInstance().queryForObject(sql, String.class, cust_name);
		if(StringUtils.isBlank(pk)) {
			mess.append(pk_customer_error);
		}
		custMap.put(cust_name, pk);
		return pk;
	}

	/**
	 * 返回运输方式的PK
	 * 
	 * @param transTypeName
	 * @return
	 */
	public String getTransTypePK(String transTypeName) {
		logger.info("根据运输方式名称匹配PK，名称：" + transTypeName);
		if(StringUtils.isBlank(transTypeName)) {
			mess.append(pk_trans_type_error);
			return null;
		}
		String pk = transTypeMap.get(transTypeName);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select pk_trans_type from ts_trans_type WITH(NOLOCK) where name=? and isnull(dr,0)=0";
		pk = NWDao.getInstance().queryForObject(sql, String.class, transTypeName);
		if(StringUtils.isBlank(pk)) {
			mess.append(pk_trans_type_error);
		}
		custMap.put(transTypeName, pk);
		return pk;
	}

	/**
	 * 返回提货方的PK
	 * 
	 * @param deliNameOrArriName
	 * @param deliCodeOrArriCode
	 * @return
	 */
	public String getDeliOrArriPK(InvoiceVO invVO, String deliNameOrArriName, String deliCodeOrArriCode, boolean if_deli) {
		if(StringUtils.isBlank(deliNameOrArriName) || StringUtils.isBlank(deliCodeOrArriCode)) {
			mess.append("|");
			if(if_deli)
				mess.append(pk_delivery_error);
			else
				mess.append(pk_arrival_error);
			return null;
		}
		// 根据代码查询，原先根据名称，后来改成代码2015-12-24 lanjian
		String pk = addrMap.get(deliCodeOrArriCode);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select pk_address from ts_address WITH(NOLOCK) where addr_code=? and isnull(dr,0)=0";
		pk = NWDao.getInstance().queryForObject(sql, String.class, deliCodeOrArriCode);
		if(StringUtils.isBlank(pk)) {
			AddressVO addrVO = new AddressVO();
			addrVO.setAddr_code(deliCodeOrArriCode);
			addrVO.setAddr_name(deliNameOrArriName);
			addrVO.setAddr_type(AddressConst.ADDR_TYPE.TYPE1.intValue());
			addrVO.setPk_corp(Constants.SYSTEM_CODE);
			if(if_deli) {
				addrVO.setPk_city(invVO.getDeli_city());
				addrVO.setPk_province(invVO.getDeli_province());
				addrVO.setPk_area(invVO.getDeli_area());
				addrVO.setDetail_addr(invVO.getDeli_detail_addr());
				addrVO.setContact(invVO.getDeli_contact());
				addrVO.setMobile(invVO.getDeli_mobile());
				addrVO.setPhone(invVO.getDeli_phone());
				addrVO.setEmail(invVO.getDeli_email());
			} else {
				addrVO.setPk_city(invVO.getArri_city());
				addrVO.setPk_province(invVO.getArri_province());
				addrVO.setPk_area(invVO.getArri_area());
				addrVO.setDetail_addr(invVO.getArri_detail_addr());
				addrVO.setContact(invVO.getArri_contact());
				addrVO.setMobile(invVO.getArri_mobile());
				addrVO.setPhone(invVO.getArri_phone());
				addrVO.setEmail(invVO.getArri_email());
			}
			addrVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(addrVO);
			NWDao.getInstance().saveOrUpdate(addrVO);

			CustAddrVO custAddrVO = new CustAddrVO();
			custAddrVO.setPk_customer(invVO.getPk_customer());
			custAddrVO.setPk_address(addrVO.getPk_address());
			custAddrVO.setStatus(VOStatus.NEW);
			NWDao.setUuidPrimaryKey(custAddrVO);
			NWDao.getInstance().saveOrUpdate(custAddrVO);

			pk = addrVO.getPk_address();
			addrMap.put(deliCodeOrArriCode, pk);
		}
		return pk;
	}

	/**
	 * 提货流程
	 * 
	 * @param pk_delivery
	 * @return
	 */
	public String getDeliProcess(String pk_delivery) {
		if(StringUtils.isBlank(pk_delivery)) {
			return null;
		}
		String sql = "select deli_process from ts_address WITH(NOLOCK)  where pk_address=?";
		return NWDao.getInstance().queryForObject(sql, String.class, pk_delivery);
	}

	/**
	 * 到货流程
	 * 
	 * @param pk_arrival
	 * @return
	 */
	public String getArriProcess(String pk_arrival) {
		if(StringUtils.isBlank(pk_arrival)) {
			return null;
		}
		String sql = "select arri_process from ts_address WITH(NOLOCK) where pk_address=?";
		return NWDao.getInstance().queryForObject(sql, String.class, pk_arrival);
	}

	/**
	 * 提货城市
	 * 
	 * @param area_name
	 */
	@SuppressWarnings("rawtypes")
	public String getDeliCityPK(EdiInvoiceVO ediVO) {
		logger.info("匹配提货城市");
		String deli_city = ediVO.getDeli_city();
		if(StringUtils.isNotBlank(ediVO.getDeli_code())) {
			logger.info("根据配置文件的配置，匹配提货城市");
			Enumeration keys = ylcityProp.keys();
			while(keys.hasMoreElements()) {
				Object key = keys.nextElement();
				Object value = ylcityProp.get(key);
				if(String.valueOf(value).indexOf(ediVO.getDeli_code()) != -1) {
					deli_city = String.valueOf(key);
				}
			}
		}
		logger.info("提货城市为：" + deli_city);

		if(StringUtils.isBlank(deli_city)) {
			return null;
		}
		String pk = areaMap.get(deli_city);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select pk_area from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and name=?";
		pk = NWDao.getInstance().queryForObject(sql, String.class, deli_city);
		if(StringUtils.isNotBlank(pk)) {
			areaMap.put(deli_city, pk);
		}
		return pk;
	}

	@SuppressWarnings("rawtypes")
	public String getArriCityPK(EdiInvoiceVO ediVO) {
		logger.info("匹配收货城市");
		String arri_city = ediVO.getArri_city();
		if(StringUtils.isNotBlank(ediVO.getArri_code())) {
			logger.info("根据配置文件的配置，匹配收货城市");
			Enumeration keys = ylcityProp.keys();
			while(keys.hasMoreElements()) {
				Object key = keys.nextElement();
				Object value = ylcityProp.get(key);
				if(String.valueOf(value).indexOf(ediVO.getArri_code()) != -1) {
					arri_city = String.valueOf(key);
				}
			}
		}
		logger.info("收货城市为：" + arri_city);
		if(StringUtils.isBlank(arri_city)) {
			return null;
		}
		String pk = areaMap.get(arri_city);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select pk_area from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and name=?";
		pk = NWDao.getInstance().queryForObject(sql, String.class, arri_city);
		if(StringUtils.isNotBlank(pk)) {
			areaMap.put(arri_city, pk);
		}
		return pk;
	}

	/**
	 * 执行这个方法时，deli_city已经转换成pk了,注意这里的deli_city可以是deli_province,arri_city,
	 * arri_province
	 * 
	 * @param deli_city
	 * @return
	 */
	public String getParentArea(String deli_city) {
		if(StringUtils.isBlank(deli_city)) {
			return null;
		}
		String pk = areaMap.get(deli_city);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select parent_id from ts_area WITH(NOLOCK) where pk_area=?";
		pk = NWDao.getInstance().queryForObject(sql, String.class, deli_city);
		if(StringUtils.isNotBlank(pk)) {
			areaMap.put(deli_city, pk);
		}
		return pk;
	}

	/**
	 * 包装单位或者最小包装
	 * 
	 * @param pack
	 * @return
	 */
	public String getPackPK(String pack) {
		logger.info("根据包装单位或者最小包装匹配PK，名称：" + pack);
		if(StringUtils.isBlank(pack)) {
			return null;
		}
		String pk = packMap.get(pack);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		String sql = "select pk_goods_packcorp from ts_goods_packcorp WITH(NOLOCK) where isnull(dr,0)=0 and name=?";
		pk = NWDao.getInstance().queryForObject(sql, String.class, pack);
		if(StringUtils.isNotBlank(pk)) {
			packMap.put(pack, pk);
		}
		return pk;
	}

	/**
	 * 匹配公司pk
	 * 
	 * @param invVO
	 * @param ediVO
	 * @return
	 */
	public String getCorpPK(InvoiceVO invVO, EdiInvoiceVO ediVO) {
		logger.info("根据配置文件匹配公司");
		String corpCode = null;
		String splitChar = "\\|";
		
		
		// 第一层
		String value = String.valueOf(ylcorpProp.get("1"));
		String[] arr = value.split(splitChar);
		if(arr.length < 2) {// 严重错误，直接抛出异常，让事务回滚
			throw new BusiException("公司配置文件[?]第一层配置不正确！",YLCORP_PROPERTIES);
		}
		if(arr[1].indexOf(ediVO.getDeli_code()) != -1) {
			corpCode = arr[0];
		}
		// 第二层
		if(StringUtils.isBlank(corpCode)) {
			value = String.valueOf(ylcorpProp.get("2"));
			arr = value.split(splitChar);
			if(arr.length < 2) {// 严重错误，直接抛出异常，让事务回滚
				throw new BusiException("公司配置文件[?]第二层配置不正确！",YLCORP_PROPERTIES);
			}
			if(arr[1].indexOf(ediVO.getArri_code()) != -1) {
				corpCode = arr[0];
			}
			}
		
		// 第三层
		if(StringUtils.isBlank(corpCode)) {
			value = String.valueOf(ylcorpProp.get("3"));
			arr = value.split(splitChar);
			if(arr.length < 2) {// 严重错误，直接抛出异常，让事务回滚
				throw new BusiException("公司配置文件[?]第三层配置不正确！",YLCORP_PROPERTIES);
			}
			if(arr[1].indexOf(ediVO.getDeli_code()) != -1) {
				corpCode = arr[0];
			}
		}
		// 第四层
		if(StringUtils.isBlank(corpCode)) {
			value = String.valueOf(ylcorpProp.get("4"));
			arr = value.split(splitChar);
			if(arr.length < 2) {// 严重错误，直接抛出异常，让事务回滚
				throw new BusiException("公司配置文件[?]第四层配置不正确！",YLCORP_PROPERTIES);
			}
			if(arr[1].indexOf(ediVO.getArri_code()) != -1) {
				corpCode = arr[0];
			}
		}

		// 第五层
		if(StringUtils.isBlank(corpCode)) {
			value = String.valueOf(ylcorpProp.get("5"));
			arr = value.split(splitChar);
			if(arr.length < 3) {
				throw new BusiException("公司配置文件[?]第五层配置不正确！",YLCORP_PROPERTIES);
			}
//			if(arr[1].indexOf(ediVO.getDeli_code()) != -1 && arr[2].indexOf(ediVO.getArri_province()) != -1) {
//				corpCode = arr[0];
//			}
			//yaojiie 2015 11 18 修改逻辑 不判断地址
			if(arr[1].indexOf(ediVO.getDeli_code()) != -1 ) {
				corpCode = arr[0];
			}
		}
		if(StringUtils.isBlank(corpCode)) {
			corpCode = "DLC";
		}
		logger.info("匹配的公司编码为：" + corpCode);
		String pk = corpMap.get(corpCode);
		if(StringUtils.isNotBlank(pk)) {
			return pk;
		}
		
		// 根据corpCode查询corpPK
		String sql = "select pk_corp from nw_corp WITH(NOLOCK) where isnull(dr,0)=0 and corp_code=?";
		pk = NWDao.getInstance().queryForObject(sql, String.class, corpCode);
		if(StringUtils.isNotBlank(pk)) {
			corpMap.put(corpCode, pk);
		} else {
			throw new BusiException("没有创建编码为[?]的公司！",corpCode);
		}
		return pk;
	}

	/**
	 * 返回运段类别，包括提货段，干线段，收货段
	 * 
	 * @return
	 */
	public String getSegmentType(EdiInvoiceVO ediVO) {
		logger.info("匹配所属的运段类型");
		Integer segmentType = null;
		String deliCode = ediVO.getDeli_code();
		String arriCode = ediVO.getArri_code();
		if(StringUtils.isNotBlank(arriCode)) {
			if(arriCode.indexOf("HUB") != -1) {
				segmentType = SegmentConst.SEG_TYPE_THD;// 提货段
			}
			if((arriCode.equals("CGQ_DC") || arriCode.equals("DACH"))
					&& (deliCode == null || deliCode.indexOf("HUB") == -1)) {
				segmentType = SegmentConst.SEG_TYPE_THD;// 提货段
			}
		}
		if(StringUtils.isNotBlank(deliCode)) {
			if(deliCode.indexOf("HUB") != -1) {
				segmentType = SegmentConst.SEG_TYPE_GXD;// 干线段
			}
			if(deliCode.equals("CGQ_DC") || deliCode.equals("DACH")) {
				segmentType = SegmentConst.SEG_TYPE_SHD;// 送货段
			}
			if(deliCode.equals("8200") || deliCode.equals("8201")) {
				segmentType = SegmentConst.SEG_TYPE_THD;// 提货段
			}
		}
		logger.info("匹配到的所属运段类型为：" + segmentType);
		return segmentType != null ? segmentType + "" : null;
	}

	/**
	 * 将中间表vo转换成历史表vo
	 * 
	 * @param ediVO
	 * @return
	 */
	public EdiHisInvoiceVO convertToHis(EdiInvoiceVO ediVO) {
		if(ediVO == null) {
			return null;
		}
		EdiHisInvoiceVO hisVO = new EdiHisInvoiceVO();
		String[] attrs = ediVO.getAttributeNames();
		for(String attr : attrs) {
			hisVO.setAttributeValue(attr, ediVO.getAttributeValue(attr));
		}
		hisVO.setSync_time(new UFDateTime(new Date()));
		return hisVO;
	}

	/**
	 * 将中间表vo转换成历史表vo
	 * 
	 * @param ediVO
	 * @return
	 */
	public EdiHisInvPackBVO convertToHis(EdiInvPackBVO ediVO) {
		if(ediVO == null) {
			return null;
		}
		EdiHisInvPackBVO hisVO = new EdiHisInvPackBVO();
		String[] attrs = ediVO.getAttributeNames();
		for(String attr : attrs) {
			hisVO.setAttributeValue(attr, ediVO.getAttributeValue(attr));
		}
		return hisVO;
	}

	public void before(JobDefVO jobDefVO) {
		service = (InvoiceService) SpringContextHolder.getApplicationContext().getBean("invoiceServiceImpl");
		paramVO = new ParamVO();
		paramVO.setFunCode(FunConst.INVOICE_CODE);
		paramVO.getAttr().put("updateAddr", false);// 地址和编码从中间表中传入，已经会做处理，比如地址不存在的话会自动加入。不要在processBeforeSave中处理，否则地址编码不好处理

		if(ylcorpProp == null) {
			String path = WebUtils.getClientConfigPath() + File.separator + YLCORP_PROPERTIES;
			try {
				File configPropFile = new File(path);
				ylcorpProp = PropertiesUtils.loadProperties(new FileInputStream(configPropFile));
			} catch(Exception e) {
				logger.error("加载配置文件出错，可能文件不存在，path：" + path);
				e.printStackTrace();
			}
		}

		if(ylcityProp == null) {
			String path = WebUtils.getClientConfigPath() + File.separator + YLCITY_PROPERTIES;
			try {
				File configPropFile = new File(path);
				ylcityProp = PropertiesUtils.loadProperties(new FileInputStream(configPropFile));
			} catch(Exception e) {
				logger.error("加载配置文件出错，可能文件不存在，path：" + path);
				e.printStackTrace();
			}
		}
	}

	public void after(JobDefVO jobDefVO) {
		// 清理缓冲
		custMap.clear();
		transTypeMap.clear();
		addrMap.clear();
		areaMap.clear();
		contactMap.clear();
		packMap.clear();
		corpMap.clear();
	}
}
