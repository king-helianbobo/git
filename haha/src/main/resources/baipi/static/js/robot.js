$(function(){
	$('body').css("height",$(window).height()-10);
	$(window).resize(function(){
		$('body').css("height",$(window).height()-20);
	});
	function nano(template, data) { //https://github.com/trix/nano
		 //{user.first_name}
		  return template.replace(/\{([\w\.]*)\}/g, function(str, key) {
		    var keys = key.split("."), v = data[keys.shift()];
		    for (var i = 0, l = keys.length; i < l; i++) v = v[keys[i]];
		    return (typeof v !== "undefined" && v !== null) ? v : "";
		  });
		}
	
	var el ={
			textInput:$('#text-input'),
			//chatform:$('#chatform'),
			messages:$('#messages')
	}
	/*$('#text-input').keyup(function(){
			$.ajax({ 
				type:'post',
				dataType:'html',
	            url:'../?method=base&nature=true',
	            data:'input='+ el.textInput.val(),
	            success: function(result) {
				var host=[];
				var jsonobj=eval('('+result+')');
				for(var i=0;i<jsonobj.suggestions.length;i++){
					host.push(jsonobj.suggestions[i].replace(/<em>/g,"<span style='color:red'>").replace(/<.?em>/g,"</span>"));
				 }
				console.dir(host)
				$('#text-input').autocomplete({
					source: host,
					select: function(event,ui) {
						alert(this.value.replace(/<span>/g,"").replace(/<.?span>/g,""))
					    $("#text-input").attr('value',this.value.replace(/<span>/g,"").replace(/<.?span>/g,""))
					}
				});
	            }
	        });
	});*/
	
	var tpl={//id='answer_"+parseInt(messagenumcount-1)+"'
			userChat:function(id,content){
				return nano('<div id="question_{id}" class="msg user" >'+
						'<div class="m">'+
						'<div class="s"></div>'+
						'<div id="laba"></div>'+
						'<div id="msguserload{id}" class="waitanswer"></div>'+
						'<div style="float:left" id="content{id}">{content}</div> '+
						'<div id="showanswer_{id}" class="down" onclick="showanswer(\'{id}\')"></div>'+
					'</div>'+
					'<div class="clear"></div>'+
				'</div>',{id:id,content:content})
			}
			
	}
	// 绑定事件
	el.textInput.on("focus", function(){
    }).on("blur", function(){
    }).on("keydown", function(ev){
    	if(ev.keyCode==13){
    		 sendMessage();
    	}
    }).on('keyup',function(ev){
    });
	
	$('#chat-btn-send').on('click',function(){
		sendMessage();
	})
	
	var forceCloseSide = true;
	var size=10;
	function sendMessage(){
		
		var value = el.textInput.val();
		if(!value){
			return;
		}
		if(value.length>20){
			alert('您输入的文字超过20个，请简明地提出您的问题。');
			return;
		}
		//el.textInput.val('');
		
		askServer(value);
		
		closeAutoComplete();
	}
	
    function linkMessage(value){
		//alert('linkMessage');
		var value = value;
		if(!value){
			return;
		}
		if(value.length>20){
			showMessage(false,'您输入的文字超过20个，请简明地提出您的问题。');
			return;
		}
		el.textInput.val('');
		
		linkServer(value);
		
		closeAutoComplete();
	}
	window.sendMessage = sendMessage;
	window.linkMessage = linkMessage;
	
	function askServer(question,extinfo){
		if(forceCloseSide) {
			toggleSide(forceCloseSide);
			forceCloseSide=false;
		}
		var mid = showUserMessage(question);
		//请求服务器
		var senddata={q:question,mid:mid,sid:sessionId};
		if(extinfo){
			senddata.extinfo=extinfo;
		}
		if(window.pssenddata){
			window.pssenddata(senddata)
		}
	    console.log(question);
	    ajaxAction(question,'0','first');
	}
	
	function ajaxAction(question,page,tagType){
		$.ajax({
		    url:'../?method=nlp&nature=true',
			type:'post',
			dataType:'html',
		    data:'input='+question+'&&from='+page+'&&size=10&&tagType='+tagType+'',
			cache:true,
			success :function(html , textStatus , xhrObj) {
			    var jsonobj=eval('('+html+')');
			    console.dir(jsonobj);
				var div = document.getElementById('content');
				var columnStr="";
				var time="";
				div.innerHTML="";
				columnStr+= "<div class='cen'>"+
							"<div class='cen_list'>"+
							"<ul>"+
							"<h3>"+
							"<div id='question_1' class='msg user'>"+
							"<div class='m'>"+
							"<div class='s'></div>"+
							"<div id='laba'></div>"+
							"<div id='msguserload1' class='waitanswer'></div>"+
							"<div style='float: left'>"+
							"栏目导航";
							//if(jsonobj.tag7!=0){
								columnStr +="<div id='extpagebox' style='background: #ccc; width: 100; height: 80;'><iframe name='content_frame' marginwidth=0 marginheight=0 width=500px height=400px src='base.html' frameborder=0 style='box-shadow: 1px 1px 1px 1px #999999;'></iframe></div>";
							//}
				 columnStr +="</div>"+
							"</div>"+
							"</div>"+
							"</h3>"+
							"<br>"+
							"<div>"+
							"<div class='msg bot' id='answer_1'>"+
							"<div class='m' style='background: #fff; border: 1px solid #CCCCCC; border-radius: 3px; box-shadow: 1px 1px 1px #CCCCCC; margin-top: 7px; padding: 10px; width: 900px;'>";
							if(jsonobj.tag8!=0&&jsonobj.tag8!=undefined){
								columnStr +="<div id='1' class='submain' style='display: block;height:200px;overflow-y:scroll;'>"+
											"<div class='submain_left' style='float: left; margin-top: 5px'>"+
											"<img border='0' src='"+jsonobj.tag8.img+"' width='100' height='130'>"+
											"</div>"+
											"<div class='submain_right' style='overflow: hidden;font-size:15px;margin-left: 130px;'>"+
											""+jsonobj.tag8.info+""+
											"</div>"+
											"</div>";
							}
				  columnStr +="<div class='s'></div>"+
							"<ul class='mtabs'>";
							if(jsonobj.tag1!=0&&jsonobj.tag1!=undefined){
								 columnStr +=  "<li onclick='setTab(1)' class='mtabli selected'  numid='0' id='1'>魅力锡城</li>"
							}
							if(jsonobj.tag2!=0&&jsonobj.tag2!=undefined){
								 columnStr +=  "<li onclick='setTab(2)' class='mtabli'  numid='1' id='2'>锡城资讯</li>";
							}
							if(jsonobj.tag3!=0&&jsonobj.tag3!=undefined){
								 columnStr +=  "<li onclick='setTab(3)' class='mtabli'  numid='2' id='3'>信息公开</li>";
							}
							if(jsonobj.tag4!=0&&jsonobj.tag4!=undefined){
								 columnStr +=  "<li onclick='setTab(4)' class='mtabli'  numid='3' id='4'>公共服务</li>";
							}
							if(jsonobj.tag5!=0&&jsonobj.tag5!=undefined){
								 columnStr +=  "<li onclick='setTab(5)' class='mtabli'  numid='4' id='5'>政务大厅 </li>";
							}
							if(jsonobj.tag6!=0&&jsonobj.tag6!=undefined){
								 columnStr +=  "<li onclick='setTab(6)' class='mtabli'  numid='5' id='6'>政民互动</li>";
							}
							if(jsonobj.tag7!=0&&jsonobj.tag7!=undefined){
								 columnStr +=  "<li onclick='setTab(7)' class='mtabli'  numid='6' id='7'>热点话题</li>";
							}
				columnStr+=	"</ul>"+
							"<div class='mtabcontents' id='tabInfoDiv'>"+
							"<div class='mcontent  selected'>"+
							"<ul class='mcontentul'>";
							if(jsonobj.tag1!=0&&jsonobj.tag1!=undefined){
								for(var i=0;i<jsonobj.tag1.pageList.length;i++){
									if(jsonobj.tag1.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag1.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag1.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
							              "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag1.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag1.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
											 columnStr += "</div>"+//共搜索到10000条结果
											              "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag1.pageList[i].content+"<a href="+jsonobj.tag1.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
								}
							}else if(jsonobj.tag2!=0&&jsonobj.tag2!=undefined){
								for(var i=0;i<jsonobj.tag2.pageList.length;i++){
									if(jsonobj.tag2.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag2.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag2.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
										 "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag2.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag2.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
										 columnStr += "</div>"+
										              "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag2.pageList[i].content+"<a href="+jsonobj.tag2.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
								}
							}else if(jsonobj.tag3!=0&&jsonobj.tag3!=undefined){
								for(var i=0;i<jsonobj.tag3.pageList.length;i++){
									if(jsonobj.tag3.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag3.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag3.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
										 "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag3.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag3.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
										 columnStr += "</div>"+
										              "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag3.pageList[i].content+"<a href="+jsonobj.tag3.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
									}
							}else if(jsonobj.tag4!=0&&jsonobj.tag4!=undefined){
								for(var i=0;i<jsonobj.tag4.pageList.length;i++){
									if(jsonobj.tag4.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag4.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag4.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
										 "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag4.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag4.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
										 columnStr += "</div>"+
										              "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag4.pageList[i].content+"<a href="+jsonobj.tag4.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
								}
							}else if(jsonobj.tag5!=0&&jsonobj.tag5!=undefined){
								for(var i=0;i<jsonobj.tag5.pageList.length;i++){
									if(jsonobj.tag5.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag5.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag5.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
										 "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag5.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag5.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
										 columnStr += "</div>"+
										               "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag5.pageList[i].content+"<a href="+jsonobj.tag5.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
								}
							}else if(jsonobj.tag6!=0&&jsonobj.tag6!=undefined){
								for(var i=0;i<jsonobj.tag6.pageList.length;i++){
									if(jsonobj.tag6.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag6.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag6.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>" +
										 "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag6.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag6.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
										 columnStr += "</div>"+
										              "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag6.pageList[i].content+"<a href="+jsonobj.tag6.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
						      }
							}else if(jsonobj.tag7!=0&&jsonobj.tag7!=undefined){
								for(var i=0;i<jsonobj.tag7.pageList.length;i++){
									if(jsonobj.tag7.pageList[i].time==null){
										time="";
									}else{
										time=jsonobj.tag7.pageList[i].time;
									}
										columnStr +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.tag7.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
										 "<div class='tags' style='display:none;'>"+
							              "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
							 				for(var j=0;j<jsonobj.tag7.pageList[i].keywords.length;j++){
							 					if(j<4)
							 						columnStr += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.tag7.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
							 				}
											 columnStr += "</div>"+
											              "<div style='display: none;color:#4B7BBE'>"+jsonobj.tag7.pageList[i].content+"<a href="+jsonobj.tag7.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
						      }
							}
				columnStr+=	"</ul>"+
							"</div>"+
							"</div>"+
							"<br>"+
							"<div class='cen_next' id='page'>"+
							"<ul>";
							if(jsonobj.tag1!=0&&jsonobj.tag1!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag1.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",1)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",1)'>"+i+"</a></li>";
									}
									if(i==Math.ceil(jsonobj.tag1.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",1)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag1.count+"</span>条结果)</span>";
									}
								}
							}else if(jsonobj.tag2!=0&&jsonobj.tag2!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag2.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",2)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",2)'>"+i+"</a></li>";
									}
									if(i==Math.ceil(jsonobj.tag2.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",2)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag2.count+"</span>条结果)</span>";
									}
								}
							}else if(jsonobj.tag3!=0&&jsonobj.tag3!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag3.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",3)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",3)'>"+i+"</a></li>";
									}
									if(i==Math.ceil(jsonobj.tag3.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",3)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag3.count+"</span>条结果)</span>";
									}
								}
							}else if(jsonobj.tag4!=0&&jsonobj.tag4!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag4.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",4)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",4)'>"+i+"</a></li>";
									}
									if(i==Math.ceil(jsonobj.tag4.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",4)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag4.count+"</span>条结果)</span>";
									}
								}
							}else if(jsonobj.tag5!=0&&jsonobj.tag5!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag5.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",5)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",5)'>"+i+"</a></li>";
									} 
									if(i==Math.ceil(jsonobj.tag5.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",5)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag5.count+"</span>条结果)</span>";
									}
								}//(共搜索到<span style='color:red'>"+jsonobj.tag5.count+"</span>条结果)
							}else if(jsonobj.tag6!=0&&jsonobj.tag6!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag6.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",6)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",6)'>"+i+"</a></li>";
									}
									if(i==Math.ceil(jsonobj.tag6.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",6)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag6.count+"</span>条结果)</span>";
									}
								}
								
							}else if(jsonobj.tag7!=0&&jsonobj.tag7!=undefined){
								for(var i=1;i<=Math.ceil(jsonobj.tag7.count/size);i++){
									if(i==1){
										columnStr+="<li class='lidown'> <a onclick='paging("+i+",\""+question+"\",7)'>"+i+"</a></li>";
									}else if(i<=10){
										columnStr+="<li class='liup'> <a onclick='paging("+i+",\""+question+"\",7)'>"+i+"</a></li>";
									}
									if(i==Math.ceil(jsonobj.tag7.count/size)){
										columnStr+="<li class='lifirst'> <a onclick='paging(parseInt("+i+"+1),\""+question+"\",7)' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.tag7.count+"</span>条结果)</span>";
									}
								}
								
							}
			    columnStr+= "</ul>"+
							"</div>"+
							"</div>"+
							"<div class='fankui'>"+
							"<a target='_blank' onClick='wentifankui(\""+question+"\",false);return false' href='#'><img width='21' src='static/images/bad.png'>无帮助</a>"+
							"<a target='_blank' onClick='wentifankui(\""+question+"\",true);return false' href='#'><img width='21' src='static/images/good.png'>有帮助</a><div style='clean: both'></div></div>"+
							"</div>"+
							"</div>"+
							"</ul>"+
							"</div>"+
							"</div>";
				div.innerHTML="";
				div.innerHTML = columnStr;
				el.messages.append(div);
			},
			/*complete:function(){
				//定位滚动条
				//_kscroll.resize();
				//_kscroll.scrollToElement($('#question_'+mid,el.messages));
			   positionScrollbar($('#question_'+mid,el.messages))
				el.textInput[0].focus();
			}*/
		});
	}
	
	window.askServer = askServer;
	window.ajaxAction = ajaxAction;
	
	function paging(pageNum,text,tagType){
		var size=10;
		var type = 'tag'+tagType;
		var num = null;
		if(pageNum==0){
			num = parseInt(pageNum*10);
		}else{
			num = parseInt(pageNum*10-10);
		}
		var sbtitle=document.getElementById('tabInfoDiv');
		var pageHtml = document.getElementById('page');
		sbtitle.innerHTML="";
		pageHtml.innerHTML="";
		$.ajax({
            url:'../?method=nlp&nature=true',
            type:'post',
            dataType:'html',
            data:'input='+text+'&&from='+num+'&&size=10&&tagType='+type+'',
            cache:true,
            success :function( html , textStatus , xhrObj) {
            	 var jsonobj=eval('('+html+')');
                // console.dir(jsonobj);
                 var time="";
                 var obj="";
                obj += "<div class='mcontent  selected'>"+
                       "<ul class='mcontentul'>";
                 		for(var i=0;i<jsonobj.pageList.length;i++){
                 			if(jsonobj.pageList[i].time==null){
								time="";
							}else{
								time=jsonobj.pageList[i].time;
							}
                 				obj +="<li><span class=tag><a onclick='javascript:showDiv($(this))'; style='cursor:pointer; color:blue; font-size:15px;'>"+jsonobj.pageList[i].contenttitle+"</a>&nbsp;"+time+"</span></li>"+
                 					  "<div class='tags' style='display:none;'>"+
                 					   "&nbsp&nbsp&nbsp&nbsp&nbsp<s style='text-decoration: none;'>标签：</s>";
                 					   for(var j=0;j<jsonobj.pageList[i].keywords.length;j++){
					 					if(j<4)
					 						obj += "<span class='tag'><a href='#' target='_blank' style='color:black'>"+jsonobj.pageList[i].keywords[j]+"</a></span>&nbsp&nbsp&nbsp";
					 				   }
                 				obj +="</div>"+
                 					  "<div style='display: none;color:#4B7BBE'>"+jsonobj.pageList[i].content+"<a href="+jsonobj.pageList[i].url+" target='_blank' style='color:red;'>[查看全文]</a></div>";
                 		}
                 obj +="</ul>"+
                 		"</div>"+
                 		"<br>"+
                 		"<div class='cen_next'>"+
						"<ul>";
                       var flag=parseInt((num/10));
                       
                       var t;
                       if(flag<11){
                    	   t=1;
                       }else{
                    	   t=parseInt(flag-10);
                       }
                      // alert(flag+'--'+Math.ceil(jsonobj.count/size)+'==='+t)
					     for(var i=t;i<=Math.ceil(jsonobj.count/size);i++){
					    	 if (i <= parseInt(flag+9)){
					    		 if(i==1){
					    			 obj+="<li class='liup'> <a onclick='paging("+i+",\""+text+"\","+tagType+")'>"+i+"</a></li>";
					    		 }else if(pageNum==i){
					    			 obj+="<li class='lidown'> <a onclick='paging("+i+",\""+text+"\","+tagType+")'>"+i+"</a></li>";
					    		 }else{
										obj+="<li class='liup'> <a onclick='paging("+i+",\""+text+"\","+tagType+")'>"+i+"</a></li>";
									}
					    	 }
					    	 if(i==Math.ceil(jsonobj.count/size)){
									obj+="<li class='lifirst'> <a onclick='paging(parseInt("+pageNum+"+1),\""+text+"\","+tagType+")' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 700px;'>(共搜索到<span style='color:red'>"+jsonobj.count+"</span>条结果)</span>";
								}
							/*if(i==1){
								obj+="<li class='liup'> <a onclick='paging("+i+",\""+text+"\","+tagType+")'>"+i+"</a></li>";
							}else if(pageNum==i){
								obj+="<li class='lidown'> <a onclick='paging("+i+",\""+text+"\","+tagType+")'>"+i+"</a></li>";
							}else{
								obj+="<li class='liup'> <a onclick='paging("+i+",\""+text+"\","+tagType+")'>"+i+"</a></li>";
							}
							if(i==Math.ceil(jsonobj.count/size)){
								obj+="<li class='lifirst'> <a onclick='paging(parseInt("+pageNum+"+1),\""+text+"\","+tagType+")' href='javascript:void(0)'>下一页</a></li><span style='color: #bbb;display: block;margin-left: 100px;margin-top: -2px; width: 500px;'>(共搜索到<span style='color:red'>"+jsonobj.count+"</span>条结果)</span>";
							}*/
							}
		obj+= "</ul>"+
					"</div>";
				sbtitle.innerHTML = obj;
            }
         });
	}
	window.paging = paging;
	
	var messagenumcount = 2;
	function showUserMessage(msg){//显示用户提问的问题		
		var id = messagenumcount++;
		var html = tpl.userChat(id,msg)
		//$(html).appendTo(el.messages);
		el.messages.append(html);
		return id;
	}
	
	function showText(){
		$('.mcontentul>li').show();
		$('.mcontentul').next('a').hide();
	}
	function showDiv(obj){
		obj.parent().parent().next().next().slideToggle();
		obj.parent().parent().next().slideToggle();
	}
	window.showText = showText;
	window.showDiv = showDiv;
	
	function closeOldAnswer(mid){
		var bots = $('.bot',el.messages);
		var len = bots.length;
		//console.dir(bots);
		for(var i=2;i<len;i++){//for(var i=0;i<len-1;i++){
			var x = $(bots[i]);
				if(x.css('display')=='none'){
					var id = x.attr('id');
					id = id.substring(7);
					showanswer(id);
				}else if(x.css('display')=='block'){
					var id = x.attr('id');
					id = id.substring(7);
					closeanswer(id);
				}
		}
	}
	
	window.closeanswer= function(id){
		if ($('#answer_'+id).css("display") == 'block') {
			$('#showanswer_'+id).removeClass('down').addClass('up');
			$('#answer_'+id).css("display",'none');
		} else if($('#ganswer_'+id).css("display") == 'block'){
			$('#showanswer_'+id).removeClass('up').addClass('down');
			$('#ganswer_'+id).css("display",'none');
		}else {
			//$('#showanswer_'+id).replaceClass("up",'down');
			$('#showanswer_'+id).removeClass('down').addClass('up');
			$('#answer_'+id).css("display",'block');
		}
	}
	
	window.showanswer=function(id){
		
		if ($('#answer_'+id).css("display") == 'block') {
			$('#showanswer_'+id).removeClass('down').addClass('up');
			$('#answer_'+id).css("display",'none');
		} else if($('#ganswer_'+id).css("display") == 'block'){
			$('#showanswer_'+id).removeClass('down').addClass('up');
			$('#ganswer_'+id).css("display",'none');
		} else {
			//$('#showanswer_'+id).replaceClass("up",'down');
			$('#showanswer_'+id).removeClass('up').addClass('down');
			$('#answer_'+id).css("display",'block');
			$('#ganswer_'+id).css("display",'block');
		}
		//_kscroll.resize()注释掉的
	}
	
	function processHtml(node){
		$('a',node).attr('target','_blank');
		activeMtabs(node)
		activeTabLiMore(node);
		processWhenClickGoAskLinks(node);
		anim(node);
		uniqueabstip(node)
	}
	//注释掉的
	function uniqueabstip(node){
		
		var tip = $('.repeatTip',node)
		tip.powerTip({
			mouseOnToPopup:true,
			smartPlacement:true,
			placement:'e',
			closeDelay:300
		});
		var md5 = tip.attr('md5')
		tip.data('powertip', "<iframe src='absmoretitles?md5="+md5 +"' frameborder=\"no\" src='about:blank' width=\"300px\" height=\"230px\" ></iframe>");
	
		/*Tooltip.attach({
		        trigger: $('.J_Trigger',node),      // 用于触发Tooltip出现的节点
		        refer:  $('.J_Refer',node),          // 用于Tooltip进行位置计算的节点
		        tooltip:  $('.J_Tooltip',node),     // Tooltip节点
		        position: 'right',
		        align: 'left',
		        onShow: function( trigger, tooltip ){ 
		        	var iframe = $('iframe',tooltip)
		        	if(iframe.attr('src')=='about:blank'){
		        		iframe.attr({src:'absmoretitles?md5='+iframe.attr('md5')})
		        	}
		        }
		});*/
	}
	
	function anim(node){
		var mm = $('.m',node);
		mm.animate(
                {
                	'background-color':'#fdfdc0'
                },
                0.7,
                'easeInOutQuad',function(){
                	mm.animate({'background-color':'#fff'},0.7,'easeInOutQuad')
                }
                );
	}
	
	//当class标记为whenClickGoAsk，那么点击动作变为一次提问
	function processWhenClickGoAskLinks(node){
		//$('a',node).attr('target','_blank')//超链弹出
		var links = $('.whenClickGoAsk',node);
		for(var i=0;i<links.length;i++){
			var a = $(links[i]);
			a.on('click',function(ev){
				var ask = $(this).text();
			    console.log(ask);
				askServer(ask,$(this).attr('extinfo'));
				return false;
			});
		}
	}
	function activeTabLiMore(node){//计算出现在ul下面的a标签
		var tabuls = $('.moreli',node);
		for(var i=0;i<tabuls.length;i++){
			var more  = $(tabuls[i]);
			if(more){
				more.on('click',function(ev){
					var x = $(this);
					var p = x.parent();
					$('li',p).css('display','');
					x.css('display','none');
					return false;
				});
			}
		
		}
	}
	
	function activeMtabs(node){
		var litabs = $('.mtabli',node);
		var contents = $('.mcontent',node);
		for(var i=0;i<litabs.length;i++){
			var tab = $(litabs[i]);
			tab.attr('numid', i);
			tab.on('click',function(ev){
				litabs.removeClass('selected');
				$(this).addClass('selected');
				
				//var x = contents[this.numid];
				var x = contents[$(this).attr('numid')];
				if(x){
					contents.css('display','none');
					$(x).css('display','block');
				}
				updateScrollbar();
				//_kscroll.resize();
				//_kscroll.scrollToElement($(node));
			});
		}
	}
	
	var _suggest = null;
	/////
	
	//注释掉的
	//重写suggest,底部对齐
    /*Suggest.prototype._setContainerRegion =  function() {
        var self = this, config = self.config,
            input = self.textInput,
            p = DOM.offset(input),
            container = self.container;

        DOM.css(container, {
            left: p.left,
            bottom: DOM.docHeight() - p.top + config.offset+20
        });
       

        DOM.width(container, config['containerWidth'] || input.offsetWidth - 2);
    };*/
//注释掉的
    //重写suggest,过滤html代码
    /*Suggest.prototype._updateInputFromSelectItem = function() {
        var self = this, val = self._getSelectedItemKey(self.selectedItem) || self.query;
        var text = val.replace(/<[^>]+>/g,"")
        self.textInput.value = text; // 如果没有 key, 就用输入值
    };*/
    
    /**
     * 变量suggestService在页面中定义
     */
	//注释掉的
   /* _suggest = new Suggest(el.textInput, suggestService, {
        resultFormat: '',
        dataType: 0,
        containerWidth:'30%',
        shim:false
    });*/
    
   /* _suggest.on("itemSelect", function(t){
    	 sendMessage($(t.target.selectedItem).text());
    });*/
    
	//定义投票"有用/无用"函数,html中调用
    window.wentifankui=function(question,yorn){
		var formdata={};
		formdata.q=question;
		formdata.help=yorn;
		$.ajax({
			url:'helpvote',
			type:'post',
			data:formdata,
			cache:false,
			success :function( data , textStatus , xhrObj){
				alert('感谢您的反馈');
			}
    	});
	}
});

