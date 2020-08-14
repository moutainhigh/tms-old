Ext.ns('uft.te');

var retMap = {};
uft.te.Authentication = function(config){
	Ext.apply(this,config);
	if(!this.record){
		uft.Utils.showErrorMsg('请选择记录！');
		return false;
	}
	var retMap = this.retMap;
	if(!retMap){
		uft.Utils.showErrorMsg('没有获取到身份证信息！');
		return false;
	}

	this.formPanel = new uft.extend.form.FormPanel({
		id : "certificateForm",
		labelWidth : 60,
		border : false,
		items : [{
				layout : 'tableform',
				layoutConfig: {columns:3},
				border : false,
				padding : '5px 5px 0',
				defaults:{
					anchor: '95%',
					xtype : 'textfield',
					readOnly : true
				},      
				items : [{
					name : 'name',
					fieldLabel : '姓名',
					value : retMap.partyName,
					newlineflag : true,
					colspan : 2
				},{
					name : 'photo',
					fieldLabel : '照片',
					value : retMap.photoUrl, 
					colspan : 1,
					xtype : 'identityField',
					readOnly : true
				},{
					name : 'gender',
					fieldLabel : '性别',
					value : retMap.gender,
					newlineflag : true,
					colspan : 1
				},{
					name : 'nation',
					fieldLabel : '民族',
					value : retMap.nation,
					colspan : 1
				},{
					name : 'birthday',
					fieldLabel : '出生日期',
					value : retMap.bornDay,
					newlineflag : true,
					colspan : 2
				},{
					name : 'address',
					fieldLabel : '住址',
					value : retMap.certAddress,
					newlineflag : true,
					colspan : 2
				},{
					name : 'id',
					fieldLabel : '身份证号',
					value : retMap.id,
					newlineflag : true,
					colspan : 2
				},{
                    xtype: "label",
                    text: "指定司机",
                    newlineflag : true,
                    style:"color:red;margin-top:10px;font-size:12px;border-bottom:#DDDDDD solid 1px; width:100px; display:block",
                    colspan : 3
                },{
					name : 'name0',
					fieldLabel : '司机姓名',
					value : retMap.system[0] == null ? "": retMap.system[0].driver_name,
					newlineflag : true,
					colspan : 1
				},{
					id : retMap.system[0] == null ? "empty0": retMap.system[0].certificate_id,
					name : 'id0',
					fieldLabel : '身份证号',
					value : retMap.system[0] == null ? "": retMap.system[0].certificate_id,
					colspan : 1
				},{
					name : 'success',
					fieldLabel : 'success',
					value : retMap.checkUrl,
					colspan : 1,
					xtype : 'identityField',
				},{
					name : 'name1',
					fieldLabel : '司机姓名',
					value : retMap.system[1] == null ? "": retMap.system[1].driver_name,
					newlineflag : true,
					colspan : 1
				},{
					name : 'id1',
					id : retMap.system[1] == null ? "empty1":retMap.system[1].certificate_id,
					fieldLabel : '身份证号',
					value : retMap.system[1] == null ?"" : retMap.system[1].certificate_id,
					colspan : 1
				},{
					name : 'name2',
					fieldLabel : '司机姓名',
					value : retMap.system[2] == null ? "" : retMap.system[2].driver_name,
					newlineflag : true,
					colspan : 1
				},{
					name : 'id2',
					id : retMap.system[2] == null ? "empty2" : retMap.system[2].certificate_id,
					fieldLabel : '身份证号',
					value : retMap.system[2] == null ?"": retMap.system[2].certificate_id,
					colspan : 1
				}]
			}]
	});
	
	
	uft.te.Authentication.superclass.constructor.call(this, {
		title : '身份验证',
		width : 620,
		height : 400,
		collapsible : false,
		frame : true,
		closable : true,
		draggable : true,
		resizable : true,
		modal : true,
		border : false,
		layout : 'fit',
		items : [this.formPanel],
		buttons : [{
			iconCls : 'btnPrint',
			text : '打印送货单',
			actiontype : 'submit',
			scope : this,
			handler : this.printAction
		},{
			iconCls : 'btnCancel',
			text : '关&nbsp;&nbsp;闭',
			scope : this,
			handler : function() {
				this.destroy();
			}
		}]
    });	
};

Ext.extend(uft.te.Authentication,Ext.Window, {
	printAction : function(){
		var record = this.record;
		if(record) {
			var id = record.get(this.app.headerPkField);
			var params = this.app.newAjaxParams();
			//支持批量打印
			params[this.app.getBillIdField()]=id;
			var url="print.do";
			if(params){
		    	if(url.indexOf('?') == -1){
		    		url +='?';
		    	}
		    	var index=0;
		    	for(key in params){
		    		if(index > 0){
		    			url+= '&';
		    		}
		    		url += key + "="+params[key];
		    		index++;
				}
		    }
			if(this.fireEvent('beforeprint',this,record,params)===false){
				return false;
			}
			window.open(url);
		}else{
			uft.Utils.showWarnMsg('请先选择记录进行打印！');
			return false;
		}
	},
	afterRender : function(){
		uft.te.Authentication.superclass.afterRender.call(this);
		if(retMap.checkUrl == 'tg.png'){
			for(var i=0;i<retMap.system.length;i++){
				if(retMap.system[i].certificate_id == retMap.id){
					Ext.getDom(retMap.id).style.color = "green";
				}
			}
		}
	}
});
