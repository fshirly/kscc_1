define(["base","datatables.net","app/commonApp"],function(base,DataTable,common){
    var grid = null;
    function setTableHospital(){
        grid = $("#tblHospital").DataTable({
            "searching":false,
            "lengthChange":false,
            "autoWidth":false,
            "serverSide":true,
            "paging":true,
            "stateSave":true,
            "ordering":false,
            "language":{"url":$.base+"/js/lib/chinese.json"},
            "ajax":{
                "type":"post",
                "url":$.base+"/hospital/findAllPageHospitalList",
                "contentType":"application/json",
                "data": function ( d ) {
                    var params={
                        "pageNo": d.start/d.length+1,
                        "pageSize": d.length,
                        "param": {
                            /*"hospitalId":idcode,
                            "hospitalOwnership" : $('#ownership').val(), //编码器归属
                            "newvideoNum": $('#account').val(), //新视通账号
                            "ip": $('#IPAddress').val()//IP地址*/
                        }
                    };
                    return JSON.stringify(params);
                }
            },
            "columns":[
                {"title":"序号","sWidth":"5%"},
                {"title":"医院名称", "data":"hospitalName","sWidth":"19%"},
                {"title":"医院简介", "data":"hospitalContent","sWidth":"19%"},
                {"title":"网站链接", "data":"hospitalUrl","sWidth":"19%"},
                {"title":"医院地址", "data":"location","sWidth":"19%"},
                {"title":"操作", "data":"id","sWidth":"19%"}
            ],
            "columnDefs":[
                {
                    "render":function(data,type,row,meta){
                        var html = "<div>"+(meta.row+1)+"</div>";
                        return html;
                    },
                    "targets":0
                },
                {
					   "render":function(data,type,row,meta){
						   var html="";
						   html="<span class='widthLength' title='"+row.hospitalName+"'>"+row.hospitalName+"</span>";
						   return html;
					   },
					   "targets":1
				},
				{
					   "render":function(data,type,row,meta){
						   var html="";
						   html="<span class='widthLength' title='"+row.hospitalUrl+"'>"+row.hospitalUrl+"</span>";
						   return html;
					   },
					   "targets":2
				},
				{
					   "render":function(data,type,row,meta){
						   var html="";
						   html="<span class='widthLength' title='"+row.hospitalContent+"'>"+row.hospitalContent+"</span>";
						   return html;
					   },
					   "targets":3
				},
				{
					   "render":function(data,type,row,meta){
						   var html="";
						   html="<span class='widthLength' title='"+row.location+"'>"+row.location+"</span>";
						   return html;
					   },
					   "targets":4
				},
                {"render":function(data,type,row,meta){
                    var html = "";
                    html =  "<div class='clearfix'>" +
                        "<div style='display:inline-block;'><button class='btn btn-link hospitalEdit' rowId='"+row.id+"' ><i class='iconfont' style='color:#0e51a2;font-weight:500;' title='编辑'>&#xe608;</i></button></div>"+
                        "<div style='margin-left:3px;display:inline-block;'><button class='btn btn-link hospitalDelete' rowId='"+row.id+"' ><i class='iconfont' style='color:#0e51a2;font-weight:500;' title='删除'>&#xe685;</i></button></div>"+
                        "</div>";
                    return html;
                },
                    "targets":5
                }
            ],

            "drawCallback":function(setting){
                
            	setAddHospital();//新增医院完整信息

                //编辑
                $(".hospitalEdit").off().on("click",function(){
                    var hospitalId=$(this).attr("rowId");

                    $(this).attr({"data-toggle":"modal","data-target":"#editHosModal"});
                    setAreaEdit();
                    var params={
                        "id":hospitalId
                    }
                    //获取原先的数据
                    $.ajax({
                        url:$.base+"/hospital/getLiveHospitalInfoById",
                        type:"post",
                        contentType:"application/json",
                        data:JSON.stringify(params),
                        success:function(data){
                            $("#hospitalNameEdit").val(data.hospitalName);
                            $("#hospitalUrlEdit").val(data.hospitalUrl);
                            $("#hospitalContentEdit").val(data.hospitalContent);
                            $("#locationEdit").val(data.location);
                        }
                    });
                    //编辑确定按钮
                    $(".saveEdit").off().on("click",function(){
                    	var locationValEdit = $("#locationEdit").val();
			        	if(locationValEdit==""||locationValEdit=="请选择省/请选择市"){
			        		$("#tipMessage2").show();
			        		$(".areaSelectEdit .pick-show").css("border-color","red");
			        		 return
			        	 }else{
			        		 $("#tipMessage2").hide();
			        		 $(".areaSelectEdit .pick-show").css("border-color","#ccc");
			        	 }
                    	base.form.validate({
                            form:$("#hosFormEdit"),
                            passCallback:function(){
                            	var params={
                                        "id":hospitalId,
                                        "hospitalName":$("#hospitalNameEdit").val(),
                                        "hospitalUrl":$("#hospitalUrlEdit").val(),
                                        "hospitalContent":$("#hospitalContentEdit").val(),
                                        "location":locationValEdit,
                                    }
                                    var requestTip=base.requestTip({position:"center"});
                                    $.ajax({
                                        url:$.base+"/hospital/updateLiveHospital",
                                        type:"post",
                                        contentType:"application/json",
                                        data:JSON.stringify(params),
                                        success:function(data){
                                            requestTip.success();
                                            $("#editHosModal").modal("hide");
                                            $("#tipMessage2").hide();
                                            common.refreshGrid(grid);
                                            setHosZtree();
                                        },
                                        beforeSend:function(){
                                            requestTip.wait();
                                        },
                                        error:function(){
                                            requestTip.error();
                                        }
                                    });
                             }
                    	});
                    });
                });

                //删除
                $(".hospitalDelete").on("click",function(){
                    var hospitalId=$(this).attr("rowId");
                    base.confirm({
                        label:"提示",
                        text:"<div style='text-align:center;font-size:13px;'>确定删除?</div>",
                        confirmCallback:function(){
                            var requestTip=base.requestTip({position:"center"});
                            $.ajax({
                                url:$.base+"/hospital/toDelLiveHospitalById?id="+hospitalId,
                                type:"POST",
                                contentType:"application/json",
                                success:function(data){
                                    if(data.status=='1'){
                                        requestTip.success();
                                        common.refreshGrid(grid);
                                        setHosZtree();
                                    }else{
                                        requestTip.error(data.tips);
                                    }
                                },
                                beforeSend:function(){
                                    requestTip.wait();
                                },
                                error:function(){
                                    requestTip.error();
                                }
                            });
                        }
                    });
                });
            }
        });
    };
    var locationValAdd;
    //新增医院完整信息
    function setAddHospital(){
    	$(".hospitalAdd").off().on("click",function(){
    		$(this).attr({"data-toggle":"modal","data-target":"#addHosModal"});
    		$(".hosAdd,.orderNumOne").addClass("active");
        	$(".hosAdd,.orderNumOne").siblings().removeClass("active");
        	setAreaAdd();//所属地市
        	findAllDepartment();//归属部门
    		setAddHosNext();//新增医院下一步按钮
    		$('#addHosModal').on('hidden.bs.modal', function () {
    			$(".hosAdd,.orderNumOne").addClass("active");
            	$(".hosAdd,.orderNumOne").siblings().removeClass("active");
            	var btnStr="<button type='button' class='btn btn-primary hosNext'>下一步</button>";
            	$("#addHosModalBtn").html(btnStr);
            	base.form.reset($("#hosAddForm"));
                base.form.reset($("#hosAdminAddForm"));
                base.form.reset($("#codecAddForm"));
			});
    	});
    }
    //新增医院下一步按钮
    function setAddHosNext(){
    	$(".hosNext").off().on("click",function(){
	    	locationValAdd = $("#locationAdd").val();
	       	if(locationValAdd==""||locationValAdd=="请选择省/请选择市"){
	       		$("#tipMessage").show();
	       		$(".areaSelectAdd .pick-show").css("border-color","red");
	       		return
	       	}else{
	       		$("#tipMessage").hide();
	       		$(".areaSelectAdd .pick-show").css("border-color","#ccc");
				base.form.validate({
	                form:$("#hosAddForm"),
	                passCallback:function(){
	                	$(".hosAdminAdd").addClass("active");
	                	$(".hosAdminAdd").siblings().removeClass("active");
	                	$(".lineSecond,.orderNumTwo").addClass("active");
	                	var btnStr="<button type='button' class='btn btn-primary hosAdminPre'>上一步</button><button type='button' class='btn btn-primary hosAdminNext'>下一步</button>";
	                	$("#addHosModalBtn").html(btnStr);
	                	setAddHospitalAdmin();//新增医院管理员信息
	                }
				});
    	    }
		});
    }
    //新增医院管理员信息
    function setAddHospitalAdmin(){
    	    //上一步
    	    $(".hosAdminPre").off().on("click",function(){
    	    	$(".hosAdd").addClass("active");
            	$(".hosAdd").siblings().removeClass("active");
            	$(".lineSecond,.orderNumTwo").removeClass("active");
            	var btnStr="<button type='button' class='btn btn-primary hosNext'>下一步</button>";
            	$("#addHosModalBtn").html(btnStr);
            	setAddHosNext();
    	    });
    	    //下一步
    		$(".hosAdminNext").off().on("click",function(){
    			
    			var requestTip=base.requestTip({position:"center"});
    			var params={
    					"loginName":$("#loginNameAdd").val()
    			};
                $.ajax({
                    url:$.base+"/fbsUser/getUserByLoginName",
                    type:"post",
                    contentType:"application/json",
                    data:JSON.stringify(params),
                    success:function(data){
                        if(data){
                            var password=$("#passwordAdd").val();
                            var confirmPassword=$("#confirmPasswordAdd").val();
                            if(password!==confirmPassword){
                                base.requestTip({position:"center"}).error("密码不一致！");
                                return
                            }
                            base.form.validate({
                                form:$("#hosAdminAddForm"),
                                passCallback:function(){
                                    $(".codecAdd").addClass("active");
                                    $(".codecAdd").siblings().removeClass("active");
                                    $(".lineThird,.orderNumThree").addClass("active");
                                    var btnStr="<button type='button' class='btn btn-primary codecPre'>上一步</button><button type='button' class='btn btn-primary saveAddHospital'>确定</button>";
                                    $("#addHosModalBtn").html(btnStr);
                                    setAddCodec();//新增编解码器信息
                                }
                            });
                        }else{
                        	base.alert("登录名称重复，请重新填写!");
                        }
                    },
                    error:function(){
                        requestTip.error();
                    }
                });
    		});
    }
    //新增编解码器信息
    function setAddCodec(){
    	 //上一步
    	 $(".codecPre").off().on("click",function(){
    		 $(".hosAdminAdd").addClass("active");
        	 $(".hosAdminAdd").siblings().removeClass("active");
        	 $(".lineThird,.orderNumThree").removeClass("active");
        	 var btnStr="<button type='button' class='btn btn-primary hosAdminPre'>上一步</button><button type='button' class='btn btn-primary hosAdminNext'>下一步</button>";
         	 $("#addHosModalBtn").html(btnStr);
         	 setAddHospitalAdmin();//新增医院管理员信息
    	 });
    	 //确定按钮
    	 $(".saveAddHospital").off().on("click",function(){
    		 var password=$("#passwordAdd").val();
         	 var confirmPassword=$("#confirmPasswordAdd").val();
         	 if(password!==confirmPassword){
         		 base.requestTip({position:"center"}).error("密码不一致！");
         		 return
         	 }

    		 base.form.validate({
                 form:$("#codecAddForm"), 
                 passCallback:function(){
                	 var params={
                             "hospitalName":$("#hospitalNameAdd").val(),
                             "hospitalContent":$("#hospitalContentAdd").val(),
                             "hospitalUrl":$("#hospitalUrlAdd").val(),
                             "location":locationValAdd,//所属地市
                             "loginName":$("#loginNameAdd").val(),
                             "user_password":$("#passwordAdd").val(),
                             "userName":$("#userNameHosAdd").val(),
                             "mobilePhone":$("#telephoneAdd").val(),
                             "email":$("#emailAdd").val(),
                             "departmentId":$("#departmentIdAdd").find("option:selected").val(),//归属科室
            			     "codecOwnership":$("#ownershipAdd").val(),
            				 "newvideoNum":$("#accountAdd").val(),
            				 "ftpPort":$("#ftpAdd").val(),
            				 "ip":$("#IPAddressAdd").val(),
            				 "port":$("#portAdd").val(),
            				 "username":$("#userNameAdd").val(),
                             "code_password":$("#userPasswordAdd").val(),
                             "remarks":$("#remarksAdd").val(),
                             "hospitalId":'',
                         };
                         var requestTip=base.requestTip({position:"center",timeout:-1});
                         $.ajax({
                             url:$.base+"/hospital/insertHospitalInfo",
                             type:"post",
                             contentType:"application/json",
                             data:JSON.stringify(params),
                             success:function(data){
                                 if(data.status=='1'){
                                     requestTip.success();
                                     $(".orderDiv>.orderNum,.orderDiv>.line").removeClass("active");
                                     $("#addModal").modal("hide");
                                     $("#tipMessage").hide();
                                     $(".areaSelectAdd .pick-show").css("border-color","#ccc");
                                     common.refreshGrid(grid);
                                     setHosZtree();//刷新左边医院列表
                                     base.form.reset($("#hosAddForm"));
                                     base.form.reset($("#hosAdminAddForm"));
                                     base.form.reset($("#codecAddForm"));
                                     var btnStr="<button type='button' class='btn btn-primary hosNext'>下一步</button>";
                                     $("#addHosModalBtn").html(btnStr);
                                 }else {
                                     requestTip.error(data.tips);
                                 }
                             },
                             beforeSend:function(){
                                 requestTip.wait();
                             },
                             error:function(){
                                 requestTip.error();
                             }
                         });
                 }
    		 });
         });
    }
    
    //刷新左边医院列表
    var setting = {
	        view: {
	            selectedMulti:true,
	        },
	        data: {
	            simpleData: {
	                enable: true,
	                pIdKey: "pid"
	            }
	        },
	        callback: {
	            onClick: zTreeOnClick
	        }
	};
    function setHosZtree(){
    	
        var data = {
            userId:1
        };
        $.ajax({
            type: "POST",
            data: JSON.stringify(data),
            url:$.base+"/hospital/getUserInfoById",
            contentType:"application/json",
            success:function(data){
                setZtree2(data);
            },
            error:function(data){}
        });
    }
    
    function setZtree2(zNodes){
        require(["bootstrap","ztreeCore","ztreeExcheck","ztreeExedit"],function(){
            $.fn.zTree.init($("#treeDemo2"), setting, zNodes);
            treeObj = $.fn.zTree.getZTreeObj("treeDemo2");
            treeObj.expandAll(true);
            var adminNode = treeObj.getNodeByParam("id", "0");
            treeObj.selectNode(adminNode);
            var adminUrl="/loginController/toHospitalList";
            getJSP(adminUrl);
            $(".modal-backdrop").hide();
        });
    }
    
    function  zTreeOnClick(event, treeId, treeNode){
        var url=treeNode.address;
        if(url){
            getJSP(url);
        };
        var rootId=treeNode.id;
        if(rootId=="0"){
        	setHosZtree();//刷新左边医院列表
        }
    }
    
    //加载页面
    function getJSP(url){
        $.ajax({
            type:"GET",
            url:$.base+url,
            error:function(){
                alert("加载错误！");
            },
            success:function(data){
                $(".hospitalUser").html(data);
            }
        });
    }
    
    function setAreaAdd(){
    	$(".areaSelectAdd").empty();
    	base.form.areaSelect({ 
            container:$(".areaSelectAdd"),
            clsUrl:"../css/pages/areaSelect.css", 
            name:"locationAdd",
            id:"locationAdd",
            option:{
               "format":"请选择省/请选择市"
            }
         });
    }

    function setAreaEdit(){
    	$(".areaSelectEdit").empty();
    	base.form.areaSelect({
            container:$(".areaSelectEdit"),
            clsUrl:"../css/pages/areaSelect.css",
            name:"locationEdit",
            id:"locationEdit",
            option:{ 
               "format":"请选择省/请选择市" 
            } 
         });
    }
    
    //归属科室
    function findAllDepartment(){
            $.ajax({
                url:$.base+"/FbsDepartment/findAllDepartment",
                type:"POST",
                contentType:"application/json",
                dateType:"json",
                success:function(data){
                    $("#departmentIdAdd").html("");
                    $("#departmentIdAdd").append("<option value='-1' style='color:#555;'>--请选择--</option>");
                    var html = "";//初始化
                    $.each(data,function(index,item){
						html+="<option value='"+item.id+"'>"+item.departmentName+"</option>";
					})
                    $("#departmentIdAdd").append(html);
                },
                error:function(data){}
            });
    }
    
    return {
        run:function(){
            setTableHospital();
        }
    };
})