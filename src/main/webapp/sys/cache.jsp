<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	</body>
	<script type="text/javascript" >
		var cacheColumns = [{
			header : 'cacheName',
			dataIndex : 'cacheName',
			width : 100,
			sortable : true
		},{
			header : 'cacheKey',
			dataIndex : 'cacheKey',
			sortable : true
		},{
			header : 'value',
			width : 250,
			dataIndex : 'value',
			sortable : true
		},{
			header : '命中次数',
			dataIndex : 'hitCount',
			width : 70,
			sortable : true
		},{
			header : '有效时间',
			dataIndex : 'expirationTime',
			width : 130,
			sortable : true
		},{
			header : '创建时间',
			dataIndex : 'creationTime',
			width : 130,
			sortable : true
		}];
		var cacheRecordType = [{
			name : 'cacheName',
			type : 'string'
		}, {
			name : 'cacheKey',
			type : 'string'
		}, {
			name : 'value',
			type : 'string'
		}, {
			name : 'hitCount',
			type : 'string'
		}, {
			name : 'expirationTime',
			type : 'string'
		}, {
			name : 'creationTime',
			type : 'string'
		}];
		var cacheGrid = new uft.extend.grid.BasicGrid({
			autoExpandColumn : 2,
			border : false,
			dataUrl : webncCtxPath + '/cache/getAllCache.json',
			recordType : cacheRecordType,
			columns : cacheColumns
		});
		uft.jf.CacheToolbar = Ext.extend(uft.jf.ToftToolbar, {
			btn_clearAll : new uft.extend.Button({
		        text : '清除所有',
		        scope:this,
		        handler : function(){
					uft.Utils.doAjax({
				    	scope : this,
				    	url : 'clearAllCache.json',
				    	success : function(){
				    		this.app.reloadHeaderGrid();
				    	}
				    });
				},
		        enabledStatus:'ALL'
			}),
			btn_clear : new uft.extend.Button({
		        text : '清除',
		        scope:this,
		        handler : function(){
					var record = uft.Utils.getSelectedRecord(this.app.headerGrid,true);
					if(record) {
						uft.Utils.doAjax({
					    	scope : this,
					    	url : 'clearElement.json',
					    	params : {cacheName:record.data['cacheName'],cacheKey:record.data['cacheKey']},
					    	success : function(){
						    	var store = this.app.getHeaderGrid().getStore();
						    	store.remove(record);
					    	}
					    });
					}
				},
		        enabledStatus:[uft.jf.pageStatus.OP_NOTEDIT_LIST]
			}),
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_clearAll);
				btns.push(this.btn_clear);
				return btns;
			}
		});
		var cacheToolbar = new uft.jf.CacheToolbar();		
		var app = new uft.jf.ToftPanel({
			headerGrid : cacheGrid,
			toolbar : cacheToolbar
		});
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
