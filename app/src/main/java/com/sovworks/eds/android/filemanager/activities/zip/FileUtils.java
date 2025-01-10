package com.sovworks.eds.android.filemanager.activities.zip;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.appcompat.content.res.AppCompatResources;

import com.sovworks.eds.android.R;
import com.sovworks.eds.fs.Path;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件工具
 *
 * @author Andy.R
 */
public class FileUtils {

    /**
     * QQ文件夹
     **/
    public final static String QQ_FILES = "/Tencent/QQfile_recv/";
    /**
     * 微信文件夹
     **/
    public final static String WEIXIN_FILES = "/Tencent/MicroMsg/Download/";
    /**
     * 临时文件夹(隐藏目录，存储在安装包名下)
     **/
    public final static String DEFAULT_TEMP = "/.Temp/";

    private static final Map<String, Integer> FILE_TYPE_ICON_MAP = new HashMap<>();

    static {
        // 初始化文件类型与图标资源的映射关系
        FILE_TYPE_ICON_MAP.put("log", R.drawable.ic_file);
        FILE_TYPE_ICON_MAP.put("txt", R.drawable.ic_file);
        FILE_TYPE_ICON_MAP.put("config", R.drawable.ic_file);
        // 代码
        FILE_TYPE_ICON_MAP.put("json", R.drawable.ic_file_code);
        FILE_TYPE_ICON_MAP.put("c", R.drawable.ic_file_code);
        FILE_TYPE_ICON_MAP.put("java", R.drawable.ic_file_code);
        FILE_TYPE_ICON_MAP.put("ts", R.drawable.ic_file_code);
        FILE_TYPE_ICON_MAP.put("js", R.drawable.ic_file_code);
        FILE_TYPE_ICON_MAP.put("kt", R.drawable.ic_file_code);
        // 图片
        FILE_TYPE_ICON_MAP.put("gif", R.drawable.ic_file_image);
        FILE_TYPE_ICON_MAP.put("png", R.drawable.ic_file_image);
        FILE_TYPE_ICON_MAP.put("jpg", R.drawable.ic_file_image);
        FILE_TYPE_ICON_MAP.put("jpeg", R.drawable.ic_file_image);
        FILE_TYPE_ICON_MAP.put("bmp", R.drawable.ic_file_image);
        FILE_TYPE_ICON_MAP.put("heic", R.drawable.ic_file_image);
        // 视频
        FILE_TYPE_ICON_MAP.put("mp4", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("avi", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("wmv", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("mkv", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("mov", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("rm", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("rmvb", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("3gp", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("flv", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("mpg", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("swf", R.drawable.ic_file_video);
        FILE_TYPE_ICON_MAP.put("vob", R.drawable.ic_file_video);
        // 音频
        FILE_TYPE_ICON_MAP.put("mp3", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("wav", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("aac", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("flac", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("ogg", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("wma", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("aiff", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("alac", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("m4a", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("m4r", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("m4b", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("mp2", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("mpeg", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("ra", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("ram", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("wv", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("ac3", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("dts", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("amr", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("caf", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("mid", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("midi", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("xmf", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("mxmf", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("imy", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("rtttl", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("ota", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("rtx", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("smf", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("mmf", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("cmx", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("mka", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("au", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("snd", R.drawable.ic_file_audio);
        FILE_TYPE_ICON_MAP.put("sd2", R.drawable.ic_file_audio);
        // 办公文档
        FILE_TYPE_ICON_MAP.put("doc", R.mipmap.file_icon_doc);
        FILE_TYPE_ICON_MAP.put("docx", R.mipmap.file_icon_docx);
        FILE_TYPE_ICON_MAP.put("xls", R.drawable.ic_file_excel);
        FILE_TYPE_ICON_MAP.put("xlsx", R.drawable.ic_file_excel);
        FILE_TYPE_ICON_MAP.put("ppt", R.mipmap.file_icon_ppt);
        FILE_TYPE_ICON_MAP.put("pptx", R.mipmap.file_icon_pptx);
        FILE_TYPE_ICON_MAP.put("pdf", R.drawable.ic_file_pdf);
        // 压缩文件
        FILE_TYPE_ICON_MAP.put("zip", R.drawable.ic_file_zip);
        FILE_TYPE_ICON_MAP.put("rar", R.mipmap.file_icon_rar);

        FILE_TYPE_ICON_MAP.put("dwg", R.mipmap.file_icon_dwg);
        FILE_TYPE_ICON_MAP.put("dws", R.mipmap.file_icon_dws);
        FILE_TYPE_ICON_MAP.put("dwt", R.mipmap.file_icon_dwt);
        FILE_TYPE_ICON_MAP.put("dxf", R.mipmap.file_icon_dxf);
        FILE_TYPE_ICON_MAP.put("ocf", R.mipmap.file_icon_ocf);
        FILE_TYPE_ICON_MAP.put("ttf", R.mipmap.file_icon_ttf);
        FILE_TYPE_ICON_MAP.put("ttc", R.mipmap.file_icon_ttc);
        FILE_TYPE_ICON_MAP.put("shx", R.mipmap.file_icon_shx);
        FILE_TYPE_ICON_MAP.put("sht", R.mipmap.file_icon_sht);
        FILE_TYPE_ICON_MAP.put("shp", R.mipmap.file_icon_shp);
        FILE_TYPE_ICON_MAP.put("fon", R.mipmap.file_icon_fon);

        FILE_TYPE_ICON_MAP.put("tif", R.mipmap.file_icon_tif);
        FILE_TYPE_ICON_MAP.put("rtf", R.mipmap.file_icon_rtf);
        FILE_TYPE_ICON_MAP.put("jww", R.mipmap.file_icon_jww);


    }

    /**
     * 根据文件扩展名,获取文件图标
     */
    public static Drawable getFileIcon(Context context, Path path) {
        if (path == null || path.getPathString() == null) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_file_unknown);
        }

        String fileType = getFileExtensionNoPoint(path.getPathString()).toLowerCase();
        Integer resId = FILE_TYPE_ICON_MAP.getOrDefault(fileType, R.drawable.ic_file_unknown);
        if (resId != null) {
            return AppCompatResources.getDrawable(context, resId);
        }
        return AppCompatResources.getDrawable(context, R.drawable.ic_file_unknown);
    }

    /**
     * 转换文件大小单位(KB/MB/GB)
     *
     * @param fileSize 转换文件大小
     */
    public static String formatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("0.00");
        String fileSizeString;
        if (fileSize <= 0) {
            fileSizeString = "0KB";
        } else if (fileSize < (1024 * 1024)) {
            fileSizeString = df.format((double) fileSize / 1024) + "KB";
        } else if (fileSize < (1024 * 1024 * 1024)) {
            fileSizeString = df.format((double) fileSize / (1024 * 1024)) + "MB";
        } else {
            fileSizeString = df.format((double) fileSize / (1024 * 1024 * 1024)) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取文件扩展名(不包含前面那个点 ‘.’)
     *
     * @param filePath .
     */
    private static String getFileExtensionNoPoint(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        // 处理多点文件扩展名
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastDotIndex < lastSlashIndex) {
            return "";
        }
        return filePath.substring(lastDotIndex + 1);
    }

    /**
     * 获取文件扩展名(不包含前面那个点 ‘.’)
     *
     * @param file .
     */
    private static String getFileExtensionNoPoint(File file) {
        if (file == null || file.isDirectory()) {
            return "";
        }
        String fileName = file.getName();
        if (fileName != null && fileName.length() > 0) {
            int lastIndex = fileName.lastIndexOf('.');
            if ((lastIndex > -1) && (lastIndex < (fileName.length() - 1))) {
                return fileName.substring(lastIndex + 1);
            }
        }
        return "";
    }


    /**
     * 获取文件icon
     */
    public static int getFileIcon(boolean isDir, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            return getFileIcon(isDir, new File(filePath));
        } else {
            return 0;
        }
    }


    /**
     * 比较两个文件是否相同
     *
     * @return true 相同,false 不同
     */
    private static boolean isCompareFiles(String path1, String path2) {
        if (TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2)) {
            return false;
        }
        if (path1.equalsIgnoreCase(path2)) {
            return true;
        } else {
            return isCompareFiles(new File(path1), new File(path2));
        }
    }

    /**
     * 比较两个文件是否相同
     *
     * @return true 相同,false 不同
     */
    private static boolean isCompareFiles(File file1, File file2) {
        if (file1 == null || file2 == null) {
            return false;
        }
        if (file1.getPath().equalsIgnoreCase(file2.getPath())) {
            return true;
        }
        return false;
    }

    /**
     * 获取文件icon
     */
    private static int getFileIcon(boolean isDir, File file) {
        int resId = 0;
        if (isDir) {
            if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_QQ())) {
                resId = R.mipmap.file_icon_folder_qq;
            } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_WeiXin())) {
                resId = R.mipmap.file_icon_folder_weixin;
            } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardDownloadPath())) {
                resId = R.mipmap.file_icon_folder_download;
            } else {
                resId = R.mipmap.file_icon_folder;
            }
            return resId;
        }
        if (file != null) {
            String fileType = getFileExtensionNoPoint(file);
            if (TextUtils.isEmpty(fileType) || file.isDirectory()) {
                if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_QQ())) {
                    resId = R.mipmap.file_icon_folder_qq;
                } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardFilesPath_WeiXin())) {
                    resId = R.mipmap.file_icon_folder_weixin;
                } else if (FileUtils.isCompareFiles(file.getPath(), FileUtils.getSDCardDownloadPath())) {
                    resId = R.mipmap.file_icon_folder_download;
                } else {
                    resId = R.mipmap.file_icon_folder;
                }
            } else if (fileType.equalsIgnoreCase("dwg")) {
                resId = R.mipmap.file_icon_dwg;
            } else if (fileType.equalsIgnoreCase("dws")) {
                resId = R.mipmap.file_icon_dws;
            } else if (fileType.equalsIgnoreCase("dwt")) {
                resId = R.mipmap.file_icon_dwt;
            } else if (fileType.equalsIgnoreCase("dxf")) {
                resId = R.mipmap.file_icon_dxf;
            } else if (fileType.equalsIgnoreCase("ocf")) {
                resId = R.mipmap.file_icon_ocf;
            } else if (fileType.equalsIgnoreCase("ttf")) {
                resId = R.mipmap.file_icon_ttf;
            } else if (fileType.equalsIgnoreCase("ttc")) {
                resId = R.mipmap.file_icon_ttc;
            } else if (fileType.equalsIgnoreCase("shx")) {
                resId = R.mipmap.file_icon_shx;
            } else if (fileType.equalsIgnoreCase("sht")) {
                resId = R.mipmap.file_icon_sht;
            } else if (fileType.equalsIgnoreCase("shp")) {
                resId = R.mipmap.file_icon_shp;
            } else if (fileType.equalsIgnoreCase("fon")) {
                resId = R.mipmap.file_icon_fon;
            } else if (fileType.equalsIgnoreCase("pdf")) {
                resId = R.mipmap.file_icon_pdf;
            } else if (fileType.equalsIgnoreCase("gif")) {
                resId = R.mipmap.file_icon_gif;
            } else if (fileType.equalsIgnoreCase("png")) {
                resId = R.mipmap.file_icon_png;
            } else if (fileType.equalsIgnoreCase("jpg")) {
                resId = R.mipmap.file_icon_jpg;
            } else if (fileType.equalsIgnoreCase("jpeg")) {
                resId = R.mipmap.file_icon_jpg;
            } else if (fileType.equalsIgnoreCase("bmp")) {
                resId = R.mipmap.file_icon_bmp;
            } else if (fileType.equalsIgnoreCase("doc")) {
                resId = R.mipmap.file_icon_doc;
            } else if (fileType.equalsIgnoreCase("docx")) {
                resId = R.mipmap.file_icon_docx;
            } else if (fileType.equalsIgnoreCase("xls")) {
                resId = R.mipmap.file_icon_xls;
            } else if (fileType.equalsIgnoreCase("xlsx")) {
                resId = R.mipmap.file_icon_xlsx;
            } else if (fileType.equalsIgnoreCase("ppt")) {
                resId = R.mipmap.file_icon_ppt;
            } else if (fileType.equalsIgnoreCase("pptx")) {
                resId = R.mipmap.file_icon_pptx;
            } else if (fileType.equalsIgnoreCase("txt")) {
                resId = R.mipmap.file_icon_txt;
            } else if (fileType.equalsIgnoreCase("tif")) {
                resId = R.mipmap.file_icon_tif;
            } else if (fileType.equalsIgnoreCase("rtf")) {
                resId = R.mipmap.file_icon_rtf;
            } else if (fileType.equalsIgnoreCase("jww")) {
                resId = R.mipmap.file_icon_jww;
            } else if (fileType.equalsIgnoreCase("rar")) {
                resId = R.mipmap.file_icon_rar;
            } else if (fileType.equalsIgnoreCase("zip")) {
                resId = R.mipmap.file_icon_zip;
            } else {
                resId = R.mipmap.file_icon_other;
            }
        }
        return resId;
    }

    /**
     * 获取SD卡根目下的QQ接收文件路径
     */
    private static String getSDCardFilesPath_QQ() {
        if (!isSDExist()) {
            return "";
        }
        String qqFilePath = getSDCardFilesPath() + QQ_FILES;
        if (checkFileExists(qqFilePath)) {
            return qqFilePath;
        } else {
            return "";
        }
    }

    /**
     * 获取SD卡根目下的微信接收文件路径
     */
    private static String getSDCardFilesPath_WeiXin() {
        if (!isSDExist()) {
            return "";
        }
        String qqFilePath = getSDCardFilesPath() + WEIXIN_FILES;
        if (checkFileExists(qqFilePath)) {
            return qqFilePath;
        } else {
            return "";
        }
    }

    /**
     * 检测SD卡是否存在
     */
    private static boolean isSDExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return e.g. /storage/sdcard0/
     */
    public static String getSDCardFilesPath() {
        if (!isSDExist()) {
            return "";
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    /**
     * 获取SD卡根目下的下载目录路径
     *
     * @return e.g. /storage/sdcard0/Download/
     */
    private static String getSDCardDownloadPath() {
        if (!isSDExist()) {
            return "";
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";
    }


    /**
     * 判断SD卡中给定位置的文件是否存在
     *
     * @param strURL
     * @return true 存在 false 不存在
     */
    private static Boolean checkFileExists(String strURL) {
        if (strURL == null || "".equals(strURL)) {
            return false;
        }
        File file = new File(strURL);
        boolean result = true;
        if (FileUtils.isSDExist()) {
            // 判断文件是否存在
            result = file.exists();
        }
        return result;
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param filePath
     * @return
     */
    public static String getFileNameNoExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        return getFileNameNoExtension(new File(filePath));
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param file
     * @return
     */
    public static String getFileNameNoExtension(File file) {
        if (file == null) {
            return "";
        }
        String filename = file.getName();
        if (!TextUtils.isEmpty(filename)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 检测文件是否存在
     *
     * @param path
     * @return
     */
    private static boolean isFileExist(String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                return file.exists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 调用当前app打开文件
     *
     * @param context
     * @param filePath 文件路径
     */
    /*public static void openFileByApp(Context context, String filePath) {
        try {
            if (!TextUtils.isEmpty(filePath) && isFileExist(filePath)) {
                openFileBySystemApp(context, new File(filePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 根据文件mimeType调用系统相关程序打开
     *
     * @param context
     * @param file
     */
    /*private static void openFileBySystemApp(Context context, File file) {
        try {
            if (file != null && file.exists()) {
                String mimeType = getFileMimeTypeFromExtension(getFileExtensionNoPoint(file));
                Intent intent = new Intent();
                // 设置intent的Action属性
                intent.setAction(Intent.ACTION_VIEW);
                // 设置intent的data和Type属性。
                FileProvider7.setIntentDataAndType(context, intent, mimeType, file, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * 获取文件的mimeType
     *
     * @param fileType
     * @return
     */
    private static String getFileMimeTypeFromExtension(String fileType) {
        try {
            if (TextUtils.isEmpty(fileType)) {
                return "*/*";
            }
            fileType = fileType.replace(".", "");
            if (fileType.equalsIgnoreCase("docx") || fileType.equalsIgnoreCase("wps")) {
                fileType = "doc";
            } else if (fileType.equalsIgnoreCase("xlsx")) {
                fileType = "xls";
            } else if (fileType.equalsIgnoreCase("pptx")) {
                fileType = "ppt";
            }
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            if (mimeTypeMap.hasExtension(fileType)) {
                // 获得文件类型的MimeType
                return mimeTypeMap.getMimeTypeFromExtension(fileType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "*/*";
    }
}
