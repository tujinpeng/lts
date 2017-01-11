package com.github.ltsopensource.alarm;

/**
 * @author Robert HG (254963746@qq.com)  on 2/17/16.
 */
public enum AlarmType {
    BLOCK("消息阻塞报警");

    private String desc;

    AlarmType(String desc){
        this.desc = desc;
    }
}
