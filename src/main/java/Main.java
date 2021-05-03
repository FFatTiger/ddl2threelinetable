import com.alibaba.druid.pool.DruidDataSource;
import entity.Database;
import entity.TableDetail;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.jdbc.core.JdbcTemplate;
import util.ExportWord;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wzy
 * @date 2021/5/2
 */
public class Main {

    private static final String SQL = "select col.table_schema, col.table_name, col.column_name, col.column_type, col.column_key, col.is_nullable,col.column_default,col.column_comment,col.character_set_name, col.EXTRA, tab.table_comment\n" +
            "            from information_schema.columns col inner JOIN information_schema.`TABLES` tab ON  col.TABLE_NAME = tab.TABLE_NAME AND col.TABLE_SCHEMA = tab.TABLE_SCHEMA where col.table_schema=?\n" +
            "            ORDER BY col.table_name,ORDINAL_POSITION ";

    public static void main(String[] args) throws Exception {
        ExportWord ew = new ExportWord();
        Main main = new Main();
        //修改此处数据库名
        String databaseName = "cczucampusforum";
        List<Database> databases = main.getTableDetails(databaseName);
        XWPFDocument document = ew.createXWPFDocument(databases);
        ew.exportCheckWord(databases, document, "expWordTest.docx");
        System.out.println("文档生成成功");
    }

    private List<Database> getTableDetails(String databaseName) throws IOException {
        InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("dbinfo.properties");
        Properties pro = new Properties();
        pro.load(resourceAsStream);

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(pro.getProperty("url"));
        druidDataSource.setUsername(pro.getProperty("username"));
        druidDataSource.setPassword(pro.getProperty("pass"));
        druidDataSource.setDriverClassName(pro.getProperty("driver"));

        JdbcTemplate jdbcTemplate = new JdbcTemplate(druidDataSource);
        final HashMap<String, Database> tableMap = new HashMap<>();


        return jdbcTemplate.query(SQL, (resultSet, rowNum) -> {
            String tableName = resultSet.getString("TABLE_NAME");
            Database database = tableMap.get(tableName);
            if (database == null) {
                database = new Database();
                tableMap.put(tableName, database);
                database.setTableSchema(resultSet.getString("col.TABLE_SCHEMA"));
                database.setTableComment(resultSet.getString("tab.TABLE_COMMENT"));
                database.setTableName(tableName);
                List<TableDetail> tableDetails = database.getTableDetails();
                TableDetail tableDetail = new TableDetail();
                tableDetail.setColumnName(resultSet.getString("col.COLUMN_NAME"));
                tableDetail.setColumnType(resultSet.getString("col.COLUMN_TYPE"));
                tableDetail.setColumnKey(resultSet.getString("col.COLUMN_KEY"));
                tableDetail.setIsNullable(resultSet.getString("col.IS_NULLABLE"));
                tableDetail.setColumnComment(resultSet.getString("col.COLUMN_COMMENT"));
                tableDetail.setColumnDefault(resultSet.getString("col.COLUMN_DEFAULT"));
                tableDetails.add(tableDetail);
                return database;
            } else {
                List<TableDetail> tableDetails = database.getTableDetails();
                TableDetail tableDetail = new TableDetail();
                tableDetail.setColumnName(resultSet.getString("col.COLUMN_NAME"));
                tableDetail.setColumnType(resultSet.getString("col.COLUMN_TYPE"));
                tableDetail.setColumnKey(resultSet.getString("col.COLUMN_KEY"));
                tableDetail.setIsNullable(resultSet.getString("col.IS_NULLABLE"));
                tableDetail.setColumnComment(resultSet.getString("col.COLUMN_COMMENT"));
                tableDetail.setColumnDefault(resultSet.getString("col.COLUMN_DEFAULT"));
                tableDetails.add(tableDetail);

                return null;
            }
        }, databaseName).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
