alter table ts_expense_type add if_preset char(1);
insert into nw_billtemplet_b (cardflag, datatype, defaultshowname, defaultvalue, dr, editflag, editformula, foreground, idcolname, inputlength, itemkey, itemtype, leafflag, listflag, listshowflag, loadformula, lockflag, metadatapath, metadataproperty, metadatarelation, newlineflag, nullflag, OPTIONS, pk_billtemplet, pk_billtemplet_b, pk_corp, pos, reftype, resid, resid_tabname, reviseflag, showflag, showorder, table_code, table_name, totalflag, ts, userdefflag, userdefine1, userdefine2, userdefine3, usereditflag, userflag, userreviseflag, usershowflag, validateformula, width) values (1, 4, '是否预置', null, 0, 0, null, 0, null, 1, 'if_preset', 0, 'N', 1, 'Y', null, 0, null, null, null, 'N', 0, null, '0001A410000000000LNQ', '1720236736910001JkY7', '@@@@', 1, null, null, null, 'N', 1, 4, 'ts_expense_type', 'ts_expense_type', 0, '2014-12-29 17:29:41', 'N', null, null, null, 0, 1, 'N', 0, null, 100);
update ts_expense_type set if_preset='Y' where name in ('运费','客户索赔','承运商索赔','保险费');
insert into nw_ref_relation (pk_ref_relation, dr, ts, referencedtablekey, referencedtablename, referencingtablecolumn, referencingtablename, ismodifycheck, create_user, create_time) values ('6d73a05f1b674eeeb42932be06edfab4', 0, '2014-12-29 17:28:01', 'pk_expense_type', 'ts_expense_type', 'pk_expense_type', 'ts_pay_detail_b', 'N', '0001', '2014-12-29 17:28:01');
insert into nw_ref_relation (pk_ref_relation, dr, ts, referencedtablekey, referencedtablename, referencingtablecolumn, referencingtablename, ismodifycheck, create_user, create_time) values ('893ac93420774e69b8975cc8d69f62f3', 0, '2014-12-29 17:27:48', 'pk_expense_type', 'ts_expense_type', 'pk_expense_type', 'ts_rece_detail_b', 'N', '0001', '2014-12-29 17:27:48');
insert into nw_ref_relation (pk_ref_relation, dr, ts, referencedtablekey, referencedtablename, referencingtablecolumn, referencingtablename, ismodifycheck, create_user, create_time) values ('a7ae0ca2822c47f082b8034366c002a7', 0, '2014-12-29 17:27:02', 'pk_expense_type', 'ts_expense_type', 'pk_expense_type', 'ts_contract_b', 'N', '0001', '2014-12-29 17:26:11');

--数据调整
update ts_rece_detail_b set pk_expense_type='4468028d8b6f4fe3b4f163c29ed74245' where ts > '2014-12-29' and pk_expense_type in ('c1ed7838e57148ffbfd9295b338681dd','2543af2ea95343ccbb245b9402109f38','c1ed7838e57148ffbfd9295b338681dd');
update ts_expense_type set dr=1 where pk_expense_type in ('c1ed7838e57148ffbfd9295b338681dd','2543af2ea95343ccbb245b9402109f38','c1ed7838e57148ffbfd9295b338681dd');
update ts_expense_type set dr=0 where pk_expense_type='4468028d8b6f4fe3b4f163c29ed74245';
update ts_rece_detail_b set pk_expense_type='b980f421ca4b4f689b261254bcdbe445' where ts > '2014-12-29' and pk_expense_type in ('f00281796ab44033bf4ec12110c6b843','90f9e17c72c04b17b71ba1ff40e97612');
update ts_expense_type set dr=1 where pk_expense_type in ('f00281796ab44033bf4ec12110c6b843','90f9e17c72c04b17b71ba1ff40e97612');
update ts_expense_type set dr=0 where pk_expense_type='b980f421ca4b4f689b261254bcdbe445';
update ts_rece_detail_b set pk_expense_type='899f3b85bea84a39985964947cd5928b' where ts > '2014-12-29' and pk_expense_type in ('ea67a3ea78b549cc946a0872301d845f','0fa812d9465948268061b3d8eb543db0');
update ts_expense_type set dr=1 where pk_expense_type in ('ea67a3ea78b549cc946a0872301d845f','0fa812d9465948268061b3d8eb543db0');
update ts_expense_type set dr=0 where pk_expense_type='899f3b85bea84a39985964947cd5928b';
update ts_rece_detail_b set pk_expense_type='918c4465a9e34f7087ec0d591fa6be46' where ts > '2014-12-29' and pk_expense_type in ('854b1a3666f74529a1dd6f3e4d713d7e','30e7afda8c9b4db485c4fc3adc94e6f7');
update ts_expense_type set dr=1 where pk_expense_type in ('854b1a3666f74529a1dd6f3e4d713d7e','30e7afda8c9b4db485c4fc3adc94e6f7');
update ts_expense_type set dr=0 where pk_expense_type='918c4465a9e34f7087ec0d591fa6be46';

