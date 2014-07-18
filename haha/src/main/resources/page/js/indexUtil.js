/**
 * 参数注释
 * v:输入框关键字
 * f:记录是哪个栏目(比如tag1代表锡城资讯,tag2信息公开tag3行政服务,tag4政民互动,tag5代表锡城资讯,其他)
 * data:ajax请求后返回来的json数据
 * flag:记录跳转页
 * n:选择的页数(比如选择页码2或是页码3....这个页码2或是页码3就是n)
 * c:记录当前页(比如从当前页第2页跳转到第3页,这个第2页就是c)
 * t:控制每页显示多少条标记位
 */
var size=8;//每页显示多少数据
/**
 * 获取CSS标签
 * @return
 */
function getCss(){  
	  var styleStr ="<style type='text/css'>"+
					".order_box .stitle { "+
					"width: 825px;"+
					"clear: right;"+
					"height: 27px;"+
					"border-bottom: 2px solid #A10000;}"+
					".order_box .stitle .close {"+
					"width: 80px;"+
					"height: 18px;"+
					"border-top: 1px solid #dedede;"+
					"border-left: 1px solid #dedede;"+
					"border-right: 1px solid #dedede;"+
					"background: #f1f1f1;"+
					"color: #000;"+
					"text-align: center;"+
					"float: left;"+
					"margin-right: 5px;"+
					"padding-top: 8px;}"+
					".order_box .stitle .open {"+
					"width: 82px;"+
					"height: 20px;"+
					"background: #A10000;"+
					"color: #fff;"+
					"text-align: center;"+
					"float: left;"+
					"margin-right: 5px;"+
					"padding-top: 8px;"+
					"overflow: hidden;}"+
					".order_box ul li {"+
					"cursor: pointer;"+
					"display: list-item;"+
					"list-style:none;"+
				"</style>";
			return styleStr;
    }

/**
 * 获取<li>标签
 * @param url
 * @param value
 * @param time
 * @return
 */
function getLiTarGet(url,value,time,content) {
	var liStr = "<li><a href="+url+">"+value+"</a>&nbsp;"+time+"<br>"+content+"</li><br>";
	return liStr;
}

/**
 * 获取栏目div
 * @param htmlStr
 * @return
 */
function getColumn(htmlStr){ 
    var columnStr = "<div class='order_box'>"+
					"<div class='stitle'>"+
						"<ul>"+
						"<li class='open'>魅力锡城</li>"+
						"<li class='close'>锡城资讯</li>"+
						"<li class='close'>信息公开 </li>"+
						"<li class='close'>公共服务</li>"+
						"<li class='close'>行政服务</li>"+
						"<li class='close'>政民互动</li>"+
						"</ul>"+
					"</div>"+
                       "<div class='cntorder' id='tabInfoDiv'>"+htmlStr+"</div>"+
					"<div class='cntorder' id='publicDiv' style='display: none;'></div>"+
					"<div class='cntorder' id='serverDiv' style='display: none;'></div>"+
					"<div class='cntorder' id='interactionDiv' style='display: none;'></div>"+
					"<div class='cntorder' id='ontherDiv' style='display: none;'></div>"+
					"</div> ";
 return columnStr;
}

/**
 * 查询后初始化界面
 * @param v
 * @param data
 * @return
 */
function initPage(v,data){ 
    var intiUrl = '';
    var jsonobj=eval('('+data+')');
    var strAry = new Array();
    var htmlStr='';
    var pageStr='';//然后其他的地方columnUrl,pagination
       clearCache(null);//初始化frame
       var time;
       for(var i=0;i<jsonobj.pageList.length;i++){
    	      if(jsonobj.pageList[i].time=="null"){
    	    	  time ="";
    	      }else{
    	    	  time = jsonobj.pageList[i].time;
    	      }
	   intiUrl +=  getLiTarGet(jsonobj.pageList[i].url,jsonobj.pageList[i].contenttitle,time,jsonobj.pageList[i].content);
          }
       intiUrl += "<div style=width:600px;text-align:center;>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	      for (var i = 1; i <= Math.ceil(jsonobj.count/size); i++){
	
	                   if (i <= size){
	                   if(i==1||i==jsonobj.from/size){
	                       var  pg = "<Font color=black>" + i + "</font>";
	                     }else{
	                       var  pg = "<Font color=red>" + i + "</font>";
	                     }
	                     
	                     intiUrl += "<a style='cursor:pointer' onclick='javascript:pageT(\"" + v+ "\","+i+","+jsonobj.from+",0);'>["+pg+"]</a>&nbsp;";
	                   }
	           }
         if(jsonobj.from/size>size){
            intiUrl += "<span id='page_next' style='cursor:pointer' onclick='pages_next(\"" + v+ "\",0,0)'>下一页>></span>"+
                  "</div>";
         }
	   return intiUrl;
}   

