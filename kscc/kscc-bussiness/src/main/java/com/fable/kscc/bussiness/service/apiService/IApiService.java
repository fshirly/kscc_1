package com.fable.kscc.bussiness.service.apiService;

import com.fable.kscc.api.model.fbsInterface.FbsInterface;
import com.fable.kscc.api.model.hospitalInformation.FbsHospitalInformation;
import com.fable.kscc.api.model.livebroadcast.FbsLiveBroadcast;
import com.fable.kscc.api.model.page.PageRequest;
import com.fable.kscc.api.model.page.PageResponse;
import com.fable.kscc.api.model.page.ServiceResponse;

import javax.xml.ws.Service;
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
 * Time :15:34
 * </p>
 * <p>
 * Department :
 * </p>
 * <p> Copyright : 江苏飞博软件股份有限公司 </p>
 */
public interface IApiService {

    ServiceResponse getAllHospitalList();

    PageResponse<List> getDisk(PageRequest<Map<String,String>> param);

    PageResponse<FbsInterface> getMedInterfaceInfo(PageRequest<Map<String,Object>> param);

    ServiceResponse getDiskInfo(Map<String,String> param);

    Map<String,Object> medtOkOrNot();
}
