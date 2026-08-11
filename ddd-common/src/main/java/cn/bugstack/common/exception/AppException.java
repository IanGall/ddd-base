package cn.bugstack.common.exception;

public class AppException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /** 异常码 */
    private final String code;

    /** 异常信息 */
    private final String info;

    public AppException(String code) {
        super(code);
        this.code = code;
        this.info = null;
    }

    public AppException(String code, Throwable cause) {
        super(code, cause);
        this.code = code;
        this.info = null;
    }

    public AppException(String code, String message) {
        super(message);
        this.code = code;
        this.info = message;
    }

    public AppException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.info = message;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

}
