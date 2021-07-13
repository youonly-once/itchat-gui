package cn.shu.wechat.utils;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.beans.pojo.MessageExample;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.enums.WXSendMsgCodeEnum;
import cn.shu.wechat.mapper.ContactsMapper;
import cn.shu.wechat.mapper.MessageMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Test;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jfree.data.general.DatasetUtils.createCategoryDataset;


/**
 * <p><br/>
 *
 * @version v1.0.0
 * @className CreateChartServiceImpl.java<br />
 * @packageName com.sinosoft.webmodule.landLibrary<br />
 * @date 2014-3-23 下午04:39:19<br/>
 * </p>
 */
@Component
@Slf4j
public class ChartUtil {
    private static final String CHART_PATH = "F:/1/";

    @Resource
    private AttrHistoryMapper attrHistoryMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ContactsMapper contactsMapper;


    public static void main(String[] args) {
        // 创建主题样式
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        // 设置标题字体
        standardChartTheme.setExtraLargeFont(new Font("宋书", Font.BOLD, 28));
        // 设置图例的字体
        standardChartTheme.setRegularFont(new Font("宋书", Font.PLAIN, 14));
        // 设置轴向的字体
        standardChartTheme.setLargeFont(new Font("宋书", Font.PLAIN, 14));
        // 应用主题样式
        ChartFactory.setChartTheme(standardChartTheme);


        ChartUtil chart = new ChartUtil();
       /* // 生成单组柱状图
        chart.makeBarChart();
        // 生成单组柱状图
        chart.makeBarChart2();
        // 生成多组柱状图
        chart.makeBarGroupChart();
        // 生成堆积柱状图
        chart.makeStackedBarChart();
        // 生成折线图
        chart.makeLineAndShapeChart();

        // 生成饼状图
        chart.makeWXContactUpdateAttrBarChart();
        chart.makeWXContactInfoPieChart();

        */
        // chart.makeWXContactMessageTop();
        System.out.println((int) Math.ceil(3 / 50.0));
    }

    public void create() {
        // 创建主题样式
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        // 设置标题字体
        standardChartTheme.setExtraLargeFont(new Font("宋书", Font.BOLD, 28));
        // 设置图例的字体
        standardChartTheme.setRegularFont(new Font("宋书", Font.PLAIN, 14));
        // 设置轴向的字体
        standardChartTheme.setLargeFont(new Font("宋书", Font.PLAIN, 14));
        // 应用主题样式
        ChartFactory.setChartTheme(standardChartTheme);

        // 生成饼状图
        makeWXContactUpdateAttrBarChart();
        makeWXContactInfoPieChart();
        makeWXContactMessageTop();

    }

