
alter table nw_data_dict_b add  dr int;
alter table nw_data_dict_b add  ts char(19);

alter table nw_user add pk_customer varchar(50);
alter table nw_user add pk_carrier varchar(50);


/*==============================================================*/
/* Table: nw_portlet_plan                                       */
/*==============================================================*/
create table nw_portlet_plan (
   pk_portlet_plan      varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   plan_code            varchar(50)          null,
   plan_name            varchar(50)          null,
   if_default           char(1)              null,
   locked_flag          char(1)              null,
   create_time          varchar(50)          null,
   create_user          varchar(50)          null,
   pk_corp              varchar(50)          null,
   modify_time          char(19)             null,
   modify_user          varchar(50)          null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   def6                 varchar(50)          null,
   def7                 varchar(50)          null,
   def8                 varchar(50)          null,
   def9                 varchar(50)          null,
   def10                varchar(50)          null,
   memo                 varchar(200)         null,
   constraint pk_nw_portlet_plan primary key nonclustered (pk_portlet_plan)
)
go


/*==============================================================*/
/* Table: nw_portlet_plan_b                                     */
/*==============================================================*/
create table nw_portlet_plan_b (
   pk_portlet_plan_b    varchar(50)          not null,
   pk_portlet_plan      varchar(50)          null,
   dr                   integer              null,
   ts                   char(19)             null,
   portlet_id           varchar(50)          null,
   column_index         int                  null,
   display_order        int                  null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   constraint pk_nw_portlet_plan_b primary key nonclustered (pk_portlet_plan_b)
)
go


/*==============================================================*/
/* Table: nw_role_plan                                          */
/*==============================================================*/
create table nw_role_plan (
   pk_role_plan         varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   pk_portlet_plan      varchar(50)          null,
   pk_role              varchar(50)          null,
   constraint pk_nw_role_plan primary key nonclustered (pk_role_plan)
)
go

alter table ts_invoice alter column deli_process varchar(500);
alter table ts_invoice alter column arri_process varchar(500);
alter table ts_invoice alter column note varchar(500);
alter table ts_address add  deli_process varchar(500);
alter table ts_address add  arri_process varchar(500);

--修改发货单中提货方和收货方的编辑公式

alter table ts_customer add billing_rule int;

/*==============================================================*/
/* Table: ts_cust_rate                                          */
/*==============================================================*/
create table ts_cust_rate (
   pk_cust_fee          varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   pk_customer          varchar(50)          null,
   pk_trans_type        varchar(50)          null,
   rate                 decimal(20,8)        null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   def6                 varchar(100)         null,
   def7                 varchar(100)         null,
   def8                 varchar(100)         null,
   def9                 varchar(200)         null,
   def10                varchar(200)         null,
   def11                decimal(20,8)        null,
   def12                decimal(20,8)        null,
   constraint pk_ts_cust_rate primary key nonclustered (pk_cust_fee)
)
go

declare @cmtts_cust_rate varchar(128)
select @cmtts_cust_rate = user_name()
execute sp_addextendedproperty 'MS_Description', 
   '客户的体积重换算比',
   'user', @cmtts_cust_rate, 'table', 'ts_cust_rate'
go

alter table ts_carrier add billing_rule int;


/*==============================================================*/
/* Table: ts_supp_addr                                          */
/*==============================================================*/
create table ts_supp_addr (
   pk_supp_addr         varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   pk_supplier          varchar(50)          null,
   pk_address           varchar(50)          null,
   memo                 varchar(200)         null,
   if_default           char(1)              null,
   locked_flag          char(1)              null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   def6                 varchar(100)         null,
   def7                 varchar(100)         null,
   def8                 varchar(100)         null,
   def9                 varchar(200)         null,
   def10                varchar(200)         null,
   def11                decimal(20,8)        null,
   def12                decimal(20,8)        null,
   constraint pk_ts_supp_addr primary key (pk_supp_addr)
)
go

declare @cmtts_supp_addr varchar(128)
select @cmtts_supp_addr = user_name()
execute sp_addextendedproperty 'MS_Description', 
   '供应商收发货地址',
   'user', @cmtts_supp_addr, 'table', 'ts_supp_addr'
go


/*==============================================================*/
/* Table: ts_carr_rate                                          */
/*==============================================================*/
create table ts_carr_rate (
   pk_carr_rate         varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   pk_carrier           varchar(50)          null,
   pk_trans_type        varchar(50)          null,
   rate                 decimal(20,8)        null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   def6                 varchar(100)         null,
   def7                 varchar(100)         null,
   def8                 varchar(100)         null,
   def9                 varchar(200)         null,
   def10                varchar(200)         null,
   def11                decimal(20,8)        null,
   def12                decimal(20,8)        null,
   constraint pk_ts_carr_rate primary key nonclustered (pk_carr_rate)
)
go

