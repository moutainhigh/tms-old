Ext.namespace("uft.te");
uft.te.NodeArrivalWindow = Ext
		.extend(
				Ext.Window,
				{
					currentRecord : null,
					pk_entrust : null,
					DATETIME_FORMAT : "Y-m-d H:i:s",
					disabledRowClass : "x-grid3-row-selected-disable",
					constructor : function(b) {
						Ext.apply(this, b);
						com_tms_jf_ts_ent_line_b_recordType = [];
						com_tms_jf_ts_ent_line_b_columns = [];
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_address",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "addr_code",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "addr_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_city",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "city_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_province",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "province_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_area",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "area_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "detail_addr",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "act_arri_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "act_leav_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "memo",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "req_arri_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "req_leav_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "dr",
							type : "int"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_ent_line_b",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_entrust",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "serialno",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "ts",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "dr",
							type : "int"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "ts",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "addr_flag",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_segment",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "arrival_flag",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "segment_node",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>地址</span>",
									width : 100,
									dataIndex : "pk_address",
									xtype : "gridcolumn",
									hidden : true,
									editable : false,
									editor : {
										xtype : "textfield",
										maxLength : 50
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-null'>实际到达时间</span>",
									width : 120,
									dataIndex : "act_arri_date",
									xtype : "datetimecolumn",
									editor : {
										xtype : "datetimefield",
										allowBlank : false,
										maxLength : 200
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column'>实际离开时间</span>",
									width : 120,
									dataIndex : "act_leav_date",
									xtype : "datetimecolumn",
									editor : {
										xtype : "datetimefield",
										maxLength : 200
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-null'>要求到达时间</span>",
									width : 120,
									dataIndex : "req_arri_date",
									xtype : "datetimecolumn",
									hidden : true
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-null'>要求离开时间</span>",
									width : 120,
									dataIndex : "req_leav_date",
									xtype : "datetimecolumn",
									hidden : true
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column'>备注</span>",
									width : 200,
									dataIndex : "memo",
									xtype : "gridcolumn",
									editor : {
										xtype : "textfield",
										maxLength : 200
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column'>地址编码</span>",
									width : 100,
									dataIndex : "addr_code",
									xtype : "refcolumn"
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>地址名称</span>",
									width : 100,
									dataIndex : "addr_name",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>城市</span>",
									width : 100,
									dataIndex : "pk_city",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>城市</span>",
									width : 100,
									dataIndex : "city_name",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>省份</span>",
									width : 100,
									dataIndex : "pk_province",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>省份</span>",
									width : 100,
									dataIndex : "province_name",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>区域</span>",
									width : 100,
									dataIndex : "pk_area",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>区域</span>",
									width : 100,
									dataIndex : "area_name",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>详细地址</span>",
									width : 200,
									dataIndex : "detail_addr",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'><center>删除标识</center></span>",
									width : 100,
									dataIndex : "dr",
									xtype : "gridcolumn",
									hidden : true,
									editable : false,
									align : "right"
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>pk_ent_line_b</span>",
									width : 100,
									dataIndex : "pk_ent_line_b",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>委托单</span>",
									width : 100,
									dataIndex : "pk_entrust",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>序号</span>",
									width : 100,
									dataIndex : "serialno",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>更新时间戳</span>",
									width : 100,
									dataIndex : "ts",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'><center>dr</center></span>",
									width : 0,
									dataIndex : "dr",
									xtype : "gridcolumn",
									hidden : true,
									editable : false,
									align : "right"
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>ts</span>",
									width : 0,
									dataIndex : "ts",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>addr_flag</span>",
									width : 0,
									dataIndex : "addr_flag",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>pk_segment</span>",
									width : 0,
									dataIndex : "pk_segment",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>arrival_flag</span>",
									width : 100,
									dataIndex : "arrival_flag",
									xtype : "checkcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>是否系统节点</span>",
									width : 100,
									dataIndex : "segment_node",
									xtype : "checkcolumn",
									editable : false
								});
						var a = new uft.extend.grid.EditorGrid({
							id : "ts_ent_line_b_1",
							pkFieldName : "pk_ent_line_b",
							dataUrl : "loadEntLineB.json",
							params : {
								pk_entrust : this.pk_entrust
							},
							border : true,
							isCheckboxSelectionModel : true,
							sm : new Ext.grid.RowSelectionModel(),
							singleSelect : false,
							isAddBbar : false,
							immediatelyLoad : true,
							onlyValidateSelected : true,
							plugins : [ new Ext.ux.plugins.GridValidator() ],
							recordType : com_tms_jf_ts_ent_line_b_recordType,
							columns : com_tms_jf_ts_ent_line_b_columns
						});
						a.on({
							afteredit : function(c) {
								if (c.field == "act_arri_date") {
									this
											.afterEditAct_arri_date(c.record,
													c.row);
								} else {
									if (c.field == "act_leav_date") {
										this.afterEditAct_leav_date(c.record,
												c.row);
									}
								}
							},
							beforeedit : function(c) {
								var d = c.record.get("arrival_flag");
								if (String(d) == "true" || String(d) == "Y") {
									return false;
								}
							},
							scope : this
						});
						a.getStore().on(
								{
									load : function(e, d) {
										var c = d.length;
										for ( var f = 0; f < c; f++) {
											var g = d[f];
											var j = g.get("arrival_flag");
											if (String(j) == "true"
													|| String(j) == "Y") {
												a.getView().addRowClass(f,
														this.disabledRowClass);
											}
											var h = g.get("act_arri_date");
											if (Utils.isBlank(h)) {
												g.set("act_arri_date", g
														.get("req_arri_date"));
											}
										}
									},
									scope : this
								});
						uft.te.NodeArrivalWindow.superclass.constructor.call(
								this, {
									title : this.title || "节点到货",
									width : 850,
									height : 450,
									collapsible : false,
									frame : true,
									closable : true,
									draggable : true,
									resizable : true,
									modal : true,
									border : false,
									layout : "fit",
									items : [ a ],
									buttons : [ {
										xtype : "button",
										text : "确认",
										iconCls : "btnYes",
										scope : this,
										handler : this.confirmArrival
									}, {
										xtype : "button",
										text : "反确认",
										iconCls : "btnCancel",
										scope : this,
										handler : this.unconfirmArrival
									}, new Ext.Button({
										iconCls : "btnCancel",
										text : "关&nbsp;&nbsp;闭",
										scope : this,
										handler : function() {
											this.close();
										}
									}) ]
								});
					},
					afterEditAct_leav_date : function(a, b) {
					},
					afterEditAct_arri_date : function(a, b) {
					},
					confirmArrival : function() {
						var a = Ext.getCmp("ts_ent_line_b_1");
						a.stopEditing();
						var f = uft.Utils.getSelectedRecords(a);
						if (!f) {
							uft.Utils.showWarnMsg("请先选择记录！");
							return;
						}
						if (!a.isValid()) {
							errors = a.getAllErrors();
							uft.Utils.showWarnMsg(errors);
							return;
						}
						var m = f.length, e = a.getStore();
						var l = [];
						for ( var h = 0; h < e.getCount(); h++) {
							var k = e.getAt(h);
							var b = null;
							if ((h - 1) >= 0) {
								b = e.getAt(h - 1);
							}
							var q = false, d = false;
							for ( var g = 0; g < m; g++) {
								if (k.id == f[g].id) {
									q = true;
								}
								if (b) {
									if (f[g].id == b.id) {
										d = true;
									}
								}
							}
							if (!q) {
								continue;
							} else {
								if (b && !d) {
									var c = b.get("arrival_flag");
									if (String(c) != "true" && String(c) != "Y") {
										uft.Utils.showWarnMsg("请先确认上一个节点！");
										return;
									}
								}
							}
							var p = k.get("act_arri_date");
							if (p instanceof Date) {
								k.set("act_arri_date", p
										.dateFormat(this.DATETIME_FORMAT));
							}
							var n = k.get("act_leav_date");
							if (n) {
								if (n instanceof Date) {
									k.set("act_leav_date", n
											.dateFormat(this.DATETIME_FORMAT));
								}
							}
							if (b) {
								var o = b.get("act_arri_date");
								if (o) {
									if (typeof (o) == "string") {
										o = Date.parseDate(o,
												this.DATETIME_FORMAT);
									}
									var p = k.get("act_arri_date");
									if (typeof (p) == "string") {
										p = Date.parseDate(p,
												this.DATETIME_FORMAT);
									}
									if (p < o) {
										uft.Utils
												.showWarnMsg("第"
														+ (h + 1)
														+ "行的实际到达时间必须大于等于上一个节点的实际到达时间！");
										return;
									}
								}
							}
							l.push(k.data);
						}
						uft.Utils
								.doAjax({
									scope : this,
									params : {
										APP_POST_DATA : Ext.encode(l)
									},
									url : ctxPath
											+ "/te/tracking/confirmArrival.json",
									success : function(s) {
										if (s && s.datas) {
											var j = s.datas;
											for ( var u = 0; u < j.length; u++) {
												var w = j[u];
												var t = w[0];
												var v = this.currentRecord;
												v.beginEdit();
												v.set("vbillstatus",
														t.vbillstatus);
												v.set("act_deli_date",
														t.act_deli_date);
												v.set("act_arri_date",
														t.act_arri_date);
												v.endEdit();
											}
											a.getStore().reload();
										}
									}
								});
					},
					unconfirmArrival : function() {
						var a = Ext.getCmp("ts_ent_line_b_1");
						var d = uft.Utils.getSelectedRecords(a);
						if (!d) {
							uft.Utils.showWarnMsg("请先选择记录！");
							return;
						}
						var k = d.length, c = a.getStore();
						var h = [];
						for ( var f = 0; f < c.getCount(); f++) {
							var g = c.getAt(f);
							var p = null;
							if ((f + 1) < c.getCount()) {
								p = c.getAt(f + 1);
							}
							var o = false, m = false;
							for ( var e = 0; e < k; e++) {
								if (g.id == d[e].id) {
									o = true;
								}
								if (p) {
									if (d[e].id == p.id) {
										m = true;
									}
								}
							}
							if (!o) {
								continue;
							} else {
								if (p && !m) {
									var b = p.get("arrival_flag");
									if (String(b) == "true" || String(b) == "Y") {
										uft.Utils.showWarnMsg("请先反确认下一个节点！");
										return;
									}
								}
							}
							var n = g.get("act_arri_date");
							if (n instanceof Date) {
								g.set("act_arri_date", n
										.dateFormat(this.DATETIME_FORMAT));
							}
							var l = g.get("act_leav_date");
							if (l) {
								if (l instanceof Date) {
									g.set("act_leav_date", l
											.dateFormat(this.DATETIME_FORMAT));
								}
							}
							h.push(g.data);
						}
						h = h.reverse();
						uft.Utils
								.doAjax({
									scope : this,
									params : {
										APP_POST_DATA : Ext.encode(h)
									},
									url : ctxPath
											+ "/te/tracking/unconfirmArrival.json",
									success : function(q) {
										if (q && q.datas) {
											var j = q.datas;
											for ( var t = 0; t < j.length; t++) {
												var v = j[t];
												var s = v[0];
												var u = this.currentRecord;
												u.beginEdit();
												u.set("vbillstatus",
														s.vbillstatus);
												u.set("act_deli_date",
														s.act_deli_date);
												u.set("act_arri_date",
														s.act_arri_date);
												u.endEdit();
											}
											a.getStore().reload();
										}
									}
								});
					}
				});
Ext.namespace("uft.te");
uft.te.BatchNodeArrivalWindow = Ext
		.extend(
				Ext.Window,
				{
					currentRecords : null,
					DATETIME_FORMAT : "Y-m-d H:i:s",
					disabledRowClass : "x-grid3-row-selected-disable",
					constructor : function(a) {
						Ext.apply(this, a);
						this.form = new uft.extend.form.FormPanel({
							border : true,
							autoScroll : true,
							padding : "5px 0 0 0",
							items : this.buildFieldset()
						});
						uft.te.BatchNodeArrivalWindow.superclass.constructor
								.call(this, {
									title : this.title || "批量节点到货",
									width : 850,
									height : 460,
									collapsible : false,
									frame : true,
									closable : true,
									draggable : true,
									resizable : true,
									modal : true,
									border : false,
									layout : "fit",
									items : [ this.form ],
									buttons : [ {
										xtype : "button",
										text : "全部确认",
										iconCls : "btnYes",
										scope : this,
										handler : this.confirmArrival
									}, {
										xtype : "button",
										text : "全部反确认",
										iconCls : "btnCancel",
										scope : this,
										handler : this.unconfirmArrival
									}, {
										xtype : "button",
										iconCls : "btnCancel",
										text : "关&nbsp;&nbsp;闭",
										scope : this,
										handler : function() {
											this.close();
										}
									} ]
								});
					},
					buildFieldset : function() {
						var d = [];
						var f = this.currentRecords;
						for ( var e = 0; e < f.length; e++) {
							var c = f[e], b = c.get("vbillno");
							var a = {
								height : 185,
								xtype : "fieldset",
								collapsible : true,
								title : "对单据" + b + "节点到货",
								layout : "fit",
								padding : "5px 5px 0",
								defaults : {
									anchor : "95%"
								},
								items : [ this.buildGrid(c) ]
							};
							d.push(a);
						}
						return d;
					},
					buildGrid : function(e) {
						var a = e.get("vbillno"), d = e.get("pk_entrust");
						var b = "ts_ent_line_b_" + a;
						com_tms_jf_ts_ent_line_b_recordType = [];
						com_tms_jf_ts_ent_line_b_columns = [];
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_address",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "addr_code",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "addr_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_city",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "city_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_province",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "province_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_area",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "area_name",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "detail_addr",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "act_arri_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "act_leav_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "memo",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "req_arri_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "req_leav_date",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "dr",
							type : "int"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_ent_line_b",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_entrust",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "serialno",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "ts",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "dr",
							type : "int"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "ts",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "addr_flag",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "pk_segment",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "arrival_flag",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_recordType.push({
							name : "segment_node",
							type : "string"
						});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>地址</span>",
									width : 100,
									dataIndex : "pk_address",
									xtype : "gridcolumn",
									hidden : true,
									editable : false,
									editor : {
										xtype : "textfield",
										maxLength : 50
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-null'>实际到达时间</span>",
									width : 110,
									dataIndex : "act_arri_date",
									xtype : "datetimecolumn",
									editor : {
										xtype : "datetimefield",
										allowBlank : false,
										maxLength : 200
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column'>实际离开时间</span>",
									width : 110,
									dataIndex : "act_leav_date",
									xtype : "datetimecolumn",
									editor : {
										xtype : "datetimefield",
										maxLength : 200
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-null'>要求到达时间</span>",
									width : 120,
									dataIndex : "req_arri_date",
									xtype : "datetimecolumn",
									hidden : true
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-null'>要求离开时间</span>",
									width : 120,
									dataIndex : "req_leav_date",
									xtype : "datetimecolumn",
									hidden : true
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column'>备注</span>",
									width : 120,
									dataIndex : "memo",
									xtype : "gridcolumn",
									editor : {
										xtype : "textfield",
										maxLength : 200
									}
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column'>地址编码</span>",
									width : 60,
									dataIndex : "addr_code",
									xtype : "refcolumn"
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>地址名称</span>",
									width : 100,
									dataIndex : "addr_name",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>城市</span>",
									width : 80,
									dataIndex : "pk_city",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>城市</span>",
									width : 80,
									dataIndex : "city_name",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>省份</span>",
									width : 60,
									dataIndex : "pk_province",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>省份</span>",
									width : 60,
									dataIndex : "province_name",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>区域</span>",
									width : 60,
									dataIndex : "pk_area",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>区域</span>",
									width : 60,
									dataIndex : "area_name",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>详细地址</span>",
									width : 170,
									dataIndex : "detail_addr",
									xtype : "gridcolumn",
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'><center>删除标识</center></span>",
									width : 100,
									dataIndex : "dr",
									xtype : "gridcolumn",
									hidden : true,
									editable : false,
									align : "right"
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>pk_ent_line_b</span>",
									width : 100,
									dataIndex : "pk_ent_line_b",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>委托单</span>",
									width : 100,
									dataIndex : "pk_entrust",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>序号</span>",
									width : 100,
									dataIndex : "serialno",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>更新时间戳</span>",
									width : 100,
									dataIndex : "ts",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'><center>dr</center></span>",
									width : 0,
									dataIndex : "dr",
									xtype : "gridcolumn",
									hidden : true,
									editable : false,
									align : "right"
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>ts</span>",
									width : 0,
									dataIndex : "ts",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>addr_flag</span>",
									width : 0,
									dataIndex : "addr_flag",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>pk_segment</span>",
									width : 0,
									dataIndex : "pk_segment",
									xtype : "gridcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>arrival_flag</span>",
									width : 100,
									dataIndex : "arrival_flag",
									xtype : "checkcolumn",
									hidden : true,
									editable : false
								});
						com_tms_jf_ts_ent_line_b_columns
								.push({
									header : "<span class='uft-grid-header-column-not-edit'>是否系统节点</span>",
									width : 100,
									dataIndex : "segment_node",
									xtype : "checkcolumn",
									editable : false
								});
						var c = new uft.extend.grid.EditorGrid({
							id : b,
							border : true,
							pkFieldName : "pk_ent_line_b",
							dataUrl : "loadEntLineB.json",
							params : {
								pk_entrust : d
							},
							isCheckboxSelectionModel : false,
							sm : new Ext.grid.RowSelectionModel(),
							singleSelect : false,
							isAddBbar : false,
							immediatelyLoad : true,
							plugins : [ new Ext.ux.plugins.GridValidator() ],
							recordType : com_tms_jf_ts_ent_line_b_recordType,
							columns : com_tms_jf_ts_ent_line_b_columns
						});
						c.on({
							afteredit : function(f) {
								if (f.field == "act_arri_date") {
									this
											.afterEditAct_arri_date(f.record,
													f.row);
								} else {
									if (f.field == "act_leav_date") {
										this.afterEditAct_leav_date(f.record,
												f.row);
									}
								}
							},
							beforeedit : function(f) {
								var g = f.record.get("arrival_flag");
								if (String(g) == "true" || String(g) == "Y") {
									return false;
								}
							},
							scope : this
						});
						c.getStore().on(
								{
									load : function(h, g) {
										var f = g.length;
										for ( var j = 0; j < f; j++) {
											var k = g[j];
											var m = k.get("arrival_flag");
											if (String(m) == "true"
													|| String(m) == "Y") {
												c.getView().addRowClass(j,
														this.disabledRowClass);
											}
											var l = k.get("act_arri_date");
											if (Utils.isBlank(l)) {
												k.set("act_arri_date", k
														.get("req_arri_date"));
											}
										}
									},
									scope : this
								});
						return c;
					},
					afterEditAct_leav_date : function(a, b) {
					},
					afterEditAct_arri_date : function(a, b) {
					},
					getAllRecords : function(d) {
						var b = [], e = d.getStore(), a = e.getCount();
						for ( var c = 0; c < a; c++) {
							b.push(e.getAt(c));
						}
						return b;
					},
					confirmArrival : function() {
						var f = this.currentRecords;
						var m = [];
						for ( var h = 0; h < f.length; h++) {
							var r = f[h], g = r.get("vbillno");
							var a = Ext.getCmp("ts_ent_line_b_" + g);
							a.stopEditing();
							var e = this.getAllRecords(a);
							if (!e) {
								continue;
							}
							if (!a.isValid()) {
								errors = a.getAllErrors();
								uft.Utils.showWarnMsg(errors);
								return;
							}
							var n = e.length, d = a.getStore();
							for ( var j = 0; j < d.getCount(); j++) {
								var l = d.getAt(j);
								var b = null;
								if ((j - 1) >= 0) {
									b = d.getAt(j - 1);
								}
								var q = l.get("act_arri_date");
								if (q instanceof Date) {
									l.set("act_arri_date", q
											.dateFormat(this.DATETIME_FORMAT));
								}
								var o = l.get("act_leav_date");
								if (o) {
									if (o instanceof Date) {
										l
												.set(
														"act_leav_date",
														o
																.dateFormat(this.DATETIME_FORMAT));
									}
								}
								if (b) {
									var p = b.get("act_arri_date");
									if (p) {
										if (typeof (p) == "string") {
											p = Date.parseDate(p,
													this.DATETIME_FORMAT);
										}
										var q = l.get("act_arri_date");
										if (typeof (q) == "string") {
											q = Date.parseDate(q,
													this.DATETIME_FORMAT);
										}
										if (q < p) {
											uft.Utils
													.showWarnMsg("第"
															+ (j + 1)
															+ "行的实际到达时间必须大于等于上一个节点的实际到达时间！");
											return;
										}
									}
								}
								var c = l.get("arrival_flag");
								if (String(c) == "true" || String(c) == "Y") {
								} else {
									m.push(l.data);
								}
							}
						}
						if (m.length == 0) {
							uft.Utils.showWarnMsg("没有记录需要确认！");
							return;
						}
						Ext.Msg
								.confirm(
										"询问",
										"你确定要批量【确认】吗？",
										function(i) {
											if (i == "yes") {
												uft.Utils
														.doAjax({
															scope : this,
															params : {
																APP_POST_DATA : Ext
																		.encode(m)
															},
															url : ctxPath
																	+ "/te/tracking/confirmArrival.json",
															success : function(
																	u) {
																if (u
																		&& u.append) {
																	Ext.MessageBox
																			.show({
																				title : "警告",
																				msg : u.append,
																				icon : Ext.MessageBox.WARNING,
																				modal : true,
																				buttons : Ext.Msg.OK
																			});
																}
																var s = this.currentRecords;
																for ( var t = 0; t < s.length; t++) {
																	var x = s[t], w = x
																			.get("vbillno");
																	var v = Ext
																			.getCmp("ts_ent_line_b_"
																					+ w);
																	v
																			.getStore()
																			.reload();
																}
															}
														});
											}
										}, this);
					},
					unconfirmArrival : function() {
						var e = this.currentRecords;
						var l = [];
						for ( var g = 0; g < e.length; g++) {
							var p = e[g], f = p.get("vbillno");
							var a = Ext.getCmp("ts_ent_line_b_" + f);
							var d = this.getAllRecords(a);
							if (!d) {
								continue;
							}
							var m = d.length, c = a.getStore();
							for ( var h = 0; h < c.getCount(); h++) {
								var j = c.getAt(h);
								var o = j.get("act_arri_date");
								if (o instanceof Date) {
									j.set("act_arri_date", o
											.dateFormat(this.DATETIME_FORMAT));
								}
								var n = j.get("act_leav_date");
								if (n) {
									if (n instanceof Date) {
										j
												.set(
														"act_leav_date",
														n
																.dateFormat(this.DATETIME_FORMAT));
									}
								}
								var b = j.get("arrival_flag");
								if (String(b) == "true" || String(b) == "Y") {
									l.push(j.data);
								}
							}
						}
						if (l.length == 0) {
							uft.Utils.showWarnMsg("没有记录需要反确认！");
							return;
						}
						l = l.reverse();
						Ext.Msg
								.confirm(
										"询问",
										"你确定要批量【反确认】吗？",
										function(i) {
											if (i == "yes") {
												uft.Utils
														.doAjax({
															scope : this,
															params : {
																APP_POST_DATA : Ext
																		.encode(l)
															},
															url : ctxPath
																	+ "/te/tracking/unconfirmArrival.json",
															success : function(
																	s) {
																if (s
																		&& s.append) {
																	Ext.MessageBox
																			.show({
																				title : "警告",
																				msg : s.append,
																				icon : Ext.MessageBox.WARNING,
																				modal : true,
																				buttons : Ext.Msg.OK
																			});
																}
																var q = this.currentRecords;
																for ( var r = 0; r < q.length; r++) {
																	var v = q[r], u = v
																			.get("vbillno");
																	var t = Ext
																			.getCmp("ts_ent_line_b_"
																					+ u);
																	t
																			.getStore()
																			.reload();
																}
															}
														});
											}
										}, this);
					}
				});
function _processImg() {
	return "<div align='center'><img src='"
			+ ctxPath
			+ "/images/plus.gif' border=0 onclick='deleteEntTracking()' style='cursor:pointer'>";
}
function deleteEntTracking() {
	var b = Ext.getCmp("ts_ent_tracking");
	var e = uft.Utils.getSelectedRecord(b);
	if (e) {
		var j = null;
		var g = b.getStore();
		var d = g.getAt(g.getCount() - 1);
		var f = g.indexOf(e);
		if (f == g.getCount() - 1) {
			if (f > 0) {
				j = g.getAt(f - 1).get("pk_ent_tracking");
			}
		} else {
			j = d.get("pk_ent_tracking");
		}
		for ( var c = 0; c < g.getCount(); c++) {
			if (f > 0) {
				var a = g.getAt(f - 1);
				j = a.get("pk_ent_tracking");
			}
		}
		var h = e.get("pk_ent_tracking");
		uft.Utils.doAjax({
			scope : this,
			method : "GET",
			url : "deleteEntTracking.json",
			actionType : "删除",
			params : {
				pk_ent_tracking : h,
				last_pk : j
			},
			success : function(i) {
				b.getStore().remove(e);
				app.toReload = true;
			}
		});
	}
}
Ext.namespace("uft.te");
uft.te.Tracking = Ext
		.extend(
				Ext.Window,
				{
					constructor : function(d) {
						Ext.apply(this, d);
						if (!this.record) {
							uft.Utils.showErrorMsg("请先选中一条记录！");
							return false;
						}
						app.toReload = false;
						var e = this.record.get("vbillno");
						var b = this.record.get("pk_carrier");
						var c = Utils.doSyncRequest("getTempletMap.json", {
							pk_billtypecode : "ts_te_trin",
							tabCode : "ts_invoice"
						}, "POST");
						if (!c || !c.data) {
							uft.Utils
									.showErrorMsg("没有模板数据，pk_billtypecode：ts_te_trin,tabCode:ts_invoice！");
							return false;
						}
						var f = c.data.records;
						var g = c.data.columns;
						this.trackingInvoiceGrid = new uft.extend.grid.BasicGrid(
								{
									height : 200,
									id : "tracking_invoice",
									pkFieldName : "pk_invoice",
									dataUrl : "loadTrackingInvoice.json",
									params : {
										entrust_vbillno : e
									},
									border : true,
									isCheckboxSelectionModel : true,
									singleSelect : false,
									immediatelyLoad : true,
									recordType : f,
									columns : g
								});
						this.trackingInvoiceGrid.getStore().on(
								"load",
								function(i, h, j) {
									var k = this.trackingInvoiceGrid
											.getSelectionModel().selectAll();
								}, this);
						this.formPanel = new uft.extend.form.FormPanel(
								{
									id : "trackingForm",
									region : "north",
									height : 240,
									labelWidth : 80,
									bodyStyle : "overflow-y:auto;overflow-x:hidden",
									margins : "0px 5px 0px 5px",
									border : true,
									items : [ {
										layout : "tableform",
										layoutConfig : {
											columns : 3
										},
										border : false,
										padding : "5px 5px 0",
										defaults : {
											anchor : "95%",
											xtype : "textfield"
										},
										items : [
												{
													xtype : "uftcombo",
													name : "tracking_status",
													fieldLabel : "跟踪状态",
													itemCls : "uft-form-label-not-null",
													allowBlank : false,
													dataUrl : ctxPath
															+ "/datadict/getDataDict4Combo.json",
													baseParams : {
														datatype_code : "tracking_status"
													},
													colspan : 1
												},
												{
													name : "tracking_time",
													fieldLabel : "跟踪时间",
													itemCls : "uft-form-label-not-null",
													allowBlank : false,
													value : new Date()
															.dateFormat("Y-m-d H:i:s"),
													xtype : "datetimefield",
													colspan : 1
												},
												{
													name : "est_arri_time",
													fieldLabel : "预计到达时间",
													xtype : "datetimefield",
													colspan : 1
												},
												{
													xtype : "textarea",
													name : "tracking_memo",
													fieldLabel : "跟踪信息",
													height : 50,
													colspan : 3
												},
												{
													xtype : "uftcheckbox",
													name : "exp_flag",
													fieldLabel : "是否异常",
													inputValue : "false",
													colspan : 1
												},
												{
													id : "exp_info",
													xtype : "fieldset",
													title : "异常详细信息",
													collapsible : true,
													collapsed : true,
													layout : "tableform",
													padding : "5px 5px 0",
													colspan : 3,
													autoScroll : false,
													layoutConfig : {
														columns : 3
													},
													defaults : {
														anchor : "95%"
													},
													items : [
															{
																name : "vbillno",
																fieldLabel : "单据号",
																xtype : "textfield",
																colspan : 1,
																maxLength : 50
															},
															{
																name : "vbillstatus",
																fieldLabel : "状态",
																itemCls : "uft-form-label-not-edit",
																value : 0,
																xtype : "localcombo",
																readOnly : true,
																colspan : 1,
																hidden : false,
																maxLength : 200,
																store : {
																	fields : [
																			"text",
																			"value" ],
																	data : [
																			[
																					"&nbsp;",
																					"" ],
																			[
																					"新建",
																					"0" ],
																			[
																					"待处理",
																					"71" ],
																			[
																					"处理中",
																					"72" ],
																			[
																					"已处理",
																					"73" ],
																			[
																					"已关闭",
																					"74" ] ],
																	xtype : "arraystore"
																}
															},
															{
																refName : "发货单-web",
																xtype : "headerreffield",
																name : "invoice_vbillno",
																fieldLabel : "发货单号",
																width : 1,
																colspan : 1,
																refWindow : {
																	model : 1,
																	leafflag : false,
																	params : "refName:'发货单-web'",
																	gridDataUrl : ctxPath
																			+ "/ref/common/inv/load4Grid.json?entrust_vbillno="
																			+ e,
																	extGridColumnDescns : [
																			{
																				type : "string",
																				width : 120,
																				dataIndex : "vbillno",
																				xtype : "gridcolumn",
																				hidden : true
																			},
																			{
																				header : "发货单号",
																				type : "string",
																				width : 120,
																				dataIndex : "vbillno",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "客户订单号",
																				type : "string",
																				width : 120,
																				dataIndex : "cust_orderno",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "订单号",
																				type : "string",
																				width : 120,
																				dataIndex : "orderno",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "客户",
																				type : "string",
																				width : 120,
																				dataIndex : "customer_name",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "结算客户",
																				type : "string",
																				width : 120,
																				dataIndex : "bala_customer_name",
																				xtype : "gridcolumn"
																			},
																			{
																				type : "string",
																				width : 120,
																				dataIndex : "pk_customer",
																				xtype : "gridcolumn",
																				hidden : true
																			},
																			{
																				type : "string",
																				width : 120,
																				dataIndex : "bala_customer",
																				xtype : "gridcolumn",
																				hidden : true
																			} ]
																},
																pkField : "vbillno",
																codeField : "vbillno",
																nameField : "vbillno",
																fillinable : true,
																showCodeOnBlur : false,
																getByPkUrl : ctxPath
																		+ "/ref/common/inv/getByPk.do",
																getByCodeUrl : ctxPath
																		+ "/ref/common/inv/getByCode.do"
															},
															{
																id : "_pk_customer",
																itemCls : "uft-form-label-not-edit",
																readOnly : true,
																refName : "客户档案-web",
																xtype : "headerreffield",
																name : "pk_customer",
																fieldLabel : "客户",
																width : 1,
																colspan : 1,
																editFormulaUrl : "execFormula.json",
																refWindow : {
																	model : 1,
																	leafflag : false,
																	params : "refName:'客户档案-web'",
																	gridDataUrl : ctxPath
																			+ "/ref/common/cust/load4Grid.json",
																	extGridColumnDescns : [
																			{
																				type : "string",
																				width : 120,
																				dataIndex : "pk_customer",
																				xtype : "gridcolumn",
																				hidden : true
																			},
																			{
																				header : "客户编码",
																				type : "string",
																				width : 120,
																				dataIndex : "cust_code",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "客户名称",
																				type : "string",
																				width : 120,
																				dataIndex : "cust_name",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "客户类型",
																				type : "string",
																				width : 120,
																				dataIndex : "cust_type_name",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "备注",
																				type : "string",
																				width : 120,
																				dataIndex : "memo",
																				xtype : "gridcolumn"
																			},
																			{
																				type : "string",
																				width : 120,
																				dataIndex : "cust_type",
																				xtype : "gridcolumn",
																				hidden : true
																			} ]
																},
																pkField : "pk_customer",
																codeField : "cust_code",
																nameField : "cust_name",
																showCodeOnBlur : false,
																getByPkUrl : ctxPath
																		+ "/ref/common/cust/getByPk.do",
																getByCodeUrl : ctxPath
																		+ "/ref/common/cust/getByCode.do"
															},
															{
																itemCls : "uft-form-label-not-edit",
																value : e,
																fieldLabel : "委托单号",
																readOnly : true,
																xtype : "textfield",
																name : "entrust_vbillno"
															},
															{
																itemCls : "uft-form-label-not-edit",
																value : b,
																readOnly : true,
																refName : "承运商档案-web",
																xtype : "headerreffield",
																name : "pk_carrier",
																fieldLabel : "承运商",
																width : 1,
																colspan : 1,
																editFormulaUrl : "execFormula.json",
																refWindow : {
																	model : 1,
																	leafflag : false,
																	params : "refName:'承运商档案-web'",
																	gridDataUrl : ctxPath
																			+ "/ref/common/carr/load4Grid.json",
																	extGridColumnDescns : [
																			{
																				type : "string",
																				width : 120,
																				dataIndex : "pk_carrier",
																				xtype : "gridcolumn",
																				hidden : true
																			},
																			{
																				header : "承运商编码",
																				type : "string",
																				width : 120,
																				dataIndex : "carr_code",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "承运商名称",
																				type : "string",
																				width : 120,
																				dataIndex : "carr_name",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "承运商类型",
																				type : "string",
																				width : 120,
																				dataIndex : "carr_type",
																				xtype : "gridcolumn"
																			},
																			{
																				header : "备注",
																				type : "string",
																				width : 120,
																				dataIndex : "memo",
																				xtype : "gridcolumn"
																			} ]
																},
																pkField : "pk_carrier",
																codeField : "carr_code",
																nameField : "carr_name",
																showCodeOnBlur : false,
																getByPkUrl : ctxPath
																		+ "/ref/common/carr/getByPk.do",
																getByCodeUrl : ctxPath
																		+ "/ref/common/carr/getByCode.do"
															},
															{
																name : "type",
																fieldLabel : "类型",
																xtype : "multiselectfield",
																colspan : 1,
																allowBlank : true,
																layzyLoad : false,
																dataUrl : ctxPath
																		+ "/datadict/getDataDict4MultiSelect.json",
																baseParams : {
																	datatype_code : "exp_accident_type"
																}
															},
															{
																name : "memo",
																fieldLabel : "描述",
																xtype : "textfield",
																colspan : 2,
																maxLength : 200
															},
															{
																name : "fb_user",
																fieldLabel : "反馈人",
																xtype : "textfield",
																colspan : 1,
																maxLength : 20
															},
															{
																name : "fb_date",
																fieldLabel : "反馈日期",
																xtype : "uftdatefield",
																colspan : 1,
																maxLength : 20
															},
															{
																name : "occur_date",
																fieldLabel : "发生日期",
																xtype : "uftdatefield",
																colspan : 1,
																maxLength : 10,
																newlineflag : true
															},
															{
																name : "occur_addr",
																fieldLabel : "发生地点",
																xtype : "textfield",
																colspan : 1,
																maxLength : 200
															},
															{
																id : "pk_filesystem",
																name : "pk_filesystem",
																fieldLabel : "附件",
																xtype : "fileuploadfield",
																colspan : 1,
																maxLength : 200,
																permitted_extensions : [
																		"doc",
																		"xdoc",
																		"xls",
																		"xlsx",
																		"pdf",
																		"jpg",
																		"jpeg",
																		"png" ]
															} ]
												},
												{
													xtype : "uftcheckbox",
													name : "sync_flag",
													fieldLabel : "是否同步",
													inputValue : "false",
													checked : true,
													colspan : 1
												},
												{
													id : "sync_info",
													xtype : "fieldset",
													title : "可同步的发货单",
													collapsible : true,
													collapsed : true,
													layout : "fit",
													autoScroll : false,
													colspan : 3,
													items : [ this.trackingInvoiceGrid ]
												}, {
													name : "mainno",
													fieldLabel : "主单号",
													xtype : "textfield",
													newlineflag : true,
													colspan : 1,
													maxLength : 50
												}, {
													name : "flightno",
													fieldLabel : "航班号",
													xtype : "textfield",
													colspan : 1,
													maxLength : 50
												}, {
													name : "flight_time",
													fieldLabel : "航班时间",
													xtype : "datetimefield",
													colspan : 1,
													maxLength : 50
												} ]
									} ]
								});
						this.formPanel.getForm().items
								.each(
										function(h) {
											h
													.on(
															"change",
															function(l, k, i) {
																if (l.name == "invoice_vbillno") {
																	var j = Utils
																			.doSyncRequest(
																					ctxPath
																							+ "/te/ea/getCustomerByInvoice_vbillno.json",
																					{
																						invoice_vbillno : k
																					},
																					"POST");
																	if (j
																			&& j.data) {
																		Ext
																				.getCmp(
																						"_pk_customer")
																				.setValue(
																						j.data);
																	}
																}
															});
											if (h.name == "exp_flag") {
												h
														.on(
																"check",
																function(l, k) {
																	var i = Ext
																			.getCmp("exp_info");
																	var j = Ext
																			.getCmp("trackingForm");
																	if (k == "Y") {
																		i
																				.expand();
																		j
																				.setHeight(j
																						.getHeight() + 135);
																		this
																				.doLayout();
																	} else {
																		i
																				.collapse();
																		j
																				.setHeight(j
																						.getHeight() - 135);
																		this
																				.doLayout();
																	}
																}, this);
											} else {
												if (h.name == "pk_filesystem") {
													h
															.on(
																	"fileselected",
																	function(j,
																			k) {
																		var i = Ext
																				.getBody();
																		i
																				.mask(uft.jf.Constants.UPLOADING_MSG);
																		$
																				.ajaxFileUpload({
																					url : "uploadAttach.json",
																					secureuri : false,
																					fileElementId : "pk_filesystem-file",
																					referTarget : j,
																					dataType : "json",
																					success : function(
																							l,
																							m) {
																						i
																								.unmask();
																						if (l.success) {
																							uft.Utils
																									.showInfoMsg("文件上传成功！");
																							j
																									.setValue(l.data.pk_filesystem);
																						} else {
																							j
																									.setValue(null);
																							uft.Utils
																									.showErrorMsg(l.msg);
																							return;
																						}
																					}
																				});
																	}, this);
												}
											}
										}, this);
						var c = Utils.doSyncRequest("getTempletMap.json", {
							funCode : "t510",
							tabCode : "ts_ent_tracking"
						}, "POST");
						if (!c || !c.data) {
							uft.Utils
									.showErrorMsg("没有模板数据，funCode：t510,tabcode:ts_ent_tracking！");
							return false;
						}
						var f = c.data.records;
						var g = c.data.columns;
						f.unshift({
							name : "_processor",
							type : "string"
						});
						g
								.unshift({
									header : "<span class='uft-grid-header-column'>操作</span>",
									renderer : _processImg,
									width : 30,
									dataIndex : "_processor",
									xtype : "gridcolumn",
									editable : false
								});
						var a = new uft.extend.grid.BasicGrid({
							region : "center",
							id : "ts_ent_tracking",
							pkFieldName : "pk_ent_tracking",
							dataUrl : "loadEntTracking.json",
							params : {
								entrust_vbillno : e
							},
							border : true,
							isCheckboxSelectionModel : false,
							sm : new Ext.grid.RowSelectionModel(),
							singleSelect : true,
							isAddBbar : true,
							immediatelyLoad : true,
							recordType : f,
							columns : g
						});
						uft.te.Tracking.superclass.constructor.call(this, {
							title : "异常跟踪",
							width : 850,
							height : 520,
							closable : true,
							draggable : true,
							resizable : true,
							modal : true,
							border : false,
							layout : "border",
							items : [ this.formPanel, a ],
							buttons : [ {
								xtype : "button",
								text : "保存",
								iconCls : "btnSave",
								scope : this,
								handler : this.saveEntTracking
							}, new Ext.Button({
								iconCls : "btnCancel",
								text : "关&nbsp;&nbsp;闭",
								scope : this,
								handler : function() {
									this.close();
								}
							}) ]
						});
					},
					saveEntTracking : function() {
						var d = this.formPanel.getForm();
						var g = this.formPanel.getErrors();
						if (g.length != 0) {
							uft.Utils.showWarnMsg(Utils.arrayToString(g, ""));
							return;
						}
						var b = d.getFieldValues(false);
						Ext.apply(b, {
							origin : "异常跟踪"
						});
						var f = app.newAjaxParams();
						Ext.apply(f, b);
						var e = [];
						var a = uft.Utils
								.getSelectedRecords(this.trackingInvoiceGrid);
						if (a) {
							for ( var c = 0; c < a.length; c++) {
								e.push(a[c].get("vbillno"));
							}
						}
						f.invoiceVbillnoAry = e;
						uft.Utils.doAjax({
							scope : this,
							method : "POST",
							url : "saveEntTracking.json",
							params : f,
							success : function(i) {
								this.formPanel.getForm().reset();
								if (i && i.data) {
									var m = i.data;
									var l = Ext.getCmp("ts_ent_tracking");
									var n = l.getStore().recordType;
									var h = new n();
									h.beginEdit();
									for ( var k in m) {
										if (m[k] && m[k].pk) {
											h.set(k, m[k].pk);
										} else {
											h.set(k, m[k]);
										}
									}
									h.endEdit();
									var j = l.getStore().getCount();
									l.getStore().insert(j, h);
									app.toReload = true;
								}
							}
						});
					}
				});
Ext.namespace("uft.te");
uft.te.BatchTracking = Ext.extend(Ext.Window, {
	constructor : function(a) {
		Ext.apply(this, a);
		if (!this.records) {
			uft.Utils.showErrorMsg("请先选中记录！");
			return false;
		}
		this.formPanel = new uft.extend.form.FormPanel({
			region : "north",
			height : 225,
			labelWidth : 80,
			autoScroll : true,
			border : true,
			items : [ {
				layout : "tableform",
				layoutConfig : {
					columns : 3
				},
				border : false,
				padding : "5px 5px 0",
				defaults : {
					anchor : "95%",
					xtype : "textfield"
				},
				items : [ {
					xtype : "uftcombo",
					name : "tracking_status",
					fieldLabel : "跟踪状态",
					itemCls : "uft-form-label-not-null",
					allowBlank : false,
					dataUrl : ctxPath + "/datadict/getDataDict4Combo.json",
					baseParams : {
						datatype_code : "tracking_status"
					},
					colspan : 1
				}, {
					name : "tracking_time",
					fieldLabel : "跟踪时间",
					itemCls : "uft-form-label-not-null",
					allowBlank : false,
					value : new Date().dateFormat("Y-m-d H:i:s"),
					xtype : "datetimefield",
					colspan : 1
				}, {
					name : "est_arri_time",
					fieldLabel : "预计到达时间",
					xtype : "datetimefield",
					colspan : 1
				}, {
					xtype : "textarea",
					name : "tracking_memo",
					fieldLabel : "跟踪信息",
					height : 100,
					colspan : 3
				} ]
			} ]
		});
		uft.te.BatchTracking.superclass.constructor.call(this, {
			title : "跟踪",
			width : 700,
			height : 300,
			closable : true,
			draggable : true,
			resizable : true,
			modal : true,
			border : false,
			layout : "fit",
			items : [ this.formPanel ],
			buttons : [ {
				xtype : "button",
				text : "保存",
				iconCls : "btnSave",
				scope : this,
				handler : this.batchSaveEntTracking
			}, new Ext.Button({
				iconCls : "btnCancel",
				text : "关&nbsp;&nbsp;闭",
				scope : this,
				handler : function() {
					this.close();
				}
			}) ]
		});
	},
	batchSaveEntTracking : function() {
		var e = this.formPanel.getForm();
		var g = this.formPanel.getErrors();
		if (g.length != 0) {
			uft.Utils.showWarnMsg(Utils.arrayToString(g, ""));
			return;
		}
		var c = e.getFieldValues(false);
		Ext.apply(c, {
			origin : "异常跟踪"
		});
		var a = [], b = this.records;
		for ( var d = 0; d < b.length; d++) {
			a.push(b[d].get("vbillno"));
		}
		Ext.apply(c, {
			vbillno : a
		});
		var f = app.newAjaxParams();
		Ext.apply(f, c);
		uft.Utils.doAjax({
			scope : this,
			method : "POST",
			url : "batchSaveEntTracking.json",
			params : f,
			success : function(h) {
				app.headerGrid.getStore().reload();
				this.close();
			}
		});
	}
});
Ext.namespace("uft.te");
$import("/busi/te/TrackingMap.js");
uft.te.Transbility = Ext
		.extend(
				Ext.Window,
				{
					constructor : function(b) {
						Ext.apply(this, b);
						if (!this.record) {
							uft.Utils.showErrorMsg("请先选中一条记录！");
							return false;
						}
						var g = this.record.get("pk_entrust");
						var h = Utils.doSyncRequest("getTempletMap.json", {
							funCode : "t501",
							tabCode : "ts_ent_transbility_b"
						}, "POST");
						if (!h || !h.data) {
							uft.Utils
									.showErrorMsg("没有模板数据，funCode：t501,tabcode:ts_ent_transbility_b！");
							return false;
						}
						var f = h.data.records;
						var a = h.data.columns;
						var d = h.data.params;
						this.entTransbilityGrid = new uft.extend.grid.EditorGrid(
								{
									id : "ts_ent_transbility_b",
									pkFieldName : "pk_ent_transbility_b",
									dataUrl : "loadEntTransbilityB.json",
									params : Ext.apply({
										pk_entrust : g
									}, d),
									border : true,
									isCheckboxSelectionModel : false,
									singleSelect : true,
									isAddBbar : false,
									immediatelyLoad : true,
									recordType : f,
									columns : a
								});
						var i = {};
						i.ajaxLoadDefaultValue = false;
						i.headerPkField = "";
						i.bodyGrids = [];
						i.bodyGrids.push(this.entTransbilityGrid);
						i.context = new uft.jf.Context();
						i.context.setTemplateID(d.templateID);
						i.context.setBodyTabCode("ts_ent_transbility_b");
						i.context.setFunCode(d.funCode);
						i.autoRender = false;
						var e = Ext
								.extend(
										uft.jf.BodyToolbar,
										{
											saveUrl : ctxPath
													+ "/te/tb/save.json",
											getBtnArray : function() {
												var j = new Array();
												j.push(this.btn_edit);
												j.push(this.btn_save);
												j.push(this.btn_can);
												j.push(this.btn_ref);
												j.push("-");
												j.push(this.btn_row_add);
												j.push(this.btn_row_del);
												j.push(this.btn_row_ins);
												j.push("-");
												j
														.push(new uft.extend.Button(
																{
																	text : "路线跟踪",
																	scope : this,
																	handler : function() {
																		var l = this.app
																				.getActiveBodyGrid()
																				.getSelectedRow();
																		if (!l) {
																			uft.Utils
																					.showErrorMsg("请先选中一条记录！");
																			return false;
																		}
																		var k = l
																				.get("pk_entrust");
																		var n = l
																				.get("gps_id");
																		if (!n) {
																			uft.Utils
																					.showErrorMsg("该委托单还没有绑定GPS！");
																			return false;
																		}
																		var m = Ext
																				.getCmp("TrackingMap");
																		if (!m) {
																			m = new uft.te.TrackingMap(
																					{
																						pk_entrust : k,
																						gps_id : n
																					});
																		}
																		m
																				.show();
																	}
																}));
												return j;
											},
											getRowDefaultValues : function(j) {
												var k = e.superclass.getRowDefaultValues
														.call(this, j);
												return Ext
														.apply(
																{
																	pk_entrust : this.pk_entrust
																}, k);
											}
										});
						i.toolbar = new e(Ext.apply({
							pk_entrust : g
						}, i));
						var c = new uft.jf.ToftPanel(i);
						uft.te.Transbility.superclass.constructor.call(this, {
							title : "运力信息",
							width : 800,
							height : 400,
							closable : true,
							draggable : true,
							resizable : true,
							modal : true,
							border : false,
							layout : "fit",
							buttons : [ new Ext.Button({
								iconCls : "btnCancel",
								text : "关&nbsp;&nbsp;闭",
								scope : this,
								handler : function() {
									this.close();
								}
							}) ],
							items : [ c ]
						});
					}
				});
Ext.namespace("uft.te");
uft.te.CurrentTracking = Ext
		.extend(
				Ext.Window,
				{
					interval : 60,
					constructor : function(b) {
						Ext.apply(this, b);
						if (!this.pk_entrust) {
							uft.Utils.showErrorMsg("请先选中一条委托单！");
							return false;
						}
						if (!this.carno) {
							uft.Utils.showErrorMsg("请先选中一个车牌号号！");
							return false;
						}
						var a = new Ext.Panel(
								{
									layout : "fit",
									html : '<div id="container" style="width:100%;height:100%;"></div>'
								});
						uft.te.CurrentTracking.superclass.constructor.call(
								this, {
									id : "CurrentTracking",
									title : "车辆跟踪",
									width : 900,
									height : 500,
									closable : true,
									draggable : true,
									resizable : true,
									modal : true,
									border : false,
									layout : "fit",
									buttons : [ new Ext.Button({
										iconCls : "btnCancel",
										text : "关&nbsp;&nbsp;闭",
										scope : this,
										handler : function() {
											this.close();
										}
									}) ],
									items : [ a ]
								});
					},
					show : function() {
						this.doTrack();
						uft.te.CurrentTracking.superclass.show.call(this);
						window.setUftInterval(function() {
							var a = Ext.fly("secDiv");
							if (!a) {
								return;
							}
							var b = a.select("#secDiv span").elements[0];
							var c = b.innerHTML;
							c = parseInt(c);
							if (c == 0) {
								b.innerHTML = this.interval;
								this.doTrack();
							} else {
								b.innerHTML = String(c - 1);
							}
						}, this, 1000);
					},
					doTrack : function() {
						if (this.map) {
							this.map.clearOverlays();
						}
						uft.Utils
								.doAjax({
									scope : this,
									params : {
										pk_entrust : this.pk_entrust,
										carno : this.carno
									},
									url : ctxPath
											+ "/te/tracking/getCurrentTracking.json",
									isTip : false,
									mask : false,
									success : function(b) {
										if (b && b.result) {
											var f = b.dataset;
											if (f && f.length > 0) {
												var e = f[0];
												var g = new BMap.Point(
														e.longitude, e.latitude);
												var d = new BMap.Icon(
														ctxPath
																+ "/busi/te/images/green_2.gif",
														new BMap.Size(28, 28));
												var a = new BMap.Marker(g, {
													icon : d
												});
												this.map.addOverlay(a);
												this.map.centerAndZoom(g, 15);
												var h = Ext.fly("ct_addr");
												if (!h) {
													return;
												}
												h.update(e.place_name
														+ e.road_name);
												var c = "<ul><li>名称：" + e.gpsid
														+ "</li><li>经度："
														+ e.longitude
														+ "</li><li>纬度："
														+ e.latitude
														+ "</li><li>速度："
														+ e.speed
														+ "</li><li>里程："
														+ e.distance
														+ "</li><li>地址："
														+ e.place_name
														+ e.road_name
														+ "</li><li>定位时间："
														+ e.gps_time
														+ "</li><li>备注："
														+ e.memo
														+ "</li><li>来源："
														+ b.source
														+ "</li></ul>";
												Ext.fly("gpsDetailDiv").update(
														c);
											}
										}
									}
								});
					},
					afterRender : function() {
						uft.te.CurrentTracking.superclass.afterRender
								.call(this);
						this.map = new BMap.Map("container");
						this.map.addControl(new BMap.NavigationControl());
						this.map.addControl(new BMap.ScaleControl());
						this.map.addControl(new BMap.OverviewMapControl());
						this.map.addControl(new BMap.MapTypeControl());
						this.map.enableScrollWheelZoom(true);
						this.map.centerAndZoom("北京", 5);
						var b = Ext.get("container"), a = Ext.DomHelper;
						a
								.append(
										b,
										"<div id='secDiv'><span>"
												+ this.interval
												+ "</span>秒后刷新    位置：<span id='ct_addr'></span></div>",
										true);
						a
								.append(
										b,
										"<div id='gpsDiv'><div id='gpsDiv1'><h1 style='padding: 5px 0 0 5px;font-size: 14px;'>GPS</h1><HR style='position: inherit;' width='90%' color='#987cb9'/><div id='gpsDetailDiv' style='margin: 15px;line-height: 23px;'></div><div></div>",
										true);
						a
								.append(
										Ext.get("gpsDiv"),
										'<div class="gpsDiv_om omBtnClosed" style="bottom: 0px; left: 0px; top: auto; right: auto;"></div>');
						Ext.query(".gpsDiv_om")[0].onclick = function() {
							var c = Ext.get(this);
							var d = c.hasClass("omBtnClosed");
							if (d) {
								c.removeClass("omBtnClosed");
								c.addClass("omBtn");
							} else {
								c.removeClass("omBtn");
								c.addClass("omBtnClosed");
							}
							Ext.get("gpsDiv1").toggle();
						};
					}
				});