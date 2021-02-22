package weixin.templatemsg.bean;

import lombok.extern.log4j.Log4j2;

import java.util.Map;

/**
 * Auto-generated: 2019-03-05 12:28:29
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Log4j2
public class WechatTemplate {

    private Map<String, TemplateData> data;
    private String template_id;
    private String touser;
    private String url;
    public void setData(Map<String, TemplateData> data) {
         this.data = data;
     }
     public Map<String, TemplateData> getData() {
         return data;
     }

    public void setTemplate_id(String template_id) {
         this.template_id = template_id;
     }
     public String getTemplate_id() {
         return template_id;
     }

    public void setTouser(String touser) {
         this.touser = touser;
     }
     public String getTouser() {
         return touser;
     }

    public void setUrl(String url) {
         this.url = url;
     }
     public String getUrl() {
         return url;
     }

}