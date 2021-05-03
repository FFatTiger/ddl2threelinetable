package util;

/**
 * @author wzy
 * @date 2021/5/2
 */

import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;


/**
 * @Description 设置docx文档的样式及一些操作
 * 基本概念说明：XWPFDocument代表一个docx文档，其可以用来读docx文档，也可以用来写docx文档
 *     XWPFParagraph代表文档、表格、标题等种的段落，由多个XWPFRun组成
 *     XWPFRun代表具有同样风格的一段文本
 *  XWPFTable代表一个表格
 *  XWPFTableRow代表表格的一行
 *  XWPFTableCell代表表格的一个单元格
 *  XWPFChar 表示.docx文件中的图表
 *  XWPFHyperlink 表示超链接
 *  XWPFPicture  代表图片
 *
 *  注意：直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun。
 */
public class XWPFHelper {
    private static Logger logger = Logger.getLogger(XWPFHelper.class.toString());

    /**
     * 创建一个word对象
     * @return
     */
    public XWPFDocument createDocument() {
        XWPFDocument document = new XWPFDocument();
        return document;
    }
    /**
     * 打开word文档
     * @param path 文档所在路径
     * @return
     * @throws IOException
     */
    public XWPFDocument openDoc(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        return new XWPFDocument(is);
    }
    /**
     * 保存word文档
     * @param document 文档对象
     * @param savePath    保存路径
     * @throws IOException
     */
    public void saveDocument(XWPFDocument document, String savePath) throws IOException {
        File file = new File(savePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream os = new FileOutputStream(file);
        document.write(os);
        os.close();
    }
    /**
     * 设置段落文本样式  设置超链接及样式
     * @param paragraph
     * @param textStyle
     * @param url
     */
    public void addParagraphTextHyperlink(XWPFParagraph paragraph, TextStyle textStyle) {
        String id = paragraph.getDocument().getPackagePart().
                addExternalRelationship(textStyle.getUrl(),
                        XWPFRelation.HYPERLINK.getRelation()).getId();
        //追加链接并将其绑定到关系中
        CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
        cLink.setId(id);
        //创建链接文本
        CTText ctText = CTText.Factory.newInstance();
        ctText.setStringValue(textStyle.getText());
        CTR ctr = CTR.Factory.newInstance();
        CTRPr rpr = ctr.addNewRPr();
        //以下设置各种样式 详情看TextStyle类
        if(textStyle.getFontFamily() != "" && textStyle.getFontFamily() != null     ) {
            CTFonts fonts = rpr.isSetRFonts() ? rpr.getRFonts() : rpr.addNewRFonts();
            fonts.setAscii(textStyle.getFontFamily());
            //...
        }
        //设置字体大小
    }
    /**
     * 设置段落的基本样式  设置段落间距信息， 一行 = 100    一磅=20
     * @param paragraph
     * @param paragStyle
     */
    public void setParagraphSpacingInfo(XWPFParagraph paragraph, ParagraphStyle paragStyle, STLineSpacingRule.Enum lineValue) {
        CTPPr pPPr = getParagraphCTPPr(paragraph);
        CTSpacing pSpacing = pPPr.getSpacing() != null ? pPPr.getSpacing() : pPPr.addNewSpacing();
        if(paragStyle.isSpace()) {
            //段前磅数
            if(paragStyle.getBefore() != null) {
                pSpacing.setBefore(new BigInteger(paragStyle.getBefore()));
            }
            //段后磅数
            if(paragStyle.getAfter() != null) {
                pSpacing.setAfter(new BigInteger(paragStyle.getAfter()));
            }
            //依次设置段前行数、段后行数
            //...
        }
        //间距
        if(paragStyle.isLine()) {
            if(paragStyle.getLine() != null) {
                pSpacing.setLine(new BigInteger(paragStyle.getLine()));
            }
            if(lineValue != null) {
                pSpacing.setLineRule(lineValue);    //
            }
        }
    }
    /**
     * 设置段落缩进信息  1厘米 约等于 567
     * @param paragraph
     * @param paragStyle
     */
    public void setParagraphIndInfo(XWPFParagraph paragraph, ParagraphStyle paragStyle) {
        CTPPr pPPr = getParagraphCTPPr(paragraph);
        CTInd pInd = pPPr.getInd() != null ? pPPr.getInd() : pPPr.addNewInd();
        if(paragStyle.getFirstLine() != null) {
            pInd.setFirstLine(new BigInteger(paragStyle.getFirstLine()));
        }
        //再进行其他设置
        //...
    }
    /**
     * 设置段落对齐 方式
     * @param paragraph
     * @param pAlign
     * @param valign
     */
    public void setParagraphAlignInfo(XWPFParagraph paragraph, ParagraphAlignment pAlign, TextAlignment valign) {
        if(pAlign != null) {
            paragraph.setAlignment(pAlign);
        }
        if(valign != null) {
            paragraph.setVerticalAlignment(valign);
        }
    }
    /**
     * 得到段落的CTPPr
     * @param paragraph
     * @return
     */
    public CTPPr getParagraphCTPPr(XWPFParagraph paragraph) {
        CTPPr pPPr = null;
        if(paragraph.getCTP() != null) {
            if(paragraph.getCTP().getPPr() != null) {
                pPPr = paragraph.getCTP().getPPr();
            } else {
                pPPr = paragraph.getCTP().addNewPPr();
            }
        }
        return pPPr;
    }
    /**
     * 得到XWPFRun的CTRPr
     * @param paragraph
     * @param pRun
     * @return
     */
    public CTRPr getRunCTRPr(XWPFParagraph paragraph, XWPFRun pRun) {
        CTRPr ctrPr = null;
        if(pRun.getCTR() != null) {
            ctrPr = pRun.getCTR().getRPr();
            if(ctrPr == null) {
                ctrPr = pRun.getCTR().addNewRPr();
            }
        } else {
            ctrPr = paragraph.getCTP().addNewR().addNewRPr();
        }
        return ctrPr;
    }


    /**
     * 复制表格
     * @param targetTable
     * @param sourceTable
     */
    public void copyTable(XWPFTable targetTable, XWPFTable sourceTable) {
        //复制表格属性
        targetTable.getCTTbl().setTblPr(sourceTable.getCTTbl().getTblPr());
        //复制行
        for(int i = 0; i < sourceTable.getRows().size(); i++) {
            XWPFTableRow targetRow = targetTable.getRow(i);
            XWPFTableRow sourceRow = sourceTable.getRow(i);
            if(targetRow == null) {
                targetTable.addRow(sourceRow);
            } else {
                copyTableRow(targetRow, sourceRow);
            }
        }
    }
    /**
     * 复制单元格
     * @param targetRow
     * @param sourceRow
     */
    public void copyTableRow(XWPFTableRow targetRow, XWPFTableRow sourceRow) {
        //复制样式
        if(sourceRow != null) {
            targetRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
        }
        //复制单元格
        for(int i = 0; i < sourceRow.getTableCells().size(); i++) {
            XWPFTableCell tCell = targetRow.getCell(i);
            XWPFTableCell sCell = sourceRow.getCell(i);
            if(tCell == sCell) {
                tCell = targetRow.addNewTableCell();
            }
            copyTableCell(tCell, sCell);
        }
    }
    /**
     * 复制单元格（列） 从sourceCell到targetCell
     * @param targetCell
     * @param sourceCell
     */
    public void copyTableCell(XWPFTableCell targetCell, XWPFTableCell sourceCell) {
        //表格属性
        if(sourceCell.getCTTc() != null) {
            targetCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());
        }
        //删除段落
        for(int pos = 0; pos < targetCell.getParagraphs().size(); pos++) {
            targetCell.removeParagraph(pos);
        }
        //添加段落
        for(XWPFParagraph sourceParag : sourceCell.getParagraphs()) {
            XWPFParagraph targetParag = targetCell.addParagraph();
            copyParagraph(targetParag, sourceParag);
        }
    }
    /**
     * 复制段落，从sourceParag到targetParag
     * @param targetParag
     * @param sourceParag
     */
    public void copyParagraph(XWPFParagraph targetParag, XWPFParagraph sourceParag) {
        targetParag.getCTP().setPPr(sourceParag.getCTP().getPPr());    //设置段落样式
        //移除所有的run
        for(int pos = targetParag.getRuns().size() - 1; pos >= 0; pos-- ) {
            targetParag.removeRun(pos);
        }
        //copy新的run
        for(XWPFRun sRun : sourceParag.getRuns()) {
            XWPFRun tarRun = targetParag.createRun();
            copyRun(tarRun, sRun);
        }
    }
    /**
     * 复制XWPFRun 从sourceRun到targetRun
     * @param targetRun
     * @param sourceRun
     */
    public void copyRun(XWPFRun targetRun, XWPFRun sourceRun) {
        //设置targetRun属性
        targetRun.getCTR().setRPr(sourceRun.getCTR().getRPr());
        targetRun.setText(sourceRun.text());//设置文本
        //处理图片
        List<XWPFPicture> pictures = sourceRun.getEmbeddedPictures();
        for(XWPFPicture picture : pictures) {
            try {
                copyPicture(targetRun, picture);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
                logger.log(Level.WARNING, "copyRun", e);
            } catch (IOException e) {
                e.printStackTrace();
                logger.log(Level.WARNING, "copyRun", e);
            }
        }
    }
    /**
     * 复制图片从sourcePicture到 targetRun（XWPFPicture --> XWPFRun）
     * @param targetRun
     * @param source
     * @throws IOException
     * @throws InvalidFormatException
     */
    public void copyPicture(XWPFRun targetRun, XWPFPicture sourcePicture) throws InvalidFormatException, IOException {
        XWPFPictureData picData = sourcePicture.getPictureData();
        String fileName = picData.getFileName();    //图片的名称
        InputStream picInIsData = new ByteArrayInputStream(picData.getData());
        int picType = picData.getPictureType();
        int width = (int) sourcePicture.getCTPicture().getSpPr().getXfrm().getExt().getCx();
        int height =  (int) sourcePicture.getCTPicture().getSpPr().getXfrm().getExt().getCy();
        targetRun.addPicture(picInIsData, picType, fileName, width, height);
//        targetRun.addBreak();//分行
    }
}