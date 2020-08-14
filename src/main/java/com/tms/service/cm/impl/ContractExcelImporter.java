package com.tms.service.cm.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exp.BillExcelImporter;
import org.nw.service.IBillService;
import org.nw.service.IToftService;
import org.nw.utils.NWUtils;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.pub.VOStatus;
import org.nw.vo.sys.ImportConfigVO;

import com.tms.vo.base.AddressVO;
import com.tms.vo.base.AreaVO;
import com.tms.vo.base.CarTypeVO;
import com.tms.vo.base.GoodsPackcorpVO;
import com.tms.vo.cm.ContractBVO;
import com.tms.vo.cm.ContractVO;

/**
 * 合同导入，合同只导入到表体
 * 
 * @author xuqc
 * @date 2014-5-2 下午07:28:27
 */
public class ContractExcelImporter extends BillExcelImporter {

	public ContractExcelImporter(ParamVO paramVO, IBillService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	public ContractExcelImporter(ParamVO paramVO, IToftService service,ImportConfigVO configVO) {
		super(paramVO, service, configVO);
	}

	public void processAfterResolveOneRow(SuperVO parentVO1, SuperVO childVO1, List<AggregatedValueObject> aggVOs,
			int rowNum) {
		if(StringUtils.isBlank(this.paramVO.getBillId())) {
			throw new BusiException("请先选择一行合同记录进行导入！");
		}
		ContractBVO childVO = (ContractBVO) childVO1;
		childVO.setStatus(VOStatus.NEW);
		NWDao.setUuidPrimaryKey(childVO);
		childVO.setPk_contract(this.paramVO.getBillId());
		AggregatedValueObject aggVO = null;
		ContractVO parentVO = null;
		if(aggVOs != null && aggVOs.size() > 0) {
			aggVO = aggVOs.get(0);// aggVOs肯定只有一行记录
			parentVO = (ContractVO) aggVO.getParentVO();
			ContractBVO[] childVOs = (ContractBVO[]) aggVO.getChildrenVO();
			ContractBVO[] newChildVOs = new ContractBVO[childVOs.length + 1];
			for(int i = 0; i < childVOs.length; i++) {
				newChildVOs[i] = childVOs[i];
			}
			newChildVOs[childVOs.length] = childVO;
			aggVO.setChildrenVO(newChildVOs);
		} else {
			parentVO = this.service.getByPrimaryKey(ContractVO.class, this.paramVO.getBillId());
			aggVO = new HYBillVO();
			aggVO.setParentVO(parentVO);
			parentVO.setStatus(VOStatus.UPDATED);
			aggVO.setChildrenVO(new ContractBVO[] { childVO });
			aggVOs.add(aggVO);
		}
	}
	
	public void _import(File file) throws Exception{
		List<AggregatedValueObject> aggVOs = resolve(file);
		//只会存在一个合同头
		HYBillVO billVO = (HYBillVO) aggVOs.get(0);
		ContractBVO[] contractBVOs = (ContractBVO[])billVO.getChildrenVO();
		List<String> area_addrs = new ArrayList<String>();
		List<String> addr_addrs = new ArrayList<String>();
		List<String> equip_types = new ArrayList<String>();
		List<String> packs = new ArrayList<String>();
		List<SuperVO> toBeUpdate = new ArrayList<SuperVO>();
		toBeUpdate.addAll(Arrays.asList(contractBVOs));
		for(ContractBVO  contractBVO : contractBVOs){
			if(contractBVO.getStart_addr_type() ==0 && StringUtils.isNotBlank( contractBVO.getStart_addr())){
				area_addrs.add(contractBVO.getStart_addr());
			}
			if(contractBVO.getEnd_addr_type() ==0 && StringUtils.isNotBlank(contractBVO.getEnd_addr())){
				area_addrs.add(contractBVO.getEnd_addr());
			}
			
			if(contractBVO.getStart_addr_type() ==1 && StringUtils.isNotBlank( contractBVO.getStart_addr())){
				addr_addrs.add(contractBVO.getStart_addr());
			}
			if(contractBVO.getEnd_addr_type() ==1 && StringUtils.isNotBlank(contractBVO.getEnd_addr())){
				addr_addrs.add(contractBVO.getEnd_addr());
			}
			if(StringUtils.isNotBlank(contractBVO.getEquip_type())){
				equip_types.add(contractBVO.getEquip_type());
			}
			if(StringUtils.isNotBlank(contractBVO.getPack())){
				packs.add(contractBVO.getPack());
			}
		}
		String areaCond = NWUtils.buildConditionString(area_addrs.toArray(new String[area_addrs.size()]));
		String addrCond = NWUtils.buildConditionString(addr_addrs.toArray(new String[addr_addrs.size()]));
		String equipTypeCond = NWUtils.buildConditionString(equip_types.toArray(new String[equip_types.size()]));
		String packCond = NWUtils.buildConditionString(packs);
		if(StringUtils.isNotBlank(areaCond)){
			AreaVO[] areaVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AreaVO.class, "name in " + areaCond);
			for(String pk_area : area_addrs){
				boolean flag = true;
				for(AreaVO areaVO : areaVOs){
					if(pk_area.equals(areaVO.getName())){
						for(ContractBVO contractBVO : contractBVOs){
							if(pk_area.equals(contractBVO.getStart_addr())){
								contractBVO.setStart_addr(areaVO.getPk_area());
							}
						}
						for(ContractBVO contractBVO : contractBVOs){
							if(pk_area.equals(contractBVO.getEnd_addr())){
								contractBVO.setEnd_addr(areaVO.getPk_area());
							}
						}
						flag = false;
					};
				}
				if(flag){
					throw new BusiException("区域 [?]错误，请检查数据！",pk_area);
				}
			}
		}
		
		if(StringUtils.isNotBlank(addrCond)){
			AddressVO[] addrVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(AddressVO.class, "addr_name in " + addrCond);
			for(String pk_addr : addr_addrs){
				boolean flag = true;
				for(AddressVO addrVO : addrVOs){
					if(pk_addr.equals(addrVO.getAddr_name())){
						for(ContractBVO contractBVO : contractBVOs){
							if(pk_addr.equals(contractBVO.getStart_addr())){
								contractBVO.setStart_addr(addrVO.getPk_address());
							}
						}
						for(ContractBVO contractBVO : contractBVOs){
							if(pk_addr.equals(contractBVO.getEnd_addr())){
								contractBVO.setEnd_addr(addrVO.getPk_address());
							}
						}
						flag = false;
					};
				}
				if(flag){
					throw new BusiException("地址[?]错误，请检查数据！",pk_addr);
				}
			}
		}
		if(StringUtils.isNotBlank(equipTypeCond)){
			CarTypeVO[] carTypeVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CarTypeVO.class, "name in " + equipTypeCond);
			for(String equip_type : equip_types ){
				boolean flag = true;
				for(CarTypeVO carTypeVO : carTypeVOs){
					if(equip_type.equals(carTypeVO.getName())){
						for(ContractBVO contractBVO : contractBVOs){
							if(equip_type.equals(contractBVO.getEquip_type())){
								contractBVO.setEquip_type(carTypeVO.getPk_car_type());
							}
						}
						flag = false;
					}
				}
				if(flag){
					throw new BusiException("设备类型 [?]错误，请检查数据",equip_type);
				}
			}
		}
		