update ts_rece_detail_b set pk_expense_type='a9ad2bc825bd4ceabcc2695a2581250a' where ts > '2014-12-29' and pk_expense_type in ('5d03fce4dc80455597f7692adb5a35eb','4de4038862304f429b80546c6e18bbc0');
update ts_expense_type set dr=1 where pk_expense_type in ('5d03fce4dc80455597f7692adb5a35eb','4de4038862304f429b80546c6e18bbc0');
update ts_expense_type set dr=0 where pk_expense_type='a9ad2bc825bd4ceabcc2695a2581250a';

update ts_rece_detail_b set pk_expense_type='0d32c5c178544806b5fccc142409064f' where ts > '2014-12-29' and pk_expense_type in ('3a76977f0beb4f0a885aa7fb06840427','61338580f21549978994dc0b9bca062d');
update ts_expense_type set dr=1 where pk_expense_type in ('3a76977f0beb4f0a885aa7fb06840427','61338580f21549978994dc0b9bca062d');
update ts_expense_type set dr=0 where pk_expense_type='0d32c5c178544806b5fccc142409064f';

update ts_rece_detail_b set pk_expense_type='83e9d3cc953046b19a79460047e8b795' where ts > '2014-12-29' and pk_expense_type in ('83e9d3cc953046b19a79460047e8b795','d02942672ebd4e0e952f05c1a885b3df');
update ts_expense_type set dr=1 where pk_expense_type in ('83e9d3cc953046b19a79460047e8b795','d02942672ebd4e0e952f05c1a885b3df');
update ts_expense_type set dr=0 where pk_expense_type='83e9d3cc953046b19a79460047e8b795';
update ts_rece_detail_b set pk_expense_type='3835f6468b4a4d1cb934cb76c767e0a9' where ts > '2014-12-29' and pk_expense_type in ('ff32ce24568b4952a3a766e5090352a1');
update ts_expense_type set dr=1 where pk_expense_type in ('ff32ce24568b4952a3a766e5090352a1');
update ts_expense_type set dr=0 where pk_expense_type='3835f6468b4a4d1cb934cb76c767e0a9';

update ts_rece_detail_b set pk_expense_type='3a136fd4b40543f5a649d34a31978426' where ts > '2014-12-29' and pk_expense_type in ('02f16394218a4521ad400581e04ac444');
update ts_expense_type set dr=1 where pk_expense_type in ('02f16394218a4521ad400581e04ac444');
update ts_expense_type set dr=0 where pk_expense_type='3a136fd4b40543f5a649d34a31978426';
update ts_rece_detail_b set pk_expense_type='5d80f1b988d34f1bb198edb74e71785a' where ts > '2014-12-29' and pk_expense_type in ('990dc16709d349feabf3884b62d8f820');
update ts_expense_type set dr=1 where pk_expense_type in ('990dc16709d349feabf3884b62d8f820');
update ts_expense_type set dr=0 where pk_expense_type='5d80f1b988d34f1bb198edb74e71785a';

update ts_expense_type set code='FY002' where pk_expense_type='a9ad2bc825bd4ceabcc2695a2581250a';
update ts_expense_type set code='FY003' where pk_expense_type='0d32c5c178544806b5fccc142409064f';
update ts_expense_type set code='FY004' where pk_expense_type='83e9d3cc953046b19a79460047e8b795';
update ts_expense_type set code='FY006' where pk_expense_type='3835f6468b4a4d1cb934cb76c767e0a9';
update ts_expense_type set code='FY007' where pk_expense_type='3a136fd4b40543f5a649d34a31978426';
update ts_expense_type set code='FY010',name='折扣减免' where pk_expense_type='5d80f1b988d34f1bb198edb74e71785a';
update ts_expense_type set code='FY011' where pk_expense_type='5d80f1b988d34f1bb198edb74e71785a';


update ts_invoice set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_pod set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_inv_tracking set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_segment set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_entrust set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_ent_tracking set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_exp_accident set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_contract set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_receive_detail set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_rece_check_sheet set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_pay_detail set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_pay_check_sheet set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_instorage set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_outstorage set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_pick_detail set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_transaction set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_lot_qty set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';

update ts_lot set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';
update ts_storage_ajust set pk_corp='1b2bc00334674853baf78818feadf24e' where pk_corp='0001';