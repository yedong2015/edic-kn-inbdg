package com.leaderrun.edic.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.leaderrun.edic.entity.Message;
import com.leaderrun.edic.service.OmsService;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;


/**
 * @author yedong
 * @date 20210323
 * @description KN新XML转换旧XML控制层
 */
@RestController
public class KnController {
    @Value("${authorization.val}")
    private String authorization_val;
    @Autowired
    private OmsService omsService;

    /**
     * 接收客户提交XML文件及验证
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    @ResponseBody
    @RequestMapping(value = "/shipment",method = RequestMethod.POST)
    public JSONObject shipment(HttpServletRequest request, HttpServletResponse response) throws IOException, DocumentException {
        String refName=request.getParameter("refName");
        String customerId=request.getParameter("customerId");
        String authorizationVal=request.getHeader("Authorization");
        if(authorization_val.equals(authorizationVal)){
            BufferedReader bufferedReader=request.getReader();
            String str=null;
            StringBuffer sbStr=new StringBuffer();
            while ((str=bufferedReader.readLine())!=null){
                sbStr.append(str);
            }
            bufferedReader.close();
            HashMap<String,String> map=new HashMap<>();
            map.put("refName",refName);
            map.put("customerId",customerId);
            map.put("Authorization",authorizationVal);
            map.put("newXml",sbStr.toString());
            JSONObject res= omsService.submitXmlToOms(map);
            return res;
        }else{
            Message message=new Message();
            message.setSuccess(false);
            message.setMessage("验证检查不通过");
            return JSONUtil.parseObj(message);
        }
    }
}
