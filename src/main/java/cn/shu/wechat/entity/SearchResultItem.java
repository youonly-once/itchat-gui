package cn.shu.wechat.entity;

import cn.shu.wechat.constant.SearchResultType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 搜索结果条目
 *
 * @author 舒新胜
 * @date 24/03/2017
 */
@Data
@NoArgsConstructor
@ToString
public class SearchResultItem implements Comparable<SearchResultItem> {
    /**
     * id
     */
    private String id;
    /**
     * 显示名称
     */
    private String name;
    /**
     * 类型
     */
    private int type;

    /**
     * 时间
     */
    private LocalDateTime dateTime;

    private String tag;

    private int score;

    public SearchResultItem(String id, String name, SearchResultType type) {
        this.id = id;
        this.name = name;
        this.type = type.CODE;
        this.tag = id;
    }

    public SearchResultItem(String id, String name, SearchResultType type, LocalDateTime dateTime, String tag) {
        this.id = id;
        this.name = name;
        this.type = type.CODE;
        this.tag = tag;
        this.dateTime = dateTime;
    }

    @Override
    public int compareTo(SearchResultItem o) {
        return 0;
    }

}
