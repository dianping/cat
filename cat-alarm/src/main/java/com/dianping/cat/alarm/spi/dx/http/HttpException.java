/**
 * 
 */
package com.dianping.cat.alarm.spi.dx.http;

/**
 * @author zhangdongxiao
 * @created 2013-3-8
 * @since 1.0
 *
 */
public class HttpException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 6512995684962626608L;

    private int status;
    private String reason;
    private String content;

    public int getStatus() {
        return status;
    }
    

    public String getReason() {
        return reason;
    }
    


    public String getContent() {
        return content;
    }


    public HttpException(int status, String reason) {
        super("status="+status+",reason="+reason);
        this.status=status;
        this.reason=reason;
    }
    public HttpException(int status, String reason, Throwable th) {
        super("status="+status+",reason="+reason,th);
        this.status=status;
        this.reason=reason;
    }

    public HttpException(int status, String reason, String content) {
        super("status="+status+",reason="+reason+",content="+content);
        this.status=status;
        this.reason=reason;
        this.content=content;
    }
}