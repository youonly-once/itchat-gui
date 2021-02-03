/**
  * Copyright 2018 bejson.com 
  */
package weixin.infor.bean;

import lombok.extern.log4j.Log4j2;

/**
 * Auto-generated: 2018-10-12 12:58:59
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Log4j2
public class User_list {

    private String openid;
    private String lang;
    public void setOpenid(String openid) {
         this.openid = openid;
     }
     public String getOpenid() {
         return openid;
     }

    public void setLang(String lang) {
         this.lang = lang;
     }
     public String getLang() {
         return lang;
     }

}