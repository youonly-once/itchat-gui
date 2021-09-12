package cn.shu.wechat.swing.constant;

/**
 * @author user
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/20/2021 16:56
 */
public enum SearchResultType {
    /**
     * 搜索结果类型
     */
    CONTACTS(0),
    ROOM(1),
    FILE(2),
    MESSAGE(3),
    SEARCH_MESSAGE(4),
    SEARCH_FILE(5);
    public final int CODE;
    SearchResultType(int type) {
       this.CODE = type;
    }
    public static SearchResultType getByCode(int code){
        for (SearchResultType value : SearchResultType.values()) {
            if (value.CODE == code){
                return value;
            }
        }
        throw new RuntimeException("Type错误");
    }
}
