package com.tms.service.base.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nw.basic.util.StringUtils;
import org.nw.dao.NWDao;
import org.nw.utils.CorpHelper;
import org.nw.utils.NWUtils;
import com.tms.vo.base.AddressVO;
import com.tms.vo.base.TransLineVO;
/**
 * 基础资料工具类
 * 
 * @author xuqc
 * @date 2013-4-14 下午01:47:28
 */
public class BaseUtils {
	// yaojiie 2015 12 02
	// 添加获取路线信息功能：用户登录调用此功能，返回这个公司和集团的所有的路线和路线下所以节点组成的Map组成的集合
	public static List<Map<String, List<AddressVO>>> getLine() {
		// 当前公司和集团
		String corpCond = CorpHelper.getCurrentCorpWithGroup();
		String cond = "";
		// 获取符合条件的所有线路
		TransLineVO[] lineVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(TransLineVO.class,
				" isnull(locked_flag,'N')='N' and isnull(dr,0)=0 and line_type=2 and " + corpCond);

		if (lineVOs != null && lineVOs.length > 0) {
			List<String> primaryKeys = new ArrayList<String>();
			for (TransLineVO lineVO : lineVOs) {
				primaryKeys.add(lineVO.getPrimaryKey());
			}
			cond = NWUtils.buildConditionString(primaryKeys.toArray(new String[primaryKeys.size()]));
		}
		// 在AddressVO里加了一个临时用的字段pk_trans_line，方便查询
		//yaojiie 2015 12 10 增加对cond的判断，当cond为空时，肯定查不到数据，直接return null;
		String sql = "";
		if(StringUtils.isNotBlank(cond)){
			sql = "SELECT addr.*,node.s_timeline as s_timeline,node.pk_trans_line as pk_trans_line FROM ts_address addr WITH(NOLOCK),ts_line_node node WITH(NOLOCK) "
					+ " WHERE addr.pk_address=node.pk_address AND isnull(addr.dr,0)=0 AND isnull(node.dr,0)=0 "
					+ "AND isnull(node.locked_flag,'N')='N' and node.pk_trans_line in " + cond
					+ " order by node.serialno asc ";
		}else{
			return null;
		}
		
		// 获取这个路线下所有的节点的地址VO
		List<AddressVO> addrVOs = NWDao.getInstance().queryForList(sql, AddressVO.class);
		if (addrVOs == null || addrVOs.size() == 0) {
			return null;
		}
		String keyChar = "|";
		List<Map<String, List<AddressVO>>> lineList = new ArrayList<Map<String, List<AddressVO>>>();
		for (TransLineVO lineVO : lineVOs) {
			//yaojiie 2015 12 03将当前pk_corp拼入Key中否则找不到所属公司信息。
			// 拼接条件，形成key
			String key = new StringBuffer().append(lineVO.getPk_trans_type()).append(keyChar)
					.append(lineVO.getStart_addr()).append(keyChar).append(lineVO.getStart_addr_type()).append(keyChar)
					.append(lineVO.getEnd_addr()).append(keyChar).append(lineVO.getEnd_addr_type()).append(keyChar)
					.append(lineVO.getPk_corp()).toString();
			List<AddressVO> newaddrVOs = new ArrayList<AddressVO>();
			Map<String, List<AddressVO>> linemap = new HashMap<String, List<AddressVO>>();
			// 将addrVOs放入要输出的map中
			for (AddressVO addrVO : addrVOs) {
				if (lineVO.getPk_trans_line().equals(addrVO.getPk_trans_line())) {
					newaddrVOs.add(addrVO);
				}
			}
			linemap.put(key, newaddrVOs);
			lineList.add(linemap);
		}
		return lineList;
	}

}
