<%@page contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset='utf-8'>
<meta http-equiv="X-UA-Compatible" content="chrome=1">
<meta name="viewport" content="width=640">

<link rel="stylesheet" href="stylesheets/core.css" media="screen">
<link rel="stylesheet" href="stylesheets/mobile.css"
	media="handheld, only screen and (max-device-width:640px)">
<link rel="stylesheet" href="stylesheets/github-light.css">

<script type="text/javascript" src="javascripts/modernizr.js"></script>
<script type="text/javascript" src="javascripts/jquery.min.js"></script>
<script type="text/javascript" src="javascripts/headsmart.min.js"></script>
<script type="text/javascript" src="javascripts/jquery.timer.js"></script>
<script src="javascripts/vendor/jquery.ui.widget.js"></script>
<script src="javascripts/jquery.iframe-transport.js"></script>
<script src="javascripts/jquery.fileupload.js"></script>
<script>
var progress;
var timer;
var isIOSExpand=0;
function uploadInit() {
    $('#demo').fileupload({
        dataType: 'json',
        autoUpload : true,
        add: function (e, data) {
        	$('#statusHint').text('彩票上传中...');
        	$('#progress').show();
        	$('#progress .bar').show();
        	$('#progress .bar').css('width', '0%');
        	progress = 0;
        	timer = $.timer(500, function() {
        		progress = progress + 2;
        		$('#progress .bar').css('width',progress + '%');
        		timer.reset(500);
        	});
            if (data.autoUpload || (data.autoUpload !== false &&
                    $(this).fileupload('option', 'autoUpload'))) {
                data.process().done(function () {
                    data.submit();
                });
            }
        },
        fail: function (e, data) {
        	$('#progress .bar').hide();
        	$('#progress').hide();
        	timer.stop();
        	$('#statusHint').text('彩票解析失败，请重试');
        },
        done: function (e, data) {
        	$('#progress').hide();
        	$('#progress .bar').hide();
        	timer.stop();
        	$('#statusHint').text('彩票上传并解析完毕，您可以上传下张彩票');
            $('#ocrResult').show();
            if (data.result.periodId != null) {
            	$('#periodIdHint').show();
            	$('#periodId').html(data.result.periodId);
            }
            if (data.result.amount != null) {
            	$('#amountHint').show();
            	$('#amount').html(data.result.amount+'元');
            }
            if (data.result.amount != null) {
            	$('#multipleHint').show();
            	$('#multiple').html(data.result.multiple);
            }
            if (data.result.added == '1')
            	$('#added').show();
            else
            	$('#added').hide();
            hidefields();
            if (data.result.codes != null) {
            	$('#codes').html(data.result.codes.replace(/,/g,"<br />"));
            	$('#codes').show();
            	$('#codesHint').show();
            }
            if (data.result.frontCodes != null) {
            	$('#frontCodes').html(data.result.frontCodes);
            	$('#frontCodes').show();
            	$('#frontCodesHint').show();
            }
            if (data.result.backCodes != null) {
            	$('#backCodes').html(data.result.backCodes);
            	$('#backCodes').show();
            	$('#backCodesHint').show();
            }
            if (data.result.frontCodesDan != null) {
            	$('#frontCodesDan').html(data.result.frontCodesDan);
            	$('#frontCodesDan').show();
            	$('#frontCodesDanHint').show();
            }
            if (data.result.frontCodesTuo != null) {
            	$('#frontCodesTuo').html(data.result.frontCodesTuo);
            	$('#frontCodesTuo').show();
            	$('#frontCodesTuoHint').show();
            }
            if (data.result.backCodesDan != null) {
            	$('#backCodesDan').html(data.result.backCodesDan);
            	$('#backCodesDan').show();
            	$('#backCodesDanHint').show();
            }
            if (data.result.backCodesTuo != null) {
            	$('#backCodesTuo').html(data.result.backCodesTuo);
            	$('#backCodesTuo').show();
            	$('#backCodesTuoHint').show();
            }
        }
    });	
} 

$(document).ready(function () {
    $('#main_content').headsmart()
  });
$(function () {
	uploadInit();
	$("#downloadButtonIOS").click(function(){
		if (isIOSExpand == 0) {
	    	$("#downloadBarCode").animate({height:'200px',width:'200px',opacity:'1'},"slow");
	    	//$("#downloadBarCode").css('z-index','10');
	    	isIOSExpand=1;
		} else {
			$("#downloadBarCode").animate({height:'0px',width:'0px',opacity:'0'},"slow");
			isIOSExpand=0;
		}
	});
});
function hidefields() {
	$('#codesHint').hide();
	$('#codes').hide();
	$('#frontCodesHint').hide();
	$('#frontCodes').hide();
	$('#backCodesHint').hide();
	$('#backCodes').hide();
	$('#frontCodesDanHint').hide();
	$('#frontCodesDan').hide();
	$('#frontCodesTuoHint').hide();
	$('#frontCodesTuo').hide();
	$('#backCodesDanHint').hide();
	$('#backCodesDan').hide();
	$('#backCodesTuoHint').hide();
	$('#backCodesTuo').hide();
}
</script>
<title>实体彩票识别 by kajelas</title>
</head>

