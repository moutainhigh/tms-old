package com.tms.service.edi;

import org.nw.service.IBillService;

public interface EdiInvoiceService extends IBillService {

	public void sync(String[] pk_invoices) ;
	
	public void cancel(String[] pk_invoices) ;
}
