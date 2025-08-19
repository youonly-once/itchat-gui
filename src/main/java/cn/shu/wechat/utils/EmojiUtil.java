package cn.shu.wechat.utils;

import cn.shu.wechat.dto.response.sync.AddMsgList;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 舒新胜 on 2017/7/1.
 */
public final class EmojiUtil {
    private EmojiUtil(){}
    String[] codeList = new String[]{
            ":smile:", ":blush:", ":confused:", ":anguished:", ":cold_sweat:", ":astonished:", ":cry:", "[Lol]",
            ":disappointed_relieved:", ":disappointed:", ":anguished:", ":confounded:", ":angry:", ":dizzy_face:", ":expressionless:", ":fearful:",
            ":flushed:", ":frowning:", ":grin:", ":heart_eyes:", ":heart_eyes_cat:", ":hushed:", ":imp:", ":innocent:",
            ":kissing_closed_eyes:", ":kissing_heart:", ":laughing:", ":neutral_face:", ":no_mouth:", ":open_mouth:", ":pensive:", ":persevere:",
            ":rage:", ":relaxed:", ":relieved:", ":scream:", ":sleeping:", ":broken_heart:", ":smirk:", ":sob:",
            ":stuck_out_tongue_closed_eyes:", ":sunglasses:", ":sweat_smile:", ":sweat:", ":triumph:", ":unamused:", ":wink:", ":yum:",
            ":cat:", ":dog:", ":bear:", ":chicken:", ":cow:", ":ghost:", ":hear_no_evil:", ":koala:",
            ":mouse:", ":airplane:", ":ambulance:", ":bike:", ":bullettrain_side:", ":bus:", ":metro:", ":oncoming_taxi:",
            ":walking:", ":apple:", ":banana:", ":beer:", ":birthday:", ":cake:", ":cherries:", ":tada:",
            ":clap:", ":fist:", ":ok_hand:", ":pray:", ":thumbsup:", ":thumbsdown:", ":muscle:", ":v:"
    };
    public static List<String> emojis = Arrays.asList("/::)",     //微笑
            "/::~",     //撇嘴
            "/::B",     //色
            "/::|",     //发呆
            "/:8-)",     //得意
            "/::<",     //流泪
            "/::$",     //害羞
            "/::X",     //闭嘴
            "/::Z",     //睡
            "/::'(",     //大哭
            "/::-|",     //尴尬
            "/::@",     //发怒
            "/::P",     //调皮
            "/::D",     //呲牙
            "/::O",     //惊讶
            "/::(",     //难过
            "/::+",     //酷
            "/:--b",     //冷汗
            "/::Q",     //抓狂
            "/::T",     //吐
            "/:,@P",     //偷笑
            "/:,@-D",     //可爱
            "/::d",     //白眼
            "/:,@o",     //傲慢
            "/::g",     //饥饿
            "/:|-)",     //困
            "/::!",     //惊恐
            "/::L",     //流汗
            "/::>",     //憨笑
            "/::,@",     //大兵
            "/:,@f",     //努力
            "/::-S",     //咒骂
            "/:?",     //疑问
            "/:,@x",     //嘘
            "/:,@@",     //晕
            "/::8",     //折磨
            "/:,@!",     //衰
            "/:!!!",     //骷髅
            "/:xx",     //敲打
            "/:bye",     //再见
            "/:wipe",     //擦汗
            "/:dig",     //抠鼻
            "/:handclap",     //鼓掌
            "/:&-(",     //溴大了
            "/:B-)",     //坏笑
            "/:<@",     //左哼哼
            "/:@>",     //右哼哼
            "/::-O",     //哈欠
            "/:>-|",     //鄙视
            "/:P-(",     //委屈
            "/::'|",     //快哭了
            "/:X-)",     //阴险
            "/::*",     //亲亲
            "/:@x",     //吓
            "/:8*",     //可怜
            "/:pd",     //菜刀
            "/:<W>",     //西瓜
            "/:beer",     //啤酒
            "/:basketb",     //篮球
            "/:oo",     //乒乓
            "/:coffee",     //咖啡
            "/:eat",     //饭
            "/:pig",     //猪头
            "/:rose",     //玫瑰
            "/:fade",     //凋谢
            "/:showlove",     //示爱
            "/:heart",     //爱心
            "/:break",     //心碎
            "/:cake",     //蛋糕
            "/:li",     //闪电
            "/:bome",     //炸弹
            "/:kn",     //刀
            "/:footb",     //足球
            "/:ladybug",     //瓢虫
            "/:shit",     //便便
            "/:moon",     //月亮
            "/:sun",     //太阳
            "/:gift",     //礼物
            "/:hug",     //拥抱
            "/:strong",     //强
            "/:weak",     //弱
            "/:share",     //握手
            "/:v",     //胜利
            "/:@)",     //抱拳
            "/:jj",     //勾引
            "/:@@",     //拳头
            "/:bad",     //差劲
            "/:lvu",     //爱你
            "/:no",     //No
            "/:ok",     //Ok
            "/:love",     //爱情
            "/:<L>",     //飞吻
            "/:jump",     //跳舞
            "/:shake",     //发抖
            "/:<O>",     //怄火
            "/:circle",     //转圈
            "/:kotow",     //磕头
            "/:turn",     //回头
            "/:skip",     //跳绳
            "/:oY",     //挥手
            "/:#-0",     //激动
            "/:hiphot",     //街舞 // hiphot doesnot work!
            "/:kiss",     //献吻
            "/:<&",     //左太极
            "/:&>"     //右太极)
    );
    public static List<String> wechatEmojiList = Arrays.asList(
            "[Aaagh!]", "[Pout]", "[Awesome]", "[Awkward]", "[Bah！R]", "[Bah！L]", "[Beckon]", "[Beer]",
            "[Blessing]", "[Blush]", "[Bomb]", "[Boring]", "[Broken]", "[BrokenHeart]", "[Bye]", "[Cake]",
            "[Chuckle]", "[Clap]", "[Cleaver]", "[Coffee]", "[Commando]", "[Concerned]", "[CoolGuy]", "[Cry]",
            "[Determined]", "[Dizzy]", "[Doge]", "[Drool]", "[Drowsy]", "[Duh]", "[Emm]", "[Facepalm]",
            "[Fireworks]", "[Fist]", "[Flushed]", "[Frown]", "[Gift]", "[GoForIt]", "[Grimace]", "[Grin]",
            "[Hammer]", "[Happy]", "[Heart]", "[Hey]", "[Hug]", "[Hurt]", "[Joyful]", "[KeepFighting]", "[Kiss]",
            "[Laugh]", "[Let Down]", "[LetMeSee]", "[Lips]", "[Lol]", "[Moon]", "[MyBad]", "[NoProb]",
            "[NosePick]", "[OK]", "[OMG]", "[Onlooker]", "[Packet]", "[Panic]", "[Party]", "[Peace]", "[Pig]",
            "[Pooh-pooh]", "[Poop]", "[Puke]", "[Respect]", "[Rose]", "[Salute]", "[Scold]", "[Scowl]",
            "[Scream]", "[Shake]", "[Shhh]", "[Shocked]", "[Shrunken]", "[Shy]", "[Sick]", "[Sigh]", "[Silent]",
            "[Skull]", "[Sleep]", "[Slight]", "[Sly]", "[Smart]", "[Smile]", "[Smirk]", "[Smug]", "[Sob]",
            "[Speechless]", "[Sun]", "[Surprise]", "[Sweat]", "[Sweats]", "[TearingUp]", "[Terror]", "[ThumbsDown]",
            "[ThumbsUp]", "[Toasted]", "[Tongue]", "[Tremble]", "[Trick]", "[Twirl]", "[Watermelon]", "[Waddle]",
            "[Whimper]", "[Wilt]", "[Worship]", "[Wow]", "[Yawn]", "[Yeah!]"
    );
    public static List<String> wechatEmojiListZh = Arrays.asList(
            "[啊啊啊!]", "[撅嘴]", "[棒极了]", "[尴尬]", "[呸！右]", "[呸！左]", "[招手]", "[啤酒]",
            "[祝福]", "[脸红]", "[炸弹]", "[无聊]", "[伤心]", "[心碎]", "[再见]", "[蛋糕]",
            "[轻笑]", "[鼓掌]", "[砍刀]", "[咖啡]", "[突击]", "[担心]", "[酷哥]", "[哭泣]",
            "[坚定]", "[眩晕]", "[狗头]", "[流口水]", "[困倦]", "[呃]", "[嗯]", "[捂脸]",
            "[烟花]", "[拳头]", "[脸红羞涩]", "[皱眉]", "[礼物]", "[加油]", "[鬼脸]", "[咧嘴笑]",
            "[锤子]", "[开心]", "[心]", "[嘿]", "[拥抱]", "[受伤]", "[喜悦]", "[继续奋斗]", "[亲吻]",
            "[大笑]", "[失望]", "[让我看看]", "[嘴唇]", "[笑出声]", "[月亮]", "[我的错]", "[没问题]",
            "[挖鼻]", "[OK]", "[天哪]", "[旁观者]", "[红包]", "[惊慌]", "[派对]", "[和平]", "[猪]",
            "[便便]", "[大便]", "[呕吐]", "[敬意]", "[玫瑰]", "[敬礼]", "[训斥]", "[生气]",
            "[尖叫]", "[摇晃]", "[嘘]", "[震惊]", "[缩小]", "[害羞]", "[生病]", "[叹气]", "[沉默]",
            "[骷髅]", "[睡觉]", "[轻微]", "[狡猾]", "[聪明]", "[微笑]", "[奸笑]", "[得意]", "[抽泣]",
            "[无语]", "[太阳]", "[惊讶]", "[流汗]", "[冷汗]", "[眼泛泪光]", "[恐惧]", "[不赞]",
            "[点赞]", "[烤面包]", "[吐舌]", "[颤抖]", "[捣蛋]", "[旋转]", "[西瓜]", "[摇摆]",
            "[呜咽]", "[枯萎]", "[崇拜]", "[哇]", "[打哈欠]", "[耶!]"
    );

