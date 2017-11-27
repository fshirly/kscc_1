<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
 request.setCharacterEncoding("UTF-8");
%>
<%
	Object idcode = request.getAttribute("id");
%>
<style>
.dataTables_wrapper .dataTables_paginate .paginate_button{
    padding:0px!important;
}
#tblRecord_paginate{
   font-size:10px!important;
}
</style>

<div>
    <div id="codecSearchDiv">
		<input type="hidden" id="idcodestr" value="<%=idcode %>"/>
	     <form class="form-inline" id="codecSearchForm">
       		<div class="row" style="margin:0 auto;">
	            <div class="form-group col-sm-6 col-md-6">
	                <label class="text-right" for="ownership">编码器归属：</label>
	                <input id="ownership" name="ownership" class="form-control form-width" style="width:50%;">
	            </div>
	            <div class="form-group col-sm-6 col-md-6">
	                <label class="text-right" for="account">新视通账号：</label>
	                <input id="account" name="account" class="form-control form-width" style="width: 50%;"/>
	            </div>
	    	</div>
            <div class="row clearfix" style="margin:0 auto;margin-top:10px;">
	           <div class="form-group col-sm-6 col-md-6">
	                <label class="text-right" for="IPAddress">IP地址：</label>
	                <input id="IPAddress" name="IPAddress" class="form-control form-width" style="width: 50%;"> 
	           </div>            
	           <div class="col-sm-6 col-md-6 codecBtn">
	                <button id="searchBtn2" type="button" class="btn btn-primary btn-search">搜索</button>
	            	<button id="resetBtn2" type="button"  class="btn btn-primary btn-warning" style="margin-left:20px;">重置</button>
	           </div>
            </div>				
	    </form>
	</div>
	<%--<div class="clearfix">
        <button class="btn btn-primary codecAdd">新增</button>
    </div>--%>
    <div id="tCodec" class="tCodec">
	    <table id="tblCodec" class="table table-striped kscc-grid"></table>
	</div>
</div>    

<!--新增弹框-->
<div class="modal fade" id="addModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" style="top:100px!important;">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
               <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
               <h4 class="modal-title">新增</h4>
            </div>
            <div class="modal-body pl-p15">
                <form id="codecFormAdd" method="post" action="">
            		<div class="clearfix mt-10">
			            <label class="text-right">编解码器归属：</label>
			            <input class="form-control form-width" id="ownershipAdd" style="width:65%;" role="{required:true,length:60}"><dfn>*</dfn>
			        </div>
			        <div class="clearfix mt-10">
			            <label class="text-right">ftp端口号：</label>
			            <input class="form-control form-width" id="ftpAdd" style="width:65%;" role="{required:true,port:true}"><dfn>*</dfn>
			        </div>
			        <div class="clearfix mt-10">
			            <label class="text-right">新视通账号：</label>
			            <input class="form-control form-width" id="accountAdd" style="width:65%;" role="{required:true,number:true,length:20}"><dfn>*</dfn>
			        </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">IP地址：</label>
		                <input class="form-control form-width" id="IPAddressAdd" style="width:65%;" role="{required:true,ip:true}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">端口号：</label>
		                <input class="form-control form-width" id="portAdd" style="width:65%;" role="{required:true,port:true}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">用户名：</label>
		                <input class="form-control form-width" id="userNameAdd" style="width:65%;" role="{required:true,length:20}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">密码：</label>
		                <input class="form-control form-width" type="password" id="userPasswordAdd" style="width:65%;" role="{required:true,length:50}"><dfn>*</dfn>
		            </div>
		        </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary saveAdd">确定</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<!--编辑弹框-->
<div class="modal fade" id="editModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" style="top:20px!important;">
    <div class="modal-dialog modal-md">
        <div class="modal-content">
            <div class="modal-header">
               <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
               <h4 class="modal-title">编辑</h4>
            </div>
            <div class="modal-body pl-p15">
                <form id="codecFormEdit" method="post" action="">
			        <div class="clearfix mt-10">
			            <label class="text-right">归属科室：</label>
						<select class="form-control form-width" name="departmentIdEdit" id ="departmentIdEdit" role="{required:true}"></select><dfn>*</dfn>
			        </div>
            		<div class="clearfix mt-10">
			            <label class="text-right">编解码器归属：</label>
			            <input class="form-control form-width" id="ownershipEdit" style="width:65%;" role="{required:true,length:60}"><dfn>*</dfn>
			        </div>
			        <div class="clearfix mt-10">
			            <label class="text-right">ftp端口号：</label>
			            <input class="form-control form-width" id="ftpEdit" style="width:65%;" role="{required:true,port:true}"><dfn>*</dfn>
			        </div>
			        <div class="clearfix mt-10">
			            <label class="text-right">新视通账号：</label>
			            <input class="form-control form-width" id="accountEdit" style="width:65%;" role="{required:true,number:true}"><dfn>*</dfn>
			        </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">IP地址：</label>
		                <input class="form-control form-width" id="IPAddressEdit" style="width:65%;" role="{required:true,ip:true}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">端口号：</label>
		                <input class="form-control form-width" id="portEdit" style="width:65%;" role="{required:true,port:true}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">用户名：</label>
		                <input class="form-control form-width" id="userNameEdit" style="width:65%;" role="{required:true,length:20}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">密码：</label>
		                <input class="form-control form-width" type="password" id="userPasswordEdit" style="width:65%;" role="{required:true,length:50}"><dfn>*</dfn>
		            </div>
		            <div class="clearfix mt-10">
		                <label class="text-right">备注：</label>
		                <input class="form-control form-width" id="remarksEdit" style="width:65%;" role="{length:100}">
		            </div>
		        </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary saveEdit">确定</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>
<script>
	require(["app/accountManage/codecApp"],function(page){
	    page.run();
	})
</script>
