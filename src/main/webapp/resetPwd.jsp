<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title><%=Global.productName%>-重置密码</title>
<%@ include file="/common/ulwHeader.jsp"%>
<link href="<%=request.getContextPath()%>/css/login.css"
	rel="stylesheet" type="text/css" />
<style>
#div_verify_code {
	position: absolute;
	background-color: #93b0cc;
	border: solid 1px #545454;
	padding: 3px;
	z-index: 9999999;
	right: 288px;
	top: 299px;
}

.orange {
	color: #fef4e9;
	border: solid 1px #da7c0c;
	background: #f78d1d;
	background: -webkit-gradient(linear, left top, left bottom, from(#faa51a),
		to(#f47a20) );
	background: -moz-linear-gradient(top, #faa51a, #f47a20);
	filter: progid : DXImageTransform.Microsoft.gradient ( startColorstr =
		'#faa51a', endColorstr = '#f47a20' );
}

.button {
	display: inline-block;
	zoom: 1;
	vertical-align: baseline;
	margin: 0 2px;
	outline: none;
	cursor: pointer;
	text-align: center;
	text-decoration: none;
	font: 14px/100% Arial, Helvetica, sans-serif;
	padding: .5em 2em .55em;
	text-shadow: 0 1px 1px rgba(0, 0, 0, .3);
	-webkit-border-radius: .5em;
	-moz-border-radius: .5em;
	border-radius: .5em;
	-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .2);
	-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .2);
	box-shadow: 0 1px 2px rgba(0, 0, 0, .2);
}
</style>
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
						src="<c:url value="/images/login-1.png"/>" alt="">
					</a>
				</div>

				<div class="banner banner2" style="z-index: 0; opacity: 1;">
					<a href="javascript:;" target="_blank"><img
						src="<c:url value="/images/login-2.png"/>" alt="">
					</a>
				</div>
			</div>
			<div id="_focus" class="focus">
				<a data-index="0" href="#" class=""> <span class="bg-b"></span>
					<span class="inner"></span> </a> <a data-index="1" href="#" class="">
					<span class="bg-b"></span> <span class="inner"></span> </a>
			</div>
		</div>
		<div class="login_form">
			<div class="login_content">
				<h2>重置密码</h2>
				<ul>
					<form id="login_form" method="post"
						onSubmit="return formCheck();">
						<li><label>登陆名：</label><input type="text" id="username"
							name="username" class="login_account" style="font-size: 14px" />
						</li>
						<li><label>验证码：</label><input type="text" class="login_psw"
							id="verifyCode" name="verifyCode" />
						</li>
						<li align='right'><button type="button" class="button orange"
								style='margin-right: 10px;'>提交</button></li>
						<li align='right'><a href='login.html' style='margin-right: 10px;text-decoration: underline;font-size:12px;'>已有账号？</a></li>
					</form>
				</ul>
			</div>
		</div>
		<div id='div_verify_code' style='display: none;'>
			<img id='img_verify_code' onclick="getVerify()"
				style="cursor: pointer; margin-bottom: 5px"
				src="verifyCodeGenerator.do" alt="获取中..." title="点击更换" width="120"
				height="45" /><br />
			<span onclick="getVerify()"
				style="font-size: 11px; color: white; cursor: pointer">看不清楚?换一个</span>
		</div>
	</div>
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
			$('#username').bind('keypress', function(event) {
				if (event.keyCode == "13") {
					$('#verifyCode').focus();
				}
			});
		});

		var verifyCounter = [], timeout;

		$('#verifyCode').focus(function() {
			$('#div_verify_code').show();
		});
		$('#verifyCode').blur(function() {
			timeout = setTimeout(function() {
				$('#div_verify_code').hide();
			}, 200);
		});

		function formCheck() {
			var username = $('#username').val();
			var verifyCode = $('#verifyCode').val();
			if (!username) {
				$('#username').focus();
				alert('登陆名不能为空！');
				return false;
			}
			if (!verifyCode) {
				$('#verifyCode').focus();
				alert('验证码不能为空！');
				return false;
			}
			return true;
		}

		function getVerify() {
			clearTimeout(timeout);
			$('#verifyCode').focus();
			var dc = new Date().getTime();
			$('#img_verify_code')[0].src = 'verifyCodeGenerator.do?_dc=' + dc;
		}

		$('.orange').click(function() {
			$('.orange').html('提交中...');
			$('.orange').attr("disabled",true); 
			var bol = formCheck();
			if (!bol) {
				$('.orange').attr("disabled",false); 
				$('.orange').html('提交');
				return false;
			}
			var username = $('#username').val();
			var verifyCode = $('#verifyCode').val();
			Utils.request({
				url : 'resetPassword.json',
				params : {
					username : username,
					verifyCode : verifyCode
				},
				onSuccess : function(result) {
					var result = JSON.parse(result);
					if (result.msg) {
						alert(result.msg);
					}
					$('#verifyCode').val('');
					$('#username').val('');
					$('.orange').attr("disabled",false); 
					$('.orange').html('提交');
				},
				onFailure : function(result) {
					alert('服务器处理失败，请重试，若还有问题，请联系管理员！');
					$('.orange').attr("disabled",false); 
					$('.orange').html('提交');
				}
			});
		});
	</script>
</body>
<%@ include file="/common/footer.jsp"%>
</html>
