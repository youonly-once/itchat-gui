package cn.shu.wechat.utils;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.service.LoginService;
import lombok.extern.slf4j.Slf4j;
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
import org.jfree.chart.util.SortOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


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
public final class ChartUtil {
    private ChartUtil(){

    }
    @Resource
    private WechatConfiguration wechatConfiguration;

    @Resource
    private AttrHistoryMapper attrHistoryMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private LoginService loginService;


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
        makeWXContactMessageTop();

    }
    /**
     * 创建群成员属性饼图，或者创建所有好友的属性饼图
     *
     * @param userName 用户昵称
     * @param attrName 属性名称
     * @param width    图片宽度
     * @param height   图片高度
     * @return JFreeChart
     */
    private Optional<JFreeChart> makeContactsAttrPieChart(String userName, String attrName, int width, int height){
        Optional<JFreeChart> jFreeChartOptional = Optional.empty();
        if (ContactsTools.isRoomContact(userName)) {
            String remarkNameByGroupUserName = ContactsTools.getContactDisplayNameByUserName(userName);
            jFreeChartOptional = makeGroupMemberAttrPieChart(userName, remarkNameByGroupUserName, attrName, width, height);
        } else if (userName.equals(Core.getUserName())) {
            jFreeChartOptional = makeMineContactsAttrPieChart(attrName, width, height);

        }
        return jFreeChartOptional;
    }
    /**
     * 创建群成员属性饼图，或者创建所有好友的属性饼图
     *
     * @param userName 用户昵称
     * @param attrName 属性名称
     * @param width    图片宽度
     * @param height   图片高度
     * @return 图片路径
     */
    public Optional<String> makeContactsAttrPieChartAsPng(String userName, String attrName, int width, int height) {
        String chartName = UUID.randomUUID().toString().replace("-", "") + "_" + attrName + ".png";
        Optional<JFreeChart> jFreeChartOptional = makeContactsAttrPieChart(userName, attrName, width, height);
        return jFreeChartOptional.map(a->saveChartAsPng(chartName,a , width, height));
    }

    /**
     * 创建群成员属性饼图，或者创建所有好友的属性饼图
     *
     * @param userName 用户昵称
     * @param attrName 属性名称
     * @param width    图片宽度
     * @param height   图片高度
     * @return BufferedImage
     */
    public Optional<BufferedImage> makeContactsAttrPieChartAsBufferedImage(String userName, String attrName, int width, int height) {
        Optional<JFreeChart> jFreeChartOptional = makeContactsAttrPieChart(userName, attrName, width, height);
        return jFreeChartOptional.map(jFreeChart -> jFreeChart.createBufferedImage(width, height));
    }

    /**
     * 群成员属性分布图
     *
     * @param groupName       群id
     * @param groupRemarkName 群备注
     * @param attrName        属性名
     * @param width           图片宽度
     * @param height          图片高度
     * @return 图表对象
     */
    public Optional<JFreeChart> makeGroupMemberAttrPieChart(String groupName, String groupRemarkName, String attrName, int width, int height) {
        log.info("makeGroupMemberAttrPieChart：" + attrName);

        List<Contacts> memberList = Optional.ofNullable(Core.getMemberMap().get(groupName))
                .map(Contacts::getMemberlist)
                .orElseGet(() -> loginService.WebWxBatchGetContact(groupName));
        if (!Optional.ofNullable(memberList).isPresent()) {
            return Optional.empty();
        }
        String title = "【" + groupRemarkName + "】群成员" + attrName + "比例";
        Optional<DefaultPieDataset<String>> dataPieSet = getPieChatDatasetByWXContactsAttr(memberList, attrName);
        return dataPieSet.flatMap(stringDefaultPieDataset -> createValidityComparePimChar(stringDefaultPieDataset, title));

    }

    /**
     * 我的好友属性饼图
     *
     * @param attrName 属性名
     * @param width    图片宽度
     * @param height   图片高度
     * @return 图表对象
     */
    public Optional<JFreeChart> makeMineContactsAttrPieChart(String attrName, int width, int height) {
        String title = attrName + "分布图";
        Optional<DefaultPieDataset<String>> dataPieSet = getPieChatDatasetByWXContactsAttr(Core.getContactMap().values(), attrName);
        return dataPieSet.flatMap(stringDefaultPieDataset -> createValidityComparePimChar(stringDefaultPieDataset, title));
    }

    /**
     * 根据属性创建饼图数据集
     *
     * @param sourceList 数据源
     * @param attr       属性名
     * @return 饼图数据集
     */
    public Optional<DefaultPieDataset<String>> getPieChatDatasetByWXContactsAttr(Collection<Contacts> sourceList, String attr) {
        try {
            Field declaredField = Contacts.class.getDeclaredField(attr);
            declaredField.setAccessible(true);
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            Map<String, Long> map = sourceList.stream()
                    .collect(Collectors.groupingBy(contacts -> {
                        Object value = null;
                        try {
                            value = declaredField.get(contacts);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        if (value == null || "".equals(value.toString())) {
                            value = "未设置" + attr;
                        } else if (attr.equals("sex")) {
                            if (Byte.parseByte(value.toString()) == 2) {
                                value = "女";
                            } else if (Byte.parseByte(value.toString()) == 1) {
                                value = "男";
                            } else {
                                value = "未设置";
                            }
                        }
                        return value.toString();
                    }, Collectors.counting()));
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
            dataset.sortByValues(SortOrder.DESCENDING);
            return Optional.of(dataset);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    /**
     * 聊天双方用户活跃度(消息发送次数)
     *
     * @param userName 用户名
     * @return 文件路径
     */
    public String makeWXUserActivityFile(String userName) {
        Optional<BufferedImage> bufferedImage = makeWXUserActivityBufferedImage(userName);
        return writeBufferedImageAsPNG(bufferedImage,"makeWXUserActivity"+System.nanoTime()+".png");
    }

    /**
     * writeBufferedImageAsPNG
     * @param bufferedImage bufferedImage
     * @return 文件路径
     */
    private String writeBufferedImageAsPNG(Optional<BufferedImage> bufferedImage,String fileName){
        if (!bufferedImage.isPresent()){
            return null;
        }
        FileOutputStream fos_jpg = null;
        try {
            isChartPathExist(wechatConfiguration.getBasePath());
            String chartName = wechatConfiguration.getBasePath() + fileName;
            fos_jpg = new FileOutputStream(chartName);
            ChartUtils.writeBufferedImageAsPNG(fos_jpg, bufferedImage.get());

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
     * 聊天双方用户活跃度(消息发送次数)
     *
     * @param userName 用户名
     * @return 文件路径
     */
    public Optional<BufferedImage>  makeWXUserActivityBufferedImage(String userName) {


        List<Map<String, Object>> maps = messageMapper.selectUserMessageCount(
                userName
                , ContactsTools.getContactNickNameByUserName(userName)
                , ContactsTools.getContactRemarkNameByUserName(userName));

        //生成数据
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map<String, Object> map : maps) {
            dataset.setValue(Double.parseDouble(map.get("count").toString()),
                    "发送数量",
                    map.get("nickName").toString());
        }
        Optional<JFreeChart> barChart = createBarChart(dataset, "用户昵称", "发送消息数量", "双方发送消息数");
        return barChart.map(jFreeChart -> jFreeChart.createBufferedImage(500, 400));
    }
    /**
     * 群用户活跃度(消息发送次数)
     *
     * @param userName 用户名称
     * @return 图片路径
     */
    public String makeWXMemberOfGroupActivityFile(String userName) {

        //查询数据
        Optional<BufferedImage> bufferedImage = makeWXMemberOfGroupActivityBufferedImage(userName);
        return writeBufferedImageAsPNG(bufferedImage,"makeWXGroupMessageTop"+System.nanoTime()+".png");


    }
    /**
     * 群用户活跃度(消息发送次数)
     *
     * @param userName 用户名称
     * @return BufferedImage
     */
    public Optional<BufferedImage> makeWXMemberOfGroupActivityBufferedImage(String userName) {

        //查询数据
        List<Map<String, Object>> maps = messageMapper.selectGroupUserMessageCount(userName
                , ContactsTools.getContactNickNameByUserName(userName)
                , ContactsTools.getContactRemarkNameByUserName(userName)
                ,10);
        //生成数据
        DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
        for (Map<String, Object> map : maps) {

            String name = "";
            if (map.get("displayName")!=null){
                name = map.get("displayName").toString();
            }else  if (map.get("nickName")!=null){
                name = map.get("nickName").toString();
            }
            defaultCategoryDataset.setValue(Double.parseDouble(map.get("count").toString()),
                    "发送数量",
                    name.length()>20?name.substring(0,19)+"...": name);
        }
        Optional<JFreeChart> barChart = createBarChart(defaultCategoryDataset, "用户昵称", "发送消息数量", "【"+ContactsTools.getContactNickNameByUserName(userName)+"】群成员活跃度");
        return barChart.map(jFreeChart -> jFreeChart.createBufferedImage(900, 540));
    }

    /**
     * 聊天双方聊天消息类型排行排行
     * 消息类型排行
     *
     * @param userName 用户名
     * @return BufferedImage
     */
    public Optional<BufferedImage> makeWXGroupMessageTypeTopBufferedImage(String userName) {

        String nickName = ContactsTools.getContactNickNameByUserName(userName);
        String remarkName = ContactsTools.getContactRemarkNameByUserName(userName);

        //类型汇总
        List<Map<String, Object>> mapsType = messageMapper.groupByType(userName, nickName, remarkName);
        DefaultCategoryDataset categoryDatasetOfType= new DefaultCategoryDataset();

        for (Map<String, Object> map : mapsType) {
            categoryDatasetOfType.setValue(Double.parseDouble(map.get("count").toString())
                    ,"发送次数"
                    , WxRespConstant.WXReceiveMsgCodeEnum.getByCode(Integer.parseInt(map.get("type").toString())).getDesc());
        }
        Optional<JFreeChart> barChart = createBarChart(categoryDatasetOfType, "消息类型", "发送数量", "【" + nickName + "】群消息类型排行");
        return barChart.map(chart -> chart.createBufferedImage(500, 400));

    }
    /**
     * 聊天双方聊天词语排行
     *
     *消息内容排行
     * @param userName 用户名
     * @return BufferedImage
     */
    public Optional<BufferedImage> makeWXGroupMessageTopBufferedImage(String userName) {

        String nickName = ContactsTools.getContactNickNameByUserName(userName);
        String remarkName = ContactsTools.getContactRemarkNameByUserName(userName);
        //内容汇总
        List<Map<String, Object>> mapsContent = messageMapper.groupByContent(userName, nickName, remarkName);
        DefaultCategoryDataset categoryDatasetOfContent = new DefaultCategoryDataset();

        for (Map<String, Object> map : mapsContent) {
            categoryDatasetOfContent.setValue(Double.parseDouble(map.get("count").toString())
                    ,"发送次数"
                    ,map.get("content").toString().length()>10?map.get("content").toString().substring(0,10)+"...":map.get("content").toString());
        }
        Optional<JFreeChart> barChart = createBarChart(categoryDatasetOfContent, "消息内容", "发送数量", "【" + nickName + "】群消息内容排行");
        return barChart.map(chart -> chart.createBufferedImage(900, 700));

    }


    /**
     * 聊天双方聊天消息类型排行排行
     * 消息类型排行
     *
     * @param userName 用户名
     * @return 文件路径
     */
    public String makeWXGroupMessageTypeTopFile(String userName) {

        Optional<BufferedImage> bufferedImage = makeWXGroupMessageTypeTopBufferedImage(userName);
        return writeBufferedImageAsPNG(bufferedImage,"makeWXGroupMessageTop2"+System.nanoTime()+".png");

    }
    /**
     * 聊天双方聊天词语排行
     *
     *
     * @param userName 用户名
     * @return 文件路径
     */
    public String makeWXGroupMessageTopFile(String userName) {
        Optional<BufferedImage> bufferedImage = makeWXGroupMessageTopBufferedImage(userName);
        return writeBufferedImageAsPNG(bufferedImage,"makeWXGroupMessageTop1"+System.nanoTime()+".png");
    }

    /**
     * 获取消息top20
     */
    public void makeWXContactMessageTop() {

    }

    /**
     * 生成微信好友个人信息更新次数柱状图
     * @return 图片列表
     */
    public List<String> makeWXContactUpdateAttrBarChart() {
        ArrayList<String> imgList = new ArrayList<>();
        List<Map<String, Object>> updateInfoMap = attrHistoryMapper.selectUpdateInfoCount(10);
        DefaultCategoryDataset categoryDatasetInfo = new DefaultCategoryDataset();
        for (Map<String, Object> map : updateInfoMap) {
            categoryDatasetInfo.setValue(Double.parseDouble(
                    map.get("count").toString()),
                    "更新次数",
                    map.get("name").toString().length()>10?map.get("name").toString().substring(0,10):map.get("name").toString());
        }
        Optional<JFreeChart> barChart1 = createBarChart(categoryDatasetInfo, "好友昵称", "更新数量", "微信好友个人信息更新次数/月");
        Optional<BufferedImage> bufferedImage1 = barChart1.map(chart -> chart.createBufferedImage(1024, 768));
        String barChartFile1 = writeBufferedImageAsPNG(bufferedImage1, "makeWXContactUpdateAttrBarChart.png");
        imgList.add(barChartFile1);

        List<Map<String, Object>> attrMap = attrHistoryMapper.selectUpdateAttrCount(10);
        DefaultCategoryDataset categoryDatasetAttr = new DefaultCategoryDataset();
        for (Map<String, Object> map : attrMap) {
            categoryDatasetAttr.setValue(Double.parseDouble(
                            map.get("count").toString()),
                    "更新次数",
                    map.get("name").toString());
        }

        Optional<JFreeChart> barChart2= createBarChart(categoryDatasetAttr, "好友昵称", "更新数量", "微信好友个人信息更新次数TYPE/月");
        Optional<BufferedImage> bufferedImage2 = barChart2.map(chart -> chart.createBufferedImage(1024, 768));
        String barChartFile2 = writeBufferedImageAsPNG(bufferedImage2, "makeWXContactUpdateAttrBarChartTYPE.png");
        imgList.add(barChartFile2);
        return imgList;

    }

    /**
     * 生成 柱状图,折线图 数据集
     * @param data 数据
     * @param rowKeys 行
     * @param columnKeys 列
     * @return 数据集
     */
    public CategoryDataset getBarData(double[][] data, String[] rowKeys,
                                      String[] columnKeys) {
        return DatasetUtils.createCategoryDataset(rowKeys, columnKeys, data);

    }

    // 饼状图 数据集
    public PieDataset getDataPieSetByUtil(Map<String, AtomicInteger> datas) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, AtomicInteger> stringAtomicIntegerEntry : datas.entrySet()) {
            Object value = stringAtomicIntegerEntry.getValue();
            dataset.setValue(stringAtomicIntegerEntry.getKey(), value == null ? 0 : Integer.parseInt(value.toString()));
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
     * @return JFreeChart
     */
    public Optional<JFreeChart> createBarChart(CategoryDataset dataset, String xName, String yName, String chartTitle) {
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
        chart.getTitle().setFont(new Font("SansSerif", Font.PLAIN, 14));
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
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 14));
        class CustomRender extends BarRenderer {
            private Paint[] colors;
            // 初始化柱子颜色

            public CustomRender() {
                colors = colorValues;
            }

            // 每根柱子以初始化的颜色不断轮循
            @Override
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

        return Optional.of(chart);
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
            isChartPathExist(wechatConfiguration.getBasePath());
            String chartName = wechatConfiguration.getBasePath() + charName;
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
     * @return 图表对象
     */
    public Optional<JFreeChart> createValidityComparePimChar(PieDataset<String> dataset, String chartTitle) {

        JFreeChart chart = ChartFactory.createPieChart3D(chartTitle,
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

        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 24));

        // 指定图片的透明度(0.0-1.0)
        plot.setForegroundAlpha(1);
        // 指定显示的饼图上圆形(false)还椭圆形(true)
        plot.setCircular(false, true);
        LegendTitle legend = chart.getLegend();
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 24));
        // 设置第一个 饼块section 的开始位置，默认是12点钟方向
        plot.setStartAngle(90);
        plot.setBackgroundPaint(new Color(39, 43, 88));
        // // 设置分饼颜色
        String[] pieKeys = {"男", "女", "无"};
        plot.setSectionPaint(pieKeys[0], new Color(65, 105, 225));
        plot.setSectionPaint(pieKeys[1], new Color(30, 144, 255));
        plot.setLabelLinkMargin(0.2);
        return Optional.of(chart);
    }

    /**
     * 将图表保存为文件
     *
     * @param chartName 图表名称
     * @param chart     图表
     * @param width     宽度
     * @param height    高度
     * @return 文件路径
     */
    public String saveChartAsPng(String chartName, JFreeChart chart, int width, int height) {
        FileOutputStream fosJpg = null;
        try {
            // 文件夹不存在则创建
            isChartPathExist(wechatConfiguration.getBasePath());
            chartName = wechatConfiguration.getBasePath() + chartName;

            fosJpg = new FileOutputStream(chartName);
            //高宽的设置影响椭圆饼图的形状
            ChartUtils.writeChartAsPNG(fosJpg, chart, width, height);
            fosJpg.close();
            return chartName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fosJpg != null) {
                    fosJpg.close();
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
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
    public JFreeChart createTimeXYChar(String chartTitle, String x, String y,
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

      return chart;
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
    public JFreeChart createStackedBarChart(CategoryDataset dataset, String xName,
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

       return chart;
    }


    /**
     * 生成折线图
     */

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

    public void makeStackedBarChart() {
        double[][] data = new double[][]{{0.21, 0.66, 0.23, 0.40, 0.26},
                {0.25, 0.21, 0.10, 0.40, 0.16}};
        String[] rowKeys = {"苹果", "梨子"};
        String[] columnKeys = {"北京", "上海", "广州", "成都", "深圳"};
        CategoryDataset dataset = getBarData(data, rowKeys, columnKeys);
        createStackedBarChart(dataset, "x坐标", "y坐标", "柱状图", "stsckedBar.png");
    }
}



