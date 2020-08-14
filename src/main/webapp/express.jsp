<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>订单信息查询</title>
<%@ include file="/common/ulwHeader.jsp"%>
<link href="<%=request.getContextPath()%>/css/express.css"
	rel="stylesheet" type="text/css" />
</head>
<body>
	<div class="wrapper">
		<div class="logo">
			<img src="<c:url value="/images/logo-login.png"/>" alt="物流运输管理平台">
		</div>
		<div class="ic-mod-banner">
			<div id="_banners" class="banners">
				<div class="banner banner1" style="z-index: 0; opacity: 1;">
					<a href="javascript:;" target="_blank"><img
						src="<c:url value="/images/express-1.jpg"/>" alt="">
					</a>
				</div>

				<div class="banner banner2" style="z-index: 0; opacity: 1;">
					<a href="javascript:;" target="_blank"><img
						src="<c:url value="/images/express-2.jpg"/>" alt="">
					</a>
				</div>
			</div>
			<div id="_focus" class="focus">
				<a data-index="0" href="#" class=""> <span class="bg-b"></span>
					<span class="inner"></span> </a> <a data-index="1" href="#" class="">
					<span class="bg-b"></span> <span class="inner"></span> </a>
			</div>
		</div>
		<div class="express">
			<div class="express_content">
				<h2>快递查询</h2>
				<ul>
					<li><input type="text" id="billno" name="billno" style="font-size: 14px" /></li>
					<li><button type="submit" class="express_btn" onclick="loadExpress();">查询</button></li>
				</ul>
			</div>
			<div class="express_msg" style="display: block;" >
				<p>欢迎使用....</p>
			</div>
		</div>
	</div>
	<div class="center" align="center">上海闰知信息科技有限公司版权所有</div>
	<script type="text/javascript">
		var glume = function(banners_id, focus_id) {
			this.$ctn = $('#' + banners_id);
			this.$focus = $('#' + focus_id);
			this.$adLis = null;
			this.$btns = null;
			this.switchSpeed = 5;//自动播放间隔(s)
			this.defOpacity = 1;
			this.crtIndex = 0;
			this.adLength = 0;
			this.timerSwitch = null;
			this.init();
		};
		glume.prototype = {
			fnNextIndex : function() {
				return (this.crtIndex >= this.adLength - 1) ? 0
						: this.crtIndex + 1;
			},
			//动画切换
			fnSwitch : function(toIndex) {
				if (this.crtIndex == toIndex) {
					return;
				}
				this.$adLis.css('zIndex', 0);
				this.$adLis.filter(':eq(' + this.crtIndex + ')').css('zIndex',
						2);
				this.$adLis.filter(':eq(' + toIndex + ')').css('zIndex', 1)
						.css('display', '');
				this.$btns.removeClass('on');
				this.$btns.filter(':eq(' + toIndex + ')').addClass('on');
				var me = this;

				$(this.$adLis[this.crtIndex]).animate({
					opacity : 0
				}, 1000, function() {
					me.crtIndex = toIndex;
					$(this).css({
						opacity : me.defOpacity,
						zIndex : 0
					});
				});
			},
			fnAutoPlay : function() {
				this.fnSwitch(this.fnNextIndex());
			},
			fnPlay : function() {
				var me = this;
				me.timerSwitch && clearInterval(me.timerSwitch);
				me.timerSwitch = window.setInterval(function() {
					me.fnAutoPlay();
				}, me.switchSpeed * 1000);
			},
			fnStopPlay : function() {
				clearInterval(this.timerSwitch);
				this.timerSwitch = null;
			},

			init : function() {
				this.$adLis = this.$ctn.children();
				this.$btns = this.$focus.children();
				this.adLength = this.$adLis.length;

				var me = this;
				//点击切换
				this.$focus.on('click', 'a', function(e) {
					e.preventDefault();
					var index = parseInt($(this).attr('data-index'), 10)
					me.fnSwitch(index);
				});
				this.$adLis.filter(':eq(' + this.crtIndex + ')').css('zIndex',
						2);
				this.fnPlay();

				//hover时暂停动画
				this.$ctn.hover(function() {
					me.fnStopPlay();
				}, function() {
					me.fnPlay();
				});

				if ($.browser.msie && $.browser.version < 7) {
					this.$btns.hover(function() {
						$(this).addClass('hover');
					}, function() {
						$(this).removeClass('hover');
					});
				}
			}
		};
		var player1 = new glume('_banners', '_focus');

		$(function() {
			$('#billno').bind('keypress', function(event) {
				if (event.keyCode == "13") {
					loadExpress();
				}
			});
		});


		function loadExpress() {
			var billno = $('#billno').val();
			if (!billno) {
				$('#billno').focus();
				alert('单号不能为空！');
				return;
			}
			$.ajax({
				type : "GET",
				url :'loadExpress.json',
				data : {'billno':billno},
				success : function(data) {
					var html ="";
					if(data.success){//成功
						var expressList = data.datas;
						html = "<ul>";
						for(var i=0;i<expressList.length;i++){
							var img = "<img src='<c:url value='/images/line.png'/>'>";
							if(i == 0){
								img = "<img src='<c:url value='/images/start.png'/>'>";
							}
							if(i == expressList.length-1){
								img = "<img src='<c:url value='/images/end.png'/>'>";
							}
							var status = expressList[i].status;
							var tracking_time = expressList[i].tracking_time;
								tracking_time = tracking_time.substring(0,tracking_time.length-3);
							var tracking_memo = expressList[i].tracking_memo;
							var tracking_memo_show = tracking_memo;
							if(tracking_memo_show.length > 26){
								tracking_memo_show = tracking_memo_show.substring(0,26)+"...";
							}
							html +="<li style='font-size:12px;'>"+img+"<span>"+tracking_time+"</span><span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+status+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><p title='"+tracking_memo+"' style='display: inline;'>"+tracking_memo_show+"</p></li>";
						}
						html +="</ul>";
					}else{
						html = "<p class='tips_icon'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+data.msg+"</p>"
					}
					$('.express_msg').html(html);
				}
			});
		}

	</script>
</body>
<%@ include file="/common/footer.jsp"%>
</html>
