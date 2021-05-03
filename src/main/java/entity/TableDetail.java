package entity;

import anno.TableField;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author wzy
 * @date 2021/5/2
 */
public class TableDetail {
    @TableField("字段名")
    private String columnName;
    @TableField("类型")
    private String columnType;
    @TableField("是否为空")
    private String isNullable;
    @TableField("索引")
    private String columnKey;
    @TableField("默认值")
    private String columnDefault;
    @TableField("说明")
    private String columnComment;

    public static void main(String[] args) {
        Field declaredField = Arrays.stream(new TableDetail().getClass().getDeclaredFields()).iterator().next();
        TableField annotation = declaredField.getAnnotation(TableField.class);
        System.out.println(annotation.value());
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        if ("".equals(columnKey) || columnKey == null) {
            this.columnKey = "无";

        } else {
            this.columnKey = columnKey;
        }
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        if ("".equals(columnDefault) || columnDefault == null) {
            this.columnDefault = "无";
        } else {
            this.columnDefault = columnDefault;
        }
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }


    @Override
    public String toString() {
        return "TableDetail{" +
                "columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", columnKey='" + columnKey + '\'' +
                ", isNullable='" + isNullable + '\'' +
                ", columnDefault='" + columnDefault + '\'' +
                ", columnComment='" + columnComment + '\'' +
                '}';
    }
}
