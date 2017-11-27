package com.fable.kscc.bussiness.service.apiService;

import com.fable.kscc.api.medTApi.MedTApi;
import com.fable.kscc.api.model.fbsInterface.FbsInterface;
import com.fable.kscc.api.model.hospitalInformation.FbsHospitalInformation;
import com.fable.kscc.api.model.liveCodec.FbsLiveCodec;
import com.fable.kscc.api.model.livebroadcast.FbsLiveBroadcast;
import com.fable.kscc.api.model.page.PageRequest;
import com.fable.kscc.api.model.page.PageResponse;
import com.fable.kscc.api.model.page.ResultKit;
import com.fable.kscc.api.model.page.ServiceResponse;
import com.fable.kscc.bussiness.mapper.fbsinterface.FabsInterfaceMapper;
import com.fable.kscc.bussiness.mapper.hospitalInformation.HospitalInformationMapper;
import com.fable.kscc.bussiness.mapper.livecodec.LiveCodecMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Time :15:36
 * </p>
 * <p>
 * Department :
 * </p>
 * <p> Copyright : 江苏飞博软件股份有限公司 </p>
 */
@Service
public class ApiServiceImpl implements IApiService {

    @Autowired
    HospitalInformationMapper hospitalInformationMapper;
    @Autowired
    private MedTApi medApi;
    @Autowired
    private LiveCodecMapper liveCodecMapper;
    @Autowired
    private FabsInterfaceMapper fabsInterfaceMapper;
    @Override
    public ServiceResponse getAllHospitalList() {
        return ResultKit.serviceResponse(hospitalInformationMapper.getAllHospitalList());
    }

    @Override
    public PageResponse<List> getDisk(PageRequest<Map<String, String>> param) {

        //根据医院id查询相应的医院信息
        List<FbsLiveCodec> codeList = liveCodecMapper.findAllLiveCodecForYw(param.getParam());
        Map<String,String> map = new HashMap<>();
        List arryList = new ArrayList<>();
        for (FbsLiveCodec code : codeList){
            map.put("ip",code.getIp());
            map.put("port",code.getPort());
            map.put("username",code.getUserName());
            map.put("password",code.getPassword());
            map.put("hospitalId",String.valueOf(code.getHospitalId()));
            Map<String,Object> codeMap = medApi.getDisks(map);
            Map<String,Object> mpuVidMap = medApi.getMpuVidEnc(map);
            if (!"0".equals(codeMap.get("success")) || !"0".equals(mpuVidMap.get("success"))){
                //计算分辨率
                String resoultion = mpuVidMap.get("Width").toString() + "*" + mpuVidMap.get("Height").toString();
                codeMap.put("resoultion",resoultion);
                //计算帧数
                String frameRate = mpuVidMap.get("FrameRate").toString();
                codeMap.put("FrameRate",frameRate);
                //计算剩余空间
                Integer remainStorm = Integer.parseInt(codeMap.get("Capacity").toString()) - Integer.parseInt(codeMap.get("Free").toString());
                codeMap.put("remainStorm",remainStorm);
                //讯世通账号、ip、医院名称、归属医院
                String hospitalName = hospitalInformationMapper.findAllById(Integer.parseInt(code.getHospitalId().toString()));//根据医院id查询相应的医院名称
                codeMap.put("newvideoNum",code.getNewvideoNum());
                codeMap.put("ip",code.getIp());
                codeMap.put("hospitalName",hospitalName);
                codeMap.put("codecOwnership",code.getCodecOwnership());
                arryList.add(codeMap);
            }else {
                codeMap.put("resoultion",null);
                //计算帧数
                codeMap.put("FrameRate",null);
                //计算剩余空间
                codeMap.put("remainStorm",null);
                codeMap.put("Free", null);
                codeMap.put("Capacity", null);
                //讯世通账号、ip、医院名称、归属医院
                String hospitalName = hospitalInformationMapper.findAllById(Integer.parseInt(code.getHospitalId().toString()));//根据医院id查询相应的医院名称
                codeMap.put("newvideoNum",code.getNewvideoNum());
                codeMap.put("ip",code.getIp());
                codeMap.put("hospitalName",hospitalName);
                codeMap.put("codecOwnership",code.getCodecOwnership());
                arryList.add(codeMap);
            }
        }
        return PageResponse.wrap(arryList, param.getPageNo(), param.getPageSize());
    }

    @Override
    public PageResponse<FbsInterface> getMedInterfaceInfo(PageRequest<Map<String, Object>> param) {
        Map<String,Object> map=param.getParam();
        Page<FbsInterface> result = PageHelper.startPage(param.getPageNo(), param.getPageSize());
        fabsInterfaceMapper.selectFbsInterfaceList(map);
        return PageResponse.wrap(result);
    }

    @Override
    public ServiceResponse getDiskInfo(Map<String,String> param) {
        //根据医院id查询相应的医院信息
        List<FbsLiveCodec> codeList = liveCodecMapper.findAllLiveCodecForYw(param);
        Map<String,String> map = new HashMap<>();
        List<Map<String,Object>> arryList = new ArrayList<>();
        for (FbsLiveCodec code : codeList){
            map.put("ip",code.getIp());
            map.put("port",code.getPort());
            map.put("username",code.getUserName());
            map.put("password",code.getPassword());
            map.put("hospitalId",String.valueOf(code.getHospitalId()));
            Map<String,Object> codeMap = null;
            codeMap = medApi.getDiskInfo(map);
            arryList.add(codeMap);
        }
        return ResultKit.serviceResponse(arryList);
    }

    @Override
    public Map<String,Object> medtOkOrNot() {
        Map<String, String> param = new HashMap<>();
        List<FbsLiveCodec> codeList = liveCodecMapper.findAllLiveCodecForYw(param);
        int total = codeList.size();
        int bad = 0;
        for (FbsLiveCodec codec:codeList){
            param.put("ip", codec.getIp());
            param.put("port", codec.getPort());
            int statusCode=testWsdlConnection(getSocketAddress(param));
            if(statusCode==404){
                bad++;
                codec.setStatus("bad");
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("abnormal", bad);
        map.put("normal", total-bad);
        map.put("codeList", codeList);
        return map;
    }

    private  int testWsdlConnection(String address){
        int status = 404;
        try {
            URL urlObj = new URL(address);
            HttpURLConnection oc = (HttpURLConnection) urlObj.openConnection();
            oc.setUseCaches(false);
            oc.setConnectTimeout(10000); // 设置超时时间
            status = oc.getResponseCode();// 请求状态
            if (200 == status) {
                // 200是请求地址顺利连通。。
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    private String getSocketAddress(Map<String,String> param){
        return String.format("http://%s:%s/", param.get("ip"), param.get("port"));
    }
}
