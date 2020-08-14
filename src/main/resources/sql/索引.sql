

=====发货单索引=========



/****** Object:  Index [INDEX_status_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_invoice] 
(
	[vbillno] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====================



/****** Object:  Index [INDEX_status_customer_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_customer] ON [dbo].[ts_invoice] 
(
	[pk_customer] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====================



/****** Object:  Index [INDEX_vbillno_custno_ord_status]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_cust_orderno] ON [dbo].[ts_invoice] 
(

	[cust_orderno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


====================



/****** Object:  Index [INDEX_vbillno_custno_ord_status]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_orderno] ON [dbo].[ts_invoice] 
(

	[orderno] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO



=====应收明细索引===



/****** Object:  Index [INDEX_status_invoice]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_invoice_vbillno] ON [dbo].[ts_receive_detail] 
(
	[invoice_vbillno] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

===================


/****** Object:  Index [INDEX_invoice_status_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_receive_detail] 
(

	[vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

===================


/****** Object:  Index [INDEX_invoice_status_customer_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_customer] ON [dbo].[ts_receive_detail] 
(
	[pk_customer] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====应收对账索引===



/****** Object:  Index [INDEX_status_vbillno_customer]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_rece_check_sheet] 
(
	[vbillno] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO
===================

/****** Object:  Index [INDEX_status_vbillno_customer]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_customer] ON [dbo].[ts_rece_check_sheet] 
(
	[pk_customer] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

=====运段索引===


/****** Object:  Index [INDEX_status_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_segment] 
(
	[vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====================

/****** Object:  Index [INDEX_invoice_status_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_invoice_vbillno] ON [dbo].[ts_segment] 
(

	[invoice_vbillno] ASC
	
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====================



=====委托单索引===

/****** Object:  Index [INDEX_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_entrust] 
(
	[vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

================


/****** Object:  Index [INDEX_vbillno_carrier_vbillstatus]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_carrier] ON [dbo].[ts_entrust] 
(

	[plan_carrier] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


================

/****** Object:  Index [INDEX_serialno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_serialno_invoice] ON [dbo].[ts_ent_pack_b] 
(
	[pk_entrust] ASC,
	[pk_invoice] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====应付明细索引===


/****** Object:  Index [INDEX_status_entrust_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_pay_detail] 
(
	[vbillno] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

=====


/****** Object:  Index [INDEX_status_entrust_carrier_vbillno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_entrust_vbillno] ON [dbo].[ts_pay_detail] 
(
	[entrust_vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO



=====应付对账索引===


/****** Object:  Index [INDEX_status_vbillno_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_vbillno] ON [dbo].[ts_pay_check_sheet] 
(
	[vbillno] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


==========


/****** Object:  Index [INDEX_status_vbillno_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_carrier] ON [dbo].[ts_pay_check_sheet] 
(
	[pk_carrier] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO




=====在途跟踪索引===


/****** Object:  Index [INDEX_entrust]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_entrust] ON [dbo].[ts_ent_tracking] 
(
	[entrust_vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====

/****** Object:  Index [INDEX_invoice_entrust]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_invoice_entrust] ON [dbo].[ts_ent_tracking] 
(
	[invoice_vbillno] ASC,

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====POD索引===



/****** Object:  Index [INDEX_invoice]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_invoice] ON [dbo].[ts_pod] 
(
	[pk_invoice] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====

/****** Object:  Index [INDEX_invoice_entrust_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_entrust_vbillno] ON [dbo].[ts_pod] 
(
	[pod_entrust_vbillno] 

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====合同索引===


/****** Object:  Index [INDEX_code_name_type]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_code] ON [dbo].[ts_contract] 
(
	[code] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

======

/****** Object:  Index [INDEX_code_name_type_customer]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_name] ON [dbo].[ts_contract] 
(
	[name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====地址索引===

/****** Object:  Index [INDEX_code_address]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_addr_code] ON [dbo].[ts_address] 
(
	[addr_code] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

====


/****** Object:  Index [INDEX_code_name_address]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_addr_name] ON [dbo].[ts_address] 
(
	[addr_name] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

=====供应商索引===


/****** Object:  Index [INDEX_code_supplier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_supp_code] ON [dbo].[ts_supplier] 
(
	[supp_code] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====客户索引===


/****** Object:  Index [INDEX_code_ustomer]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_cust_code] ON [dbo].[ts_customer] 
(
	[cust_code] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====承运商索引===

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_carr_code] ON [dbo].[ts_carrier] 
(
	[carr_code] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============
=============

辅助表
=====发货单辅助表===

/****** Object:  Index [INDEX_orderno]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_orderno] ON [dbo].[ts_order_ass] 
(
	[orderno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====批次表===

/****** Object:  Index [INDEX_lot]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_lot] ON [dbo].[ts_orderlot] 
(
	[lot] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============

/****** Object:  Index [INDEX_pk_orderlot]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_pk_orderlot] ON [dbo].[ts_orderlot] 
(
	[pk_orderlot] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO



=====批次费用表===

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_lot] ON [dbo].[ts_orderlot_rd] 
(
	[lot] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_pk_orderlot_rd] ON [dbo].[ts_orderlot_rd] 
(
	[pk_orderlot_rd] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====批次订单表===

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_pk_orderlot_inv] ON [dbo].[ts_orderlot_inv] 
(
	[pk_orderlot_inv] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============
/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_lot] ON [dbo].[ts_orderlot_inv] 
(
	[lot] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============
/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_invoice_vbillno] ON [dbo].[ts_orderlot_inv] 
(
	[invoice_vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

================
/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_rd_vbillnoe] ON [dbo].[ts_orderlot_inv] 
(
	[rd_vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


=====批次费用分摊表===

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_pk_orderlot_devi] ON [dbo].[ts_orderlot_devi] 
(
	[pk_orderlot_devi] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_pk_orderlot_rd] ON [dbo].[ts_orderlot_devi] 
(
	[pk_orderlot_rd] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_lot] ON [dbo].[ts_orderlot_devi] 
(
	[lot] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_invoice_vbillno] ON [dbo].[ts_orderlot_devi] 
(
	[invoice_vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

============

/****** Object:  Index [INDEX_code_carrier]    Script Date: 06/08/2015 17:17:15 ******/
CREATE NONCLUSTERED INDEX [INDEX_rd_vbillno] ON [dbo].[ts_orderlot_devi] 
(
	[rd_vbillno] ASC

)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

================

