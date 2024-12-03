package cn.net.explorer.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Value("${sa-token.default-username}")
    private String defaultUserName;
    @Value("${sa-token.default-password}")
    private String defaultPassword;

    @Override
    public ApiResponse<String> login(  JSONObject parameter) {
        String username = parameter.getString("username");
        String password = parameter.getString("password");
        if (!Objects.equals(username, defaultUserName)) throw new BusinessException("用户名不存在");
        if (!Objects.equals(password, defaultPassword)) throw new BusinessException("密码错误");

        StpUtil.login(1);
        return ApiResponse.ok(StpUtil.getTokenValue());
    }

    @Override
    public ApiResponse<JSONObject> info() {
        JSONObject jb = new JSONObject();
        jb.put("id", 1);
        jb.put("username", defaultUserName);
        return ApiResponse.ok(jb);
    }
}
