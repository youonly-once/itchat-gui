package cn.shu.wechat.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.thoughtworks.xstream.security.AnyTypePermission;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 舒新胜
 * @version 创建时间：2020年3月24日 下午5:38:41
 * 类说明 微信发送消息的XML转为对象
 */
@Log4j2
public class XmlStreamUtil {
	public static <T> T xmlToBean(String xml, Class<T> clazz) {
		XStream xStream = getInstance();
		xStream.processAnnotations(clazz);
		Object object = xStream.fromXML(xml);
		T cast = clazz.cast(object);
		return cast;
	}

	private static XStream getInstance() {
		XStream xStream = new XStream(new DomDriver("UTF-8")) {
			/**
			 * 忽略xml中多余字段
			 */
			@Override
			protected MapperWrapper wrapMapper(MapperWrapper next) {
				return new MapperWrapper(next) {
					@SuppressWarnings("rawtypes")
					@Override
					public boolean shouldSerializeMember(Class definedIn, String fieldName) {
						if (definedIn == Object.class) {
							return false;
						}
						return super.shouldSerializeMember(definedIn, fieldName);
					}
				};
			}
		};

		// 设置默认的安全校验
		// XStream.setupDefaultSecurity(xStream);
		// 使用本地的类加载器
		xStream.setClassLoader(XmlStreamUtil.class.getClassLoader());
		// 允许所有的类进行转换
		xStream.addPermission(AnyTypePermission.ANY);
		return xStream;
	}

	/**
	 * 注意：节点属性会转为MAP，并在KEY后加上“_V”
	 * @description 将xml字符串转换成map
	 * @param xml
	 * @return Map
	 */
	public static Map<String,Object> toMap(String xml) {
		Map<String,Object>  map = new HashMap<String,Object>();
		Document doc = null;
		try {
			// 将字符串转为XML
			doc = DocumentHelper.parseText(xml);
			// 获取根节点
			Element rootElt = doc.getRootElement();
			//根节点属性
			List<Attribute> attributes = rootElt.attributes();
			HashMap<String, String> attrMap = new HashMap<>();
			for (Attribute attribute : attributes) {
				attrMap.put(attribute.getName(),attribute.getValue());
			}
			map.put(rootElt.getName()+"_V",attrMap);
			// 拿到根节点的名称
			//获取根节点下的子节点body
			List<Element> elements = rootElt.elements();
			getXmlMap(map,elements);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	private static void getXmlMap(Map<String, Object> map, List<Element> list) {
		for (Element e : list) {
			if (e.elements().isEmpty()) {
				map.put(e.getName(), e.getText());// 遍历list对象，并将结果保存到集合中
				//节点属性
				List<Attribute> attributes = e.attributes();
				HashMap<String, String> attrMap = new HashMap<>();
				for (Attribute attribute : attributes) {
					attrMap.put(attribute.getName(),attribute.getValue());
				}
				map.put(e.getName()+"_V",attrMap);
			} else {
				getXmlMap(map, e.elements());
			}
		}

	}
}
