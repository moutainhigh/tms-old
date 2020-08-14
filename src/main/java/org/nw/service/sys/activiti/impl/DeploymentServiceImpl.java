package org.nw.service.sys.activiti.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.nw.dao.NWDao;
import org.nw.exception.BusiException;
import org.nw.exception.JsonException;
import org.nw.service.impl.AbsToftServiceImpl;
import org.nw.service.sys.activiti.DeploymentService;
import org.nw.vo.HYBillVO;
import org.nw.vo.ParamVO;
import org.nw.vo.VOTableVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.nw.vo.pub.CircularlyAccessibleValueObject;
import org.nw.vo.pub.SuperVO;
import org.nw.vo.sys.activiti.DeploymentVO;
import org.nw.vo.sys.activiti.ProcdefVO;
import org.nw.web.utils.ProcessEngineHelper;
import org.springframework.stereotype.Service;

/**
 * 流程部署
 * 
 * @author xuqc
 *
 */
@Service
public class DeploymentServiceImpl extends AbsToftServiceImpl implements DeploymentService {
	AggregatedValueObject billInfo = null;

	public AggregatedValueObject getBillInfo() {
		if(billInfo == null) {
			billInfo = new HYBillVO();
			VOTableVO vo = new VOTableVO();
			vo.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			vo.setAttributeValue(VOTableVO.HEADITEMVO, DeploymentVO.class.getName());
			vo.setAttributeValue(VOTableVO.PKFIELD, DeploymentVO.ID_);
			billInfo.setParentVO(vo);

			VOTableVO childVO = new VOTableVO();
			childVO.setAttributeValue(VOTableVO.BILLVO, HYBillVO.class.getName());
			childVO.setAttributeValue(VOTableVO.HEADITEMVO, ProcdefVO.class.getName());
			childVO.setAttributeValue(VOTableVO.PKFIELD, ProcdefVO.DEPLOYMENT_ID_);
			childVO.setAttributeValue(VOTableVO.ITEMCODE, "ACT_RE_PROCDEF");
			childVO.setAttributeValue(VOTableVO.VOTABLE, "ACT_RE_PROCDEF");

			CircularlyAccessibleValueObject[] childrenVO = { childVO };
			billInfo.setChildrenVO(childrenVO);
		}
		return billInfo;
	}

	@Override
	public String buildLoadDataOrderBy(ParamVO paramVO, Class<? extends SuperVO> clazz, String orderBy) {
		orderBy = super.buildLoadDataOrderBy(paramVO, clazz, orderBy);
		if(StringUtils.isBlank(orderBy)) {
			orderBy = "order by deploy_time_ desc";
		}
		return orderBy;
	}

	public ProcessInstance doStartFlow(ParamVO paramVO) {
		if(StringUtils.isBlank(paramVO.getBillId())) {
			return null;
		}
		ProcdefVO defVO = NWDao.getInstance()
				.queryByCondition(ProcdefVO.class, "DEPLOYMENT_ID_=?", paramVO.getBillId());
		if(defVO == null) {
			throw new BusiException("流程定义已经被删除，DEPLOYMENT_ID_[?]！",paramVO.getBillId());
		}
		RuntimeService runtimeService = ProcessEngineHelper.getProcessEngine().getRuntimeService();
		// 第一个参数是流程key，第二个参数是busiessKey
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(defVO.getId_(), "TEST_BUSI_ID");
		// System.out.println("id " + processInstance.getId() + " " +
		// processInstance.getProcessDefinitionId());
		return processInstance;
	}

	/**
	 * 发布流程 map的对象是：资源名称（文件名称）：输入流
	 * 
	 * @param inAry
	 */
	public void doDeploy(Map<String, InputStream> inMap) {
		if(inMap == null) {
			return;
		}
		Set<String> keys = inMap.keySet();
		if(keys.size() == 0) {
			return;
		}
		ProcessEngine processEngine = ProcessEngineHelper.getProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		DeploymentBuilder builder = repositoryService.createDeployment();
		for(String key : keys) {
			builder.addInputStream(key, inMap.get(key));
		}
		/* Deployment deployment = */builder.deploy();
		// 更新流程的pk_corp字段

	}

	/**
	 * 预览流程图
	 * 
	 * @param vo
	 * @param out
	 */
	@SuppressWarnings("rawtypes")
	public void preview(DeploymentVO vo, OutputStream out) throws Exception {
		if(vo == null || vo.getId_() == null) {
			return;
		}
		ProcdefVO defVO = NWDao.getInstance().queryByCondition(ProcdefVO.class, "DEPLOYMENT_ID_=?", vo.getId_());
		if(defVO == null) {
			throw new JsonException("流程定义不存在，可以已经被删除！");
		}
		//yaojiie 2015 12 08添加 WITH (NOLOCK)
		String sql = "select BYTES_ from ACT_GE_BYTEARRAY WITH(NOLOCK) where DEPLOYMENT_ID_=? AND NAME_=?";
		HashMap retMap = NWDao.getInstance().queryForObject(sql, HashMap.class, vo.getId_(),
				defVO.getDgrm_resource_name_());
		if(retMap == null) {
			logger.warn("您预览的文件已经不存在,deployment_id=" + vo.getId_() + ",文件名称：" + defVO.getDgrm_resource_name_());
			throw new BusiException("您预览的文件已经不存在,deployment_id[?],文件名称[?]",vo.getId_(),defVO.getDgrm_resource_name_());
		}
		Object contentdata = retMap.get("bytes_");
		if(contentdata != null) {
			InputStream in = new ByteArrayInputStream((byte[]) contentdata);
			IOUtils.copy(in, out);
		}
	}
}
