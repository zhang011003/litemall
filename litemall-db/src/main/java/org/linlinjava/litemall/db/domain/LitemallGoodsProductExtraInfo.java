package org.linlinjava.litemall.db.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class LitemallGoodsProductExtraInfo {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    public static final Boolean IS_DELETED = Deleted.IS_DELETED.value();

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    public static final Boolean NOT_DELETED = Deleted.NOT_DELETED.value();

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.id
     *
     * @mbg.generated
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.product_id
     *
     * @mbg.generated
     */
    private Integer productId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.goods_id
     *
     * @mbg.generated
     */
    private Integer goodsId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.admin_id
     *
     * @mbg.generated
     */
    private Integer adminId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.base_price
     *
     * @mbg.generated
     */
    private BigDecimal basePrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.dispatch_price
     *
     * @mbg.generated
     */
    private BigDecimal dispatchPrice;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.price
     *
     * @mbg.generated
     */
    private BigDecimal price;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.number
     *
     * @mbg.generated
     */
    private Integer number;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.parent_admin_id
     *
     * @mbg.generated
     */
    private Integer parentAdminId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.add_time
     *
     * @mbg.generated
     */
    private LocalDateTime addTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.update_time
     *
     * @mbg.generated
     */
    private LocalDateTime updateTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column litemall_goods_product_extra_info.deleted
     *
     * @mbg.generated
     */
    private Boolean deleted;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.id
     *
     * @return the value of litemall_goods_product_extra_info.id
     *
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.id
     *
     * @param id the value for litemall_goods_product_extra_info.id
     *
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.product_id
     *
     * @return the value of litemall_goods_product_extra_info.product_id
     *
     * @mbg.generated
     */
    public Integer getProductId() {
        return productId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.product_id
     *
     * @param productId the value for litemall_goods_product_extra_info.product_id
     *
     * @mbg.generated
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.goods_id
     *
     * @return the value of litemall_goods_product_extra_info.goods_id
     *
     * @mbg.generated
     */
    public Integer getGoodsId() {
        return goodsId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.goods_id
     *
     * @param goodsId the value for litemall_goods_product_extra_info.goods_id
     *
     * @mbg.generated
     */
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.admin_id
     *
     * @return the value of litemall_goods_product_extra_info.admin_id
     *
     * @mbg.generated
     */
    public Integer getAdminId() {
        return adminId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.admin_id
     *
     * @param adminId the value for litemall_goods_product_extra_info.admin_id
     *
     * @mbg.generated
     */
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.base_price
     *
     * @return the value of litemall_goods_product_extra_info.base_price
     *
     * @mbg.generated
     */
    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.base_price
     *
     * @param basePrice the value for litemall_goods_product_extra_info.base_price
     *
     * @mbg.generated
     */
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.dispatch_price
     *
     * @return the value of litemall_goods_product_extra_info.dispatch_price
     *
     * @mbg.generated
     */
    public BigDecimal getDispatchPrice() {
        return dispatchPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.dispatch_price
     *
     * @param dispatchPrice the value for litemall_goods_product_extra_info.dispatch_price
     *
     * @mbg.generated
     */
    public void setDispatchPrice(BigDecimal dispatchPrice) {
        this.dispatchPrice = dispatchPrice;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.price
     *
     * @return the value of litemall_goods_product_extra_info.price
     *
     * @mbg.generated
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.price
     *
     * @param price the value for litemall_goods_product_extra_info.price
     *
     * @mbg.generated
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.number
     *
     * @return the value of litemall_goods_product_extra_info.number
     *
     * @mbg.generated
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.number
     *
     * @param number the value for litemall_goods_product_extra_info.number
     *
     * @mbg.generated
     */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.parent_admin_id
     *
     * @return the value of litemall_goods_product_extra_info.parent_admin_id
     *
     * @mbg.generated
     */
    public Integer getParentAdminId() {
        return parentAdminId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.parent_admin_id
     *
     * @param parentAdminId the value for litemall_goods_product_extra_info.parent_admin_id
     *
     * @mbg.generated
     */
    public void setParentAdminId(Integer parentAdminId) {
        this.parentAdminId = parentAdminId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.add_time
     *
     * @return the value of litemall_goods_product_extra_info.add_time
     *
     * @mbg.generated
     */
    public LocalDateTime getAddTime() {
        return addTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.add_time
     *
     * @param addTime the value for litemall_goods_product_extra_info.add_time
     *
     * @mbg.generated
     */
    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.update_time
     *
     * @return the value of litemall_goods_product_extra_info.update_time
     *
     * @mbg.generated
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.update_time
     *
     * @param updateTime the value for litemall_goods_product_extra_info.update_time
     *
     * @mbg.generated
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    public void andLogicalDeleted(boolean deleted) {
        setDeleted(deleted ? Deleted.IS_DELETED.value() : Deleted.NOT_DELETED.value());
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column litemall_goods_product_extra_info.deleted
     *
     * @return the value of litemall_goods_product_extra_info.deleted
     *
     * @mbg.generated
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column litemall_goods_product_extra_info.deleted
     *
     * @param deleted the value for litemall_goods_product_extra_info.deleted
     *
     * @mbg.generated
     */
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", IS_DELETED=").append(IS_DELETED);
        sb.append(", NOT_DELETED=").append(NOT_DELETED);
        sb.append(", id=").append(id);
        sb.append(", productId=").append(productId);
        sb.append(", goodsId=").append(goodsId);
        sb.append(", adminId=").append(adminId);
        sb.append(", basePrice=").append(basePrice);
        sb.append(", dispatchPrice=").append(dispatchPrice);
        sb.append(", price=").append(price);
        sb.append(", number=").append(number);
        sb.append(", parentAdminId=").append(parentAdminId);
        sb.append(", addTime=").append(addTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", deleted=").append(deleted);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        LitemallGoodsProductExtraInfo other = (LitemallGoodsProductExtraInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getProductId() == null ? other.getProductId() == null : this.getProductId().equals(other.getProductId()))
            && (this.getGoodsId() == null ? other.getGoodsId() == null : this.getGoodsId().equals(other.getGoodsId()))
            && (this.getAdminId() == null ? other.getAdminId() == null : this.getAdminId().equals(other.getAdminId()))
            && (this.getBasePrice() == null ? other.getBasePrice() == null : this.getBasePrice().equals(other.getBasePrice()))
            && (this.getDispatchPrice() == null ? other.getDispatchPrice() == null : this.getDispatchPrice().equals(other.getDispatchPrice()))
            && (this.getPrice() == null ? other.getPrice() == null : this.getPrice().equals(other.getPrice()))
            && (this.getNumber() == null ? other.getNumber() == null : this.getNumber().equals(other.getNumber()))
            && (this.getParentAdminId() == null ? other.getParentAdminId() == null : this.getParentAdminId().equals(other.getParentAdminId()))
            && (this.getAddTime() == null ? other.getAddTime() == null : this.getAddTime().equals(other.getAddTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getProductId() == null) ? 0 : getProductId().hashCode());
        result = prime * result + ((getGoodsId() == null) ? 0 : getGoodsId().hashCode());
        result = prime * result + ((getAdminId() == null) ? 0 : getAdminId().hashCode());
        result = prime * result + ((getBasePrice() == null) ? 0 : getBasePrice().hashCode());
        result = prime * result + ((getDispatchPrice() == null) ? 0 : getDispatchPrice().hashCode());
        result = prime * result + ((getPrice() == null) ? 0 : getPrice().hashCode());
        result = prime * result + ((getNumber() == null) ? 0 : getNumber().hashCode());
        result = prime * result + ((getParentAdminId() == null) ? 0 : getParentAdminId().hashCode());
        result = prime * result + ((getAddTime() == null) ? 0 : getAddTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
        return result;
    }

    /**
     * This enum was generated by MyBatis Generator.
     * This enum corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    public enum Deleted {
        NOT_DELETED(new Boolean("0"), "未删除"),
        IS_DELETED(new Boolean("1"), "已删除");

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private final Boolean value;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private final String name;

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        Deleted(Boolean value, String name) {
            this.value = value;
            this.name = name;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public Boolean getValue() {
            return this.value;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public Boolean value() {
            return this.value;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String getName() {
            return this.name;
        }
    }

    /**
     * This enum was generated by MyBatis Generator.
     * This enum corresponds to the database table litemall_goods_product_extra_info
     *
     * @mbg.generated
     */
    public enum Column {
        id("id", "id", "INTEGER", false),
        productId("product_id", "productId", "INTEGER", false),
        goodsId("goods_id", "goodsId", "INTEGER", false),
        adminId("admin_id", "adminId", "INTEGER", false),
        basePrice("base_price", "basePrice", "DECIMAL", false),
        dispatchPrice("dispatch_price", "dispatchPrice", "DECIMAL", false),
        price("price", "price", "DECIMAL", false),
        number("number", "number", "INTEGER", true),
        parentAdminId("parent_admin_id", "parentAdminId", "INTEGER", false),
        addTime("add_time", "addTime", "TIMESTAMP", false),
        updateTime("update_time", "updateTime", "TIMESTAMP", false),
        deleted("deleted", "deleted", "BIT", false);

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private static final String BEGINNING_DELIMITER = "`";

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private static final String ENDING_DELIMITER = "`";

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private final String column;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private final boolean isColumnNameDelimited;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private final String javaProperty;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        private final String jdbcType;

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String value() {
            return this.column;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String getValue() {
            return this.column;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String getJavaProperty() {
            return this.javaProperty;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String getJdbcType() {
            return this.jdbcType;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public static Column[] excludes(Column ... excludes) {
            ArrayList<Column> columns = new ArrayList<>(Arrays.asList(Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new Column[]{});
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table litemall_goods_product_extra_info
         *
         * @mbg.generated
         */
        public String getAliasedEscapedColumnName() {
            return this.getEscapedColumnName();
        }
    }
}