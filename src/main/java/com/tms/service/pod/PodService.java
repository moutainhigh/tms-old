package com.tms.service.pod;

import java.util.List;
import java.util.Map;

import org.nw.service.IBillService;
import org.nw.vo.ParamVO;
import org.nw.vo.pub.AggregatedValueObject;
import org.springframework.web.multipart.MultipartFile;

import com.tms.vo.inv.InvoiceVO;
import com.tms.vo.pod.PodVO;
import com.tms.vo.te.EntrustVO;

/**
 * 签收回单处理接口
 * 
 * @author xuqc
 * @date 2013-4-16 下午03:21:16
 */
public interface PodService extends IBillService {

	/**
	 * 发货单状态更新为已到货后的处理,增加一条记录到签收回单表
	 */
	public PodVO afterChangeInvoiceToArrival(InvoiceVO invVO, EntrustVO entVO);

	/**
	 * 发货单状态更新为已提货后的处理，这里只要处理从已到货--》已提货的状态改变，删除签收回单表中的数据
	 */
	public void afterChangeInvoiceToDelivery(InvoiceVO invVO, EntrustVO entVO);

	/**
	 * 选择多条发货单执行签收动作
	 * 
	 * @param pk_invoice
	 * @param podVO
	 * @param paramVO
	 * @return
	 */
	public List<Map<String, Object>> doPod(String[] pk_invoice, PodVO newPodVO, ParamVO paramVO);

	/**
	 * 选择一条记录进行异常签收
	 * 
	 * @param pk_invoice
	 * @param podVO
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> doExpPod(String pk_invoice, AggregatedValueObject billVO, ParamVO paramVO);

	/**
	 * 选择多条发货单执行撤销签收动作
	 * 
	 * @param pk_invoice
	 * @param paramVO
	 * @return
	 */
	public List<Map<String, Object>> doUnpod(String[] pk_invoice, ParamVO paramVO);

	/**
	 * 选择多条发货单执行回单动作
	 * 
	 * @param pk_invoice
	 * @param podVO
	 * @param paramVO
	 * @return
	 */
	public List<Map<String, Object>> doReceipt(String[] pk_invoice, PodVO newPodVO, ParamVO paramVO);

	/**
	 * 选择一条记录进行异常回单
	 * 
	 * @param pk_invoice
	 * @param podVO
	 * @param paramVO
	 * @return
	 */
	public Map<String, Object> doExpReceipt(String pk_invoice, PodVO newPodVO, ParamVO paramVO);

	/**
	 * 选择多条发货单执行撤销回单动作
	 * 
	 * @param pk_invoice
	 * @param paramVO
	 * @return
	 */
	public List<Map<String, Object>> doUnreceipt(String[] pk_invoice, ParamVO paramVO);
	
	public String fileupload(MultipartFile file, String originalFilename,String vbillnoOrCustOrderno);
}
