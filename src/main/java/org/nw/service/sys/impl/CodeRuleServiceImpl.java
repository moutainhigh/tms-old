package org.nw.service.sys.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nw.constants.FunRegisterConst;
import org.nw.dao.NWDao;
import org.nw.jf.vo.UiQueryTempletVO;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.CodeRuleService;
import org.nw.service.sys.FunService;
import org.nw.utils.CorpHelper;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.TreeVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.sys.CodeRuleVO;
import org.nw.vo.sys.FunVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 编码规则的处理类
 * 
 * @author xuqc
 * @date 2012-7-27 下午10:44:40
 */
@Service
public class CodeRuleServiceImpl extends AbsToftServiceImpl implements CodeRuleService {

	private static String BASEDATA_FUN_CODE = "t1";

	@Autowired
	private FunService funService;

	private AggregatedValueObject billInfo;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();

			// 由于是档案型，所以这里手工创建billInfo
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, CodeRuleVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, CodeRuleVO.PK_BILLNO_RULE);
			billInfo.setParentVO(vo);
		}
		return billInfo;
	}

	/**
	 * 返回不需要使用编码规则的节点
	 * 
	 * @return
	 */
	private List<String> getFilterFunCode() {
		List<String> funCodeAry = new ArrayList<String>();
		// funCodeAry.add("t110"); // 数据字典
		// funCodeAry.add("t10704");// 车辆管理
		return funCodeAry;
	}

	public List<TreeVO> getFunCodeTree() {
		List<FunVO> powerFunVOs = funService.getPowerFunVO(false);// 当前用户具有权限的菜单
		List<String> filterAry = getFilterFunCode();
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		for(FunVO funVO : powerFunVOs) {
			boolean ifExist = false;
			for(String funCode : filterAry) {
				if(funVO.getFun_code().equals(funCode)) {
					ifExist = true;
					break;
				}
			}
			if(ifExist) {
				continue;
			}
			if(funVO.getFun_property().intValue() == FunRegisterConst.LFW_FUNC_NODE
					&& (funVO.getIf_code_rule() != null && funVO.getIf_code_rule().booleanValue())
					&& StringUtils.isBlank(funVO.getBill_type())) {
				// 轻量级功能节点
				TreeVO treeVO = convertFunVO(funVO);
				treeVOs.add(treeVO);
			}
		}
		return treeVOs;
	}

	public List<TreeVO> getBaseDataFunCodeTree() {
		FunVO parentVO = getFunVOByFunCode(BASEDATA_FUN_CODE);
		List<FunVO> funVOs = funService.getFunVOs(parentVO.getPk_fun(), false);
		List<TreeVO> treeVOs = new ArrayList<TreeVO>();
		List<String> filterAry = getFilterFunCode();
		for(FunVO funVO : funVOs) {
			boolean ifExist = false;
			for(String funCode : filterAry) {
				if(funVO.getFun_code().equals(funCode)) {
					ifExist = true;
					break;
				}
			}
			if(ifExist) {
				continue;
			}
			if(funVO.getFun_property().intValue() == FunRegisterConst.LFW_FUNC_NODE) {
				// 轻量级功能节点
				TreeVO treeVO = convertFunVO(funVO);
				treeVOs.add(treeVO);
			}
		}
		return treeVOs;
	}

	public static TreeVO convertFunVO(FunVO funVO) {
		if(funVO == null) {
			return null;
		}
		TreeVO treeNode = new TreeVO();
		treeNode.setId(funVO.getFun_code());
		treeNode.setCode(funVO.getFun_code());
		treeNode.setText(funVO.getFun_name());
		treeNode.setExpanded(false);
		treeNode.setLeaf(true);
		return treeNode;
	}

	public CodeRuleVO getByFunCode(String fun_code) {
		String where = " isnull(dr,0)=0 and fun_code=? ";
		String corpCond = CorpHelper.getCurrentCorpWithChildrenAndParent();
		if(StringUtils.isNotBlank(corpCond)) {
			where += " and " + corpCond;
		}
		CodeRuleVO[] ruleVOs = NWDao.getInstance().queryForSuperVOArrayByCondition(CodeRuleVO.class, where, fun_code);
		if(ruleVOs == null || ruleVOs.length == 0) {
			return null;
		}
		// 这里的排序有点复杂，真正想要的顺序是，自己公司的规则，如果没有，那么上级公司的规则，一直到集团。
		// 在CorpHelper中查询公司的时候，实际上就是根据公司编码进行排序的，保证子公司都排在前面，从而查询的规则也是子公司在前面
		return ruleVOs[0];
	}

	public String buildLoadDataCondition(String params, ParamVO paramVO, UiQueryTempletVO templetVO) {
		String fCond = "1=1";
		String cond = super.buildLoadDataCondition(params, paramVO, templetVO);
		if(StringUtils.isNotBlank(cond)) {
			fCond += " and ";
			fCond += cond;
		}
		// 根据当前登录用户的公司，查找所有子公司，不分级次
		String corpCond = CorpHelper.getCurrentCorpWithGroup();
		if(StringUtils.isNotBlank(corpCond)) {
			fCond += " and " + corpCond;
		}
		return fCond;
	}

	public Map<String, Object> getHeaderDefaultValues(ParamVO paramVO) {
		Map<String, Object> headerMap = super.getHeaderDefaultValues(paramVO);
		headerMap.put("serial_no_count", 4);
		return headerMap;
	}
}
