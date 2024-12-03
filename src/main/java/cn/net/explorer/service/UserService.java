package cn.net.explorer.service;

import cn.net.explorer.domain.response.ApiResponse;
import com.alibaba.fastjson.JSONObject;

public interface UserService {

    ApiResponse<String> login(JSONObject parameter);

    ApiResponse<JSONObject> info();
}
