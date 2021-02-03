package shu.cn.weichat.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 1/31/2021 7:12 PM
 */
public class XmlUtil {
    /**
     * @description 将xml字符串转换成map
     * @param xml
     * @return Map
     */
    public static Map<String,String> toMap(String xml) {
        Map<String,String>  map = new HashMap<String,String> ();
        Document doc = null;
        try {
            // 将字符串转为XML
            doc = DocumentHelper.parseText(xml);
            // 获取根节点
            Element rootElt = doc.getRootElement();
            // 拿到根节点的名称
            //获取根节点下的子节点body
            List<Element> elements = rootElt.elements();
            getXmlMap(map,elements);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
    private static void getXmlMap(Map<String, String> map, List<Element> list) {
        for (Element e : list) {
            if (e.elements().isEmpty()) {
                map.put(e.getName(), e.getText());// 遍历list对象，并将结果保存到集合中
            } else {
                getXmlMap(map, e.elements());
            }
        }

    }
}
