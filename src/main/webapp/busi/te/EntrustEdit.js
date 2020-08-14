//如果系统匹配了2个相同类型的费用，那么需要提示
app.toolbar.on('beforesave',function(toolbar,params){
	var grid = Ext.getCmp('ts_pay_detail_b');
	var store = grid.getStore();
	var pks = [];
	for(var i=0;i<store.getCount();i++){
		var _record = store.getAt(i);
		var system_create = _record.get('system_create');
		if(!system_create || (String(system_create) != 'Y' && String(system_create) != 'true')){
			continue;
		}
		var pk = _record.get('pk_expense_type');
		var code = _record.get('expense_type_code');
		var exist = false;
		for(var j=0;j<pks.length;j++){
			if(pk == pks[j]){
				//pk已经存在
				exist = true;
				break;
			}
		}
		if(!exist){
			pks.push(pk);
		}else{
			var r=confirm("费用明细中存在相同费用类型["+code+"]的记录，您确认要保存吗？");
			if(r==true){
				
			}else{
				return false;
			}
		}
	}
},this);
//使用一个ajax缓存去读取运输方式和换算比率的缓存map
var transTypeFeeMap = {};
//编辑承运商后读取体积重换算比的map,目前使用止血script脚本，所以不能使用这个afterEditHead
//afterEditHead = function(field,value,oriValue){
//	if(field.id == 'pk_carrier'){
//		
//	}else if(field.id == 'pk_trans_type'){
//		updateHeaderFeeWeightCount();
//	}
//};


function afterEditPk_carrier(field,value,oriValue){
	updateHeaderFeeWeightCount();
}



//编辑后事件
var DATETIME_FORMAT = "Y-m-d H:i:s";
//编辑要求离开时间，必须是介于第一行的要求到达时间和第二行的要求到达时间之间
function afterEditReq_leav_date(record,row){
	var req_arri_date = record.get('req_arri_date');
	if(typeof(req_arri_date) == 'string'){
		req_arri_date = Date.parseDate(req_arri_date,DATETIME_FORMAT);
	}
	var req_leav_date = record.get('req_leav_date');
	if(typeof(req_leav_date) == 'string'){
		req_leav_date = Date.parseDate(req_leav_date,DATETIME_FORMAT);
	}
	if(req_leav_date < req_arri_date){
		uft.Utils.showWarnMsg('要求离开时间必须大于等于要求到达时间！');
		record.set('req_leav_date',null);
		return;
	}
	var entLineGrid = Ext.getCmp('ts_ent_line_b');
	var store = entLineGrid.getStore();
	if(row != store.getCount()){
		//不是最后一行
		var nextRecord = store.getAt(row+1);
		if(!nextRecord){
			return;
		}
		next_req_arri_date = nextRecord.get('req_arri_date');
		if(typeof(next_req_arri_date) == 'string'){
			next_req_arri_date = Date.parseDate(next_req_arri_date,DATETIME_FORMAT);
		}
		if(req_leav_date > next_req_arri_date){
			uft.Utils.showWarnMsg('要求离开时间必须小于等于下一行的要求到达时间！');
			record.set('req_leav_date',null);
			return;
		}
	}
}
//编辑要求到达时间，如果存在要求离开时间，那么要求到达时间必须小于要求离开时间
function afterEditReq_arri_date(record,row){
	var req_arri_date = record.get('req_arri_date');
	if(typeof(req_arri_date) == 'string'){
		req_arri_date = Date.parseDate(req_arri_date,DATETIME_FORMAT);
	}
	var req_leav_date = record.get('req_leav_date');
	if(typeof(req_leav_date) == 'string'){
		req_leav_date = Date.parseDate(req_leav_date,DATETIME_FORMAT);
	}
	if(req_leav_date && req_leav_date < req_arri_date){
		uft.Utils.showWarnMsg('要求到达时间必须小于等于要求离开时间！');
		record.set('req_arri_date',null);
		return;
	}
}

