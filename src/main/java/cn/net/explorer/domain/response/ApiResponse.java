package cn.net.explorer.domain.response;

import cn.hutool.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private static final long serialVersionUID = 1L;

    /**
     * 响应code码
     */
    private Integer code;

    /**
     * 响应错误信息
     */
    private String msg;

    /**
     * 响应结果
     */
    private T data;


    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder().code(HttpStatus.HTTP_OK).build();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().code(HttpStatus.HTTP_OK).data(data).build();
    }

    public static <T> ApiResponse<T> fail() {
        return ApiResponse.<T>builder().code(HttpStatus.HTTP_INTERNAL_ERROR).msg("系统异常").build();
    }

    public static <T> ApiResponse<T> fail(Integer code, String description) {
        return ApiResponse.<T>builder().code(code).msg(description).build();
    }


    public static <T> ApiResponse<T> fail(String description) {
        return ApiResponse.<T>builder().code(HttpStatus.HTTP_INTERNAL_ERROR).msg(description).build();
    }



    /**
     * 分页
     */
/*
    public static <T> ApiResponse<ApiPageResponse<T>> page(IPage<T> page) {
        //构建page信息
         return ApiResponse.page(page.getCurrent(), page.getPages(), page.getTotal(), page.getRecords());
    }
*/

}
