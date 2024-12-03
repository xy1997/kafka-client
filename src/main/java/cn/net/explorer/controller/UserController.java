package cn.net.explorer.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.net.explorer.ExplorerApplication;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.service.UserService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserService userService;

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/print")
    public ApiResponse<?> print(){
        String username = System.getenv("ENV_USERNAME");
        logger.info("username : {}",username);
        return ApiResponse.ok(username);
    }

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
        // 获取资源
        Resource resource = resourceLoader.getResource("classpath:/static/data/nav.json");

        // 读取文件内容并转换为字符串
        try (InputStream inputStream = resource.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
            return JSONObject.parseObject(stringBuilder.toString());
        }

    }

    @PostMapping("/loginOut")
    public ApiResponse<?> loginOut(){
        StpUtil.logout();
        return ApiResponse.ok();
    }

}
