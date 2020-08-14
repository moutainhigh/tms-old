Ext.namespace("Ext.ux.layout");

Ext.ux.layout.TableFormLayout = Ext.extend(Ext.layout.TableLayout, {
    monitorResize: true,
    labelAutoWidth: true,
    packFields: false,
    trackLabels: Ext.layout.FormLayout.prototype.trackLabels,
    setContainer: function(ct) {
        Ext.layout.FormLayout.prototype.setContainer.apply(this, arguments);
        if (ct.labelAlign == 'top') {
            this.labelAutoWidth = false;
            if (this.fieldSpacing)
               this.elementStyle = 'padding-left: ' + this.fieldSpacing + 'px;';
        } else {
            if (this.labelAutoWidth)
                this.labelStyle = 'width: auto;';
            if (this.packFields && !ct.labelWidth)
                ct.labelWidth = 1;
        }
        if (this.fieldSpacing)
            this.labelStyle += 'padding-left: ' + this.fieldSpacing + 'px;';
        this.currentRow = 0;
        this.currentColumn = 0;
        this.cells = [];
    },
    renderItem : function(c, position, target) {
        if (c && !c.rendered) {
            var cell = Ext.get(this.getNextCell(c));
            cell.addClass("x-table-layout-column-" + this.currentColumn);
            if (c.anchor)
                c.width = 1;
            
            //FIXME XUQC 2013-3-18,支持表单域可以定义label的位置
            var hLabelStyle =this.labelStyle;
            var hElementStyle = this.elementStyle;
            if(c.labelAlign == 'top'){
                Ext.apply(this, {
                    labelStyle: 'width:auto;',
                    elementStyle: 'padding-left:0;'
                });
            }
            Ext.layout.FormLayout.prototype.renderItem.call(this, c, 0, cell);
            this.labelStyle = hLabelStyle;
            this.elementStyle = hElementStyle;
            delete hLabelStyle,hElementStyle;
        }
    },
    getLayoutTargetSize : Ext.layout.AnchorLayout.prototype.getLayoutTargetSize,
    parseAnchorRE : Ext.layout.AnchorLayout.prototype.parseAnchorRE,
    parseAnchor : Ext.layout.AnchorLayout.prototype.parseAnchor,
    getTemplateArgs : Ext.layout.FormLayout.prototype.getTemplateArgs,
    isValidParent : Ext.layout.FormLayout.prototype.isValidParent,
    onRemove : Ext.layout.FormLayout.prototype.onRemove,
    isHide : Ext.layout.FormLayout.prototype.isHide,
    onFieldShow : Ext.layout.FormLayout.prototype.onFieldShow,
    onFieldHide : Ext.layout.FormLayout.prototype.onFieldHide,
    adjustWidthAnchor : Ext.layout.FormLayout.prototype.adjustWidthAnchor,
    adjustHeightAnchor : Ext.layout.FormLayout.prototype.adjustHeightAnchor,
    getLabelStyle : Ext.layout.FormLayout.prototype.getLabelStyle,
    onLayout : function(ct, target) {
        Ext.ux.layout.TableFormLayout.superclass.onLayout.call(this, ct, target);
        if (!target.hasClass("x-table-form-layout-ct")) {
            target.addClass("x-table-form-layout-ct");
        }
        var viewSize = this.getLayoutTargetSize();
        if (this.fieldSpacing)
            viewSize.width -= this.fieldSpacing;
        var aw, ah;
        if (ct.anchorSize) {
            if (Ext.isNumber(ct.anchorSize)) {
                aw = ct.anchorSize;
            } else {
                aw = ct.anchorSize.width;
                ah = ct.anchorSize.height;
            }
        } else {
            aw = ct.initialConfig.width;
            ah = ct.initialConfig.height;
        }
        var cs = this.getRenderedItems(ct), len = cs.length, i, j, c;
        var x, col, columnWidthsPx, w;
        var ajustCols = [],ajustWidth = 55;//对于包括两个输入框的，比如日期查询条件
        // calculate label widths
        if (this.labelAutoWidth) {
            var labelWidths = new Array(this.columns);
            var pad = ct.labelPad || 5;
//            for (i = 0; i < this.columns; i++){
//                labelWidths[i] = ct.labelWidth || 0;
//            }
            // first pass: determine maximal label width for each column
            for (i = 0; i < len; i++) {
            	//初始化labelWidth，根据字符长度设置
                c = cs[i];
                // get table cell
                x = c.getEl().parent(".x-table-layout-cell");
                // get column
                col = parseInt(x.dom.className.replace(/.*x\-table\-layout\-column\-([\d]+).*/, "$1"));
                if(c.items && c.items.length == 2 && c.fieldLabel && !(c instanceof uft.extend.form.MultiSelectField)){
                	//这种包括两个输入框的设置为调整列，增加fieldLabel条件，否则查询和重置按钮也符合
                	var exist = false;
                	for(var j=0;j<ajustCols.length;j++){
                		if(ajustCols[j] == col){
                			//已经存在了，不要重复加入
                			exist = true;
                			break;
                		}
                	}
                	if(!exist){
                		ajustCols.push(col);
                	}
                }
                // set the label width
                if (c.fieldLabel){
                	if(c.fieldLabel.indexOf('<') == 0){//有些可能只是一些占位符
                		continue;
                	}
                	var oldWidth = labelWidths[col];
                	var newWidth = this._getLabelWidth(c.fieldLabel);
                	if(!oldWidth){
                		labelWidths[col] = newWidth;
                	}else{
                		//取大者
                		if(newWidth > oldWidth){
                			labelWidths[col] = newWidth;
                		}
                	}
                }
            }
            // second pass: set the label width
            for (i = 0; i < len; i++) {
                c = cs[i];
                // get table cell
                x = c.getEl().parent(".x-table-layout-cell");
                // get column
                col = parseInt(x.dom.className.replace(/.*x\-table\-layout\-column\-([\d]+).*/, "$1"));
                // get label
                if (c.label) {
                    // set the label width and the element padding
                    c.label.setWidth(labelWidths[col]);
                    var paddingLeft = labelWidths[col];
                    c.getEl().parent(".x-form-element").setStyle('paddingLeft',(paddingLeft + pad - 3) + 'px');
                }
            }
        }
        if (!this.packFields) {
            var rest = viewSize.width;
            columnWidthsPx = new Array(this.columns);
            // Calculate the widths in pixels
            //平均值
            var avgWidth = Math.floor(rest / this.columns);
            //优先分配调整列的宽
            for(var j=0;j<ajustCols.length;j++){
            	var w = avgWidth+ajustWidth;
            	columnWidthsPx[ajustCols[j]] = w;
            	rest -= w;
            }
            //重新计算平均值，为没有调整的列设置宽
            avgWidth = Math.floor(rest / (this.columns-ajustCols.length));
            for (j = 0; j < this.columns; j++) {
            	if(columnWidthsPx[j]){
            		//使用调整的方式设置过宽度了
            		continue;
            	}
                columnWidthsPx[j] = avgWidth;
                rest -= columnWidthsPx[j];
            }
            // 如果因为小数点计算的问题还有宽度剩余，那么假如最后一列中
            if (rest > 0)
                columnWidthsPx[this.columns - 1] += rest;
        }
        for (i = 0; i < len; i++) {
            c = cs[i];
            // get table cell
            x = c.getEl().parent(".x-table-layout-cell"); //代表一个单元格
            if(c.xtype == 'button'){
            	continue;
            }
            var xfe = c.getEl().parent(".x-form-element");
			if(c.xtype == 'imagefield'){
				//如果是图片展示域，则居中
				xfe.dom.align='center';
            } 
			if(c.xtype == 'identityField'){
				//如果是身份证展示域，则居中
				xfe.dom.align='center';
            }  
            if (!this.packFields) {
                // get column
                col = parseInt(x.dom.className.replace(/.*x\-table\-layout\-column\-([\d]+).*/, "$1"));
                // get cell width (based on column widths)
                c.colspan = c.colspan || 1
                for (j = col, w = 0; j < (col + c.colspan); j++){//col=0,col=1,col=2 
                	var t = j;
                	if(j>=columnWidthsPx.length){//保证数组不越界
                		t=j-columnWidthsPx.length;
                	}
                    w += columnWidthsPx[t];
                }
//                if(navigator.userAgent.toLowerCase().indexOf("360se") > -1 || Ext.isIE7){
//                	//360浏览器，fuck you!
//                	w = w-15;
//                }else{
	                //FIXME 当有多列合并时，加入10像素作为补充
	                if(c.colspan == 2){
	                	if(Ext.isGecko){
	                		w+=19;
	                	}else if(Ext.isChrome){
	                		w+=17;
	                	}else{
	                		w +=23;
	                	}
	                }
	                else if(c.colspan == 3){
	                	if(Ext.isGecko || Ext.isChrome){
	                		w +=33;
	                	}else{
	                		w +=35;
	                	}
	                }
	                else if(c.colspan == 4){
	                	if(Ext.isGecko){
	                		w +=43;
	                	}else if(Ext.isChrome){
	                		w += 43;
	                	}else{
		                	w +=47;
	                	}
	                }
//                }
                // set table cell width
                x.setWidth(w);
            }
            // perform anchoring
            if (c.anchor) {
                var a, h, cw, ch;
                if (this.packFields)
                    w = x.getWidth();
                // get cell width (subtract padding for label) & height to be base width of anchored component
                if(c.label){
	                this.labelAdjust = xfe.getPadding('l');
	                if (this.labelAdjust && ct.labelAlign == 'top')
	                    w -= this.labelAdjust;
                }
                h = x.getHeight();
                a = c.anchorSpec;
                if (!a) {
                    var vs = c.anchor.split(" ");
                    c.anchorSpec = a = {
                        right: this.parseAnchor(vs[0], c.initialConfig.width, aw),
                        bottom: this.parseAnchor(vs[1], c.initialConfig.height, ah)
                    };
                }
                cw = a.right ? this.adjustWidthAnchor(a.right(w), c) : undefined;
                ch = a.bottom ? this.adjustHeightAnchor(a.bottom(h), c) : undefined;
                if (cw || ch) {
                	if(c.autoEl){
                		//如果已经定义了宽和高，则使用这个宽和高
                		cw = c.autoEl.width || cw;
                		ch = c.autoEl.height || ch;
                	}
                    c.setSize(cw || undefined, ch || undefined);
                }
            }
        }
    },
    //得到label的长度，注意中文包括2个字节，长度是2
    _getLabelWidth : function(label){
    	var cArr = String(label).match(/[^\x00-\xff]/ig);
		var len=label.length + (cArr == null ? 0 : cArr.length);
		if(len <= 8){
			return 53;
		}else if(len <=10){
			return 63;
		}else{
			return 76;
		}
    }
});

Ext.Container.LAYOUTS["tableform"] = Ext.ux.layout.TableFormLayout;