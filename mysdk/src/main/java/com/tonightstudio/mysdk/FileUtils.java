package com.tonightstudio.mysdk;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.socks.library.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liyiwei
 * on 2017/8/23.
 */

public class FileUtils {

    /**
     * @param context
     * @return
     */
    public static String getExternalAppDir(Context context) {
        String packageName = context.getPackageName();
        String dir = Environment.getExternalStorageDirectory().getPath() + File.separator + ".Android/data/" + packageName + File.separator;
        return dir;
    }

    /**
     * @param context
     * @return
     */
    public static String getCrashLogDir(Context context) {
        String dir = getExternalAppDir(context) + "crashLog" + File.separator;
        return dir;
    }

    /**
     *
     * @param context
     * @return
     */
    public static String getPrefSetDir(Context context) {
        String dir = getExternalAppDir(context) + "preference" + File.separator;
        return dir;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        int bytesum = 0;
        int byteread;
        File oldfile = new File(oldPath);
        File newFilePath = new File(newPath);
        if (newFilePath.exists()) {
            return true;
        }
        if (!newFilePath.getParentFile().exists()) {
            boolean mkdirs = newFilePath.getParentFile().mkdirs();
        }
        if (!oldfile.exists()) {
            return false;
        }
        try {
            inStream = new FileInputStream(oldPath); //读入原文件
            fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread; //字节数 文件大小
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            return true;
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param imagePath 图片路径
     * @return
     */
    public static String getImageMIMEType(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        String type = options.outMimeType;
        KLog.e("图片类型 image type -> " + type);
        return type;
    }
}