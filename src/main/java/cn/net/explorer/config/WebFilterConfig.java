package cn.net.explorer.config;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;


@Component
public class WebFilterConfig implements Filter {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${server.ip}")
    private String ip;

    @Value("${server.port}")
    String port;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        //获取请求uri
        String requestUri = httpRequest.getRequestURI();
        String type = httpRequest.getParameter("type");
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");


        if (requestUri.contains(".json") && StringUtils.isEmpty(type)) {
            logger.info("即将要请求的地址:" + ip + ":" + port + "/" + requestUri);
            String body = HttpUtil.createGet(ip + ":" + port + "/" + requestUri + "?type=666").execute().body();
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setCharacterEncoding("UTF-8");
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(body.getBytes());
            outputStream.flush();
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {

    }

    private String getBodyData(HttpServletRequest request) {
        BufferedReader reader = null;
        StringBuffer data = new StringBuffer();
        try {
            String line = "";
            reader = request.getReader();
            while (null != (line = reader.readLine())) {
                data.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null == reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data.toString();
    }
}
