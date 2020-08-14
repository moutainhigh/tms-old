alter table ts_car add gps_id varchar(50);
--修改配载模板中车牌号的显示公式
--pk_carrier,pk_car_type,pk_driver,gps_id->getcolsvalue(ts_car,pk_carrier,pk_car_type,pk_driver,gps_id,carno,carno);

alter table ts_entrust add gps_id varchar(50);
--调整委托单运力信息的模板 

--调整在途跟踪模板

--委托单的车牌号、车型、司机不能编辑