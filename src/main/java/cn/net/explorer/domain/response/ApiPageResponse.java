package cn.net.explorer.domain.response;

import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.omg.CORBA.Object;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiPageResponse<T> {

    /**
     * 响应code码
     */
    private Integer code;


    private ApiPageData<T> data;

    /**
     * 总条数
     */
    private String msg;


    public static <T> ApiPageResponse<T> page(IPage<T> page) {
        return ApiPageResponse.<T>builder().data(new ApiPageData<T>(page.getRecords(),page.getTotal())).code(HttpStatus.HTTP_OK).build();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ApiPageData<T>{
        private List<T> list;
        private Long total;
    }
}
