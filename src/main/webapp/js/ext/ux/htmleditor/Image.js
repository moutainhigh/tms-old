Ext.namespace('Ext.ux.form.HtmlEditor');
/**
 * @author Shea Frederick - http://www.vinylfox.com
 * @class Ext.ux.form.HtmlEditor.Image
 * @extends Ext.util.Observable
 * <p>A plugin that creates an image button in the HtmlEditor toolbar for inserting an image. The method to select an image must be defined by overriding the selectImage method. Supports resizing of the image after insertion.</p>
 * <p>The selectImage implementation must call insertImage after the user has selected an image, passing it a simple image object like the one below.</p>
 * <pre>
 *      var img = {
 *         Width: 100,
 *         Height: 100,
 *         ID: 123,
 *         Title: 'My Image'
 *      };
 * </pre>
 */
Ext.ux.form.HtmlEditor.Image = Ext.extend(Ext.util.Observable, {
	// Image language text
	langTitle: '插入图片',
    urlSizeVars: ['width','height'],
    uploadPath: ctxPath + '/js/editor/uploadImage.do',
    init: function(cmp){
        this.cmp = cmp;
        this.cmp.on('render', this.onRender, this);
        this.cmp.on('initialize', this.onInit, this, {delay:100, single: true});
    },
    onEditorMouseUp : function(e){
        Ext.get(e.getTarget()).select('img').each(function(el){
            var w = el.getAttribute('width'), h = el.getAttribute('height'), src = el.getAttribute('src')+' ';
            src = src.replace(new RegExp(this.urlSizeVars[0]+'=[0-9]{1,5}([&| ])'), this.urlSizeVars[0]+'='+w+'$1');
            src = src.replace(new RegExp(this.urlSizeVars[1]+'=[0-9]{1,5}([&| ])'), this.urlSizeVars[1]+'='+h+'$1');
            el.set({src:src.replace(/\s+$/,"")});
        }, this);
        
    },
    onInit: function(){
        Ext.EventManager.on(this.cmp.getDoc(), {
			'mouseup': this.onEditorMouseUp,
			buffer: 100,
			scope: this
		});
    },
    onRender: function() {
        var btn = this.cmp.getToolbar().addButton({
            iconCls: 'x-edit-image',
            handler: this.selectImage,
            scope: this,
            tooltip: {
                title: this.langTitle
            },
            overflowText: this.langTitle
        });
    },
    //构建插入图片的窗口，包括网络图片和上传图片2个tab
    selectImage: function(){
		//网络图片选项卡  
	    var webImg=new Ext.form.FormPanel({  
	        title:'网络图片',  
	        anchor:'100%',  
	        autoHeight:true,  
	        labelWidth: 80,  
	        labelAlign: 'right',  
	        border: false,  
	        bodyStyle : 'padding-top:10px;',
	        defaults : {
	        	 anchor:'90%'
	        },
	        items:[{  
	            xtype:'uftnumberfield',
	            decimalPrecision : 0,
	            id:'wimgWidth',  
	            fieldLabel:'显示宽度',
	            name:'width'
	        },{  
	            xtype:'uftnumberfield',
	            decimalPrecision : 0,
	            id:'wimgHeight',  
	            fieldLabel:'显示高度',  
	            name:'height'
	        },{  
	            xtype:'textfield',  
	            id:'wimgUrl',  
	            fieldLabel:'图片网址',  
	            emptyText:'http://',  
	            allowBlank:false
	        }],  
	        buttons:[{  
	            text:'确定',
	            scope : this,
	            handler:function(){  
	                if(webImg.form.isValid()){  
	                    //确定插入网络图片  
	                    var img="<img src='"+Ext.getCmp('wimgUrl').getValue()+"'";  
	                    //高度不为空  
	                    if(""!=Ext.getCmp('wimgHeight').getValue()){  
	                        img+= " height="+Ext.getCmp('wimgHeight').getValue();  
	                    }  
	                    //宽度不为空  
	                    if(""!=Ext.getCmp('wimgWidth').getValue()){  
	                        img+= " width="+Ext.getCmp('wimgWidth').getValue()  
	                    }  
	                    img+= " />";  
	                    //插入图片  
	                    this.cmp.insertAtCursor(img);
	                    this.closeWin();
	                }  
	            }  
	        },{  
	            text:'取消',
	            scope : this,
	            handler:function(){   
	                this.closeWin();
	            }  
	        }]  
	    });  
	      
	    //上传图片选项卡  
	    var uploadImage=new Ext.form.FormPanel({
	        title:'本地上传',
	        fileUpload : true,
	        autoHeight:true,  
	        labelWidth: 80,  
	        labelAlign: 'right',  
	        border: false,
	        autoDestroy:false,  
	        bodyStyle : 'padding-top:10px;',
	        defaults : {
	        	 anchor:'90%'
	        },
	        items:[{  
	            xtype:'uftnumberfield',
	            decimalPrecision : 0,
	            id:'imgWidth',
	            fieldLabel:'显示宽度', 
	            name:'width' 
	        },{  
	            xtype:'uftnumberfield',
	            decimalPrecision : 0,
	            id:'imgHeight',  
	            fieldLabel:'显示高度',  
	            name:'height'
	        },{fieldLabel:'选择文件',
				name:'imgFile',
				xtype:'fileuploadfield',
				allowBlank:false,
				buttonText: '浏览...',
				permitted_extensions:['jpg','jpeg','png','gif']
			}],  
	        buttons:[{  
	            text:'确定',
	            formBind : true,
	            type : 'submit',
	            scope:this,
	            handler:function(){
	            	var form = uploadImage.getForm();
					if(form.isValid()){
						var params = form.getFieldValues(false);
	            		uploadImage.getForm().submit({
		            		url : this.uploadPath,
		            		params : params,
		            		method : 'POST',
		            		waitTitle: '请稍后',   
                            waitMsg: '正在上传 ...',
                            scope : this,   
	            			success:function(form,action){
		            			if(action && action.result) {
		            				var url = action.result.url;
			            			//确定插入上传的图片  
					                var img="<img src='"+url+"'";  
					                //高度不为空  
					                if(""!=Ext.getCmp('imgHeight').getValue()){  
					                    img+= " height="+Ext.getCmp('imgHeight').getValue();  
					                }  
					                //宽度不为空  
					                if(""!=Ext.getCmp('imgWidth').getValue()){  
					                    img+= " width="+Ext.getCmp('imgWidth').getValue()  
					                }  
					                img+= " />";  
					                //插入图片  
					                this.cmp.insertAtCursor(img);
					                this.closeWin();
			            		}
	            			},
	            			failure:function(form, action){
	            				var result = action.result;
		            			var msg = "出现未知错误！";
		            			if(result && result.msg){
									msg = result.msg;
			            		}
	            				uft.Utils.showErrorMsg(msg);
	            			}
	            		});
            		}           	
	            }  
	        },{  
	            text:'取消',
	            scope : this,
	            handler:function(){    
	                this.closeWin(); 
	            }  
	        }]  
	    });
		new Ext.Window({
			id : '_imageWin',
            title:'插入图片',  
            width:350,  
            height:180,  
            resizable:false,  
            items:[{  
                xtype:'tabpanel',  
                anchor:'100%',  
                border: false,  
                activeTab: 0,  
                items:[  
                    //网络图片  
                    webImg,  
                    //本地上传  
                    uploadImage  
                ]  
            }]  
        }).show(); 	    
    },
    closeWin : function(){
    	Ext.getCmp('_imageWin').close();
    }
});