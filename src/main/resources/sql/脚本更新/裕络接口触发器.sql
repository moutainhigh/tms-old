if (object_id('tgr_ORDER_RELEASE_insert', 'tr') is not null)
    drop trigger tgr_ORDER_RELEASE_insert
go
create trigger tgr_ORDER_RELEASE_insert
on ORDER_RELEASE
    for insert --插入触发
as
    --定义变量
    declare @ERP_DELIVERY_NO varchar(50), --客户订单号
            @LOGISTICS_ORDER_GID varchar(50), --订单号
            @CUST_ID varchar(50),--客户名称
            @BU_GID varchar(50),--结算客户
            @TRANSPORT_MODE_GID varchar(50),--运输方式
            @LATE_PICKUP_DATE char(19),--要求提货日期
            @LATE_DELIVERY_DATE char(19),--要求到货日期
            @SRC_LOCATION_GID varchar(50),--提货方编码
            @SRC_LOCATION_NAME varchar(50),--提货方
            @SRC_CITY_GID varchar(50),--提货城市
            @SRC_PROVINCE varchar(50),--提货省份
            @SRC_CONTACT_NAME varchar(50),--提货联系人
            @SRC_PHONE varchar(50),--提货联系电话
            @SRC_TEL varchar(50),--提货联系手机
            @SRC_FAX varchar(50),--提货联系邮箱
            @SRC_ADDRESS varchar(200),--提货地址
            @DEST_LOCATION_GID varchar(50),--收货方编码
            @DEST_LOCATION_NAME varchar(50),--收货方
            @DEST_CITY_GID varchar(50),--收货城市
            @DEST_PROVINCE varchar(50),--收货省份
            @DEST_CONTACT_NAME varchar(50),--收货联系人
            @DEST_PHONE varchar(50),--收货联系手机
            @DEST_TEL varchar(50),--收货联系电话
            @DEST_FAX varchar(50),--收货联系邮箱
            @DEST_ADDRESS varchar(200),--收货地址
            @CREATED_BY varchar(50),--创建人
            @CREATED_DATE char(19),--创建时间
            @DOMAIN_NAME varchar(50),--域
            @ORDER_RELEASE_GID varchar(50)--发货单号

    select 
    		@ERP_DELIVERY_NO 		=ERP_DELIVERY_NO,
    	   	@LOGISTICS_ORDER_GID 	=LOGISTICS_ORDER_GID,
    	   	@CUST_ID				=CUST_ID,
            @BU_GID					=BU_GID,
            @TRANSPORT_MODE_GID		=TRANSPORT_MODE_GID,
            @LATE_PICKUP_DATE		=CONVERT(varchar(19),LATE_PICKUP_DATE,120),
            @LATE_DELIVERY_DATE		=CONVERT(varchar(19),LATE_DELIVERY_DATE,120),
            @SRC_LOCATION_GID		=SRC_LOCATION_GID,
            @SRC_LOCATION_NAME		=SRC_LOCATION_NAME,
            @SRC_CITY_GID			=SRC_CITY_GID,
            @SRC_PROVINCE			=SRC_PROVINCE,
            @SRC_CONTACT_NAME		=SRC_CONTACT_NAME,
            @SRC_PHONE				=SRC_PHONE,
            @SRC_TEL				=SRC_TEL,
            @SRC_FAX				=SRC_FAX,
            @SRC_ADDRESS			=SRC_ADDRESS,
            @DEST_LOCATION_GID		=DEST_LOCATION_GID,
            @DEST_LOCATION_NAME		=DEST_LOCATION_NAME,
            @DEST_CITY_GID			=DEST_CITY_GID,
            @DEST_PROVINCE			=DEST_PROVINCE,
            @DEST_CONTACT_NAME		=DEST_CONTACT_NAME,
            @DEST_PHONE				=DEST_PHONE,
            @DEST_TEL				=DEST_TEL,
            @DEST_FAX				=DEST_FAX,
            @DEST_ADDRESS			=DEST_ADDRESS,
            @CREATED_BY				=CREATED_BY,
            @CREATED_DATE			=CONVERT(varchar(19),CREATED_DATE,120),
            @DOMAIN_NAME			=DOMAIN_NAME,
            @ORDER_RELEASE_GID		=ORDER_RELEASE_GID 
   	from inserted
   	--上面是声明的部分，不能使用逗号结尾
    insert into [TMS].[dbo].edi_invoice(
    	cust_orderno,orderno,pk_customer,bala_customer,pk_trans_type,req_deli_date,req_arri_date,
    	deli_code,pk_delivery,deli_city,deli_province,deli_contact,deli_mobile,deli_phone,deli_email,deli_detail_addr,
    	arri_code,pk_arrival,arri_city,arri_province,arri_contact,arri_mobile,arri_phone,arri_email,arri_detail_addr,
    	create_user,create_time,pk_invoice,def3) 
    values(
    	@ERP_DELIVERY_NO,@LOGISTICS_ORDER_GID,@CUST_ID,@BU_GID,@TRANSPORT_MODE_GID,@LATE_PICKUP_DATE,@LATE_DELIVERY_DATE,
    	@SRC_LOCATION_GID,@SRC_LOCATION_NAME,@SRC_CITY_GID,@SRC_PROVINCE,@SRC_CONTACT_NAME,@SRC_PHONE,@SRC_TEL,@SRC_FAX,@SRC_ADDRESS,
    	@DEST_LOCATION_GID,@DEST_LOCATION_NAME,@DEST_CITY_GID,@DEST_PROVINCE,@DEST_CONTACT_NAME,@DEST_PHONE,@DEST_TEL,@DEST_FAX,@DEST_ADDRESS,
    	@CREATED_BY,@CREATED_DATE,@ORDER_RELEASE_GID,@DOMAIN_NAME
    )

