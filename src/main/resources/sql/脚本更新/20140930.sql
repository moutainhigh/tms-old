alter table ts_inv_pack_b alter column  serialno int;
alter table ts_seg_pack_b alter column  serialno int;
alter table ts_ent_line_b alter column  serialno int;
alter table ts_ent_line_b alter column  addr_flag varchar(50);
alter table ts_ent_line_b alter column  pk_segment varchar(500);

alter table ts_ent_transbility_b add gps_id varchar(50);

alter table ts_pay_detail add check_no varchar(300);
alter table ts_pay_detail add check_head varchar(300);
alter table ts_receive_detail add check_no varchar(300);
alter table ts_receive_detail add check_head varchar(300);

alter table nw_portlet add query_where varchar(1000);

