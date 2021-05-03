package util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import anno.TableField;
import entity.Result;
import entity.TableDetail;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTBorderImpl;
import util.XWPFHelper;
import util.XWPFHelperTable;


/**
 * @Description 导出word文档
 */
public class ExportWord {
    private XWPFHelperTable xwpfHelperTable = null;
    private XWPFHelper xwpfHelper = null;
    public ExportWord() {
        xwpfHelperTable = new XWPFHelperTable();
        xwpfHelper = new XWPFHelper();
    }
    /**
     * 创建好文档的基本 标题，表格  段落等部分
     * @return
     */
    public XWPFDocument createXWPFDocument(List<Result> results) {
        XWPFDocument doc = new XWPFDocument();

        for (Result result : results) {
            int rows = result.getTableDetails().size();
            int cols = TableDetail.class.getDeclaredFields().length;
            createTableParagraph(doc, rows + 1, cols);
            XWPFParagraph titleParagraph = doc.createParagraph();    //新建一个标题段落对象（就是一段文字）
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);//样式居中
            XWPFRun titleFun = titleParagraph.createRun();    //创建文本对象
            titleFun.setText(result.getTableName());
            titleFun.addBreak();    //换行
            titleFun.addBreak();    //换行
        }
        return doc;
    }

    /**
     * 设置表格
     *
     * @param tableDetailClass
     * @param document
     * @param rows
     * @param cols
     */
    public void createTableParagraph( XWPFDocument document, int rows, int cols) {
        XWPFTable table = document.createTable(rows, cols);
        xwpfHelperTable.setTableWidthAndHAlign(table, "9072", STJc.CENTER);

        List<XWPFTableRow> rowList = table.getRows();
        CTTblBorders borders=table.getCTTbl().getTblPr().addNewTblBorders();
        CTBorder hBorder=borders.addNewInsideH();
        hBorder.setVal(STBorder.Enum.forString("nil"));
        hBorder.setSz(new BigInteger("1"));

        CTBorder vBorder=borders.addNewInsideV();
        vBorder.setVal(STBorder.Enum.forString("nil"));
        vBorder.setSz(new BigInteger("1"));

        CTBorder lBorder=borders.addNewLeft();
        lBorder.setVal(STBorder.Enum.forString("nil"));
        lBorder.setSz(new BigInteger("1"));

        CTBorder rBorder=borders.addNewRight();
        rBorder.setVal(STBorder.Enum.forString("nil"));
        rBorder.setSz(new BigInteger("1"));

        CTBorder tBorder=borders.addNewTop();
        tBorder.setVal(STBorder.Enum.forString("thick"));
        tBorder.setSz(new BigInteger("10"));

        CTBorder bBorder=borders.addNewBottom();
        bBorder.setVal(STBorder.Enum.forString("thick"));
        bBorder.setSz(new BigInteger("10"));

        CTTbl ttbl = table.getCTTbl();
        CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        CTJc cTJc=tblPr.addNewJc();
        cTJc.setVal(STJc.Enum.forString("center"));
        tblWidth.setW(new BigInteger("8000"));
        tblWidth.setType(STTblWidth.DXA);

        //设置表头样式与标题
        List<XWPFTableCell> tableCells = rowList.get(0).getTableCells();
        Iterator<Field> fieldIterator = Arrays.stream(TableDetail.class.getDeclaredFields()).iterator();
        for (XWPFTableCell tableCell : tableCells) {
            Field field = fieldIterator.next();
            TableField annotation = field.getAnnotation(TableField.class);
            String value = annotation.value();
            CTTcBorders ctTcBorders = tableCell.getCTTc().addNewTcPr().addNewTcBorders();
            CTBorder ctBorder = ctTcBorders.addNewBottom();
            ctBorder.setVal(STBorder.Enum.forString("single"));
            ctBorder.setSz(new BigInteger("1"));
            setCellText(tableCell, value,"FFFFFF", 1600);
        }


    }

    /**
     * 往表格中填充数据
     * @param dataList
     * @param document
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void exportCheckWord(List<Result> results, XWPFDocument document, String savePath) throws IOException, IllegalAccessException {

        Iterator<XWPFTable> tableIterator = document.getTables().iterator();
        for (Result result : results) {
            XWPFTable table = tableIterator.next();
            fillTableData(table, result.getTableDetails());
        }

        xwpfHelper.saveDocument(document, savePath);
    }
    /**
     * 往表格中填充数据
     * @param table
     * @param tableData
     */
    public void fillTableData(XWPFTable table, List<TableDetail> tableDetails) throws IllegalAccessException {
        Iterator<XWPFTableRow> rowIterator = table.getRows().iterator();
        rowIterator.next();

        for (TableDetail tableDetail : tableDetails) {
            List<XWPFTableCell> tableCells = rowIterator.next().getTableCells();
            Iterator<Field> fieldIterator = Arrays.stream(tableDetail.getClass().getDeclaredFields()).iterator();

            for (XWPFTableCell tableCell : tableCells) {
                Field field = fieldIterator.next();
                field.setAccessible(true);
                setCellText(tableCell, (String) field.get(tableDetail),"FFFFFF", 1600);

            }
        }
    }

    private void setCellText(XWPFTableCell cell,String text, String bgcolor, int width) {
        CTTc cttc = cell.getCTTc();
        CTTcPr cellPr = cttc.addNewTcPr();
        cellPr.addNewTcW().setW(BigInteger.valueOf(width));
        //cell.setColor(bgcolor);
        CTTcPr ctPr = cttc.addNewTcPr();
        CTShd ctshd = ctPr.addNewShd();
        ctshd.setFill(bgcolor);
        ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
        cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
        cell.setText(text);
    }


}