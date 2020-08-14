Ext.namespace('uft');

uft.Utils = {
	
	/**
	 * 显示警告提示框
	 * @param {} msg
	 */
	showWarnMsg : function(msg,title){
		uft.extend.tip.Tip.msg(uft.Internal.getText(title) || 'warn',uft.Internal.getText(msg));
	},
	/**
	 * 显示信息提示框
	 * @param {} msg
	 */
	showInfoMsg : function(msg,title){
		uft.extend.tip.Tip.msg(uft.Internal.getText(title) || 'info',uft.Internal.getText(msg));
	},
	/**
	 * 显示错误提示框
	 * @param {} msg
	 */
	showErrorMsg : function(msg,title){
		uft.extend.tip.Tip.msg(uft.Internal.getText(title) || 'error',uft.Internal.getText(msg));
	},
	/**
	 * 显示操作提示框 
	 * 该方法在IE下的性能非常差，建议不使用
	 * @param {} msg
	 */
	showProcessMsg : function(msg,title){
		Ext.Msg.show({
			title:uft.Internal.getText(title)||uft.Internal.getText('请稍等'), 
			msg: uft.Internal.getText(msg)||uft.Internal.getText('加载中，请稍候...'),
			wait:true
		});
	},
	/**
	 * 拷贝json中的元素到newJson
	 * @param {} json
	 * @param {} newJson
	 * @return {}
	 */
	cloneJsonObject : function(json,newJson){
		if(json==undefined||json==null){
			this.showErrorMsg("待拷贝对象不能为空！");
			return;
		}
		if(newJson==undefined||newJson==null){
			newJson={};
		}
		for(key in json){
			newJson[key]=json[key];
		}
	},
	assert : function(bCondition, sErrorMsg) {
		if (bCondition==null) {
			this.showErrorMsg(sErrorMsg);
			throw new Error(sErrorMsg);
		}
	},
	
	/**
	 * 返回选中的行对象
	 * 当多选时，返回第一个行对象
	 * @param {} grid
	 * @return {Boolean}
	 */
	getSelectedRecord : function(grid,showMsg) {
		//grid参数可以为空，此时返回null
//		this.assert(grid, 'grid参数不能为空！');
		try{
			if(!grid){
				return null;
			}
			var record = grid.getSelectionModel().getSelected();
			if(record){
				return record;
			}
			if(showMsg){
				this.showWarnMsg('请选择要操作的数据！');
			}
		}catch(e){
			
		}
		return null;
	},
	/**
	 * 返回选中的行对象
	 * 支持多选，以数组形式返回
	 * @param {} grid
	 * @param {} showMsg
	 * @return {}
	 */
	getSelectedRecords : function(grid,showMsg) {
		//grid参数可以为空，此时返回null
//		this.assert(grid, 'grid参数不能为空！');
		try{
			if(!grid){
				return null;
			}
			//有些可能不是RowSelectionModel对象，如CellSelectionModel,这时候就没有getSelections方法
			var records = grid.getSelectionModel().getSelections();
			if(records&&records.length>0){
				return records;
			}
			if(showMsg){
				this.showWarnMsg('请选择要操作的数据！');
			}
		}catch(e){}
		return null;
	},
	getSelectedRecordId : function(grid,pkField){
		var record = uft.Utils.getSelectedRecord(grid);
		if(!record){
			return null;
		}
		return record.data[pkField];
	},	
	/**
	 * 返回所选记录的主键集合
	 */
	getSelectedRecordIds : function(grid,pkField){
		var records = uft.Utils.getSelectedRecords(grid);
		if(!records){
			return null;
		}
		var ids = [],len = records.length;
		for(var i=0;i<len;i++){
			ids.push(records[i].data[pkField]);
		}
		return ids;
	},
	/**
	 * 发送AJAX请求，这里封装了ext的发送和处理，业务系统建议用此方法来做AJAX调用，而不要直接用EXT等
	 * @param {} config
	 */
	doAjax : function(config) {
		var URL = config.url;
		if(config.mask !== false){
	    	if(!config.el){
	    		config.el = Ext.getBody();
	    	}
	    	config.el.mask(uft.Internal.getText('加载中，请稍候...'));
		}
		
		var params = config.params || {};
		config.callback = config.success || {};
		config.failureCallback = config.failure || {};
		
		Ext.Ajax.request({
			scope : config.scope || this,
			url : URL,
			params : params,
			method : config.method||'POST',
			timeout : Constants.timeOut,
			success : function(response){
				if(config.mask !== false){
					config.el.unmask();
				}
				
				var responseText = response.responseText;
				var jsonData = null;
				try{
					jsonData = Ext.decode(responseText);
				}catch(e){
					return responseText;
				}
				
				try{
					//后台设置success的值推荐使用boolean，这是为了兼容表单submit的情况，判断表单是否成功提交就是使用success的true/false
					//使用submit需要设置response.setContentType("text/html");
					if(jsonData.success==true || jsonData.success==false || jsonData.success=="true" || jsonData.success=="false"){
						if(jsonData.success.toString()=="true") {
							if(config.isTip==undefined || config.isTip==true){
								if(jsonData.msg && jsonData.msg != "操作成功！"){
									uft.extend.tip.Tip.msg('warn',jsonData.msg);
								}else{
									uft.extend.tip.Tip.msg(config.msgTitle || '结果', config.msgText || '恭喜您，'+(config.actionType||'操作')+'成功！');
								}
							}
							if(typeof(config.callback) == "function") {
								config.callback.call(this, jsonData);
							}
						}else {
							var msg = jsonData.msg || jsonData.exception;
							msg = msg==null?"服务器处理错误！":msg.replaceAll("\n","<br/>",true);
							if(jsonData.type && String(jsonData.type) == '1'){//属于业务的错误，使用警告提示
								uft.Utils.showWarnMsg(msg);
							}else if(jsonData.type && String(jsonData.type) == '2'){
								Ext.Msg.show({
									title:'警告', 
									msg: msg,
									icon:Ext.Msg.WARNING,
									buttons:Ext.Msg.OK
								});
							}else{
								uft.Utils.showErrorMsg(msg);
							}
						}
					}else {
						if(typeof(config.callback) == "function") {
							config.callback.call(this, jsonData);
						}
					}
				}catch(e){
					var msg="调用成功，但处理出现异常，此情况属于程序BUG，请及时反馈开发人员进行处理!";
					msg+="<br>"+e.name+":"+e.message;
					msg+="<br>发生错误的文件位于："+e.fileName+"(第"+e.lineNumber+"行)";
					msg+="<br>堆栈信息："+e.stack;
					Ext.Msg.show({
						title:'错误',
						msg: msg,
						buttons : Ext.Msg.OK,
						icon : Ext.Msg.ERROR
					});
				}
			},
			failure : function(response) {
				if(config.mask !== false){
					config.el.unmask();
				}
				
				var json=null;
				try{
					json=eval("("+response.responseText+")");
				}catch(e){
					json={};
				}
				
				/**
				 * 执行自定义的callback
				 */
				if(typeof(config.failureCallback) == "function") {
					config.failureCallback.call(this, json);
				}
				
				var msg;
				if(json && json.msg){
					msg = json.msg;
				}else{
					msg = "请求超时,网络异常!";
				}
				uft.Utils.showErrorMsg(msg);
			}
		});
	},
	/**
	 * 返回两个日期的时间差，以天数返回
	 * @param {} beginDate 
	 * @param {} endDate 
	 * @return {}
	 */
	getSubDays : function(beginDate,endDate){
		if(!beginDate || !endDate){
			return null;
		}
		if(!(beginDate instanceof Date)){
			beginDate = Date.parseDate(beginDate,'Y-m-d'); 
		}
		if(!(endDate instanceof Date)){
			endDate = Date.parseDate(endDate,'Y-m-d'); 
		}
		return Math.floor((endDate-beginDate)/(1000 * 60 * 60 * 24));
	},
	/**
	 * 返回当前行记录中的columnName列的值
	 * @param {} record 	当前编辑的行
	 * @param {} columnName 列名
	 * @return
	 */
	getColumnValue : function(record,columnName){ 
		return record.get(columnName);
	},
	/**
	 * 返回当前行记录中的columnName列的值,该列必须是日期项
	 * 日期列在可编辑表格中会render为字符型，故提供此方法
	 * @param {} record
	 * @param {} columnName
	 */
	getDateColumnValue : function(record,columnName){
		var value = record.get(columnName);
		if(value){
			if(value instanceof Date){
				//这么处理是为了只取日期，不计算时间
				return value.clearTime();
			}
			//这里value是2011-10-02格式的
			var date = Date.parseDate(value,'Y-m-d'); //使用这个格式是因为DateField的默认格式使用这个
			return date.clearTime();
		}
		return null;
	},
	/**
	 * 返回当前行记录中的columnName列的值,该列必须是checkbox逻辑类型
	 * 返回true or false
	 * @param {} record
	 * @param {} columnName
	 */
	getBooleanColumnValue : function(record,columnName){
		var value = record.get(columnName);
		if(value){
			if(value == 'Y' || value == 'true'){
				return true;
			}
			return false;
		}
		return false;
	},
	/**
	 * 返回当前行记录中的columnName列的值,没有值则返回0
	 * @deprecated 此方法命名不规范，以后不再使用
	 * @param {} record 	当前编辑的行
	 * @param {} columnName 列名
	 * @return
	 */
	getColumnValueForNumber : function(record,columnName){
		return uft.Utils.getNumberColumnValue(record,columnName);
	},
	/**
	 * 返回当前行记录中的columnName列的值,没有值则返回0
	 * @param {} record		当前编辑的行
	 * @param {} columnName 列名
	 * @return {}
	 */
	getNumberColumnValue : function(record,columnName){
		var value=record.get(columnName);
		if(Utils.isBlank(value)){
			return 0;
		}else{
			try{
				value=parseFloat(value);
			}catch(e){
				value=0;
			}
		}
		return value;
	},	
	/**
	 * 将当前记录的columnName列的值设置为value
	 * @param {} record	当前编辑的行
	 * @param {} columnName 列名
	 * @param {} value	待设置的值
	 */
	setColumnValue : function(record,columnName,value){
		record.set(columnName,value);
	},
	/**
	 * 设置列可编辑
	 * @param {} grid 表格对象
	 * @param {} columnName 列名称
	 * @param {} editflag 设置为是否可编辑，true/false
	 * @param {} allowBlank 设置成可编辑的情况下，是否允许为空，及是否not null类型
	 */
	setColumnEditable : function(grid,columnName,editflag,allowBlank){
		if(!grid || !columnName){
			return;
		}
		var view = grid.getView();
		var column = uft.Utils.getColumn(grid,columnName);
		column.editable = editflag;
		var index = grid.getColumnModel().findColumnIndex(columnName);
		var header = view.getHeaderCell(index);
		var span = header.firstChild.firstChild;
		if(editflag){
			span.className = '';
			if(!allowBlank){
				//设置header增加不能为空的样式uft-grid-header-column-not-null
				span.className = 'uft-grid-header-column-not-null';
			}
		}else{
			//设置header增加不可编辑的样式uft-grid-header-column-not-edit
			span.className = 'uft-grid-header-column-not-edit';
		}
	},	
	/**
	 * 返回表格中指定列的最小值
	 */
	getMinColumnValue : function(gridId,columnName){
		var grid = Ext.getCmp(gridId);
		if(!grid){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('Does not exist in the form of'+gridId+'for id!');
			}else{
				uft.Utils.showErrorMsg('不存在id为'+gridId+'的表格！');
			}
			return null;
		}
		var min=null;
		var count=grid.getStore().getCount();
		for(var i=0;i<count;i++){
			var record=grid.getStore().getAt(i);
			var value=uft.Utils.getColumnValue(record,columnName);
			if(min==null||min>value){
				min=value;
			}
		}
		return min;
	},
	/**
	 * 返回表格中指定列的最小值,该列的数据类型是日期
	 * @param {} gridId
	 * @param {} columnName
	 * @return {}
	 */
	getMinDateColumnValue : function(gridId,columnName){
		var grid = Ext.getCmp(gridId);
		if(!grid){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('Does not exist in the form of'+gridId+'for id!');
			}else{
				uft.Utils.showErrorMsg('不存在id为'+gridId+'的表格！');
			}
			return null;
		}
		var min=null;
		var count=grid.getStore().getCount();
		for(var i=0;i<count;i++){
			var record=grid.getStore().getAt(i);
			var value=uft.Utils.getColumnValue(record,columnName);
			if(value instanceof Date){
				value = value.clearTime();
			}else{
				var date = Date.parseDate(value,'Y-m-d'); //使用这个格式是因为DateField的默认格式使用这个
				value = date.clearTime();
			}
			if(min==null||min>value){
				min=value;
			}
		}
		return min;
	},	
	/**
	 * 返回表格中指定列的最大值
	 */
	getMaxColumnValue : function(gridId,columnName){
		var grid = Ext.getCmp(gridId);
		if(!grid){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('Does not exist in the form of'+gridId+'for id!');
			}else{
				uft.Utils.showErrorMsg('不存在id为'+gridId+'的表格！');
			}
			return null;
		}
		var max=null;
		var count=grid.getStore().getCount();
		for(var i=0;i<count;i++){
			var record=grid.getStore().getAt(i);
			var value=uft.Utils.getColumnValue(record,columnName);
			if(max==null||max<value){
				max=value;
			}
		}
		return max;
	},
	/**
	 * 返回表格中指定列的最大值，该列的数据类型是日期
	 * @param {} gridId
	 * @param {} columnName
	 * @return {}
	 */
	getMaxDateColumnValue : function(gridId,columnName){
		var grid = Ext.getCmp(gridId);
		if(!grid){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg('Does not exist in the form of'+gridId+'for id!');
			}else{
				uft.Utils.showErrorMsg('不存在id为'+gridId+'的表格！');
			}
			return null;
		}
		var max=null;
		var count=grid.getStore().getCount();
		for(var i=0;i<count;i++){
			var record=grid.getStore().getAt(i);
			var value=uft.Utils.getColumnValue(record,columnName);
			if(value instanceof Date){
				value = value.clearTime();
			}else{
				var date = Date.parseDate(value,'Y-m-d'); //使用这个格式是因为DateField的默认格式使用这个
				value = date.clearTime();
			}			
			if(max==null||max<value){
				max=value;
			}
		}
		return max;
	},
	/**
	 * 返回当前行的指定列的值的和,
	 * 
	 * @param {} record
	 * @param {} columnNames 可以为单个列，也可以为数组
	 * @return {}
	 */
	getRecordSumValue : function(record,columnNames){
		if(!columnNames){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg(columnNames+' Parameters cannot be empty!');
			}else{
				uft.Utils.showErrorMsg(columnNames+'参数不能为空！');
			}
			return 0;
		}else{
			if(columnNames instanceof Array){
				if(columnNames.length==0){
					if(langague && langague == 'en_US'){
						uft.Utils.showErrorMsg(columnNames+' Parameters cannot be empty!');
					}else{
						uft.Utils.showErrorMsg(columnNames+'参数不能为空！');
					}
					return null;
				}
				var sum=0;
				for(var i=0;i<columnNames.length;i++){
					var value=uft.Utils.getNumberColumnValue(record,columnNames[i]);
					sum+=value;
				}
				return sum;
			}else{
				return uft.Utils.getNumberColumnValue(record,columnNames);
			}
		}
	},
	/**
	 * 返回指定表格的指定行的值的总和，
	 * 若单个列则返回单个列的和，多个列则先统计单个列，再统计所有列总和。
	 * @param {} gridId
	 * @param {} columnNames 可以为单个列，也可以为数组
	 * @return {}
	 */
	getGridSumValue : function(gridId,columnNames){
		if(!columnNames){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg(columnNames+' Parameters cannot be empty!');
			}else{
				uft.Utils.showErrorMsg(columnNames+'参数不能为空！');
			}
			return 0;
		}else{
			var grid = Ext.getCmp(gridId);
			if(!grid){
				if(langague && langague == 'en_US'){
					uft.Utils.showErrorMsg('Does not exist in the form of'+gridId+'for id!');
				}else{
					uft.Utils.showErrorMsg('不存在id为'+gridId+'的表格！');
				}
				return 0;
			}			
			if(columnNames instanceof Array){
				if(columnNames.length==0){
					if(langague && langague == 'en_US'){
						uft.Utils.showErrorMsg(columnNames+' Parameters cannot be empty!');
					}else{
						uft.Utils.showErrorMsg(columnNames+'参数不能为空！');
					}
					return 0;
				}
				
				//先从合计行中读取
				var sum = uft.Utils._getGridSummarySumValue(grid,columnNames);
				if(sum){
					return sum;
				}
				
				sum=0;
				var count=grid.getStore().getCount();
				for(var i=0;i<count;i++){
					var record=grid.getStore().getAt(i);
					sum+=uft.Utils.getRecordSumValue(record,columnNames);
				}
				return sum;
			}else{
				//先从合计行中读取
				var sum = uft.Utils._getGridSummarySumValue(grid,columnNames);
				if(sum){
					return sum;
				}
				
				sum=0;
				var count=grid.getStore().getCount();
				for(var i=0;i<count;i++){
					var record=grid.getStore().getAt(i);
					sum+=uft.Utils.getNumberColumnValue(record,columnNames);
				}
				return sum;
			}
		}
	},
	/**
	 * 返回行的统计值，每列对应一个统计值,返回对象
	 * @param {} gridId
	 * @param {} columnNames
	 */
	getGridSumValueMap : function(gridId,columnNames){
		var resultMap = {};
		if(!columnNames){
			if(langague && langague == 'en_US'){
				uft.Utils.showErrorMsg(columnNames+' Parameters cannot be empty!');
			}else{
				uft.Utils.showErrorMsg(columnNames+'参数不能为空！');
			}
			return resultMap;
		}else{
			var grid = Ext.getCmp(gridId);
			if(!grid){
				if(langague && langague == 'en_US'){
					uft.Utils.showErrorMsg('Does not exist in the form of'+gridId+'for id!');
				}else{
					uft.Utils.showErrorMsg('不存在id为'+gridId+'的表格！');
				}
				return resultMap;
			}			
			if(columnNames instanceof Array){
				if(columnNames.length==0){
					if(langague && langague == 'en_US'){
						uft.Utils.showErrorMsg(columnNames+' Parameters cannot be empty!');
					}else{
						uft.Utils.showErrorMsg(columnNames+'参数不能为空！');
					}
					return resultMap;
				}
				
				//先从合计行中读取
				var hasGridSummaryPlugin = false;
				for(var i=0;i<columnNames.length;i++){
					var sum = uft.Utils._getGridSummarySumValue(grid,columnNames[i]);
					if(sum){
						hasGridSummaryPlugin = true;
						resultMap[columnNames[i]] = sum;
					}else{
						break; //没有合计行，直接跳出，下面会根据表格数据重新计算
					}
				}
				if(hasGridSummaryPlugin){//如果存在合计行，则返回合计行的统计数据
					return resultMap;
				}
				
				var count=grid.getStore().getCount();
				for(var i=0;i<columnNames.length;i++){
					var sum = 0;
					for(var j=0;j<count;j++){
						var record=grid.getStore().getAt(j);
						sum+=uft.Utils.getNumberColumnValue(record,columnNames[i]);
					}	
					resultMap[columnNames[i]] = sum;
				}
				return resultMap;
			}else{
				//先从合计行中读取
				var sum = uft.Utils._getGridSummarySumValue(grid,columnNames);
				if(sum){
					resultMap[columnNames] = sum;
					return resultMap;
				}
				
				sum=0;
				var count=grid.getStore().getCount();
				for(var i=0;i<count;i++){
					var record=grid.getStore().getAt(i);
					sum+=uft.Utils.getNumberColumnValue(record,columnNames);
				}
				resultMap[columnNames] = sum;
				return resultMap;
			}
		}
	},
	/**
	 * 统计表格的合计行的多列的和
	 * @param {} grid
	 * @param {} columnNames
	 * @return {}
	 * @private
	 */
	_getGridSummarySumValue : function(grid,columnNames){
		if(!grid){
			return null;
		}
		if(!columnNames){
			return null;
		}
		//这里从合计行中读取数据，而不是重新统计表格的所有行
		//这里判断是否存在插件，插件数，并且判断是否引入了GridSummary
		if(grid.plugins && grid.plugins.length > 0 && Ext.ux.grid.GridSummary){
			for(var i=0;i<grid.plugins.length;i++){
				if(grid.plugins[i] instanceof Ext.ux.grid.GridSummary){
					var gridSummary = grid.plugins[i];
					if(columnNames instanceof Array){
						var sum = 0;
						for(var j=0;j<columnNames.length;j++){
							sum+= gridSummary.recordData[columnNames[j]];
						}
						return sum;
					}else{
						return gridSummary.recordData[columnNames] + 0; //确保返回数据
					}
				}
			}
		}
		return null;
	},
	/**
	 * 根据列名找到列对象
	 * @param {} grid
	 * @param {} columnName
	 * @return {}
	 */
	getColumn : function(grid,columnName){
		var columns = grid.colModel.config;
		var length = columns.length;
		for(var i=0;i<length;i++){
			if(columns[i].dataIndex == columnName){
				return columns[i];
			}
		}
		return null;
	},
	/**
	 * 根据列名找到列的编辑对象
	 * @param {} grid
	 * @param {} columnName
	 * @return {}
	 */
	getColumnEditor : function(grid,columnName){
		var column = uft.Utils.getColumn(grid,columnName);
		if(column&&column.editor){
			return column.editor;
		}
		return null;
	},
	/**
	 * 根据id返回组件，当组件不存在时，返回自定义的NullField
	 * @param {} id
	 * @return {}
	 */
	getField : function(id){
		if(typeof id == 'string'){
			var cmp = Ext.getCmp(id);
			if(!cmp){
				return new uft.extend.form.NullField();
			}
			return cmp;
		}else{
			//直接就是对象了
			return id;
		}
	},
	/**
	 * 返回指定表单域的值
	 * @param {} fieldId
	 * @return {}
	 */
	getFieldValue : function(fieldId){
		var field = uft.Utils.getField(fieldId);
		return field.getValue();
	},
	/**
	 * 返回指定表单域的值,数值型，没值则返回0
	 * @deprecated 此方法命名不规范，以后不再使用
	 * @param {} fieldId
	 * @return {}
	 */	
	getFieldValueForNumber : function(fieldId){
		return uft.Utils.getNumberFieldValue(fieldId);
	},
	/**
	 * 返回指定表单域的值,数值型，没值则返回0
	 * @param {} fieldId
	 * @return {}
	 */
	getNumberFieldValue : function(fieldId){
		var value=uft.Utils.getFieldValue(fieldId);
		if(Utils.isBlank(value)){
			return 0;
		}else{
			try{
				value=parseFloat(value);
			}catch(e){
				value=0;
			}
		}
		return value;		
	},
	/**
	 * 返回指定表单域的值,日期型
	 * @param {} fieldId
	 * @return {}
	 */
	getDateFieldValue : function(fieldId){
		var value=uft.Utils.getFieldValue(fieldId);
		if(Utils.isBlank(value)){
			return null;
		}
		if(value instanceof Date){
			return value.clearTime();
		}
		var date = Date.parseDate(value,'Y-m-d'); //使用这个格式是因为DateField的默认格式使用这个
		return date.clearTime();
	},
	/**
	 * 将指定表单域的值设置为value
	 * @param {} fieldId
	 * @param {} value
	 */
	setFieldValue : function(fieldId,value){
		var field = uft.Utils.getField(fieldId);
		field.setValue(value);
	},
	/**
	 * 识别Ext的表达式，如Ext.getCmp(id).getValue();
	 * 实际上是使用eval执行这些ext表达式，再返回。
	 * <li>表头：${Ext.getCmp(id).getValue()}</li>
	 * <li>表体：${record.get(id)}</li>
	 * @param {} expression
	 * @param {} record 
	 * 				执行表达式的上下文环境，比如表体，通常是一个record,这里定义成record，因为最终表达式使用的就是record
	 */
	resolveExpression : function(expression,record){
		if(!expression){
			return null;
		}
		var startExp="${",endExp="}";
		var start=expression.indexOf(startExp);
		if(start==-1){
			return expression;
		}
		var sb=expression.substring(0,start);
		var remain=expression.substring(start);
		while(start>-1){
			var end=remain.indexOf(endExp);
			var ext=remain.substring(0,end+1); //1+${Ext.getCmp(id).getValue()}+c+${Ext.getCmp(id).getValue()}+a
			var afterResolve=eval(ext.substring(2,ext.length-1));
			sb+=afterResolve;
			remain=remain.substring(end+1);
			start=remain.indexOf(startExp);
			if(start>0){
				sb+=remain.substring(0,start);
				remain=remain.substring(start);
			}
		}
		return sb+remain;
	},
	/**
	* 将金额转换为中文大写
	* arg1:金额，arg2:精度
	*/
	chgMoneyToCNUpperCase : function(dValue, maxDec) {
		  // 验证输入金额数值或数值字符串：
		  dValue = dValue.toString().replace(/,/g, ""); 
		  dValue = dValue.replace(/^0+/, ""); // 金额数值转字符、移除逗号、移除前导零
		  if (dValue == "") { return "零元整"; } // （错误：金额为空！）
		  else if (isNaN(dValue)) { return "错误：金额不是合法的数值！"; }  
		    
		  var minus = ""; // 负数的符号“-”的大写：“负”字。可自定义字符，如“（负）”。
		  var CN_SYMBOL = ""; // 币种名称（如“人民币”，默认空）
		  if (dValue.length > 1)
		  {
		  if (dValue.indexOf('-') == 0) { dValue = dValue.replace("-", ""); minus = "负"; } // 处理负数符号“-”
		  if (dValue.indexOf('+') == 0) { dValue = dValue.replace("+", ""); } // 处理前导正数符号“+”（无实际意义）
		  }
		    
		  // 变量定义：
		  var vInt = ""; var vDec = ""; // 字符串：金额的整数部分、小数部分
		  var resAIW; // 字符串：要输出的结果
		  var parts; // 数组（整数部分.小数部分），length=1时则仅为整数。
		  var digits, radices, bigRadices, decimals; // 数组：数字（0~9——零~玖）；基（十进制记数系统中每个数字位的基是10——拾,佰,仟）；大基（万,亿,兆,京,垓,杼,穰,沟,涧,正）；辅币（元以下，角/分/厘/毫/丝）。
		  var zeroCount; // 零计数
		  var i, p, d; // 循环因子；前一位数字；当前位数字。
		  var quotient, modulus; // 整数部分计算用：商数、模数。

		  // 金额数值转换为字符，分割整数部分和小数部分：整数、小数分开来搞（小数部分有可能四舍五入后对整数部分有进位）。
		  var NoneDecLen = (typeof(maxDec) == "undefined" || maxDec == null || Number(maxDec) < 0 || Number(maxDec) > 5); // 是否未指定有效小数位（true/false）
		  parts = dValue.split('.'); // 数组赋值：（整数部分.小数部分），Array的length=1则仅为整数。
		  if (parts.length > 1)  
		  {
		  vInt = parts[0]; vDec = parts[1]; // 变量赋值：金额的整数部分、小数部分
		    
		  if(NoneDecLen) { maxDec = vDec.length > 5 ? 5 : vDec.length; } // 未指定有效小数位参数值时，自动取实际小数位长但不超5。
		  var rDec = Number("0." + vDec);   
		  rDec *= Math.pow(10, maxDec); rDec = Math.round(Math.abs(rDec)); rDec /= Math.pow(10, maxDec); // 小数四舍五入
		  var aIntDec = rDec.toString().split('.');
		  if(Number(aIntDec[0]) == 1) { vInt = (Number(vInt) + 1).toString(); } // 小数部分四舍五入后有可能向整数部分的个位进位（值1）
		  if(aIntDec.length > 1) { vDec = aIntDec[1]; } else { vDec = ""; }
		  }
		  else { vInt = dValue; vDec = ""; if(NoneDecLen) { maxDec = 0; } }  
		  if(vInt.length > 44) { return "错误：金额值太大了！整数位长【" + vInt.length.toString() + "】超过了上限——44位/千正/10^43（注：1正=1万涧=1亿亿亿亿亿，10^40）！"; }
		    
		  // 准备各字符数组 Prepare the characters corresponding to the digits:
		  digits = new Array("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"); // 零~玖
		  radices = new Array("", "拾", "佰", "仟"); // 拾,佰,仟
		  bigRadices = new Array("", "万", "亿", "兆", "京", "垓", "杼", "穰" ,"沟", "涧", "正"); // 万,亿,兆,京,垓,杼,穰,沟,涧,正
		  decimals = new Array("角", "分", "厘", "毫", "丝"); // 角/分/厘/毫/丝
		    
		  resAIW = ""; // 开始处理
		    
		  // 处理整数部分（如果有）
		  if (Number(vInt) > 0)  
		  {
		  zeroCount = 0;
		  for (i = 0; i < vInt.length; i++)  
		  {
		  p = vInt.length - i - 1; d = vInt.substr(i, 1); quotient = p / 4; modulus = p % 4;
		  if (d == "0") { zeroCount++; }
		  else  
		  {
		  if (zeroCount > 0) { resAIW += digits[0]; }
		  zeroCount = 0; resAIW += digits[Number(d)] + radices[modulus];
		  }
		  if (modulus == 0 && zeroCount < 4) { resAIW += bigRadices[quotient]; }
		  }
		  resAIW += "元";
		  }
		    
		  // 处理小数部分（如果有）
		  for (i = 0; i < vDec.length; i++) { d = vDec.substr(i, 1); if (d != "0") { resAIW += digits[Number(d)] + decimals[i]; } }
		    
		  // 处理结果
		  if (resAIW == "") { resAIW = "零" + "元"; } // 零元
		  if (vDec == "") { resAIW += "整"; } // ...元整
		  resAIW = CN_SYMBOL + minus + resAIW; // 人民币/负......元角分/整
		  return resAIW;
	},
	floatTip : function(column){
		if(column){
			column.renderer = function (data, metadata, record, rowIndex, columnIndex, store) {
				if(data!=undefined){
					metadata.attr = 'title="' + data + '"';
				}
				return data;
			};
		}
	},
	/**
	 * Clone Function
	 * @param {Object/Array} o Object or array to clone
	 * @return {Object/Array} Deep clone of an object or an array
	 * @author Ing. Jozef Sakáloš
	 */
	clone : function(o) {
	    if(!o || 'object' !== typeof o) {
	        return o;
	    }
	    if('function' === typeof o.clone) {
	        return o.clone();
	    }
	    var c = '[object Array]' === Object.prototype.toString.call(o) ? [] : {};
	    var p, v;
	    for(p in o) {
	        if(o.hasOwnProperty(p)) {
	            v = o[p];
	            if(v && 'object' === typeof v) {
	                c[p] = uft.Utils.clone(v);
	            }
	            else {
	                c[p] = v;
	            }
	        }
	    }
	    return c;
	},
	/**
	 * 打开节点，如果是在框架里面，则调用父窗口的openNode方法，否则调用window.open
	 * @param {} nodeId
	 * @param {} title
	 * @param {} url
	 */
	openNode : function(nodeId,title,url){
		if(typeof(parent.openNode) == 'function'){
			parent.openNode(nodeId,title,url,true,true);
		}else{
			window.open(url);
		}
	},
	//从剪贴板中读取数据，不兼容chrome
	getClipboardData : function() {
		if(window.clipboardData){
			return window.clipboardData.getData('Text');
		}else if(window.netscape){
		    netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');  
		    var clip = Components.classes['@mozilla.org/widget/clipboard;1'].createInstance(Components.interfaces.nsIClipboard);
		    if(!clip)
		    	return;  
		    var trans = Components.classes['@mozilla.org/widget/transferable;1'].createInstance(Components.interfaces.nsITransferable);
		    if(!trans)
		    	return;  
		    trans.addDataFlavor('text/unicode');  
		    clip.getData(trans, clip.kGlobalClipboard);  
		    var str = new Object();  
		    var len = new Object();  
		    try {
		        trans.getTransferData('text/unicode', str, len);
		    } catch(error) {
		        return null;
		    }
		    if(str) {  
		        if(Components.interfaces.nsISupportsWString)
		        	strstr = str.value.QueryInterface(Components.interfaces.nsISupportsWString);
		        else if(Components.interfaces.nsISupportsString) 
		        	strstr = str.value.QueryInterface(Components.interfaces.nsISupportsString);  
		        else 
		        	str = null;  
		    }  
		    if(str){  
		        //alert(str.data.substring(0, len.value / 2));
		        return (str.data.substring(0, len.value / 2));
		    }
	    }
	    return null;
	},
	/**
	 * 对数据进行处理,将换行符缓存逗号，并过滤前后的逗号
	 *
	 * @param {} data
	 * @return {}
	 */
	escapeChar : function(data){
		if(data){
			data = data.replaceAll('\r\n',',');
			var arr = data.split(','),str = '';
			for(var i=0;i<arr.length;i++){
				if(arr[i]){
					str += arr[i];
					if(i != arr.length-1){
						str += ',';
					}
				}
			}
			if(str.endWith(',')){
				str = str.substring(0,str.length-1);
			}
			return str.trim();
		}
		return data;
	}	
};

