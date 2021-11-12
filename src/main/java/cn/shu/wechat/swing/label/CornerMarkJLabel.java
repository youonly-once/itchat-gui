package cn.shu.wechat.swing.label;

import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @Description
 * @Author SXS
 * @Date 2021/11/12 11:20
 */
public class CornerMarkJLabel extends JLabel {
    public void setCornerText(String cornerText) {
        this.cornerText = cornerText;
    }

    public String getCornerText() {
        return cornerText;
    }

    private String cornerText;
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (StringUtils.isEmpty(cornerText)){
            return;
        }
        ImageIcon chatIcon = IconUtil.getIcon(this, "/image/chat_active.png");
        int x = (this.getWidth() - chatIcon.getIconWidth()) / 2;
        int y = (this.getHeight() - chatIcon.getIconHeight()) / 2;
        ImageIcon icon = null;
        g.setColor(Color.WHITE);
        g.setFont(FontUtil.getDefaultFont(12));
        if (cornerText.length()>2){
            cornerText = "99";
        }
        if (cornerText.length()==2){
            icon  = IconUtil.getIcon(this, "/image/count_bg.png");
            g.drawImage(icon.getImage()
                    ,x+13,y-5,null);
            g.drawString(cornerText,x+17,y+8);

        }else {
            icon  = IconUtil.getIcon(this, "/image/red_point16.png");
            g.drawImage(icon.getImage()
                    ,x+13,y-5,null);
            g.drawString(cornerText,x+17,y+8);
        }




    }
}
