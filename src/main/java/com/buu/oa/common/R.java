package com.buu.oa.common;

import lombok.Data;

/**
 * 统一接口返回结果
 * 封装所有接口的响应格式，保证前后端交互规范统一
 * @param <T> 响应数据泛型
 */
@Data
public class R<T> {
    /** 响应状态码：200成功，500失败 */
    private Integer code;
    /** 响应提示信息 */
    private String msg;
    /** 响应业务数据 */
    private T data;

    /**
     * 成功响应（携带数据）
     * @param data 接口返回数据
     * @return 统一响应对象
     */
    public static <T> R<T> success(T data) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     * @return 统一响应对象
     */
    public static <T> R<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     * @param message 错误描述信息
     * @return 统一响应对象
     */
    public static <T> R<T> fail(String message) {
        R<T> result = new R<>();
        result.setCode(500);
        result.setMsg(message);
        return result;
    }

    /**
     * 失败响应（自定义状态码）
     * @param code 错误状态码
     * @param message 错误描述信息
     * @return 统一响应对象
     */
    public static <T> R<T> fail(Integer code, String message) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMsg(message);
        return result;
    }
}
