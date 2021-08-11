package cn.shu.wechat.swing.panels;

import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.adapter.search.SearchResultItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCSearchTextField;
import cn.shu.wechat.swing.constant.SearchResultType;
import cn.shu.wechat.swing.db.model.FileAttachment;
import cn.shu.wechat.swing.db.model.Message;
import cn.shu.wechat.swing.entity.SearchResultItem;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SleepUtils;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 舒新胜 on 17-5-29.
 */
@Log4j2
public class SearchPanel extends ParentAvailablePanel {
    private static SearchPanel context;
    private RCSearchTextField searchTextField;
    private boolean setSearchMessageOrFileListener = false;
    private final List<SearchResultItem> searchResultItemList = new ArrayList<>();
    private volatile int  searchVer = 0;
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
        searchTextField.setForeground(Colors.FONT_WHITE);
    }

    private void initView() {
        setBackground(Colors.DARK);
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
               // System.out.println("456");;
            }
        });

        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // ESC清除已输入内容
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchTextField.setText("");
                }

                super.keyTyped(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (searchTextField.getText().length() > 8) {
                    e.consume();
                }
            }
        });

    }

    /**
     * 搜索
     */
    private void search() {
        System.out.println("searchResultItemList.size() = " + searchResultItemList.size());
        SearchResultPanel searchResultPanel = SearchResultPanel.getContext();
        ListPanel listPanel = ListPanel.getContext();
        final String text = searchTextField.getText();
        if (text == null || text.isEmpty()) {
            listPanel.showPanel(listPanel.getPreviousTab());
            return;
        }
        searchVer++;
        listPanel.showPanel(ListPanel.SEARCH);
        new SwingWorker<Object, Object>() {
            private List<SearchResultItem> data;
            final int finalI = searchVer;
            @Override
            protected Object doInBackground() throws Exception {
                //TODO 会创建大量 SearchResultItem对象 导致FUllGC卡顿
                if (finalI >= searchVer) {
                    searchCount.set(0);
                    searchUserOrRoom(text, finalI);
                    if (searchResultItemList.isEmpty()||searchCount.get()<=0){
                        return null;
                    }
                    try {
                        data = searchResultItemList.subList(0, searchCount.get() - 1);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                //list.add(new SearchResultItem("searchAndListMessage", "搜索 \"" + key + "\" 相关消息", SearchResultType.SEARCH_MESSAGE));
                //list.add(new SearchResultItem("searchFile", "搜索 \"" + key + "\" 相关文件", SearchResultType.SEARCH_FILE));

                return null;
            }

            @Override
            protected void done() {
                if (finalI <searchVer) {
                    return;
                }
                if (data==null ){
                    data = new ArrayList<>();
                }
                searchResultPanel.setData(data);
                searchResultPanel.setKeyWord(text);
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
     *
     * @param key
     * @return
     */
    private void searchUserOrRoom(String key,int version) {

    /*  long begin =  System.currentTimeMillis();*/

        //搜索通讯录
       // long start = System.currentTimeMillis();
        searchContacts(key,version);
        // 搜索房间
         searchChannel(key,version);
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
        SearchResultPanel searchResultPanel = SearchResultPanel.getContext();
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
        searchResultPanel.notifyDataSetChanged(false);
    }

    /**
     * 搜索并展示文件
     *
     * @param key
     */
    private void searchAndListFile(String key) {
        SearchResultPanel searchResultPanel = SearchResultPanel.getContext();
        List<FileAttachment> fileAttachments = null;//fileAttachmentService.search(key);
        List<SearchResultItem> searchResultItems = new ArrayList<>();

        if (fileAttachments == null || fileAttachments.size() < 1) {
            searchResultPanel.getTipLabel().setVisible(true);
        } else {
            searchResultPanel.getTipLabel().setVisible(false);
            SearchResultItem item;
            for (FileAttachment file : fileAttachments) {
                String content = file.getTitle();
                //content = content.length() > 10 ? content.substring(0, 10) : content;

                item = new SearchResultItem(file.getId(), content, SearchResultType.FILE);
                //item.setTag(msg.getRoomId());

                searchResultItems.add(item);
            }
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
                if (version!=searchVer){
                    break;
                }
                Contacts recentContact = Core.getMemberMap().get(userId);
                String remark = recentContact.getRemarkname();
                String nick = recentContact.getNickname();
                if (remark.contains(key)||nick.contains(key)) {
                    int i = searchCount.getAndIncrement();
                    if (searchResultItemList.size() > i) {
                        item = searchResultItemList.get(i);
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
     *
     * @param key
     * @return
     */
    private void searchContacts(String key,int version) {
        //long start = System.currentTimeMillis();
        Map<String, Contacts> memberMap = Core.getMemberMap();
        SearchResultItem item = null;
        try {
            for (Map.Entry<String, Contacts> entry : memberMap.entrySet()) {
                if (version!=searchVer){
                    break;
                }
                Contacts recentContact = entry.getValue();

                String remark = recentContact.getRemarkname();
                String nick = recentContact.getNickname();
                if (remark.contains(key)||nick.contains(key)){
                    int i = searchCount.getAndIncrement();
                    if (searchResultItemList.size()>i) {
                        item = searchResultItemList.get(i);
                    }else{
                        item = new SearchResultItem();
                        searchResultItemList.add(item);
                    }
                    item.setType(SearchResultType.CONTACTS.CODE);
                    item.setId(entry.getKey());
                    item.setTag(entry.getKey());
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
    }

}
