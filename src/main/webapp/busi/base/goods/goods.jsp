<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<%@ include file="/common/header.jsp"%>
	</head>
	<body>
	<!-- 加入这个注释避免当body没有html内容时，在ie下出现空白行	-->
	<nw:Bill templetVO="${templetVO}" headerGridPageSize="20" headerGridImmediatelyLoad="true" headerGridCheckboxSelectionModel="true"
				headerGridSingleSelect="false" bodyGridsPagination="false"/>
	</body>
	<script type="text/javascript">

		//自定义工具类，从基类继承可以使用基类已经定义的button，以及button和handler的绑定
		MyToolbar = Ext.extend(uft.jf.ToftToolbar, {
			getBtnArray : function(){
				var btns = new Array();
				btns.push(this.btn_query);
				btns.push(this.btn_add);
				btns.push(this.btn_edit);
				btns.push(this.btn_save);
				btns.push(this.btn_can);
				btns.push(this.btn_del);
				btns.push(this.btn_ref);
				btns.push(this.btn_list);
				btns.push(this.btn_card);
				btns.push(this.btn_import);
				btns.push(this.btn_export);
				return btns;
			},
			btn_edit_handler : function(){
				MyToolbar.superclass.btn_edit_handler.call(this);
				var goods_code = uft.Utils.getField('goods_code');
				goods_code.setReadOnly(true);
			}
		});
		${moduleName}.appUiConfig.toolbar = new MyToolbar(${moduleName}.appUiConfig);
		var app = new uft.jf.ToftPanel(${moduleName}.appUiConfig);
		//编辑 长、宽、高，计算体积
		function afterChangeLengthOrWidthOrHeight(){
			var length = uft.Utils.getField("length").getValue();
			var width = uft.Utils.getField("width").getValue();
			var height = uft.Utils.getField("height").getValue();
			uft.Utils.setFieldValue("volume",length*width*height);
		}
		//编辑 长、宽、高，计算体积
		function afterEditLengthOrWidthOrHeight(record){
			var length = uft.Utils.getNumberColumnValue(record,"length");
			var width = uft.Utils.getNumberColumnValue(record,"width");
			var height = uft.Utils.getNumberColumnValue(record,"height");
			uft.Utils.setColumnValue(record,"volume",length*width*height);
		}
		// 编辑货品包装后，需要判定是否已经存在该包装了
		function afterEditGoodsPackcorpName(record){
			var pk_goods_packcorp = uft.Utils.getColumnValue(record,'pk_goods_packcorp');
			var grid = Ext.getCmp('ts_goods_pack_rela');
			if(grid){
				var store = grid.getStore();
				var count = store.getCount();
				for(var i=0;i<count;i++){
					var _record = store.getAt(i);
					var _pk_goods_packcorp = uft.Utils.getColumnValue(_record,'pk_goods_packcorp');
					if(record.id != _record.id && pk_goods_packcorp == _pk_goods_packcorp){
						record.beginEdit();
						record.set('pk_goods_packcorp',null);
						record.set('goods_packcorp_name',null);
						record.endEdit();
						uft.Utils.showWarnMsg(uft.Internal.getText('不能选择相同的包装明细！'));
						return;
					}
				}
			}
		}
	</script>
	<%@ include file="/common/footer.jsp"%>
</html>
	
