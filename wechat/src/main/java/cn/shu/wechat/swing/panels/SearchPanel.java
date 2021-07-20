package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
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
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.ExecutorServiceUtil;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by song on 17-5-29.
 */
public class SearchPanel extends ParentAvailablePanel {
    private static SearchPanel context;
    private RCSearchTextField searchTextField;
    private boolean setSearchMessageOrFileListener = false;

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
    private void search(){
        SearchResultPanel searchResultPanel = SearchResultPanel.getContext();
        ListPanel listPanel = ListPanel.getContext();
        if (searchTextField.getText() == null || searchTextField.getText().isEmpty()) {
            listPanel.showPanel(listPanel.getPreviousTab());
            return;
        }
        listPanel.showPanel(ListPanel.SEARCH);
        new SwingWorker<Object,Object>(){
            private List<SearchResultItem> data;
            @Override
            protected Object doInBackground() throws Exception {
                data = searchUserOrRoom(searchTextField.getText());
                return null;
            }

            @Override
            protected void done() {
                searchResultPanel.setData(data);
                searchResultPanel.setKeyWord(searchTextField.getText());
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
    private List<SearchResultItem> searchUserOrRoom(String key) {
        List<SearchResultItem> list = new ArrayList<>();

        list.add(new SearchResultItem("searchAndListMessage", "搜索 \"" + key + "\" 相关消息", SearchResultType.SEARCH_MESSAGE));
        list.add(new SearchResultItem("searchFile", "搜索 \"" + key + "\" 相关文件", SearchResultType.SEARCH_FILE));
      /*  long begin =  System.currentTimeMillis();*/

        //搜索通讯录
        Future<List<SearchResultItem>> contacts = ExecutorServiceUtil.getGlobalExecutorService().submit(() -> searchContacts(key));
        // 搜索房间
        Future<List<SearchResultItem>> chanelAndGroup = ExecutorServiceUtil.getGlobalExecutorService().submit(() -> searchChannel(key));
        try {
            list.addAll(contacts.get());
            list.addAll(chanelAndGroup.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

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

        return list;
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
    private List<SearchResultItem> searchChannel(String key) {
        List<SearchResultItem> retList = new ArrayList<>();
        Set<Contacts> recentContacts = Core.getRecentContacts();
        SearchResultItem item;
        for (Contacts recentContact : recentContacts) {
            try {
                String remark = recentContact.getRemarkname();
                String nick = recentContact.getNickname();
                if (remark.contains(key)){
                    item = new SearchResultItem(recentContact.getUsername(),remark, SearchResultType.ROOM);
                }else if(nick.contains(key)){
                    item = new SearchResultItem(recentContact.getUsername(),nick, SearchResultType.ROOM);
                }else {
                    continue;
                }
                retList.add(item);
            }catch(Exception e){

            }
        }

        return retList;
    }

    /**
     * 搜索通讯录
     *
     * @param key
     * @return
     */
    private List<SearchResultItem> searchContacts(String key) {

        Map<String, Contacts> memberMap = Core.getMemberMap();
        List<SearchResultItem> retList = new ArrayList<>();
        SearchResultItem item = null;
        for (Map.Entry<String, Contacts> entry : memberMap.entrySet()) {
            Contacts recentContact = entry.getValue();
            try {
                String remark = recentContact.getRemarkname();
                String nick = recentContact.getNickname();
                if (remark.contains(key)){
                    item = new SearchResultItem(entry.getKey(),remark, SearchResultType.CONTACTS);
                }else if(nick.contains(key)){
                    item = new SearchResultItem(entry.getKey(),nick, SearchResultType.CONTACTS);
                }else {
                   continue;
                }
                retList.add(item);
            }catch(Exception e){

            }
        }

        return retList;
    }

}
