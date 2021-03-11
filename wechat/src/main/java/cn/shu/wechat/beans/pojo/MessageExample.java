package cn.shu.wechat.beans.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
  *@作者     舒新胜
  *@项目     AutoWeChat
  *@创建时间  3/10/2021 10:36 PM
*/
public class MessageExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MessageExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andMsgIdIsNull() {
            addCriterion("msg_id is null");
            return (Criteria) this;
        }

        public Criteria andMsgIdIsNotNull() {
            addCriterion("msg_id is not null");
            return (Criteria) this;
        }

        public Criteria andMsgIdEqualTo(String value) {
            addCriterion("msg_id =", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotEqualTo(String value) {
            addCriterion("msg_id <>", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdGreaterThan(String value) {
            addCriterion("msg_id >", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdGreaterThanOrEqualTo(String value) {
            addCriterion("msg_id >=", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdLessThan(String value) {
            addCriterion("msg_id <", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdLessThanOrEqualTo(String value) {
            addCriterion("msg_id <=", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdLike(String value) {
            addCriterion("msg_id like", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotLike(String value) {
            addCriterion("msg_id not like", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdIn(List<String> values) {
            addCriterion("msg_id in", values, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotIn(List<String> values) {
            addCriterion("msg_id not in", values, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdBetween(String value1, String value2) {
            addCriterion("msg_id between", value1, value2, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotBetween(String value1, String value2) {
            addCriterion("msg_id not between", value1, value2, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIsNull() {
            addCriterion("msg_type is null");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIsNotNull() {
            addCriterion("msg_type is not null");
            return (Criteria) this;
        }

        public Criteria andMsgTypeEqualTo(Integer value) {
            addCriterion("msg_type =", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotEqualTo(Integer value) {
            addCriterion("msg_type <>", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeGreaterThan(Integer value) {
            addCriterion("msg_type >", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("msg_type >=", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLessThan(Integer value) {
            addCriterion("msg_type <", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLessThanOrEqualTo(Integer value) {
            addCriterion("msg_type <=", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIn(List<Integer> values) {
            addCriterion("msg_type in", values, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotIn(List<Integer> values) {
            addCriterion("msg_type not in", values, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeBetween(Integer value1, Integer value2) {
            addCriterion("msg_type between", value1, value2, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("msg_type not between", value1, value2, "msgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeIsNull() {
            addCriterion("app_msg_type is null");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeIsNotNull() {
            addCriterion("app_msg_type is not null");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeEqualTo(Integer value) {
            addCriterion("app_msg_type =", value, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeNotEqualTo(Integer value) {
            addCriterion("app_msg_type <>", value, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeGreaterThan(Integer value) {
            addCriterion("app_msg_type >", value, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("app_msg_type >=", value, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeLessThan(Integer value) {
            addCriterion("app_msg_type <", value, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeLessThanOrEqualTo(Integer value) {
            addCriterion("app_msg_type <=", value, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeIn(List<Integer> values) {
            addCriterion("app_msg_type in", values, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeNotIn(List<Integer> values) {
            addCriterion("app_msg_type not in", values, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeBetween(Integer value1, Integer value2) {
            addCriterion("app_msg_type between", value1, value2, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andAppMsgTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("app_msg_type not between", value1, value2, "appMsgType");
            return (Criteria) this;
        }

        public Criteria andMsgDescIsNull() {
            addCriterion("msg_desc is null");
            return (Criteria) this;
        }

        public Criteria andMsgDescIsNotNull() {
            addCriterion("msg_desc is not null");
            return (Criteria) this;
        }

        public Criteria andMsgDescEqualTo(String value) {
            addCriterion("msg_desc =", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescNotEqualTo(String value) {
            addCriterion("msg_desc <>", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescGreaterThan(String value) {
            addCriterion("msg_desc >", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescGreaterThanOrEqualTo(String value) {
            addCriterion("msg_desc >=", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescLessThan(String value) {
            addCriterion("msg_desc <", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescLessThanOrEqualTo(String value) {
            addCriterion("msg_desc <=", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescLike(String value) {
            addCriterion("msg_desc like", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescNotLike(String value) {
            addCriterion("msg_desc not like", value, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescIn(List<String> values) {
            addCriterion("msg_desc in", values, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescNotIn(List<String> values) {
            addCriterion("msg_desc not in", values, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescBetween(String value1, String value2) {
            addCriterion("msg_desc between", value1, value2, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andMsgDescNotBetween(String value1, String value2) {
            addCriterion("msg_desc not between", value1, value2, "msgDesc");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andContentIsNull() {
            addCriterion("content is null");
            return (Criteria) this;
        }

        public Criteria andContentIsNotNull() {
            addCriterion("content is not null");
            return (Criteria) this;
        }

        public Criteria andContentEqualTo(String value) {
            addCriterion("content =", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotEqualTo(String value) {
            addCriterion("content <>", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThan(String value) {
            addCriterion("content >", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThanOrEqualTo(String value) {
            addCriterion("content >=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThan(String value) {
            addCriterion("content <", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThanOrEqualTo(String value) {
            addCriterion("content <=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLike(String value) {
            addCriterion("content like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotLike(String value) {
            addCriterion("content not like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentIn(List<String> values) {
            addCriterion("content in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotIn(List<String> values) {
            addCriterion("content not in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentBetween(String value1, String value2) {
            addCriterion("content between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotBetween(String value1, String value2) {
            addCriterion("content not between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andFilePathIsNull() {
            addCriterion("file_path is null");
            return (Criteria) this;
        }

        public Criteria andFilePathIsNotNull() {
            addCriterion("file_path is not null");
            return (Criteria) this;
        }

        public Criteria andFilePathEqualTo(String value) {
            addCriterion("file_path =", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotEqualTo(String value) {
            addCriterion("file_path <>", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathGreaterThan(String value) {
            addCriterion("file_path >", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathGreaterThanOrEqualTo(String value) {
            addCriterion("file_path >=", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathLessThan(String value) {
            addCriterion("file_path <", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathLessThanOrEqualTo(String value) {
            addCriterion("file_path <=", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathLike(String value) {
            addCriterion("file_path like", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotLike(String value) {
            addCriterion("file_path not like", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathIn(List<String> values) {
            addCriterion("file_path in", values, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotIn(List<String> values) {
            addCriterion("file_path not in", values, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathBetween(String value1, String value2) {
            addCriterion("file_path between", value1, value2, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotBetween(String value1, String value2) {
            addCriterion("file_path not between", value1, value2, "filePath");
            return (Criteria) this;
        }

        public Criteria andMsgJsonIsNull() {
            addCriterion("msg_json is null");
            return (Criteria) this;
        }

        public Criteria andMsgJsonIsNotNull() {
            addCriterion("msg_json is not null");
            return (Criteria) this;
        }

        public Criteria andMsgJsonEqualTo(String value) {
            addCriterion("msg_json =", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonNotEqualTo(String value) {
            addCriterion("msg_json <>", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonGreaterThan(String value) {
            addCriterion("msg_json >", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonGreaterThanOrEqualTo(String value) {
            addCriterion("msg_json >=", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonLessThan(String value) {
            addCriterion("msg_json <", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonLessThanOrEqualTo(String value) {
            addCriterion("msg_json <=", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonLike(String value) {
            addCriterion("msg_json like", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonNotLike(String value) {
            addCriterion("msg_json not like", value, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonIn(List<String> values) {
            addCriterion("msg_json in", values, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonNotIn(List<String> values) {
            addCriterion("msg_json not in", values, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonBetween(String value1, String value2) {
            addCriterion("msg_json between", value1, value2, "msgJson");
            return (Criteria) this;
        }

        public Criteria andMsgJsonNotBetween(String value1, String value2) {
            addCriterion("msg_json not between", value1, value2, "msgJson");
            return (Criteria) this;
        }

        public Criteria andFromUsernameIsNull() {
            addCriterion("from_username is null");
            return (Criteria) this;
        }

        public Criteria andFromUsernameIsNotNull() {
            addCriterion("from_username is not null");
            return (Criteria) this;
        }

        public Criteria andFromUsernameEqualTo(String value) {
            addCriterion("from_username =", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameNotEqualTo(String value) {
            addCriterion("from_username <>", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameGreaterThan(String value) {
            addCriterion("from_username >", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("from_username >=", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameLessThan(String value) {
            addCriterion("from_username <", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameLessThanOrEqualTo(String value) {
            addCriterion("from_username <=", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameLike(String value) {
            addCriterion("from_username like", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameNotLike(String value) {
            addCriterion("from_username not like", value, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameIn(List<String> values) {
            addCriterion("from_username in", values, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameNotIn(List<String> values) {
            addCriterion("from_username not in", values, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameBetween(String value1, String value2) {
            addCriterion("from_username between", value1, value2, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromUsernameNotBetween(String value1, String value2) {
            addCriterion("from_username not between", value1, value2, "fromUsername");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameIsNull() {
            addCriterion("from_remarkname is null");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameIsNotNull() {
            addCriterion("from_remarkname is not null");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameEqualTo(String value) {
            addCriterion("from_remarkname =", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameNotEqualTo(String value) {
            addCriterion("from_remarkname <>", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameGreaterThan(String value) {
            addCriterion("from_remarkname >", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameGreaterThanOrEqualTo(String value) {
            addCriterion("from_remarkname >=", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameLessThan(String value) {
            addCriterion("from_remarkname <", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameLessThanOrEqualTo(String value) {
            addCriterion("from_remarkname <=", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameLike(String value) {
            addCriterion("from_remarkname like", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameNotLike(String value) {
            addCriterion("from_remarkname not like", value, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameIn(List<String> values) {
            addCriterion("from_remarkname in", values, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameNotIn(List<String> values) {
            addCriterion("from_remarkname not in", values, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameBetween(String value1, String value2) {
            addCriterion("from_remarkname between", value1, value2, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromRemarknameNotBetween(String value1, String value2) {
            addCriterion("from_remarkname not between", value1, value2, "fromRemarkname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameIsNull() {
            addCriterion("from_nickname is null");
            return (Criteria) this;
        }

        public Criteria andFromNicknameIsNotNull() {
            addCriterion("from_nickname is not null");
            return (Criteria) this;
        }

        public Criteria andFromNicknameEqualTo(String value) {
            addCriterion("from_nickname =", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameNotEqualTo(String value) {
            addCriterion("from_nickname <>", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameGreaterThan(String value) {
            addCriterion("from_nickname >", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameGreaterThanOrEqualTo(String value) {
            addCriterion("from_nickname >=", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameLessThan(String value) {
            addCriterion("from_nickname <", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameLessThanOrEqualTo(String value) {
            addCriterion("from_nickname <=", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameLike(String value) {
            addCriterion("from_nickname like", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameNotLike(String value) {
            addCriterion("from_nickname not like", value, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameIn(List<String> values) {
            addCriterion("from_nickname in", values, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameNotIn(List<String> values) {
            addCriterion("from_nickname not in", values, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameBetween(String value1, String value2) {
            addCriterion("from_nickname between", value1, value2, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromNicknameNotBetween(String value1, String value2) {
            addCriterion("from_nickname not between", value1, value2, "fromNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameIsNull() {
            addCriterion("from_member_of_group_username is null");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameIsNotNull() {
            addCriterion("from_member_of_group_username is not null");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameEqualTo(String value) {
            addCriterion("from_member_of_group_username =", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameNotEqualTo(String value) {
            addCriterion("from_member_of_group_username <>", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameGreaterThan(String value) {
            addCriterion("from_member_of_group_username >", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("from_member_of_group_username >=", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameLessThan(String value) {
            addCriterion("from_member_of_group_username <", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameLessThanOrEqualTo(String value) {
            addCriterion("from_member_of_group_username <=", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameLike(String value) {
            addCriterion("from_member_of_group_username like", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameNotLike(String value) {
            addCriterion("from_member_of_group_username not like", value, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameIn(List<String> values) {
            addCriterion("from_member_of_group_username in", values, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameNotIn(List<String> values) {
            addCriterion("from_member_of_group_username not in", values, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameBetween(String value1, String value2) {
            addCriterion("from_member_of_group_username between", value1, value2, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupUsernameNotBetween(String value1, String value2) {
            addCriterion("from_member_of_group_username not between", value1, value2, "fromMemberOfGroupUsername");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameIsNull() {
            addCriterion("from_member_of_group_nickname is null");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameIsNotNull() {
            addCriterion("from_member_of_group_nickname is not null");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameEqualTo(String value) {
            addCriterion("from_member_of_group_nickname =", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameNotEqualTo(String value) {
            addCriterion("from_member_of_group_nickname <>", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameGreaterThan(String value) {
            addCriterion("from_member_of_group_nickname >", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameGreaterThanOrEqualTo(String value) {
            addCriterion("from_member_of_group_nickname >=", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameLessThan(String value) {
            addCriterion("from_member_of_group_nickname <", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameLessThanOrEqualTo(String value) {
            addCriterion("from_member_of_group_nickname <=", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameLike(String value) {
            addCriterion("from_member_of_group_nickname like", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameNotLike(String value) {
            addCriterion("from_member_of_group_nickname not like", value, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameIn(List<String> values) {
            addCriterion("from_member_of_group_nickname in", values, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameNotIn(List<String> values) {
            addCriterion("from_member_of_group_nickname not in", values, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameBetween(String value1, String value2) {
            addCriterion("from_member_of_group_nickname between", value1, value2, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupNicknameNotBetween(String value1, String value2) {
            addCriterion("from_member_of_group_nickname not between", value1, value2, "fromMemberOfGroupNickname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameIsNull() {
            addCriterion("from_member_of_group_displayname is null");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameIsNotNull() {
            addCriterion("from_member_of_group_displayname is not null");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameEqualTo(String value) {
            addCriterion("from_member_of_group_displayname =", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameNotEqualTo(String value) {
            addCriterion("from_member_of_group_displayname <>", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameGreaterThan(String value) {
            addCriterion("from_member_of_group_displayname >", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameGreaterThanOrEqualTo(String value) {
            addCriterion("from_member_of_group_displayname >=", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameLessThan(String value) {
            addCriterion("from_member_of_group_displayname <", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameLessThanOrEqualTo(String value) {
            addCriterion("from_member_of_group_displayname <=", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameLike(String value) {
            addCriterion("from_member_of_group_displayname like", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameNotLike(String value) {
            addCriterion("from_member_of_group_displayname not like", value, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameIn(List<String> values) {
            addCriterion("from_member_of_group_displayname in", values, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameNotIn(List<String> values) {
            addCriterion("from_member_of_group_displayname not in", values, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameBetween(String value1, String value2) {
            addCriterion("from_member_of_group_displayname between", value1, value2, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andFromMemberOfGroupDisplaynameNotBetween(String value1, String value2) {
            addCriterion("from_member_of_group_displayname not between", value1, value2, "fromMemberOfGroupDisplayname");
            return (Criteria) this;
        }

        public Criteria andToUsernameIsNull() {
            addCriterion("to_username is null");
            return (Criteria) this;
        }

        public Criteria andToUsernameIsNotNull() {
            addCriterion("to_username is not null");
            return (Criteria) this;
        }

        public Criteria andToUsernameEqualTo(String value) {
            addCriterion("to_username =", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameNotEqualTo(String value) {
            addCriterion("to_username <>", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameGreaterThan(String value) {
            addCriterion("to_username >", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("to_username >=", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameLessThan(String value) {
            addCriterion("to_username <", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameLessThanOrEqualTo(String value) {
            addCriterion("to_username <=", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameLike(String value) {
            addCriterion("to_username like", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameNotLike(String value) {
            addCriterion("to_username not like", value, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameIn(List<String> values) {
            addCriterion("to_username in", values, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameNotIn(List<String> values) {
            addCriterion("to_username not in", values, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameBetween(String value1, String value2) {
            addCriterion("to_username between", value1, value2, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToUsernameNotBetween(String value1, String value2) {
            addCriterion("to_username not between", value1, value2, "toUsername");
            return (Criteria) this;
        }

        public Criteria andToRemarknameIsNull() {
            addCriterion("to_remarkname is null");
            return (Criteria) this;
        }

        public Criteria andToRemarknameIsNotNull() {
            addCriterion("to_remarkname is not null");
            return (Criteria) this;
        }

        public Criteria andToRemarknameEqualTo(String value) {
            addCriterion("to_remarkname =", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameNotEqualTo(String value) {
            addCriterion("to_remarkname <>", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameGreaterThan(String value) {
            addCriterion("to_remarkname >", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameGreaterThanOrEqualTo(String value) {
            addCriterion("to_remarkname >=", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameLessThan(String value) {
            addCriterion("to_remarkname <", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameLessThanOrEqualTo(String value) {
            addCriterion("to_remarkname <=", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameLike(String value) {
            addCriterion("to_remarkname like", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameNotLike(String value) {
            addCriterion("to_remarkname not like", value, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameIn(List<String> values) {
            addCriterion("to_remarkname in", values, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameNotIn(List<String> values) {
            addCriterion("to_remarkname not in", values, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameBetween(String value1, String value2) {
            addCriterion("to_remarkname between", value1, value2, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToRemarknameNotBetween(String value1, String value2) {
            addCriterion("to_remarkname not between", value1, value2, "toRemarkname");
            return (Criteria) this;
        }

        public Criteria andToNicknameIsNull() {
            addCriterion("to_nickname is null");
            return (Criteria) this;
        }

        public Criteria andToNicknameIsNotNull() {
            addCriterion("to_nickname is not null");
            return (Criteria) this;
        }

        public Criteria andToNicknameEqualTo(String value) {
            addCriterion("to_nickname =", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameNotEqualTo(String value) {
            addCriterion("to_nickname <>", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameGreaterThan(String value) {
            addCriterion("to_nickname >", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameGreaterThanOrEqualTo(String value) {
            addCriterion("to_nickname >=", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameLessThan(String value) {
            addCriterion("to_nickname <", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameLessThanOrEqualTo(String value) {
            addCriterion("to_nickname <=", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameLike(String value) {
            addCriterion("to_nickname like", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameNotLike(String value) {
            addCriterion("to_nickname not like", value, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameIn(List<String> values) {
            addCriterion("to_nickname in", values, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameNotIn(List<String> values) {
            addCriterion("to_nickname not in", values, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameBetween(String value1, String value2) {
            addCriterion("to_nickname between", value1, value2, "toNickname");
            return (Criteria) this;
        }

        public Criteria andToNicknameNotBetween(String value1, String value2) {
            addCriterion("to_nickname not between", value1, value2, "toNickname");
            return (Criteria) this;
        }

        public Criteria andIsSendIsNull() {
            addCriterion("is_send is null");
            return (Criteria) this;
        }

        public Criteria andIsSendIsNotNull() {
            addCriterion("is_send is not null");
            return (Criteria) this;
        }

        public Criteria andIsSendEqualTo(Boolean value) {
            addCriterion("is_send =", value, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendNotEqualTo(Boolean value) {
            addCriterion("is_send <>", value, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendGreaterThan(Boolean value) {
            addCriterion("is_send >", value, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_send >=", value, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendLessThan(Boolean value) {
            addCriterion("is_send <", value, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendLessThanOrEqualTo(Boolean value) {
            addCriterion("is_send <=", value, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendIn(List<Boolean> values) {
            addCriterion("is_send in", values, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendNotIn(List<Boolean> values) {
            addCriterion("is_send not in", values, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendBetween(Boolean value1, Boolean value2) {
            addCriterion("is_send between", value1, value2, "isSend");
            return (Criteria) this;
        }

        public Criteria andIsSendNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_send not between", value1, value2, "isSend");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}