//更新表头的总计费重(fee_weight_count)，更新运输方式时以及更新统计信息时会更新总计费重
function updateHeaderFeeWeightCount(){
	var pk_trans_type = uft.Utils.getField('pk_trans_type').getValue();
	if(pk_trans_type && pk_trans_type.length > 0){
		var rate = transTypeFeeMap[pk_trans_type];
		if(rate){
			//缓存中已经存在换算比率
			var volume_count = uft.Utils.getField('volume_count').getValue(); //总体积
			var weight_count = uft.Utils.getField('weight_count').getValue(); //总重量
			var volume_weight_count = volume_count*rate;//总体积重=总体积*体积重换算比率
	    	uft.Utils.getField('volume_weight_count').setValue(volume_weight_count);
			var fee = volume_weight_count; //总体积/体积重换算比率
			if(fee < weight_count){
				fee =  weight_count;
			}
    		uft.Utils.getField('fee_weight_count').setValue(fee);
		}else{
			var pk_carrier = uft.Utils.getField('pk_carrier').getValue();
			var deli_city = uft.Utils.getField('deli_city').getValue();
			var arri_city = uft.Utils.getField('arri_city').getValue();
			uft.Utils.doAjax({
		    	scope : this,
		    	params : {
		    			pk_trans_type:pk_trans_type,
		    			pk_carrier:pk_carrier,
		    			deli_city:deli_city,
		    			arri_city:arri_city
		    		},
		    	isTip : false,
		    	url : ctxPath+'/te/ent/getFeeRate.json',
		    	success : function(values){
		    		if(values.data){
		    			var volume_count = uft.Utils.getField('volume_count').getValue(); //总体积
		    			var weight_count = uft.Utils.getField('weight_count').getValue(); //总重量
		    			var fee = volume_count*values.data; //总体积*体积重换算比率
				    	uft.Utils.getField('volume_weight_count').setValue(fee);
		    			if(fee < weight_count){
		    				fee =  weight_count;
		    			}
			    		uft.Utils.getField('fee_weight_count').setValue(fee);
		    		}
		    	}
		    });
		}
	}
}
//编辑计价方式、报价类型、单价时，更新金额，及表头的总金额
function afterEditQuoteTypeOrValuationTypeOrPrice(record){
	var quote_type = record.get('quote_type'); //报价类型
	var valuation_type = record.get('valuation_type'); //计价方式
	var price_type = record.get('price_type'); //价格类型
	var price = uft.Utils.getNumberColumnValue(record,'price'); //单价
	if(valuation_type==null ||valuation_type==='' || !price){
		return;
	}
	if(String(quote_type)=="0"){//报价类型=区间报价
		if(String(price_type) == "0"){//价格类型=单价
			var amount = 0;
			var i_valuation_type = parseInt(valuation_type);
			switch(i_valuation_type){ //计价方式
				case 0: //重量
					var fee_weight_count = uft.Utils.getNumberFieldValue('fee_weight_count');//总计费重
					amount = fee_weight_count*price;
					break;
				case 1: //体积
					var volume_count = uft.Utils.getNumberFieldValue('volume_count'); //总体积
					amount = volume_count*price;
					break;
				case 2: //件数
					var num_count = uft.Utils.getNumberFieldValue('num_count'); //总件数
					amount = num_count*price;
					break;
				case 3: //设备-这里是和发货单不同的地方，这里只有设备类型(车辆类型)，没有设备数量,不需要计算
					break;
				case 4: //吨公里-这里是和发货单不同的地方，不需要计算
					break;
				case 6: //节点 FIXME 后面会用到
					break;
				case 7: //数量
					var pack_num_count = uft.Utils.getNumberFieldValue('pack_num_count'); //总数量
					break;
					
			}
			uft.Utils.setColumnValue(record,'amount',amount);
		}
	}else{
		uft.Utils.setColumnValue(record,'price',null); //将单价置为空
	}
	//更新表头的总金额
	updateHeaderCostAmount();
}

