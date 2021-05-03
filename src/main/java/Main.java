import com.alibaba.druid.pool.DruidDataSource;
import entity.Result;
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

    private static final String SQL = "select table_schema,table_name,column_name,column_type,column_key,is_nullable,column_default,column_comment,character_set_name, EXTRA\n" +
            "\n" +
            "from information_schema.columns where table_schema=?\n" +
            "ORDER BY table_name,ORDINAL_POSITION\n" +
            ";";

    public static void main(String[] args) throws Exception {
        ExportWord ew = new ExportWord();
        Main main = new Main();

        //修改此处数据库名
        String databaseName = "cczucampusforum";
        List<Result> results = main.getTableDetails(databaseName);
        XWPFDocument document = ew.createXWPFDocument(results);
        ew.exportCheckWord(results, document, "expWordTest.docx");
        System.out.println("文档生成成功");
    }

    private List<Result> getTableDetails(String databaseName) throws IOException {
        InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("dbinfo.properties");
        Properties pro = new Properties();
        pro.load(resourceAsStream);

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(pro.getProperty("url"));
        druidDataSource.setUsername(pro.getProperty("username"));
        druidDataSource.setPassword(pro.getProperty("pass"));
        druidDataSource.setDriverClassName(pro.getProperty("driver"));

        JdbcTemplate jdbcTemplate = new JdbcTemplate(druidDataSource);
        final HashMap<String, Result> tableMap = new HashMap<>();


        return jdbcTemplate.query(SQL, (resultSet, rowNum) -> {
            String tableName = resultSet.getString("TABLE_NAME");
            Result result = tableMap.get(tableName);
            if (result == null) {
                result = new Result();
                tableMap.put(tableName, result);
                result.setTableSchema(resultSet.getString("TABLE_SCHEMA"));
                result.setTableName(tableName);
                List<TableDetail> tableDetails = result.getTableDetails();
                TableDetail tableDetail = new TableDetail();
                tableDetail.setColumnName(resultSet.getString("COLUMN_NAME"));
                tableDetail.setColumnType(resultSet.getString("COLUMN_TYPE"));
                tableDetail.setColumnKey(resultSet.getString("COLUMN_KEY"));
                tableDetail.setIsNullable(resultSet.getString("IS_NULLABLE"));
                tableDetail.setColumnComment(resultSet.getString("COLUMN_COMMENT"));
                tableDetail.setColumnDefault(resultSet.getString("COLUMN_DEFAULT"));
                tableDetails.add(tableDetail);
                return result;
            } else {
                List<TableDetail> tableDetails = result.getTableDetails();
                TableDetail tableDetail = new TableDetail();
                tableDetail.setColumnName(resultSet.getString("COLUMN_NAME"));
                tableDetail.setColumnType(resultSet.getString("COLUMN_TYPE"));
                tableDetail.setColumnKey(resultSet.getString("COLUMN_KEY"));
                tableDetail.setIsNullable(resultSet.getString("IS_NULLABLE"));
                tableDetail.setColumnComment(resultSet.getString("COLUMN_COMMENT"));
                tableDetail.setColumnDefault(resultSet.getString("COLUMN_DEFAULT"));
                tableDetails.add(tableDetail);

                return null;
            }
        }, databaseName).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
