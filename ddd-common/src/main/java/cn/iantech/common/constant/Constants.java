package cn.iantech.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class Constants {

    public static final String SPLIT = ",";

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode {

        SUCCESS("SUCCESS", "成功"),
        INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误"),
        INVALID_ARGUMENT("INVALID_ARGUMENT", "请求参数不合法"),
        AUTH_REQUIRED("AUTH_REQUIRED", "需要认证"),
        AUTH_UNAVAILABLE("AUTH_UNAVAILABLE", "认证服务暂不可用"),
        AUTH_REFRESH_BUSY("AUTH_REFRESH_BUSY", "刷新请求处理中，请稍后重试"),
        AUTH_RATE_LIMITED("AUTH_RATE_LIMITED", "登录尝试过于频繁，请稍后重试"),
        ACCESS_DENIED("ACCESS_DENIED", "无权访问"),
        RPC_ERROR("RPC_ERROR", "下游服务调用失败"),
        RPC_NO_PROVIDER("RPC_NO_PROVIDER", "下游服务暂无可用提供者"),
        RPC_TIMEOUT("RPC_TIMEOUT", "下游服务调用超时"),
        ;

        private String code;
        private String info;

    }

}
