package cn.net.explorer.controller;

import cn.hutool.core.date.DateUtil;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.service.BrokerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {

    @Resource
    private BrokerService brokerService;

    @GetMapping("/get")
    public String get() {
        return "get hello";
    }

    public static void main(String[] args) {
        String now = DateUtil.now();
        System.out.println(now);
    }
}
