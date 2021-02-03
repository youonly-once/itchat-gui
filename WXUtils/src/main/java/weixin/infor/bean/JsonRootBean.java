/**
  * Copyright 2018 bejson.com 
  */
package weixin.infor.bean;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Auto-generated: 2018-10-12 12:58:59
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Log4j2
public class JsonRootBean {

    private List<User_list> user_list;
    public void setUser_list(List<User_list> user_list) {
         this.user_list = user_list;
     }
     public List<User_list> getUser_list() {
         return user_list;
     }

}