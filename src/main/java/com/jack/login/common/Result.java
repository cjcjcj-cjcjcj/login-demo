package com.jack.login.common;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public Result(){};
    public Result(int code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> Result<T> ok(T data){
        return new Result<>(200,"success",data);
    }
    public static <T> Result<T> fail(int code,String msg){
        return new Result<>(code,msg,null);
    }

    public int getCode(){
        return this.code;
    }
    public void setCode(int code){
        this.code=code;
    }
    public String getMsg(){
        return this.msg;
    }
    public void setMsg(String msg){
        this.msg=msg;
    }
    public T getData(){
        return this.data;
    }
    public void setData(T data){
        this.data=data;
    }
}



