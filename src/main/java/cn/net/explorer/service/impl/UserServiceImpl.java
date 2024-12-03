package cn.net.explorer.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {


    @Override
    public ApiResponse<String> login(  JSONObject parameter) {
        if (!Objects.equals(parameter.getString("username"), System.getenv("ENV_USERNAME"))) throw new BusinessException("用户名不存在");
        if (!Objects.equals(parameter.getString("password"), System.getenv("ENV_PASSWORD"))) throw new BusinessException("密码错误");

        StpUtil.login(1);
        return ApiResponse.ok(StpUtil.getTokenValue());
    }

    @Override
    public ApiResponse<JSONObject> info() {
        JSONObject jb = new JSONObject();
        jb.put("id", 1);
        jb.put("username",  System.getenv("ENV_USERNAME"));
        return ApiResponse.ok(jb);
    }
}
