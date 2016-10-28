<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="zh-cn" style="background: #fff;">
<head>
    <meta charset="utf-8">
    <title>工程管理</title>    
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <jsp:include page="/commonJSP/comcss.jsp"></jsp:include>
	<script src="<%=request.getContextPath()%>/js/app/common.js"></script>
	
	<style type="text/css">
		 li{
    	list-style:none;
    }
    /* 遮罩层 */
    .loadContainer{
           	display:none;   
        	position: absolute;
            top: 0;
            left: 0;
            width:100%;
            height: 100%;
            z-index: 100;
        }
        .loadingMask{
            position: absolute;
            top: 0;
            left: 0;
            width:100%;
            height: 100%;
            background: #ddd;
            opacity: 0.6;
        }
        .loadingContainer{
        	position: absolute;
            top: 0;
            left: 0;
            width:100%;
            height: 100%;
        }
        .loading{
            margin-bottom: 0;
            width: 30%;
            height: 20px;
            top: 40%;
            left: 30%;
        }
        .progress-bar{
            background-color: #337ab7;
            background-image: linear-gradient(45deg,rgba(255,255,255,.15)25%,transparent 25%,transparent 50%,rgba(255,255,255,.15)50%,rgba(255,255,255,.15)75%,transparent 75%,transparent);
        }
            small{
        	font-size:1.5em;
        }
        
        a:hover{
        	text-decoration:underline;
        }
    	 /*说明文字*/
        .directionTh{
        	position:relative;
        }
        .directionDiv{
        	display:none;
        	position:absolute;
        	right:0;
        	bottom:100%;
        	width:200%;
        	height:300%;
        	background:#fafafa;
        	border:1px solid #ddd;
        	font-weight:normal;
        }
        table.gridtable {
			font-family: verdana,arial,sans-serif;
			font-size:11px;
			color:#333333;
			border-width: 1px;
			border-color: #666666;
			border-collapse: collapse;
		}
		table.gridtable th {
			border-width: 1px;
			padding: 8px;
			border-style: solid;
			border-color: #666666;
			background-color: #dedede;
		}
		table.gridtable td {
			border-width: 1px;
			padding: 8px;
			border-style: solid;
			border-color: #666666;
			background-color: #ffffff;
		}
	</style>
</head>
<body style="background: #fff;">
	<script src="<%=request.getContextPath()%>/js/cookie_util.js"></script>
	<script src="<%=request.getContextPath()%>/js/libs/jquery-2.1.1.min.js" type="text/javascript"></script>
	<script src="<%=request.getContextPath()%>/js/jquery.form.js" type="text/javascript"></script>
	<script src="<%=request.getContextPath()%>/js/app/businessManage/testDataManagement.js" type="text/javascript"></script>
	<script src="<%=request.getContextPath()%>/js/utils/md5.js"></script>
<div id="main1" role="main">
    <!-- MAIN CONTENT -->
    <div id="content">
        <div style="position: relative;">
				<ol class="breadcrumb">
					<li>业务管理</li>
					<li>SQL数据管理</li>
				</ol>
			</div>
        <section id="widget-grid" class="">
			<div class="row">
				<article class="col-sm-12">                          
                    <div class="jarviswidget" id="wid-id-2" data-widget-togglebutton="false" data-widget-editbutton="false" data-widget-fullscreenbutton="false" data-widget-colorbutton="false" data-widget-deletebutton="false">
  		
		        		<div class="no-padding">
		        		<!-- 正在加载数据遮罩层  开始 -->
              				<div class="loadContainer"  id="loadingRoleManagent">
	               				<div class="loadingMask"></div>
	               					<div class="loadingContainer">
	               						<div class="progress loading">
	                           			<div class="progress-bar progress-bar-striped active" role="progressbar"  style="width: 100%">
	                           				正在加载中...
	                           			</div>
	                       			</div>
	               				</div>
               				</div> 
               			<!-- 正在加载数据遮罩层  结束 -->
		        			<div class="widget-body">
		        				<div id="myQualityContent" class="tab-content container-fluid">                                           
                                    <div class="row" style="padding-top: 1%;padding-left:1%;padding-bottom:0.5%;">                                               
                                        <form class="form-inline">
	                                          <div class="form-group"  style="margin-right:1.5em">
	                                               	<textarea id="mysql" rows="8" cols="150">select * from sc_dic limit 0,20</textarea>
	                                          </div> 
	                                          <div class="form-group">
	                                                <button type="button" class="btn btn-primary" id="search" onclick="queryFun()">
														<span class="glyphicon glyphicon-search"></span>                                                   	
                                                        	查询
													</button>
	                                          </div>   
				        				</form>
				        			</div>
                                    <div style="padding-top: 1%">	
	                                        <div  class="widget-body no-padding" >
	                                        <b>结果展示:</b>    <br>
	                                        	<div id="dispalyData" style="overflow:auto;height:500px;width:1100px;border:1px solid grey;display:block;margin:10px 18px;">
	                                        	
	                                        	</div>
	                                        </div>
                                    </div>
		        				</div>		        			
		        			</div>
		        		</div>		        	
		        	</div>
		        </article>
	        </div>
        </section>
    </div>
</div>
 
 


<jsp:include page="/commonJSP/commonjs.jsp"></jsp:include>


<script>
    var operator="<%=session.getAttribute("userName")%>"; 
	$("#roleManagement").addClass("active");


//控制添加的格式错误
var addSure = true;
var addMimaSure = true;
 $(function(){
	 initManager();
 });

</script>
</body>
</html>