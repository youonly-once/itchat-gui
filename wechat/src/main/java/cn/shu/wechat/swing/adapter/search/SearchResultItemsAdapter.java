package cn.shu.wechat.swing.adapter.search;

import cn.shu.wechat.swing.adapter.BaseAdapter;
import cn.shu.wechat.swing.app.Launcher;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.constant.SearchResultType;
import cn.shu.wechat.swing.db.model.CurrentUser;
import cn.shu.wechat.swing.db.model.FileAttachment;
import cn.shu.wechat.swing.db.model.Message;
import cn.shu.wechat.swing.db.model.Room;
import cn.shu.wechat.swing.db.service.CurrentUserService;
import cn.shu.wechat.swing.db.service.FileAttachmentService;
import cn.shu.wechat.swing.db.service.MessageService;
import cn.shu.wechat.swing.db.service.RoomService;
import cn.shu.wechat.swing.entity.SearchResultItem;
import cn.shu.wechat.swing.helper.AttachmentIconHelper;
import cn.shu.wechat.swing.listener.AbstractMouseListener;
import cn.shu.wechat.swing.panels.*;
import cn.shu.wechat.swing.tasks.DownloadTask;
import cn.shu.wechat.swing.tasks.HttpResponseListener;
import cn.shu.wechat.swing.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索结果适配器
 * Created by 舒新胜 on 17-5-30.
 */
public class SearchResultItemsAdapter extends BaseAdapter<SearchResultItemViewHolder> {
    private List<SearchResultItem> searchResultItems;
    private String keyWord;
    private SearchMessageOrFileListener searchMessageOrFileListener;

    public static final int VIEW_TYPE_CONTACTS_ROOM = 0;
    public static final int VIEW_TYPE_MESSAGE = 1;
    public static final int VIEW_TYPE_FILE = 2;
    private AttachmentIconHelper attachmentIconHelper = new AttachmentIconHelper();
    private FileCache fileCache = new FileCache();
    private List<String> downloadingFiles = new ArrayList<>(); // 正在下载的文件

    //private List<SearchResultFileItemViewHolder> fileItemViewHolders = new ArrayList<>();
    private Map<String, SearchResultFileItemViewHolder> fileItemViewHolders = new HashMap<>();


    public SearchResultItemsAdapter(List<SearchResultItem> searchResultItems) {
        this.searchResultItems = searchResultItems;
    }

    @Override
    public int getCount() {
        return searchResultItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        // return super.getItemViewType(position);
        SearchResultType byCode = SearchResultType.getByCode(searchResultItems.get(position).getType());
        switch (byCode) {
            case SEARCH_MESSAGE:
            case SEARCH_FILE:
            case CONTACTS:
            case ROOM:
                return VIEW_TYPE_CONTACTS_ROOM;
            case FILE:
                return VIEW_TYPE_FILE;
            case MESSAGE:
                return VIEW_TYPE_MESSAGE;
            default:
                throw new RuntimeException("ViewType 不正确");
        }
    }


    @Override
    public SearchResultItemViewHolder onCreateViewHolder(int viewType, int position) {
        switch (viewType) {
            case VIEW_TYPE_CONTACTS_ROOM: {
                return new SearchResultUserItemViewHolder();
            }
            case VIEW_TYPE_MESSAGE: {
                return new SearchResultMessageItemViewHolder();
            }
            case VIEW_TYPE_FILE: {
                return new SearchResultFileItemViewHolder();
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
        } else if (viewHolder instanceof SearchResultMessageItemViewHolder) {
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
        //fileItemViewHolders.add(holder);
        fileItemViewHolders.put(item.getId(), holder);

        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(item.getName());
        attachmentTypeIcon.setImage(attachmentTypeIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        holder.avatar.setIcon(attachmentTypeIcon);
        holder.name.setKeyWord(keyWord);

        String filename = item.getName();
        if (item.getName().length() > 20) {
            String suffix = filename.substring(filename.lastIndexOf("."));
            filename = item.getName().substring(0, 15) + "..." + suffix;
        }

        holder.name.setText(filename);

        String filePath = fileCache.tryGetFileCache(item.getId(), item.getName());
        if (filePath != null) {
            holder.size.setText(fileCache.fileSizeString(filePath));
        } else {
            holder.size.setText("未下载");
        }

        holder.setToolTipText(item.getName());

        holder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(holder, Colors.ITEM_SELECTED_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(holder, Colors.DARK);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    downloadOrOpenFile(item.getId(), holder);
                }
                super.mouseReleased(e);
            }
        });
    }