var payDetailGrid = Ext.getCmp('ts_pay_detail_b'); //费用明细表
 payDetailGrid.on('beforeedit',function(e){
	 var r = e.record,system_create = r.get('system_create');
	 if(String(system_create) == 'true' || String(system_create) == 'Y'){
		 //不能修改系统创建的费用明细（从合同匹配而来的记录）
		 return false;
	 }
 });
//加载费用明细
function refreshPayDetail(){
	var pk_carrier = uft.Utils.getField('pk_carrier').getValue();//承运商
	var pk_trans_type = uft.Utils.getField('pk_trans_type').getValue();//运输方式
	//yaojiie 2016 1 11 增加3个字段
	var urgent_level = uft.Utils.getField('urgent_level').getValue();//紧急程度
	var item_code = uft.Utils.getField('item_code').getValue();//项目编码
	var pk_trans_line = uft.Utils.getField('pk_trans_line').getValue();//线路
	//2015-06-08,使用运力信息的车型
	//var pk_car_type = uft.Utils.getField('pk_car_type').getValue();//车辆类型
	var start_addr,end_addr,start_city,end_city; //起始地址、目的地址、起始城市、目的城市
	var entLineGrid = Ext.getCmp('ts_ent_line_b');
	var store = entLineGrid.getStore();
	var firstRecord = store.getAt(0);
	if(firstRecord){
		start_addr = firstRecord.get('pk_address');
		start_city = firstRecord.get('pk_city');
	}
	var lastRecord = store.getAt(store.getCount()-1);
	if(lastRecord){
		end_addr = lastRecord.get('pk_address');
		end_city = lastRecord.get('pk_city');
	}
	if(pk_carrier && pk_trans_type && start_addr && end_addr){
		var pack_num_count = uft.Utils.getNumberFieldValue('pack_num_count'); //总数量
		var num_count = uft.Utils.getNumberFieldValue('num_count');//总件数
		var weight_count = uft.Utils.getNumberFieldValue('weight_count'); //总重量
		var volume_count = uft.Utils.getNumberFieldValue('volume_count'); //总体积
		var fee_weight_count = uft.Utils.getNumberFieldValue('fee_weight_count'); //总计费重
		var ts_trans_bility_b = Ext.getCmp('ts_trans_bility_b');
		var options={};
		options.params={};
		//主键
		if(app.headerGrid){
			//修改委托单的情况,委托单只能修改，不能新增
			var ids = uft.Utils.getSelectedRecordIds(app.headerGrid,app.headerPkField);
			if(ids && ids.length > 0){
				options.params['pk_entrust'] = ids[0];
			}
		}
		//2015-06-04对于配载页面，传入发货单的参数
		var ts_segment = Ext.getCmp('ts_segment');
		if(ts_segment){
			var _ds = ts_segment.getStore(),_count = _ds.getCount();
			var _pks = [];
			for(var i=0;i<_count;i++){
				var _r = _ds.getAt(i);
				_pks.push(_r.get('invoice_vbillno'));
			}
			options.params['invoiceVbillnoAry']=_pks;
		}
		
		
		var pk_car_type = [];//车辆类型
		var ts_ent_transbility_b = Ext.getCmp('ts_ent_transbility_b');
		if(ts_ent_transbility_b){
			var ds = ts_ent_transbility_b.getStore();
			for(var i=0;i<ds.getCount();i++){
				var r = ds.getAt(i);
				pk_car_type.push(r.get('pk_car_type'));
			}
		}
		//对于配载界面，传入运段号，
		var corp = Ext.getCmp('pk_corp');
		if(corp != null){
			options.params['pk_corp'] = corp.getValue();
		}
		options.params['req_arri_date'] = uft.Utils.getField('req_arri_date').getValue();
		options.params['pack_num_count']=pack_num_count;
		options.params['num_count']=num_count;
		options.params['weight_count']=weight_count;
		options.params['volume_count']=volume_count;
		options.params['fee_weight_count']=fee_weight_count;
		options.params['node_count']=entLineGrid.getStore().getCount();//节点数
		options.params['pk_carrier']=pk_carrier;
		options.params['pk_trans_type']=pk_trans_type;
		options.params['pk_car_type']=pk_car_type;
		options.params['start_addr']=start_addr;
		options.params['end_addr']=end_addr;
		options.params['start_city']=start_city;
		options.params['end_city']=end_city;
		options.params['urgent_level']=urgent_level;
		options.params['item_code']=item_code;
		options.params['pk_trans_line']=pk_trans_line;
		payDetailGrid.getStore().reload(options);
	}
}
//当加载了费用明细的记录后，需要更新表头的总金额
payDetailGrid.getStore().on('load',function(){
	updateHeaderCostAmount();
},this);

