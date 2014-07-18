var _viewpostWidth,_viewportHeight,_kscroll;
$(document).ready(function(){
    var wd = $(window);
    var el ={
	head:$('#header'),
	footer:$('#footer'),
	content:$("#content"),
	bodyframe:$("#bodyframe"),
	btnarea:$("#btn-area"),
	workarea:$("#work-area"),
	workAreaCao:$("#work-area-cao"),
	extpagebox:$('#extpagebox'),
	chatbox:$("#chatbox"),
	chatbox_inner:$("#chatbox-inner"),
	chatlogpanel:$("#chatlogpanel"),//messagesbox:$('#messages-box'),
	inputpanel:$("#inputpanel"),
	extbox:$("#extbox"),
	splitbox:$('#splitbox'),
	inputcount:$("#inputcount"),
	
	textinput:$("#text-input")
    }
    
    var headHeight = el.head.outerHeight(true)//添加的
    var footerHeight = el.footer.outerHeight(true)//添加的
    var workAreaWidth=0;
    var workAreaHeight=0;
    var chatboxWidth=0;
    //函数定义
    function resetLayout(){
	/*var viewpostWidth = DOM.viewportWidth();
	  var viewportHeight = DOM.viewportHeight();
	  if(_viewpostWidth!=viewpostWidth && _viewportHeight!=viewportHeight){*/
	var viewpostWidth = wd.width()
	var viewportHeight = wd.height()
	if(_viewpostWidth != viewpostWidth || _viewportHeight != viewportHeight){
	    _viewpostWidth=viewpostWidth;
	    _viewportHeight=viewportHeight;
	    
	    /*el.bodyframe.height(_viewportHeight);
	      el.content.height(_viewportHeight- el.footer.outerHeight(true) -el.head.outerHeight(true));//除去头和尾的高度，求出主要内容区的高度
	      
	      var contentHeight = el.content.height();*/
	    var contentHeight = viewportHeight - headHeight - footerHeight;//添加的
	    el.content.height(contentHeight  < 250 ? 250 : contentHeight);//添加的
	    
	    el.workarea.height(contentHeight);//工作区域的宽已经在css中设置
	    el.workarea.width(el.workarea.width());
	    
	    workAreaWidth = el.workarea.width();
	    workAreaHeight = el.workarea.height();
	    chatboxWidth = el.chatbox.width();
	    el.chatbox.height(workAreaHeight - 2);
	    
	    
	    el.chatlogpanel.height(el.chatbox.height()-el.inputpanel.outerHeight(true)-5);
	    
	    var caoblocks = $('.caoblock');
	    for(var i=0;i<caoblocks.length;i++){
		$(caoblocks[i]).width(workAreaWidth);
		$(caoblocks[i]).height(workAreaHeight);
	    }
	    el.workAreaCao.width(caoblocks.length*workAreaWidth);
	    el.workAreaCao.height(workAreaHeight);
	    
	    $('#ext_public_service').height(workAreaHeight -7 - ($('#sidetopbar').height() + $('#ext_xw').height() + $('#ext_gg').height() ));
	}
    }
    
    resetLayout();
    
    //注册事件
    wd.on('resize',function(){
	resetLayout();
    });
    
    wd.resetLayout = resetLayout;
    
    $('#chat-btn').on('click',function(e){
	toggleSide();
    });
    
    //左侧工具条按钮	
    var blockcount = $('.caoblock').length;
    var allbtn = $('#btns .btn');
    for(var i=0;i<allbtn.length;i++){
	var x = $(allbtn[i]);
	x.attr('numid', i);
/*	x.on('click',function(ev){
	    allbtn.removeClass('active');
    	    $(this).addClass('active');
	    
	    el.workAreaCao.animate(
	        {
	            "left":-1*workAreaWidth*($(this).attr('numid'))// "left": -1*workAreaWidth*($(this).numid)
                },
                    .3,
                'easeInOutQuart');
	    
	    if($(this).attr('numid')*1===0){
		$('#zhuangshi').css("display","block");
	    }else{
		$('#zhuangshi').css("display","none");
	    }
	    
	});*/
    };
    
    /* this allows us to pass in HTML tags to autocomplete. Without this they get escaped */
    $[ "ui" ][ "autocomplete" ].prototype["_renderItem"] = function( ul, item) {
	//console.log(item);
	//console.log(item.label);
	//console.log(this.term);
	return $( "<li></li>" ) 
	    .data( "item.autocomplete", item )
	    .append( $( "<a></a>" ).html( item.label ) )
	    .appendTo( ul );
    };
    
    //绑定智能提示suggest
    window.closeAutoComplete=function() {
	$( "#text-input" ).autocomplete( "close" );
    }
    $( "#text-input" ).autocomplete({
	source: function( request, response ) {
	    $.ajax({
		url:'../?method=base&nature=true',
		dataType: "json",
		data:'input='+request.term,
		success: function( data ) {
		    var dataArray = new Array();
		    dataArray = data.suggestions;
		    response( $.map( dataArray, function( item ) {
			return {
			    label: item.replace(/<em>/g,"<span style='color:red'>").replace(/<.?em>/g,"</span>"),
			    value: item.replace(/<em>/g,"").replace(/<.?em>/g,"")
			}
		    }));
		}
	    });
	},
	minLength: 1,
    }
				   );
    
    
    
    //滚动条	
    /*	_kscroll = new Kscroll($('#messages'),{prefix:"clear-",allowArrow:false})
	_kscroll.resize(el.chatlogpanel.innerWidth(),el.chatlogpanel.innerHeight());

	window.resizeScroll=function(w,h){
    	_kscroll.resize(w,h);
	}*/
    window.resizeScroll=function(w,h){
    	$('#chatlogpanel').resize(w,h);
    }

    $('#fontcontorl_s').on('click',function(){
	$('#messages').css('font-size','11px');
    }
			  );
    $('#fontcontorl_b').on('click',function(){
	$('#messages').css('font-size','14px');
    }
			  );
    //安装消息显示区域滚动条，自己添加的
    el.chatlogpanel.perfectScrollbar({
	wheelSpeed:100,
	wheelPropagation:false
    });
    var updateScrollbar=function(){
	el.chatlogpanel.perfectScrollbar('update');
    }
    var positionScrollbar = function(topOrObj){
	el.chatlogpanel.scrollTo(topOrObj)
        
	updateScrollbar();
    }
    window.updateScrollbar=updateScrollbar
    window.positionScrollbar=positionScrollbar	
    
    //字数统计
    var inputcount = el.inputcount.text(200);
    function chkinput(){
        var val = el.textinput.val(),
        len = 200-val.length;
        if(len<0){
            el.textinput.val(val.substring(0,200));
            len = 0;
        }
        el.inputcount.text(len);
    };
    el.textinput.on('keyup',function(ev){
    	chkinput();
    });
    
    /**
     * 开关聊天界面右侧的侧栏
     */
    var sideStatus = true;
    function toggleSide(forceClose){
	if(sideStatus || forceClose){
	    $('#chatbox').animate(
	        {
	            "width": workAreaWidth - 12
	        },
	            .6,
	        'easeInOutQuart',function(){
	            resizeScroll(el.chatlogpanel.innerWidth(),el.chatlogpanel.innerHeight());
	            if(forceClose){
	            }
	        });
	    sideStatus=false;
	    
	    $('#spbtn').addClass('on');
	}else{
	    $('#chatbox').animate(
	        {
	            "width": chatboxWidth
	        },
	            .3,
	        'easeInOutQuart',function(){
	            resizeScroll(el.chatlogpanel.innerWidth(),el.chatlogpanel.innerHeight())
	        });
	    sideStatus=true;
	    $('#spbtn').removeClass('on');
	}
    }
    window.toggleSide=toggleSide;
    $('#splitbox').on('click',function(){
	toggleSide();
	
    });
    
    //欢迎语帮助折叠图标
    $('.quickbtn').on('click',function(){
	closeQuickBtnTop();
	var xx = $('#quickhelp');
	if(xx.css('display')=='none'){
	    xx.css('display','block')
	    //$('#quickbtn')[0].src = 'static/images/help/help_expand.png';
	    $('#quickbtn').attr ('src','static/images/help/help_expand.png');
	}else{
	    xx.css('display','none');
	    $('#quickbtn').attr('src','static/images/help/help_collapse.png');
	}
	
	//_kscroll.resize();注释掉的
	
    });
    $('#quickbtntip .mbClose').on('click',function(e){
	closeQuickBtnTop();
    })
    
    function closeQuickBtnTop(){
	//$('#quickbtntip').style('display','none');
	$('#quickbtntip').css('display','none');
    }
    window.setTimeout(function(){closeQuickBtnTop()},20*1000);
    
    //为欢迎语中的帮助问题添加事件
    var links = $('#welcome a');
    for(var i=0;i<links.length;i++){
	var a = $(links[i]);
	if(a.hasClass('quickbtn')){
	    continue;
	}
	a.on('click',function(ev){
	    //var ask = this[0].innerText;
	    var ask = $(this).text();
	    askServer(ask);
	    return false;
	});
    }
    
    window.submitFeedback=function(){
	var formdata = $('#feedback form').serialize();
	if(!$('#j_userName').val()){
	    alert('请填写姓名');
	    return;
	}
	if(!$('#j_email').val()){
	    alert('请填写邮件地址');
	    return;
	}
	if(!$('#j_letterTitle').val()){
	    alert('请填写标题');
	    return;
	}
	if(!$('#j_content').val()){
	    alert('请填写内容');
	    return;
	}
	$.ajax({
	    url:'feedback',
	    method:'post',
	    data:formdata,
	    cache:false,
	    success :function( data , textStatus , xhrObj){
		alert('提交成功,感谢您的反馈！');
	    }
	});
    }
    
});


