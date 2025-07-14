package cn.shu.wechat.swing.panels.search;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.adapter.search.SearchResultItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCSearchTextField;
import cn.shu.wechat.swing.constant.SearchResultType;
import cn.shu.wechat.swing.entity.SearchResultItem;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import com.github.promeg.pinyinhelper.Pinyin;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 舒新胜 on 17-5-29.
 * 主要包括搜索框组件 、搜索处理逻辑以及调用相应SearchResultPanel渲染数据
 */
@Log4j2
public class SearchPanel extends ParentAvailablePanel {
    private RCSearchTextField searchTextField;
    private boolean setSearchMessageOrFileListener = false;
    /**
     * 防抖
     */
    private final SearchDebounce debounce = new SearchDebounce(100);
    private SwingWorker<Object, Object> swingWorker;

    /**
     * 设置的搜索返回结果上线
     */
    private final int resultSize = 20;

    private final AtomicInteger searchVer = new AtomicInteger();

    /**
     * 展示搜索结果的Panel
     */
    private final SearchResultPanel searchResultPanel;

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();

    private static final int MAX_RESULT = 20;


    public SearchPanel(JPanel parent, SearchResultPanel searchResultPanel) {
        super(parent);
        this.searchResultPanel = searchResultPanel;
        initComponent();
        initView();
        setListeners();
    }


    private void initComponent() {
        searchTextField = new RCSearchTextField();
        searchTextField.setFont(FontUtil.getDefaultFont(14));
        searchTextField.setForeground(Colors.DARK);
    }

    private void initView() {
        setBackground(Colors.WINDOW_BACKGROUND);
        this.setLayout(new GridBagLayout());
        this.add(searchTextField, new GBC(0, 0)
                .setFill(GBC.HORIZONTAL)
                .setWeight(1, 1)
                .setInsets(0, 15, 0, 15)
        );
    }