    public static List<String> getWechatEmojiList() {
        return wechatEmojiList;
    }

    public static void setWechatEmojiList(List<String> wechatEmojiList) {
        EmojiUtil.wechatEmojiList = wechatEmojiList;
    }

    /**
     * 获取微信表情
     * @param context context
     * @param code 表情代码
     * @return Icon
     */
    public static ImageIcon getWeChatEmoji(Object context,String code) {

        return getWeChatEmoji(context,code,22,22);
    }
    /**
     * 获取微信表情
     * @param context context
     * @param code 表情代码
     * @return Icon
     */
    public static ImageIcon getWeChatEmoji(Object context,String code,int width,int height) {
        int i = wechatEmojiList.indexOf(code);
        if (i == -1){
            int i1 = wechatEmojiListZh.indexOf(code);
            if (i1 == -1){
                return null;
            }
        }
        String weChatIconPath = "/emoji/wechat_emoji/";

        return IconUtil.getIcon(context, weChatIconPath + (2*i + 4) + ".png",width,height);
    }

    /**
     * 是否为微信表情
     * @param code 表情代码
     * @return Boolean
     */
    public static boolean isWeChatEmoji(String code) {
       return wechatEmojiList.contains(code)||wechatEmojiListZh.contains(code);
    }
    /**
     * 获取Emoji表情
     *
     * @param code emoji代码，形式如 {@code :dog:}
     * @return Icon
     */
    public static ImageIcon getEmoji(Object context, String code) {
        ImageIcon weChatEmoji = getWeChatEmoji(context, code);
        if (weChatEmoji != null){
            return weChatEmoji;
        }
        String iconPath = "/emoji/" + code.subSequence(1, code.length() - 1) + ".png";
        URL url = context.getClass().getResource(iconPath);
        return url == null ? null : new ImageIcon(url);
    }