declare @cmtts_carr_rate varchar(128)
select @cmtts_carr_rate = user_name()
execute sp_addextendedproperty 'MS_Description', 
   '成运商的体积重换算比',
   'user', @cmtts_carr_rate, 'table', 'ts_carr_rate'
go


--发货单
alter table ts_invoice add act_deli_date char(19);
alter table ts_invoice add act_arri_date char(19);
alter table ts_invoice add con_arri_date char(19);
alter table ts_invoice add item_name varchar(100);
alter table ts_invoice add item_code varchar(100);

--运段
alter table ts_segment add  delegate_status int;
alter table ts_segment add  delegate_corp varchar(50);
alter table ts_segment add  delegate_user varchar(50);
alter table ts_segment add  delegate_time char(19);


/*==============================================================*/
/* Table: ts_order_ass                                          */
/*==============================================================*/
create table ts_order_ass (
   pk_order_ass         varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   order_type           varchar(10)          null,
   orderno              varchar(50)          null,
   item_name            varchar(50)          null,
   pk_supplier          varchar(50)          null,
   pk_delivery          varchar(50)          null,
   deli_city            varchar(50)          null,
   pk_arrival           varchar(50)          null,
   arri_city            varchar(50)          null,
   req_deli_date        char(19)             null,
   act_deli_date        char(19)             null,
   req_arri_date        char(19)             null,
   act_arri_date        char(19)             null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   def6                 varchar(100)         null,
   def7                 varchar(100)         null,
   def8                 varchar(100)         null,
   def9                 varchar(200)         null,
   def10                varchar(200)         null,
   def11                decimal(20,8)        null,
   def12                decimal(20,8)        null,
   constraint pk_ts_order_ass primary key nonclustered (pk_order_ass)
)
go

declare @cmtts_order_ass varchar(128)
select @cmtts_order_ass = user_name()
execute sp_addextendedproperty 'MS_Description', 
   '订单辅助表',
   'user', @cmtts_order_ass, 'table', 'ts_order_ass'
go

--增加参数配置
INSERT INTO nw_parameter (pk_parameter, dr, ts, type, param_name, param_value, memo, pk_corp, create_user, create_time, modify_user, modify_time, def1, def2, def3, def4, def5, def6, def7, def8, def9, def10) VALUES ('b7daa13830574186bd26bcc8b8d08067', 0, '2015-05-29 23:45:10', 1, 'use_order_ass', 'N', '是否启用订单辅助表', '0001', '0001', '2015-05-29 23:45:10', null, null, null, null, null, null, null, null, null, null, null, null);
--客服要求提货日期
alter table ts_invoice add cs_req_deli_date char(19);
--解决委托单运力信息，司机显示成pk的问题
update nw_billtemplet_b set loadformula='pk_driver1->pk_driver;pk_driver->getcolvalue(ts_driver,driver_name,pk_driver,pk_driver);pk_driver->iif(pk_driver==null,pk_driver1,pk_driver);' where pk_billtemplet_b='20322422331B0001cDF4';

--费用类型增加所属大类
alter table ts_expense_type add parent_type int;
--合同增加自定义算法
alter table ts_contract_b add custom_proc varchar(100);
alter table ts_contract_b add if_return char(1);

--车型吨位对照

/*==============================================================*/
/* Table: ts_cartype_tonnage                                    */
/*==============================================================*/
create table ts_cartype_tonnage (
   pk_cartype_tonnage   varchar(50)          not null,
   dr                   integer              null,
   ts                   char(19)             null,
   pk_expense_type      varchar(50)          null,
   tonnage              int                  null,
   num                  int                  null,
   pk_corp              varchar(50)          null,
   if_preset            char(1)              null,
   memo                 varchar(200)         null,
   def1                 varchar(50)          null,
   def2                 varchar(50)          null,
   def3                 varchar(50)          null,
   def4                 varchar(50)          null,
   def5                 varchar(50)          null,
   def6                 varchar(100)         null,
   def7                 varchar(100)         null,
   def8                 varchar(100)         null,
   def9                 varchar(200)         null,
   def10                varchar(200)         null,
   def11                decimal(20,8)        null,
   def12                decimal(20,8)        null,
   constraint pk_ts_cartype_tonnage primary key nonclustered (pk_cartype_tonnage)
)
go

declare @cmtts_cartype_tonnage varchar(128)
select @cmtts_cartype_tonnage = user_name()
execute sp_addextendedproperty 'MS_Description', 
   '车型吨位换算',
   'user', @cmtts_cartype_tonnage, 'table', 'ts_cartype_tonnage'
go

--定时任务
alter table nw_job_def add exec_type int;
alter table nw_job_def add exec_time varchar(200);
alter table nw_job_def drop column repeat_time;

alter table ts_receive_detail add weight_count decimal(20,8);
alter table ts_pay_detail add weight_count decimal(20,8);

alter table ts_rece_detail_b add pk_contract_b varchar(50);
alter table ts_pay_detail_b add pk_contract_b varchar(50);