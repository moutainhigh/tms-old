package com.tms.web.edi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nw.web.AbsBillController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tms.service.edi.EdiInvoiceService;


@Controller
@RequestMapping(value = "/edi/inv")
public class EdiInvoiceController extends AbsBillController {
	@Autowired
	private EdiInvoiceService ediInvoiceService;
	@Override
	public EdiInvoiceService getService() {
		return ediInvoiceService;
	}
	
	
	@RequestMapping(value = "/sync.json")
	@ResponseBody
	public void sync(HttpServletRequest request, HttpServletResponse response){
		String[] pk_invoices = request.getParameterValues("pk_invoice");
		this.getService().sync(pk_invoices);
	}
	
	
	@RequestMapping(value = "/cancel.json")
	@ResponseBody
	public void cancel(HttpServletRequest request, HttpServletResponse response){
		String[] pk_invoices = request.getParameterValues("pk_invoice");
		this.getService().cancel(pk_invoices);
	}
	
	
}