    /**
     * 判断给定的emoji代码是否可识别
     * @param context 上下文
     * @param code emoji代码
     * @return true 可识别
     */
    public static boolean isRecognizableEmoji(Object context, String code) {
        return getEmoji(context, code) != null;
    }


    public static void emojiFormatter(AddMsgList msg) {

        msg.setContent(StringEscapeUtils.unescapeXml(msg.getContent()));
        msg.setContent(emojiFormatter(msg.getContent()));

    }

    /**
     * 消息格式化
     *
     * @author SXS
     * @date 2017年4月23日 下午4:19:08
     */
    public static String emojiFormatter(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return msg;
        }
        msg = msg.replace("<br/>", "\n");
        Matcher matcher = getMatcher("<span class=\"emoji emoji(.{1,10})\"></span>", msg);
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        while (matcher.find()) {
            String str = matcher.group(1);
            if (str.length() == 6) {

            } else if (str.length() == 10) {

            } else {
                String tmp = msg.substring(lastStart, matcher.start());
                sb.append(tmp).append("&#x").append(str).append(";");
                lastStart = matcher.end();
            }
        }
        if (lastStart < msg.length()) {
            sb.append(msg.substring(lastStart));
        }
        if (sb.length() != 0) {
            msg = EmojiParser.parseToUnicode(sb.toString());
        } else {
            msg = EmojiParser.parseToUnicode(msg);
        }
        return msg;
        // TODO 与emoji表情有部分兼容问题，目前暂未处理解码处理 d.put(k,
        // StringEscapeUtils.unescapeHtml4(d.getString(k)));

    }

    /**
     * 正则表达式处理工具
     *
     * @return
     * @author SXS
     * @date 2017年4月9日 上午12:27:10
     */
    public static Matcher getMatcher(String regEx, String text) {
        Pattern pattern = Pattern.compile(regEx);
        return pattern.matcher(text);
    }

}
