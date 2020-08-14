Ext.namespace('uft.extend.form');
/**
 * 颜色选择器
 * @class uft.extend.form.ColorPicker
 * @extends Ext.form.TextField
 */
uft.extend.form.ColorPicker = Ext.extend(Ext.form.TriggerField,{
	defaultAutoCreate : {tag: "input", type: "text", size: "24", autocomplete: "off"},
	triggerClass : 'x-form-search-trigger',
	value:"#000",
    onTriggerClick : function(){
    	if(this.readOnly || this.disabled){
            return;
        }
        //不能执行失去焦点动作，否则得到的坐标是不准确的
        //this.triggerBlur(); //点击后调用失去焦点动作,Ext.Window不是真正的modal窗口
        //每次都新建一个窗口，然后colse掉，因为每个单元格的对象都会不一样，跟参照不同
		var w = new Ext.Window({
			x:this.getPosition()[0]+this.getWidth(),
			y:this.getPosition()[1]+this.getHeight(),
			title:"取色框",
			items:[{
				xtype:"colorpalette",
				listeners:{"select":function(p, color){
					var v = '#'+color;
					var ge = this.gridEditor;
					if(ge){//在编辑表格中使用
						var grid = ge.grid,col = ge.col,row = ge.row;
						if(!grid.colModel.isCellEditable(col, row)){
							return;
						}
						grid.startEditing(row, col);
						this.el.dom.title = v;
						this.setValue(v);
						this.gridEditor.grid.stopEditing();
					}else{
						this.setValue(v);
					}
					w.close();
				},scope:this}}]
		});
		w.show();
    }	
});
	
Ext.reg('colorpicker', uft.extend.form.ColorPicker);
