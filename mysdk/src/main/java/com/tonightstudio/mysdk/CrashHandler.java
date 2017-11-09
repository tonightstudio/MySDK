package com.tonightstudio.mysdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 使用：
 * CrashHandler.getInstance().init(this);
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    private long MAX_CRASH_FILE_SIZE = 500 * 1024;//默认最大日志量500KB

    //系统默认的UncaughtException处理类   
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例  
    private static CrashHandler INSTANCE;
    //程序的Context对象  
    private Context mContext;
    //用来存储设备信息和异常信息  
    private Map<String, String> mInfoList = new HashMap<>();

    //用于格式化日期,作为日志文件名的一部分
    private DateFormat mFormatter;//
    private String mPackageName;
    private String mCrashFileDir;//日志默认存储位置/storage/emulated/0/Android/data/<包名>/crash/

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (CrashHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CrashHandler();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mPackageName = context.getPackageName();
        mFormatter = new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.getDefault());
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器  
        Thread.setDefaultUncaughtExceptionHandler(this);

//        String sdRootPath = Environment.getExternalStorageDirectory().getPath();
//        mCrashFileDir = sdRootPath + "/Android/data/" + mPackageName + "/crash";
        mCrashFileDir = FileUtils.getCrashLogDir(context);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理  
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息  
//        new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
//                Looper.loop();
//            }
//        }.start();
        //收集设备参数信息   
        collectDeviceInfo(mContext);
        //保存日志文件
        saveCrashInfo2File(ex);
//        if (BuildConfig.DEBUG) {
        return false;//返回false跳转到系统默认处理方式
//        } else {
//            return true;
//        }
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mPackageName, PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                mInfoList.put("versionName", versionName);
                mInfoList.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            System.out.println("an error occured when collect package info " + e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mInfoList.put(field.getName(), field.get(null).toString());
//                KLog.e(field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                System.out.println("an error occured when collect crash info " + e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : mInfoList.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = mFormatter.format(new Date());
            String fileName = "crash-" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(mCrashFileDir);
                if (!dir.exists()) {
                    boolean mkdirs = dir.mkdirs();
                }
                String crashFilePath = mCrashFileDir + File.separator + fileName;
                System.out.println("崩溃日志路径：" + crashFilePath);
                FileOutputStream fos = new FileOutputStream(crashFilePath);
                fos.write(sb.toString().getBytes());
                fos.close();

                List<File> fileList = Arrays.asList(dir.listFiles());
                //将文件按最后一次修改时间排序，越早的越放List的前面
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        long l = o1.lastModified();
                        long l1 = o2.lastModified();
                        if (l > l1) {
                            return 1;
                        } else if (l < l1) {
                            return -1;
                        }
                        return 0;
                    }
                });

                int loopCount = 0;
                long folderSize = getFolderSize(dir);
                while (folderSize > MAX_CRASH_FILE_SIZE) {
                    System.out.println("日志文件夹已大于最大存储日志量，开始删除早期日志");
                    File file = fileList.get(loopCount);
                    boolean delete = file.delete();
                    loopCount++;
                    folderSize = getFolderSize(dir);
                    System.out.println("删除日志：" + delete + " 当前大小：" + getFormatSize(folderSize));
                }
                Log.i(TAG, "当前日志文件总大小: " + getFormatSize(folderSize));
            }
            return fileName;
        } catch (Exception e) {
            System.out.println("an error occured while writing file..." + e);
        }
        return null;
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    private String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte(s)";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    private long getFolderSize(java.io.File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }
}  