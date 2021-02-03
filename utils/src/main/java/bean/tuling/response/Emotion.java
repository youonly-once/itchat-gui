/**
  * Copyright 2021 bejson.com 
  */
package bean.tuling.response;

/**
 * Auto-generated: 2021-02-02 15:2:56
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Emotion {

    private RobotEmotion robotEmotion;
    private UserEmotion userEmotion;
    public void setRobotEmotion(RobotEmotion robotEmotion) {
         this.robotEmotion = robotEmotion;
     }
     public RobotEmotion getRobotEmotion() {
         return robotEmotion;
     }

    public void setUserEmotion(UserEmotion userEmotion) {
         this.userEmotion = userEmotion;
     }
     public UserEmotion getUserEmotion() {
         return userEmotion;
     }

}