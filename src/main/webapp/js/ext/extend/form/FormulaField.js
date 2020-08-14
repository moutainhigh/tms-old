Ext.namespace('uft.extend.form');
/**
 * 专门用于公式的大文本输入框
 * 
 * @class uft.extend.form.FormulaField
 * @extends uft.extend.form.BigTextField
 */
uft.extend.form.FormulaField = Ext.extend(uft.extend.form.BigTextField, {
	onTriggerClick : function(){
    	if(this.readOnly || this.disabled){
            return;
        }
        this.triggerBlur(); //点击后调用失去焦点动作,Ext.Window不是真正的modal窗口
        //每次都新建一个窗口，然后colse掉，因为每个单元格的对象都会不一样，跟参照不同
		var bigTextWin = new uft.extend.form.FormulaWindow({
			srcField : this,
			textValue : this.gridEditor.startValue
		}); 
		bigTextWin.on('aftersubmit',function(){ //大文本输入窗口点击确定后触发的事件
			//只有处于编辑状态，下面的setValue才有效
			if(this.gridEditor){
				var grid = this.gridEditor.grid;
				grid.startEditing(this.gridEditor.row, this.gridEditor.col);
			}
            this.setValueAfterSubmit(bigTextWin.returnValue);
	    },this);
    	bigTextWin.show();
    }
});
Ext.reg("formulafield",uft.extend.form.FormulaField);