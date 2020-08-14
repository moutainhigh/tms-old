/*!
 * Ext JS Library 3.2.1
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
Ext.ns('Ext.ux.form');

/**
 * 文件上传组件，在uploadWindow.js中使用
 * @class Ext.ux.form.FileUploadField
 * @extends Ext.form.TextField
 * Creates a file upload field.
 * @xtype fileuploadfield
 */
Ext.ux.form.FileUploadField = Ext.extend(Ext.form.TextField,  {
    /**
     * @cfg {String} buttonText The button text to display on the upload button (defaults to
     * 'Browse...').  Note that if you supply a value for {@link #buttonCfg}, the buttonCfg.text
     * value will be used instead if available.
     */
    buttonText: '浏览...',
    /**
     * @cfg {Boolean} buttonOnly True to display the file upload field as a button with no visible
     * text field (defaults to false).  If true, all inherited TextField members will still be available.
     */
    buttonOnly: false,
    /**
     * @cfg {Number} buttonOffset The number of pixels of space reserved between the button and the text field
     * (defaults to 3).  Note that this only applies if {@link #buttonOnly} = false.
     */
    buttonOffset: 3,
    /**
     * @cfg {Object} buttonCfg A standard {@link Ext.Button} config object.
     */

    // private
    readOnly: false,

    /**
     * @hide
     * @method autoSize
     */
    autoSize: Ext.emptyFn,
    
    //合法文件集合,扩展名集合
    permitted_extensions : ['doc','docx','xls','xlsx','ppt','pptx','pdf','jpg','jpeg','png','bmp'],

    // private
    initComponent: function(){
        Ext.ux.form.FileUploadField.superclass.initComponent.call(this);
        this.addEvents(
            /**
             * @event fileselected
             * Fires when the underlying file input field's value has changed from the user
             * selecting a new file from the system file selection dialog.
             * @param {Ext.ux.form.FileUploadField} this
             * @param {String} value The file value returned by the underlying file input field
             */
            'fileselected'
        );
    },

    // private
    onRender : function(ct, position){
        Ext.ux.form.FileUploadField.superclass.onRender.call(this, ct, position);

        this.wrap = this.el.wrap({cls:'x-form-field-wrap x-form-file-wrap'});
        this.el.addClass('x-form-file-text');
        this.el.dom.removeAttribute('name');
        this.createFileInput();

        var btnCfg = Ext.applyIf(this.buttonCfg || {}, {
            text: this.buttonText
        });
        this.button = new Ext.Button(Ext.apply(btnCfg, {
        	disabled : this.readOnly,
            renderTo: this.wrap,
            cls: 'x-form-file-btn' + (btnCfg.iconCls ? ' x-btn-icon' : '')
        }));

        if(this.buttonOnly){
            this.el.hide();
            this.wrap.setWidth(this.button.getEl().getWidth());
        }

        this.bindListeners();
        this.resizeEl = this.positionEl = this.wrap;
    },
    //fileInput对象可以从外部传入，这样外部可以调用该函数，进行重新绑定事件，
    //这种情况出现在无刷新上传时，通过clone一个fileInput，但是clone后的fileInput没有事件，需要重新绑定
    bindListeners: function(fileInput){
    	if(!fileInput){
    		fileInput = this.fileInput;
    	}
        fileInput.on({
            scope: this,
            mouseenter: function() {
                this.button.addClass(['x-btn-over','x-btn-focus']);
            },
            mouseleave: function(){
                this.button.removeClass(['x-btn-over','x-btn-focus','x-btn-click']);
            },
            mousedown: function(){
                this.button.addClass('x-btn-click');
            },
            mouseup: function(){
                this.button.removeClass(['x-btn-over','x-btn-focus','x-btn-click']);
            },
            change: function(){
                var v = fileInput.dom.value;
                this.setValue(v);
                this.fileSelected(v);
            }
        }); 
    },
    
    createFileInput : function() {
        this.fileInput = this.wrap.createChild({
            id: this.getFileInputId(),
            name: this.name||this.getId(),
            cls: 'x-form-file',
            tag: 'input',
            type: 'file',
            size: 1
        });
        this.fileInput.dom.disabled = this.readOnly;
    },
    reset : function(){
//    	if(this.fileInput){
//    		this.fileInput.remove();
//    		this.createFileInput();
//        	this.bindListeners();
//    	}
        Ext.ux.form.FileUploadField.superclass.reset.call(this);
    },

    // private
    getFileInputId: function(){
        return this.id + '-file';
    },

    // private
    onResize : function(w, h){
        Ext.ux.form.FileUploadField.superclass.onResize.call(this, w, h);

        this.wrap.setWidth(w);

        if(!this.buttonOnly){
            var w = this.wrap.getWidth() - this.button.getEl().getWidth() - this.buttonOffset;
            this.el.setWidth(w);
        }
    },
    setReadOnly : function(readOnly){
        Ext.ux.form.FileUploadField.superclass.setReadOnly.call(this,readOnly);
        //对按钮特殊处理
        if(this.fileInput){
        	this.fileInput.dom.disabled = readOnly;
        }
        if(this.button){
        	this.button.setDisabled(readOnly);
        }
    },    

    // private
    onDestroy: function(){
        Ext.ux.form.FileUploadField.superclass.onDestroy.call(this);
        Ext.destroy(this.fileInput, this.button, this.wrap);
    },
    
    onDisable: function(){
        Ext.ux.form.FileUploadField.superclass.onDisable.call(this);
        this.doDisable(true);
    },
    
    onEnable: function(){
        Ext.ux.form.FileUploadField.superclass.onEnable.call(this);
        this.doDisable(false);
    },
    
    // private
    doDisable: function(disabled){
        this.fileInput.dom.disabled = disabled;
        this.button.setDisabled(disabled);
    },

    // private
    preFocus : Ext.emptyFn,

    // private
    alignErrorIcon : function(){
        this.errorIcon.alignTo(this.wrap, 'tl-tr', [2, 0]);
    },
	getFileExtension : function(filename){
	    var result = null;
	    var parts = filename.split('.');
	    if (parts.length > 1) {
	      result = parts.pop();
	    }
	    if(result){
	    	result = result.toLowerCase();
	    }
	    return result;
	}
    ,isPermittedFileType : function(filename){
	   var result = true;
	   if (this.permitted_extensions.length > 0) {
	      result = this.permitted_extensions.indexOf(this.getFileExtension(filename)) != -1;
	   }
	   return result;
   }
	,fileSelected : function(filename) {
		if (!this.isPermittedFileType(filename)) {
			this.setValue(null);
			Ext.WindowMgr.zseed = 30000;
			Ext.MessageBox.show({
	           	title: '警告',
	           	msg: String.format('您选择的文件不允许上传.<br/>请选择以下扩展名的文件: {1}', filename,this.permitted_extensions.join(',')),
	           	width:300,
	           	buttons: Ext.MessageBox.OK,
	           	icon:Ext.MessageBox.ERROR, 
	           	animEl: 'mb3'
	       	});
			return;
		}	 
		this.fireEvent('fileselected', this, filename); 
	}
});

Ext.reg('fileuploadfield', Ext.ux.form.FileUploadField);

// backwards compat
Ext.form.FileUploadField = Ext.ux.form.FileUploadField;
