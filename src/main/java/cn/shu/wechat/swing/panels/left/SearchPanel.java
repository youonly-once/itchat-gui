package cn.shu.wechat.swing.panels.left;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.swing.adapter.search.SearchResultItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCSearchTextField;
import cn.shu.wechat.swing.constant.SearchResultType;
import cn.shu.wechat.swing.entity.SearchResultItem;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.left.tabcontent.LeftTabContentPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.SearchResultPanel;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by 舒新胜 on 17-5-29.
 */
@Log4j2
public class SearchPanel extends ParentAvailablePanel {
    private static SearchPanel context;
    private RCSearchTextField searchTextField;
    private boolean setSearchMessageOrFileListener = false;
    /**
     * 设置的搜索返回结果上线
     */
    private final int resultSize = 20;
    private final List<SearchResultItem> searchResultItemList = new ArrayList<>();
    private final AtomicInteger searchVer = new AtomicInteger();
    /**
     * 搜索到的数量
     */
    private final AtomicInteger searchCount = new AtomicInteger();

    public SearchPanel(JPanel parent) {
        super(parent);
        context = this;

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

    public static SearchPanel getContext() {
        return context;
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
        SearchResultPanel searchResultPanel = SearchResultPanel.getContext();
        LeftTabContentPanel leftTabContentPanel = LeftTabContentPanel.getContext();
        final String key = searchTextField.getText();
        if (key == null || key.isEmpty()) {
            leftTabContentPanel.showPanel(leftTabContentPanel.getPreviousTab());
            return;
        }

        leftTabContentPanel.showPanel(LeftTabContentPanel.SEARCH);
        new SwingWorker<Object, Object>() {
            private List<SearchResultItem> data;
            final int finalI = searchVer.incrementAndGet();
            @Override
            protected Object doInBackground() throws Exception {
                //TODO 会创建大量 SearchResultItem对象 导致FUllGC卡顿
                if (finalI >= searchVer.get()) {
                    searchCount.set(0);
                    searchUserOrRoom(key, finalI);
                    if (searchResultItemList.isEmpty()||searchCount.get()<=0){
                        return null;
                    }
                    try {
                        data = searchResultItemList.subList(0, Math.min(searchResultItemList.size(),searchCount.get()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    log.warn("当前：" + key + "（" + finalI + ")" + "===" + searchVer.get() + "-版本号异常停止更新");
                }

                return null;
            }

            @Override
            protected void done() {
                if (finalI <searchVer.get()) {
                    log.warn("当前：" + key + "（" + finalI + ")" + "===" + searchVer.get() + "-版本号异常停止更新");
                    return;
                }
                if (data==null ){
                    data = new ArrayList<>();
                }
                data.add(0,new SearchResultItem("searchAndListMessage", "搜索 \"" + key + "\" 相关消息", SearchResultType.SEARCH_MESSAGE));
                data.add(0,new SearchResultItem("searchFile", "搜索 \"" + key + "\" 相关文件", SearchResultType.SEARCH_FILE));
                searchResultPanel.setData(data);
                searchResultPanel.setKeyWord(key);
                searchResultPanel.notifyDataSetChanged(false);
                searchResultPanel.getTipLabel().setVisible(false);
            }
        }.execute();
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
    private void searchUserOrRoom(String key,int version) {

    /*  long begin =  System.currentTimeMillis();*/

        //搜索通讯录
       // long start = System.currentTimeMillis();
        searchContacts(key,version);
/*        // 搜索房间
         searchChannel(key,version);*/
        /* System.out.println((System.currentTimeMillis() - begin)/1000.00);*/
        if (!setSearchMessageOrFileListener) {
            // 查找消息、文件
            SearchResultPanel.getContext().setSearchMessageOrFileListener(new SearchResultItemsAdapter.SearchMessageOrFileListener() {
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
        SearchResultPanel searchResultPanel = SearchResultPanel.getContext();

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
     * 搜索房间
     *
     * @param key
     * @return
     */
    private void searchChannel(String key,int version) {
        List<SearchResultItem> retList = new ArrayList<>();
        Set<String> recentContacts = Core.getRecentContacts();
        // long start = System.currentTimeMillis();
        SearchResultItem item;
        try {
            for (String userId : recentContacts) {
                if (version!=searchVer.get()){
                    log.error("版本号不对，终止");
                    break;
                }
                Contacts recentContact = Core.getMemberMap().get(userId);
                String remark = recentContact.getRemarkname();
                String nick = recentContact.getNickname();
                if (remark.contains(key)||nick.contains(key)) {
                    int i = searchCount.getAndIncrement();
                    if (i >= resultSize+1){
                        log.warn("已达搜索条数上限10");
                        return;
                    }
                    if (searchResultItemList.size() > i) {
                        item = searchResultItemList.get(i);
                        if (item == null){
                            item = new SearchResultItem();
                            searchResultItemList.add(item);
                        }
                    } else {
                        item = new SearchResultItem();
                        searchResultItemList.add(item);
                    }
                    item.setTag(userId);
                    item.setType(SearchResultType.ROOM.CODE);
                    item.setId(userId);
                    if (remark.contains(key)) {
                        item.setName(remark);
                    } else if (nick.contains(key)) {
                        item.setName(nick);
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        // System.out.println("System.currentTimeMillis()-start Channel= " + (System.currentTimeMillis() - start));

    }

    /**
     * 搜索通讯录
     * @param key 关键词
     * @param version 搜索版本 本次搜索未完成时另一次搜索开始，此时通过版本号终止本次搜索
     */
    private void searchContacts(String key,int version) {
        Map<String, Contacts> memberMap = Core.getMemberMap();
        SearchResultItem item = null;
        try {
            for (Map.Entry<String, Contacts> entry : memberMap.entrySet()) {
                if (version!=searchVer.get()){
                    log.error("版本号不对，终止");
                    break;
                }
                Contacts recentContact = entry.getValue();
                String remark = recentContact.getRemarkname();
                String nick = recentContact.getNickname();
                String displayname = recentContact.getDisplayname();
                if ((remark!=null && remark.contains(key))
                        ||(nick!=null && nick.contains(key))
                        || (displayname!=null && displayname.contains(key))){
                    int i = searchCount.get();
                    if (i >= resultSize){
                        log.warn("已达搜索条数上限" + resultSize);
                        return;
                    }
                    searchCount.incrementAndGet();
                    if (searchResultItemList.size()>i) {
                        item = searchResultItemList.get(i);
                        if (item == null){
                            item = new SearchResultItem();
                            searchResultItemList.add(item);
                        }
                    }else{
                        item = new SearchResultItem();
                        searchResultItemList.add(item);
                    }
                    item.setType(SearchResultType.CONTACTS.CODE);
                    item.setId(entry.getKey());
                    item.setTag(entry.getKey());
                    if (remark!=null && remark.contains(key)) {
                        item.setName(remark);
                    } else if (nick!=null && nick.contains(key)) {
                        item.setName(nick);
                    } else if (displayname!=null && displayname.contains(key)) {
                        item.setName(displayname);
                    }
                }

            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}