(function(){
	ua = navigator.userAgent.toLowerCase(),
	check = function(r){
	    return r.test(ua);
	},
	DOC = document,
	docMode = DOC.documentMode,
	isStrict = DOC.compatMode == "CSS1Compat",
	isOpera = check(/opera/),
	isChrome = check(/\bchrome\b/),
	isWebKit = check(/webkit/),
	isSafari = !isChrome && check(/safari/),
	isSafari2 = isSafari && check(/applewebkit\/4/), // unique to Safari 2
	isSafari3 = isSafari && check(/version\/3/),
	isSafari4 = isSafari && check(/version\/4/),
	isIE = !isOpera && check(/msie/),
	isIE7 = isIE && (check(/msie 7/) || docMode == 7),
	isIE8 = isIE && ((check(/msie 8/) && docMode != 7)||(check(/msie 9/) && docMode == 8)),
	isIE9 = isIE && (check(/msie 9/) && docMode == 9),
	isIE6 = isIE && !isIE7 && !isIE8 && !isIE9,
	isGecko = !isWebKit && check(/gecko/),
	isGecko2 = isGecko && check(/rv:1\.8/),
	isGecko3 = isGecko && check(/rv:1\.9/),
	isBorderBox = isIE && !isStrict,
	isWindows = check(/windows|win32/),
	isMac = check(/macintosh|mac os x/),
	isAir = check(/adobeair/),
	isLinux = check(/linux/),
	isSecure = /^https/i.test(window.location.protocol);
	
	uft.Utils.isIE =isIE;
	uft.Utils.isIE6=isIE6;
	uft.Utils.isIE7=isIE7;
	uft.Utils.isIE8=isIE8;
	uft.Utils.isIE9=isIE9;
	uft.Utils.isSecure=isSecure;
})();