/*KISSY.add('util/placeholder', function(S) {

  var $=S.all,
  WRAP_TMPL='<div class="placeholder" style="position: relative;display:'+(S.UA.ie>7?'inline-block':'inline')+';zoom:1;"></div>',
  TIP_TMPL='<label style="display: none;position:absolute;left:0;top:0;">{tip}</label>',
  isSupport = "placeholder" in document.createElement("input");
*/
$(document).add(function() {

    /*  var $=S.all,*/
    WRAP_TMPL='<div class="placeholder" style="position: relative;display:'+(S.UA.ie>7?'inline-block':'inline')+';zoom:1;"></div>',
    TIP_TMPL='<label style="display: none;position:absolute;left:0;top:0;">{tip}</label>',
    isSupport = "placeholder" in document.createElement("input");
    
    /**
     * config{
     *  el：{HtmlElement}目标表单元素
     *  wrap: {Boolean} default true 需要创建一个父容器
     * }
     *
     * 支持两种方式：
     * 1、html5的placeholder属性
     * 2、其他浏览器的支持
     */
    function placeholder(el, cfg) {  
        //支持html5的placeHolder属性
        if(isSupport) return;

        var self=this,
        defaultCfg = {
            wrap:true
        };

        if(self instanceof placeholder) {
            var config = S.merge(defaultCfg, cfg);
            self._init(el, config);
        } else {
            return new placeholder(el, cfg);
        }
    }

    S.augment(placeholder, {
        _init:function(target, cfg) {
            var self = this;

            if(!target) {
                S.log('[placeholder] has no target to decorate');
                return;
            }

            target = $(target);

            var placeHolderTip = target.attr('placeholder');

            if(!placeHolderTip) return;

            function _decorate() {
                //创建一个label
                var str=S.substitute(TIP_TMPL, {
                    tip:placeHolderTip
                });
                var triggerLabel = self.triggerLabel = $(str);
                triggerLabel.css("width",target.css("width"));
                if(target.attr('id')) {
                    triggerLabel.attr('for', target.attr('id'));
                } else {
                    triggerLabel.on('click', function() {
                        target[0].focus();
                    });
                }

                //create parent               
                var targetBox = $(WRAP_TMPL);
                targetBox.appendTo(target.parent())
                    .append(target);

                //insertbefore target
		triggerLabel.insertBefore(target);

                //judge value && init form reset
                S.later(function() {
                    if(!target.val()) {
                        triggerLabel.show();
                    }
                }, 100);
            };

            target.on('focus', function(ev) {
                self.triggerLabel.hide();
            });

            target.on('blur', function(ev) {
                if(!target.val()) {
                    self.triggerLabel.show();
                }
            });

            _decorate();

        },
        /**
         * 可以修改tip文案
         * @param newTip
         */
        text:function(newTip) {
            this.triggerLabel.text(newTip);
        }
    });

    return placeholder;
});

