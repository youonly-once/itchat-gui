package cn.shu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/3/2021 9:19 AM
 */
public class Text {
    public static void main(String[] args) {
        String str = "#接龙<br/>Ex. 啥事<br/><br/>1. 哈哈<br/>2. 哈哈 呵呵";
        String regex = "#接龙<br/>.*<br/><br/>.*(\\d+)\\.(.+)$";
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(str);
        if (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                System.out.println(matcher.group(i));
            }
        }
    }
}
