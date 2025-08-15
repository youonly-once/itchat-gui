package cn.shu.wechat.swing.adapter.search;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.constant.SearchResultType;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.SearchResultItem;
import cn.shu.wechat.swing.adapter.BaseAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.helper.AttachmentIconHelper;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.media.HeadLoadingSwingWorker;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.swing.panels.UserInfoPanel;
import cn.shu.wechat.swing.panels.chat.ChatPanelContainer;
import cn.shu.wechat.swing.panels.left.TabOperationPanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.utils.DateUtils;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.FileUtil;
import cn.shu.wechat.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.ref.WeakReference;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 搜索结果适配器
 * Created by 舒新胜 on 17-5-30.
 */
public class SearchResultItemsAdapter extends BaseAdapter<SearchResultItemViewHolder> {
    private final List<SearchResultItem> searchResultItems;
    private String keyWord;
    private SearchMessageOrFileListener searchMessageOrFileListener;

    public static final int VIEW_TYPE_CONTACTS_ROOM = 0;
    public static final int VIEW_TYPE_MESSAGE = 1;
    public static final int VIEW_TYPE_FILE = 2;
    private final AttachmentIconHelper attachmentIconHelper = new AttachmentIconHelper();

    private final List<WeakReference<SearchResultFileItemViewHolder>> fileItemViewHolders = new ArrayList<>();
    private final List<WeakReference<SearchResultUserItemViewHolder>> searchResultUserItemViewHolderList = new ArrayList<>(10);

    public SearchResultItemsAdapter(List<SearchResultItem> searchResultItems) {
        this.searchResultItems = searchResultItems;
    }

    @Override
    public int getCount() {
        return searchResultItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        SearchResultType byCode = SearchResultType.getByCode(searchResultItems.get(position).getType());
        return switch (byCode) {
            case SEARCH_MESSAGE, SEARCH_FILE, CONTACTS, ROOM -> VIEW_TYPE_CONTACTS_ROOM;
            case FILE -> VIEW_TYPE_FILE;
            case MESSAGE -> VIEW_TYPE_MESSAGE;
            default -> throw new RuntimeException("ViewType 不正确");
        };
    }


