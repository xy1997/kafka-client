package cn.net.explorer.domain.response;

import cn.hutool.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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



    private List<T> data;

    /**
     * 总条数
     */
    private Long count;

    /**
     * 分页条数
     */
  //  private Long pageSize;

    /**
     * 当前页
     */
    //private Long pageNum;

    /**
     * 分页
     */
    public static <T> ApiPageResponse<T> page(Long count, List<T> data) {
        //构建page信息
        return ApiPageResponse.<T>builder().data(data).count(count).code(HttpStatus.HTTP_OK).build();
    }
}
