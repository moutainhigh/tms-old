Ext.ns('uft');
/**
 * 自定义按钮打开的窗口
 * @class CustomBtnWindow
 * @extends Ext.Window
 */
uft.CustomBtnWindow = Ext.extend(Ext.Window, {
	billId : null,//表头所选择的单据
	pk_templet : null,//自定义按钮所分配的模板
	funVO : null,//按钮定义对象
	constructor : function(config) {
		Ext.apply(this, config);
		if(!this.billId || this.billId.length == 0){
			uft.Utils.showWarnMsg('请先选择记录！');
			return false;
		}
		if(!this.pk_templet){
			uft.Utils.showWarnMsg('模板参数[pk_templet]不能为空！');
			return false;
		}
		
		var values = Utils.request({
			type : false,
			url : 'getTempletMapByPK4Form.json',
			params : {pk_templet:this.pk_templet}
		});
		//这里做一个排序把隐藏的放后面
		if(!values || !values.data){//可能是请求返回的是错误信息
			if(langague && langague == 'en_US'){
				uft.Utils.showWarnMsg('Template[PK: '+this.pk_templet+'],No items!');
			}else{
				uft.Utils.showWarnMsg('模板[PK:'+this.pk_templet+'],没有任何项目！');
			}
			return false;
		}
		var items = values.data.items;
		var panelHeight = (items.length/2)*35 + 23;
		this.myForm = new uft.extend.form.FormPanel({
			labelWidth : 105,
			border : false,
			autoScroll : true,
			layout : 'tableform',
			padding : '5px 5px 0',
			layoutConfig: {columns:2},
			height : panelHeight,
			defaults:{
				anchor: '95%'
			},
		    items : items
		});
		
		var btns = [{
			xtype : 'button',
			text : '确定',
			scope : this,
			handler : function() {
				var params = app.newAjaxParams(),me = this,funVO = me.funVO;
				var f = me.myForm,ff = f.getForm();
				if(ff.isValid()){
					Ext.apply(params,ff.getFieldValues(false));
				}else{
					var errors = f.getErrors();
					if(errors.length!=0){
						uft.Utils.showWarnMsg(Utils.arrayToString(errors,''));
						return false;
					}
				}
				params['billId'] = me.billId;
				params['pk_fun'] = funVO.pk_fun;
				params['pk_templet'] = me.pk_templet;
				uft.Utils.doAjax({
			    	scope : this,
			    	params : params,
			    	url : ctxPath + funVO.class_name,
			    	isTip : true,
			    	actionType:funVO.fun_name,
			    	success : function(){
			    		if(funVO.help_name){//请求后跳转的文件，作为回调函数
			    			funVO.help_name.call(app,ids);
			    		}
			    		this.close();
			    	}
			    });
			}},{
				xtype : 'button',
				text : '取消',
				scope : this,
				handler : function() {
					this.close();
				}
		}];
		uft.CustomBtnWindow.superclass.constructor.call(this, {
			title : this.funVO.fun_name,
			width : 600,
			collapsible : false,
			shim : true,
			frame : true,
			closable : true, 
			maximizable : true,
			border : false,
			modal : true,
			border : false,
			layout : 'fit',
			items : [this.myForm],
			buttons : btns
	    });
	}
});