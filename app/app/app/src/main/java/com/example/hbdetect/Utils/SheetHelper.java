package com.example.hbdetect.Utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SheetHelper {
    static String TAG = "<<< SheetHelper >>>";

    /**
     * 导出Excel
     *
     * @param title           标题，配合 DeviceInfo 按需传入
     * @param listData        导出行数据
     * @param fileDir         导出文件夹
     * @param fileName        导出文件名
     * @param context         activity上下文
     * @param fileNameReplace 文件名称存在时，是否需要替换
     * @return
     */
    public static boolean exportExcel(String[] title, List<Patient> listData, String fileDir, String fileName, Context context, boolean fileNameReplace,DatabaseHelper db) {
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName) || listData == null) {
            Log.e(TAG, " 导出" + "入参不合规");
            return false;
        }
        try {
            // 创建excel xlsx格式
            Workbook wb = new XSSFWorkbook();
            // 创建工作表
            Sheet sheet = wb.createSheet();
            //创建行对象
            Row row = sheet.createRow(0);
            // 设置有效数据的行数和列数
            int colNum = title.length;
            for (int i = 0; i < colNum; i++) {
                sheet.setColumnWidth(i, 20 * 256);  // 显示20个字符的宽度  列宽
                Cell cell1 = row.createCell(i);
                //第一行
                cell1.setCellValue(title[i]);
            }
            int rowCounter = 1;
            //String[] title = {"病历号", "姓名", "年龄", "性别", "科室", "床号", "拍摄次数","拍摄时间","预测值","真实值"};
            for (int rowNum = 0; rowNum < listData.size(); rowNum++) {

                // 之所以rowNum + 1 是因为要设置第二行单元格

                // DeviceInfo 这个是我的业务类，这个是根据业务来进行填写数据
                Patient bean = listData.get(rowNum);
                List<Entity> entities = db.selectEntities(bean.getCase_id());
                for (Entity entity:entities
                     ) {
                    row = sheet.createRow(rowCounter);
                    rowCounter ++;
                    // 设置单元格显示宽度
                    row.setHeightInPoints(28f);
                    for (int j = 0; j < title.length; j++) {
                        Cell cell = row.createCell(j);

                        //要和title[]一一对应
                        switch (j) {
                            case 0:
                                //病历号
                                cell.setCellValue(bean.getCase_id());
                                break;
                            case 1:
                                //姓名
                                cell.setCellValue(bean.getName());
                                break;
                            case 2:
                                //年龄
                                cell.setCellValue(bean.getAge());
                                break;
                            case 3:
                                //性别
                                cell.setCellValue(bean.getGender());
                                break;
                            case 4:
                                //科室
                                cell.setCellValue(bean.getDeparments());
                                break;
                            case 5:
                                //床位
                                cell.setCellValue(bean.getBed_id());
                                break;
                            case 6:
                                //拍摄号
                                cell.setCellValue(entity.getTake());
                                break;
                            case 7:
                                //拍摄时间
                                cell.setCellValue(entity.getTime());
                                break;
                            case 8:
                                //预测值
                                cell.setCellValue(entity.getMchc());
                                break;
                            case 9:
                                //真实值
                                cell.setCellValue(entity.getMchc_real());
                                break;
                        }
                    }
                }
            }

            String s = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + fileDir;
            File dir = new File(s);
            //判断文件是否存在
            if (!dir.exists()) {
                //不存在则创建
                dir.mkdirs();
            }
            //获取当前时间
            // 获取当前时间
            Date currentDate = new Date();

            // 定义日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault());

            // 格式化当前时间
            String formattedDate = dateFormat.format(currentDate);
            File excel = new File(dir, fileName+formattedDate + ".xlsx");
            if (!excel.exists()) {
                excel.createNewFile();
            } else {
                if (fileNameReplace) {
                    String newFileName = getXlsxNewFileName(excel);
                    excel = new File(newFileName);
                    excel.createNewFile();
                }
            }
            Log.e(TAG, " 导出路径" + excel.getPath().toString());

            FileOutputStream fos = new FileOutputStream(excel);
            wb.write(fos);
            wb.close();
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e("ExpressExcle", "exportExcel", e);
            return false;
        }
    }

    public static boolean initPath(String fileDir, Context context) {
        String fileName = "ResultsSavedHere";
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName)) {
            Log.e(TAG, " 导出" + "入参不合规");
            return false;
        }
        try {
            String s = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + fileDir;
            File dir = new File(s);
            //判断文件是否存在
            if (!dir.exists()) {
                //不存在则创建
                dir.mkdirs();
            }
            File excel = new File(dir, fileName + ".txt");
            if (!excel.exists()) {
                excel.createNewFile();
            }
            return true;
        } catch (IOException e) {
            Log.e("ExpressExcle", "exportExcel", e);
            return false;
        }
    }
    private static String getXlsxNewFileName(File file) {
        if (file.exists()) {
            String newPath = file.getPath().substring(0, file.getPath().length() - 5) + "(1).xlsx";
            return getXlsxNewFileName(new File(newPath));
        } else {
            return file.getPath();
        }
    }


}