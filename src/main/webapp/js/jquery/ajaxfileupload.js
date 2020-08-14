$.extend({
	J_UPLOAD_FRAME_PREFIX : 'jUploadFrame', //iframe��idǰ׺
	J_UPLOAD_FORM_PREFIX : 'jUploadForm', //�ϴ��?��idǰ׺
	J_UPLOAD_FILE_PREFIX : 'jUploadFile', //�ϴ����idǰ׺
    createUploadIframe: function(id, uri){
		//create frame
		var frameId = this.J_UPLOAD_FRAME_PREFIX + id;
		var io;
		if(window.ActiveXObject) {
			if($.browser.version=="9.0"){
				io = document.createElement('iframe');
				io.id = frameId;
				io.name = frameId;
			}else if($.browser.version=="6.0" || $.browser.version=="7.0" || $.browser.version=="8.0"){
				 io = document.createElement('<iframe id="' + frameId + '" name="' + frameId + '" />');
				 if(typeof uri== 'boolean'){
					 io.src = 'javascript:false';
				 }
				 else if(typeof uri== 'string'){
					 io.src = uri;
				 }
			}
		}
		else {
			io = document.createElement('iframe');
			io.id = frameId;
			io.name = frameId;
		}
		io.style.position = 'absolute';
		io.style.top = '-1000px';
		io.style.left = '-1000px';

		document.body.appendChild(io);

		return io;	
    },
    //��IE�� file input��valueû�����ƣ������޷���newElement����upload form�У��ȷ��͵�ʱ��϶��Ƿ���oldElement
    //�����ֳ���һ�����⣬newElement��Ϊ��copy�ģ���ôԭ������oldElement�е��¼��޷����ơ�
    //ʹ��referTarget�����һ��Ext������������������°��¼�
    createUploadForm: function(id, fileElementId,referTarget){
		//create form	
		var formId = this.J_UPLOAD_FORM_PREFIX + id;
		var fileId = this.J_UPLOAD_FILE_PREFIX + id;
		var form = $('<form  action="" method="POST" name="' + formId + '" id="' + formId + '" enctype="multipart/form-data"></form>');	
		var oldElement = $('#' + fileElementId);
		var newElement = $(oldElement).clone();
		$(oldElement).attr('id', fileId);
		$(oldElement).before(newElement);
		$(oldElement).appendTo(form);
		//set attributes
		$(form).css('position', 'absolute');
		$(form).css('top', '-1200px');
		$(form).css('left', '-1200px');
		$(form).appendTo('body');	
		if(referTarget){
			referTarget.bindListeners(Ext.get(fileElementId));
		}		
		return form;
    },
	addOtherRequestsToForm: function(form,data){
		// add extra parameter
		var originalElement = $('<input type="hidden" name="" value="">');
		for(var key in data){
			name = key;
			value = data[key];
			var cloneElement = originalElement.clone();
			cloneElement.attr({'name':name,'value':value});
			$(cloneElement).appendTo(form);
		}
		delete originalElement;
		return form;
	},

    ajaxFileUpload: function(s) {
        // TODO introduce global settings, allowing the client to modify them for all requests, not only timeout		
        s = $.extend({}, $.ajaxSettings, s);
        var id = 123;
        var frameId = this.J_UPLOAD_FRAME_PREFIX + id;
		var formId = this.J_UPLOAD_FORM_PREFIX + id;	
		var form = $.createUploadForm(id, s.fileElementId,s.referTarget);
		if(s.data){
			form = $.addOtherRequestsToForm(form,s.data);
		}
		var io = $.createUploadIframe(id, s.secureuri);
			
        // Watch for a new set of requests
        if(s.global && ! $.active++){
			$.event.trigger("ajaxStart");
		}            
        var requestDone = false;
        // Create the request object
        var xml = {}   
        if(s.global){
            $.event.trigger("ajaxSend", [xml, s]);
        }
        // Wait for a response to come back
        var uploadCallback = function(isTimeout){			
			var io = document.getElementById(frameId);
            try{				
				if(io.contentWindow){
					xml.responseText = io.contentWindow.document.body?io.contentWindow.document.body.innerHTML:null;
                	xml.responseXML = io.contentWindow.document.XMLDocument?io.contentWindow.document.XMLDocument:io.contentWindow.document;
				}else if(io.contentDocument){
					xml.responseText = io.contentDocument.document.body?io.contentDocument.document.body.innerHTML:null;
                	xml.responseXML = io.contentDocument.document.XMLDocument?io.contentDocument.document.XMLDocument:io.contentDocument.document;
				}						
            }catch(e){
				$.handleError(s, xml, null, e);
			}
            if(xml || isTimeout == "timeout"){				
                requestDone = true;
                var status;
                try{
                    status = isTimeout != "timeout" ? "success" : "error";
                    // Make sure that the request was successful or notmodified
                    if(status != "error"){
                        // process the data (runs the xml through httpData regardless of callback)
                        var data = $.uploadHttpData(xml, s.dataType);    
                        // If a local callback was specified, fire it and pass it the data
                        if(s.success)
                            s.success( data, status );
                        // Fire the global callback
                        if(s.global)
                            $.event.trigger("ajaxSuccess", [xml, s]);
                    } else{
                        $.handleError(s, xml, status);
                    }
                }catch(e){
                    status = "error";
                    $.handleError(s, xml, status, e);
                }

                // The request was completed
                if(s.global){
                    $.event.trigger("ajaxComplete", [xml, s]);
                }
                // Handle the global AJAX counter
                if(s.global && ! --$.active){
                    $.event.trigger("ajaxStop");
                }
                // Process result
                if(s.complete){
                    s.complete(xml, status);
                }
                $(io).unbind()

                setTimeout(function(){	
                				try{
									$(io).remove();
									$(form).remove();	
								} catch(e){
									$.handleError(s, xml, null, e);
								}}, 100);
                xml = null;
            }
        }
        // Timeout checker
        if(s.timeout > 0){
            setTimeout(function(){
                // Check to see if the request is still happening
                if(!requestDone){
                	uploadCallback("timeout");
                }
            }, s.timeout);
        }
        try{
           // var io = $('#' + frameId);
			var form = $('#' + formId);
			$(form).attr('action', s.url);
			$(form).attr('method', 'POST');
			$(form).attr('target', frameId);
            if(form.encoding){
                form.encoding = 'multipart/form-data';				
            }else{				
                form.enctype = 'multipart/form-data';
            }			
            $(form).submit();
        } catch(e){			
            $.handleError(s, xml, null, e);
        }
        if(window.attachEvent){
            document.getElementById(frameId).attachEvent('onload', uploadCallback);
        }else{
            document.getElementById(frameId).addEventListener('load', uploadCallback, false);
        } 		
        return {abort: function () {}};	
    },

    uploadHttpData: function(r, type){
        var data = !type;
        data = type == "xml" || data ? r.responseXML : r.responseText;
        // If the type is "script", eval it in global context
        if(type == "script")
            $.globalEval( data );
        // Get the JavaScript object, if JSON is used.
        if(type == "json"){
//            data = $.parseJSON(jQuery(data).text());
        	eval( "data = " + data );
        }
        // evaluate scripts within html
        if(type == "html"){
            $("<div>").html(data).evalScripts();
        }
		//alert($('param', data).each(function(){alert($(this).attr('value'));}));
        return data;
    }
})

