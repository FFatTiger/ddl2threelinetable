package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wzy
 * @date 2021/5/2
 */
public class Result {

    private String tableSchema;
    private String tableName;

    private List<TableDetail> tableDetails;

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TableDetail> getTableDetails() {
        if (tableDetails == null) {
            tableDetails = new ArrayList<>();
        }
        return tableDetails;
    }

    public void setTableDetails(List<TableDetail> tableDetails) {
        this.tableDetails = tableDetails;
    }

    @Override
    public String toString() {
        return "Result{" +
                "tableSchema='" + tableSchema + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableDetails=" + tableDetails +
                '}' + '\n';
    }
}