/**
 * 引入js标签
 * @return
 */
function importJs(){
    var jsStr = "<script src='js/jquery.min.js'><"+"/script>"+
				 "<script src='js/jquery.autocomplete.min.js'><"+"/script> "+
				 "<script src='js/indexUtil.js'><"+"/script>";
		return jsStr;
  }  
  
/**
 * jquery实现切换栏目
 * @param v
 * @return
 */
function jqueryColumn(v){
    var jquertStr =  "$(function () {"+
		          "$('.stitle li').click(function () {"+
		          " var tabFlag='';var index_tab = $(this).parent().children().index($(this));"+
		          "tabFlag = index_tab;"+
		          "$(this).parent().find('.open').removeClass('open').addClass('close'); "+
		          "$(this).removeClass('close').addClass('open');"+
		          "var content_obj = $('.cntorder');"+
		          "content_obj.hide();"+
		          "content_obj.eq(index_tab).show();"+
		          "columnPage(\"" + v+ "\",tabFlag,document.getElementById('tabInfoDiv'));"+
		          "});"+ 
		          "});";
		return 	jquertStr;		          
  }
 /**
  * 切换栏目,内容填充
  * @param v
  * @return
  */
function columnUrl(v){
    var columnStr = "function columnPage(v,flag,div){"+
			           "$.ajax({"+
			           "type:'POST',"+ 
                         "url:'../?method=mypage&nature=true',"+
                         "data:'input='+v+'&&from=0&&size="+size+"&&tagType=tag'+parseInt(flag+1)+'',"+
                         "success : function(data) {"+
                         "var obj='';var time;"+
                          "var jsonobj=eval('('+data+')');"+
                          "for(var i=0;i<jsonobj.pageList.length;i++){"+
                          "if(jsonobj.pageList[i].time=='null'){"+
                	      "	  time ='';"+
                	      "	}else{"+
                	      "  time = jsonobj.pageList[i].time;"+
                	      "}"+
                           "obj+='<li><a href='+jsonobj.pageList[i].url+'>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'<br>'+jsonobj.pageList[i].content+'</li><br>';"+
                          "}"+
                          "obj+='<span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>';"+
                          "for (var i = 1; i <= Math.ceil(jsonobj.count/"+size+"); i++){"+
                          " if (i <= "+size+"){"+
                          "if(i==1||i==parseInt(jsonobj.from/"+size+")){"+
                          "var  pg = '<Font color=black>' + i + '</font>';"+
                          "}else{"+
                          "var pg = '<Font color=red>' + i + '</font>';"+
                        "}"+
                       "obj+='<a style=cursor:pointer onclick=javascript:pageT(\"" + v+ "\",'+i+','+jsonobj.from+','+flag+');>['+pg+']</a>&nbsp;'"+
                        "}"+
                        "}"+
                         "if(jsonobj.from/"+size+">"+size+"){"+
			             "obj+='<span style=cursor:pointer onclick=pages_next(\"" + v+ "\",'+currentPage+','+flag+')>下一页>></span>';"+
			            "}"+
			            "document.getElementById('tabInfoDiv').style.display='none';"+
			            "document.getElementById('tabInfoDiv').innerHTML='';"+
			            "div.style.display='block';"+
			            "div.innerHTML=obj;"+
                          "}"+
			           "});"+
			          "}";
	   return columnStr;
  }
 
 /**
  * 分页:上一页,下一页,页码
  * @param v
  * @return
  */
