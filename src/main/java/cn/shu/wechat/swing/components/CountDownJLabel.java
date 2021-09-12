package cn.shu.wechat.swing.components;

import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * 倒计时label
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 8/11/2021 12:33
 */
@NoArgsConstructor
public class CountDownJLabel extends JLabel {
    private  String text ;
    private int count ;
    private final Timer timer = new Timer(1000, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            --count;
            if (count <0){
                stop();
            }else{
                CountDownJLabel.super.setText(String.valueOf(count));
            }
        }
    });
    public CountDownJLabel(String text) {
        super(text);
        if (StringUtils.isEmpty(text)){
            throw new NullPointerException("text can not be null.");
        }
        this.text = text;
        count = Integer.parseInt( this.text);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.text = text;
        if (text != null && !text.isEmpty()){
            count = Integer.parseInt( this.text);
        }
    }

    /**
     * 开始倒计时
     */
    public void start(){
        timer.start();
    }

    /**
     * 停止倒计时
     */
    public void stop(){
        setText(text);
        count = Integer.parseInt( this.text);
        timer.stop();
    }

    public static void main(String[] args) throws IOException {
        JFrame jFrame = new JFrame();
        Container contentPane = jFrame.getContentPane();
        contentPane.setLayout(new FlowLayout());

        CountDownJLabel countDownJLabel = new CountDownJLabel("100");
        contentPane.add(countDownJLabel);
        JButton jButton = new JButton("start");
        JButton jButton1 = new JButton("stop");
        contentPane.add(jButton);
        contentPane.add(jButton1);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countDownJLabel.start();
            }
        });
        jButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countDownJLabel.stop();
            }
        });
        JLabel jLabel = new JLabel(new ImageIcon("D:/1.gif"));
        contentPane.add(jLabel);
        jFrame.setVisible(true);
        jFrame.setSize(400,300);
    }
}
