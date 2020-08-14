if exists (select 1
            from  sysobjects
           where  id = object_id('nw_job_def')
            and   type = 'U')
   drop table nw_job_def
go

/*==============================================================*/
/* Table: nw_job_def                                            */
/*==============================================================*/
/* tablename: nw_job_def */

create table nw_job_def (
pk_job_def           varchar(50)          not null
      /*pk_job_def*/,
dr                   integer              null
      /*删除标识*/,
ts                   char(19)             null
      /*更新时间戳*/,
job_code             varchar(50)          null
      /*job_code*/,
job_name             varchar(50)          null
      /*job_name*/,
begin_date           char(10)             null
      /*开始日期*/,
end_date             char(10)             null
      /*结束日期*/,
interval             int                  null
      /*获取时间(分)*/,
repeat_time          int                  null
      /*重复次数*/,
busi_clazz           varchar(100)         null
      /*业务类*/,
convert_clazz        varchar(100)         null
      /*业务转换类*/,
memo                 varchar(200)         null
      /*备注*/,
pk_corp              varchar(50)          null
      /*公司*/,
def1                 varchar(50)          null
      /*def1*/,
def2                 varchar(50)          null
      /*def2*/,
def3                 varchar(50)          null
      /*def3*/,
def4                 varchar(50)          null
      /*def4*/,
def5                 varchar(50)          null
      /*def5*/,
def6                 varchar(50)          null
      /*def6*/,
def7                 varchar(50)          null
      /*def7*/,
def8                 varchar(50)          null
      /*def8*/,
def9                 varchar(50)          null
      /*def9*/,
def10                varchar(50)          null
      /*def10*/,
create_user          varchar(50)          null
      /*创建人*/,
create_time          char(19)             null
      /*创建时间*/,
modify_user          varchar(50)          null
      /*修改人*/,
modify_time          char(19)             null
      /*修改时间*/,
locked_flag          char(1)              null
      /*是否锁定*/,
api_type             varchar(10)          null
      /*接口类型*/,
url                  varchar(200)         null
      /*url*/,
url_method           varchar(50)          null
      /*url_method*/,
username_param       varchar(50)          null
      /*username_param*/,
password_param       varchar(50)          null
      /*password_param*/,
username             varchar(50)          null
      /*username*/,
password             varchar(50)          null
      /*password*/,
constraint pk_nw_job_def primary key  (pk_job_def)
)
go




alter table ts_invoice add modify_user varchar(50);
alter table ts_invoice add modify_time char(19);

alter table ts_pod add modify_user varchar(50);
alter table ts_pod add modify_time char(19);

alter table ts_segment add modify_user varchar(50);
alter table ts_segment add modify_time char(19);

alter table ts_entrust add modify_user varchar(50);
alter table ts_entrust add modify_time char(19);

alter table ts_exp_accident add modify_user varchar(50);
alter table ts_exp_accident add modify_time char(19);

alter table ts_receive_detail add modify_user varchar(50);
alter table ts_receive_detail add modify_time char(19);

alter table ts_rece_check_sheet add modify_user varchar(50);
alter table ts_rece_check_sheet add modify_time char(19);

alter table ts_pay_detail add modify_user varchar(50);
alter table ts_pay_detail add modify_time char(19);

alter table ts_pay_check_sheet add modify_user varchar(50);
alter table ts_pay_check_sheet add modify_time char(19);

