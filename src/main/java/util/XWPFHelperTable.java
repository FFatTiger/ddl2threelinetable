package util;

/**
 * 
 * @date 2021/5/2
 */
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

/**
 * @Description 操作word的基本工具类
 * 2018年12月3日  上午11:12:18
 * 
 */
public class XWPFHelperTable {

    /**
     * 删除指定位置的表格，被删除表格后的索引位置
     * @param document
     * @param pos
     * 
     */
    public void deleteTableByIndex(XWPFDocument document, int pos) {
        Iterator<IBodyElement> bodyElement = document.getBodyElementsIterator();
        int eIndex = 0, tableIndex = -1;
        while(bodyElement.hasNext()) {
            IBodyElement element = bodyElement.next();
            BodyElementType elementType = element.getElementType();
            if(elementType == BodyElementType.TABLE) {
                tableIndex++;
                if(tableIndex == pos) {
                    break;
                }
            }
            eIndex++;
        }
        document.removeBodyElement(eIndex);
    }
    /**
     * 获得指定位置的表格
     * @param document
     * @param index
     * @return
     * 
     */
    public XWPFTable getTableByIndex(XWPFDocument document, int index) {
        List<XWPFTable> tableList = document.getTables();
        if(tableList == null || index < 0 || index > tableList.size()) {
            return null;
        }
        return tableList.get(index);
    }
    /**
     * 得到表格的内容（第一次跨行单元格视为一个，第二次跳过跨行合并的单元格）
     * @param table
     * @return
     * 
     */
    public List<List<String>> getTableRConten(XWPFTable table) {
        List<List<String>> tableContextList = new ArrayList<List<String>>();
        for(int rowIndex = 0, rowLen = table.getNumberOfRows(); rowIndex < rowLen; rowIndex++) {
            XWPFTableRow row = table.getRow(rowIndex);
            List<String> cellContentList = new ArrayList<String>();
            for(int colIndex = 0, colLen = row.getTableCells().size(); colIndex < colLen; colIndex++ ) {
                XWPFTableCell cell = row.getCell(colIndex);
                CTTc ctTc = cell.getCTTc();
                if(ctTc.isSetTcPr()) {
                    CTTcPr tcPr = ctTc.getTcPr();
                    if(tcPr.isSetHMerge()) {
                        CTHMerge hMerge = tcPr.getHMerge();
                        if(STMerge.RESTART.equals(hMerge.getVal())) {
                            cellContentList.add(getTableCellContent(cell));
                        }
                    } else if(tcPr.isSetVMerge()) {
                        CTVMerge vMerge = tcPr.getVMerge();
                        if(STMerge.RESTART.equals(vMerge.getVal())) {
                            cellContentList.add(getTableCellContent(cell));
                        }
                    } else {
                        cellContentList.add(getTableCellContent(cell));
                    }
                }
            }
            tableContextList.add(cellContentList);
        }
        return tableContextList;
    }

    /**
     * 获得一个表格的单元格的内容
     * @param cell
     * @return
     * 
     */
    public String getTableCellContent(XWPFTableCell cell) {
        StringBuffer sb = new StringBuffer();
        List<XWPFParagraph> cellParagList = cell.getParagraphs();
        if(cellParagList != null && cellParagList.size() > 0) {
            for(XWPFParagraph xwpfPr: cellParagList) {
                List<XWPFRun> runs = xwpfPr.getRuns();
                if(runs != null && runs.size() > 0) {
                    for(XWPFRun xwpfRun : runs) {
                        sb.append(xwpfRun.getText(0));
                    }
                }
            }
        }
        return sb.toString();
    }
    /**
     * 得到表格内容，合并后的单元格视为一个单元格
     * @param table
     * @return
     * 
     */
    public List<List<String>> getTableContent(XWPFTable table) {
        List<List<String>> tableContentList = new ArrayList<List<String>>();
        for (int rowIndex = 0, rowLen = table.getNumberOfRows(); rowIndex < rowLen; rowIndex++) {
            XWPFTableRow row = table.getRow(rowIndex);
            List<String> cellContentList = new ArrayList<String>();
            for (int colIndex = 0, colLen = row.getTableCells().size(); colIndex < colLen; colIndex++) {
                XWPFTableCell cell = row.getCell(colIndex);
                cellContentList.add(getTableCellContent(cell));
            }
            tableContentList.add(cellContentList);
        }
        return tableContentList;
    }

    /**
     * 跨列合并
     * @param table
     * @param row    所合并的行
     * @param fromCell    起始列
     * @param toCell    终止列
     * @Description
     * 
     */
    public void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for(int cellIndex = fromCell; cellIndex <= toCell; cellIndex++ ) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if(cellIndex == fromCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }
    /**
     * 跨行合并
     * @param table
     * @param col    合并的列
     * @param fromRow    起始行
     * @param toRow    终止行
     * @Description
     * 
     */
    public void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for(int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            //第一个合并单元格用重启合并值设置
            if(rowIndex == fromRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                //合并第一个单元格的单元被设置为“继续”
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * @Description: 创建表格,创建后表格至少有1行1列,设置列宽
     */
    public XWPFTable createTable(XWPFDocument xdoc, int rowSize, int cellSize,
                                 boolean isSetColWidth, int[] colWidths) {
        XWPFTable table = xdoc.createTable(rowSize, cellSize);
        if (isSetColWidth) {
            CTTbl ttbl = table.getCTTbl();
            CTTblGrid tblGrid = ttbl.addNewTblGrid();
            for (int j = 0, len = Math.min(cellSize, colWidths.length); j < len; j++) {
                CTTblGridCol gridCol = tblGrid.addNewGridCol();
                gridCol.setW(new BigInteger(String.valueOf(colWidths[j])));
            }
        }
        return table;
    }

    /**
     * @Description: 设置表格总宽度与水平对齐方式
     */
    public void setTableWidthAndHAlign(XWPFTable table, String width,
                                       STJc.Enum enumValue) {
        CTTblPr tblPr = getTableCTTblPr(table);
        // 表格宽度
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        if (enumValue != null) {
            CTJc cTJc = tblPr.addNewJc();
            cTJc.setVal(enumValue);
        }
        // 设置宽度
        tblWidth.setW(new BigInteger(width));
        tblWidth.setType(STTblWidth.DXA);
    }

    /**
     * @Description: 得到Table的CTTblPr,不存在则新建
     */
    public CTTblPr getTableCTTblPr(XWPFTable table) {
        CTTbl ttbl = table.getCTTbl();
        // 表格属性
        CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();
        return tblPr;
    }

    /**
     * 设置表格行高
     * @param infoTable
     * @param heigth 高度
     * @param vertical 表格内容的显示方式：居中、靠右...
     * 
     */
    public void setTableHeight(XWPFTable infoTable, int heigth, STVerticalJc.Enum vertical) {
        List<XWPFTableRow> rows = infoTable.getRows();
        for(XWPFTableRow row : rows) {
            CTTrPr trPr = row.getCtRow().addNewTrPr();
            CTHeight ht = trPr.addNewTrHeight();
            ht.setVal(BigInteger.valueOf(heigth));
            List<XWPFTableCell> cells = row.getTableCells();
            for(XWPFTableCell tableCell : cells ) {
                CTTcPr cttcpr = tableCell.getCTTc().addNewTcPr();
                cttcpr.addNewVAlign().setVal(vertical);
            }
        }
    }
}
