define(["base","datatables.net","app/commonApp"],function(base,DataTable,common){
    function getBase() {
        $.ajax({
            type: "post",
            async:true,
            url:$.base+"/hospital/getHospitalInfoById",
            contentType:"application/json",
            data:JSON.stringify({
                "hospitalIds":$("#idstr").val()
            }),
            dataType: "json",
            success:function(data){
            	$("#hospitalInfoForm input,#hospitalInfoForm textarea,#hospitalInfoForm #imgBtnLogo").attr("disabled","disabled");
            	$("#hospitalInfoForm textarea").attr("placeHolder","");
            	$(".hosNameEdit").addClass("disNone");
            	$(".setBtns").html("<button class='btn btn-primary editHos'>编辑</button>");
                $("#hospitalName").val(data.hospitalName);
                $("#hospitalUrl").val(data.hospitalUrl);
                $("#hospitalContent").val(data.hospitalContent);
                locationInfo=data.location;
                $("#locationDetail").val(locationInfo);
                //图片路径
                var picturePath=data.logoUrl;
                if(picturePath){
                	$('#pictureView').attr("src",$.base+"/loginController/showPic?name="+picturePath);
                	$('#pictureView').attr("imgUrl",picturePath);
                }
             }
        });
    }
    
    //编辑医院信息
    function editHos(){
    	$(".setBtns").off().on("click",function(){
    		var strs="<button class='btn btn-primary saveHosEdit'>保存</button><button class='btn btn-primary cancelHosEdit' style='margin-left:8px;'>取消</button>";
    		$(".setBtns").html(strs);
    		$("#hospitalInfoForm input,#hospitalInfoForm textarea,#hospitalInfoForm #imgBtnLogo").removeAttr("disabled");
    		$("#hospitalInfoForm textarea").attr("placeHolder","不能超过100个汉字");
    		$(".hosNameEdit").removeClass("disNone");
    		$("#locationDetail").css("display","none");
    		$("#areaSelectInfo").css("display","block");
    		$("#locationInfo").val(locationInfo);
    		setAreaInfo();//医院地址
    		setImageUpload();//上传医院logo
    		//保存
    		$(".saveHosEdit").off().on("click",function(){
    			var locationValEdit = $("#locationInfo").val();
    	       	if(locationValEdit==""||locationValEdit=="请选择省/请选择市"){
    	       		$("#tipMessage").show();
    	       		$(".areaSelectInfo .pick-show").css("border","1px solid red");
    	       		return
    	       	}else{
    	       		$("#tipMessage").hide();
    	       		$(".areaSelectInfo .pick-show").css("border","1px solid #ccc");
	    			base.form.validate({
	                    form:$("#hospitalInfoForm"),
	                    passCallback:function(){
	                    	var params={
	                                "hospitalName":$("#hospitalName").val(),
	                                "hospitalUrl":$("#hospitalUrl").val(),
	                                "logoUrl":$('#pictureView').attr("imgUrl")?$('#pictureView').attr("imgUrl"):"",
	                                "hospitalContent":$("#hospitalContent").val(),
	                                "location":locationValEdit,
	                                "id":$("#idstr").val()
	                            }
	                            var requestTip=base.requestTip({position:"center"});
	                            $.ajax({
	                                url:$.base+"/hospital/updateLiveHospital",
	                                type:"POST",
	                                contentType:"application/json",
	                                dateType:"json",
	                                data:JSON.stringify(params),
	                                success:function(data){
	                                    switch(data.status){
	        	                            case '1':
	        	                                requestTip.success("修改医院信息成功！");
	        	                                getBase();
	        	                                break;
	        	                            default:
	        	                                requestTip.error("修改医院信息失败！");
	        	                                break;
	                                    }
	                                },
	                                error:function(){
	                                    requestTip.error();
	                                }
	                            });
	                       }
	    			  });
    	        }
    		});
    		//取消
    		$(".cancelHosEdit").off().on("click",function(){
    			$("#hospitalInfoForm input,#hospitalInfoForm textarea,#hospitalInfoForm #imgBtnLogo").attr("disabled","disabled");
    			$("#hospitalInfoForm textarea").attr("placeHolder","");
    			$(".hosNameEdit").addClass("disNone");
    			$(".setBtns").html("<button class='btn btn-primary editHos'>编辑</button>");
    			$("#locationDetail").css("display","block");
    			$(".areaSelectInfo").css("display","none");
    			$(".ui-form-error").remove();
    			$("#hospitalInfoForm input,#hospitalInfoForm textarea").removeClass("errorStyle");
    			getBase();//获得先前数据
    		});
    	});
    }

    //新增医院
    function  addHospital(){
    	$(".hospitalAdd").off().on("click",function(){
            var codecId=$(this).attr("rowId");
            $(this).attr({"data-toggle":"modal","data-target":"#addModal"});
            //新增确定按钮
            $(".saveAdd1").off().on("click",function(){
                var params={
                    "hospitalName":$("#hospitalNameAdd").val(),
                    "hospitalContent":$("#hospitalContentAdd").val(),
                    "hospitalUrl":$("#hospitalUrlAdd").val(),
                }
                var requestTip=base.requestTip();
                $.ajax({
                    url:$.base+"/hospital/insertHospitalInfo",
                    type:"post",
                    contentType:"application/json",
                    data:JSON.stringify(params),
                    success:function(data){
                        if(data.status=='1'){
                            requestTip.success();
                            $("#addModal").modal("hide");
                            common.refreshGrid(grid);
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
            });
        });
    }

    /**上传图片**/
    var setImageUpload = function(){
        $("#pictureInputLogo").off().on("change",function(){
        	var arr=$("#pictureInputLogo").val().split("\\");
        	var picArr=arr[arr.length-1];
        	if(!base.form.validateFileExtname($("#pictureInputLogo"),"jpg,png")){
				base.requestTip({position:"center"}).error("图片格式不正确");
				return;
			}
        	var modal = base.modal({
				label: "提示",
				context: "<div style='text-align:center;font-size:13px;'>确定上传此图片？</div>",
				width: 250,
				height: 50,
				buttons: [
					{
						label: "确定",
						cls: "btn btn-info",
						clickEvent: function () {
							uploadPic(picArr);
							modal.hide();
						}
					},{
						label:"取消",
						cls:"btn btn-warning",
						clickEvent:function(){
							modal.hide();
						}
					}
				]
			});
            
        });
    };
    
    function uploadPic(picArr){
    	base.form.fileUpload({
            url: $.base + "/loginController/upload",
            id: "pictureInputLogo",
            success: function (data) {
            	if(data.status==="1"){
                    picturePath=data.data.fileUrl;
                    pictureName=data.data.fileName;
                    $('#pictureView').attr("src",$.base+"/loginController/showPic?name="+picturePath);
                    $('#pictureView').attr("imgUrl",picturePath);
                    base.requestTip({
                        position:"center"
                    }).success("附件上传成功！");
                  //$(".picNameDiv").text(picArr);
				}
            }
        });
    }

    function setAreaInfo(){
    	$(".areaSelectInfo").empty();
    	base.form.areaSelect({
            container:$(".areaSelectInfo"),
            clsUrl:"../css/pages/areaSelect.css",
            name:"locationInfo",
            id:"locationInfo",
            option:{
               "format":"请选择省/请选择市"
            }
         });
    }

    return {
        run:function(){
            getBase();
            addHospital();
            editHos();
        }
    };
})