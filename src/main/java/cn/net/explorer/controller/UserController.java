package cn.net.explorer.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("classpath:/static/data/nav.json")
    private Resource resource;

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody JSONObject parameter) {
        return userService.login(parameter);
    }

    @GetMapping("/info")
    public ApiResponse<JSONObject> info() {
        return userService.info();
    }

    @GetMapping("/menus")
    public JSONObject menus() throws Exception {
        String str = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        return JSONObject.parseObject(str);
    }

    @PostMapping("/loginOut")
    public ApiResponse<?> loginOut(){
        StpUtil.logout();
        return ApiResponse.ok();
    }

}