go

if (object_id('tgr_ORDER_RELEASE_LINE_insert', 'tr') is not null)
    drop trigger tgr_ORDER_RELEASE_LINE_insert
go
create trigger tgr_ORDER_RELEASE_LINE_insert
on ORDER_RELEASE_LINE
    for insert --插入触发
as
    --定义变量
    declare @LINE_ID varchar(50), --行号
            @ITEM_GID varchar(50), --货品编码
            @ITEM_NAME varchar(50),--货品名称
            @TRANS_PACKAGE_COUNT int,--件数
            @TOTAL_GROSS_WEIGHT decimal(20,8),--重量
            @TOTAL_GROSS_VOLUME decimal(20,8),--体积
            @PACKAGE_COUNT decimal(20,8),--数量，目标系统是整数
            @NET_WEIGHT_BASE decimal(20,8),--单位重量
            @NET_VOLUME_BASE decimal(20,8),--单位体积
            @PACKAGING_UOM varchar(50),--包装单位
            @TRANS_UOM varchar(50),--最小包装
            @LENGTH decimal(20,8),--长
            @WIDTH decimal(20,8),--宽
            @HEIGHT decimal(20,8),--高
            @ORDER_RELEASE_GID varchar(50),--发货单号
            @LENGTH_UOM varchar(50),
            @LENGTH_BASE decimal(20,8),--目标系统是string
            @WIDTH_UOM varchar(50),
            @WIDTH_BASE decimal(20,8),--目标系统是string
            @HEIGHT_BASE decimal(20,8),--目标系统是string
            @TOTAL_GROSS_VOLUME_UOM varchar(50),
            @NET_WEIGHT decimal(20,8),--目标系统是string
            @NET_VOLUME decimal(20,8)
    select 
    		@LINE_ID			=LINE_ID,
            @ITEM_GID			=ITEM_GID,
            @ITEM_NAME			=ITEM_NAME,
            @TRANS_PACKAGE_COUNT=TRANS_PACKAGE_COUNT,
            @TOTAL_GROSS_WEIGHT	=TOTAL_GROSS_WEIGHT,
            @TOTAL_GROSS_VOLUME =TOTAL_GROSS_VOLUME,
            @PACKAGE_COUNT		=PACKAGE_COUNT,
            @NET_WEIGHT_BASE	=NET_WEIGHT_BASE,
            @NET_VOLUME_BASE	=NET_VOLUME_BASE,
            @PACKAGING_UOM		=PACKAGING_UOM,
            @TRANS_UOM			=TRANS_UOM,
            @LENGTH 			=LENGTH,
            @WIDTH				=WIDTH,
            @HEIGHT				=HEIGHT,
            @ORDER_RELEASE_GID	=ORDER_RELEASE_GID,
            @LENGTH_UOM			=LENGTH_UOM,
            @LENGTH_BASE		=LENGTH_BASE,
            @WIDTH_UOM			=WIDTH_UOM,
            @WIDTH_BASE			=WIDTH_BASE,
            @HEIGHT_BASE		=HEIGHT_BASE,
            @TOTAL_GROSS_VOLUME_UOM	=TOTAL_GROSS_VOLUME_UOM,
            @NET_WEIGHT			=NET_WEIGHT,
            @NET_VOLUME			=NET_VOLUME
   	from inserted
   	
    insert into [TMS].[dbo].edi_inv_pack_b(pk_inv_pack_b,
    	serialno,goods_code,goods_name,num,weight,volume,pack_num_count,
    	unit_weight,unit_volume,pack,min_pack,length,width,height,pk_invoice,def3,def4,def5,def7,def8,def9,def10,def11) 
    values(@ORDER_RELEASE_GID+'_'+@LINE_ID,
    	@LINE_ID,@ITEM_GID,@ITEM_NAME,@TRANS_PACKAGE_COUNT,@TOTAL_GROSS_WEIGHT,@TOTAL_GROSS_VOLUME,convert(int,@PACKAGE_COUNT),
    	@NET_WEIGHT_BASE,@NET_VOLUME_BASE,@PACKAGING_UOM,@TRANS_UOM,@LENGTH,@WIDTH,@HEIGHT,
    	@ORDER_RELEASE_GID,@LENGTH_UOM,convert(varchar(50),@LENGTH_BASE),@WIDTH_UOM,convert(varchar(50),@WIDTH_BASE),convert(varchar(50),@HEIGHT_BASE),@TOTAL_GROSS_VOLUME_UOM,convert(varchar(50),@NET_WEIGHT),@NET_VOLUME
    )
go