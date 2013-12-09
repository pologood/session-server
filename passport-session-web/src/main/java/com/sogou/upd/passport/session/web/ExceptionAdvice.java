package com.sogou.upd.passport.session.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-6
 * Time: 下午5:44
 */
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String handleIOException(Exception ex) {
        return "{\"status\":\"10001\",\"statusText\":\"系统级错误\"}";
    }
}