/**
 * 检测费用类型是否唯一
 * @param {} record
 */
function afterEditExpenseTypeName(record){
	var store = payDetailGrid.getStore();
	var pk_expense_type = record.get('pk_expense_type');
	var expense_type_code = record.get('expense_type_code');
	if(expense_type_code == transFeeCode){//这个变量在jsp中定义了
		//如果是运费，检查是否唯一
		for(var i=0;i<store.getCount();i++){
			var _record = store.getAt(i);
			if(record.id != _record.id && pk_expense_type == _record.get('pk_expense_type')){
				uft.Utils.showWarnMsg('费用类型为[运费]的记录已经存在！');
				record.set('pk_expense_type',null);
				record.set('expense_type_name',null);
				record.set('expense_type_code',null);
				return;
			}
		}
	}
}

//更新表头的总金额
function updateHeaderCostAmount(){
	var resultMap = uft.Utils.getGridSumValueMap('ts_pay_detail_b','amount');//费用明细的金额
	uft.Utils.getField('cost_amount').setValue(resultMap['amount']);
}

//操作辅助工具栏时的更新内容 
function afterEditBodyAssistToolbar(){
	var grid = app.getActiveBodyGrid();
//	if(grid.id == 'ts_ent_pack_b'){
//		//货品包装明细
//		updateHeaderPackSummary();
//		updateCostDetailAmount();
//		updateHeaderCostAmount();
//	}else 
	if(grid.id == 'ts_ent_line_b'){
		//路线信息
		refreshPayDetail();
	}else if(grid.id == 'ts_pay_detail_b'){
		//费用明细
		updateHeaderCostAmount();
	}
}

/**
 * 配载保存时、委托单保存时校验下要求到达时间和要求离开时间
 * 校验规则：1、上一行的要求离开时间是否小于下一行的要求到达时间
 * 			2、第一条记录肯定是某个运段的始发地
 * 			3、同一个运段的始发地不能在目的地后面
 * @return true表示校验通过，否则校验失败
 */
