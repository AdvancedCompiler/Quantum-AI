package com.rf.AIquantum.utils;

import com.alibaba.fastjson.JSON;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;

/**
 * @Description:xml工具
 * @Author: zsf
 * @Date: 2022/8/12
 */
public class XMLUtil {

    /**
     * 选择需要返回的格式: 1、返回 List<Map>\2、返回 Map
     * @param xml
     * @return
     */
    @SuppressWarnings("all")
    public static Map<String, Object> parseXML(String xml) throws DocumentException {
        // 1、 List<Map>
        List<Map<String, Object>> resultList = new ArrayList<>();
        // 2、 Map
        Map<String, Object> resultMap = new HashMap<>();
        Document document = null;
        document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();
        Iterator<Element> rootIter = root.elementIterator();
        while (rootIter.hasNext()) {
            Element ele = rootIter.next();
            Map<String, Object> parenMap = new HashMap<>();
            chile(ele, null, parenMap, null, null);
            // 1、list
            resultList.add(parenMap);
            // 2、map
            resultMap.put(ele.getName(), parenMap);
        }
        // toJSON
        String listJSON = JSON.toJSONString(resultList);
        String mapJSON = JSON.toJSONString(resultMap);
        //System.out.println(listJSON);
        //System.out.println(JSON.toJSONString(mapJSON));
        return resultMap;
    }

    public static void chile(Element ele, String keyName, Map<String, Object> parenMap, Map<String, Object> childMap,
                             List<Map<String, Object>> childData) {
        Iterator<Element> childIter = ele.elementIterator();
        while (childIter.hasNext()) {
            Element attr = childIter.next();
            Iterator<Element> childIter2 = attr.elementIterator();
            if (childIter2.hasNext()) {
                Element attr2 = childIter2.next();
                Map<String, Object> childMap2 = new HashMap<>();
                List<Map<String, Object>> childData2 = new ArrayList<>();

                chile(attr, attr.getName(), parenMap, childMap2, childData2);
            } else if (keyName != null) {
                childMap.put(attr.getName().trim(), attr.getText().trim());
            } else {
                //parenMap.put(attr.getName().trim(), attr.getText().trim());
                parenMap.put(attr.attribute(0).getValue().trim(), attr.getText().trim());
            }
        }
        if (keyName != null && childMap != null && !childMap.isEmpty()) {
            if (parenMap != null && parenMap.get(keyName) != null) {
                List<Map<String, Object>> object = (List<Map<String, Object>>) parenMap.get(keyName);
                object.add(childMap);
            } else {
                childData.add(childMap);
                parenMap.put(keyName, childData);
            }
        }
    }

}
