// function OnInput (event) { 
//     $.ajax({ 
// 	type:"POST", 
// 	url:'../?method=base&nature=true',
// 	data:'input='+event.target.value,
// 	async: true,
// 	cache: false,
// 	success : function(data) {
// 	    // parse result json string
// 	    var jsonobj=eval('('+data+')');
// 	    var dataArray = new Array();
// 	    dataArray = jsonobj.suggestions;
// 	    $('#input').AutoComplete({
// 		'data': dataArray,
// 		'itemHeight': 10,
// 		"listDirection":"up",
// 		'width': "auto",
// 		'term':jsonobj.term
// 	    }).AutoComplete('show');
// 	}
//     });
    
// }
// //搜索按钮ajax请求返回结果
// function skipUrl (v) { 

//     $.ajax({ 
// 	type:"POST", 
// 	url:'../?method=mypage&nature=true',
// 	data:'input='+v+'&&from=0&&size='+size+'&&tagType=tag1',
// 	success : function(data) {
// 	    var tabStr ='';
	    
// 	    htmlStr =initPage(v,data);//查询后初始化界面
	    
// 	    tabStr += getCss();//获取css标签
	    
// 	    tabStr +=importJs();//引入js标签
	    
// 	    tabStr +="<script>";//添加script标签
	    
// 	    tabStr += jqueryColumn(v);//jquery实现切换栏目
	    
// 	    tabStr += columnUrl(v);//切换栏目,内容填充
	    
// 	    tabStr += pagination(v);//分页:上一页,下一页,页码
	    
// 	    tabStr += "<"+"/script>";//结束script标签
	    
// 	    tabStr += getColumn(htmlStr);//获取栏目标签
	    
// 	    clearCache(tabStr); //frame里面填充内容
// 	}
//     });
// }

// $(document).ready(
//     function() {   
// 	$('#segButton').click(function() {//点击搜索按钮执行的操作
// 	    skipUrl(document.getElementById('input').value);//执行查询的方法
// 	});
//     });