function checkEntLineB(){
	var entLineGrid = Ext.getCmp('ts_ent_line_b');
	var store = entLineGrid.getStore();
	var count = store.getCount();
	if(count > 1){
		//检测日期
		for(var i=0 ;i<count-1; i++){
			var record = store.getAt(i);
			var nextRecord = store.getAt(i+1);
			var req_leav_date = record.get('req_leav_date'); //要求离开时间
			if(typeof(req_leav_date) == 'string'){
				req_leav_date = Date.parseDate(req_leav_date,DATETIME_FORMAT);
			}
			var req_arri_date = nextRecord.get('req_arri_date'); //下一行的要求到达时间
			if(typeof(req_arri_date) == 'string'){
				req_arri_date = Date.parseDate(req_arri_date,DATETIME_FORMAT);
			}
			if(req_leav_date > req_arri_date){
				if(langague && langague == 'en_US'){
					uft.Utils.showWarnMsg('Line '+i+1+',Required to leave time must be less than or equal to the time required for the next line!');
				}else{
					uft.Utils.showWarnMsg('第'+(i+1)+'行的要求离开时间必须小于等于下一行的要求到达时间！');
				}
				return false;
			}
		}
		//2014-10-03在后台进行检测
//		//检测顺序
//		var firstRecord = store.getAt(0);
//		var addr_flag = firstRecord.get('addr_flag');
//		if(addr_flag != 'S'){
//			uft.Utils.showWarnMsg('第一个节点必须是某个运段的始发地,请调整路线信息的节点顺序!');
//			return false;
//		}
//		var lastRecord = store.getAt(count-1); //最后一行
//		var addr_flag = lastRecord.get('addr_flag');
//		if(addr_flag != 'E'){
//			uft.Utils.showWarnMsg('最后一个节点必须是某个运段的目的地,请调整路线信息的节点顺序!');
//			return false;
//		}
//		var addrMap = {}; //存储运段和始发地和目的地所在行号的map，即{pk_segment:[1,2]}
//		for(var i=0 ;i<count; i++){
//			var record = store.getAt(i);
//			var pk_segment = record.get('pk_segment');//所属运段
//			if(!pk_segment){
//				continue;
//			}
//			if(!addrMap[pk_segment]){
//				addrMap[pk_segment] = [];
//			}
//			var arr = addrMap[pk_segment];
//			var addr_flag = record.get('addr_flag');
//			if(addr_flag == 'S'){ //如果是起始地，则加入数组的第一个元素
//				arr[0] = i;
//			}else if(addr_flag == 'E'){//如果是目的地，则加入数组的第二个元素
//				arr[1] = i;
//			}
//		}
//		var key;
//		for(key in addrMap){
//			var arr = addrMap[key];
//			if(arr[0] > arr[1]){
//				//如果始发地所在的记录大于目的地的记录，那么不正确
//				uft.Utils.showWarnMsg('第'+arr[0]+'条记录[始发地]不能在第'+arr[1]+'条记录[目的地]后面!');
//				return false;
//			}
//		}
	}
	return true;
}

//拖动和释放路线信息的行记录时，需要进行判定 
var entLineGrid = Ext.getCmp('ts_ent_line_b');
var dragDropPlugin;
for(var i=0;i<entLineGrid.plugins.length;i++){
	if(entLineGrid.plugins[i] instanceof Ext.ux.dd.GridDragDropRowOrder){
		dragDropPlugin = entLineGrid.plugins[i];//取得插件
	}
}
if(dragDropPlugin){
	//plugin对象、原始行的行号，新的行号，选择的行[是个数组]
	//同一个运段的始发地不能在目的地后面
	dragDropPlugin.on('beforerowmove',function(plugin,rowIndex,toRowIndex,rows){
		if(app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_ADD && app.statusMgr.getCurrentPageStatus() != uft.jf.pageStatus.OP_EDIT){
			uft.Utils.showWarnMsg('非编辑态不能拖动行！');
			return false;
		}
		//同一个运段，起始地不能在目的地的后面
		//2014-10-03放到后台进行检测
//		if(rows.leng > 1){
//			uft.Utils.showWarnMsg('每次只能拖动一行！');
//			return false;
//		}
//		var pk_segment = rows[0].get('pk_segment');//运段PK
//		var addr_flag = rows[0].get('addr_flag');//标记是运段的起始地或目的地
//		var ds = plugin.grid.getStore();
//		for(var i=0;i<ds.getCount();i++){
//			if(i == rowIndex){//当前拖动的行
//				continue;
//			}
//			var r = ds.getAt(i);
//			var curr_pk_segment = r.get('pk_segment');
//			if(pk_segment == curr_pk_segment){//找到同一个运段的行了
//				//判断行号和待拖动的行所要拖动到的行号进行比较
//				if(addr_flag == 'S'){//拖动行是起始地
//					if(toRowIndex >= i){
//						uft.Utils.showWarnMsg('运段的起始地不能放到目的地的后面！');
//						return false;
//					}
//				}else{//拖动行是目的地
//					if(toRowIndex <= i){
//						uft.Utils.showWarnMsg('运段的目的地不能放到起始地的前面！');
//						return false;
//					}
//				}
//			}
//		}
	},this);
}