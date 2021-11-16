package cn.shu.wechat.swing.adapter;

import javax.swing.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class ContactsHeaderViewHolder extends HeaderViewHolder {
    private String letter;
    public JLabel letterLabel;

    public ContactsHeaderViewHolder(String ch) {
        this.letter = ch;

    }


    public String getLetter() {
        return letter;
    }
}
