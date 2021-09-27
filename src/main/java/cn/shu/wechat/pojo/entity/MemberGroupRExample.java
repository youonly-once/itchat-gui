package cn.shu.wechat.pojo.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 7:47 PM
 */
public class MemberGroupRExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MemberGroupRExample() {
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

        public Criteria andMemberusernameIsNull() {
            addCriterion("MemberUserName is null");
            return (Criteria) this;
        }

        public Criteria andMemberusernameIsNotNull() {
            addCriterion("MemberUserName is not null");
            return (Criteria) this;
        }

        public Criteria andMemberusernameEqualTo(String value) {
            addCriterion("MemberUserName =", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameNotEqualTo(String value) {
            addCriterion("MemberUserName <>", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameGreaterThan(String value) {
            addCriterion("MemberUserName >", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameGreaterThanOrEqualTo(String value) {
            addCriterion("MemberUserName >=", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameLessThan(String value) {
            addCriterion("MemberUserName <", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameLessThanOrEqualTo(String value) {
            addCriterion("MemberUserName <=", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameLike(String value) {
            addCriterion("MemberUserName like", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameNotLike(String value) {
            addCriterion("MemberUserName not like", value, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameIn(List<String> values) {
            addCriterion("MemberUserName in", values, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameNotIn(List<String> values) {
            addCriterion("MemberUserName not in", values, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameBetween(String value1, String value2) {
            addCriterion("MemberUserName between", value1, value2, "memberusername");
            return (Criteria) this;
        }

        public Criteria andMemberusernameNotBetween(String value1, String value2) {
            addCriterion("MemberUserName not between", value1, value2, "memberusername");
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