function pagination(v){
    var pagStr = "function pageT(v,n,c,f){"+
		           "$.ajax({"+
		           "type:'POST',"+ 
                        "url:'../?method=mypage&nature=true',"+
                        "data:'input='+v+'&&from='+parseInt(n-1)*"+size+"+'&&size="+size+"&&tagType=tag'+parseInt(f+1)+'',"+
                        "success : function(data) {"+
                        " var obj='';var time;var t=1;var flag= '';document.getElementById('tabInfoDiv').innerHTML='';"+
                        "var jsonobj=eval('('+data+')');"+
                        "var currentPage =jsonobj.from;"+
                        "for(var i=0;i<jsonobj.pageList.length;i++){"+
                        "if(jsonobj.pageList[i].time=='null'){"+
              	      "	  time ='';"+
              	      "	}else{"+
              	      "  time = jsonobj.pageList[i].time;"+
              	      "}"+
		    		"obj+='<li><a href='+jsonobj.pageList[i].url+'>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'<br>'+jsonobj.pageList[i].content+'</li><br>';"+
		            "}"+
		            "obj+='<span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>';"+
		            "if(jsonobj.from!=0){"+
		            "obj+='<span style=cursor:pointer; onclick=pages_prev(\"" + v+ "\",'+currentPage+','+f+')><<上一页&nbsp;</span>';"+
		            "}"+
		            "if(c<n){"+
		            "if(c==0){"+
		            "flag=parseInt(n-c-1)"+
		            "}else if(c==1){"+
		            "flag=parseInt(n-c+2)"+
		            "}else if(c==2){"+
		             "flag=parseInt(n-c+1)"+
		            "}else{"+
		            "flag=parseInt(n-1)}"+
		            "}else{"+
		            "if(n==2){"+
		            "flag=1"+
		            "}else if(n==1){"+
		            "flag=0"+
		            "}else{"+
		            "flag=n"+
		            "}"+
		            "}"+
		            "if(n>=12){"+
		            "t=parseInt(n-10)"+
		            "}else{"+
		            "t=1"+
		            "}"+
		            "for (var i=t; i <= Math.ceil(jsonobj.count/"+size+"); i++){"+
		            "if (i <= parseInt(flag+10)){"+
		             " if(i==1||i==parseInt(jsonobj.from+"+size+")/"+size+"){"+
                         " var  pg = '<Font color=black>' + i + '</font>';"+
                        "}else{"+
                        "  var  pg = '<Font color=red>' + i + '</font>';"+
                        "}"+
                        
                        "obj+='<a style=cursor:pointer onclick=javascript:pageT(\"" + v+ "\",'+i+','+(jsonobj.from+"+size+")/"+size+"+','+f+');>['+pg+']</a>&nbsp;'"+
		            "}"+
		            "}"+
		            "if(n!=Math.ceil(jsonobj.count/"+size+")){"+
		             "obj+='<span style=cursor:pointer onclick=pages_next(\"" + v+ "\",'+currentPage+','+f+')>下一页>></span>';"+
		            "}"+
		            "document.getElementById('tabInfoDiv').innerHTML=obj"+
                        "}"+
		           "});"+
				  "}"+
				   "function pages_next(v,n,f){"+
				   "$.ajax({"+
		           "type:'POST',"+ 
                        "url:'../?method=mypage&nature=true',"+
                        "data:'input='+v+'&&from='+parseInt(n+"+size+")+'&&size="+size+"&&tagType=tag'+parseInt(f+1)+'',"+
                        "success : function(data) {"+
                        " var obj='';var time;var t=1;var flag= '';document.getElementById('tabInfoDiv').innerHTML='';"+
                        "var jsonobj=eval('('+data+')');"+
                        "var currentPage =jsonobj.from;"+
                        "for(var i=0;i<jsonobj.pageList.length;i++){"+
                        "if(jsonobj.pageList[i].time=='null'){"+
              	      "	  time ='';"+
              	      "	}else{"+
              	      "  time = jsonobj.pageList[i].time;"+
              	      "}"+
		    		"obj+='<li><a href='+jsonobj.pageList[i].url+'>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'<br>'+jsonobj.pageList[i].content+'</li><br>';"+
		            "}"+
		            "obj+='<span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>';"+
		            "obj+='<span style=cursor:pointer; onclick=pages_prev(\"" + v+ "\",'+currentPage+','+f+')><<上一页&nbsp;</span>';"+
		            "flag=parseInt((n/"+size+")+1);"+
		            "if(flag>=11){"+
		            "t=flag-9"+
		            "}else{"+
		            "t=1"+
		            "}"+
		            "for (var i = t; i <= Math.ceil(jsonobj.count/"+size+"); i++){"+
		            "if (i <= parseInt(flag+10)){"+
		             " if(i==t||i==parseInt(jsonobj.from+"+size+")/"+size+"){"+
                         " var  pg = '<Font color=black>' + i + '</font>';"+
                        "}else{"+
                        "  var  pg = '<Font color=red>' + i + '</font>';"+
                        "}"+
                        
                        "obj+='<a style=cursor:pointer onclick=javascript:pageT(\"" + v+ "\",'+i+','+f+');>['+pg+']</a>&nbsp;'"+
		            "}"+
		            "}"+
		            "obj+='<span style=cursor:pointer onclick=pages_next(\"" + v+ "\",'+currentPage+','+f+')>下一页>></span>';"+
		            "document.getElementById('tabInfoDiv').innerHTML=obj"+
                        "}"+
		           "});"+
				   "}"+
				   "function pages_prev(v,n,f){"+
				   "$.ajax({"+
		           "type:'POST',"+ 
                        "url:'../?method=mypage&nature=true',"+
                        "data:'input='+v+'&&from='+parseInt(n-"+size+")+'&&size="+size+"&&tagType=tag'+parseInt(f+1)+'',"+
                        "success : function(data) {"+
                        " var obj='';var time;var t=1;var flag= '';document.getElementById('tabInfoDiv').innerHTML='';"+
                        "var jsonobj=eval('('+data+')');"+
                        "var currentPage =jsonobj.from;"+
                        "for(var i=0;i<jsonobj.pageList.length;i++){"+
                        "if(jsonobj.pageList[i].time=='null'){"+
              	      "	  time ='';"+
              	      "	}else{"+
              	      "  time = jsonobj.pageList[i].time;"+
              	      "}"+
		    		"obj+='<li><a href='+jsonobj.pageList[i].url+'>'+jsonobj.pageList[i].contenttitle+'</a>&nbsp;'+time+'<br>'+jsonobj.pageList[i].content+'</li><br>';"+
		            "}"+
		            "obj+='<span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>';"+
		            "if(jsonobj.from!=0){"+
		            "obj+='<span style=cursor:pointer; onclick=pages_prev(\"" + v+ "\",'+currentPage+','+f+')><<上一页&nbsp;</span>';"+
		            "}"+
		            "flag=parseInt((n/"+size+"));"+
		            "if(flag<11){"+
		            "t=1"+
		            "}else{"+
		            "t=parseInt(flag-10)"+
		            "}"+
		            "for (var i = t; i <= Math.ceil(jsonobj.count/"+size+"); i++){"+
		            "if (i <= parseInt(flag+9)){"+
		             " if(i==1||i==parseInt(jsonobj.from+"+size+")/"+size+"){"+
                         " var  pg = '<Font color=black>' + i + '</font>';"+
                        "}else{"+
                        "  var  pg = '<Font color=red>' + i + '</font>';"+
                        "}"+
                        
                        "obj+='<a style=cursor:pointer onclick=javascript:pageT(\"" + v+ "\",'+i+','+f+');>['+pg+']</a>&nbsp;'"+
		            "}"+
		            "}"+
		            "obj+='<span style=cursor:pointer onclick=pages_next(\"" + v+ "\",'+currentPage+','+f+')>下一页>></span>';"+
		            "document.getElementById('tabInfoDiv').innerHTML=obj"+
                        "}"+
		           "});"+
				   "}";
	  return pagStr;
  }
  
 /**
  * 初始化frame
  * @param tabStr
  * @return
  */
function clearCache(tabStr){
	     
      window.frames["NLP"].document.open(); 
      
      if(tabStr!=null||tabStr!="")
       window.frames["NLP"].document.write(tabStr);
      else
      window.frames["NLP"].document.write("");
      
      window.frames["NLP"].document.close();
 }
