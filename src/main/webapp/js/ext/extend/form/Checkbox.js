/*!
 * Ext JS Library 3.3.0
 * Copyright(c) 2006-2010 Ext JS, Inc.
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/**
 * Ext默认的Checkbox不能处理inputValue==0的情况，因为js中“0”==false，导致不能取消选择
 * 与Ext.form.Checkbox的不同是，选中值为true，否则为false,
 * 当value='Y'时，也选中。这是为了兼容nc中有些boolean值是使用Y存储
 * @class uft.extend.form.Checkbox
 * @extends Ext.form.Field
 * Single checkbox field.  Can be used as a direct replacement for traditional checkbox fields.
 * @constructor
 * Creates a new Checkbox
 * @param {Object} config Configuration options
 * @xtype checkbox
 */
uft.extend.form.Checkbox = Ext.extend(Ext.form.Field,  {
    /**
     * @cfg {String} focusClass The CSS class to use when the checkbox receives focus (defaults to undefined)
     */
    focusClass : undefined,
    /**
     * @cfg {String} fieldClass The default CSS class for the checkbox (defaults to 'x-form-field')
     */
    fieldClass : 'x-form-field',
    /**
     * @cfg {Boolean} checked <tt>true</tt> if the checkbox should render initially checked (defaults to <tt>false</tt>)
     */
    checked : false,
    /**
     * @cfg {String} boxLabel The text that appears beside the checkbox
     */
    boxLabel: '&#160;',
    /**
     * @cfg {String/Object} autoCreate A DomHelper element spec, or true for a default element spec (defaults to
     * {tag: 'input', type: 'checkbox', autocomplete: 'off'})
     */
    defaultAutoCreate : { tag: 'input', type: 'checkbox', autocomplete: 'off'},
    /**
     * @cfg {String} boxLabel The text that appears beside the checkbox
     */
    /**
     * @cfg {String} inputValue The value that should go into the generated input element's value attribute
     */
    /**
     * @cfg {Function} handler A function called when the {@link #checked} value changes (can be used instead of
     * handling the check event). The handler is passed the following parameters:
     * <div class="mdetail-params"><ul>
     * <li><b>checkbox</b> : Ext.form.Checkbox<div class="sub-desc">The Checkbox being toggled.</div></li>
     * <li><b>checked</b> : Boolean<div class="sub-desc">The new checked state of the checkbox.</div></li>
     * </ul></div>
     */
    /**
     * @cfg {Object} scope An object to use as the scope ('this' reference) of the {@link #handler} function
     * (defaults to this Checkbox).
     */

    // private
    actionMode : 'wrap',

	// private
    initComponent : function(){
        uft.extend.form.Checkbox.superclass.initComponent.call(this);
        this.addEvents(
            /**
             * @event check
             * Fires when the checkbox is checked or unchecked.
             * @param {Ext.form.Checkbox} this This checkbox
             * @param {Boolean} checked The new checked value
             */
            'check'
        );
    },

    // private
    onResize : function(){
        uft.extend.form.Checkbox.superclass.onResize.apply(this, arguments);
        if(!this.boxLabel && !this.fieldLabel){
            this.el.alignTo(this.wrap, 'c-c');
        }
    },

    // private
    initEvents : function(){
        uft.extend.form.Checkbox.superclass.initEvents.call(this);
        this.mon(this.el, {
            scope: this,
            click: this.onClick,
            change: this.onClick
        });
    },

    /**
     * @hide
     * Overridden and disabled. The editor element does not support standard valid/invalid marking.
     * @method
     */
    markInvalid : Ext.emptyFn,
    /**
     * @hide
     * Overridden and disabled. The editor element does not support standard valid/invalid marking.
     * @method
     */
    clearInvalid : Ext.emptyFn,

    // private
    onRender : function(ct, position){
        uft.extend.form.Checkbox.superclass.onRender.call(this, ct, position);
        if(this.inputValue !== undefined){
            this.el.dom.value = this.inputValue;
        }
        this.wrap = this.el.wrap({cls: 'x-form-check-wrap'});
        if(this.boxLabel){
            this.wrap.createChild({tag: 'label', htmlFor: this.el.id, cls: 'x-form-cb-label', html: this.boxLabel});
        }
        if(this.checked){
            this.setValue(true);
        }else{
            this.checked = this.el.dom.checked;
        }
        // Need to repaint for IE, otherwise positioning is broken
        if (Ext.isIE && !Ext.isStrict) {
            this.wrap.repaint();
        }
        this.resizeEl = this.positionEl = this.wrap;
    },

    // private
    onDestroy : function(){
        Ext.destroy(this.wrap);
        uft.extend.form.Checkbox.superclass.onDestroy.call(this);
    },

    // private
    initValue : function() {
        this.originalValue = this.getValue();
    },

    /**
     * Returns the checked state of the checkbox.
     * @return {Boolean} True if checked, else false
     */
    getValue : function(){
        if(this.rendered){
            return this.el.dom.checked?"Y":"N";
        }
        return this.checked?"Y":"N";
    },
    setReadOnly : function(readOnly){
    	uft.extend.form.Checkbox.superclass.setReadOnly.call(this,readOnly);
        if(this.rendered){
        	if(readOnly === true){
        		if(this.label){
        			this.label.parent().addClass('uft-form-label-not-edit');
        		}
	            this.el.dom.onclick = function(){
	            	return false;
	            };
        	}else{
        		if(this.label){
        			this.label.parent().removeClass('uft-form-label-not-edit');
        		}
        		this.el.dom.onclick = null;
        	}
        }
    },    

	// private
    onClick : function(){
    	//如果是只读的或者不可用的，则直接返回
    	if(this.readOnly || this.disabled){
    		return;
    	}
        if(this.el.dom.checked != this.checked){
            this.setValue(this.el.dom.checked);
        }
    },

    /**
     * Sets the checked state of the checkbox, fires the 'check' event, and calls a
     * <code>{@link #handler}</code> (if configured).
     * @param {Boolean/String} checked The following values will check the checkbox:
     * <code>true, 'true', '1', or 'on'</code>. Any other value will uncheck the checkbox.
     * @return {Ext.form.Field} this
     */
    setValue : function(v){
        var checked = this.checked,
            inputVal = this.inputValue;
            
        //数据库中可能存储的是N&Y
        this.checked = (v === true || v === 'true' || v=='Y' || v==1 );
        if(this.rendered){
            this.el.dom.checked = this.checked;
            this.el.dom.defaultChecked = this.checked;
        }
        if(checked != this.checked){
            this.fireEvent('check', this, this.checked?"Y":"N");
            if(this.handler){
                this.handler.call(this.scope || this, this, this.checked?"Y":"N");
            }
        }
        return this;
    }
});
Ext.reg('uftcheckbox', uft.extend.form.Checkbox);
