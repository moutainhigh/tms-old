<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
		<script type="text/javascript">
		var statusMgr = new uft.jf.StatusMgr();
		var funTree = new uft.extend.tree.Tree({
			id : 'funTree',
			treeRootNodeText:'功能菜单', //默认根节点名称
			rootVisible : false,
			border : false,
			dataUrl : 'getFunTree.json', //默认数据来源
			isTreeFilter:true
		});
		var funPanel = new Ext.Panel({
			region : 'west',
			layout : 'fit',
			width : 200, 
			border : true,
			split : true,
			items : [funTree]
		});		
		var columns = [{
			   header : 'pk_templet',
			   hidden : true,
		       dataIndex: 'pk_templet'
		    },{
			   header : 'tempstyle',
			   hidden : true,
		       dataIndex: 'tempstyle'
		    },{
		        header: '<span class="uft-grid-header-column-not-edit">模板类型</span>',
		        dataIndex: 'tempstyle_name'
			},{
		         header: '<span class="uft-grid-header-column-not-edit">模板编码</span>',
		         dataIndex: 'templet_code'
			},{
		         header: '节点标识',
		         dataIndex: 'nodekey',
		         editor : {xtype:'textfield'}
			},{
		       header: '<span class="uft-grid-header-column-not-edit">模板名称</span>',
		       width : 200,
		       dataIndex: 'templet_name'
		}];
		var recordType =[
	        {name: 'pk_templet', type: 'string'},
	        {name: 'tempstyle', type: 'int'},
	        {name: 'tempstyle_name', type: 'string'},
	        {name: 'templet_code', type: 'string'},
	        {name: 'nodekey', type: 'string'},
	        {name: 'templet_name', type: 'string'}
		];
		
		var templetGrid = new uft.extend.grid.EditorGrid({
			region : 'center',
			dataUrl : 'loadTemplet.json',
			params : {},
			singleSelect : false,
			immediatelyLoad : false,
			isCheckboxSelectionModel : true,
			refMultiSelectionModel : true,
			isAddBbar : false,
			disabled : true,
			recordType : recordType,
			columns : columns
		});
		var main = new Ext.Panel({
			region : 'center',
			layout : 'border',
			border : false,
			items : [funPanel,templetGrid]
		});
		TempletDistToolbar = function(config){
			Ext.apply(this, config);
			this.btn_dist = new Ext.Button({
				text : '分配',
				tooltip : 'Ctrl+E',
				keyBinding: {
		            key: 'e',
		            ctrl : true
		        },
				iconCls : 'btnEdit',
				handler : this.btn_dist_handler,
				enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_CARD]
			});
			this.btn_save = new Ext.Button({
				text : '保存',
				tooltip : 'Ctrl+S',
				keyBinding: {
		            key: 's',
		            ctrl : true
		        },
				iconCls : 'btnSave',
				handler : this.btn_save_handler,
				enabledStatus:[uft.jf.pageStatus.OP_EDIT]
			});
			this.btn_cancel = new Ext.Button({
				text : '取消',
				tooltip : 'Ctrl+Q',
				keyBinding: {
		            key: 'q',
		            ctrl : true
		        },
				iconCls : 'btnCancel',
				handler : this.btn_cancel_handler,
				enabledStatus:[uft.jf.pageStatus.OP_EDIT]
			});
			this.btn_ref = new Ext.Button({
				text : '刷新',
				tooltip : 'Ctrl+R',
				keyBinding: {
		            key: 'r',
		            ctrl : true
		        },
				iconCls : 'btnRef',
				handler : this.btn_ref_hanlder,
				enabledStatus:[uft.jf.pageStatus.OP_INIT,uft.jf.pageStatus.OP_NOTEDIT_CARD]
			});
			TempletDistToolbar.superclass.constructor.call(this,{
				autoHeight : true,
				defaults : {
					scope : this
				},
				plugins:new Ext.ux.ToolbarKeyMap(),
				items : [this.btn_dist,this.btn_save,this.btn_cancel,this.btn_ref]
			});
		};
		Ext.extend(TempletDistToolbar,Ext.Toolbar, {
			btn_dist_handler : function(){
				var node = funTree.getSelectedNode();
				//只能选择按钮节点
				var props = node.attributes.properties;
				if(props){
					if(props['fun_property'] != 2 && props['fun_property'] != 3){
						uft.Utils.showWarnMsg('只能选择按钮节点！');
						return false;
					}
				}
				templetGrid.enable();
				statusMgr.setPageStatus(uft.jf.pageStatus.OP_EDIT);
				statusMgr.updateStatus();
			},
			btn_save_handler : function(){
				var pk_fun = funTree.getSelectedNode().id;
				var params = [];
				var records = uft.Utils.getSelectedRecords(templetGrid);
				var tempstyleAry = [];
				if(records){
					for(var i=0;i<records.length;i++){
						var param = {};
						param.pk_fun = pk_fun;
						param.pk_templet = records[i].get('pk_templet');
						param.nodekey = records[i].get('nodekey');
						param.tempstyle = records[i].get('tempstyle');
						params.push(param);
					}
				}
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : {pk_fun:pk_fun,templetAry: Ext.encode(params)},
			    	url : 'saveTempletDist.json',
			    	isTip : true,
			    	success : function(values){
			    		templetGrid.disable();
			    		statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
						statusMgr.updateStatus();
			    	}
			    });
			},
			btn_cancel_handler : function(){
				templetGrid.disable();
				statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
				statusMgr.updateStatus();
			},
			btn_ref_handler : function(){
				templetGrid.getStore().reload();
			}
		});
		var toolbar = new TempletDistToolbar();
		var app = new Ext.Panel({
			layout:'fit',
			border : false,
			tbar : toolbar,
			renderTo : document.body,
			height : document.documentElement.clientHeight,
			items : [main],
			fireResize : function(w, h){
		        this.fireEvent('resize', this, w, h, w, h);
		        this.setWidth(w);
		        this.setHeight(h);
		        this.doLayout();
		    },
		    initComponent : function() {
		        uft.jf.UIPanel.superclass.initComponent.call(this);
		        Ext.EventManager.onWindowResize(this.fireResize, this);
		    },
		});
		Ext.onReady(function(){
			var items = toolbar.items.items;
			for(var i=0;i<items.length;i++){
				statusMgr.addListener(items[i],items[i].onstatuschange);
				statusMgr.addBizListener(items[i],items[i].onbizstatuschange);
			}
			statusMgr.setPageStatus(uft.jf.pageStatus.OP_INIT);
			statusMgr.updateStatus();
			
			funTree.on('click',function(node,event){
				var props = node.attributes.properties;
				if(props){
					if(props['fun_property'] == 2 || props['fun_property'] == 3){
						var options = {};
						options.params = {};
						options.params['pk_fun'] = node.id;
						templetGrid.getStore().reload(options);
						statusMgr.setPageStatus(uft.jf.pageStatus.OP_NOTEDIT_CARD);
						statusMgr.updateStatus();
					}
				}
			});
			//当加载完数据后，根据已选中的数据恢复行数据的状态，如该模板已经分配，那么需要选中该行
			templetGrid.getStore().on('load',function(store,records,options){
				var pk_fun = funTree.getSelectedNode().id;
			    uft.Utils.doAjax({
			    	scope : this,
			    	params : {pk_fun:pk_fun},
			    	url : 'loadTempletDist.json',
			    	isTip : false,
			    	success : function(values){
			    		if(values && values.datas){
			    			var templetDistVOs = values.datas;
			    			//恢复选中状态
			    			var rows = [];
			    			for(var i=0;i<templetDistVOs.length;i++){
			    				for(var j=0;j<store.getCount();j++){
			    					var record = store.getAt(j);
			    					//这里需要比较模板类型以及模板pk
			    					if(templetDistVOs[i].pk_templet==record.get('pk_templet') && templetDistVOs[i].tempstyle == record.get('tempstyle')){
			    						record.set('nodekey',templetDistVOs[i].nodekey);
			    						rows.push(j);
			    						break;
			    					}
			    				}
			    			}
			    			templetGrid.getSelectionModel().selectRows(rows);
			    		}
			    	}
			    });
			});	
		});		
		</script>	
	</body>
	<%@ include file="/common/footer.jsp"%>
</html>
	
