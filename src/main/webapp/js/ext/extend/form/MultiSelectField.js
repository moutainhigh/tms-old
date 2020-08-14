Ext.namespace('uft.extend.form');
/**
 * 多选checkbox，该field将打开一个包括多个checkbox的选择窗口，用户可以多选。
 * 当点击确定按钮，将选择的值赋给该field
 * 与uft.extend.form.MultiSelectWindow结合使用
 * 
 * @class uft.extend.form.MultiSelectField
 * @extends Ext.form.TriggerField
 */
uft.extend.form.MultiSelectField = Ext.extend(Ext.form.TriggerField, {
	defaultAutoCreate : {tag: "input", type: "text", size: "24", autocomplete: "off"},
	triggerClass : 'x-form-search-trigger',
	editable : true,
	layzyLoad : true,//是否延迟加载，是则在点击了trigger后再加载数据
	constructor : function(config) {
		this.refWinId = Ext.id();
		Ext.apply(this, config);
		uft.extend.form.MultiSelectField.superclass.constructor.call(this);
	},
    initComponent : function(){
    	this.hiddenName="_"+this.name;
    	this.hiddenId = "_"+this.id;
        if(this.dataUrl && this.layzyLoad === false){
        	//通过url去读取选项值
        	this.items = Utils.doSyncRequest(this.dataUrl,this.baseParams);
        }    	
        uft.extend.form.MultiSelectField.superclass.initComponent.call(this);
    },
    onRender : function(ct, position){
    	uft.extend.form.MultiSelectField.superclass.onRender.call(this, ct, position);
        this.hiddenField = this.el.insertSibling({tag:'input', type:'hidden', name: this.hiddenName,
                id: (this.hiddenId || Ext.id())}, 'before', true);
    },	
    onTriggerClick : function(){
    	if(this.readOnly || this.disabled){
            return;
        }
        var refWin = Ext.getCmp(this.refWinId);
    	if(!refWin){
    		if(!this.items && this.dataUrl){
    			//通过url去读取选项值
    			this.items = Utils.doSyncRequest(this.dataUrl,this.baseParams);
    		}
    		refWin = new uft.extend.form.MultiSelectWindow({srcField : this,items:this.items});
			refWin.on('submit',function(refWin){
				if(this.gridEditor){
					//参照处于可编辑表格中
					var grid = this.gridEditor.grid;
					grid.startEditing(this.gridEditor.row, this.gridEditor.col);
				}				
	            this.setValueAfterSubmit(refWin.returnValue);
		    },this);
    	}
	    refWin.show();
    },
    /**
     * 鼠标移开后，根据当前值进行匹配,此时的当前值是label值，比如[自由态,提交态]
     */
	onBlur : function(){
		var label = this.el.dom.value;
		if(!label || label.length < 3){
			this.clearValue();
			return;
		}
		var value = this.findValues(label);
    	if(this.hiddenField){
	    	if(value && value.indexOf('[') == 0){
	    		this.hiddenField.value=value.substring(1,value.length-1);
	    	}else{
	    		this.hiddenField.value=value;
	    	}
    	}
	},
    /**
     * 返回的value不包括[]
     * @return {}
     */
    getValue : function(){
        return Ext.isDefined(this.hiddenField.value) ? this.hiddenField.value : uft.extend.form.MultiSelectField.superclass.getValue.call(this);
    },
    clearValue : function(){
        this.setRawValue('');
        this.hiddenField.value='';
        this.store = null;
        uft.extend.form.MultiSelectField.superclass.setValue.call(this, '');
    },
    /**
     * value的格式使用[1,2]
     * hiddenField.value的格式如:1,2，不包括[]
     * @param {} value
     * @return {}
     */
    setValue : function(value){
    	var labels = this.findLabels(value);
    	if(this.hiddenField){
	    	if(value && value.indexOf('[') == 0){
	    		this.hiddenField.value=value.substring(1,value.length-1);
	    	}else{
	    		this.hiddenField.value=value;
	    	}
    	}
        uft.extend.form.MultiSelectField.superclass.setValue.call(this, labels);
        return this;
    },
    /**
     * 
     * @param {} value
     */
    setValueAfterSubmit : function(value){
    	var originalValue = this.getValue();
		if(value && value.indexOf('[') == 0){
			value = value.substring(1,value.length-1);
		}    	
    	if(value != originalValue){
    		this.setValue(value);
    		this.fireEvent('change', this,value,originalValue);
    	}
    },
    findValues : function(label){
    	if(!label){
    		return '';
    	}
    	//如果value不是标准格式[value]，则加入前后缀
    	if(label.indexOf('[')!=0){
    		label = '[' + label + ']';
    	}
		label = label.substring(1,label.length-1); //去除两边的中跨号
		var labelAry = label.split(',');
		var valueAry = [];
		for(var i=0;i<labelAry.length;i++){
	    	for(var j=0;j<this.items.length;j++){
	    		if(labelAry[i] == this.items[j].boxLabel){
	    			valueAry.push(this.items[j].inputValue);
	    			break;
	    		}
	    	}
    	}
    	if(valueAry.length > 0){
    		return '['+valueAry.join(',')+']';
    	}
    	return '';
    },
    findLabels : function(value){
    	if(!value){
    		return '';
    	}
    	//如果value不是标准格式[value]，则加入前后缀
    	if(value.indexOf('[')!=0){
    		value = '[' + value + ']';
    	}
    	var labels = new Array();
    	if(value&& value.length>2){
	    	value = value.substring(1,value.length-1); //过滤前后两个[]
	    	var arr = value.split(",");
	    	for(var i=0;i<arr.length;i++){
		    	for(var j=0;j<this.items.length;j++){
		    		if(arr[i] == this.items[j].inputValue){
		    			labels.push(this.items[j].boxLabel);
		    			break;
		    		}
		    	}
	    	}
	    	if(labels.length>0)
				return '['+labels.join(',')+']';
    	}
    	return '';
    }
});
Ext.reg("multiselectfield",uft.extend.form.MultiSelectField);