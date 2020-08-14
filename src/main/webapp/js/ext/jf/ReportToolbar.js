Ext.namespace('uft.jf');
/**
 * 报表的toolbar，只包括导出和打印按钮
 * @class uft.jf.ReportToolbar
 * @extends Ext.Toolbar
 */
uft.jf.ReportToolbar = Ext.extend(Ext.Toolbar, {
	cls : "uft-grid-row-toolbar",
	app : null,
	constructor : function(config){
		Ext.apply(this, config);
		this.btn_export = {
			text : '导出', 
			tooltip : 'Ctrl+O',
			iconCls : 'btnExport',
			keyBinding: {
	            key: 'o',
	            ctrl : true
	        },
			handler : this.btn_export_handler
		};
		this.btn_print = {
			text : '打印', 
			tooltip : 'Crtl+P',
			iconCls : 'btnPrint',
			keyBinding: {
	            key: 'p',
	            ctrl : true
	        },
			handler : this.btn_print_handler
		};
		uft.jf.ReportToolbar.superclass.constructor.call(this,{
			autoHeight : true,
			defaults : {
				scope : this
			},
			plugins:new Ext.ux.ToolbarKeyMap(),
			items : this.getBtnArray()
		});		
	},
	getBtnArray : function(){
		var btns = new Array();
		btns.push(this.btn_export);
		//btns.push('-');
		//btns.push(this.btn_print);
		return btns;
	},
	/**
	 * 报表的打印是打印表头的多条记录
	 * @Override
	 */
	btn_print_handler : function(){
		var params = this.app.newAjaxParams();
		var url="reportPrint.do";
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
		if(this.fireEvent('beforeprint',this,params)===false){
			return false;
		}
		window.open(url);
	},
	//导出
	btn_export_handler : function(){
		var app = this.app;
		
		var config = app.newAjaxParams();
		//得到查询框的查询参数
		if(app.topQueryForm){
			var params = app.topQueryForm.getFormParams();
			//存在查询参数
			config.PUB_PARAMS = params;
		}
		//对于钻取的明细报表，需要加入url中的参数
		var aParams = document.location.search.substr(1).split('&');
		for (i = 0; i < aParams.length; i++) {
			var aParam = aParams[i].split('=');
			config[aParam[0]] = aParam[1];
		}
		var headerGrid = app.getHeaderGrid();
		headerGrid.doExport(config);
	}	
});