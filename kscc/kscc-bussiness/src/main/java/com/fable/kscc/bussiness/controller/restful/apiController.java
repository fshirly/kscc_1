package com.fable.kscc.bussiness.controller.restful;

import com.fable.kscc.api.model.fbsInterface.FbsInterface;
import com.fable.kscc.api.model.livebroadcast.FbsLiveBroadcast;
import com.fable.kscc.api.model.page.PageRequest;
import com.fable.kscc.api.model.page.PageResponse;
import com.fable.kscc.api.model.page.ResultKit;
import com.fable.kscc.api.model.page.ServiceResponse;
import com.fable.kscc.bussiness.service.apiService.IApiService;
import com.fable.kscc.bussiness.service.uploadfile.UploadFileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title :
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Author :Hairui
 * Date :2017/11/15
 * Time :13:17
 * </p>
 * <p>
 * Department :
 * </p>
 * <p> Copyright : 江苏飞博软件股份有限公司 </p>
 */
@Controller
@RequestMapping("api")
public class apiController {

    @Autowired
    IApiService service;
    
    @Autowired
    private UploadFileService uploadFileService;

    @RequestMapping("/getMedT100Status")
    @ResponseBody
    public ServiceResponse getMedT100Status(){
        return service.getAllHospitalList();
    }

    @RequestMapping("/getMedTDisks")
    @ResponseBody
    public PageResponse<List> getMedTDisks(@RequestBody PageRequest<Map<String,String>> param){
        return service.getDisk(param);
    }

    @RequestMapping("/getMedTDiskInfo")
    @ResponseBody
    public ServiceResponse getMedTDiskInfo(@RequestBody Map<String,String> param){
        try {
            return service.getDiskInfo(param);
        } catch (Exception e) {
            return ResultKit.fail(e.getMessage());
        }
    }
    
    
    @RequestMapping(value="/getCloudUsage")
   	@ResponseBody
   	public String getCloudUsage() throws Exception {
       	try {
       		return uploadFileService.getCloudUsage();
   		} catch (Exception e) {
   			e.printStackTrace();
   			return "0";
   		}
   	}

    @RequestMapping(value="/medtOkOrNot")
    @ResponseBody
    public Map<String,Object> medtOkOrNot() throws Exception {
       return service.medtOkOrNot();
    }

    /**
     *  查询新视通接口
     * @param param
     * @return
     */
    @RequestMapping("/getMedInterfaceInfo")
    @ResponseBody
    public PageResponse<FbsInterface> getMedInterfaceInfo(@RequestBody PageRequest<Map<String,Object>> param){
        PageResponse<FbsInterface> fbsRequest =  service.getMedInterfaceInfo(param);
        return fbsRequest;
    }
}
