/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/*
 * Note that this control will most likely remain as an example, and not as a core Ext form
 * control.  However, the API will be changing in a future release and so should not yet be
 * treated as a final, stable API at this time.
 */

/**
 * @class Ext.ux.form.ItemSelector
 * @extends Ext.form.Field
 * A control that allows selection of between two Ext.ux.form.MultiSelect controls.
 *
 *  @history
 *    2008-06-19 bpm Original code contributed by Toby Stuart (with contributions from Robert Williams)
 *
 * @constructor
 * Create a new ItemSelector
 * @param {Object} config Configuration options
 * @xtype itemselector 
 */
Ext.ux.form.ItemSelector = Ext.extend(Ext.form.Field,  {
    hideNavIcons:false,
    imagePath:"",
    iconUp:"up.gif",
    iconDown:"down.gif",
    iconLeft:"left.gif",
    iconRight:"right.gif",
    iconTop:"top.gif",
    iconBottom:"bottom.gif",
    drawUpIcon:true,
    drawDownIcon:true,
    drawLeftIcon:true,
    drawRightIcon:true,
    drawTopIcon:true,
    drawBotIcon:true,
    
    leftLegend : 'Available',
    rightLegend : 'Selected',
    
    delimiter:',',
    bodyStyle:null,
    border:false,
    defaultAutoCreate:{tag: "div"},
    /**
     * @cfg {Array} multiselects An array of {@link Ext.ux.form.MultiSelect} config objects, with at least all required parameters (e.g., store)
     */
    multiselects:null,

    initComponent: function(){
        Ext.ux.form.ItemSelector.superclass.initComponent.call(this);
        this.addEvents({
            'rowdblclick' : true,
            'change' : true
        });
    },

    onRender: function(ct, position){
        Ext.ux.form.ItemSelector.superclass.onRender.call(this, ct, position);

        // Internal default configuration for both multiselects
        var msConfig = [{
            legend: this.leftLegend,
            draggable: true,
            droppable: true,
            autoHeight : true
        },{
            legend: this.rightLegend,
            droppable: true,
            draggable: true,
            autoHeight : true
        }];

        this.fromMultiselect = new Ext.ux.form.MultiSelect(Ext.applyIf(this.multiselects[0], msConfig[0]));
        this.fromMultiselect.on('dblclick', this.onRowDblClick, this);

        this.toMultiselect = new Ext.ux.form.MultiSelect(Ext.applyIf(this.multiselects[1], msConfig[1]));
        this.toMultiselect.on('dblclick', this.onRowDblClick, this);

        var p = new Ext.Panel({
            border:this.border,
            layout:"table",
            layoutConfig:{columns:3}
        });

        p.add(this.fromMultiselect);
        var icons = new Ext.Panel({style : 'padding:0px 10px 0px 10px',header:false});
        p.add(icons);
        p.add(this.toMultiselect);
        p.render(this.el);
        icons.el.down('.'+icons.bwrapCls).remove();

        // ICON HELL!!!
        if (this.imagePath!="" && this.imagePath.charAt(this.imagePath.length-1)!="/")
            this.imagePath+="/";
            
        var el=icons.getEl();    
        
        this.iconUp = this.imagePath + (this.iconUp || 'up.gif');
        this.iconDown = this.imagePath + (this.iconDown || 'down.gif');
        this.iconLeft = this.imagePath + (this.iconLeft || 'left.gif');
        this.iconRight = this.imagePath + (this.iconRight || 'right.gif');
        this.iconTop = this.imagePath + (this.iconTop || 'top.gif');
        this.iconBottom = this.imagePath + (this.iconBottom || 'bottom.gif');
        
        if (this.drawUpIcon && !this.hideNavIcons) { 
	        this.upIcon = el.createChild({tag:'img', src:this.iconUp, style:{cursor:'pointer', margin:'2px'}});
	        el.createChild({tag: 'br'});
	        el.createChild({tag: 'br'});
	        this.upIcon.on('click', this.up, this);
        }
        if (this.drawDownIcon && !this.hideNavIcons) {
	        this.downIcon = el.createChild({tag:'img', src:this.iconDown, style:{cursor:'pointer', margin:'2px'}});
	        el.createChild({tag: 'br'});
	        el.createChild({tag: 'br'});
	        this.downIcon.on('click', this.down, this);
        }
        if (this.drawLeftIcon && !this.hideNavIcons) {
	        this.addIcon = el.createChild({tag:'img', src:this.iconRight, style:{cursor:'pointer', margin:'2px'}});
	        el.createChild({tag: 'br'});
	        el.createChild({tag: 'br'});
	        this.addIcon.on('click', this.fromTo, this);
        }
        if (this.drawTopIcon && !this.hideNavIcons) {
			this.toTopIcon = el.createChild({tag:'img', src:this.iconTop, style:{cursor:'pointer', margin:'2px'}});
        	el.createChild({tag: 'br'});
        	el.createChild({tag: 'br'});
        	this.toTopIcon.on('click', this.toTop, this);
        }
        if (this.drawRightIcon && !this.hideNavIcons) {
	        this.removeIcon = el.createChild({tag:'img', src:this.iconLeft, style:{cursor:'pointer', margin:'2px'}});
	        el.createChild({tag: 'br'});
	        el.createChild({tag: 'br'});
	        this.removeIcon.on('click', this.toFrom, this);
        }
        if (this.drawBotIcon && !this.hideNavIcons) {
        	this.toBottomIcon = el.createChild({tag:'img', src:this.iconBottom, style:{cursor:'pointer', margin:'2px'}});
        	this.toBottomIcon.on('click', this.toBottom, this);
        }
        
//        this.upIcon = el.createChild({tag:'img', src:this.iconUp, style:{cursor:'pointer', margin:'2px'}});
//        el.createChild({tag: 'br'});
//        this.addIcon = el.createChild({tag:'img', src:this.iconRight, style:{cursor:'pointer', margin:'2px'}});
//        el.createChild({tag: 'br'});
//        this.removeIcon = el.createChild({tag:'img', src:this.iconLeft, style:{cursor:'pointer', margin:'2px'}});
//        el.createChild({tag: 'br'});
//        this.downIcon = el.createChild({tag:'img', src:this.iconDown, style:{cursor:'pointer', margin:'2px'}});
//        el.createChild({tag: 'br'});
//        this.toBottomIcon = el.createChild({tag:'img', src:this.iconBottom, style:{cursor:'pointer', margin:'2px'}});
//        
//        this.toTopIcon.on('click', this.toTop, this);
//        this.upIcon.on('click', this.up, this);
//        this.downIcon.on('click', this.down, this);
//        this.toBottomIcon.on('click', this.toBottom, this);
//        this.addIcon.on('click', this.fromTo, this);
//        this.removeIcon.on('click', this.toFrom, this);
        
//        if (!this.drawUpIcon || this.hideNavIcons) { this.upIcon.dom.style.display='none'; }
//        if (!this.drawDownIcon || this.hideNavIcons) { this.downIcon.dom.style.display='none'; }
//        if (!this.drawLeftIcon || this.hideNavIcons) { this.addIcon.dom.style.display='none'; }
//        if (!this.drawRightIcon || this.hideNavIcons) { this.removeIcon.dom.style.display='none'; }
//        if (!this.drawTopIcon || this.hideNavIcons) { this.toTopIcon.dom.style.display='none'; }
//        if (!this.drawBotIcon || this.hideNavIcons) { this.toBottomIcon.dom.style.display='none'; }

        var tb = p.body.first();
        this.el.setWidth(p.body.first().getWidth());
        p.body.removeClass();

        this.hiddenName = this.name;
        var hiddenTag = {tag: "input", type: "hidden", value: "", name: this.name};
        this.hiddenField = this.el.createChild(hiddenTag);
    },
    doLayout: function(){
        if(this.rendered){
            this.fromMultiselect.fs.doLayout();
            this.toMultiselect.fs.doLayout();
        }
    },
    afterRender: function(){
        Ext.ux.form.ItemSelector.superclass.afterRender.call(this);

        this.toStore = this.toMultiselect.store;
        this.toStore.on('add', this.valueChanged, this);
        this.toStore.on('remove', this.valueChanged, this);
        this.toStore.on('load', this.valueChanged, this);
        this.valueChanged(this.toStore);
    },
    toTop : function() {
    	if(this.fireEvent('totop',this) !== false){
	    	var nums = this.fromMultiselect.view.store.getCount();
	        var records = [];
	        if (nums > 0) {
	            for (var i=0; i<nums; i++) {
	                record = this.fromMultiselect.view.store.getAt(i);
	                records.push(record);
	            }
	            for (var i=0; i<records.length; i++) {
	                record = records[i];
	                if(this.allowDup){
	                    var x=new Ext.data.Record();
	                    record.id=x.id;
	                    delete x;
	                    this.toMultiselect.view.store.add(record);
	                }else{
	                    this.fromMultiselect.view.store.remove(record);
	                    this.toMultiselect.view.store.add(record);
	                }
	            }
	        }
	        this.toMultiselect.view.refresh();
	        this.fromMultiselect.view.refresh();
	        var si = this.toMultiselect.store.sortInfo;
	        if(si){
	            this.toMultiselect.store.sort(si.field, si.direction);
	        }
    	}
    },
    toBottom : function() {
    	if(this.fireEvent('tobottom',this) !== false){
	        var nums = this.toMultiselect.view.store.getCount();
	        var records = [];
	        if (nums > 0) {
	            for (var i=0; i<nums; i++) {
	                record = this.toMultiselect.view.store.getAt(i);
	                records.push(record);
	            }
	            for (var i=0; i<records.length; i++) {
	                record = records[i];
	                this.toMultiselect.view.store.remove(record);
	                if(!this.allowDup){
	                    this.fromMultiselect.view.store.add(record);
	                }
	            }
	        }
	        this.fromMultiselect.view.refresh();
	        this.toMultiselect.view.refresh();
	        var si = this.fromMultiselect.store.sortInfo;
	        if (si){
	            this.fromMultiselect.store.sort(si.field, si.direction);
	        }
    	}
    },

    up : function() {
        var record = null;
        var selectionsArray = this.toMultiselect.view.getSelectedIndexes();
        selectionsArray.sort();
        var newSelectionsArray = [];
        if (selectionsArray.length > 0) {
            for (var i=0; i<selectionsArray.length; i++) {
                record = this.toMultiselect.view.store.getAt(selectionsArray[i]);
                if ((selectionsArray[i] - 1) >= 0) {
                    this.toMultiselect.view.store.remove(record);
                    this.toMultiselect.view.store.insert(selectionsArray[i] - 1, record);
                    newSelectionsArray.push(selectionsArray[i] - 1);
                }
            }
            this.toMultiselect.view.refresh();
            this.toMultiselect.view.select(newSelectionsArray);
        }
    },

    down : function() {
        var record = null;
        var selectionsArray = this.toMultiselect.view.getSelectedIndexes();
        selectionsArray.sort();
        selectionsArray.reverse();
        var newSelectionsArray = [];
        if (selectionsArray.length > 0) {
            for (var i=0; i<selectionsArray.length; i++) {
                record = this.toMultiselect.view.store.getAt(selectionsArray[i]);
                if ((selectionsArray[i] + 1) < this.toMultiselect.view.store.getCount()) {
                    this.toMultiselect.view.store.remove(record);
                    this.toMultiselect.view.store.insert(selectionsArray[i] + 1, record);
                    newSelectionsArray.push(selectionsArray[i] + 1);
                }
            }
            this.toMultiselect.view.refresh();
            this.toMultiselect.view.select(newSelectionsArray);
        }
    },

    fromTo : function() {
    	if(this.fireEvent('fromto',this) !== false){
	        var selectionsArray = this.fromMultiselect.view.getSelectedIndexes();
	        var records = [];
	        if (selectionsArray.length > 0) {
	            for (var i=0; i<selectionsArray.length; i++) {
	                record = this.fromMultiselect.view.store.getAt(selectionsArray[i]);
	                records.push(record);
	            }
	            if(!this.allowDup)selectionsArray = [];
	            for (var i=0; i<records.length; i++) {
	                record = records[i];
	                if(this.allowDup){
	                    var x=new Ext.data.Record();
	                    record.id=x.id;
	                    delete x;
	                    this.toMultiselect.view.store.add(record);
	                }else{
	                    this.fromMultiselect.view.store.remove(record);
	                    this.toMultiselect.view.store.add(record);
	                    selectionsArray.push((this.toMultiselect.view.store.getCount() - 1));
	                }
	            }
	        }
	        this.toMultiselect.view.refresh();
	        this.fromMultiselect.view.refresh();
	        var si = this.toMultiselect.store.sortInfo;
	        if(si){
	            this.toMultiselect.store.sort(si.field, si.direction);
	        }
	        this.toMultiselect.view.select(selectionsArray);
    	}
    },

    toFrom : function() {
    	if(this.fireEvent('tofrom',this) !== false){
	        var selectionsArray = this.toMultiselect.view.getSelectedIndexes();
	        var records = [];
	        if (selectionsArray.length > 0) {
	            for (var i=0; i<selectionsArray.length; i++) {
	                record = this.toMultiselect.view.store.getAt(selectionsArray[i]);
	                records.push(record);
	            }
	            selectionsArray = [];
	            for (var i=0; i<records.length; i++) {
	                record = records[i];
	                this.toMultiselect.view.store.remove(record);
	                if(!this.allowDup){
	                    this.fromMultiselect.view.store.add(record);
	                    selectionsArray.push((this.fromMultiselect.view.store.getCount() - 1));
	                }
	            }
	        }
	        this.fromMultiselect.view.refresh();
	        this.toMultiselect.view.refresh();
	        var si = this.fromMultiselect.store.sortInfo;
	        if (si){
	            this.fromMultiselect.store.sort(si.field, si.direction);
	        }
	        this.fromMultiselect.view.select(selectionsArray);
    	}
    },

    valueChanged: function(store) {
        var record = null;
        var values = [];
        for (var i=0; i<store.getCount(); i++) {
            record = store.getAt(i);
            values.push(record.get(this.toMultiselect.valueField));
        }
        this.hiddenField.dom.value = values.join(this.delimiter);
        this.fireEvent('change', this, this.getValue(), this.hiddenField.dom.value);
    },

    getValue : function() {
        return this.hiddenField.dom.value;
    },

    onRowDblClick : function(vw, index, node, e) {
        if (vw == this.toMultiselect.view){
            this.toFrom();
        } else if (vw == this.fromMultiselect.view) {
            this.fromTo();
        }
        return this.fireEvent('rowdblclick', vw, index, node, e);
    },

    reset: function(){
        range = this.toMultiselect.store.getRange();
        this.toMultiselect.store.removeAll();
        this.fromMultiselect.store.add(range);
        var si = this.fromMultiselect.store.sortInfo;
        if (si){
            this.fromMultiselect.store.sort(si.field, si.direction);
        }
        this.valueChanged(this.toMultiselect.store);
    }
});

Ext.reg('itemselector', Ext.ux.form.ItemSelector);

//backwards compat
Ext.ux.ItemSelector = Ext.ux.form.ItemSelector;
