package cn.shu.wechat.swing.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
import java.awt.*;

// 自定义的 TextFieldUI 类，用于实现提示文本效果
public class HintTextFieldUI extends BasicTextFieldUI {
        private String hint;

        public HintTextFieldUI(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintSafely(Graphics g) {
            super.paintSafely(g);
            JTextComponent component = getComponent();
            if (component.getText().isEmpty()) {
                Font originalFont = component.getFont();
                Font italicFont = originalFont.deriveFont(Font.ITALIC);
                g.setFont(italicFont);
                g.setColor(UIManager.getColor("textInactiveText"));
                int padding = (component.getHeight() - component.getFontMetrics(italicFont).getHeight()) / 2;
                g.drawString(hint, 2, component.getHeight() - padding - 1);
            }
        }
    }