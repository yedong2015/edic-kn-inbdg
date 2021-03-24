package com.leaderrun.edic.util;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.List;

/**
 * @author yedong
 * @date 2021-03-24
 * @description 客户KN专用工具
 */
public class KnUtil {
    static final Logger logger=LoggerFactory.getLogger(KnUtil.class);
    /**
     * 新XML模板转换为旧XML模板
     * @param xmlStr 待转换的XML字符串
     * @return 返回转换后的旧XML字符串
     */
    public static String newXmlToOldXmlTemplate(String xmlStr) throws DocumentException {
        //读取xml文件产生document对象
        SAXReader saxReader=new SAXReader();
        InputSource source=new InputSource(new StringReader(xmlStr));
        //XML字符串读取方式
        Document document=saxReader.read(source);
        //检查一下是否新XML文件，不是的话直接返回旧XML，不做任何处理
        Element element_check=document.getRootElement();
        String check_name=element_check.getName();
        if(check_name.equals("KNLOBI")){
            return xmlStr;
        }
        //创建XML文档
        Document createDocument= DocumentHelper.createDocument();
        //创建根节点
        Element root=createDocument.addElement("KNLOBI");

        /**
         * 1.开始填充MSGHDR节点的数据
         * 已处理
         * /KNLOBI/MSGHDR/LOGSND
         * /KNLOBI/MSGHDR/PHYSND
         */
        Element msghdr_point=root.addElement("MSGHDR");
        List<Node> list_20=document.selectNodes("//ns24:SEASHP//ns3:MSGHDR//REC");
        for (Node node:list_20){
            Element element=(Element) node;
            Element rec_point=msghdr_point.addElement("REC").addAttribute("ADDTYP",element.attributeValue("ADRTYP"));
            rec_point.addElement("PHYREC").addText(element.elementText("PHYREC"));
            rec_point.addElement("LOGREC").addText(element.elementText("LOGREC"));
        }
        msghdr_point.addElement("PHYSND").addText((document.selectSingleNode("/ns24:SEASHP/ns3:MSGHDR/PHYSND")).getStringValue());
        msghdr_point.addElement("LOGSND").addText((document.selectSingleNode("/ns24:SEASHP/ns3:MSGHDR/LOGSND")).getStringValue());
        msghdr_point.addElement("SNDAPP").addText((document.selectSingleNode("/ns24:SEASHP/ns3:MSGHDR/SNDAPP")).getStringValue());
        msghdr_point.addElement("MSGTYP").addText((document.selectSingleNode("/ns24:SEASHP/ns3:MSGHDR/MSGTYP")).getStringValue());
        msghdr_point.addElement("VERNUM").addText((document.selectSingleNode("/ns24:SEASHP/ns3:MSGHDR/VERNUM")).getStringValue());
        msghdr_point.addElement("CREDTM").addText((document.selectSingleNode("/ns24:SEASHP/ns3:MSGHDR/CREDTM")).getStringValue());
        /**
         * 2.创建S节点
         */
        Element s_point=root.addElement("S");
        /**
         * 2.1.创建MSGLVL节点
         */
        Element msglvl_point=s_point.addElement("MSGLVL");
        Node node_20=document.selectSingleNode("//ns24:SEASHP//ns4:MSGLVL//TRCNUM");
        Element element_trcnum=(Element) node_20;
        msglvl_point.addElement("TRCNUM").addAttribute("CTR",element_trcnum.attributeValue("CTR")).addText(element_trcnum.getText());
        msglvl_point.addElement("MODTRS").addText(document.selectSingleNode("//ns24:SEASHP//ns4:MSGLVL//SNDINF//MODTRS").getText());
        //没有对应字段，使用默认值代替  /KNLOBI/S/MSGLVL/REF[@ADDTYP='FF']/CDE
        msglvl_point.addElement("MSGNUM").addText("172973363");
        /**
         * 2.1.1.创建STSINF节点
         * 邮箱
         * 已处理
         * /KNLOBI/S/MSGLVL/STSINF/ADDSTSINF/USRIDE
         */
        List<Node> list_21=document.selectNodes("//ns24:SEASHP//ns5:STSINF");
        for(Node node:list_21){
            Element element=(Element) node;
            Element element1=msglvl_point.addElement("STSINF").addAttribute("STSCDE",element.attributeValue("STSCDE"));
            //WMS文档指定的是这个值对应 ns24:SEASHP/SHP/SHPSEA/ROU/ns14:ROUSEA/SEASCD/LEG/POL/EXCTME/DTE
            Node node_dte=document.selectSingleNode("//ns24:SEASHP//SHP//SHPSEA//ROU//ns14:ROUSEA//SEASCD//LEG//POL//EXCTME//DTE");
            if (StringUtils.isEmpty(node_dte)) {
                element1.addElement("STSDTE").addElement("VAL").addText(element.element("DTE").getText());
            }else {
                element1.addElement("STSDTE").addElement("VAL").addText(node_dte.getText());
            }
            element1.addElement("STSTME").addElement("VAL").addText(element.element("TME").getText());

            List<Element> list_22=element.elements("STSLOC");
            for(Element element2:list_22){
                //LOCTYP没有属性字段对应，暂时使用172替代
                Element element3=element1.addElement("LOC").addAttribute("LOCTYP","172");
                element3.addElement("LOCVAL").addAttribute("LOCISS",element2.attributeValue("LOCISS")).addText(element2.getText());
            }

            Element element2=element.element("ADDSTSINF");
            Element element4=element1.addElement("ADDSTSINF");
            //没有对应字段对应，暂时使用16代替
            element4.addElement("MSGIDEFLG").addText("16");
            //没有值替换，暂时留空
            element4.addElement("CREIDE").addText("");
            element4.addElement("ENTDTM").addElement("VAL").addText(element2.elementText("ENTDTM"));
            element4.addElement("USRIDE").addText(element2.elementText("USRIDE"));
            if(StringUtils.isEmpty(element2.elementText("REM"))){
                element4.addElement("REM").addText(" ");
            }else {
                element4.addElement("REM").addText(element2.elementText("REM"));
            }

        }
        msglvl_point.addElement("EXPIMPFLG").addText(document.selectSingleNode("//ns24:SEASHP//ns4:MSGLVL//SNDINF//EXPIMPFLG").getText());
        /**
         * 2.1.2.创建REF节点
         * 已处理
         * /KNLOBI/S/MSGLVL/REF/VAL[../CDE[text()='AQY']]
         * /KNLOBI/S/MSGLVL/REF/VAL[../CDE[text()='INN']]
         * 已处理
         * /KNLOBI/S/MSGLVL/REF[@ADDTYP='FF']/CDE
         * /KNLOBI/S/MSGLVL/REF[@ADDTYP='FF']/VAL
         */
        List<Node> list_22=document.selectNodes("//ns24:SEASHP//ns4:MSGLVL//REF");
        for(Node node:list_22){
            Element element=(Element) node;
            Element element_1= msglvl_point.addElement("REF").addAttribute("ADDTYP",element.attributeValue("ADRTYP"));
            element_1.addElement("CDE").addText(element.elementText("CDE"));
            element_1.addElement("VAL").addText(element.elementText("VAL"));
        }
        Element el_inn=msglvl_point.addElement("REF").addAttribute("ADDTYP","FF");
        el_inn.addElement("CDE").addText("AQY");
        //文档中有指定的值对应//ns24:SEASHP//ns4:MSGLVL//TRCNUM[@CTR='3'],读取的数据长度是10位数字（如：3251160043）；OMS目前读取最少14位数据
        Node node_ctr=document.selectSingleNode("//ns24:SEASHP//ns4:MSGLVL//TRCNUM");
        //SO唯一编号
        String bookNo=node_ctr.getText();
        //OMS系统分割读取数据，保证14位数据的长度，不足部分暂时使用0000补足
        el_inn.addElement("VAL").addText(bookNo+"0000");

        /**
         * 2.2.创建SHPLVL节点
         */
        Element shplvl_point=s_point.addElement("SHPLVL");
        /**
         * 2.2.1创建SHPINF节点
         * 旧XML样板当中没有REF标签数据,新文档当中也没有REF标签数据（整个新XML文档中只有/KNLOBI/S/MSGLVL/REF下存在REF标签，是否使用此填充？？）
         * 已处理
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SHI']
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SHI']/../VAL
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SWL']/../VAL
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SWL']
         */
        List<Node> list_30=document.selectNodes("//ns24:SEASHP//SHP//SHPINF//ns7:ADR[@ns2:ADRTYP='SH']//REF");
        Element shpinf_point=shplvl_point.addElement("SHPINF");
        for(Node node:list_30){
            Element element=(Element) node;
            String cde_val=element.elementText("CDE");
            String val_val=element.elementText("VAL");
            Element ele_ref=shpinf_point.addElement("REF").addAttribute("ADDTYP","SH");
            ele_ref.addElement("CDE").addText(cde_val);
            ele_ref.addElement("VAL").addText(val_val);
        }

        /**
         * 2.2.1.1创建CNI节点
         * 已处理
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/TOTPAC
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/MEA[@MEATYP='AAD']/VAL
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/MEA[@MEATYP='ABJ']/VAL
         * 已处理
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/SEA/ETSDTE/VAL  新XML中ns24:SEASHP/SHP/SHPSEA/ROU/ns14:ROUSEA/SEASCD/LEG/POL/EXCTME/DTE标签替换
         */
        Element cni_element=shpinf_point.addElement("CNI");
        List<Node> list_23=document.selectNodes("//ns24:SEASHP//SHP//SHPINF//ns6:SHPDTL");
        for(Node node:list_23){
            Element element=(Element) node;
            //没有对应字段替换，暂时使用61替换
            cni_element.addElement("TOTPAC").addText(element.elementText("TOTPAC"));

            List<Node> list=element.elements("MEA");
            for (Node node1:list){
                Element element1=(Element) node1;
                Element mea_element=cni_element.addElement("MEA").addAttribute("MEATYP",element1.attributeValue("MEATYP"));
                mea_element.addElement("VAL").addText(element1.elementText("VAL"));
                mea_element.addElement("UOMCDE").addText(element1.elementText("UOMCDE"));
            }

            cni_element.addElement("DGSGOOFLG").addText(element.elementText("DGSGOOFLG"));

            Element element1=cni_element.addElement("TOD");
            element1.addElement("INC").addText(element.element("TRMTRD").element("INC").getText());
            //没有对应属性LOCTYP使用1代替，属性LOCISS使用22代替
            element1.addElement("LOC").addAttribute("LOCTYP","1").addElement("LOCVAL")
                    .addAttribute("LOCISS","22").addText(element.element("TRMTRD").element("INCLOC").getText());

            //没有对应的标签对应，使用旧档案的旧数据暂时替换
            //wms文档提示使用 ns24:SEASHP/SHP/SHPSEA/ROU/ns15:ONCAR/ENDPNT/<LOC@ns2:LOCISS="6"替换LOCVAL的值
            Node node_10=document.selectSingleNode("//ns24:SEASHP//SHP//SHPSEA//ROU//ns15:ONCAR//ENDPNT//LOC[@ns2:LOCISS='6']");
            Element element3=cni_element.addElement("PLCDEL");
            if(StringUtils.isEmpty(node_10)){
                element3.addElement("LOC").addElement("LOCVAL").addAttribute("LOCISS","3").addText("LPL");
            }else{
                element3.addElement("LOC").addElement("LOCVAL").addAttribute("LOCISS","6").addText(node_10.getText());
            }
            //没有对应的标签对应，使用旧档案的旧数据暂时替换
            Element element4=cni_element.addElement("SEA");

            //wms文档提示对应的是  ns24:SEASHP/SHP/SHPSEA/ROU/ns14:ROUSEA/SEASCD/LEG/POD/LOC@ns2:LOCTYP="202"@ns2:LOCISS="6"
            Node node_loc=document.selectSingleNode("//ns24:SEASHP//SHP//SHPSEA//ROU//ns14:ROUSEA//SEASCD//LEG//POD//LOC[@ns2:LOCTYP='202' and @ns2:LOCISS='6']");
            if(StringUtils.isEmpty(node_loc)){
                element4.addElement("POTDIS").addElement("LOCVAL").addAttribute("LOCISS","6").addText(" ");
            }else {
                element4.addElement("POTDIS").addElement("LOCVAL").addAttribute("LOCISS","6").addText(node_loc.getText());
            }

            //文档中提示使用新XML替换的日期，暂时注析掉，出问题再处理
            Node node_4=document.selectSingleNode("//ns14:ROUSEA//SEASCD//LEG//POL//EXCTME//DTE");
            cni_element.addElement("SEA").addElement("ETSDTE").addElement("VAL").addText(node_4.getText());

        }
        /**
         * 2.2.1.2.创建SHPSEA节点
         * 已处理
         * /KNLOBI/S/SHPLVL/SHPINF/SHPSEA/SEADTL/SHPMOVCDE         *
         */
        Element shpsea_element=shpinf_point.addElement("SHPSEA");
        List<Node> list_24=document.selectNodes("//ns24:SEASHP//SHP//SHPSEA//ns12:SEADTL");
        for(Node node:list_24){
            Element element=(Element) node;
            Element element1=shpsea_element.addElement("SEADTL");
            //没有对应字段，使用旧版默认替换
            element1.addElement("FRTPAYTRM").addText("FC");
            //没有对应字段，使用旧版默认替换
            element1.addElement("MODTRS").addText("3");
            element1.addElement("SHPMOVCDE").addText(element.elementText("SHPMOVCDE"));
        }

        List<Node> list_25=document.selectNodes("//ns24:SEASHP//SHP//SHPSEA//ROU//ns14:ROUSEA");
        for(Node node:list_25){
            Element element=(Element)node;
            Element element1=shpsea_element.addElement("ROUSEA");
            Element element4=element1.addElement("ROUSEADTL");
            element4.addElement("CARSCACDE").addText(element.elementText("CARSCACDE"));
            Element element8=element.element("SEASCD").element("LEG").element("MCADTL");
            //wms文档提示使用ns24:SEASHP/SHP/SHPSEA/ROU/ns14:ROUSEA/SEASCD/LEG/MCADTL/VOYNUM代替VOYNUM
            element4.addElement("VOYNUM").addText(element8.elementText("VOYNUM"));
            //wms文档提示使用ns24:SEASHP/SHP/SHPSEA/ROU/ns14:ROUSEA/SEASCD/LEG/MCADTL/VSLNAM替代
            element4.addElement("VSLNAM").addText(element8.elementText("VSLNAM"));
            element4.addElement("VSLFLG").addText(element8.elementText("VSLFLG"));


        }

        /**
         * 2.2.1.3.创建CGODSC节点
         * 能否请客户解析一下文档中的新旧XML对应关系？？？
         * 已处理
         * 估计对应的是新XML标签中的这个//SHPINF//CGODSC//ns8:CGODTL，有一个问题是不知道SEQNUM编码的对应关系，因为OMS系统需要SEQNUM=1,2值的都需要读取处理.
         * /KNLOBI/S/SHPLVL/SHPINF/CGODSC[@SEQNUM='1']/PAC/TYP
         * /KNLOBI/S/SHPLVL/SHPINF/CGODSC[@SEQNUM='2']/CTT
         */
        List<Node> list_26=document.selectNodes("//ns24:SEASHP//SHP//SHPINF//CGODSC//ns8:CGODTL");
        int seqnum=0;
        for (Node node:list_26){
            seqnum++;
            Element element=(Element) node;
            Element cgodsc_element_1=shpinf_point.addElement("CGODSC").addAttribute("SEQNUM",seqnum+"");
            //wms文档提示使用ns24:SEASHP/SHP/SHPINF/CGODSC/ns8:CGODTL/MKSNUM 多个拼接替代
            List<Element> list_element=element.elements("MKSNUM");
            StringBuffer ss=new StringBuffer();
            for(Element element1:list_element){
                //使用；分割
                ss.append(element1.getText()+";");
            }
            if(StringUtils.isEmpty(ss)){
                cgodsc_element_1.addElement("MKSNUM").addText(" ");
            }else{
                cgodsc_element_1.addElement("MKSNUM").addText(ss.toString());
            }

            //WMS文档提示使用ns24:SEASHP/SHP/SHPINF/CGODSC/ns8:CGODTL/CGOCTT  多个拼接代替
            List<Element> list_cgoctt=element.elements("CGOCTT");
            StringBuffer sb=new StringBuffer();
            for(Element element2:list_cgoctt){
                //使用；分割
                sb.append(element2.getText()+";");
            }
            if(StringUtils.isEmpty(sb)){
                cgodsc_element_1.addElement("CTT").addText(" ");
            }else{
                cgodsc_element_1.addElement("CTT").addText(sb.toString());
            }

            Element element6=element.element("NUMPAC");
            Element element_pac=cgodsc_element_1.addElement("PAC");
            element_pac.addElement("VAL").addText(element6.elementText("VAL"));
            //WMS文档提示使用 ns24:SEASHP/SHP/SHPINF/CGODSC/ns8:CGODTL/NUMPAC/TYP替换
            element_pac.addElement("TYP").addText(element6.elementText("TYP"));

            //货物重量信息
            Element element2=element.element("GRSWGT");
            Element element3=cgodsc_element_1.addElement("MEA").addAttribute("MEATYP","ADD");
            element3.addElement("VAL").addText(element2.elementText("VAL"));
            element3.addElement("UOMCDE").addText(element2.elementText("UOMCDE"));
            //货物体积信息
            Element element4=element.element("VOL");
            Element element5=cgodsc_element_1.addElement("MEA").addAttribute("MEATYP","ABJ");
            element5.addElement("VAL").addText(element4.elementText("VAL"));
            element5.addElement("UOMCDE").addText(element4.elementText("UOMCDE"));
        }

        /**
         * 2.2.1.4.创建NADINF节点
         * 已处理
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF/CTA/COM[@COMTYP='EM']
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='CU']/NAD/IDE
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='FF']/CTA/NAM
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='SH']/NAD/NAM
         */
        List<Node> list_27=document.selectNodes("//SHP//SHPINF//ns7:ADR|//SHP//SHPSEA//ns7:ADR");
        for(Node node:list_27){
            Element element=(Element) node;
            String adr_ff=element.attributeValue("ADRTYP");
            Element nadinf_point=shpinf_point.addElement("NADINF").addAttribute("ADDTYP",element.attributeValue("ADRTYP"));
            Element nad_point=nadinf_point.addElement("NAD");
            nad_point.addElement("NAM").addText(element.elementText("NAM"));
            List<Element> list_add=element.elements("STR");
            for(Element element1:list_add){
                nad_point.addElement("ADD").addText(element.elementText("STR"));
            }
            nad_point.addElement("CTY").addText(element.elementText("CTY"));
            nad_point.addElement("CNYCDE").addText(element.elementText("CNYCDE"));
            if(!StringUtils.isEmpty(element.elementText("IDE"))){
                nad_point.addElement("IDE").addText(element.elementText("IDE"));
            }
            if(!StringUtils.isEmpty(element.elementText("ZIPCDE"))){
                nad_point.addElement("ZIPCDE").addText(element.elementText("ZIPCDE"));
            }
            ///KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='FF']/CTA/NAM   不存在报错，默认添加一个进去
            //wms文档使用ns24:SEASHP/SHP/SHPSEA/ROU/ns14:ROUSEA/CSLINF/EXPOFC/DPTCDE代替NAM标签值
            Element element_dptcde= (Element) document.selectSingleNode("//ns24:SEASHP//SHP//SHPSEA//ROU//ns14:ROUSEA//CSLINF//EXPOFC//DPTCDE");
            if("FF".equals(adr_ff)){
                Element ele_cat=nadinf_point.addElement("CTA");
                if(StringUtils.isEmpty(element_dptcde)){
                    ele_cat.addElement("NAM").addText(" ");
                }else{
                    ele_cat.addElement("NAM").addText(element_dptcde.getText());
                }
            }
            Element element_26=element.element("CTAINF");
            //判断一下标签节点是否存在
            if(!StringUtils.isEmpty(element_26)){
                String name=element_26.elementText("FSTNAM")+" "+element_26.elementText("LSTNAM");
                Element cta_point=nadinf_point.addElement("CTA");
                if(StringUtils.isEmpty(element_dptcde)){
                    cta_point.addElement("NAM").addText(name);
                }else{
                    cta_point.addElement("NAM").addText(element_dptcde.getText());
                }
                List<Element> list_com=element_26.elements("COM");
                for(Element element2:list_com){
                    cta_point.addElement("COM").addAttribute("COMTYP",element2.attributeValue("COMTYP")).addText(element2.getText());
                }
            }
        }

        StringWriter sw=new StringWriter();
        OutputFormat format=OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        //行缩进
        format.setIndentSize(3);
        //一个节点为一行
        format.setNewlines(true);
        //去重复空格
        format.setTrimText(true);
        format.setPadText(true);
        //放置XML文件第二行为空白行
        format.setNewLineAfterDeclaration(false);
        try {
           XMLWriter xmlWriter=new XMLWriter(sw,format);
           xmlWriter.write(createDocument);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                sw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * 进行必要的日志输出
         * /KNLOBI/S/MSGLVL/REF/VAL[../CDE[text()='AQY']]  SO号码
         * /KNLOBI/S/MSGLVL/REF/VAL[../CDE[text()='INN']]
         * /KNLOBI/S/MSGLVL/REF[@ADDTYP='FF']/CDE
         * /KNLOBI/S/MSGLVL/REF[@ADDTYP='FF']/VAL
         *
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SHI']
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SHI']/../VAL
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SWL']/../VAL
         * /KNLOBI/S/SHPLVL/SHPINF/REF[@ADDTYP='SH']/CDE[text()='SWL']
         *
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/TOTPAC
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/MEA[@MEATYP='AAD']/VAL
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/MEA[@MEATYP='ABJ']/VAL
         * /KNLOBI/S/SHPLVL/SHPINF/CNI/SEA/ETSDTE/VAL  代表截止日期
         *
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF/CTA/COM[@COMTYP='EM']
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='CU']/NAD/IDE
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='FF']/CTA/NAM
         * /KNLOBI/S/SHPLVL/SHPINF/NADINF[@ADDTYP='SH']/NAD/NAM
         *
         * /KNLOBI/S/SHPLVL/SHPINF/SHPSEA/SEADTL/SHPMOVCDE
         *
         * /KNLOBI/S/SHPLVL/SHPINF/CGODSC[@SEQNUM='1']/PAC/TYP
         * /KNLOBI/S/SHPLVL/SHPINF/CGODSC[@SEQNUM='2']/CTT
         */
        logger.info("bookNo：{}", bookNo);
        return sw.toString();
    }
}
