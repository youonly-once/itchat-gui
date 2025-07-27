package cn.shu.wechat.swing.components;

import cn.shu.wechat.swing.adapter.BaseAdapter;
import cn.shu.wechat.swing.adapter.HeaderViewHolder;
import cn.shu.wechat.swing.adapter.ViewHolder;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class RCListView<T extends ViewHolder, M extends BaseAdapter<T>> extends JScrollPane {
    private M adapter;
    @Getter
    private JPanel contentPanel;
    private int vGap;
    private int hGap;
    private List<Rectangle> rectangleList = new ArrayList<>();
    boolean scrollToBottom = false;
    private AdjustmentListener adjustmentListener;
    private MouseAdapter mouseAdapter;
    private ScrollUI scrollUI;

    // 监听滚动到顶部事件
    @Setter
    private ScrollToTopListener scrollToTopListener;
    // 滚动事件
    @Setter
    private ScrollListener scrollListener;
    private boolean scrollBarPressed = false;
    private int lastScrollValue = -1;

    private static int lastItemCount = 0;
    private MouseAdapter scrollMouseListener;
    private boolean scrollAttachMouseListener = false;
    private boolean messageLoading = false;
    private long lastWeelTime = 0;

    public RCListView() {
        this(0, 0);
    }

    public RCListView(int hGap, int vGap) {
        this.vGap = vGap;
        this.hGap = hGap;

        initComponents();
        setListeners();
    }

    public void setScrollHiddenOnMouseLeave(JComponent component) {
        if (scrollMouseListener == null) {

            scrollMouseListener = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setScrollBarColor(Colors.SCROLL_BAR_THUMB, Colors.WINDOW_BACKGROUND);
                    getVerticalScrollBar().repaint();

                    super.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setScrollBarColor(Colors.WINDOW_BACKGROUND, Colors.WINDOW_BACKGROUND);
                    getVerticalScrollBar().repaint();

                    super.mouseExited(e);
                }
            };
        }

        if (!scrollAttachMouseListener) {
            getVerticalScrollBar().addMouseListener(scrollMouseListener);
            scrollAttachMouseListener = true;
        }

        component.addMouseListener(scrollMouseListener);
    }

    /**
     * 设置滚动条的颜色，此方法必须在setAdapter()方法之前执行
     *
     * @param thumbColor
     * @param trackColor
     */
    public void setScrollBarColor(Color thumbColor, Color trackColor) {
        if (scrollUI == null) {
            scrollUI = new ScrollUI(thumbColor, trackColor);
            this.getVerticalScrollBar().setUI(scrollUI);
        } else {
            scrollUI.setThumbColor(thumbColor);
            scrollUI.setTrackColor(trackColor);
        }
        //this.getVerticalScrollBar().setUI(new ScrollUI(thumbColor, trackColor));
    }

    private void initComponents() {
        contentPanel = new JPanel();
        //用VerticalFlowLayout有问题，显示的房间数和实际不符
        contentPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, hGap, vGap, true, false));
        //contentPanel.setLayout(new GridLayout(0,1,hGap,vGap));
        contentPanel.setBackground(Colors.WINDOW_BACKGROUND);

        this.setViewportView(contentPanel);
        this.setBorder(null);
        this.getVerticalScrollBar().setUnitIncrement(25);
        this.getVerticalScrollBar().setUI(new ScrollUI());
    }

    private void setListeners() {
        adjustmentListener = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                // 之所以要加上!scrollBarPressed这个条件，scrollBar在顶部的时间，scrollbar点击和释放都分别会触发adjustmentValueChanged这个事件
                // 所以只让scrollBar释放的时候触发这个回调
                // !scrollToBottom 这个条件保证在自动滚动到底部之前，不会调用此回调
                if (evt.getValue() == 0 && evt.getValue() != lastScrollValue && scrollToTopListener != null && !scrollBarPressed && !scrollToBottom) {
                    messageLoading = true;
                    scrollToTopListener.onScrollToTop();
                }
                if (evt.getAdjustmentType() == AdjustmentEvent.TRACK && scrollToBottom) {
                    getVerticalScrollBar().setValue(getVerticalScrollBar().getModel().getMaximum()
                            - getVerticalScrollBar().getModel().getExtent());
                }
                if (scrollListener != null){
                    scrollListener.onScroll(evt.getValue()
                            ,evt.getAdjustable().getMaximum());
                }
                lastScrollValue = evt.getValue();

            }
        };

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                scrollToBottom = false;
                scrollBarPressed = true;
                super.mouseEntered(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                scrollBarPressed = false;
                super.mouseReleased(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // 如果两次鼠标滚轮间隔小于1秒，则忽略
                if (System.currentTimeMillis() - lastWeelTime < 1000) {
                    lastWeelTime = System.currentTimeMillis();
                    return;
                }

                if (getVerticalScrollBar().getValue() == 0) {
                    if (messageLoading) {
                        messageLoading = false;
                    } else {
                        System.out.println("鼠标滚轮到顶，自动加载");
                        if (scrollToTopListener != null) {
                            scrollToTopListener.onScrollToTop();
                        }
                    }

                }

                scrollToBottom = false;

                lastWeelTime = System.currentTimeMillis();

                super.mouseWheelMoved(e);
            }
        };

        getVerticalScrollBar().addAdjustmentListener(adjustmentListener);
        getVerticalScrollBar().addMouseListener(mouseAdapter);
        addMouseListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    public void setAutoScrollToBottom() {
        scrollToBottom = true;
    }

    public void setAutoScrollToTop() {
        scrollToBottom = false;
        getVerticalScrollBar().setValue(1);
    }

    public void fillComponents() {
        if (adapter == null) {
            return;
        }

        lastItemCount = adapter.getCount();
        for (int i = 0; i < adapter.getCount(); i++) {
            int viewType = adapter.getItemViewType(i);
            //联系人列表的 Header
            HeaderViewHolder headerViewHolder = adapter.onCreateHeaderViewHolder(viewType, i);
            if (headerViewHolder != null) {
                adapter.onBindHeaderViewHolder(headerViewHolder, i);
                contentPanel.add(headerViewHolder);
                rectangleList.add(headerViewHolder.getBounds());
            }
            T holder = adapter.onCreateViewHolder(viewType, 0, i);
            adapter.onBindViewHolder(holder, i);
            contentPanel.add(holder);
        }

    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(M adapter) {
        this.adapter = adapter;

        fillComponents();
        //scrollToPosition(0);
    }

    public void setContentPanelBackground(Color color) {
        contentPanel.setOpaque(true);
        contentPanel.setBackground(color);
    }

    public void scrollToPosition(int value) {
        getVerticalScrollBar().setValue(value);
    }

    /**
     * 获取滚动条在底部时显示的条目数
     */
    private int getLastVisibleItemCount() {
        int height = getHeight();

        int elemHeight = 0;
        int count = 0;
        for (int i = contentPanel.getComponentCount() - 1; i >= 0; i--) {
            count++;
            int h = contentPanel.getComponent(i).getHeight() + 20;
            elemHeight += h;

            if (elemHeight >= height) {
                break;
            }
        }

        return count;
    }


    /**
     * 重绘整个listView
     */
    public void notifyDataSetChanged(boolean keepSize) {
        if (keepSize) {
            if (lastItemCount == adapter.getCount()) {
                // 保持原来内容面板的宽高，避免滚动条长度改变或可见状态改变时闪屏
                contentPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), contentPanel.getHeight()));
            }
        }

        contentPanel.removeAll();
        contentPanel.repaint();
        fillComponents();
        contentPanel.revalidate();

    }

    public void removeComponent(int pos) {
        contentPanel.remove(pos);
        contentPanel.revalidate();
    }
    /**
     * 重绘指定区间内的元素
     *
     * @param startPosition
     * @param count
     */
    public void notifyItemRangeInsertedHead(int startPosition, int count) {
        JViewport viewport = this.getViewport();

// 1. 记录当前视口位置（以像素为单位）
        Point viewPosBefore = viewport.getViewPosition();
        int heightBefore = viewport.getViewSize().height;
        //从下往上
        for (int i = count - 1; i >= startPosition; i--) {
            int viewType = adapter.getItemViewType(i);
            int subViewType = adapter.getItemSubViewType(i);
            T holder = adapter.onCreateViewHolder(viewType, subViewType, i);

            adapter.onBindViewHolder(holder, i);
            contentPanel.add(holder, startPosition);
        }

        contentPanel.revalidate();
        SwingUtilities.invokeLater(() -> {
            int heightAfter = viewport.getPreferredSize().height;
            int delta = heightAfter - heightBefore;
            viewport.setViewPosition(new Point(viewPosBefore.x, viewPosBefore.y + delta));
        });
    }

    /**
     * 从元素集合start出开始取count个追加到UI末尾
     * @param start 元素开始位置
     * @param count 追加数量
     */
    public void notifyItemAppend(int start,int count){
        for (int i = start; i <start+ count; i++)
        {
            Map positionMap = adapter.getPositionMap();
            if (positionMap!=null && positionMap.containsKey(i)){
                //添加标签
                HeaderViewHolder holder = adapter.onCreateHeaderViewHolder(0,i);
                adapter.onBindHeaderViewHolder((HeaderViewHolder) holder, i);
                contentPanel.add(holder, -1);
            }

            int viewType = adapter.getItemViewType(i);
            T holder = adapter.onCreateViewHolder(viewType, 0, i);
            adapter.onBindViewHolder(holder, i);
            contentPanel.add(holder, -1);



        }
    }

    /**
     * 重绘指定位置的元素
     *
     * @param position 元素位置  不包括map的位置
     */
    public void notifyItemChanged(int position) {
        //contentPanel.remove(position);
        //int viewType = adapter.getItemViewType(position);
        //ViewHolder holder = adapter.onCreateViewHolder(viewType);
        //加入 0位置有个map，0对应也有一个元素，此时元素在view中的位置应是1
        Map<Integer, String> positionMap = adapter.getPositionMap();
        int i = 0;
        if (positionMap != null) {
            for (Integer integer : positionMap.keySet()) {
                if (integer <= position) {
                    i++;
                }
            }
        }

        T holder = (T) getItem(position + i);
        if (holder instanceof HeaderViewHolder) {
            adapter.onBindHeaderViewHolder((HeaderViewHolder) holder, position);
        } else {
            //元素pos
            adapter.onBindViewHolder(holder, position);
        }
        holder.repaint();
    }

    /**
     * 重绘指定位置的元素
     *
     * @param position 元素位置  不包括map的位置
     */
    public void notifyItemChanged(T viewHolder, int position) {

        Map<Integer, String> positionMap = adapter.getPositionMap();
        int i = 0;
        if (positionMap != null) {
            for (Integer integer : positionMap.keySet()) {
                if (integer <= position) {
                    i++;
                }
            }
        }
        if (viewHolder instanceof HeaderViewHolder) {
            adapter.onBindHeaderViewHolder((HeaderViewHolder) viewHolder, position);
        } else {
            //元素pos
            adapter.onBindViewHolder(viewHolder, position);
        }
        viewHolder.repaint();
    }

    public Component getItem(int n) {
        return contentPanel.getComponent(n);
    }


    public T notifyItemInserted(int position, boolean end) {
        int viewType = adapter.getItemViewType(position);
        int subViewType = adapter.getItemSubViewType(position);
        T holder = adapter.onCreateViewHolder(viewType, subViewType, position);

        adapter.onBindViewHolder(holder, position);

        position = end ? -1 : position;
        //如果之前的数据添加失败 数据项和panel组件数量对不上，这时添加到末尾
        position = Math.min(contentPanel.getComponentCount(), position);

        JViewport viewport = this.getViewport();

// 1. 记录当前视口位置（以像素为单位）
        Point viewPosBefore = viewport.getViewPosition();
        int heightBefore = viewport.getViewSize().height;

        contentPanel.add(holder, position);
        contentPanel.revalidate();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int heightAfter = viewport.getPreferredSize().height;
                int delta = heightAfter - heightBefore;
                viewport.setViewPosition(new Point(viewPosBefore.x, viewPosBefore.y + delta));
            }
        });

        return holder;
    }

    public void notifyItemRemoved(int position) {
        contentPanel.remove(position);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * 获取列表中所有的ViewHolder项目，不包括HeaderViewHolder
     *
     * @return
     */
    public List<T> getItems() {
        Component[] components = contentPanel.getComponents();
        List<T> viewHolders = new ArrayList<>();
        for (Component com : components) {
            if (!(com instanceof HeaderViewHolder)) {
                viewHolders.add((T) com);
            }
        }

        return viewHolders;
    }

    public interface ScrollToTopListener {
        void onScrollToTop();
    }
    public interface ScrollListener {
        void onScroll(int curr,int max);
    }

    /**
     * 移除
     * @param pos 位置
     */
    public void removeItem(int pos){
        contentPanel.remove(pos);
    }
}