    /**
     * 添加搜索框事件
     */
    private void setListeners() {
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
               search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC清除已输入内容
                    clearSearchText();
                }else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    //回车搜索
                    search();
                }
                super.keyTyped(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
               /* if (searchTextField.getText().length() > 8) {
                    e.consume();
                }*/
            }
        });

    }

    /**
     * 搜索
     */
    private void search() {
        final String keyword = searchTextField.getText();
        if (StringUtils.isEmpty(keyword)) {
            searchResultPanel.showPreviousTab();
            return;
        }
        //展示搜索结果panel
        searchResultPanel.showSelf();
        if (swingWorker != null && !swingWorker.isDone()) {
            log.info("正在取消上一个搜索任务");
            // 请求中断
            swingWorker.cancel(true);
        }
        swingWorker = new SwingWorker<>() {
            //当前搜索版本
            final int currentVersion = searchVer.incrementAndGet();
            private List<SearchResultItem> data = new ArrayList<>();

            @Override
            protected Object doInBackground() throws Exception {
                if (outdatedVersionAndInterrupted(currentVersion)) {
                    return null;
                }
                data = new ArrayList<>();
                data.add(new SearchResultItem("searchAndListMessage", "搜索 \"" + keyword + "\" 相关消息", SearchResultType.SEARCH_MESSAGE));
                data.add(new SearchResultItem("searchFile", "搜索 \"" + keyword + "\" 相关文件", SearchResultType.SEARCH_FILE));
                searchUserOrRoom(keyword, currentVersion, data);

                if (outdatedVersionAndInterrupted(currentVersion)) {
                    return null;
                }

                return null;
            }

            @Override
            protected void done() {
                if (outdatedVersionAndInterrupted(currentVersion)) {
                    return;
                }

                //渲染搜索结果Panel
                searchResultPanel.setData(data);
                searchResultPanel.setKeyWord(keyword);
                searchResultPanel.notifyDataSetChanged(false);
                searchResultPanel.getTipLabel().setVisible(false);
            }
        };
        //延迟调用 防抖
        debounce.debounce(swingWorker);
    }

    /**
     * 清空搜索文本
     */
    public void clearSearchText() {
        searchTextField.setText("");
    }


    /**
     * 搜索用户或房间
     * @param key 关键词
     * @param version 搜索版本 本次搜索未完成时另一次搜索开始，此时通过版本号终止本次搜索
     */
    private void searchUserOrRoom(String key, int version, List<SearchResultItem> data) {

        //搜索通讯录
        searchContacts(key, version, data);

        if (!setSearchMessageOrFileListener) {
            // 查找消息、文件
            searchResultPanel.setSearchMessageOrFileListener(new SearchResultItemsAdapter.SearchMessageOrFileListener() {
                @Override
                public void onSearchMessage() {
                    searchAndListMessage(searchTextField.getText());
                }

                @Override
                public void onSearchFile() {
                    searchAndListFile(searchTextField.getText());
                }
            });

            setSearchMessageOrFileListener = true;
        }

    }

    /**
     * 搜索并展示消息
     *
     * @param key
     */
    private void searchAndListMessage(String key) {
       /* SearchResultPanel searchResultPanel = SearchResultPanel.getContext();
        List<Message> messages = null; //= messageService.search(key);
        List<SearchResultItem> searchResultItems = new ArrayList<>();

        if (messages == null || messages.size() < 1) {
            searchResultPanel.getTipLabel().setVisible(true);
        } else {
            searchResultPanel.getTipLabel().setVisible(false);

            SearchResultItem item;
            for (Message msg : messages) {
                String content = msg.getMessageContent();
                int startPos = content.toLowerCase().indexOf(key.toLowerCase());
                int endPos = startPos + 10;
                //endPos = endPos > content.length() ? content.length() : endPos;
                if (endPos > content.length()) {
                    endPos = content.length();
                    content = content.substring(startPos, endPos);
                } else {
                    content = content.substring(startPos, endPos) + "...";
                }

                item = new SearchResultItem(msg.getId(), content, SearchResultType.MESSAGE);
                item.setTag(msg.getRoomId());

                searchResultItems.add(item);
            }
        }

        searchResultPanel.setData(searchResultItems);
        searchResultPanel.setKeyWord(key);
        searchResultPanel.notifyDataSetChanged(false);*/
    }

    /**
     * 搜索并展示文件
     *
     * @param key 搜索关键字
     */
    private void searchAndListFile(String key) {
        MessageMapper messageMapper = SpringContextHolder.getBean(MessageMapper.class);

        //搜索数据库
        List<SearchResultItem> searchResultItems;
       List<Message> messages = messageMapper.searchFileByName(key);
        if (messages.isEmpty()) {
            searchResultItems = new ArrayList<>();
            searchResultPanel.getTipLabel().setVisible(true);
        } else {
            searchResultPanel.getTipLabel().setVisible(false);

            searchResultItems = messages.stream()
                    .map(message -> new SearchResultItem(key, message.getPlaintext(),
                            SearchResultType.FILE,
                            message.getCreateTime(),
                            message.getFilePath()))
                    .collect(Collectors.toList());

        }

        searchResultPanel.setKeyWord(key);
        searchResultPanel.setData(searchResultItems);
        searchResultPanel.notifyDataSetChanged(false);
    }

    /**
     * 搜索通讯录
     * @param keyWord 关键词
     * @param version 搜索版本 本次搜索未完成时另一次搜索开始，此时通过版本号终止本次搜索
     */
    private void searchContacts(String keyWord, int version, List<SearchResultItem> data) {
        Map<String, Contacts> memberMap = Core.getMemberMap();


        List<SearchResultItem> results = memberMap.entrySet().stream()
                .takeWhile(entry -> !outdatedVersionAndInterrupted(version))
                .map(entry -> {
                    Contacts contact = entry.getValue();
                    // 计算匹配分数
                    String match = Stream.of(contact.getRemarkname(), contact.getNickname(), contact.getDisplayname())
                            .filter(Objects::nonNull)
                            .filter(field -> field.toLowerCase().contains(keyWord))
                            .findAny()
                            .orElse(null);
                    if (match == null) return null;

                    SearchResultItem item = new SearchResultItem();
                    item.setType(SearchResultType.CONTACTS.CODE);
                    item.setId(entry.getKey());
                    item.setTag(entry.getKey());
                    item.setName(match);
                    return item;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(SearchResultItem::getScore).reversed())
                .limit(MAX_RESULT)
                .toList();

        data.addAll(results);
    }

    /**
     * 判断版本号是否过时以及线程是否终止
     *
     * @param version 当前版本号
     * @return
     */
    private boolean outdatedVersionAndInterrupted(int version) {
        if (version != searchVer.get()) {
            log.warn("版本号不对，终止，{}！={}", version, searchVer.get());
            //return true;
        }
        if (swingWorker.isCancelled()) {
            log.warn("线程被Cancelled");
            return true;
        }
        if (Thread.currentThread().isInterrupted()) {
            //这个好像不生效
            log.warn("线程被Interrupted");
            return true;
        }
        return false;
    }

    public static class SearchDebounce {
        private final int delayMs;
        private Timer timer;

        public SearchDebounce(int delayMs) {
            this.delayMs = delayMs;
        }

        /**
         * 每次调用都会重置定时器，延迟 delayMs 后执行 task
         */
        public synchronized <T, V> void debounce(SwingWorker<T, V> task) {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    task.execute();
                }
            }, delayMs);
        }
    }

    /**
     * 转换字符串为拼音全拼，非汉字保持原样（用TinyPinyin）
     */
    private String toPinyin(String input) {
        return Pinyin.toPinyin(input, "").toLowerCase();
    }

    /**
     * 转换字符串为拼音首字母简写
     */
    private String toInitial(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) continue;
            if (c >= 'a' && c <= 'z') {
                sb.append(c);
            } else {
                String p = Pinyin.toPinyin(String.valueOf(c), "");
                if (!p.isEmpty()) sb.append(p.charAt(0));
                else sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }

    private int calculateScore(Contacts contact, String keyWordLower, String keyWordPinyin, String keyWordInitial) {
        int maxScore = 0;
        for (FieldData fd : List.of(
                new FieldData(contact.getRemarkname(), contact.getRemarkpyquanpin(), 30),
                new FieldData(contact.getNickname(), contact.getPyquanpin(), 20),
                new FieldData(contact.getDisplayname(), contact.getPyquanpin(), 10))) {
            String field = fd.text;
            String pinyin = fd.pinyin;
            if (field == null) continue;

            String fieldLower = field.toLowerCase();
            int score = 0;

            // 基础规则匹配
            if (fieldLower.equals(keyWordLower)) score = 100;
            else if (fieldLower.startsWith(keyWordLower)) score = 80;
            else if (fieldLower.contains(keyWordLower)) score = 50;

            // 拼音匹配（直接用已有拼音字段）
            if (pinyin != null) {
                String pinyinLower = pinyin.toLowerCase();
                if (pinyinLower.contains(keyWordPinyin)) score = Math.max(score, 40);
                if (pinyinLower.startsWith(keyWordInitial)) score = Math.max(score, 30);
            }

            // 模糊匹配（编辑距离）
            int distance = levenshtein.apply(keyWordLower, fieldLower);
            if (distance >= 0 && distance <= 2) {
                int fuzzyScore = 20 + (2 - distance) * 10;
                score = Math.max(score, fuzzyScore);
            }

            // 字段优先级加权
            score += fd.weight;

            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }

    private static class FieldData {
        final String text;
        final String pinyin;
        final int weight;

        FieldData(String text, String pinyin, int weight) {
            this.text = text;
            this.pinyin = pinyin;
            this.weight = weight;
        }
    }

}
