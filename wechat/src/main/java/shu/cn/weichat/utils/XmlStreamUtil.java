package shu.cn.weichat.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.thoughtworks.xstream.security.AnyTypePermission;
import lombok.extern.log4j.Log4j2;

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
}