    /**
     * 获取群成员性别分布
     *
     * @param groupName
     * @param attrName
     * @return
     */
    public String makeGroupMemberAttrPieChart(String groupName, String groupRemarkName, String attrName, int width, int height) {
        log.info("makeGroupMemberAttrPieChart：" + attrName);
        List<Contacts> memberlist = Core.getMemberMap().get(groupName).getMemberlist();
        if (memberlist == null || memberlist.isEmpty()) {
            return null;
        }
        String[] sexKeys = {"男", "女", "无"};
        Class<Contacts> contactsClass = Contacts.class;
        Field[] declaredFields = contactsClass.getDeclaredFields();
        HashMap<String, HashMap<String, String>> stringHashMapHashMap = new HashMap<>();
        //属性值转汉字
        HashMap<String, String> stringStringHashMap = new HashMap<String, String>();
        stringStringHashMap.put("0", "未设置");
        stringStringHashMap.put("1", "男");
        stringStringHashMap.put("2", "女");
        stringHashMapHashMap.put("Sex", stringStringHashMap);
        stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("", "未设置");
        stringHashMapHashMap.put("Province", stringStringHashMap);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            String key = field.getName();
            if (!key.equals(attrName)) {
                continue;
            }
            String title = key;
            switch (key) {
                case "Sex":
                    title = "【" + groupRemarkName + "】群成员性别比例";
                    break;
                case "Province":
                    title = "【" + groupRemarkName + "】群成员省市分布";
                    break;
            }

            Map<String, AtomicInteger> testMap = new HashMap<>(10);
            for (Contacts o : memberlist) {
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
                String value = null;
                if (stringHashMapHashMap.get(key) != null) {
                    value = stringHashMapHashMap.get(key).get(jsonObject.getString(key));
                }
                if (value == null) {
                    value = jsonObject.getString(key);
                }

                testMap.computeIfAbsent(value, v -> new AtomicInteger()).getAndIncrement();
            }
            String imgPath = createValidityComparePimChar(getDataPieSetByUtil(sortMapByValue(testMap)), title,
                    UUID.randomUUID().toString().replace("-", "") + "_" + key + ".png", sexKeys, width, height);
            return imgPath;


        }
        return null;
    }

    /**
     * 生成折线图
     */
    @Test
    public void makeLineAndShapeChart() {
        double[][] data = new double[][]{{672, 766, 223, 540, 126},
                {325, 521, 210, 340, 106}, {332, 256, 523, 240, 526}};
        String[] rowKeys = {"苹果", "梨子", "葡萄"};
        String[] columnKeys = {"北京", "上海", "广州", "成都", "深圳"};
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
        createTimeXYChar("折线图", "x轴", "y轴", dataset, "lineAndShap.jpg");
    }

    /**
     * 生成分组的柱状图
     */
    public void makeBarGroupChart() {
        double[][] data = new double[][]{{672, 766, 223, 540, 126},
                {325, 521, 210, 340, 106}, {332, 256, 523, 240, 526}};
        String[] rowKeys = {"苹果", "梨子", "葡萄"};
        String[] columnKeys = {"北京", "上海", "广州", "成都", "深圳"};
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
/*
        createBarChart(dataset, "x坐标", "y坐标", "柱状图", "barGroup.png");
*/
    }

    /**
     * 生成柱状图
     */
    @Test
    public void makeBarChart() {
        double[][] data = new double[][]{{672, 766, 223, 540, 126}};
        String[] rowKeys = {"苹果"};
        String[] columnKeys = {"北京", "上海", "广州", "成都", "深圳"};
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
/*
        createBarChart(dataset, "x坐标", "y坐标", "柱状图", "bar.png");
*/
    }

    /**
     * 生成柱状图
     */
    @Test
    public void makeBarChart2() {
        double[][] data = new double[][]{{672, 766, 223, 540, 126}};
        String[] rowKeys = {"苹果"};
        String[] columnKeys = {"北京", "上海", "广州", "成都", "深圳"};
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
        createHorizontalBarChart(dataset, "x坐标", "y坐标", "柱状图", "bar2.png");
    }

    /**
     * 生成堆栈柱状图
     */
    @Test
    public void makeStackedBarChart() {
        double[][] data = new double[][]{{0.21, 0.66, 0.23, 0.40, 0.26},
                {0.25, 0.21, 0.10, 0.40, 0.16}};
        String[] rowKeys = {"苹果", "梨子"};
        String[] columnKeys = {"北京", "上海", "广州", "成都", "深圳"};
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
        createStackedBarChart(dataset, "x坐标", "y坐标", "柱状图", "stsckedBar.png");
    }

    /**
     * 生成微信好友属性饼状图
     */
    public void makeWXContactInfoPieChart() {
        System.out.println("create pie-chart.");
        Map<String, Contacts> contactMap = Core.getContactMap();
        String[] sexKeys = {"男", "女", "无"};
        Set<String> keys = new HashSet<>();
        for (Contacts value : contactMap.values()) {
            Class<Contacts> contactsClass = Contacts.class;
            for (Field declaredField : contactsClass.getDeclaredFields()) {
                declaredField.setAccessible(true);
                keys.add(declaredField.getName());
            }
            break;
        }
        if (keys.isEmpty()) {
            return;
        }
        HashMap<String, HashMap<String, String>> stringHashMapHashMap = new HashMap<>();
        HashMap<String, String> stringStringHashMap = new HashMap<String, String>();
        stringStringHashMap.put("0", "未设置");
        stringStringHashMap.put("1", "男");
        stringStringHashMap.put("2", "女");
        stringHashMapHashMap.put("Sex", stringStringHashMap);
        stringStringHashMap.put("", "未设置");
        stringHashMapHashMap.put("Province", stringStringHashMap);
        for (String key : keys) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String title = key;
                    switch (key) {
                        case "Sex":
                            title = "微信好友性别分布";
                            break;
                        case "Province":
                            title = "微信好友省份分布";
                            break;
                    }

                    Map<String, AtomicInteger> testMap = new HashMap<>(10);
                    for (Contacts value : contactMap.values()) {
                        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(value));
                        String s = null;
                        if (stringHashMapHashMap.get(key) != null) {
                            s = stringHashMapHashMap.get(key).get(jsonObject.getString(key));
                        }
                        if (s == null) {
                            s = jsonObject.getString(key);
                        }

                        testMap.computeIfAbsent(s, v -> new AtomicInteger()).getAndIncrement();
                    }
                    createValidityComparePimChar(getDataPieSetByUtil(sortMapByValue(testMap)), title,
                            key + ".png", sexKeys, 1920, 1080);
                }
            }).start();

        }


    }

    /**
     * 聊天双方用户活跃度(消息发送次数)
     *
     * @param userName
     * @return
     */
    public String makeWXUserActivity(String userName) {

        MessageExample messageExample = new MessageExample();
        MessageExample.Criteria criteria = messageExample.or();
        MessageExample.Criteria criteria1 = messageExample.or();
        MessageExample.Criteria criteria2 = messageExample.or();
        //我发给对方的
        criteria.andFromUsernameEqualTo(Core.getUserName());
        criteria1.andFromNicknameEqualTo(Core.getNickName());
        criteria2.andFromRemarknameEqualTo(Core.getNickName());
        messageExample.or().andToNicknameEqualTo(ContactsTools.getContactNickNameByUserName(userName));
        messageExample.or().andToUsernameEqualTo(userName);
        messageExample.or().andToRemarknameEqualTo(ContactsTools.getContactRemarkNameByUserName(userName));

        List<Message> messages = messageMapper.selectByExample(messageExample);

        Map<String, AtomicInteger> msgCount = new HashMap<>();
        for (Message message : messages) {
            String fromMemberOfGroupDisplayname = message.getFromMemberOfGroupDisplayname();
            if (StringUtils.isEmpty(fromMemberOfGroupDisplayname)) {
                fromMemberOfGroupDisplayname = message.getFromNickname();
            }
            if (message.getMsgType() >= 1 && message.getMsgType() <= 48) {
                msgCount.computeIfAbsent(fromMemberOfGroupDisplayname, v -> new AtomicInteger()).getAndIncrement();
            }
        }


        msgCount = sortMapByValue(msgCount);
        int maxSize = 10;
        int size = Math.min(maxSize, msgCount.size());
        double[][] data = new double[1][size];
        String[] columnKeys = new String[size];
        String[] rowKeys = {"发送数量"};
        double[] values = new double[size];

        int i = 0;
        for (Map.Entry<String, AtomicInteger> type : msgCount.entrySet()) {
            if (i == size) {
                break;
            }
            columnKeys[i] = type.getKey();
            values[i] = type.getValue().get();
            i++;
        }
        data[0] = values;
        if (values.length > 0) {
            CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
            String barImg1 = createBarChart(dataset, "用户昵称", "发送消息数量", "群成员活跃度", "makeWXGroupMessageTop1.png", 1024, 768);
            return barImg1;
        }
        return null;
    }

    /**
     * 群用户活跃度(消息发送次数)
     *
     * @param userName
     * @return
     */
    public String makeWXMemberOfGroupActivity(String userName) {

        MessageExample messageExample = new MessageExample();
        MessageExample.Criteria criteria = messageExample.or();
        MessageExample.Criteria criteria1 = messageExample.or();
        MessageExample.Criteria criteria2 = messageExample.or();

        criteria.andFromUsernameEqualTo(userName);
        criteria1.andFromNicknameEqualTo(ContactsTools.getContactNickNameByUserName(userName));
        criteria2.andFromRemarknameEqualTo(ContactsTools.getContactRemarkNameByUserName(userName));
        messageExample.or().andToNicknameEqualTo(ContactsTools.getContactNickNameByUserName(userName));
        messageExample.or().andToUsernameEqualTo(userName);
        messageExample.or().andToRemarknameEqualTo(ContactsTools.getContactRemarkNameByUserName(userName));
        List<Message> messages = messageMapper.selectByExample(messageExample);

        Map<String, AtomicInteger> msgCount = new HashMap<>();
        for (Message message : messages) {
            String fromMemberOfGroupDisplayname = message.getFromMemberOfGroupDisplayname();
            if (StringUtils.isEmpty(fromMemberOfGroupDisplayname)) {
                fromMemberOfGroupDisplayname = message.getFromNickname();
            }
            if (message.getMsgType() >= 1 && message.getMsgType() <= 48) {
                msgCount.computeIfAbsent(fromMemberOfGroupDisplayname, v -> new AtomicInteger()).getAndIncrement();
            }
        }


        msgCount = sortMapByValue(msgCount);
        int maxSize = 10;
        int size = Math.min(maxSize, msgCount.size());
        double[][] data = new double[1][size];
        String[] columnKeys = new String[size];
        String[] rowKeys = {"发送数量"};
        double[] values = new double[size];

        int i = 0;
        for (Map.Entry<String, AtomicInteger> type : msgCount.entrySet()) {
            if (i == size) {
                break;
            }
            columnKeys[i] = type.getKey();
            values[i] = type.getValue().get();
            i++;
        }
        data[0] = values;
        if (values.length > 0) {
            CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
            String barImg1 = createBarChart(dataset, "用户昵称", "发送消息数量", "群成员活跃度", "makeWXGroupMessageTop1.png", 1024, 768);
            return barImg1;
        }
        return null;
    }

    /**
     * 聊天信息统计
     *
     * @param userName
     * @return
     */
    public List<String> makeWXUserMessageTop(String userName) {

        MessageExample messageExample = new MessageExample();
        MessageExample.Criteria criteria = messageExample.or();
        MessageExample.Criteria criteria1 = messageExample.or();
        MessageExample.Criteria criteria2 = messageExample.or();
        String contactRemarkNameByUserName = ContactsTools.getContactRemarkNameByUserName(userName);
        criteria.andFromUsernameEqualTo(userName);
        criteria1.andFromNicknameEqualTo(ContactsTools.getContactNickNameByUserName(userName));
        if (StringUtils.isNotEmpty(contactRemarkNameByUserName)) {
            criteria2.andFromRemarknameEqualTo(contactRemarkNameByUserName);
        }
        messageExample.or().andToNicknameEqualTo(ContactsTools.getContactNickNameByUserName(userName));
        messageExample.or().andToUsernameEqualTo(userName);
        if (StringUtils.isNotEmpty(contactRemarkNameByUserName)) {
            messageExample.or().andToRemarknameEqualTo(contactRemarkNameByUserName);
        }

        List<Message> messages = messageMapper.selectByExample(messageExample);

        Map<String, AtomicInteger> msgType = new HashMap<>();
        Map<String, AtomicInteger> msgTerm = new HashMap<>();
        for (Message message : messages) {
            String msg = message.getContent();
            String type = message.getMsgDesc();
            msgType.computeIfAbsent(type, v -> new AtomicInteger()).getAndIncrement();
            if (message.getMsgType() == WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode()
                    && StringUtils.isNotBlank(msg)) {
                Result result = ToAnalysis.parse(msg);
                for (Term term : result.getTerms()) {
                    //System.out.println(term);
                    String natureStr = term.getNatureStr().substring(0, 1);
                    if (StringUtils.isBlank(term.getName())) {
                        continue;
                    }
                    switch (natureStr) {
                        //case "e":
                        case "n":
                        //case "v":
                        case "t":

                        //case "a":
                      //  case "b":
                       // case "d":
                            // case "r":
                            msgTerm.computeIfAbsent(term.toString(), v -> new AtomicInteger()).getAndIncrement();
                            break;
                        default:

                    }

                }
            }
        }


        ArrayList<String> imgs = new ArrayList<>();
        msgType = sortMapByValue(msgType);
        msgTerm = sortMapByValue(msgTerm);

        int maxSize = 10;
        int size = Math.min(maxSize, msgType.size());
        double[][] data = new double[1][size];
        String[] columnKeys = new String[size];
        String[] rowKeys = {"发送数量"};
        double[] values = new double[size];

        int i = 0;
        for (Map.Entry<String, AtomicInteger> type : msgType.entrySet()) {
            if (i == size) {
                break;
            }
            columnKeys[i] = type.getKey();
            values[i] = type.getValue().get();
            i++;
        }
        data[0] = values;
        if (values.length > 0) {
            CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
            String barImg1 = createBarChart(dataset, "消息类型", "发送数量", "消息类型排行", "makeWXGroupMessageTop1.png", 500, 400);
            imgs.add(barImg1);
        }

        size = Math.min(maxSize, msgTerm.size());
        double[][] data1 = new double[1][size];
        String[] columnKeys1 = new String[size];
        String[] rowKeys1 = {"更新次数"};
        double[] values1 = new double[size];

        int i1 = 0;
        for (Map.Entry<String, AtomicInteger> term : msgTerm.entrySet()) {
            if (i1 == size) {
                break;
            }
            columnKeys1[i1] = term.getKey();
            values1[i1] = term.getValue().get();
           /* if (term.getValue().get() >= 1500) {
                continue;
            }*/
            i1++;
        }
        data1[0] = values1;
        if (size > 0) {
            CategoryDataset dataset1 = getBarData(data1, rowKeys1, columnKeys1);
            String barImg2 = createBarChart(dataset1, "词语", "发送数量", "消息常用关键词排行", "makeWXGroupMessageTop2.png", 500, 400);
            imgs.add(barImg2);
        }

        return imgs;
    }

    /**
     * 获取消息top20
     */
    public void makeWXContactMessageTop() {
        Path path = Paths.get("E:\\JAVA\\project_idea\\weixin");
        File[] files = path.toFile().listFiles();
        if (files == null) {
            return;
        }
        Properties properties = new Properties();
        Map<String, AtomicInteger> msgType = new TreeMap<>();
        Map<String, AtomicInteger> msgTerm = new HashMap<>();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".property")) {
                try {
                    properties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                    for (Object value : properties.values()) {

                        String type = value.toString().substring(0, value.toString().indexOf(":"));
                        String msg = value.toString().substring(value.toString().indexOf("-") + 1);
                        msgType.computeIfAbsent(type, v -> new AtomicInteger()).getAndIncrement();
                        if (type.equals("TEXT") && StringUtils.isNotBlank(msg)
                                && !value.toString().contains("@@")) {
                            Result result = ToAnalysis.parse(msg);
                            for (Term term : result.getTerms()) {
                                //System.out.println(term);
                                String natureStr = term.getNatureStr().substring(0, 1);
                                if (StringUtils.isBlank(term.getName())) {
                                    continue;
                                }
                                switch (natureStr) {
                                    //case "e":
                                    case "n":
                                        //case "v":
                                        //case "t":

                                        // case "a":
                                        // case "b":
                                        //case "d":
                                        //case "r":
                                        msgTerm.computeIfAbsent(term.toString(), v -> new AtomicInteger()).getAndIncrement();
                                        break;
                                    default:

                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        msgType = sortMapByValue(msgType);
        msgTerm = sortMapByValue(msgTerm);

        int maxSize = 20;
        double[][] data = new double[1][msgType.size()];
        String[] columnKeys = new String[msgType.size()];
        String[] rowKeys = {"发送数量"};
        double[] values = new double[msgType.size()];

        int i = 0;
        for (Map.Entry<String, AtomicInteger> type : msgType.entrySet()) {
            /*if (i==maxSize){
                break;
            }*/
            columnKeys[i] = type.getKey();
            values[i] = type.getValue().get();
            i++;
        }
        data[0] = values;
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
        createBarChart(dataset, "消息类型", "发送数量", "消息类型排行", "makeWXContactMessageTop2.png", 500, 400);


        double[][] data1 = new double[1][maxSize];
        String[] columnKeys1 = new String[maxSize];
        String[] rowKeys1 = {"更新次数"};
        double[] values1 = new double[maxSize];

        int i1 = 0;
        for (Map.Entry<String, AtomicInteger> term : msgTerm.entrySet()) {
            if (i1 == maxSize) {
                break;
            }
            columnKeys1[i1] = term.getKey();
            values1[i1] = term.getValue().get();
            if (term.getValue().get() >= 1500) {
                continue;
            }
            i1++;
        }
        data1[0] = values1;
        CategoryDataset dataset1 = getBarData(data1, rowKeys1, columnKeys1);
        createBarChart(dataset1, "词语", "发送数量", "消息常用词语", "makeWXContactMessageTop2.png", 500, 400);


    }

    /**
     * 生成微信好友个人信息更新次数柱状图
     */
    public void makeWXContactUpdateAttrBarChart() {

        List<Map<String, Object>> stringAtomicIntegerMap = attrHistoryMapper.selectUpdateInfoCount(10);
        double[][] data = new double[1][stringAtomicIntegerMap.size()];
        String[] columnKeys = new String[stringAtomicIntegerMap.size()];
        String[] rowKeys = {"更新次数"};
        double[] values = new double[stringAtomicIntegerMap.size()];
        for (int i = 0; i < stringAtomicIntegerMap.size(); i++) {
            Map<String, Object> stringObjectMap = stringAtomicIntegerMap.get(i);
            columnKeys[i] = stringObjectMap.get("name").toString();
            values[i] = Double.parseDouble(stringObjectMap.get("cou").toString());
        }
        data[0] = values;
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
        createBarChart(dataset, "好友昵称", "更新数量", "微信好友个人信息更新次数/月", "makeWXContactUpdateAttrBarChart.png", 1024, 768);


        List<Map<String, Object>> stringAtomicIntegerMapAttr = attrHistoryMapper.selectUpdateAttrCount(10);
        for (int i = 0; i < stringAtomicIntegerMapAttr.size(); i++) {
            Map<String, Object> stringObjectMap = stringAtomicIntegerMapAttr.get(i);
            columnKeys[i] = stringObjectMap.get("name").toString();
            values[i] = Double.parseDouble(stringObjectMap.get("cou").toString());
        }
        data[0] = values;
        dataset = getBarData(data, rowKeys, columnKeys);
        createBarChart(dataset, "好友昵称", "更新数量", "微信好友个人信息更新次数TYPE/月", "makeWXContactUpdateAttrBarChartTYPE.png"
                , 1024, 768);


    }

    // 柱状图,折线图 数据集
    public CategoryDataset getBarData(double[][] data, String[] rowKeys,
                                      String[] columnKeys) {

        return DatasetUtils.createCategoryDataset(rowKeys, columnKeys, data);

    }

    // 饼状图 数据集
    public PieDataset getDataPieSetByUtil(Map<String, AtomicInteger> datas) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, AtomicInteger> stringAtomicIntegerEntry : datas.entrySet()) {
            Object value = stringAtomicIntegerEntry.getValue();
            dataset.setValue(stringAtomicIntegerEntry.getKey().toString(), value == null ? 0 : Integer.parseInt(value.toString()));
        }
        return dataset;
    }


    // 饼状图 数据集
    public PieDataset getDataPieSetByUtil(double[] data,
                                          String[] datadescription) {
        if (data != null && datadescription != null) {
            if (data.length == datadescription.length) {
                DefaultPieDataset dataset = new DefaultPieDataset();
                for (int i = 0; i < data.length; i++) {
                    dataset.setValue(datadescription[i], data[i]);
                }
                return dataset;
            }

        }

        return null;
    }

    /**
     * 柱状图
     *
     * @param dataset    数据集
     * @param xName      x轴的说明（如种类，时间等）
     * @param yName      y轴的说明（如速度，时间等）
     * @param chartTitle 图标题
     * @param charName   生成图片的名字
     * @return
     */
    public String createBarChart(CategoryDataset dataset, String xName,
                                 String yName, String chartTitle, String charName, int width, int height) {
        JFreeChart chart = ChartFactory.createBarChart(chartTitle, // 图表标题
                xName, // 目录轴的显示标签
                yName, // 数值轴的显示标签
                dataset, // 数据集
                PlotOrientation.VERTICAL, // 图表方向：水平、垂直
                true, // 是否显示图例(对于简单的柱状图必须是false)
                false, // 是否生成工具
                false // 是否生成URL链接
        );

        /*
         * VALUE_TEXT_ANTIALIAS_OFF表示将文字的抗锯齿关闭,
         * 使用的关闭抗锯齿后，字体尽量选择12到14号的宋体字,这样文字最清晰好看
         */
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        chart.setTextAntiAlias(false);
        chart.getTitle().setFont(new Font("宋体", Font.PLAIN, 14));
        chart.setBackgroundPaint(Color.white);
        // create plot
        CategoryPlot plot = chart.getCategoryPlot();
        // 设置横虚线可见
        plot.setRangeGridlinesVisible(true);
        // 虚线色彩
        plot.setRangeGridlinePaint(Color.gray);

        // 数据轴精度
        NumberAxis vn = (NumberAxis) plot.getRangeAxis();
        // vn.setAutoRangeIncludesZero(true);
        DecimalFormat df = new DecimalFormat("#0.00");
        vn.setNumberFormatOverride(df); // 数据轴数据标签的显示格式
        // x轴设置
        CategoryAxis domainAxis = plot.getDomainAxis();
        Font domainAxisFont = new Font("SansSerif", Font.BOLD, 14);
        domainAxis.setLabelFont(domainAxisFont);// 轴标题
        domainAxis.setTickLabelFont(domainAxisFont);// 轴数值

        // Lable（Math.PI/3.0）度倾斜
        // domainAxis.setCategoryLabelPositions(CategoryLabelPositions
        // .createUpRotationLabelPositions(Math.PI / 3.0));
        // 横轴上的 Lable 是否完整显示
        domainAxis.setMaximumCategoryLabelWidthRatio(1f);

        // 设置距离图片左端距离
        domainAxis.setLowerMargin(0.1);
        // 设置距离图片右端距离
        domainAxis.setUpperMargin(0.1);
        // 设置 columnKey 是否间隔显示
        // domainAxis.setSkipCategoryLabelsToFit(true);

        plot.setDomainAxis(domainAxis);
        // 设置柱图背景色（注意，系统取色的时候要使用16位的模式来查看颜色编码，这样比较准确）
        plot.setBackgroundPaint(new Color(39, 43, 88));
        //设置图例

        LegendItemCollection legends = new LegendItemCollection();
        Paint[] colorValues = {new Color(0, 0, 128),
                new Color(100, 149, 190),
                new Color(72, 61, 139),
                new Color(200, 90, 205),
                new Color(123, 104, 238),
                new Color(132, 36, 255),
                new Color(0, 0, 205),
                new Color(65, 105, 225),
                new Color(30, 144, 255),
                new Color(200, 191, 255),
                new Color(0, 0, 128),
                new Color(100, 149, 190),
                new Color(72, 61, 139),
                new Color(200, 90, 205),
                new Color(123, 104, 238),
                new Color(132, 36, 255),
                new Color(0, 0, 205),
                new Color(65, 105, 225),
                new Color(30, 144, 255),
                new Color(200, 191, 255)};
        for (int i = 0; i < 20 && i < dataset.getColumnCount(); ++i) {
            plot.getRenderer().setSeriesFillPaint(i, colorValues[i]);
            LegendItem legendItem = new LegendItem(dataset.getColumnKey(i).toString(), "-", null, null, Plot.DEFAULT_LEGEND_ITEM_BOX, colorValues[i]);
            legends.add(legendItem);
        }
        plot.setFixedLegendItems(legends);
        // y轴设置
        Font rangeAxisFont = new Font("SansSerif", Font.BOLD, 14);
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(rangeAxisFont);
        rangeAxis.setTickLabelFont(rangeAxisFont);
        // 设置最高的一个 Item 与图片顶端的距离
        rangeAxis.setUpperMargin(0.15);
        // 设置最低的一个 Item 与图片底端的距离
        rangeAxis.setLowerMargin(0.15);
        plot.setRangeAxis(rangeAxis);
        //设置图例位置
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(new Font("宋体", Font.PLAIN, 14));
        class CustomRender extends BarRenderer {
            private Paint[] colors;
            // 初始化柱子颜色

            public CustomRender() {
                colors = colorValues;
            }

            // 每根柱子以初始化的颜色不断轮循
            public Paint getItemPaint(int i, int j) {
                return colors[j % colors.length];
            }
        }


        BarRenderer renderer = new CustomRender();
        // 设置柱子宽度
        renderer.setMaximumBarWidth(0.05);
        // 设置柱子高度
        renderer.setMinimumBarLength(0.2);
        // 设置柱子边框颜色

        // 设置柱子边框可见
        // renderer.setDrawBarOutline(true);

        // 设置每个地区所包含的平行柱的之间距离
        renderer.setItemMargin(0.0);
        renderer.setShadowPaint(new Color(0, 0, 0));
        // 显示每个柱的数值，并修改该数值的字体属性
        renderer.setIncludeBaseInRange(true);
        //柱子上方数字样式
        renderer.setSeriesItemLabelPaint(0, new Color(255, 255, 255));
        renderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.BOLD, 14));
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        // 设置柱子为平面图不是立体的
        renderer.setBarPainter(new StandardBarPainter());
        // 设置柱状图之间的距离0.1代表10%；
        renderer.setItemMargin(0.2);
        plot.setRenderer(renderer);
        // 设置柱的透明度
        plot.setForegroundAlpha(1.0f);

        FileOutputStream fos_jpg = null;
        try {
            isChartPathExist(CHART_PATH);
            String chartName = CHART_PATH + charName;
            fos_jpg = new FileOutputStream(chartName);
            ChartUtils.writeChartAsPNG(fos_jpg, chart, width, height, true, 10);
            return chartName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos_jpg != null) {
                    fos_jpg.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 横向图
     *
     * @param dataset    数据集
     * @param xName      x轴的说明（如种类，时间等）
     * @param yName      y轴的说明（如速度，时间等）
     * @param chartTitle 图标题
     * @param charName   生成图片的名字
     * @return
     */
    public String createHorizontalBarChart(CategoryDataset dataset,
                                           String xName, String yName, String chartTitle, String charName) {
        JFreeChart chart = ChartFactory.createBarChart(chartTitle, // 图表标题
                xName, // 目录轴的显示标签
                yName, // 数值轴的显示标签
                dataset, // 数据集
                PlotOrientation.VERTICAL, // 图表方向：水平、垂直
                true, // 是否显示图例(对于简单的柱状图必须是false)
                false, // 是否生成工具
                false // 是否生成URL链接
        );

        CategoryPlot plot = chart.getCategoryPlot();
        // 数据轴精度
        NumberAxis vn = (NumberAxis) plot.getRangeAxis();
        // 设置刻度必须从0开始
        // vn.setAutoRangeIncludesZero(true);
        DecimalFormat df = new DecimalFormat("#0.00");
        vn.setNumberFormatOverride(df); // 数据轴数据标签的显示格式

        CategoryAxis domainAxis = plot.getDomainAxis();

        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // 横轴上的
        // Lable
        Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);

        domainAxis.setLabelFont(labelFont);// 轴标题
        domainAxis.setTickLabelFont(labelFont);// 轴数值

        domainAxis.setMaximumCategoryLabelWidthRatio(0.8f);// 横轴上的 Lable 是否完整显示
        // domainAxis.setVerticalCategoryLabels(false);
        plot.setDomainAxis(domainAxis);

        ValueAxis rangeAxis = plot.getRangeAxis();
        // 设置最高的一个 Item 与图片顶端的距离
        rangeAxis.setUpperMargin(0.15);
        // 设置最低的一个 Item 与图片底端的距离
        rangeAxis.setLowerMargin(0.15);
        plot.setRangeAxis(rangeAxis);
        BarRenderer renderer = new BarRenderer();
        // 设置柱子宽度
        renderer.setMaximumBarWidth(0.03);
        // 设置柱子高度
        renderer.setMinimumBarLength(30);

        //renderer.setBaseOutlinePaint(Color.BLACK);

        // 设置柱的颜色
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesPaint(1, new Color(0, 0, 255));
        // 设置每个地区所包含的平行柱的之间距离
        renderer.setItemMargin(0.5);
        // 显示每个柱的数值，并修改该数值的字体属性
        //renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        // 设置柱的数值可见
        //renderer.setBaseItemLabelsVisible(true);

        plot.setRenderer(renderer);
        // 设置柱的透明度
        plot.setForegroundAlpha(0.6f);

        FileOutputStream fos_jpg = null;
        try {
            isChartPathExist(CHART_PATH);
            String chartName = CHART_PATH + charName;
            fos_jpg = new FileOutputStream(chartName);
            ChartUtils.writeChartAsPNG(fos_jpg, chart, 500, 500, true, 10);
            return chartName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fos_jpg.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 饼状图
     *
     * @param dataset    数据集
     * @param chartTitle 图标题
     * @param charName   生成图的名字
     * @param pieKeys    分饼的名字集
     * @return
     */
    public String createValidityComparePimChar(PieDataset dataset,
                                               String chartTitle, String charName, String[] pieKeys, int width, int height) {
        JFreeChart chart = ChartFactory.createPieChart3D(chartTitle, // chart
                // title
                dataset,// data
                true,// include legend
                true, false);

        // 使下说明标签字体清晰,去锯齿类似于
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        chart.setTextAntiAlias(false);
        // 图片背景色
        chart.setBackgroundPaint(Color.white);
        // 设置图标题的字体重新设置title
        Font font = new Font("宋体", Font.BOLD, 36);
        TextTitle title = new TextTitle(chartTitle);
        title.setFont(font);
        chart.setTitle(title);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        // 图片中显示百分比:默认方式

        // 指定饼图轮廓线的颜色
        // plot.setBaseSectionOutlinePaint(Color.BLACK);
        // plot.setBaseSectionPaint(Color.BLACK);

        // 设置无数据时的信息
        plot.setNoDataMessage("无对应的数据，请重新查询。");

        // 设置无数据时的信息显示颜色
        plot.setNoDataMessagePaint(Color.red);

        // 图片中显示百分比:自定义方式，{0} 表示选项， {1} 表示数值， {2} 表示所占比例 ,小数点后两位
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}-{1}人\n ({2})", NumberFormat.getNumberInstance(),
                new DecimalFormat("0.00%")));
        // 图例显示百分比:自定义方式， {0} 表示选项， {1} 表示数值， {2} 表示所占比例
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}-{1}人  ({2})"));

        plot.setLabelFont(new Font("宋体", Font.BOLD, 12));

        // 指定图片的透明度(0.0-1.0)
        plot.setForegroundAlpha(1);
        // 指定显示的饼图上圆形(false)还椭圆形(true)
        plot.setCircular(false, true);
        LegendTitle legend = chart.getLegend();
        legend.setItemFont(new Font("宋体", Font.PLAIN, 12));
        // 设置第一个 饼块section 的开始位置，默认是12点钟方向
        plot.setStartAngle(90);
        plot.setBackgroundPaint(new Color(39, 43, 88));
        // // 设置分饼颜色
        plot.setSectionPaint(pieKeys[0], new Color(65, 105, 225));
        plot.setSectionPaint(pieKeys[1], new Color(30, 144, 255));
        plot.setLabelLinkMargin(0.2);
        FileOutputStream fos_jpg = null;
        try {
            // 文件夹不存在则创建
            isChartPathExist(CHART_PATH);
            String chartName = CHART_PATH + charName;

            fos_jpg = new FileOutputStream(chartName);
            // 高宽的设置影响椭圆饼图的形状
            ChartUtils.writeChartAsPNG(fos_jpg, chart, width, height);
            //发送消息
            fos_jpg.close();//先关流才能使用文件发送
            ArrayList<MessageTools.Result> results = new ArrayList<>();
            MessageTools.Result filehelper = MessageTools.Result.builder()
                    .content(chartName)
                    .toUserName("filehelper")
                    .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                    .build();
            results.add(filehelper);
            //MessageTools.sendMsgByUserId(results, "filehelper");
            return chartName;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (fos_jpg != null) {
                    fos_jpg.close();
                }

            } catch (Exception e1) {
                e.printStackTrace();
            }
            return null;
        } finally {

        }

    }

    /**
     * 判断文件夹是否存在，如果不存在则新建
     *
     * @param chartPath
     */
    private void isChartPathExist(String chartPath) {
        File file = new File(chartPath);
        if (!file.exists()) {
            file.mkdirs();
            // log.info("CHART_PATH="+CHART_PATH+"create.");
        }
    }

    /**
     * 折线图
     *
     * @param chartTitle
     * @param x
     * @param y
     * @param xyDataset
     * @param charName
     * @return
     */
    public String createTimeXYChar(String chartTitle, String x, String y,
                                   CategoryDataset xyDataset, String charName) {
        JFreeChart chart = ChartFactory.createLineChart(chartTitle, x, y,
                xyDataset, PlotOrientation.VERTICAL, true, true, false);

        chart.setTextAntiAlias(false);
        chart.setBackgroundPaint(Color.WHITE);
        // 设置图标题的字体重新设置title
        Font font = new Font("隶书", Font.BOLD, 25);
        TextTitle title = new TextTitle(chartTitle);
        title.setFont(font);
        chart.setTitle(title);
        // 设置面板字体
        Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);

        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
        // x轴 // 分类轴网格是否可见
        categoryplot.setDomainGridlinesVisible(true);
        // y轴 //数据轴网格是否可见
        categoryplot.setRangeGridlinesVisible(true);

        categoryplot.setRangeGridlinePaint(Color.WHITE);// 虚线色彩

        categoryplot.setDomainGridlinePaint(Color.WHITE);// 虚线色彩

        categoryplot.setBackgroundPaint(Color.lightGray);

        // 设置轴和面板之间的距离
        // categoryplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));

        CategoryAxis domainAxis = categoryplot.getDomainAxis();

        domainAxis.setLabelFont(labelFont);// 轴标题
        domainAxis.setTickLabelFont(labelFont);// 轴数值

        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // 横轴上的
        // Lable
        // 45度倾斜
        // 设置距离图片左端距离
        domainAxis.setLowerMargin(0.0);
        // 设置距离图片右端距离
        domainAxis.setUpperMargin(0.0);

        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setAutoRangeIncludesZero(true);

        // 获得renderer 注意这里是下嗍造型到lineandshaperenderer！！
        LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
                .getRenderer();

        // lineandshaperenderer.setBaseShapesVisible(true); // series 点（即数据点）可见
        // lineandshaperenderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见

        // 显示折点数据
        // lineandshaperenderer.setBaseItemLabelGenerator(new
        // StandardCategoryItemLabelGenerator());
        // lineandshaperenderer.setBaseItemLabelsVisible(true);

        FileOutputStream fos_jpg = null;
        try {
            isChartPathExist(CHART_PATH);
            String chartName = CHART_PATH + charName;
            fos_jpg = new FileOutputStream(chartName);

            // 将报表保存为png文件
            ChartUtils.writeChartAsPNG(fos_jpg, chart, 500, 510);

            return chartName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos_jpg != null) {
                    fos_jpg.close();
                }
                System.out.println("create time-createTimeXYChar.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 堆栈柱状图
     *
     * @param dataset
     * @param xName
     * @param yName
     * @param chartTitle
     * @param charName
     * @return
     */
    public String createStackedBarChart(CategoryDataset dataset, String xName,
                                        String yName, String chartTitle, String charName) {
        // 1:得到 CategoryDataset

        // 2:JFreeChart对象
        JFreeChart chart = ChartFactory.createStackedBarChart(chartTitle, // 图表标题
                xName, // 目录轴的显示标签
                yName, // 数值轴的显示标签
                dataset, // 数据集
                PlotOrientation.VERTICAL, // 图表方向：水平、垂直
                true, // 是否显示图例(对于简单的柱状图必须是false)
                false, // 是否生成工具
                false // 是否生成URL链接
        );
        // 图例字体清晰
        chart.setTextAntiAlias(false);

        chart.setBackgroundPaint(Color.WHITE);

        // 2 ．2 主标题对象 主标题对象是 TextTitle 类型
        chart
                .setTitle(new TextTitle(chartTitle, new Font("隶书", Font.BOLD,
                        25)));
        // 2 ．2.1:设置中文
        // x,y轴坐标字体
        Font labelFont = new Font("SansSerif", Font.TRUETYPE_FONT, 12);

        // 2 ．3 Plot 对象 Plot 对象是图形的绘制结构对象
        CategoryPlot plot = chart.getCategoryPlot();

        // 设置横虚线可见
        plot.setRangeGridlinesVisible(true);
        // 虚线色彩
        plot.setRangeGridlinePaint(Color.gray);

        // 数据轴精度
        NumberAxis vn = (NumberAxis) plot.getRangeAxis();
        // 设置最大值是1
        vn.setUpperBound(1);
        // 设置数据轴坐标从0开始
        // vn.setAutoRangeIncludesZero(true);
        // 数据显示格式是百分比
        DecimalFormat df = new DecimalFormat("0.00%");
        vn.setNumberFormatOverride(df); // 数据轴数据标签的显示格式
        // DomainAxis （区域轴，相当于 x 轴）， RangeAxis （范围轴，相当于 y 轴）
        CategoryAxis domainAxis = plot.getDomainAxis();

        domainAxis.setLabelFont(labelFont);// 轴标题
        domainAxis.setTickLabelFont(labelFont);// 轴数值

        // x轴坐标太长，建议设置倾斜，如下两种方式选其一，两种效果相同
        // 倾斜（1）横轴上的 Lable 45度倾斜
        // domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        // 倾斜（2）Lable（Math.PI 3.0）度倾斜
        // domainAxis.setCategoryLabelPositions(CategoryLabelPositions
        // .createUpRotationLabelPositions(Math.PI / 3.0));

        domainAxis.setMaximumCategoryLabelWidthRatio(0.6f);// 横轴上的 Lable 是否完整显示

        plot.setDomainAxis(domainAxis);

        // y轴设置
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(labelFont);
        rangeAxis.setTickLabelFont(labelFont);
        // 设置最高的一个 Item 与图片顶端的距离
        rangeAxis.setUpperMargin(0.15);
        // 设置最低的一个 Item 与图片底端的距离
        rangeAxis.setLowerMargin(0.15);
        plot.setRangeAxis(rangeAxis);

        // Renderer 对象是图形的绘制单元
        StackedBarRenderer renderer = new StackedBarRenderer();
        // 设置柱子宽度
        renderer.setMaximumBarWidth(0.05);
        // 设置柱子高度
        renderer.setMinimumBarLength(0.1);
        // 设置柱的边框颜色
        //renderer.setBaseOutlinePaint(Color.BLACK);
        // 设置柱的边框可见
        renderer.setDrawBarOutline(true);

        // // 设置柱的颜色(可设定也可默认)
        renderer.setSeriesPaint(0, new Color(204, 255, 204));
        renderer.setSeriesPaint(1, new Color(255, 204, 153));

        // 设置每个地区所包含的平行柱的之间距离
        renderer.setItemMargin(0.4);

        plot.setRenderer(renderer);
        // 设置柱的透明度(如果是3D的必须设置才能达到立体效果，如果是2D的设置则使颜色变淡)
        // plot.setForegroundAlpha(0.65f);

        FileOutputStream fos_jpg = null;
        try {
            isChartPathExist(CHART_PATH);
            String chartName = CHART_PATH + charName;
            fos_jpg = new FileOutputStream(chartName);
            ChartUtils.writeChartAsPNG(fos_jpg, chart, 500, 500, true, 10);
            return chartName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fos_jpg.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*  *
     * 使用 Map按value进行排序
     *
     * @param
     * @return
     */
    public Map<String, AtomicInteger> sortMapByValue(Map<String, AtomicInteger> oriMap) {
        if (oriMap == null) {
            return null;
        }
        if (oriMap.isEmpty()) {
            return oriMap;
        }
        Map<String, AtomicInteger> sortedMap = new LinkedHashMap<String, AtomicInteger>();
        List<Map.Entry<String, AtomicInteger>> entryList = new ArrayList<Map.Entry<String, AtomicInteger>>(
                oriMap.entrySet());
        entryList.sort(new MapValueComparator());

        Iterator<Map.Entry<String, AtomicInteger>> iter = entryList.iterator();
        Map.Entry<String, AtomicInteger> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public void arrSortByDesc(Double[] args) {
        // 注意，要想改变默认的排列顺序，不能使用基本类型（int,double, char）
        // 而要使用它们对应的类
        // 定义一个自定义类MyComparator的对象
        Comparator cmp = new MyComparator();
        Arrays.sort(args, cmp);
    }

    static class MapValueComparator implements Comparator<Map.Entry<String, AtomicInteger>> {

        @Override
        public int compare(Map.Entry<String, AtomicInteger> me1, Map.Entry<String, AtomicInteger> me2) {
            if (me1.getValue().get() == me2.getValue().get()) {
                return 0;
            }
            return me1.getValue().get() > me2.getValue().get() ? -1 : 1;
        }
    }


    // Comparator是一个接口
//Comparator是一个比较器
//Comparator中的compare可以将传入进行比对，按照返回的参数大于(1)等于(0)小于(-1)进行排序
//默认情况下返回1的在后，返回-1的在前
//如果我们需要逆序，只要把返回值-1和1的换位置即可。
    class MyComparator implements Comparator<Double> {
        @Override
        public int compare(Double o1, Double o2) {
            // 如果o1小于o2，我们就返回正值，如果n1大于n2我们就返回负值，
            if (o1 < o2) {
                return 1;
            } else if (o1 > o2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}