//添加的
;(function( $ ){

    var $scrollTo = $.scrollTo = function( target, duration, settings ){
	$(window).scrollTo( target, duration, settings );
    };

    $scrollTo.defaults = {
	axis:'xy',
	duration: parseFloat($.fn.jquery) >= 1.3 ? 0 : 1,
	limit:true
    };

    // Returns the element that needs to be animated to scroll the window.
    // Kept for backwards compatibility (specially for localScroll & serialScroll)
    $scrollTo.window = function( scope ){
	return $(window)._scrollable();
    };

    // Hack, hack, hack :)
    // Returns the real elements to scroll (supports window/iframes, documents and regular nodes)
    $.fn._scrollable = function(){
	return this.map(function(){
	    var elem = this,
	    isWin = !elem.nodeName || $.inArray( elem.nodeName.toLowerCase(), ['iframe','#document','html','body'] ) != -1;

	    if( !isWin )
		return elem;

	    var doc = (elem.contentWindow || elem).document || elem.ownerDocument || elem;

	    return /webkit/i.test(navigator.userAgent) || doc.compatMode == 'BackCompat' ?
		doc.body : 
		doc.documentElement;
	});
    };

    $.fn.scrollTo = function( target, duration, settings ){
	if( typeof duration == 'object' ){
	    settings = duration;
	    duration = 0;
	}
	if( typeof settings == 'function' )
	    settings = { onAfter:settings };

	if( target == 'max' )
	    target = 9e9;

	settings = $.extend( {}, $scrollTo.defaults, settings );
	// Speed is still recognized for backwards compatibility
	duration = duration || settings.duration;
	// Make sure the settings are given right
	settings.queue = settings.queue && settings.axis.length > 1;

	if( settings.queue )
	    // Let's keep the overall duration
	    duration /= 2;
	settings.offset = both( settings.offset );
	settings.over = both( settings.over );

	return this._scrollable().each(function(){
	    // Null target yields nothing, just like jQuery does
	    if (target == null) return;

	    var elem = this,
	    $elem = $(elem),
	    targ = target, toff, attr = {},
	    win = $elem.is('html,body');

	    switch( typeof targ ){
		// A number will pass the regex
	    case 'number':
	    case 'string':
		if( /^([+-]=?)?\d+(\.\d+)?(px|%)?$/.test(targ) ){
		    targ = both( targ );
		    // We are done
		    break;
		}
		// Relative selector, no break!
		targ = $(targ,this);
		if (!targ.length) return;
	    case 'object':
		// DOMElement / jQuery
		if( targ.is || targ.style )
		    // Get the real position of the target 
		    toff = (targ = $(targ)).offset();
	    }
	    $.each( settings.axis.split(''), function( i, axis ){
		var Pos	= axis == 'x' ? 'Left' : 'Top',
		pos = Pos.toLowerCase(),
		key = 'scroll' + Pos,
		old = elem[key],
		max = $scrollTo.max(elem, axis);

		if( toff ){// jQuery / DOMElement
		    attr[key] = toff[pos] + ( win ? 0 : old - $elem.offset()[pos] );

		    // If it's a dom element, reduce the margin
		    if( settings.margin ){
			attr[key] -= parseInt(targ.css('margin'+Pos)) || 0;
			attr[key] -= parseInt(targ.css('border'+Pos+'Width')) || 0;
		    }

		    attr[key] += settings.offset[pos] || 0;

		    if( settings.over[pos] )
			// Scroll to a fraction of its width/height
			attr[key] += targ[axis=='x'?'width':'height']() * settings.over[pos];
		}else{ 
		    var val = targ[pos];
		    // Handle percentage values
		    attr[key] = val.slice && val.slice(-1) == '%' ? 
			parseFloat(val) / 100 * max
			: val;
		}

		// Number or 'number'
		if( settings.limit && /^\d+$/.test(attr[key]) )
		    // Check the limits
		    attr[key] = attr[key] <= 0 ? 0 : Math.min( attr[key], max );

		// Queueing axes
		if( !i && settings.queue ){
		    // Don't waste time animating, if there's no need.
		    if( old != attr[key] )
			// Intermediate animation
			animate( settings.onAfterFirst );
		    // Don't animate this axis again in the next iteration.
		    delete attr[key];
		}
	    });

	    animate( settings.onAfter );			

	    function animate( callback ){
		$elem.animate( attr, duration, settings.easing, callback && function(){
		    callback.call(this, targ, settings);
		});
	    };

	}).end();
    };

    // Max scrolling position, works on quirks mode
    // It only fails (not too badly) on IE, quirks mode.
    $scrollTo.max = function( elem, axis ){
	var Dim = axis == 'x' ? 'Width' : 'Height',
	scroll = 'scroll'+Dim;

	if( !$(elem).is('html,body') )
	    return elem[scroll] - $(elem)[Dim.toLowerCase()]();

	var size = 'client' + Dim,
	html = elem.ownerDocument.documentElement,
	body = elem.ownerDocument.body;

	return Math.max( html[scroll], body[scroll] ) 
	    - Math.min( html[size]  , body[size]   );
    };

    function both( val ){
	return typeof val == 'object' ? val : { top:val, left:val };
    };

})( jQuery );
