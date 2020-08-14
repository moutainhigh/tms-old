<script type="text/javascript">
	Ext.namespace("${moduleName}");
	if(!${moduleName + ".appUiConfig"}) {
		${moduleName + ".appUiConfig"} = {};
	}
	var S = ${moduleName + ".appUiConfig"};
	<@genHeaderGrid/>
	S.templateID='${templateID}';
	S.context = new uft.jf.Context();
	S.context.setTemplateID('${templateID}');
	S.context.setFunCode('${funCode}');
	S.context.setNodeKey('${nodeKey}');
	
	<#if '${headerHeight}' != '' && '${headerHeight}' != 'null'>
		S.headerHeight = parseInt('${headerHeight}');
	</#if>
	S.isDynReport = ('${isDynReport}' == 'true');
	S.btnArray = '${btnArray}';
	var lockingItemAry = '${lockingItemAry}';
	if( lockingItemAry != 'null'){
		var queryItems = Ext.decode(lockingItemAry);
		if(queryItems.length > 0){
			S.topQueryForm = new uft.jf.QueryFormPanel({queryItems:queryItems,grid:S.headerGrid,isDynReport:S.isDynReport,context : S.context});
		}
	}
	delete S;
</script>
<#macro genHeaderGrid>
	<#if fieldVOs?size!=0>
		var R=[],C=[];
		<#list fieldVOs as field>
			R.push(${field.genRecordType()});
		</#list>
		<#list fieldVOs as field>
			C.push(${field.genListColumn()});
		</#list>
		S.hp = {};
		S.hp['templateID'] = '${templateID}';
		S.hp['funCode']='${funCode}';
		S.hp['nodeKey']='${nodeKey}';
		var aParams = document.location.search.substr(1).split('&');
		for (i = 0; i < aParams.length; i++) {
			var aParam = aParams[i].split('=');
			S.hp[aParam[0]]=aParam[1];
		}
		S.headerGridConfig = {
			dataUrl : '${headerGridDataUrl}',
			params : Ext.apply({},S.hp),
			isAddBbar : ${headerGridPagination},
			immediatelyLoad : ${headerGridImmediatelyLoad},
			sortable : ${headerGridSortable},
			isAddPageSizePlugin : ${headerGridPageSizePlugin},
			isCheckboxSelectionModel : ${headerGridCheckboxSelectionModel},
			singleSelect : ${headerGridSingleSelect},
			plugins : ${headerGridPlugins},
			recordType : R,
			columns :  C
		};
		if('${headerGridPageSize}' != 'null'){
			S.headerGridConfig['pageSize'] = ${headerGridPageSize};
		}
		S.headerGrid = new uft.extend.grid.ReportGrid(S.headerGridConfig);
		delete hp,R,C;
	</#if>
</#macro>

