package cn.shu.wechat.beans.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 7/1/2021 6:24 PM
 */
public class StatusExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public StatusExample() {
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

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andUndoStatusIsNull() {
            addCriterion("undo_status is null");
            return (Criteria) this;
        }

        public Criteria andUndoStatusIsNotNull() {
            addCriterion("undo_status is not null");
            return (Criteria) this;
        }

        public Criteria andUndoStatusEqualTo(Short value) {
            addCriterion("undo_status =", value, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusNotEqualTo(Short value) {
            addCriterion("undo_status <>", value, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusGreaterThan(Short value) {
            addCriterion("undo_status >", value, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("undo_status >=", value, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusLessThan(Short value) {
            addCriterion("undo_status <", value, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusLessThanOrEqualTo(Short value) {
            addCriterion("undo_status <=", value, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusIn(List<Short> values) {
            addCriterion("undo_status in", values, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusNotIn(List<Short> values) {
            addCriterion("undo_status not in", values, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusBetween(Short value1, Short value2) {
            addCriterion("undo_status between", value1, value2, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andUndoStatusNotBetween(Short value1, Short value2) {
            addCriterion("undo_status not between", value1, value2, "undoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusIsNull() {
            addCriterion("auto_status is null");
            return (Criteria) this;
        }

        public Criteria andAutoStatusIsNotNull() {
            addCriterion("auto_status is not null");
            return (Criteria) this;
        }

        public Criteria andAutoStatusEqualTo(Short value) {
            addCriterion("auto_status =", value, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusNotEqualTo(Short value) {
            addCriterion("auto_status <>", value, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusGreaterThan(Short value) {
            addCriterion("auto_status >", value, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("auto_status >=", value, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusLessThan(Short value) {
            addCriterion("auto_status <", value, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusLessThanOrEqualTo(Short value) {
            addCriterion("auto_status <=", value, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusIn(List<Short> values) {
            addCriterion("auto_status in", values, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusNotIn(List<Short> values) {
            addCriterion("auto_status not in", values, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusBetween(Short value1, Short value2) {
            addCriterion("auto_status between", value1, value2, "autoStatus");
            return (Criteria) this;
        }

        public Criteria andAutoStatusNotBetween(Short value1, Short value2) {
            addCriterion("auto_status not between", value1, value2, "autoStatus");
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