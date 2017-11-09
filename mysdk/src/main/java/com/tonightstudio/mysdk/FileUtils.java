package com.tonightstudio.mysdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liyiwei
 * on 2017/8/23.
 */

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * @param context
     * @return /storage/emulated/0/Android/data/<包名>
     */
    public static String getExternalAppDir(Context context) {
        String packageName = context.getPackageName();//com.tonightstudio.sdktestdemo
        String dir = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + packageName;
        return dir;
    }

    /**
     * @param context
     * @return /storage/emulated/0/Android/data/<包名>/crash
     */
    public static String getCrashLogDir(Context context) {
        String dir = getExternalAppDir(context) + "/crash";//
        return dir;
    }

    /**
     * @param context
     * @return /storage/emulated/0/Android/data/<包名>/preference
     */
    public static String getPrefSetDir(Context context) {
        String dir = getExternalAppDir(context) + "/preference";
        return dir;
    }

    public static String getTempDir(Context context) {
        String dir = getExternalAppDir(context) + "/temp";
        return dir;
    }

    /**
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * 复制单个文件
     * 使用：
     * String sdRootDir = Environment.getExternalStorageDirectory().getPath();
     * String oldPath = sdRootDir + "/qrcode.jpg";
     * String newPath = sdRootDir + "/zzzz/qrcode.jpg";
     * boolean b = FileUtils.copyFile(oldPath, newPath);
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        File oldfile = new File(oldPath);
        if (!oldfile.exists()) {
            return false;
        }

        File newFilePath = new File(newPath);
        if (newFilePath.exists()) {
            return true;
        }
        if (!newFilePath.getParentFile().exists()) {
            boolean mkdirs = newFilePath.getParentFile().mkdirs();
        }
        InputStream inStream = null;
        FileOutputStream fs = null;
        int bytesum = 0;
        int byteread;

//        try {
        try {
            inStream = new FileInputStream(oldPath); //读入原文件

            fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread; //字节数 文件大小
                Log.i(TAG, String.valueOf(bytesum));
                fs.write(buffer, 0, byteread);
            }
            Log.i(TAG, "复制单个文件操作成功");
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "文件未找到");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "复制单个文件操作出错");
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
        return false;
    }

    /**
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     *
     * @param imagePath 图片路径
     * @return image/jpeg
     */
    public static String getImageMIMEType(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        String type = options.outMimeType;
        Log.i(TAG, "图片类型 image type -> " + type);
        return type;
    }

    /**
     * @param context 上下文
     * @param path    图片路径
     */
    public static void shareImageByIntent(Context context, String path) {
        int end = path.lastIndexOf("/");
        String fileName = path.substring(end + 1, path.length());//获取图片文件名
        String imageMIMEType = FileUtils.getImageMIMEType(path);//获取图片mimeType
        String newPath = FileUtils.getTempDir(context) + File.separator + fileName;

        boolean copyFile = FileUtils.copyFile(path, newPath);//直接分享源文件，会提示“获取资源失败”
        if (copyFile) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(newPath)));
            intent.setType(imageMIMEType);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}