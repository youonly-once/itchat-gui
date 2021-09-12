package cn.shu.wechat.pojo.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 6:42 PM
 */
public class MemberExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MemberExample() {
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

        public Criteria andUsernameIsNull() {
            addCriterion("UserName is null");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNotNull() {
            addCriterion("UserName is not null");
            return (Criteria) this;
        }

        public Criteria andUsernameEqualTo(String value) {
            addCriterion("UserName =", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotEqualTo(String value) {
            addCriterion("UserName <>", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThan(String value) {
            addCriterion("UserName >", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("UserName >=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThan(String value) {
            addCriterion("UserName <", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThanOrEqualTo(String value) {
            addCriterion("UserName <=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLike(String value) {
            addCriterion("UserName like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotLike(String value) {
            addCriterion("UserName not like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameIn(List<String> values) {
            addCriterion("UserName in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotIn(List<String> values) {
            addCriterion("UserName not in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameBetween(String value1, String value2) {
            addCriterion("UserName between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotBetween(String value1, String value2) {
            addCriterion("UserName not between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andChatroomidIsNull() {
            addCriterion("ChatRoomId is null");
            return (Criteria) this;
        }

        public Criteria andChatroomidIsNotNull() {
            addCriterion("ChatRoomId is not null");
            return (Criteria) this;
        }

        public Criteria andChatroomidEqualTo(Double value) {
            addCriterion("ChatRoomId =", value, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidNotEqualTo(Double value) {
            addCriterion("ChatRoomId <>", value, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidGreaterThan(Double value) {
            addCriterion("ChatRoomId >", value, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidGreaterThanOrEqualTo(Double value) {
            addCriterion("ChatRoomId >=", value, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidLessThan(Double value) {
            addCriterion("ChatRoomId <", value, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidLessThanOrEqualTo(Double value) {
            addCriterion("ChatRoomId <=", value, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidIn(List<Double> values) {
            addCriterion("ChatRoomId in", values, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidNotIn(List<Double> values) {
            addCriterion("ChatRoomId not in", values, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidBetween(Double value1, Double value2) {
            addCriterion("ChatRoomId between", value1, value2, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andChatroomidNotBetween(Double value1, Double value2) {
            addCriterion("ChatRoomId not between", value1, value2, "chatroomid");
            return (Criteria) this;
        }

        public Criteria andSexIsNull() {
            addCriterion("Sex is null");
            return (Criteria) this;
        }

        public Criteria andSexIsNotNull() {
            addCriterion("Sex is not null");
            return (Criteria) this;
        }

        public Criteria andSexEqualTo(Double value) {
            addCriterion("Sex =", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotEqualTo(Double value) {
            addCriterion("Sex <>", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexGreaterThan(Double value) {
            addCriterion("Sex >", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexGreaterThanOrEqualTo(Double value) {
            addCriterion("Sex >=", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexLessThan(Double value) {
            addCriterion("Sex <", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexLessThanOrEqualTo(Double value) {
            addCriterion("Sex <=", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexIn(List<Double> values) {
            addCriterion("Sex in", values, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotIn(List<Double> values) {
            addCriterion("Sex not in", values, "sex");
            return (Criteria) this;
        }

        public Criteria andSexBetween(Double value1, Double value2) {
            addCriterion("Sex between", value1, value2, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotBetween(Double value1, Double value2) {
            addCriterion("Sex not between", value1, value2, "sex");
            return (Criteria) this;
        }

        public Criteria andAttrstatusIsNull() {
            addCriterion("AttrStatus is null");
            return (Criteria) this;
        }

        public Criteria andAttrstatusIsNotNull() {
            addCriterion("AttrStatus is not null");
            return (Criteria) this;
        }

        public Criteria andAttrstatusEqualTo(Double value) {
            addCriterion("AttrStatus =", value, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusNotEqualTo(Double value) {
            addCriterion("AttrStatus <>", value, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusGreaterThan(Double value) {
            addCriterion("AttrStatus >", value, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusGreaterThanOrEqualTo(Double value) {
            addCriterion("AttrStatus >=", value, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusLessThan(Double value) {
            addCriterion("AttrStatus <", value, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusLessThanOrEqualTo(Double value) {
            addCriterion("AttrStatus <=", value, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusIn(List<Double> values) {
            addCriterion("AttrStatus in", values, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusNotIn(List<Double> values) {
            addCriterion("AttrStatus not in", values, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusBetween(Double value1, Double value2) {
            addCriterion("AttrStatus between", value1, value2, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andAttrstatusNotBetween(Double value1, Double value2) {
            addCriterion("AttrStatus not between", value1, value2, "attrstatus");
            return (Criteria) this;
        }

        public Criteria andStatuesIsNull() {
            addCriterion("Statues is null");
            return (Criteria) this;
        }

        public Criteria andStatuesIsNotNull() {
            addCriterion("Statues is not null");
            return (Criteria) this;
        }

        public Criteria andStatuesEqualTo(Double value) {
            addCriterion("Statues =", value, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesNotEqualTo(Double value) {
            addCriterion("Statues <>", value, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesGreaterThan(Double value) {
            addCriterion("Statues >", value, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesGreaterThanOrEqualTo(Double value) {
            addCriterion("Statues >=", value, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesLessThan(Double value) {
            addCriterion("Statues <", value, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesLessThanOrEqualTo(Double value) {
            addCriterion("Statues <=", value, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesIn(List<Double> values) {
            addCriterion("Statues in", values, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesNotIn(List<Double> values) {
            addCriterion("Statues not in", values, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesBetween(Double value1, Double value2) {
            addCriterion("Statues between", value1, value2, "statues");
            return (Criteria) this;
        }

        public Criteria andStatuesNotBetween(Double value1, Double value2) {
            addCriterion("Statues not between", value1, value2, "statues");
            return (Criteria) this;
        }

        public Criteria andPyquanpinIsNull() {
            addCriterion("PYQuanPin is null");
            return (Criteria) this;
        }

        public Criteria andPyquanpinIsNotNull() {
            addCriterion("PYQuanPin is not null");
            return (Criteria) this;
        }

        public Criteria andPyquanpinEqualTo(String value) {
            addCriterion("PYQuanPin =", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinNotEqualTo(String value) {
            addCriterion("PYQuanPin <>", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinGreaterThan(String value) {
            addCriterion("PYQuanPin >", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinGreaterThanOrEqualTo(String value) {
            addCriterion("PYQuanPin >=", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinLessThan(String value) {
            addCriterion("PYQuanPin <", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinLessThanOrEqualTo(String value) {
            addCriterion("PYQuanPin <=", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinLike(String value) {
            addCriterion("PYQuanPin like", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinNotLike(String value) {
            addCriterion("PYQuanPin not like", value, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinIn(List<String> values) {
            addCriterion("PYQuanPin in", values, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinNotIn(List<String> values) {
            addCriterion("PYQuanPin not in", values, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinBetween(String value1, String value2) {
            addCriterion("PYQuanPin between", value1, value2, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andPyquanpinNotBetween(String value1, String value2) {
            addCriterion("PYQuanPin not between", value1, value2, "pyquanpin");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidIsNull() {
            addCriterion("EncryChatRoomId is null");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidIsNotNull() {
            addCriterion("EncryChatRoomId is not null");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidEqualTo(String value) {
            addCriterion("EncryChatRoomId =", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidNotEqualTo(String value) {
            addCriterion("EncryChatRoomId <>", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidGreaterThan(String value) {
            addCriterion("EncryChatRoomId >", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidGreaterThanOrEqualTo(String value) {
            addCriterion("EncryChatRoomId >=", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidLessThan(String value) {
            addCriterion("EncryChatRoomId <", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidLessThanOrEqualTo(String value) {
            addCriterion("EncryChatRoomId <=", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidLike(String value) {
            addCriterion("EncryChatRoomId like", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidNotLike(String value) {
            addCriterion("EncryChatRoomId not like", value, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidIn(List<String> values) {
            addCriterion("EncryChatRoomId in", values, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidNotIn(List<String> values) {
            addCriterion("EncryChatRoomId not in", values, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidBetween(String value1, String value2) {
            addCriterion("EncryChatRoomId between", value1, value2, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andEncrychatroomidNotBetween(String value1, String value2) {
            addCriterion("EncryChatRoomId not between", value1, value2, "encrychatroomid");
            return (Criteria) this;
        }

        public Criteria andDisplaynameIsNull() {
            addCriterion("DisplayName is null");
            return (Criteria) this;
        }

        public Criteria andDisplaynameIsNotNull() {
            addCriterion("DisplayName is not null");
            return (Criteria) this;
        }

        public Criteria andDisplaynameEqualTo(String value) {
            addCriterion("DisplayName =", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameNotEqualTo(String value) {
            addCriterion("DisplayName <>", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameGreaterThan(String value) {
            addCriterion("DisplayName >", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameGreaterThanOrEqualTo(String value) {
            addCriterion("DisplayName >=", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameLessThan(String value) {
            addCriterion("DisplayName <", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameLessThanOrEqualTo(String value) {
            addCriterion("DisplayName <=", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameLike(String value) {
            addCriterion("DisplayName like", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameNotLike(String value) {
            addCriterion("DisplayName not like", value, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameIn(List<String> values) {
            addCriterion("DisplayName in", values, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameNotIn(List<String> values) {
            addCriterion("DisplayName not in", values, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameBetween(String value1, String value2) {
            addCriterion("DisplayName between", value1, value2, "displayname");
            return (Criteria) this;
        }

        public Criteria andDisplaynameNotBetween(String value1, String value2) {
            addCriterion("DisplayName not between", value1, value2, "displayname");
            return (Criteria) this;
        }

        public Criteria andVerifyflagIsNull() {
            addCriterion("VerifyFlag is null");
            return (Criteria) this;
        }

        public Criteria andVerifyflagIsNotNull() {
            addCriterion("VerifyFlag is not null");
            return (Criteria) this;
        }

        public Criteria andVerifyflagEqualTo(Double value) {
            addCriterion("VerifyFlag =", value, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagNotEqualTo(Double value) {
            addCriterion("VerifyFlag <>", value, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagGreaterThan(Double value) {
            addCriterion("VerifyFlag >", value, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagGreaterThanOrEqualTo(Double value) {
            addCriterion("VerifyFlag >=", value, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagLessThan(Double value) {
            addCriterion("VerifyFlag <", value, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagLessThanOrEqualTo(Double value) {
            addCriterion("VerifyFlag <=", value, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagIn(List<Double> values) {
            addCriterion("VerifyFlag in", values, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagNotIn(List<Double> values) {
            addCriterion("VerifyFlag not in", values, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagBetween(Double value1, Double value2) {
            addCriterion("VerifyFlag between", value1, value2, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andVerifyflagNotBetween(Double value1, Double value2) {
            addCriterion("VerifyFlag not between", value1, value2, "verifyflag");
            return (Criteria) this;
        }

        public Criteria andUnifriendIsNull() {
            addCriterion("UniFriend is null");
            return (Criteria) this;
        }

        public Criteria andUnifriendIsNotNull() {
            addCriterion("UniFriend is not null");
            return (Criteria) this;
        }

        public Criteria andUnifriendEqualTo(Double value) {
            addCriterion("UniFriend =", value, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendNotEqualTo(Double value) {
            addCriterion("UniFriend <>", value, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendGreaterThan(Double value) {
            addCriterion("UniFriend >", value, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendGreaterThanOrEqualTo(Double value) {
            addCriterion("UniFriend >=", value, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendLessThan(Double value) {
            addCriterion("UniFriend <", value, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendLessThanOrEqualTo(Double value) {
            addCriterion("UniFriend <=", value, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendIn(List<Double> values) {
            addCriterion("UniFriend in", values, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendNotIn(List<Double> values) {
            addCriterion("UniFriend not in", values, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendBetween(Double value1, Double value2) {
            addCriterion("UniFriend between", value1, value2, "unifriend");
            return (Criteria) this;
        }

        public Criteria andUnifriendNotBetween(Double value1, Double value2) {
            addCriterion("UniFriend not between", value1, value2, "unifriend");
            return (Criteria) this;
        }

        public Criteria andContactflagIsNull() {
            addCriterion("ContactFlag is null");
            return (Criteria) this;
        }

        public Criteria andContactflagIsNotNull() {
            addCriterion("ContactFlag is not null");
            return (Criteria) this;
        }

        public Criteria andContactflagEqualTo(Double value) {
            addCriterion("ContactFlag =", value, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagNotEqualTo(Double value) {
            addCriterion("ContactFlag <>", value, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagGreaterThan(Double value) {
            addCriterion("ContactFlag >", value, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagGreaterThanOrEqualTo(Double value) {
            addCriterion("ContactFlag >=", value, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagLessThan(Double value) {
            addCriterion("ContactFlag <", value, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagLessThanOrEqualTo(Double value) {
            addCriterion("ContactFlag <=", value, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagIn(List<Double> values) {
            addCriterion("ContactFlag in", values, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagNotIn(List<Double> values) {
            addCriterion("ContactFlag not in", values, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagBetween(Double value1, Double value2) {
            addCriterion("ContactFlag between", value1, value2, "contactflag");
            return (Criteria) this;
        }

        public Criteria andContactflagNotBetween(Double value1, Double value2) {
            addCriterion("ContactFlag not between", value1, value2, "contactflag");
            return (Criteria) this;
        }

        public Criteria andMemberlistIsNull() {
            addCriterion("MemberList is null");
            return (Criteria) this;
        }

        public Criteria andMemberlistIsNotNull() {
            addCriterion("MemberList is not null");
            return (Criteria) this;
        }

        public Criteria andMemberlistEqualTo(String value) {
            addCriterion("MemberList =", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistNotEqualTo(String value) {
            addCriterion("MemberList <>", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistGreaterThan(String value) {
            addCriterion("MemberList >", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistGreaterThanOrEqualTo(String value) {
            addCriterion("MemberList >=", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistLessThan(String value) {
            addCriterion("MemberList <", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistLessThanOrEqualTo(String value) {
            addCriterion("MemberList <=", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistLike(String value) {
            addCriterion("MemberList like", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistNotLike(String value) {
            addCriterion("MemberList not like", value, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistIn(List<String> values) {
            addCriterion("MemberList in", values, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistNotIn(List<String> values) {
            addCriterion("MemberList not in", values, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistBetween(String value1, String value2) {
            addCriterion("MemberList between", value1, value2, "memberlist");
            return (Criteria) this;
        }

        public Criteria andMemberlistNotBetween(String value1, String value2) {
            addCriterion("MemberList not between", value1, value2, "memberlist");
            return (Criteria) this;
        }

        public Criteria andStarfriendIsNull() {
            addCriterion("StarFriend is null");
            return (Criteria) this;
        }

        public Criteria andStarfriendIsNotNull() {
            addCriterion("StarFriend is not null");
            return (Criteria) this;
        }

        public Criteria andStarfriendEqualTo(Double value) {
            addCriterion("StarFriend =", value, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendNotEqualTo(Double value) {
            addCriterion("StarFriend <>", value, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendGreaterThan(Double value) {
            addCriterion("StarFriend >", value, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendGreaterThanOrEqualTo(Double value) {
            addCriterion("StarFriend >=", value, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendLessThan(Double value) {
            addCriterion("StarFriend <", value, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendLessThanOrEqualTo(Double value) {
            addCriterion("StarFriend <=", value, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendIn(List<Double> values) {
            addCriterion("StarFriend in", values, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendNotIn(List<Double> values) {
            addCriterion("StarFriend not in", values, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendBetween(Double value1, Double value2) {
            addCriterion("StarFriend between", value1, value2, "starfriend");
            return (Criteria) this;
        }

        public Criteria andStarfriendNotBetween(Double value1, Double value2) {
            addCriterion("StarFriend not between", value1, value2, "starfriend");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlIsNull() {
            addCriterion("HeadImgUrl is null");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlIsNotNull() {
            addCriterion("HeadImgUrl is not null");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlEqualTo(String value) {
            addCriterion("HeadImgUrl =", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlNotEqualTo(String value) {
            addCriterion("HeadImgUrl <>", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlGreaterThan(String value) {
            addCriterion("HeadImgUrl >", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlGreaterThanOrEqualTo(String value) {
            addCriterion("HeadImgUrl >=", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlLessThan(String value) {
            addCriterion("HeadImgUrl <", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlLessThanOrEqualTo(String value) {
            addCriterion("HeadImgUrl <=", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlLike(String value) {
            addCriterion("HeadImgUrl like", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlNotLike(String value) {
            addCriterion("HeadImgUrl not like", value, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlIn(List<String> values) {
            addCriterion("HeadImgUrl in", values, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlNotIn(List<String> values) {
            addCriterion("HeadImgUrl not in", values, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlBetween(String value1, String value2) {
            addCriterion("HeadImgUrl between", value1, value2, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andHeadimgurlNotBetween(String value1, String value2) {
            addCriterion("HeadImgUrl not between", value1, value2, "headimgurl");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagIsNull() {
            addCriterion("AppAccountFlag is null");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagIsNotNull() {
            addCriterion("AppAccountFlag is not null");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagEqualTo(Double value) {
            addCriterion("AppAccountFlag =", value, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagNotEqualTo(Double value) {
            addCriterion("AppAccountFlag <>", value, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagGreaterThan(Double value) {
            addCriterion("AppAccountFlag >", value, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagGreaterThanOrEqualTo(Double value) {
            addCriterion("AppAccountFlag >=", value, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagLessThan(Double value) {
            addCriterion("AppAccountFlag <", value, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagLessThanOrEqualTo(Double value) {
            addCriterion("AppAccountFlag <=", value, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagIn(List<Double> values) {
            addCriterion("AppAccountFlag in", values, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagNotIn(List<Double> values) {
            addCriterion("AppAccountFlag not in", values, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagBetween(Double value1, Double value2) {
            addCriterion("AppAccountFlag between", value1, value2, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andAppaccountflagNotBetween(Double value1, Double value2) {
            addCriterion("AppAccountFlag not between", value1, value2, "appaccountflag");
            return (Criteria) this;
        }

        public Criteria andMembercountIsNull() {
            addCriterion("MemberCount is null");
            return (Criteria) this;
        }

        public Criteria andMembercountIsNotNull() {
            addCriterion("MemberCount is not null");
            return (Criteria) this;
        }

        public Criteria andMembercountEqualTo(Double value) {
            addCriterion("MemberCount =", value, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountNotEqualTo(Double value) {
            addCriterion("MemberCount <>", value, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountGreaterThan(Double value) {
            addCriterion("MemberCount >", value, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountGreaterThanOrEqualTo(Double value) {
            addCriterion("MemberCount >=", value, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountLessThan(Double value) {
            addCriterion("MemberCount <", value, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountLessThanOrEqualTo(Double value) {
            addCriterion("MemberCount <=", value, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountIn(List<Double> values) {
            addCriterion("MemberCount in", values, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountNotIn(List<Double> values) {
            addCriterion("MemberCount not in", values, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountBetween(Double value1, Double value2) {
            addCriterion("MemberCount between", value1, value2, "membercount");
            return (Criteria) this;
        }

        public Criteria andMembercountNotBetween(Double value1, Double value2) {
            addCriterion("MemberCount not between", value1, value2, "membercount");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialIsNull() {
            addCriterion("RemarkPYInitial is null");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialIsNotNull() {
            addCriterion("RemarkPYInitial is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialEqualTo(String value) {
            addCriterion("RemarkPYInitial =", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialNotEqualTo(String value) {
            addCriterion("RemarkPYInitial <>", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialGreaterThan(String value) {
            addCriterion("RemarkPYInitial >", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialGreaterThanOrEqualTo(String value) {
            addCriterion("RemarkPYInitial >=", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialLessThan(String value) {
            addCriterion("RemarkPYInitial <", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialLessThanOrEqualTo(String value) {
            addCriterion("RemarkPYInitial <=", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialLike(String value) {
            addCriterion("RemarkPYInitial like", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialNotLike(String value) {
            addCriterion("RemarkPYInitial not like", value, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialIn(List<String> values) {
            addCriterion("RemarkPYInitial in", values, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialNotIn(List<String> values) {
            addCriterion("RemarkPYInitial not in", values, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialBetween(String value1, String value2) {
            addCriterion("RemarkPYInitial between", value1, value2, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andRemarkpyinitialNotBetween(String value1, String value2) {
            addCriterion("RemarkPYInitial not between", value1, value2, "remarkpyinitial");
            return (Criteria) this;
        }

        public Criteria andCityIsNull() {
            addCriterion("City is null");
            return (Criteria) this;
        }

        public Criteria andCityIsNotNull() {
            addCriterion("City is not null");
            return (Criteria) this;
        }

        public Criteria andCityEqualTo(String value) {
            addCriterion("City =", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotEqualTo(String value) {
            addCriterion("City <>", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityGreaterThan(String value) {
            addCriterion("City >", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityGreaterThanOrEqualTo(String value) {
            addCriterion("City >=", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLessThan(String value) {
            addCriterion("City <", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLessThanOrEqualTo(String value) {
            addCriterion("City <=", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLike(String value) {
            addCriterion("City like", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotLike(String value) {
            addCriterion("City not like", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityIn(List<String> values) {
            addCriterion("City in", values, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotIn(List<String> values) {
            addCriterion("City not in", values, "city");
            return (Criteria) this;
        }

        public Criteria andCityBetween(String value1, String value2) {
            addCriterion("City between", value1, value2, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotBetween(String value1, String value2) {
            addCriterion("City not between", value1, value2, "city");
            return (Criteria) this;
        }

        public Criteria andNicknameIsNull() {
            addCriterion("NickName is null");
            return (Criteria) this;
        }

        public Criteria andNicknameIsNotNull() {
            addCriterion("NickName is not null");
            return (Criteria) this;
        }

        public Criteria andNicknameEqualTo(String value) {
            addCriterion("NickName =", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotEqualTo(String value) {
            addCriterion("NickName <>", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameGreaterThan(String value) {
            addCriterion("NickName >", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameGreaterThanOrEqualTo(String value) {
            addCriterion("NickName >=", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameLessThan(String value) {
            addCriterion("NickName <", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameLessThanOrEqualTo(String value) {
            addCriterion("NickName <=", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameLike(String value) {
            addCriterion("NickName like", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotLike(String value) {
            addCriterion("NickName not like", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameIn(List<String> values) {
            addCriterion("NickName in", values, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotIn(List<String> values) {
            addCriterion("NickName not in", values, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameBetween(String value1, String value2) {
            addCriterion("NickName between", value1, value2, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotBetween(String value1, String value2) {
            addCriterion("NickName not between", value1, value2, "nickname");
            return (Criteria) this;
        }

        public Criteria andProvinceIsNull() {
            addCriterion("Province is null");
            return (Criteria) this;
        }

        public Criteria andProvinceIsNotNull() {
            addCriterion("Province is not null");
            return (Criteria) this;
        }

        public Criteria andProvinceEqualTo(String value) {
            addCriterion("Province =", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotEqualTo(String value) {
            addCriterion("Province <>", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceGreaterThan(String value) {
            addCriterion("Province >", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceGreaterThanOrEqualTo(String value) {
            addCriterion("Province >=", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceLessThan(String value) {
            addCriterion("Province <", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceLessThanOrEqualTo(String value) {
            addCriterion("Province <=", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceLike(String value) {
            addCriterion("Province like", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotLike(String value) {
            addCriterion("Province not like", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceIn(List<String> values) {
            addCriterion("Province in", values, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotIn(List<String> values) {
            addCriterion("Province not in", values, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceBetween(String value1, String value2) {
            addCriterion("Province between", value1, value2, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotBetween(String value1, String value2) {
            addCriterion("Province not between", value1, value2, "province");
            return (Criteria) this;
        }

        public Criteria andSnsflagIsNull() {
            addCriterion("SnsFlag is null");
            return (Criteria) this;
        }

        public Criteria andSnsflagIsNotNull() {
            addCriterion("SnsFlag is not null");
            return (Criteria) this;
        }

        public Criteria andSnsflagEqualTo(Double value) {
            addCriterion("SnsFlag =", value, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagNotEqualTo(Double value) {
            addCriterion("SnsFlag <>", value, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagGreaterThan(Double value) {
            addCriterion("SnsFlag >", value, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagGreaterThanOrEqualTo(Double value) {
            addCriterion("SnsFlag >=", value, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagLessThan(Double value) {
            addCriterion("SnsFlag <", value, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagLessThanOrEqualTo(Double value) {
            addCriterion("SnsFlag <=", value, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagIn(List<Double> values) {
            addCriterion("SnsFlag in", values, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagNotIn(List<Double> values) {
            addCriterion("SnsFlag not in", values, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagBetween(Double value1, Double value2) {
            addCriterion("SnsFlag between", value1, value2, "snsflag");
            return (Criteria) this;
        }

        public Criteria andSnsflagNotBetween(Double value1, Double value2) {
            addCriterion("SnsFlag not between", value1, value2, "snsflag");
            return (Criteria) this;
        }

        public Criteria andAliasIsNull() {
            addCriterion("`Alias` is null");
            return (Criteria) this;
        }

        public Criteria andAliasIsNotNull() {
            addCriterion("`Alias` is not null");
            return (Criteria) this;
        }

        public Criteria andAliasEqualTo(String value) {
            addCriterion("`Alias` =", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasNotEqualTo(String value) {
            addCriterion("`Alias` <>", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasGreaterThan(String value) {
            addCriterion("`Alias` >", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasGreaterThanOrEqualTo(String value) {
            addCriterion("`Alias` >=", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasLessThan(String value) {
            addCriterion("`Alias` <", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasLessThanOrEqualTo(String value) {
            addCriterion("`Alias` <=", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasLike(String value) {
            addCriterion("`Alias` like", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasNotLike(String value) {
            addCriterion("`Alias` not like", value, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasIn(List<String> values) {
            addCriterion("`Alias` in", values, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasNotIn(List<String> values) {
            addCriterion("`Alias` not in", values, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasBetween(String value1, String value2) {
            addCriterion("`Alias` between", value1, value2, "alias");
            return (Criteria) this;
        }

        public Criteria andAliasNotBetween(String value1, String value2) {
            addCriterion("`Alias` not between", value1, value2, "alias");
            return (Criteria) this;
        }

        public Criteria andKeywordIsNull() {
            addCriterion("KeyWord is null");
            return (Criteria) this;
        }

        public Criteria andKeywordIsNotNull() {
            addCriterion("KeyWord is not null");
            return (Criteria) this;
        }

        public Criteria andKeywordEqualTo(String value) {
            addCriterion("KeyWord =", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordNotEqualTo(String value) {
            addCriterion("KeyWord <>", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordGreaterThan(String value) {
            addCriterion("KeyWord >", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordGreaterThanOrEqualTo(String value) {
            addCriterion("KeyWord >=", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordLessThan(String value) {
            addCriterion("KeyWord <", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordLessThanOrEqualTo(String value) {
            addCriterion("KeyWord <=", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordLike(String value) {
            addCriterion("KeyWord like", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordNotLike(String value) {
            addCriterion("KeyWord not like", value, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordIn(List<String> values) {
            addCriterion("KeyWord in", values, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordNotIn(List<String> values) {
            addCriterion("KeyWord not in", values, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordBetween(String value1, String value2) {
            addCriterion("KeyWord between", value1, value2, "keyword");
            return (Criteria) this;
        }

        public Criteria andKeywordNotBetween(String value1, String value2) {
            addCriterion("KeyWord not between", value1, value2, "keyword");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagIsNull() {
            addCriterion("HideInputBarFlag is null");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagIsNotNull() {
            addCriterion("HideInputBarFlag is not null");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagEqualTo(Double value) {
            addCriterion("HideInputBarFlag =", value, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagNotEqualTo(Double value) {
            addCriterion("HideInputBarFlag <>", value, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagGreaterThan(Double value) {
            addCriterion("HideInputBarFlag >", value, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagGreaterThanOrEqualTo(Double value) {
            addCriterion("HideInputBarFlag >=", value, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagLessThan(Double value) {
            addCriterion("HideInputBarFlag <", value, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagLessThanOrEqualTo(Double value) {
            addCriterion("HideInputBarFlag <=", value, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagIn(List<Double> values) {
            addCriterion("HideInputBarFlag in", values, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagNotIn(List<Double> values) {
            addCriterion("HideInputBarFlag not in", values, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagBetween(Double value1, Double value2) {
            addCriterion("HideInputBarFlag between", value1, value2, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andHideinputbarflagNotBetween(Double value1, Double value2) {
            addCriterion("HideInputBarFlag not between", value1, value2, "hideinputbarflag");
            return (Criteria) this;
        }

        public Criteria andSignatureIsNull() {
            addCriterion("Signature is null");
            return (Criteria) this;
        }

        public Criteria andSignatureIsNotNull() {
            addCriterion("Signature is not null");
            return (Criteria) this;
        }

        public Criteria andSignatureEqualTo(String value) {
            addCriterion("Signature =", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureNotEqualTo(String value) {
            addCriterion("Signature <>", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureGreaterThan(String value) {
            addCriterion("Signature >", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureGreaterThanOrEqualTo(String value) {
            addCriterion("Signature >=", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureLessThan(String value) {
            addCriterion("Signature <", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureLessThanOrEqualTo(String value) {
            addCriterion("Signature <=", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureLike(String value) {
            addCriterion("Signature like", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureNotLike(String value) {
            addCriterion("Signature not like", value, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureIn(List<String> values) {
            addCriterion("Signature in", values, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureNotIn(List<String> values) {
            addCriterion("Signature not in", values, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureBetween(String value1, String value2) {
            addCriterion("Signature between", value1, value2, "signature");
            return (Criteria) this;
        }

        public Criteria andSignatureNotBetween(String value1, String value2) {
            addCriterion("Signature not between", value1, value2, "signature");
            return (Criteria) this;
        }

        public Criteria andRemarknameIsNull() {
            addCriterion("RemarkName is null");
            return (Criteria) this;
        }

        public Criteria andRemarknameIsNotNull() {
            addCriterion("RemarkName is not null");
            return (Criteria) this;
        }

        public Criteria andRemarknameEqualTo(String value) {
            addCriterion("RemarkName =", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameNotEqualTo(String value) {
            addCriterion("RemarkName <>", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameGreaterThan(String value) {
            addCriterion("RemarkName >", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameGreaterThanOrEqualTo(String value) {
            addCriterion("RemarkName >=", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameLessThan(String value) {
            addCriterion("RemarkName <", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameLessThanOrEqualTo(String value) {
            addCriterion("RemarkName <=", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameLike(String value) {
            addCriterion("RemarkName like", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameNotLike(String value) {
            addCriterion("RemarkName not like", value, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameIn(List<String> values) {
            addCriterion("RemarkName in", values, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameNotIn(List<String> values) {
            addCriterion("RemarkName not in", values, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameBetween(String value1, String value2) {
            addCriterion("RemarkName between", value1, value2, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarknameNotBetween(String value1, String value2) {
            addCriterion("RemarkName not between", value1, value2, "remarkname");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinIsNull() {
            addCriterion("RemarkPYQuanPin is null");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinIsNotNull() {
            addCriterion("RemarkPYQuanPin is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinEqualTo(String value) {
            addCriterion("RemarkPYQuanPin =", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinNotEqualTo(String value) {
            addCriterion("RemarkPYQuanPin <>", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinGreaterThan(String value) {
            addCriterion("RemarkPYQuanPin >", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinGreaterThanOrEqualTo(String value) {
            addCriterion("RemarkPYQuanPin >=", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinLessThan(String value) {
            addCriterion("RemarkPYQuanPin <", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinLessThanOrEqualTo(String value) {
            addCriterion("RemarkPYQuanPin <=", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinLike(String value) {
            addCriterion("RemarkPYQuanPin like", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinNotLike(String value) {
            addCriterion("RemarkPYQuanPin not like", value, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinIn(List<String> values) {
            addCriterion("RemarkPYQuanPin in", values, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinNotIn(List<String> values) {
            addCriterion("RemarkPYQuanPin not in", values, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinBetween(String value1, String value2) {
            addCriterion("RemarkPYQuanPin between", value1, value2, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andRemarkpyquanpinNotBetween(String value1, String value2) {
            addCriterion("RemarkPYQuanPin not between", value1, value2, "remarkpyquanpin");
            return (Criteria) this;
        }

        public Criteria andUinIsNull() {
            addCriterion("Uin is null");
            return (Criteria) this;
        }

        public Criteria andUinIsNotNull() {
            addCriterion("Uin is not null");
            return (Criteria) this;
        }

        public Criteria andUinEqualTo(Double value) {
            addCriterion("Uin =", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotEqualTo(Double value) {
            addCriterion("Uin <>", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinGreaterThan(Double value) {
            addCriterion("Uin >", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinGreaterThanOrEqualTo(Double value) {
            addCriterion("Uin >=", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinLessThan(Double value) {
            addCriterion("Uin <", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinLessThanOrEqualTo(Double value) {
            addCriterion("Uin <=", value, "uin");
            return (Criteria) this;
        }

        public Criteria andUinIn(List<Double> values) {
            addCriterion("Uin in", values, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotIn(List<Double> values) {
            addCriterion("Uin not in", values, "uin");
            return (Criteria) this;
        }

        public Criteria andUinBetween(Double value1, Double value2) {
            addCriterion("Uin between", value1, value2, "uin");
            return (Criteria) this;
        }

        public Criteria andUinNotBetween(Double value1, Double value2) {
            addCriterion("Uin not between", value1, value2, "uin");
            return (Criteria) this;
        }

        public Criteria andOwneruinIsNull() {
            addCriterion("OwnerUin is null");
            return (Criteria) this;
        }

        public Criteria andOwneruinIsNotNull() {
            addCriterion("OwnerUin is not null");
            return (Criteria) this;
        }

        public Criteria andOwneruinEqualTo(Double value) {
            addCriterion("OwnerUin =", value, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinNotEqualTo(Double value) {
            addCriterion("OwnerUin <>", value, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinGreaterThan(Double value) {
            addCriterion("OwnerUin >", value, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinGreaterThanOrEqualTo(Double value) {
            addCriterion("OwnerUin >=", value, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinLessThan(Double value) {
            addCriterion("OwnerUin <", value, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinLessThanOrEqualTo(Double value) {
            addCriterion("OwnerUin <=", value, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinIn(List<Double> values) {
            addCriterion("OwnerUin in", values, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinNotIn(List<Double> values) {
            addCriterion("OwnerUin not in", values, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinBetween(Double value1, Double value2) {
            addCriterion("OwnerUin between", value1, value2, "owneruin");
            return (Criteria) this;
        }

        public Criteria andOwneruinNotBetween(Double value1, Double value2) {
            addCriterion("OwnerUin not between", value1, value2, "owneruin");
            return (Criteria) this;
        }

        public Criteria andIsownerIsNull() {
            addCriterion("IsOwner is null");
            return (Criteria) this;
        }

        public Criteria andIsownerIsNotNull() {
            addCriterion("IsOwner is not null");
            return (Criteria) this;
        }

        public Criteria andIsownerEqualTo(Double value) {
            addCriterion("IsOwner =", value, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerNotEqualTo(Double value) {
            addCriterion("IsOwner <>", value, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerGreaterThan(Double value) {
            addCriterion("IsOwner >", value, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerGreaterThanOrEqualTo(Double value) {
            addCriterion("IsOwner >=", value, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerLessThan(Double value) {
            addCriterion("IsOwner <", value, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerLessThanOrEqualTo(Double value) {
            addCriterion("IsOwner <=", value, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerIn(List<Double> values) {
            addCriterion("IsOwner in", values, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerNotIn(List<Double> values) {
            addCriterion("IsOwner not in", values, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerBetween(Double value1, Double value2) {
            addCriterion("IsOwner between", value1, value2, "isowner");
            return (Criteria) this;
        }

        public Criteria andIsownerNotBetween(Double value1, Double value2) {
            addCriterion("IsOwner not between", value1, value2, "isowner");
            return (Criteria) this;
        }

        public Criteria andPyinitialIsNull() {
            addCriterion("PYInitial is null");
            return (Criteria) this;
        }

        public Criteria andPyinitialIsNotNull() {
            addCriterion("PYInitial is not null");
            return (Criteria) this;
        }

        public Criteria andPyinitialEqualTo(String value) {
            addCriterion("PYInitial =", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialNotEqualTo(String value) {
            addCriterion("PYInitial <>", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialGreaterThan(String value) {
            addCriterion("PYInitial >", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialGreaterThanOrEqualTo(String value) {
            addCriterion("PYInitial >=", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialLessThan(String value) {
            addCriterion("PYInitial <", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialLessThanOrEqualTo(String value) {
            addCriterion("PYInitial <=", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialLike(String value) {
            addCriterion("PYInitial like", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialNotLike(String value) {
            addCriterion("PYInitial not like", value, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialIn(List<String> values) {
            addCriterion("PYInitial in", values, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialNotIn(List<String> values) {
            addCriterion("PYInitial not in", values, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialBetween(String value1, String value2) {
            addCriterion("PYInitial between", value1, value2, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andPyinitialNotBetween(String value1, String value2) {
            addCriterion("PYInitial not between", value1, value2, "pyinitial");
            return (Criteria) this;
        }

        public Criteria andGroupusernameIsNull() {
            addCriterion("GroupUserName is null");
            return (Criteria) this;
        }

        public Criteria andGroupusernameIsNotNull() {
            addCriterion("GroupUserName is not null");
            return (Criteria) this;
        }

        public Criteria andGroupusernameEqualTo(String value) {
            addCriterion("GroupUserName =", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameNotEqualTo(String value) {
            addCriterion("GroupUserName <>", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameGreaterThan(String value) {
            addCriterion("GroupUserName >", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameGreaterThanOrEqualTo(String value) {
            addCriterion("GroupUserName >=", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameLessThan(String value) {
            addCriterion("GroupUserName <", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameLessThanOrEqualTo(String value) {
            addCriterion("GroupUserName <=", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameLike(String value) {
            addCriterion("GroupUserName like", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameNotLike(String value) {
            addCriterion("GroupUserName not like", value, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameIn(List<String> values) {
            addCriterion("GroupUserName in", values, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameNotIn(List<String> values) {
            addCriterion("GroupUserName not in", values, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameBetween(String value1, String value2) {
            addCriterion("GroupUserName between", value1, value2, "groupusername");
            return (Criteria) this;
        }

        public Criteria andGroupusernameNotBetween(String value1, String value2) {
            addCriterion("GroupUserName not between", value1, value2, "groupusername");
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