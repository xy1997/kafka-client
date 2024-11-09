package cn.net.explorer.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author xingy
 * @date 2020/12/23 10:15
 **/
public class ThrowableUtil {

    /**
     * 获取堆栈信息
     */
    public static String getStackTrace(Throwable throwable){
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }



}
