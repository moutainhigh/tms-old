
alter table ts_invoice add backbill_num int;
--发货单模板增加回单数字段
--修改签收回单模板，发货单的显示公式
--修改发货单模板，结算客户为可修订
--修改合同模板，计价方式增加数量选项

alter table ts_inv_pack_b alter column pack_num_count decimal(20,8);
alter table ts_seg_pack_b alter column pack_num_count decimal(20,8);
alter table ts_ent_pack_b alter column pack_num_count decimal(20,8);
alter table ts_inv_pack_b alter column plan_pack_num_count decimal(20,8);
alter table ts_seg_pack_b alter column plan_pack_num_count decimal(20,8);
alter table ts_ent_pack_b alter column plan_pack_num_count decimal(20,8);


alter table ts_invoice add pack_num_count decimal(20,8);
--发货单模板增加总数量，类型为小数
alter table ts_entrust add pack_num_count decimal(20,8);
--委托单模板增加总数量，类型为小数
alter table ts_segment add pack_num_count decimal(20,8);
--运输计划调度配载模板增加总数量
alter table ts_receive_detail add pack_num_count decimal(20,8);
--应收明细模板增加总数量
alter table ts_pay_detail add pack_num_count decimal(20,8);
--应付明细模板增加总数量
--配载单据模板表头和表体增加总数量

alter table ts_contract_b add tax_cat int;
alter table ts_contract_b add tax_rate decimal(20,8);
--修改数据字典，增加：税种、税率
--合同的模板,增加税种和税率
--导入字段增加tax_cat,tax_rate字段
--上传新的合同导入模板

alter table ts_receive_detail add tax_cat int;
alter table ts_receive_detail add tax_rate decimal(20,8);
alter table ts_receive_detail add taxmny decimal(20,8);
--应收明细模板增加这3个字段

alter table ts_pay_detail add tax_cat int;
alter table ts_pay_detail add tax_rate decimal(20,8);
alter table ts_pay_detail add taxmny decimal(20,8);
--应付明细模板增加这3个字段
