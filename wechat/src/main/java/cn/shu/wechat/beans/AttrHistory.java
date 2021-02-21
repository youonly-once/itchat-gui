package cn.shu.wechat.beans;

import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/19/2021 4:02 PM
 */
@Data
@Builder
public class AttrHistory {
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 属性
     */
    private String attr;

    /**
     * 备注名
     */
    private String remarkname;

    private Date createtime;

    private String newval;

    private String oldval;
}