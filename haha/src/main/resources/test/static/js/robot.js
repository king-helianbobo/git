$(function(){
	function nano(template, data) { //https://github.com/trix/nano
		 //{user.first_name}
		  return template.replace(/\{([\w\.]*)\}/g, function(str, key) {
		    var keys = key.split("."), v = data[keys.shift()];
		    for (var i = 0, l = keys.length; i < l; i++) v = v[keys[i]];
		    return (typeof v !== "undefined" && v !== null) ? v : "";
		  });
		}
	
	var divNum=1;
	var el ={
			textInput:$('#text-input'),
			chatform:$('#chatform'),
			messages:$('#messages')
	}
	
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
	function sendMessage(){
		
		var value = el.textInput.val();
		if(!value){
			return;
		}
		if(value.length>200){
			showMessage(false,'您输入的文字超过200个，请简明地提出您的问题。');
			return;
		}
		el.textInput.val('');
		
		askServer(value);
		
		closeAutoComplete();
	}
	
    function linkMessage(value){
		//alert('linkMessage');
		var value = value;
		if(!value){
			return;
		}
		if(value.length>200){
			showMessage(false,'您输入的文字超过200个，请简明地提出您的问题。');
			return;
		}
		el.textInput.val('');
		
		linkServer(value);
		
		closeAutoComplete();
	}
	window.sendMessage = sendMessage;
	window.linkMessage = linkMessage;
	
	function askServer(question,extinfo){
		  divNum++;
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
		$.ajax({
			//url:'ask',
		    url:'../?method=nlp&nature=true',
			type:'post',
			dataType:'html',
		    data:'input='+question+'&&from=0&&size='+10+'&&tagType=first',
			cache:true,
//			data:senddata,
//			cache:false,
			success :function( html , textStatus , xhrObj) {
			    console.log(html);
			    var jsonobj=eval('('+html+')');
			    var tagFlg='';
				var div = document.createElement('div');
				console.dir(jsonobj)
				//Trim('111 222');
				var columnStr="";
				var viewFlag='tag1';
				var jquertStr =  "$(function(){"+
		          "$('ul.mtabs>li').click(function(){"+
		          "$(this).addClass('selected').siblings('.mtabli').removeClass('selected');"+
		          "$(this).parent().next().children('.mcontent').removeClass('selected').eq($(this).index()).addClass('selected');"+
		          //"alert($(this).index());"+
		          //"alert($(this).parent().next().attr('id'));"+
		          "var type=1;"+
		         // "alert($(this).text());"+
		          "if($(this).text()=='魅力锡城'){"+
		          "type=1;"+
		          "}else if($(this).text()=='锡城资讯'){"+
		          "type=2;"+
		          "}else if($(this).text()=='信息公开'){"+
		          "type=3;"+
		          "}else if($(this).text()=='公共服务'){"+
		          "type=4"+
		          "}else if($(this).text()=='行政服务'){"+
		          "type=5"+
		          "}else if($(this).text()=='政民互动'){"+
		          "type=6"+
		          "}"+
		          //"alert($(this).parent().next().attr('id'));"+
		          "aa(type,$(this).parent().next().attr('id')[$(this).parent().next().attr('id').length - 1]);"+
		          "});"+ 
		          "});"+
		          "function aa(tagType,id){"+
		          //"alert(document.getElementById('content2').textContent);"+
		          "var ab=id;"+
		          //"alert('ab==='+ab);"+
		          "var cc ='content';"+
		          "var dd =cc+ab;"+
		          //"alert(dd);"+
		          "$.ajax({"+
		            "url:'../?method=nlp&nature=true'," +
		            "type:'post',"+
		            "dataType:'html',"+
		            "data:'input='+document.getElementById(dd).textContent+'&&from=0&&size='+10+'&&tagType=tag'+tagType+'',"+
		            "cache:true,"+//"+size+"
		            "success :function( html , textStatus , xhrObj) {"+
		            "var jsonobj=eval('('+html+')');"+
		            "console.dir(jsonobj);"+
		            "var obj='';"+
		            "var div = document.createElement('div');"+
		            "obj+='<div class=\"mcontent selected\">';"+
		            "obj+='<div class=tags>';"+
		            "obj +='<s>标签：</s>';"+
		            "for(var i=0;i<jsonobj.pageList[0].keywords.length;i++){"+
		            "	   if(i<4)"+
		            "	  obj += '<span class=tag><a href=# target=_blank>'+jsonobj.pageList[0].keywords[i]+'</a></span>&nbsp&nbsp&nbsp';"+
		            "}"+
		            "obj+='</div>';"+
		            "obj+='<ul class=mcontentul>';"+
		            "for(var i=0;i<jsonobj.pageList.length;i++){"+
		            "if(i<5){"+
		            "if(jsonobj.pageList[i].time=='null'){"+
		            "time ='';"+
		            "}else{"+
		            "time = jsonobj.pageList[i].time;"+
		            "}"+
		            "obj+='<li><a href='+jsonobj.pageList[i].url+' target=_blank>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'</li>';"+
		            "}"+
		            "}"+
		            "obj+='</ul>';"+
		            "obj+='<div class=recommend><span>查看更多您关注的答案</span>';"+
		            "obj+='<ul>';"+
				    "for(var i=5;i<jsonobj.pageList.length;i++){"+
				    "if(jsonobj.pageList[i].time=='null'){"+
				    "time ='';"+
				    "}else{"+
				    "time = jsonobj.pageList[i].time;"+
				   // "var aa ='111 222';alert(aa.replace(/[ ]/g,''));"+
				    "}"+//\"" + jsonobj.pageList[i].contenttitle.replace(/<.*?>/ig,"")+ "\"
				    "obj+='<li><a target=_blank style=cursor:pointer onclick=javascript:linkMessage(\"'+jsonobj.pageList[i].contenttitle.replace(/<.*?>/ig,'').replace(/[ ]/g,'')+'\")>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'</li>';"+
				   // "obj+='<li><a onclick=javascript:linkMessage('+jsonobj.pageList[i].contenttitle.replace(/<.*?>/ig,'')+') target=_blank  style=cursor:pointer;>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'</li>'"; 
				    "}"+
				    "obj+='</ul>';"+
		            "obj+='</div>';"+
		            "obj+='</div>';"+
		            "console.log(obj);"+
		            //"alert('tabInfoDiv'+ab+'');"+
		            "document.getElementById('tabInfoDiv'+ab+'').innerHTML='';"+
		            //"document.getElementById('tabInfoDiv1').style.display='block';"+
		            "document.getElementById('tabInfoDiv'+ab+'').innerHTML=obj;"+
		            "}"+
		          "});"+
		          "}";
				columnStr +="<script>";
				columnStr += jquertStr;
				columnStr += "<"+"/script>";
				    columnStr += "<div>"+
					                "<div class='msg bot' id='answer_"+parseInt(messagenumcount-1)+"' style='display:none;'>"+
						               "<div class='webnavbox'>"+
				                       "<s>栏目导航：</s>";
						               if(jsonobj.tag1!=0){
						            	   for(var i=0;i<jsonobj.tag1.pageList[0].source.length;i++){
						            		   columnStr += "<a class='bread' href="+jsonobj.tag1.pageList[0].source[i].url+" title="+jsonobj.tag1.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag1.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
						            	   }
						               }else if(jsonobj.tag2!=0){
						            	   for(var i=0;i<jsonobj.tag2.pageList[0].source.length;i++){
						            		   columnStr += "<a class='bread' href="+jsonobj.tag2.pageList[0].source[i].url+" title="+jsonobj.tag2.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag2.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
						            	   }
						               }else if(jsonobj.tag3!=0){
						            	   for(var i=0;i<jsonobj.tag3.pageList[0].source.length;i++){
						            		   columnStr += "<a class='bread' href="+jsonobj.tag3.pageList[0].source[i].url+" title="+jsonobj.tag3.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag3.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
						            	   }
						               }else if(jsonobj.tag4!=0){
						            	   for(var i=0;i<jsonobj.tag4.pageList[0].source.length;i++){
						            		   columnStr += "<a class='bread' href="+jsonobj.tag4.pageList[0].source[i].url+" title="+jsonobj.tag4.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag4.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
						            	   }
						               }else if(jsonobj.tag5!=0){
						            	   for(var i=0;i<jsonobj.tag5.pageList[0].source.length;i++){
						            		   columnStr += "<a class='bread' href="+jsonobj.tag5.pageList[0].source[i].url+" title="+jsonobj.tag5.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag5.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
						            	   }
						               }else if(jsonobj.tag6!=0){
						            	   for(var i=0;i<jsonobj.tag6.pageList[0].source.length;i++){
						            		   columnStr += "<a class='bread' href="+jsonobj.tag6.pageList[0].source[i].url+" title="+jsonobj.tag6.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag6.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
						            	   }
						               }
					    columnStr +=    "</div>"+
					                      "<div class='m' style='background:#fff;'>"+
					                      "<div id='1' class='submain' style='display: block'>"+
					                      "<div class='submain_left' style='float: left; margin-top: 5px'>"+
					                      "<img border='0' src='"+jsonobj.tag7.leaderRootUrl+jsonobj.tag7.leaderPicture+"'>"+
					                      "</div>"+
					                      "<div class='submain_right' style='overflow: hidden;font-size:15px;'>";
					     columnStr +=     ""+jsonobj.tag7.leaderPosition+"";
					     columnStr +=     "<h5>【简历】</h5>";
					   //  columnStr +=     ""+jsonobj.tag6.leaderName+"";
					     columnStr +=     ""+jsonobj.tag7.leaderResume+"";
					     columnStr +=     ""+jsonobj.tag7.leaderResponsibility+""+
					                      "</div>"+
					                      "</div>"+
					                    /*  "<div class='tags'>"+
			                                "<s>标签：</s>";
			                               if(jsonobj.tag1!=0){
			                            	   for(var i=0;i<jsonobj.tag1.pageList[0].keywords.length;i++){
			                            		   if(i<4)
			                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag1.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
			                            	   }
			                               }else if(jsonobj.tag2!=0){
			                            	   for(var i=0;i<jsonobj.tag2.pageList[0].keywords.length;i++){
			                            		   if(i<4)
			                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag2.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
			                            	   }
			                               }else if(jsonobj.tag3!=0){
			                            	   for(var i=0;i<jsonobj.tag3.pageList[0].keywords.length;i++){
			                            		   if(i<4)
			                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag3.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
			                            	   }
			                               }else if(jsonobj.tag4!=0){
			                            	   for(var i=0;i<jsonobj.tag4.pageList[0].keywords.length;i++){
			                            		   if(i<4)
			                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag4.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
			                            	   }
			                               }else if(jsonobj.tag5!=0){
			                            	   for(var i=0;i<jsonobj.tag5.pageList[0].keywords.length;i++){
			                            		   if(i<4)
			                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag5.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
			                            	   }
			                               }else if(jsonobj.tag6!=0){
			                            	   for(var i=0;i<jsonobj.tag6.pageList[0].keywords.length;i++){
			                            		   if(i<4)
			                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag6.pageList[0].keywords[i]+"</a></span>"
			                            	   }
			                               }
			               columnStr +=    "</div>"+*/
							                "<div class='s'></div>"+
								                "<ul class='mtabs'>";
				                          if(jsonobj.tag1!=0){
				                        	  columnStr +=  "<li class='mtabli selected'  numid='0'>魅力锡城</li>";
				                          }
				                          if(jsonobj.tag2!=0){
				                        	  columnStr +=  "<li class='mtabli' numid='1'>锡城资讯</li>";
				                          }
				                          if(jsonobj.tag3!=0){
				                        	  columnStr +=  "<li class='mtabli' numid='2'>信息公开</li>";
				                          }
				                          if(jsonobj.tag4!=0){
				                        	  columnStr +=  "<li class='mtabli' numid='3'>公共服务</li>";
				                          }
				                          if(jsonobj.tag5!=0){
				                        	  columnStr +=  "<li class='mtabli' numid='4'>行政服务</li>";
				                          }
				                          if(jsonobj.tag6!=0){
				                        	  columnStr +=  "<li class='mtabli' numid='5'>政民互动</li>";
				                          }
							                      /*  "<li class='mtabli selected' numid='0'>魅力锡城</li>"+
							                        "<li class='mtabli' numid='1'>锡城资讯</li>"+
							                        "<li class='mtabli' numid='2'>信息公开</li>"+
							                        "<li class='mtabli' numid='3'>公共服务</li>"+
							                        "<li class='mtabli' nnumid='4'>行政服务</li>"+
							                       " <li class='mtabli' numid='5'>政民互动</li>"+*/
				               columnStr +=     "</ul>"+
				                                "<div class='mtabcontents' id='tabInfoDiv"+divNum+"'>"+
				                                "<div class='mcontent  selected'>"+
				                                "<div class='tags'>"+
				                                 "<s>标签：</s>";
				                                if(jsonobj.tag1!=0){
					                            	   for(var i=0;i<jsonobj.tag1.pageList[0].keywords.length;i++){
					                            		   if(i<4)
					                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag1.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
					                            	   }
					                               }else if(jsonobj.tag2!=0){
					                            	   for(var i=0;i<jsonobj.tag2.pageList[0].keywords.length;i++){
					                            		   if(i<4)
					                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag2.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
					                            	   }
					                               }else if(jsonobj.tag3!=0){
					                            	   for(var i=0;i<jsonobj.tag3.pageList[0].keywords.length;i++){
					                            		   if(i<4)
					                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag3.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
					                            	   }
					                               }else if(jsonobj.tag4!=0){
					                            	   for(var i=0;i<jsonobj.tag4.pageList[0].keywords.length;i++){
					                            		   if(i<4)
					                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag4.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
					                            	   }
					                               }else if(jsonobj.tag5!=0){
					                            	   for(var i=0;i<jsonobj.tag5.pageList[0].keywords.length;i++){
					                            		   if(i<4)
					                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag5.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
					                            	   }
					                               }else if(jsonobj.tag6!=0){
					                            	   for(var i=0;i<jsonobj.tag6.pageList[0].keywords.length;i++){
					                            		   if(i<4)
					                            		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag6.pageList[0].keywords[i]+"</a></span>"
					                            	   }
					                               }
				               columnStr +="</div>"+
				                                "<ul class='mcontentul'>";
				               if(jsonobj.tag1!=0){
				            	   for(var i=0;i<jsonobj.tag1.pageList.length;i++){
								    	if(i<5){
								    	      if(jsonobj.tag1.pageList[i].time=="null"){
								    	    	  time ="";
								    	      }else{
								    	    	  time = jsonobj.tag1.pageList[i].time;
								    	      }//target='_blank'
						                  columnStr   +=   "<li><a href="+jsonobj.tag1.pageList[i].url+" target='_blank'>"+jsonobj.tag1.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
								    	}
								    }
								   
	                 columnStr   +="</ul>"+
	                                "<div class='recommend' style='display:block;' id='answer_"+parseInt(messagenumcount-1)+"'> <span>查看更多您关注的答案</span>"+
	                                  "<ul>";
	                                  for(var i=5;i<jsonobj.tag1.pageList.length;i++){
									    	      if(jsonobj.tag1.pageList[i].time=="null"){
									    	    	  time ="";
									    	      }else{
									    	    	  time = jsonobj.tag1.pageList[i].time;
									    	      }//target='_blank'
							                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.tag1.pageList[i].contenttitle.replace(/<.*?>/ig,"").replace(/[ ]/g,'')+ "\")' style='cursor:pointer;'>"+jsonobj.tag1.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
									    }
				               }else if(jsonobj.tag2!=0){
				            	   for(var i=0;i<jsonobj.tag2.pageList.length;i++){
								    	if(i<5){
								    	      if(jsonobj.tag2.pageList[i].time=="null"){
								    	    	  time ="";
								    	      }else{
								    	    	  time = jsonobj.tag2.pageList[i].time;
								    	      }//target='_blank'
						                  columnStr   +=   "<li><a href="+jsonobj.tag2.pageList[i].url+" target='_blank'>"+jsonobj.tag2.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
								    	}
								    }
								   
	                 columnStr   +="</ul>"+
	                                "<div class='recommend' style='display:block;' id='answer_"+parseInt(messagenumcount-1)+"'> <span>查看更多您关注的答案</span>"+
	                                  "<ul>";
	                                  for(var i=5;i<jsonobj.tag2.pageList.length;i++){
									    	      if(jsonobj.tag2.pageList[i].time=="null"){
									    	    	  time ="";
									    	      }else{
									    	    	  time = jsonobj.tag2.pageList[i].time;
									    	      }//target='_blank'
							                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.tag2.pageList[i].contenttitle.replace(/<.*?>/ig,"").replace(/[ ]/g,'')+ "\")' style='cursor:pointer;'>"+jsonobj.tag2.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
									    }
				               }else if(jsonobj.tag3!=0){
				            	   for(var i=0;i<jsonobj.tag3.pageList.length;i++){
								    	if(i<5){
								    	      if(jsonobj.tag3.pageList[i].time=="null"){
								    	    	  time ="";
								    	      }else{
								    	    	  time = jsonobj.tag3.pageList[i].time;
								    	      }//target='_blank'
						                  columnStr   +=   "<li><a href="+jsonobj.tag3.pageList[i].url+" target='_blank'>"+jsonobj.tag3.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
								    	}
								    }
								   
	                 columnStr   +="</ul>"+
	                                "<div class='recommend' style='display:block;' id='answer_"+parseInt(messagenumcount-1)+"'> <span>查看更多您关注的答案</span>"+
	                                  "<ul>";
	                                  for(var i=5;i<jsonobj.tag3.pageList.length;i++){
									    	      if(jsonobj.tag3.pageList[i].time=="null"){
									    	    	  time ="";
									    	      }else{
									    	    	  time = jsonobj.tag3.pageList[i].time;
									    	      }//target='_blank'
							                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.tag3.pageList[i].contenttitle.replace(/<.*?>/ig,"").replace(/[ ]/g,'')+ "\")' style='cursor:pointer;'>"+jsonobj.tag3.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
									    }
				               }else if(jsonobj.tag4!=0){
				            	   for(var i=0;i<jsonobj.tag4.pageList.length;i++){
								    	if(i<5){
								    	      if(jsonobj.tag4.pageList[i].time=="null"){
								    	    	  time ="";
								    	      }else{
								    	    	  time = jsonobj.tag4.pageList[i].time;
								    	      }//target='_blank'
						                  columnStr   +=   "<li><a href="+jsonobj.tag4.pageList[i].url+" target='_blank'>"+jsonobj.tag4.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
								    	}
								    }
								   
	                 columnStr   +="</ul>"+
	                                "<div class='recommend' style='display:block;' id='answer_"+parseInt(messagenumcount-1)+"'> <span>查看更多您关注的答案</span>"+
	                                  "<ul>";
	                                  for(var i=5;i<jsonobj.tag4.pageList.length;i++){
									    	      if(jsonobj.tag4.pageList[i].time=="null"){
									    	    	  time ="";
									    	      }else{
									    	    	  time = jsonobj.tag4.pageList[i].time;
									    	      }//target='_blank'
							                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.tag4.pageList[i].contenttitle.replace(/<.*?>/ig,"").replace(/[ ]/g,'')+ "\")' style='cursor:pointer;'>"+jsonobj.tag4.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
									    }
				               }else if(jsonobj.tag5!=0){
				            	   for(var i=0;i<jsonobj.tag5.pageList.length;i++){
								    	if(i<5){
								    	      if(jsonobj.tag5.pageList[i].time=="null"){
								    	    	  time ="";
								    	      }else{
								    	    	  time = jsonobj.tag5.pageList[i].time;
								    	      }//target='_blank'
						                  columnStr   +=   "<li><a href="+jsonobj.tag5.pageList[i].url+" target='_blank'>"+jsonobj.tag5.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
								    	}
								    }
								   
	                 columnStr   +="</ul>"+
	                                "<div class='recommend' style='display:block;' id='answer_"+parseInt(messagenumcount-1)+"'> <span>查看更多您关注的答案</span>"+
	                                  "<ul>";
	                                  for(var i=5;i<jsonobj.tag5.pageList.length;i++){
									    	      if(jsonobj.tag5.pageList[i].time=="null"){
									    	    	  time ="";
									    	      }else{
									    	    	  time = jsonobj.tag5.pageList[i].time;
									    	      }//target='_blank'
							                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.tag5.pageList[i].contenttitle.replace(/<.*?>/ig,"").replace(/[ ]/g,'')+ "\")' style='cursor:pointer;'>"+jsonobj.tag5.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
									    }
				               }else if(jsonobj.tag6!=0){
				            	   for(var i=0;i<jsonobj.tag6.pageList.length;i++){
								    	if(i<5){
								    	      if(jsonobj.tag6.pageList[i].time=="null"){
								    	    	  time ="";
								    	      }else{
								    	    	  time = jsonobj.tag6.pageList[i].time;
								    	      }//target='_blank'
						                  columnStr   +=   "<li><a href="+jsonobj.tag6.pageList[i].url+" target='_blank'>"+jsonobj.tag6.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
								    	}
								    }
								   
	                 columnStr   +="</ul>"+
	                                "<div class='recommend' style='display:block;' id='answer_"+parseInt(messagenumcount-1)+"'> <span>查看更多您关注的答案</span>"+
	                                  "<ul>";
	                                  for(var i=5;i<jsonobj.tag6.pageList.length;i++){
									    	      if(jsonobj.tag6.pageList[i].time=="null"){
									    	    	  time ="";
									    	      }else{
									    	    	  time = jsonobj.tag6.pageList[i].time;
									    	      }//target='_blank'
							                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.tag6.pageList[i].contenttitle.replace(/<.*?>/ig,"").replace(/[ ]/g,'')+ "\")' style='cursor:pointer;'>"+jsonobj.tag5.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
									    }
				               }
											    
				               columnStr   +=    "</ul>"+
				                                "</div>"+
				                              "</div>"+
				                                "</div>"+
				                            "</div>"+
			                            "</div>"+
									"</div>"+
							    "</div>";
				div.innerHTML = columnStr;
				el.messages.append(div);
				processHtml($('#answer_'+mid,div));
				closeOldAnswer(mid)
			},
			complete:function(){
				//定位滚动条
				//_kscroll.resize();
				//_kscroll.scrollToElement($('#question_'+mid,el.messages));
			   positionScrollbar($('#question_'+mid,el.messages))
				el.textInput[0].focus();
			}
		});
	}
	
	
	function linkServer(question,extinfo){
		//alert('linkServer');
		divNum++;
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
		$.ajax({
			//url:'ask',
		    url:'../?method=nlp&nature=true',
			type:'post',
			dataType:'html',
		    data:'input='+question+'&&from=0&&size='+10+'&&tagType=first',
			cache:true,
//			data:senddata,
//			cache:false,
			success :function( html , textStatus , xhrObj) {
			    console.log(html);
			    var jsonobj=eval('('+html+')');
				var div = document.createElement('div');
				console.dir(jsonobj)
				var columnStr="";
				var jquertStr =  "$(function(){"+
		          "$('ul.mtabs>li').click(function(){"+
		          "$(this).addClass('selected').siblings('.mtabli').removeClass('selected');"+
		          "$(this).parent().next().children('.mcontent').removeClass('selected').eq($(this).index()).addClass('selected');"+
		          //"alert($(this).index());"+
		         // "aa($(this).index());"+
		          "var type=1;"+
			         // "alert($(this).text());"+
			          "if($(this).text()=='魅力锡城'){"+
			          "type=1;"+
			          "}else if($(this).text()=='锡城资讯'){"+
			          "type=2;"+
			          "}else if($(this).text()=='信息公开'){"+
			          "type=3;"+
			          "}else if($(this).text()=='公共服务'){"+
			          "type=4"+
			          "}else if($(this).text()=='行政服务'){"+
			          "type=5"+
			          "}else if($(this).text()=='政民互动'){"+
			          "type=6"+
			          "}"+
			          "bb(type);"+
		          "});"+ 
		          "});"+
		          "function bb(tagType){"+
		          "$.ajax({"+
		            "url:'../?method=nlp&nature=true'," +
		            "type:'post',"+
		            "dataType:'html',"+
		            "data:'input="+question+"&&from=0&&size='+10+'&&tagType=tag'+tagType+'',"+
		            "cache:true,"+//"+size+"
		            "success :function( html , textStatus , xhrObj) {"+
		            "var jsonobj=eval('('+html+')');"+
		            "console.dir(jsonobj);"+
		            "var obj='';"+
		            "var div = document.createElement('div');"+
		            "obj+='<div class=\"mcontent selected\">';"+
		            "obj+='<div class=tags>';"+
		            "obj +='<s>标签：</s>';"+
		            "for(var i=0;i<jsonobj.pageList[0].keywords.length;i++){"+
		            "	   if(i<4)"+
		            "	  obj += '<span class=tag><a href=# target=_blank>'+jsonobj.pageList[0].keywords[i]+'</a></span>&nbsp&nbsp&nbsp';"+
		            "}"+
		            "obj+='</div>';"+
		            "obj+='<ul class=mcontentul>';"+
                    "for(var i=0;i<jsonobj.pageList.length;i++){"+
                    "if(i<5){"+
		            "if(jsonobj.pageList[i].time=='null'){"+
		            "time ='';"+
		            "}else{"+
		            "time = jsonobj.pageList[i].time;"+
		            "}"+
		            "obj+='<li><a href='+jsonobj.pageList[i].url+' target=_blank>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'</li>';"+
		            "}"+
		            "}"+
		            "for(var i=5;i<jsonobj.pageList.length;i++){"+
		            "if(jsonobj.pageList[i].time=='null'){"+
		            "time ='';"+
		            "}else{"+
		            "time = jsonobj.pageList[i].time;"+
		            "}"+
		            "obj+='<li style=display:none;><a href='+jsonobj.pageList[i].url+' target=_blank>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'</li>';"+
		            "}"+
		            "obj+='</ul>';"+
		            "if(jsonobj.pageList.length>5){"+
		            "obj +=	'<a onclick=javascript:showText();  style=cursor:pointer;color:blue;margin-left: 20px;>更多'+parseInt(jsonobj.pageList.length-5)+'个答案</a>';"+
		            "}"+
		            "obj+='</div>';"+
		            "console.log(obj);"+
		            "document.getElementById('tabInfoDiv" + divNum+ "').innerHTML='';"+
		            "document.getElementById('tabInfoDiv" + divNum+ "').innerHTML=obj;"+
		            "}"+
		          "});"+
		          "}";
				columnStr +="<script>";
				columnStr += jquertStr;
				columnStr += "<"+"/script>";
				columnStr += "<div>"+
                "<div class='msg bot' id='ganswer_"+parseInt(messagenumcount-1)+"'>"+
                "<div class='webnavbox'>"+
                "<s>栏目导航：</s>";
	               if(jsonobj.tag1!=0){
	            	   for(var i=0;i<jsonobj.tag1.pageList[0].source.length;i++){
	            		   columnStr += "<a class='bread' href="+jsonobj.tag1.pageList[0].source[i].url+" title="+jsonobj.tag1.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag1.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
	            	   }
	               }else if(jsonobj.tag2!=0){
	            	   for(var i=0;i<jsonobj.tag2.pageList[0].source.length;i++){
	            		   columnStr += "<a class='bread' href="+jsonobj.tag2.pageList[0].source[i].url+" title="+jsonobj.tag2.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag2.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
	            	   }
	               }else if(jsonobj.tag3!=0){
	            	   for(var i=0;i<jsonobj.tag3.pageList[0].source.length;i++){
	            		   columnStr += "<a class='bread' href="+jsonobj.tag3.pageList[0].source[i].url+" title="+jsonobj.tag3.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag3.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
	            	   }
	               }else if(jsonobj.tag4!=0){
	            	   for(var i=0;i<jsonobj.tag4.pageList[0].source.length;i++){
	            		   columnStr += "<a class='bread' href="+jsonobj.tag4.pageList[0].source[i].url+" title="+jsonobj.tag4.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag4.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
	            	   }
	               }else if(jsonobj.tag5!=0){
	            	   for(var i=0;i<jsonobj.tag5.pageList[0].source.length;i++){
	            		   columnStr += "<a class='bread' href="+jsonobj.tag5.pageList[0].source[i].url+" title="+jsonobj.tag5.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag5.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
	            	   }
	               }else if(jsonobj.tag6!=0){
	            	   for(var i=0;i<jsonobj.tag6.pageList[0].source.length;i++){
	            		   columnStr += "<a class='bread' href="+jsonobj.tag6.pageList[0].source[i].url+" title="+jsonobj.tag6.pageList[0].source[i].title+" target='_blank'>"+jsonobj.tag6.pageList[0].source[i].title+"</a>&nbsp&nbsp&nbsp";
	            	   }
	               }
 columnStr +=    "</div>"+
	                "<div class='m' style='background:#fff;'>"+
                    "<div>";
					   if(jsonobj.tag1!=0){
						      if(jsonobj.tag1.pageList[0].time=="null"){
						    	  time ="";
						      }else{
						    	  time = jsonobj.tag1.pageList[0].time;
						      }// target='_blank'
					   columnStr   +=   "<li style='color:BB;'>"+jsonobj.tag1.pageList[0].content.replace(/<.*?>/ig,"").substr(0,150)+"<a href="+jsonobj.tag1.pageList[0].url+" target='_blank' style='color:red;'>[查看全文]</a>&nbsp;"+time+"</li>"; 
						}else if(jsonobj.tag2!=0){
							 if(jsonobj.tag2.pageList[0].time=="null"){
						    	  time ="";
						      }else{
						    	  time = jsonobj.tag2.pageList[0].time;
						      }// target='_blank'
					  columnStr   +=   "<li style='color:BB;'>"+jsonobj.tag2.pageList[0].content.replace(/<.*?>/ig,"").substr(0,150)+"<a href="+jsonobj.tag2.pageList[0].url+" target='_blank' style='color:red;'>[查看全文]</a>&nbsp;"+time+"</li>"; 
						}else if(jsonobj.tag3!=0){
							 if(jsonobj.tag3.pageList[0].time=="null"){
						    	  time ="";
						      }else{
						    	  time = jsonobj.tag3.pageList[0].time;
						      }// target='_blank'
					  columnStr   +=   "<li style='color:BB;'>"+jsonobj.tag3.pageList[0].content.replace(/<.*?>/ig,"").substr(0,150)+"<a href="+jsonobj.tag3.pageList[0].url+" target='_blank' style='color:red;'>[查看全文]</a>&nbsp;"+time+"</li>"; 
						}else if(jsonobj.tag4!=0){
							 if(jsonobj.tag4.pageList[0].time=="null"){
						    	  time ="";
						      }else{
						    	  time = jsonobj.tag4.pageList[0].time;
						      }// target='_blank'
					  columnStr   +=   "<li style='color:BB;'>"+jsonobj.tag4.pageList[0].content.replace(/<.*?>/ig,"").substr(0,150)+"<a href="+jsonobj.tag4.pageList[0].url+" target='_blank' style='color:red;'>[查看全文]</a>&nbsp;"+time+"</li>"; 
						}else if(jsonobj.tag5!=0){
							 if(jsonobj.tag5.pageList[0].time=="null"){
						    	  time ="";
						      }else{
						    	  time = jsonobj.tag5.pageList[0].time;
						      }// target='_blank'
					  columnStr   +=   "<li style='color:BB;'>"+jsonobj.tag5.pageList[0].content.replace(/<.*?>/ig,"").substr(0,150)+"<a href="+jsonobj.tag5.pageList[0].url+" target='_blank' style='color:red;'>[查看全文]</a>&nbsp;"+time+"</li>"; 
						}else if(jsonobj.tag6!=0){
							 if(jsonobj.tag6.pageList[0].time=="null"){
						    	  time ="";
						      }else{
						    	  time = jsonobj.tag6.pageList[0].time;
						      }// target='_blank'
					  columnStr   +=   "<li style='color:BB;'>"+jsonobj.tag6.pageList[0].content.replace(/<.*?>/ig,"").substr(0,150)+"<a href="+jsonobj.tag6.pageList[0].url+" target='_blank' style='color:red;'>[查看全文]</a>&nbsp;"+time+"</li>"; 
						}
   columnStr   +=     "</div>"+
		                "<div class='s'></div>"+
		                "<div class='recommend'> <span>查看更多您关注的答案</span>"+
		                "<ul class='mcontentul'>"+
		                "</ul>" +
		                "</div>";
		                columnStr +=    "<ul class='mtabs'>";
		                	if(jsonobj.tag1!=0){
	                        	  columnStr +=  "<li class='mtabli selected'  numid='0'>魅力锡城</li>";
	                          }
	                          if(jsonobj.tag2!=0){
	                        	  columnStr +=  "<li class='mtabli' numid='1'>锡城资讯</li>";
	                          }
	                          if(jsonobj.tag3!=0){
	                        	  columnStr +=  "<li class='mtabli' numid='2'>信息公开</li>";
	                          }
	                          if(jsonobj.tag4!=0){
	                        	  columnStr +=  "<li class='mtabli' numid='3'>公共服务</li>";
	                          }
	                          if(jsonobj.tag5!=0){
	                        	  columnStr +=  "<li class='mtabli' numid='4'>行政服务</li>";
	                          }
	                          if(jsonobj.tag6!=0){
	                        	  columnStr +=  "<li class='mtabli' numid='5'>政民互动</li>";
	                          }
		                       /* "<li class='mtabli selected' numid='0'>魅力锡城</li>"+
		                        "<li class='mtabli' numid='1'>锡城资讯</li>"+
		                        "<li class='mtabli' numid='2'>信息公开</li>"+
		                        "<li class='mtabli' numid='3'>公共服务</li>"+
		                        "<li class='mtabli' nnumid='4'>行政服务</li>"+
		                       " <li class='mtabli' numid='5'>政民互动</li>"+*/
	           columnStr += "</ul>"+
                            "<div class='mtabcontents' id='tabInfoDiv" + divNum+ "'>"+
                            "<div class='mcontent  selected'>"+
                            "<div class='tags'>"+
                            "<s>标签：</s>";
                           if(jsonobj.tag1!=0){
                           	   for(var i=0;i<jsonobj.tag1.pageList[0].keywords.length;i++){
                           		   if(i<4)
                           		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag1.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
                           	   }
                              }else if(jsonobj.tag2!=0){
                           	   for(var i=0;i<jsonobj.tag2.pageList[0].keywords.length;i++){
                           		   if(i<4)
                           		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag2.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
                           	   }
                              }else if(jsonobj.tag3!=0){
                           	   for(var i=0;i<jsonobj.tag3.pageList[0].keywords.length;i++){
                           		   if(i<4)
                           		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag3.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
                           	   }
                              }else if(jsonobj.tag4!=0){
                           	   for(var i=0;i<jsonobj.tag4.pageList[0].keywords.length;i++){
                           		   if(i<4)
                           		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag4.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
                           	   }
                              }else if(jsonobj.tag5!=0){
                           	   for(var i=0;i<jsonobj.tag5.pageList[0].keywords.length;i++){
                           		   if(i<4)
                           		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag5.pageList[0].keywords[i]+"</a></span>&nbsp&nbsp&nbsp"
                           	   }
                              }else if(jsonobj.tag6!=0){
                           	   for(var i=0;i<jsonobj.tag6.pageList[0].keywords.length;i++){
                           		   if(i<4)
                           		   columnStr += "<span class='tag'><a href='#' target='_blank'>"+jsonobj.tag6.pageList[0].keywords[i]+"</a></span>"
                           	   }
                              }
          columnStr +="</div>"+
                            "<ul class='mcontentul'>";
	           if(jsonobj.tag1!=0){
	        	   for(var i=0;i<jsonobj.tag1.pageList.length;i++){
				    	if(i<5){
				    	      if(jsonobj.tag1.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag1.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li><a href="+jsonobj.tag1.pageList[i].url+" target='_blank'>"+jsonobj.tag1.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    	}
				    }
				    for(var i=5;i<jsonobj.tag1.pageList.length;i++){
				    	      if(jsonobj.tag1.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag1.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li style='display:none;'><a href="+jsonobj.tag1.pageList[i].url+" target='_blank'>"+jsonobj.tag1.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    }
               }else if(jsonobj.tag2!=0){
            	   for(var i=0;i<jsonobj.tag2.pageList.length;i++){
				    	if(i<5){
				    	      if(jsonobj.tag2.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag2.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li><a href="+jsonobj.tag2.pageList[i].url+" target='_blank'>"+jsonobj.tag2.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    	}
				    }
				    for(var i=5;i<jsonobj.tag2.pageList.length;i++){
				    	      if(jsonobj.tag2.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag2.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li style='display:none;'><a href="+jsonobj.tag2.pageList[i].url+" target='_blank'>"+jsonobj.tag2.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    }
               }else if(jsonobj.tag3!=0){
            	   for(var i=0;i<jsonobj.tag3.pageList.length;i++){
				    	if(i<5){
				    	      if(jsonobj.tag3.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag3.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li><a href="+jsonobj.tag3.pageList[i].url+" target='_blank'>"+jsonobj.tag3.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    	}
				    }
				    for(var i=5;i<jsonobj.tag3.pageList.length;i++){
				    	      if(jsonobj.tag3.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag3.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li style='display:none;'><a href="+jsonobj.tag3.pageList[i].url+" target='_blank'>"+jsonobj.tag3.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    }
               }else if(jsonobj.tag4!=0){
            	   for(var i=0;i<jsonobj.tag4.pageList.length;i++){
				    	if(i<5){
				    	      if(jsonobj.tag4.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag4.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li><a href="+jsonobj.tag4.pageList[i].url+" target='_blank'>"+jsonobj.tag4.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    	}
				    }
				    for(var i=5;i<jsonobj.tag4.pageList.length;i++){
				    	      if(jsonobj.tag4.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag4.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li style='display:none;'><a href="+jsonobj.tag4.pageList[i].url+" target='_blank'>"+jsonobj.tag4.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    }
               }else if(jsonobj.tag5!=0){
            	   for(var i=0;i<jsonobj.tag5.pageList.length;i++){
				    	if(i<5){
				    	      if(jsonobj.tag5.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag5.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li><a href="+jsonobj.tag5.pageList[i].url+" target='_blank'>"+jsonobj.tag5.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    	}
				    }
				    for(var i=5;i<jsonobj.tag5.pageList.length;i++){
				    	      if(jsonobj.tag5.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag5.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li style='display:none;'><a href="+jsonobj.tag5.pageList[i].url+" target='_blank'>"+jsonobj.tag5.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    }
               }else if(jsonobj.tag6!=0){
            	   for(var i=0;i<jsonobj.tag6.pageList.length;i++){
				    	if(i<5){
				    	      if(jsonobj.tag6.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag6.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li><a href="+jsonobj.tag6.pageList[i].url+" target='_blank'>"+jsonobj.tag6.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    	}
				    }
				    for(var i=5;i<jsonobj.tag6.pageList.length;i++){
				    	      if(jsonobj.tag6.pageList[i].time=="null"){
				    	    	  time ="";
				    	      }else{
				    	    	  time = jsonobj.tag6.pageList[i].time;
				    	      }//target='_blank'
		                  columnStr   +=   "<li style='display:none;'><a href="+jsonobj.tag6.pageList[i].url+" target='_blank'>"+jsonobj.tag6.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
				    }
               }
						   
             columnStr   +="</ul>";
             if(jsonobj.tag1!=0){
            	 if(jsonobj.tag1.pageList.length>5){
                     columnStr   +=	 "<a onclick='javascript:showText();'  style='cursor: pointer;color:blue;margin-left: 20px;'>更多"+parseInt(jsonobj.tag1.pageList.length-5)+"个答案</a>";
                     }
             }else if(jsonobj.tag2!=0){
            	 if(jsonobj.tag2.pageList.length>5){
                     columnStr   +=	 "<a onclick='javascript:showText();'  style='cursor: pointer;color:blue;margin-left: 20px;'>更多"+parseInt(jsonobj.tag2.pageList.length-5)+"个答案</a>";
                     }
             }else if(jsonobj.tag3!=0){
            	 if(jsonobj.tag3.pageList.length>5){
                     columnStr   +=	 "<a onclick='javascript:showText();'  style='cursor: pointer;color:blue;margin-left: 20px;'>更多"+parseInt(jsonobj.tag3.pageList.length-5)+"个答案</a>";
                     }
             }else if(jsonobj.tag4!=0){
            	 if(jsonobj.tag4.pageList.length>5){
                     columnStr   +=	 "<a onclick='javascript:showText();'  style='cursor: pointer;color:blue;margin-left: 20px;'>更多"+parseInt(jsonobj.tag4.pageList.length-5)+"个答案</a>";
                     }
             }else if(jsonobj.tag5!=0){
            	 if(jsonobj.tag5.pageList.length>5){
                     columnStr   +=	 "<a onclick='javascript:showText();'  style='cursor: pointer;color:blue;margin-left: 20px;'>更多"+parseInt(jsonobj.tag5.pageList.length-5)+"个答案</a>";
                     }
             }else if(jsonobj.tag6!=0){
            	 if(jsonobj.tag6.pageList.length>5){
                     columnStr   +=	 "<a onclick='javascript:showText();'  style='cursor: pointer;color:blue;margin-left: 20px;'>更多"+parseInt(jsonobj.tag6.pageList.length-5)+"个答案</a>";
                     }
             }
                           /* "<div class='recommend'> <span>查看更多您关注的答案</span>"+
                              "<ul>";
                              for(var i=5;i<jsonobj.pageList.length;i++){
							    	      if(jsonobj.pageList[i].time=="null"){
							    	    	  time ="";
							    	      }else{
							    	    	  time = jsonobj.pageList[i].time;
							    	      }//target='_blank'
					                  columnStr   +=   "<li><a onclick='javascript:linkMessage(\"" + jsonobj.pageList[i].contenttitle.replace(/<.*?>/ig,"")+ "\");' style='cursor:pointer;'>"+jsonobj.pageList[i].contenttitle+"</a>&nbsp;"+time+"</li>"; 
							    }
           columnStr   +=    "</ul>"+
                            "</div>"+*/
                          "</div>"+
                            "</div>"+
                        "</div>"+
                    "</div>"+
				"</div>"+
		    "</div>";
            div.innerHTML = columnStr;
			el.messages.append(div);
			processHtml($('#answer_'+mid,div));
			closeOldAnswer(mid)
			},
			complete:function(){
				//定位滚动条
				//_kscroll.resize();
				//_kscroll.scrollToElement($('#question_'+mid,el.messages));
			   positionScrollbar($('#question_'+mid,el.messages))
				el.textInput[0].focus();
			}
		});
	}
	
	window.askServer = askServer;
	window.linkServer = linkServer;
	
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
	window.showText = showText;
	
	function closeOldAnswer(mid){
		var bots = $('.bot',el.messages);
		var len = bots.length;
		console.dir(bots);
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
	
//		data:senddata,
	//定义投票"有用/无用"函数,html中调用
    window.wentifankui=function(question,yorn){
    	/*alert(ILData[0]);
    	return;*/
		var formdata={};
		formdata.q=question;
		formdata.help=yorn;
		$.ajax({
			url:'../?method=help&nature=true',
			type:'post',
			dataType:'html',
			data:'input='+question+'&&ip=192.168.1.87&&ifHelp='+yorn+'',
			cache:true,
			success :function( data , textStatus , xhrObj){
				alert('感谢您的反馈');
			}
    	});
	}
});

