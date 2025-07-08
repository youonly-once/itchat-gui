package cn.shu.wechat.entity;

import java.io.Serializable;

public class LoginInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uin;
    private String uuid;
    private String skey;
    private String wxsid;
    private String wxuin;
    private String passTicket;

    public LoginInfo() {
    }

    // Getter 和 Setter

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }

    public String getWxsid() {
        return wxsid;
    }

    public void setWxsid(String wxsid) {
        this.wxsid = wxsid;
    }

    public String getWxuin() {
        return wxuin;
    }

    public void setWxuin(String wxuin) {
        this.wxuin = wxuin;
    }

    public String getPassTicket() {
        return passTicket;
    }

    public void setPassTicket(String passTicket) {
        this.passTicket = passTicket;
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "uin='" + uin + '\'' +
                ", uuid='" + uuid + '\'' +
                ", skey='" + skey + '\'' +
                ", wxsid='" + wxsid + '\'' +
                ", wxuin='" + wxuin + '\'' +
                ", passTicket='" + passTicket + '\'' +
                '}';
    }
}
