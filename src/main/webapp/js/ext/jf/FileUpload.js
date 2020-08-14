Ext.namespace('uft.jf');
/**
 * 文件上传窗口,包括3个文件选择框
 */
uft.jf.FileUpload= function(config){
	var param = {};
	this.width = config.width||600;
	this.height = config.height||400;
	this.uploadUrl = config.uploadUrl||"uploadFile.do";
	this.inputNum = config.inputNum || 3; 
	//允许的文件类型
	this.permitted_extensions = config.permitted_extensions ||[];
	
	var itemAry = [];
	for(var i=0;i<this.inputNum;i++){
		itemAry.push({
            fieldLabel: '选择文件',
            name: 'userfile',
            xtype: 'fileuploadfield',
            permitted_extensions : this.permitted_extensions,
            anchor: '90%' 
        });
	}
	this.formPanel = new Ext.form.FormPanel({
        baseCls: 'x-plain',
        labelWidth: 80,
        url:this.uploadUrl,
        fileUpload:true,
        items: [itemAry]
    });
    
	uft.jf.FileUpload.superclass.constructor.call(this, {
        title: config.title||('文件上传,文件类型：'+this.permitted_extensions),
        width: config.width||400,
        height:config.height||200,
        layout: 'fit',
        plain:true,
        bodyStyle:'padding:5px;',
        buttonAlign:'center',
        modal : true,
        frame : true,
		closable : true,
		draggable : true,
		resizable : true,
        items: [this.formPanel],
        buttons: [{
            text: '上传',
            scope:this,
            handler: function() {
                if(this.checkForm()){
                    this.formPanel.getForm().submit({
                       method:"post",
                       waitMsg : '正在上传...',
                       scope:this,
                       success: function(form,action){
							if(action.result && action.result.msg){
								uft.Utils.showInfoMsg(action.result.msg);
							}
                        	this.fireEvent('fileupload',this);
                        	this.close();
                       },    
                       failure: function(form,action){
	                    	if(action.result && action.result.msg){
								uft.Utils.showErrorMsg(action.result.msg);
								return;
							}
                       }
                    });
                }
           }
        },{
            text: '关闭',
            scope:this,
            handler:function(){
            	this.close();
            }
        }]
    });
};
Ext.extend(uft.jf.FileUpload,Ext.Window, {
	/**
	 * 检测表单
	 */
	checkForm : function(){
		var values = this.formPanel.getForm().getFieldValues(false);
		if(!values){
			alert('请选择一个文件！');
			return false;
		}
		var count = 0;
		for(var key in values){
			count++;		
		}
		if(count == 0){
			alert('请选择一个文件！');
			return false;
		}
		return true;
	}
});