		if(StringUtils.isNotBlank(packCond)){
			GoodsPackcorpVO[] packVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(GoodsPackcorpVO.class, "name in " + packCond);
			for(String pack : packs ){
				boolean flag = true;
				for(GoodsPackcorpVO packVO : packVOs){
					if(pack.equals(packVO.getName())){
						for(ContractBVO contractBVO : contractBVOs){
							if(pack.equals(contractBVO.getPack())){
								contractBVO.setPack(packVO.getPk_goods_packcorp());
							}
						}
						flag = false;
					}
				}
				if(flag){
					throw new BusiException("包装 [?]错误，请检查数据",pack);
				}
			}
		}
		
		//判断导入的excel中，是否有重复行
		List<String> newStrings = CMUtils.splicString(contractBVOs);
		CMUtils.checkRepeat(newStrings);
		//判断导入的excel中，是否有与数据库中相同的数据行
		ContractVO contractVO = (ContractVO) billVO.getParentVO();
		//通过导入的合同，在数据库里寻找已有的合同明细
		ContractBVO[] ctBVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(ContractBVO.class, "pk_contract=?",
				contractVO.getPk_contract());
		List<String> oldStrings = CMUtils.splicString(ctBVOs);
		 boolean b = false;
		 List<String> errormessages = new ArrayList<String>();
		 String mesg =new String();
		 for (int i = 0; i < newStrings.size(); i++) {
	            for (int j = 0; j < oldStrings.size(); j++) {
	            	if(newStrings.get(i).equals(oldStrings.get(j))){
	            		b = true;
	            		mesg = "第" + (i + 1) + "条合同明细和已有的第" + (j + 1) + "条合同明细重复";
	            		errormessages.add(mesg);
	            	}
	            }
		 	}
	     if (b) {
	          throw new BusiException(errormessages.toString());
	        }
	     NWDao.getInstance().saveOrUpdate(toBeUpdate);
	     //加入缓存
	     ContractUtils.add(billVO);
	}
}
