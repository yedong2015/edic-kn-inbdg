package com.leaderrun.edic.service;

import cn.hutool.json.JSONObject;
import org.dom4j.DocumentException;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author yedong
 * @date 2021-03-24
 * @description KN专用OMS服务类
 */
public interface OmsService {
    /**
     * kn新XML文件转换为旧XML文件并且提交到OMS服务器
     * @param paramMap
     * 带4种KEY值，如下：
     * refName代表上传的文件名称，
     * customerId代表客户ID，
     * Authorization代表授权验证码，
     * newXml代表代转换的新XML文件字符串
     * @return
     * 返回处理结果的json对象
     */
    JSONObject submitXmlToOms(HashMap<String,String> paramMap) throws DocumentException;
}