<body>
	<div class="shell">

		<header>
			<span class="ribbon-outer"> <span class="ribbon-inner">
					<h1>实体彩票识别</h1>
					<h2>用技术变革传统购彩</h2>
			</span> <span class="left-tail"></span> <span class="right-tail"></span>
			</span>
		</header>

		<section id="downloads">
			<span class="inner"> 
				<a class="zip"><em>Android</em>暂不开放</a>
				<a style="cursor:pointer;" id="downloadButtonIOS" class="tgz"><em>IOS</em>点我扫码下载</a>
			</span>
		</section>

		<img id="downloadBarCode" src="images/downloadCode.png" height="0px" width="0px" style="position:relative;display:block;margin-left:auto;margin-right:auto;top:10px;opacity:0;z-index:10"/>

		<span class="banner-fix"></span>


		<section id="main_content">
			<h3>
				<a id="实体彩票识别" class="anchor"
					href="#%E5%AE%9E%E4%BD%93%E5%BD%A9%E7%A5%A8%E8%AF%86%E5%88%AB"
					aria-hidden="true"><span aria-hidden="true"
					class="octicon octicon-link"></span></a>项目简介
			</h3>

			<p>本项目旨在通过技术手段为彩民和彩票店主提供线下购彩的辅助服务。
				本阶段实现如下功能：用户通过手机拍照或者图片上传实体彩票，然后手机端利用图像识别技术提取投注信息，后台对实体彩票进行计奖并由手机端展示。通过这一功能，用户可以方便地知晓自己的彩票是否中奖以及中奖金额。
				由于时间有限，暂时将彩种限定为大乐透，形式为IOS APP。 后续会完善彩种并加入一键生成彩票投注单图片的功能。</p>

			<hr>
			<h3>识别效果演示</h3>
			<a id="demo" class="file" data-url="lotteryUpload">
    			<input type="file" name="loto" multiple>
    			<span id="statusHint">点击这里上传<strong>大乐透</strong>彩票照片(请尽量正对彩票拍摄)</span>
			</a>
			<div id="progress" hidden="hidden">
    			<div class="bar" style="width: 0%;"></div>
			</div>
			<div id="ocrResult" hidden="hidden">识别结果:</div>
			<div id="periodIdHint" hidden="hidden">
				期号:<span id="periodId"></span>
			</div>
			<div id="amountHint" hidden="hidden">
				金额:<span id="amount"></span>
			</div>
			<div id="multipleHint" hidden="hidden">
				倍数:<span id="multiple"></span>
			</div>
			<div id="added" hidden="hidden">追加投注</div>
			<div id="codesHint" hidden="hidden">单式号码:</div>
			<span id="codes"></span>
			<div id="frontCodesHint" hidden="hidden">复式前区号码:</div>
			<span id="frontCodes"></span>
			<div id="backCodesHint" hidden="hidden">复式后区号码:</div>
			<span id="backCodes"></span>
			<div id="frontCodesDanHint" hidden="hidden">前区胆码:</div>
			<span id="frontCodesDan"></span>
			<div id="frontCodesTuoHint" hidden="hidden">前区拖码:</div>
			<span id="frontCodesTuo"></span>
			<div id="backCodesDanHint" hidden="hidden">后区胆码:</div>
			<span id="backCodesDan"></span>
			<div id="backCodesTuoHint" hidden="hidden">后区拖码:</div>
			<span id="backCodesTuo"></span>
		</section>

		<footer>
			<p>因财力有限，目前使用的是单核服务器，速度较慢，敬请谅解=￣ω￣=</p>
			<span class="ribbon-outer"> <span class="ribbon-inner">
					<p>
						This project by <a href="https://github.com/kajelas">kajelas</a>
						can be found on <a
							href="https://github.com/kajelas/LotteryIdentify">GitHub</a>
					</p>
			</span> 
			<span class="left-tail"></span> <span class="right-tail"></span>
			</span>
			<!--<p>Generated with <a href="https://pages.github.com">GitHub Pages</a> using Merlot</p>-->
			<span class="octocat"></span>
		</footer>

	</div>
</body>
</html>
