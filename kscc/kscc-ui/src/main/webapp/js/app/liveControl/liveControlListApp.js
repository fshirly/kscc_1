define(["base"],function(base){

		var loginUserId='';
	    var roleName;
		var roleCode;

		function userId(){
			$.ajax({
	            type:"post",
	            async: false,
	            url:$.base+"/liveBroadCastController/getUserId",
	            success:function (data) {
	            	   loginUserId=data.id;
					   roleName=data.roleName;
					   roleCode=data.roleCode;
                       setCarousel();
	            }
			});
		};	
   
		 function setCarousel(){
			 $.ajax({
	               type: "POST",
	               url:  $.base+"/liveController/getLivingList",
                   dataType:'json',
                   contentType:"application/json",
				   data:roleName=='kscc管理员'?JSON.stringify({}):JSON.stringify({userId:loginUserId}),
	               success: function(data){
	            	   var lis="";
	            	   var liveIntroduction="";
	            	   var enterLiveStr="";
	            	   $(".liveManage").empty();
	            	   $("#carouselCircleManage").empty();
	            	   var dataList=data.data;
	            	   var childs=new Array();
	            	   $.each(dataList,function(i,v){
	            		   var child = "";
	            		   var picPath="";//图片变量
	            		   if(v.picturePath){
	                       	picPath=$.base+"/loginController/showPic?name="+v.picturePath;
	                       }else{
	                       	picPath=$.base+"/images/pages/changePic.png";
	                       }
	            		   $.each(v.participantNames,function(x,y){
	            			   if(y.serialNumber==1){
	            				   child+="<li att='"+y.id+"' class='controlListLi'><span class='iconfont iconfontList'>&#xe61e;</span><span class='controlParti' title='"+y.hospitalName+"'>"+y.hospitalName+"</span><span class='iconfont iconfontImg' style='color:#ee453b;' title='第一主持人'>&#xe61c;</span></li>";
	            			   }else if(y.serialNumber==2){
	            				   child+="<li att='"+y.id+"' class='controlListLi'><span class='iconfont iconfontList'>&#xe61e;</span><span class='controlParti' title='"+y.hospitalName+"'>"+y.hospitalName+"</span><span class='iconfont iconfontImg' title='第二主持人'>&#xe61c;</span></li>";
	            			   }else{
	            				   child+="<li att='"+y.id+"' class='controlListLi'><span class='iconfont iconfontList'>&#xe61e;</span><span class='controlParti' title='"+y.hospitalName+"'>"+y.hospitalName+"</span></li>";
	            			   }
	            		   });
	            		   childs.push(child);	
	            		   if(v.liveIntroduction==null){
	            			   liveIntroduction="";
	            		   }
	            		   if(v.playStatus=="2"){
	            			   enterLiveStr="<button class='iconfont noEnterBtn' title='已结束直播，不可进入' disabled>&#xe669;</button>";
            			   }else{
            				   enterLiveStr="<a att='"+v.id+"' confId='"+v.confId+"' class='iconfont enterLive' title='点击进入'><span class='enterDiv'>进入直播</span>&#xe62b;</a>";
            			   }
	            		   var index=Math.ceil((i+1)/2);
	            		   lis+="<li class='carousel_"+index+" controlLi'>"+
	            		          "<div class='controlDiv'>"+
	            		             "<div class='row clearfix'>"+
		            		             "<div class='col-xs-8 col-sm-8 col-md-8'>"+
		            		                  "<div class='introImg'><img src='"+picPath+"'></div>"+
		            		                  "<div class='introTxt'>"+
			            		                "<h5 class='titleH titleLength titleWidth' style='margin-bottom:0;' title='"+v.title+"'>"+v.title+"</h5>"+
			            		                 "<div class='introductionH'>"+v.liveIntroduction+"</div>"+
			            		              "</div>"+
			            		         "</div>"+
		            		             "<div class='col-xs-4 col-sm-4 col-md-4'>"+
		            		                  "<h5 class='titleH' style='border-bottom:1px solid #00479d;'>参与方列表</h5>"+
		            		                  "<ul class='partiList partiList_"+i+"' style='height:130px;overflow:auto;'>"+childs[i]+"</ul>"+
		            		             "</div>"+
		            		          "</div>"+enterLiveStr+"</div>"+
            		            "</li>";
	            	   });
	            	   $(".liveManage").append(lis);
	            	   for (var i=0;i<Math.ceil(dataList.length/2);i++){
	            		   $(".liveManage .carousel_"+(i+1)+"").wrapAll("<ul class='carouselUl_"+(i+1)+"'></ul>");
	            		   $(".liveManage .carouselUl_"+(i+1)+"").wrap("<div class='item'></div>");
	            		   $("#carouselCircleManage").append("<li data-target='#myCarouselManage' data-slide-to='"+i+"'></li>");
	            	   }
	            	   $(".liveManage .item:first").addClass("active");
	            	   $("#carouselCircleManage").children(":first").addClass("active");
	            	   $('#myCarouselManage').carousel({
	                       pause: true,
	                       interval: false
	                   });
	            	   setEnter();
	            	   setScroll();
	               }
			 });
		 };
		 
		 function setEnter(){
			 $(".enterLive").unbind().on("click",function(){
				 var id=$(this).attr("att");
				 var confId=$(this).attr("confId")
				 var requestTip=base.requestTip({position:"center"});
				 $.ajax({
		               type: "GET",
		               async:false,
		               url:  $.base+"/liveBroadCastController/getMts/"+confId+"/"+loginUserId+"/"+roleCode,
		               success: function(data){
		            	   if(data.status==='1'){
		            		    var url=$.base+"/loginController/toLiveControlDetail";
								$.ajax({ 
					                type:"GET", 
					                url:url, 
					                error:function(){ 
					                   alert("加载错误！"); 
					                }, 
					                success:function(data){
                                        if(data.indexOf('06a5bb21-b8f0-4dfd-8004-4b4e17d4f81c')!==-1){
                                            window.location.href=$.base+'/loginController/toLogin'
                                            return
                                        }
                                        $(".middle").html(data);
					                } 
					             });
								$("#liveIdControlDetail").val(id);
						   }
						   else{
		            	   	requestTip.error(data.tips);
						   }
		               }
				 });
			 })
		 }
		 
		 function setScroll(){
			var height = parseInt($(".middleContent").height()-30) * 0.5;
			$(".controlLi").css({"margin-top":(height-195)/2,"margin-bottom":(height-195)/2,"margin-left":"auto","margin-right":"auto"});
			var length=$(".liveManage .item:last-child").find(".controlLi").length;
			if(length==1){
				$("#myCarouselManage").height(height*2);
				$(".liveManage .item:last-child").find(".controlLi").css({"margin-top":(height*2-195)/2,"margin-bottom":(height*2-195)/2});
			}
			base.scroll({
				container:".partiList"
			});
		 }
		 
		return {
			run:function(){
				userId();
			}
		};
});
