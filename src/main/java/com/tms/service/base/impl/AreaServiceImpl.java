package com.tms.service.base.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nw.dao.NWDao;
import org.nw.exp.POI;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.stereotype.Service;

import com.tms.constants.AreaConst;
import com.tms.service.base.AreaService;
import com.tms.utils.TreeUtils;
import com.tms.vo.base.AreaVO;

/**
 * 
 * @author xuqc
 * @date 2012-7-10 下午04:17:34
 */
@Service
public class AreaServiceImpl extends AbsToftServiceImpl implements AreaService {

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, AreaVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, AreaVO.PK_AREA);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	public List<TreeVO> getAreaTree(String parent_id, String keyword) {
		String sql = "select * from ts_area WITH(NOLOCK) where isnull(dr,0)=0";
		if(StringUtils.isNotBlank(keyword)) {
			sql += " and name like '" + keyword + "%'";
		} else {
			if(StringUtils.isBlank(parent_id)) {
				sql += " and parent_id is null";
			} else {
				sql += " and parent_id='" + parent_id + "'";
			}
		}
		sql += " and " + getCorpCond();
		sql += " order by display_order asc,code asc";
		List<AreaVO> areaVOs = dao.queryForList(sql, AreaVO.class);
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		for(AreaVO areaVO : areaVOs) {
			treeVOs.add(TreeUtils.convertAreaVO(areaVO, false));
		}
		return treeVOs;
	}

	public List<TreeVO> getAreaTree(int level) {
		String sql = "select * from ts_area WITH(NOLOCK) where isnull(dr,0)=0 and area_level >= " + level;
		sql += " and " + getCorpCond();
		sql += " order by display_order asc,code asc";
		List<AreaVO> areaVOs = dao.queryForList(sql, AreaVO.class);
		List<AreaVO> topLevelVOs = new ArrayList<AreaVO>();
		for(AreaVO areaVO : areaVOs) {
			if(areaVO.getArea_level().intValue() == level) {
				topLevelVOs.add(areaVO);
			}
		}
		List<TreeVO> treeVOs = TreeUtils.convertAreaVO(topLevelVOs, areaVOs);
		TreeUtils.setChildrenAreaVOs(topLevelVOs, areaVOs, treeVOs);
		return treeVOs;
	}

	public String getCodeFieldCode() {
		return AreaVO.CODE;
	}

	public AreaVO getCityByName(String name) {
		String sql = "select * from ts_area where area_level=? and isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and "
				+ getCorpCond() + " and name=?";
		List<AreaVO> list = dao.queryForList(sql, AreaVO.class, AreaConst.CITY_LEVEL, name);
		if(list != null && list.size() > 0) {// 如果有多条记录，只返回第一条，通常不会
			return list.get(0);
		}
		return null;
	}

	public AreaVO getCityByCode(String code) {
		String sql = "select * from ts_area where (area_level=? or area_level=? or area_level=? or area_level=?) and isnull(dr,0)=0 and isnull(locked_flag,'N')='N' and "
				+ getCorpCond() + " and code=?";
		List<AreaVO> list = dao.queryForList(sql, AreaVO.class, AreaConst.CITY_LEVEL, AreaConst.AREA_LEVEL, AreaConst.TOWN_LEVEL, AreaConst.STREET_LEVEL, code);
		if(list != null && list.size() > 0) {// 如果有多条记录，只返回第一条，通常不会
			return list.get(0);
		}
		return null;
	}
	/**
	 * 默认可以查询集团和本公司的数据
	 * 
	 * @return
	 */
	private String getCorpCond() {
		return CorpHelper.getCurrentCorpWithChildrenAndParent();
	}

	public HSSFWorkbook export(ParamVO paramVO, int offset, int pageSize, String orderBy, String extendCond,
			Object... values) {
		String[] titleAry = new String[] { "洲	", "国家", "区域", "省", "市", "区" };
		List<List<String>> dataList = new ArrayList<List<String>>();
		String sql = "select * from ts_area where isnull(dr,0)=0 and area_level=?";
		NWDao dao = NWDao.getInstance();
		AreaVO vo = dao.queryForObject(sql, AreaVO.class, 1);
		sql = "select * from ts_area where isnull(dr,0)=0 and parent_id=? order by code";
		List<AreaVO> vos1 = dao.queryForList(sql, AreaVO.class, vo.getPk_area());
		int provinceNum = 0;
		int cityNum = 0;
		int areaNum = 0;
		for(int j = 0; j < vos1.size(); j++) {
			List<AreaVO> vos2 = dao.queryForList(sql, AreaVO.class, vos1.get(j).getPk_area());
			for(int k = 0; k < vos2.size(); k++) {
				List<AreaVO> vos3 = dao.queryForList(sql, AreaVO.class, vos2.get(k).getPk_area());
				provinceNum += vos3.size();
				for(int l = 0; l < vos3.size(); l++) {
					List<AreaVO> vos4 = dao.queryForList(sql, AreaVO.class, vos3.get(l).getPk_area());
					cityNum += vos4.size();
					for(int m = 0; m < vos4.size(); m++) {
						List<AreaVO> vos5 = dao.queryForList(sql, AreaVO.class, vos4.get(m).getPk_area());
						areaNum += vos5.size();
						for(int n = 0; n < vos5.size(); n++) {
							List<String> valueList = new ArrayList<String>();
							valueList.add(vo.getName());
							valueList.add(vos1.get(j).getName());
							valueList.add(vos2.get(k).getName());
							valueList.add(vos3.get(l).getName());
							valueList.add(vos4.get(m).getName());
							valueList.add(vos5.get(n).getName());
							dataList.add(valueList);
						}
					}
				}
			}
		}
		System.out.println("-------------------provinceNum:" + provinceNum + "---------------cityNum:" + cityNum
				+ "---------------areaNum:" + areaNum);
		POI excel = new POI();
		HSSFWorkbook wb = excel.buildExcel(titleAry, dataList);
		return wb;
	}

	/**
	 * 设置新增区域节点的level值
	 */
	protected void processBeforeSave(AggregatedValueObject billVO, ParamVO paramVO) {
		super.processBeforeSave(billVO, paramVO);
		AreaVO parentVO = (AreaVO) billVO.getParentVO();
		if(StringUtils.isBlank(parentVO.getParent_id())) {
			parentVO.setArea_level(1);
		} else {
			String sql = "select area_level from ts_area where pk_area=?";
			Integer level = NWDao.getInstance().queryForObject(sql, Integer.class, parentVO.getParent_id());
			if(level == null) {
				parentVO.setArea_level(1);
			} else {
				parentVO.setArea_level(level.intValue() + 1);
			}
		}
	}
}