    /**
     * 处理消息搜索结果
     *
     * @param viewHolder
     * @param item
     */
    private void processMessageResult(SearchResultItemViewHolder viewHolder, SearchResultItem item) {
        SearchResultMessageItemViewHolder holder = (SearchResultMessageItemViewHolder) viewHolder;
        Room room = null;

        Message message = null;

        holder.avatar.setIcon(new ImageIcon(getRoomAvatar(((String) item.getTag()).startsWith("@@"), room.getRoomId())));
        holder.brief.setKeyWord(keyWord);
        holder.brief.setText(item.getName());
        holder.roomName.setText(room.getName());
        holder.time.setText(TimeUtil.diff(message.getTimestamp()));

        holder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    enterRoom(room.getRoomId(), message.getTimestamp());
                    clearSearchText();
                }
                super.mouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(holder, Colors.ITEM_SELECTED_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(holder, Colors.DARK);
            }
        });
    }

    private void processMouseListeners(SearchResultItemViewHolder viewHolder, SearchResultItem item) {
        viewHolder.addMouseListener(new AbstractMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    SearchResultType byCode = SearchResultType.getByCode(item.getType());
                    switch (byCode) {
                        case CONTACTS:
                        case ROOM:
                            enterRoom(item.getId(), 0L);
                            clearSearchText();
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
                        case MESSAGE:
            /*                Room room = roomService.findById((String) ((Map) item.getTag()).get("roomId"));
                            if (room != null)
                            {
                                icon.setImage(getRoomAvatar(room.getType(), room.getName()));
                            }*/
                            break;
                        default:
                            throw new RuntimeException("ViewType 不正确");
                    }
                }
            }


            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(viewHolder, Colors.ITEM_SELECTED_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(viewHolder, Colors.DARK);
            }
        });
    }

    private void clearSearchText() {
        ListPanel.getContext().showPanel(ListPanel.CHAT);
        SearchPanel.getContext().clearSearchText();
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

        ImageIcon icon = new ImageIcon();
        SearchResultType byCode = SearchResultType.getByCode(item.getType());
        switch (byCode) {
            case CONTACTS:
                icon.setImage(getRoomAvatar(true, item.getTag().toString()));
                holder.type.setText("联系人");
                break;
            case ROOM:
                icon.setImage(getRoomAvatar(true, item.getTag().toString()));
                holder.type.setText("聊天房");
                break;
            case SEARCH_FILE:
                icon.setImage(IconUtil.getIcon(this, "/image/file_icon.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
                break;
            case SEARCH_MESSAGE:
                icon.setImage(IconUtil.getIcon(this, "/image/message.png").getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
            case MESSAGE:
/*                Room room = roomService.findById((String) ((Map) item.getTag()).get("roomId"));
                if (room != null)
                {
                    icon.setImage(getRoomAvatar(room.getType(), room.getName()));
                }*/
                break;
            default:
                throw new RuntimeException("ViewType 不正确");
        }
        holder.avatar.setIcon(icon);

        processMouseListeners(viewHolder, item);
    }


    /**
     * 根据房间类型获取对应的头像
     *
     * @param type
     * @param name
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
            return AvatarUtil.createOrLoadUserAvatar(name).getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        }

        return null;
    }*/

    /**
     * 根据房间类型获取对应的头像
     *
     * @param isGroup 是否为群
     * @param roomId  房间id
     * @return 头像
     */
    private Image getRoomAvatar(boolean isGroup, String roomId) {
        if (isGroup) {
            //群头像
            return AvatarUtil.createOrLoadGroupAvatar(roomId).getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        }
        // 私聊头像
        else {
            return AvatarUtil.createOrLoadUserAvatar(roomId).getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        }

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
        } else if (holder instanceof SearchResultMessageItemViewHolder) {
            ((SearchResultMessageItemViewHolder) holder).nameBrief.setBackground(color);
        } else if (holder instanceof SearchResultFileItemViewHolder) {
            ((SearchResultFileItemViewHolder) holder).nameProgressPanel.setBackground(color);
        }
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    private void enterRoom(String roomId, long firstMessageTimestamp) {
        //添加房间
        RoomsPanel.getContext().addRoomOrOpenRoom(roomId,"",0);
        //添加聊天房
         RoomChatPanel.getContext().createAndShow(roomId);

    }

    public void setSearchMessageOrFileListener(SearchMessageOrFileListener searchMessageOrFileListener) {
        this.searchMessageOrFileListener = searchMessageOrFileListener;
    }

    /**
     * 打开文件，如果文件不存在，则下载
     *
     * @param fileId
     * @param holder
     */
    public void downloadOrOpenFile(String fileId, SearchResultFileItemViewHolder holder) {
        FileAttachment fileAttachment = null;

        if (fileAttachment == null) {
            JOptionPane.showMessageDialog(null, "无效的附件", "附件无效", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String filepath = fileCache.tryGetFileCache(fileAttachment.getId(), fileAttachment.getTitle());
        if (filepath == null) {
            // 服务器上的文件
            if (fileAttachment.getLink().startsWith("/file-upload")) {
                // 如果当前文件正在下载，则不下载
                if (downloadingFiles.contains(fileId)) {
                    holder.progressBar.setVisible(true);
                    holder.size.setText("下载中...");
                } else {
                    downloadFile(fileAttachment);
                }
            }
            // 本地的文件
            else {
                openFileWithDefaultApplication(fileAttachment.getLink());
            }
        } else {
            openFileWithDefaultApplication(filepath);
        }
    }

    /**
     * 下载文件
     *
     * @param fileAttachment
     */
    private void downloadFile(FileAttachment fileAttachment) {
        downloadingFiles.add(fileAttachment.getId());
        //holder.fileId = fileAttachment.getId();

        final DownloadTask task = new DownloadTask(new HttpUtil.ProgressListener() {
            @Override
            public void onProgress(int progress) {
                SearchResultFileItemViewHolder holder = fileItemViewHolders.get(fileAttachment.getId());
                if (progress >= 0 && progress < 100) {

                    if (holder.size.isVisible()) {
                        holder.size.setVisible(false);
                    }
                    if (!holder.progressBar.isVisible()) {
                        holder.progressBar.setVisible(true);
                    }

                    holder.progressBar.setValue(progress);
                } else if (progress >= 100) {
                    holder.progressBar.setVisible(false);
                    holder.size.setVisible(true);
                }
            }
        });

        task.setListener(new HttpResponseListener<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                SearchResultFileItemViewHolder holder = fileItemViewHolders.get(fileAttachment.getId());

                String path = fileCache.cacheFile(fileAttachment.getId(), fileAttachment.getTitle(), data);

                if (path == null) {
                    holder.size.setVisible(true);
                    holder.size.setText("文件获取失败");
                    holder.progressBar.setVisible(false);
                } else {
                    holder.size.setVisible(true);
                    System.out.println("文件已缓存在 " + path);
                    holder.size.setText(fileCache.fileSizeString(path));
                    downloadingFiles.remove(fileAttachment.getId());

                    /*for (SearchResultFileItemViewHolder h : fileItemViewHolders)
                    {
                        if (h.fileId.equals(fileAttachment.getId()))
                        {
                            h.progressBar.setVisible(false);
                            h.size.setVisible(true);
                            h.size.setText(fileCache.fileSizeString(fileCache.tryGetFileCache(fileAttachment.getId(), fileAttachment.getTitle())));
                            //break;
                            //fileItemViewHolders.remove(h);
                        }
                    }*/

                    /*SearchResultFileItemViewHolder h = fileItemViewHolders.get(fileAttachment.getId());
                    h.progressBar.setVisible(false);
                    h.size.setVisible(true);
                    h.size.setText(fileCache.fileSizeString(fileCache.tryGetFileCache(fileAttachment.getId(), fileAttachment.getTitle())));*/
                }
            }

            @Override
            public void onFailed() {
                SearchResultFileItemViewHolder holder = fileItemViewHolders.get(fileAttachment.getId());
                holder.size.setVisible(true);
                holder.size.setText("文件获取失败");
                holder.progressBar.setVisible(false);
            }
        });

        //currentUser = currentUserService.findAll().get(0);
        //String url = Launcher.HOSTNAME + fileAttachment.getLink() + "?rc_uid=" + currentUser.getUserId() + "&rc_token=" + currentUser.getAuthToken();
        //task.execute(url);
    }

    /**
     * 使用默认程序打开文件
     *
     * @param path
     */
    private void openFileWithDefaultApplication(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "文件打开失败，没有找到关联的应用程序", "打开失败", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }
    }


    public interface SearchMessageOrFileListener {
        void onSearchMessage();

        void onSearchFile();
    }
}