    @Override
    public SearchResultItemViewHolder onCreateViewHolder(int viewType,int subViewType,  int position) {
        switch (viewType) {
            case VIEW_TYPE_CONTACTS_ROOM: {
                //避免重复创建
                SearchResultUserItemViewHolder holder = null;
                if(searchResultUserItemViewHolderList.size() > position){
                    holder = searchResultUserItemViewHolderList.get(position).get();
                    if (holder == null){
                        holder = new SearchResultUserItemViewHolder();
                        searchResultUserItemViewHolderList.set(position,new WeakReference<>(holder));
                    }
                }else{
                    holder = new SearchResultUserItemViewHolder();
                    searchResultUserItemViewHolderList.add(position,new WeakReference<>(holder));
                }
                return holder;
            }
            case VIEW_TYPE_MESSAGE: {
                return new SearchResultMessageViewHolder();
            }
            case VIEW_TYPE_FILE: {
                //避免重复创建
                SearchResultFileItemViewHolder holder = null;
                if(fileItemViewHolders.size() > position){
                    holder = fileItemViewHolders.get(position).get();
                    if (holder == null){
                        holder = new SearchResultFileItemViewHolder();
                        fileItemViewHolders.set(position,new WeakReference<>(holder));
                    }
                }else{
                    holder = new SearchResultFileItemViewHolder();
                    fileItemViewHolders.add(position,new WeakReference<>(holder));
                }
                return holder;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public void onBindViewHolder(SearchResultItemViewHolder viewHolder, int position) {
        SearchResultItem item = searchResultItems.get(position);

        if (viewHolder instanceof SearchResultUserItemViewHolder) {
            processContactsOrRoomsResult(viewHolder, item);
        } else if (viewHolder instanceof SearchResultMessageViewHolder) {
            processMessageResult(viewHolder, item);
        } else if (viewHolder instanceof SearchResultFileItemViewHolder) {
            processFileResult(viewHolder, item);
        }

//        if (!viewHolders.contains(viewHolder))
//        {
//            viewHolders.add(viewHolder);
//        }

        //viewHolder.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //SearchResultItem item = searchResultItems.get(position);

    }

    /**
     * 处理文件搜索结果
     *
     * @param viewHolder
     * @param item
     */
    private void processFileResult(SearchResultItemViewHolder viewHolder, SearchResultItem item) {
        SearchResultFileItemViewHolder holder = (SearchResultFileItemViewHolder) viewHolder;

        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(item.getName());
        attachmentTypeIcon.setImage(attachmentTypeIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        holder.avatar.setIcon(attachmentTypeIcon);
        holder.name.setKeyWord(keyWord);
        holder.dateTime.setText(item.getDateTime().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)));
        String filename = item.getName();
        if (item.getName().length() > 20) {
            String suffix = filename.substring(filename.lastIndexOf("."));
            filename = item.getName().substring(0, 15) + "..." + suffix;
        }

        holder.name.setText(filename);

        if (item.getTag() != null && new File(item.getTag().toString()).exists()) {
            holder.size.setText(FileUtil.fileSizeString(item.getTag().toString()));
        }else {
            holder.size.setText("未下载");
        }


        holder.setToolTipText(item.getName());

        processMouseListeners(viewHolder, item);
    }

    /**
     * 处理消息搜索结果
     *
     * @param viewHolder
     * @param item
     */
    private void processMessageResult(SearchResultItemViewHolder viewHolder, SearchResultItem item) {
        SearchResultMessageViewHolder holder = (SearchResultMessageViewHolder) viewHolder;

        new HeadLoadingSwingWorker(holder.avatar, item.getTag()).loadAvatar();
        holder.brief.setKeyWord(keyWord);
        String content = item.getName();

        //********
        int totalLen = 25;
        String key = item.getKey();
        int keyLen = key.length();
        int pre = (totalLen - keyLen) / 2 - 3; // 给...留空间
        if (pre < 0) pre = 0;

        int i = content.indexOf(key);
        if (content.length() > totalLen && i >= 0) {
            int start = Math.max(i - pre, 0);
            int end = Math.min(i + keyLen + pre, content.length());

            String left = content.substring(start, i);
            String right = content.substring(i + keyLen, end);

            boolean atStart = start == 0;
            boolean atEnd = end == content.length();

            if (atStart && !atEnd) {
                // 开头不加省略号
                content = left + key + right + "...";
            } else if (!atStart && atEnd) {
                // 结尾不加省略号
                content = "..." + left + key + right;
            } else if (!atStart) {
                // 两边都加省略号
                content = "..." + left + key + right + "...";
            } else {
                // 完全显示，不加省略号
                content = left + key + right;
            }
        }
        //***************
        if (item.getSender().length() > 7) {
            holder.roomName.setText(item.getSender().substring(0, 7) + "...");
        } else {

            holder.roomName.setText(item.getSender());
        }

        holder.brief.setText(content);
        holder.time.setText(item.getDateTime().format(DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS)));
        holder.setToolTipText(item.getName());
        processMouseListeners(viewHolder, item);


    }

    private void processMouseListeners(SearchResultItemViewHolder viewHolder, SearchResultItem item) {
        if (viewHolder.mouseListener != null){
            viewHolder.mouseListener.fresh(item
                    ,viewHolder);

        }else{
            viewHolder.mouseListener = new SearchResultItemAbstractMouseListener( item
            ,viewHolder);
           viewHolder.addMouseListener(  viewHolder.mouseListener);
        }

    }

    /**
     * 处理通讯录或群组探索结果
     *
     * @param viewHolder
     * @param item
     */
    private void processContactsOrRoomsResult(SearchResultItemViewHolder viewHolder, SearchResultItem item) {
        SearchResultUserItemViewHolder holder = (SearchResultUserItemViewHolder) viewHolder;
        holder.name.setKeyWord(this.keyWord);
        holder.name.setText(item.getName());
        SearchResultType byCode = SearchResultType.getByCode(item.getType());
        switch (byCode) {
            case CONTACTS:
                new HeadLoadingSwingWorker(holder.avatar, item.getTag()).loadAvatar();
                holder.type.setText("联系人");
                break;
            case ROOM:
                new HeadLoadingSwingWorker(holder.avatar, item.getTag()).loadAvatar();
                holder.type.setText("聊天房");
                break;
            case SEARCH_FILE:
                holder.avatar.setIcon(IconUtil.getIcon(this, "/image/file_icon.png", 25, 25));
                break;
            case SEARCH_MESSAGE:
                holder.avatar.setIcon(IconUtil.getIcon(this, "/image/message.png", 25, 25));
                break;
            case MESSAGE:
                new HeadLoadingSwingWorker(holder.avatar, item.getTag()).loadAvatar();
                break;
            default:
                throw new RuntimeException("ViewType 不正确");
        }

        processMouseListeners(viewHolder, item);
    }

    ;

    class SearchResultItemAbstractMouseListener extends AbstractMouseListener {
        private WeakReference<JPopupMenu> jPopupMenu ;
        private SearchResultItemViewHolder holder;
        private WeakReference<SearchResultItem> item;

        public void fresh(SearchResultItem item, SearchResultItemViewHolder viewHolder) {
            this.holder = viewHolder;
            this.item = new WeakReference<>(item);
        }
        public SearchResultItemAbstractMouseListener(SearchResultItem item, SearchResultItemViewHolder holder) {
            fresh(item,holder);

        }
        @Override
        public void mouseReleased(MouseEvent e) {
            SearchResultItem searchResultItem = item.get();
            if (searchResultItem == null) {
                return;
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
                switch (SearchResultType.getByCode(searchResultItem.getType())) {
                    case CONTACTS:
                    case ROOM:
                        UserInfoPanel.getContext().setContacts(Core.getMemberMap().get(searchResultItem.getId()));
                        RightPanel.getContext().show(RightPanel.USER_INFO);
                        //enterRoom(item.getId(), 0L);
                        //clearSearchText();
                        break;
                    case SEARCH_FILE:
                        if (searchMessageOrFileListener != null) {
                            searchMessageOrFileListener.onSearchFile();
                        }
                        break;
                    case SEARCH_MESSAGE:
                        if (searchMessageOrFileListener != null) {
                            searchMessageOrFileListener.onSearchMessage();
                        }
                        break;
                    case MESSAGE: {
                        if (Core.getMemberMap().containsKey(searchResultItem.getTag())) {
                            RoomsPanel.getContext().enterRoom(searchResultItem.getTag());
                        }else{
                            Optional<Contacts> contacts = ContactsTools.findContactsByString(searchResultItem.getSender());
                            contacts.ifPresent(con->{
                                RoomsPanel.getContext().enterRoom(con.getUsername());
                            });
                        }

                        break;
                    }
                    case FILE:{
                            downloadOrOpenFile(item.get().getTag(), holder);
                        break;
                    }
                    default:
                        throw new RuntimeException("ViewType 不正确");
                }
            }else if (e.getButton() == MouseEvent.BUTTON3){
                switch (SearchResultType.getByCode(searchResultItem.getType())) {
                    case FILE: {
                        JPopupMenu jPopupMenuLocal = jPopupMenu.get();
                        if (jPopupMenuLocal == null) {
                            jPopupMenuLocal = new JPopupMenu();
                            jPopupMenu = new WeakReference<>(jPopupMenuLocal);
                        }
                        JMenuItem jMenuItem = new JMenuItem("打开文件夹");
                        jMenuItem.addActionListener(e1 -> ExecutorServiceUtil.getGlobalExecutorService().submit(() -> FileUtil.showAtExplorer(searchResultItem.getTag())));
                        jPopupMenuLocal.add(jMenuItem);

                        jPopupMenuLocal.show(holder, e.getX()
                                , e.getY());
                        break;
                    }
                }
            }
        }


        @Override
        public void mouseEntered(MouseEvent e) {
            setBackground(holder, Colors.ITEM_SELECTED_LIGHT);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setBackground(holder, Colors.WINDOW_BACKGROUND);
        }

    }


    /**
     * 根据房间类型获取对应的头像
     *
     * @return
     */
    /*private Image getRoomAvatar(String type, String name)
    {
        if (type.equals("c"))
        {
            return AvatarUtil.createOrLoadGroupAvatar("##", name).getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        }
        else if (type.equals("p"))
        {
            return AvatarUtil.createOrLoadGroupAvatar("#", name).getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        }
        // 私聊头像
        else if (type.equals("d"))
        {
            return AvatarUtil.createOrLoadAvatar(name).getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        }

        return null;
    }*/

    private void clearSearchText() {
        //LeftTabContentPanel.getContext().showPanel(LeftTabContentPanel.CHAT);
        //SearchPanel.getContext().clearSearchText();
    }

    /**
     * 设置item的背影色
     *
     * @param holder
     * @param color
     */
    private void setBackground(SearchResultItemViewHolder holder, Color color) {
        holder.setBackground(color);
        if (holder instanceof SearchResultUserItemViewHolder) {
            ((SearchResultUserItemViewHolder) holder).name.setBackground(color);
        } else if (holder instanceof SearchResultMessageViewHolder) {
            ((SearchResultMessageViewHolder) holder).nameBrief.setBackground(color);
        } else if (holder instanceof SearchResultFileItemViewHolder) {
            ((SearchResultFileItemViewHolder) holder).nameProgressPanel.setBackground(color);
        }
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    private void enterRoom(String roomId) {
        //添加房间
        RoomsPanel.getContext().addRoomOrUpdateRoom(roomId, "", 0, null, false, false);
        TabOperationPanel.getContext().switchToChatLabel();
        //添加聊天房
         ChatPanelContainer.getContext().createAndShow(roomId);

    }

    public void setSearchMessageOrFileListener(SearchMessageOrFileListener searchMessageOrFileListener) {
        this.searchMessageOrFileListener = searchMessageOrFileListener;
    }

    /**
     * 打开文件，如果文件不存在，则下载
     *
     * @param filePath 文件路径
     * @param holder
     */
    public void downloadOrOpenFile(Object filePath, SearchResultItemViewHolder holder) {
        if (filePath!=null){
            FileUtil.openFileWithDefaultApplication(filePath.toString());
        }else{
            JOptionPane.showMessageDialog(null, "无效的附件", "附件无效", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //下载 循环设置progressBar值
    /*    holder.progressBar.setVisible(true);
        holder.size.setText("下载中...");*/
    }





    public interface SearchMessageOrFileListener {
        void onSearchMessage();

        void onSearchFile();
    }

    @Override
    public void removeAllListenersRecursively(Component comp) {
        super.removeAllListenersRecursively(comp);
        if (comp instanceof SearchResultItemViewHolder t){
            t.mouseListener = null;
        }
    }
}
