package com.sogou.upd.passport.session.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-28
 * Time: 上午11:01
 */
public class ControllerHelper {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * API请求参数校验
     */
    public static <T> String validateParams(T requestParams) {

        StringBuilder sb = new StringBuilder();

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(requestParams);
        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            sb.append(constraintViolation.getMessage()).append(CommonConstant.SEPARATOR_1);
        }
        String validateResult;
        if (sb.length() > 0) {
            validateResult = sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            validateResult = "";
        }
        return validateResult;
    }

}
