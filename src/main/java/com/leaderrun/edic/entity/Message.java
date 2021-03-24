package com.leaderrun.edic.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yedong
 * @date 2021-03-24
 * @description 消息返回实体类
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message implements Serializable {
    private Boolean success;
    private String message;
    private String id;
    private String commReference;
    private String logicalAddress;
    private String physicalAddress;
    private String email;
    private String bookingNo;
}
