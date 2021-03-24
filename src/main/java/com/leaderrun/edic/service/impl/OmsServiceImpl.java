package com.leaderrun.edic.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.leaderrun.edic.entity.Message;
import com.leaderrun.edic.service.OmsService;
import com.leaderrun.edic.util.KnUtil;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yedong
 * @date 2021-03-24
 * @description KN专用OMS服务实现类
 */
@Service
public class OmsServiceImpl implements OmsService{
    @Value("${oms.url}")
    String omsUrl;
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
    @Override
    public JSONObject submitXmlToOms(HashMap<String,String> paramMap) throws DocumentException {
        String refName=paramMap.get("refName");
        String customerId=paramMap.get("customerId");
        String Authorization=paramMap.get("Authorization");
        String newXml=paramMap.get("newXml");
        String oldXml=KnUtil.newXmlToOldXmlTemplate(newXml);
        Map<String,Object> map=new HashMap();
        map.put("refName",refName);
        map.put("customerId",customerId);
        String result= HttpRequest.post(omsUrl+"?"+HttpUtil.toParams(map))
                .header(Header.AUTHORIZATION,Authorization)
                .header(Header.CONTENT_TYPE,"application/xml")
                .body(StrUtil.utf8Bytes(oldXml))
                .execute().body();
        Message message=JSONUtil.toBean(result, Message.class);
        String result2=JSONUtil.toJsonStr(message);
        return JSONUtil.parseObj(result2);
    }
}
