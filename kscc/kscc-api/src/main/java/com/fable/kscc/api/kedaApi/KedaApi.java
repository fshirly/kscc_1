package com.fable.kscc.api.kedaApi;

import com.fable.kscc.api.beans.*;
import com.fable.kscc.api.beans.component.ConfParam;
import com.fable.kscc.api.beans.component.VideoFormats;
import com.fable.kscc.api.medTApi.MedTApi;
import com.fable.kscc.api.utils.HttpUtils;
import com.fable.kscc.api.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KedaApi {

	private static String ACCOUNT_TOKEN = "";

	private static String HTTP_COOKIE = "" ;

	public static Logger logger = LoggerFactory.getLogger(KedaApi.class);
	
	private final String  ipAndPort=ApiServerConfig.getSocketAddr();
	
	private final  Map<String,Object> cookieFailMap=new HashMap<String,Object>(){{put("success","0");put("message","登陆注册失败");}};

	@PostConstruct
	public void init(){
		getAccountToken();

		loginMcu();

		final String apiUrl = String.format("http://%s/api/v1/system/heartbeat", ipAndPort);

		final Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		//保持心跳,25分钟调用一次
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					try {
						Thread.sleep(1000*60*25);
						HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	//创建会议
	public Map<String,Object>  createConf(String param) {
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始执行创会...");
	
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", param);
		logger.info("创会的params为：" + param);
		String apiUrl = String.format("http://%s/api/v1/mc/confs", ipAndPort);
		logger.info("创会URL为:" + apiUrl);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("创建会议失败，异常信息为:{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//结束会议
	public Map<String,Object> endConf(String confId) {
	
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("_method", "DELETE");
		String url = String.format("http://%s/api/v1/mc/confs/%s", ipAndPort, confId) ;
		String jsonResp;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(url, HTTP_COOKIE, map);
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//画面选看
    public Map<String,Object> choosePicture(Map<String,Object> param){
		// /api/v1/vc/confs/{conf_id}/inspections
        if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
        }
        logger.info("开始画面选看...");
       
        String params = JsonUtils.toJson(param.get("params"));
        Map<String, String> map = new HashMap<>() ;
        map.put("account_token", ACCOUNT_TOKEN);
        map.put("params", params);
        String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/inspections";
        String apiUrl = String.format(api, ipAndPort);
        String jsonResp ;
        try {
            jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
            return JsonUtils.toBean(jsonResp, Map.class);
        } catch (Exception e1) {
            logger.error("画面选看异常{}", e1.getLocalizedMessage());
            e1.printStackTrace();
        }
        return null;
    }

    //切换主持人
    public Map<String,Object> switchHost(Map<String,Object> param){
//        if(StringUtils.isBlank(HTTP_COOKIE)) {
//			return cookieFailMap;
//        }
//        logger.info("开始切换主持人..");
//
//        String params = JsonUtils.toJson(param.get("params"));
//        Map<String, String> map = new HashMap<>() ;
//        map.put("account_token", ACCOUNT_TOKEN);
//        map.put("params", params);
//        map.put("_method", "PUT");
//        String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/chairman";
//        String apiUrl = String.format(api, ipAndPort);
//        String jsonResp ;
//        try {
//            jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
//            return JsonUtils.toBean(jsonResp, Map.class);
//        } catch (Exception e1) {
//            logger.error("切换主持人异常{}", e1.getLocalizedMessage());
//            e1.printStackTrace();
//			return new HashMap<String,Object>(){{put("success","0");}};
//        }
		/*据科达人描述，会议主席是个场景，是个异常复杂的逻辑，会干扰会议中的画面切
		换，经和客户商讨，切换主席接口不掉，主要由我们来控制业务逻辑*/
		return new HashMap<String,Object>(){{put("success","1");}};
    }
    //选择发言人
    public Map<String,Object> chooseSpeaker(Map<String,Object> param){
        //api/v1/vc/confs/{conf_id}/speaker
        if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
        }
        logger.info("开始选择发言人...");


        String params = JsonUtils.toJson(param.get("params"));
        Map<String, String> map = new HashMap<>() ;
        map.put("account_token", ACCOUNT_TOKEN);
        map.put("params", params);
        map.put("_method", "PUT");
        String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/speaker";
        String apiUrl = String.format(api, ipAndPort);
        String jsonResp ;
		Map<String,Object> paramsMap = (Map<String, Object>) param.get("params");
		String mtId = paramsMap.get("mt_id").toString();
		if ("".equals(mtId)) {
			List<Map<String, Object>> participants = (List<Map<String, Object>>) param.get("participants");
			MedTApi medTApi = new MedTApi();
			for (Map<String, Object> pmap : participants) {
				Map<String, String> temp = new HashMap<>();
				temp.put("ip",pmap.get("ip").toString());
				temp.put("port",pmap.get("port").toString());
				temp.put("hospitalId",pmap.get("hospitalId").toString());
				temp.put("hospitalName",pmap.get("hospitalName").toString());
				medTApi.viewSelfOrOther(temp,0); //自己看自己
			}
		}
        try {
            jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
            return JsonUtils.toBean(jsonResp, Map.class);
        } catch (Exception e1) {
            logger.error("选择发言人异常{}", e1.getLocalizedMessage());
            e1.printStackTrace();
        }
        return null;
    }

    //延长会议时间
    public Map<String,Object>  extendTime(Map<String,Object> param){
        // /api/v1/vc/confs/{conf_id}/delay
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始延长会议时间...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/delay";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("延长会议时间异常{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
    }
    //邀请参与方
	public Map<String,Object> inviteParticipant(Map<String,Object> param){
    	//api/v1/vc/confs/{conf_id}/mts
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始添加本级终端...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mts";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("添加本级终端异常{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//删除参与方
	public Map<String,Object> deleteParticipant(Map<String,Object> param){
	//api/v1/vc/confs/{conf_id}/mts
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始删除终端...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "DELETE");
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mts";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始删除终端异常{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	public Map<String,Object> getOutMeeting(Map<String,Object> param) {
		// /api/v1/vc/confs/{conf_id}/online_mts
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		Map<String, String> map = new HashMap<>() ;
		String params = JsonUtils.toJson(param.get("params"));
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "DELETE");
		String url = String.format("http://%s/api/v1/vc/confs/%s/online_mts", ipAndPort, param.get("confId")) ;
		String jsonResp;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(url, HTTP_COOKIE, map);
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String,Object>(){{put("success","0");}};
		}
	}
		//批量呼叫参与方
		public Map<String,Object> callParticipant(Map<String,Object> param){
			// /api/v1/vc/confs/{conf_id}/online_mts
			if(StringUtils.isBlank(HTTP_COOKIE)) {
				return cookieFailMap;
			}
			logger.info("开始呼叫终端...");
		
			String params = JsonUtils.toJson(param.get("params"));
			Map<String, String> map = new HashMap<>() ;
			map.put("account_token", ACCOUNT_TOKEN);
			map.put("params", params);
			map.put("_method", "POST");
			String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/online_mts";
			String apiUrl = String.format(api, ipAndPort);
			String jsonResp ;
			try {
				jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
				return JsonUtils.toBean(jsonResp, Map.class);
			} catch (Exception e1) {
				logger.error("开始呼叫终端异常{}", e1.getLocalizedMessage());
				e1.printStackTrace();
			}
			return null;
		}
		
	//画面合成
	public Map<String,Object> pictureSynthesis(Map<String,Object> param){
		//api/v1/vc/confs/{conf_id}/mtvmps
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始终端画面合成...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "PUT");
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mtvmps";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始终端画面合成{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//开启会议画面合成
	public Map<String,Object> pictureSynthesiss(Map<String,Object> param) {
//		/api/v1/vc/confs/{conf_id}/vmps
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开启会议画面合成...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/vmps";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开启会议画面合成{}异", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//开启会议混音
	public Map<String,Object> meettingMixs(Map<String,Object> param) {
		// /api/v1/vc/confs/{conf_id}/mixs
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开启会议混音...");

		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mixs";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开启会议混音", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//停止会议画面合成
	public Map<String,Object> cancelPictureSynthesiss(String confId) {
		///api/v1/vc/confs/{conf_id}/vmps/{vmp_id}
		///api/v1/vc/confs/{conf_id}/vmps/{vmp_id}
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("_method", "DELETE");
		String url = String.format("http://%s/api/v1/vc/confs/%s/vmps/1", ipAndPort, confId) ;
		String jsonResp;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(url, HTTP_COOKIE, map);
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String,Object>(){{put("success","-1");}};
		}
	}

	//画面合成信息
	public Map<String,Object> getPictureSynthesiss(String conf_id){
      ///api/v1/vc/confs/{conf_id}/vmps/{vmp_id}
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		String api="http://%s/api/v1/vc/confs/"+conf_id+"/vmps/1";
		String apiUrl = String.format(api, ipAndPort)+"?account_token="+ACCOUNT_TOKEN;
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("获取画面合成信息", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	public Map<String,Object> getChoosePicture(String conf_id){
		// /api/v1/vc/confs/{conf_id}/inspections
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		String api="http://%s/api/v1/vc/confs/"+conf_id+"/inspections";
		String apiUrl = String.format(api, ipAndPort)+"?account_token="+ACCOUNT_TOKEN;
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("获取画面选看信息", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//取消终端选看
	public Map<String,Object> cancelChoosePicture(Map<String,Object> param) {
		// /api/v1/vc/confs/{conf_id}/inspections/{mt_id}/{mode}
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("_method", "DELETE");
		String api = "http://%s/api/v1/vc/confs/"+param.get("confId")+"/inspections/"+param.get("mtId")+"/1";
		String url = String.format(api, ipAndPort) ;
		String jsonResp;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(url, HTTP_COOKIE, map);
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<String,Object>(){{put("success","-2");}};
		}
	}

	//静音
	public Map<String,Object> silence(Map<String,Object> param){
	///api/v1/vc/confs/{conf_id}/mts/{mt_id}/silence
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始终端静音...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "PUT");
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mts/"+param.get("mtId")+"/silence";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始终端静音{}异常", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//哑音
	public Map<String,Object> mute(Map<String,Object> param){
	//api/v1/vc/confs/{conf_id}/mts/{mt_id}/mute
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始终端哑音...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "PUT");
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mts/"+param.get("mtId")+"/mute";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始终端哑音{}异常", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//会场静音
	public Map<String,Object> allSilence(Map<String,Object> param){
		///api/v1/vc/confs/{conf_id}/silence
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始会场静音...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "PUT");
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/silence";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始会场静音{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//会场哑音
	public Map<String,Object> allMute(Map<String,Object> param){
		///api/v1/vc/confs/{conf_id}/mute
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始会场哑音...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		map.put("_method", "PUT");
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/mute";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始会场哑音{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}
	//获取本级会议终端列表
	public Map<String,Object> getMTS(String conf_id){
//		/api/v1/vc/confs/{conf_id}/mts
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		String api="http://%s/api/v1/vc/confs/"+conf_id+"/mts";
		String apiUrl = String.format(api, ipAndPort)+"?account_token="+ACCOUNT_TOKEN;
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (final Exception e1) {
			logger.error("获取本级会议终端列表失败", e1.getLocalizedMessage());
			e1.printStackTrace();
			return new HashMap<String,Object>(){{put("success",e1.getLocalizedMessage());}};
		}
	}
	
	//获取视频会议列表
	public Map<String,Object> getLiveBroadList(Map<String,Object> param){
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始获取视频会议列表...");
	
		String api="http://%s/api/v1/vc/confs";
		String apiUrl = String.format(api, ipAndPort)+"?account_token="+ACCOUNT_TOKEN+"&start=0&count=0";
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("开始视频会议列表{}", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//获取视频会议信息
	public Map<String,Object> getMeetingInfo(String confId){
		///api/v1/vc/confs/{conf_id}
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始获取视频会议信息...");
	
		String api="http://%s/api/v1/vc/confs";
		String apiUrl = String.format(api, ipAndPort)+"/"+confId;
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e1) {
			logger.error("获取视频会议详情失败", e1.getLocalizedMessage());
			e1.printStackTrace();
		}
		return null;
	}

	//发送短消息
	public Map<String,Object> sendShortMessage(Map<String,Object> param){
		///api/v1/vc/confs/{conf_id}/sms
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		logger.info("开始发送短消息...");
	
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/sms";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (final Exception e1) {
			logger.error("发送短消息", e1.getLocalizedMessage());
			e1.printStackTrace();
			return new HashMap<String,Object>(){{put("fail",e1.getLocalizedMessage());}};
		}
	}
	//获取会议发言人
	public Map<String,Object> getSpeaker(String conf_id){
		// /api/v1/vc/confs/{conf_id}/speaker
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		String api="http://%s//api/v1/vc/confs/"+conf_id+"/speaker";
		String apiUrl = String.format(api, ipAndPort)+"?account_token="+ACCOUNT_TOKEN;
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (final Exception e1) {
			logger.error("获取会议发言人失败", e1.getLocalizedMessage());
			e1.printStackTrace();
			return new HashMap<String,Object>(){{put("fail",e1.getLocalizedMessage());}};
		}
	}
	//获取录像列表
	public Map<String,Object> getRecorders(String conf_id){
		// /api/v1/vc/confs/{conf_id}/recorders
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		String api="http://%s//api/v1/vc/confs/"+conf_id+"/recorders";
		String apiUrl = String.format(api, ipAndPort)+"?account_token="+ACCOUNT_TOKEN;
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpGet(apiUrl) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (final Exception e1) {
			logger.error("获取录像列表失败", e1.getLocalizedMessage());
			e1.printStackTrace();
			return new HashMap<String,Object>(){{put("fail",e1.getLocalizedMessage());}};
		}
	}
	//停止录像
	public Map<String,Object> stopRecord(String confId,String recId) {
        ///api/v1/vc/confs/{conf_id}/recorders/{rec_id}
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("_method", "DELETE");
		String url = String.format("http://%s/api/v1/mc/confs/%s/recorders/%s", ipAndPort, confId,recId) ;
		String jsonResp;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(url, HTTP_COOKIE, map);
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//开启录像
	public Map<String,Object> startRecord(Map<String,Object> param){
		///api/v1/vc/confs/{conf_id}/recorders
		if(StringUtils.isBlank(HTTP_COOKIE)) {
			return cookieFailMap;
		}
		String params = JsonUtils.toJson(param.get("params"));
		Map<String, String> map = new HashMap<>() ;
		map.put("account_token", ACCOUNT_TOKEN);
		map.put("params", params);
		String api="http://%s/api/v1/vc/confs/"+param.get("confId")+"/recorders";
		String apiUrl = String.format(api, ipAndPort);
		String jsonResp ;
		try {
			jsonResp = HttpUtils.httpPostByFormWithCookie(apiUrl, HTTP_COOKIE, map) ;
			return JsonUtils.toBean(jsonResp, Map.class);
		} catch (final Exception e1) {
			logger.error("开启录像失败:", e1.getLocalizedMessage());
			return new HashMap<String,Object>(){{put("fail",e1.getLocalizedMessage());}};
		}
	}
	private  void loginMcu() {
		if(StringUtils.isBlank(ACCOUNT_TOKEN)) {
			return ;
		}
		logger.info("开始执行登陆MCU操作...");
		LoginMcuConfig loginMcuConfig =  new LoginMcuConfig();
		Map<String, String> map = new HashMap<>();
		map.put("account_token", ACCOUNT_TOKEN) ;
		map.put("username", loginMcuConfig.getUsername());
		map.put("password", loginMcuConfig.getPassword());
		String apiUrl = String.format("http://%s/api/v1/system/login", ipAndPort);
		logger.info("请求登陆5.0平台的地址为:" + apiUrl);
		LoginMcuResp loginMcuResp = HttpUtils.loginHttpPostByForm(apiUrl, map);
		if(loginMcuResp != null) {
			HTTP_COOKIE = loginMcuResp.getCookie() ;
			logger.info("登录MCU成功, HTTP_COOKIE={}", HTTP_COOKIE) ;
		} else {
			logger.error("登录MCU失败!");
		}
	}

	private  void getAccountToken() {
		logger.info("开始执行获取ACCOUNT_TOKEN...");
	
		OauthConsumerInfo oauthInfo = new OauthConsumerInfo();
		Map<String, String> map = new HashMap<String, String>();
		map.put("oauth_consumer_key", oauthInfo.getOauthConsumerKey());
		map.put("oauth_consumer_secret", oauthInfo.getOauthConsumerSecret());
		logger.info("oauthInfo:" + oauthInfo.toString());
		String apiUrl = String.format("http://%s/api/v1/system/token", ipAndPort);
		logger.info("请求获取token值的地址为:" + apiUrl);
		String str = HttpUtils.getTokenHttpPostByForm(apiUrl, map);
		logger.info("获取token值请求的回复为:" + str);
		if (JsonUtils.isJson(str)) {
			if (str.contains(HttpUtils.EXCEPTION_NODE)) {
				logger.error("获取token出现异常!");
				return;
			}
			OauthTokenResp oauth =JsonUtils.toBean(str, OauthTokenResp.class) ;
			// 判断是否获取到account_token
			if (oauth == null) {
				logger.error("访问MCU平台失败!");
				
			} else if (oauth.getSuccess() == 1) {
				ACCOUNT_TOKEN = oauth.getAccount_token();
				logger.info("获取ACCOUNT_TOKEN值成功，ACCOUNT_TOKEN值为:" + ACCOUNT_TOKEN);
			} else {
				logger.error("获取ACCOUNT_TOKEN值失败, error_code为:" + oauth.getError_code());
			}
		} else {
			logger.error("获取ACCOUNT_TOKEN出现异常!");
		}
	}
	
	public  String generateConfParam(String confName, List<String>  mtE164s,long duration) {
		ConfParam param = new ConfParam() ;
		param.setName(confName);
		param.setDuration((int) duration);
		VideoFormats videoFormat = new VideoFormats();
		List<VideoFormats> videoFormatList = new ArrayList<VideoFormats>();
		videoFormatList.add(videoFormat);
		param.setVideo_formats(videoFormatList) ;

		/*需要注释*/
//		List<Integer> audioFormatList = new ArrayList<>();
//		audioFormatList.add(9);
//		param.setAudio_formats(audioFormatList) ;


		if(mtE164s != null) {
			param.addInviteMembers(mtE164s);
		}
		String jsonStr = JsonUtils.toJson(param);
		return jsonStr;				
	}

}
