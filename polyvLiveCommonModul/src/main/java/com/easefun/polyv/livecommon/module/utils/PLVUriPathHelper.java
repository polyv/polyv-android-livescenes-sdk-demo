package com.easefun.polyv.livecommon.module.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.CloseUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PLVUriPathHelper {
    private static final String TAG = "PLVUriPathHelper";
    public static final String COMPRESS_IMAGE = "compressImage:";

    public static String getFilePathFromURI(Context context, Uri contentUri) {
        File rootDataDir = context.getFilesDir();
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File(rootDataDir + File.separator + fileName);
            copyFile(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copyFile(Context context, Uri srcUri, File dstFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            PLVCommonLog.e(TAG,"copyFile:"+e.getMessage());
        } finally {
            CloseUtils.closeIO(inputStream, outputStream);
        }
    }

    public static int copyStream(InputStream input, OutputStream output) throws IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0;
        int n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
           CloseUtils.closeIO(in,out);
        }
        return count;
    }

    //文件太大会阻塞主线程(2g左右)
    private static String getFilePathForN(Uri uri, Context context) {
        if (uri == null || context == null) {
            PLVCommonLog.w(TAG, "Invalid parameters: uri or context is null.");
            return null;
        }
        String displayName = null;
        Cursor returnCursor = null;
        ParcelFileDescriptor pfd = null; // 引入 ParcelFileDescriptor
        File outputFile = null; // 声明 outputFile
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null && returnCursor.moveToFirst()) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    displayName = returnCursor.getString(nameIndex);
                } else {
                    PLVCommonLog.w(TAG, "Display name column not found for URI: " + uri);
                }
            } else {
                PLVCommonLog.w(TAG, "Cursor is null or empty for URI: " + uri);
            }
            // --- 步骤 1: 安全地确定目标文件名和路径 ---
            String safeFileName;
            if (!TextUtils.isEmpty(displayName)) {
                // 从 display name 中提取安全的文件名部分
                safeFileName = new File(displayName).getName();
                // 可选：进一步验证 safeFileName 只包含允许的字符
                if (!isValidFileName(safeFileName)) {
                    PLVCommonLog.w(TAG, "Sanitized filename is invalid: " + safeFileName);
                    return null;
                }
            } else {
                // 如果没有 display name，生成一个唯一的临时文件名
                safeFileName = "temp_file_" + System.currentTimeMillis();
                PLVCommonLog.i(TAG, "Using generated temp filename: " + safeFileName + " for URI: " + uri);
            }
            // 在应用的私有文件目录下构建目标文件对象
            File targetDir = context.getFilesDir();
            File candidateFile = new File(targetDir, safeFileName);
            // --- 步骤 2: 验证目标路径是否安全 (模拟 saferOpenFile 的核心逻辑) ---
            // 获取私有目录的规范路径
            String canonicalTargetDirPath = targetDir.getCanonicalPath();
            // 获取候选文件路径的规范路径
            String canonicalCandidateFilePath = candidateFile.getCanonicalPath();
            // 检查候选文件的规范路径是否以私有目录的规范路径开头，并且不是私有目录本身
            // 这是一个防止路径遍历的关键步骤
            if (!canonicalCandidateFilePath.startsWith(canonicalTargetDirPath + File.separator)) {
                // 如果路径没有在私有目录内部，或者就是私有目录本身 (这通常是错误的)
                PLVCommonLog.e(TAG, "Path traversal attempt detected: " + canonicalCandidateFilePath);
                return null; // 拒绝不安全的文件路径
            }
            // 如果验证通过，将候选文件对象赋值给 outputFile
            outputFile = candidateFile;
            // --- 步骤 3: 通过文件描述符打开输入流 ---
            pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) {
                PLVCommonLog.w(TAG, "Failed to open ParcelFileDescriptor for URI: " + uri);
                return null;
            }
            // 从 ParcelFileDescriptor 获取 FileInputStream
            inputStream = new FileInputStream(pfd.getFileDescriptor());
            outputStream = new FileOutputStream(outputFile);
            // --- 步骤 4: 复制文件内容 ---
            byte[] buffer = new byte[4096]; // 常用缓冲区大小
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            PLVCommonLog.i(TAG, "Successfully copied file to: " + outputFile.getPath() + ", Size: " + outputFile.length());
        } catch (IOException e) {
            // 捕获 IOException，而不是通用的 Exception
            PLVCommonLog.e(TAG, "IOException during file copy from URI: " + uri + e.getMessage());
            outputFile = null; // 复制失败，不返回文件路径
        } catch (Exception e) {
            // 捕获其他潜在的异常 (如 Cursor 相关的)
            PLVCommonLog.e(TAG, "General error during file processing for URI: " + uri + e.getMessage());
            outputFile = null;
        } finally {
            // --- 步骤 6: 在 finally 块中关闭所有资源 ---
            if (returnCursor != null) {
                try {
                    returnCursor.close();
                } catch (Exception e) {
                    PLVCommonLog.e(TAG, "Error closing cursor" + e.getMessage());
                }
            }
            if (pfd != null) {
                try {
                    // ParcelFileDescriptor 也有 close 方法
                    pfd.close();
                } catch (Exception e) {
                    PLVCommonLog.e(TAG, "Error closing ParcelFileDescriptor" + e.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    PLVCommonLog.e(TAG, "Error closing input stream" + e.getMessage());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    PLVCommonLog.e(TAG, "Error closing output stream" + e.getMessage());
                }
            }
        }
        return (outputFile != null && outputFile.exists()) ? outputFile.getPath() : null;
    }

    private static boolean isValidFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        // 检查是否包含不允许的字符，例如 / \ : * ? " < > |
        // 一个简单的检查：不允许包含路径分隔符
        if (fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }
        return true;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使 need external storage permission
     */
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        PLVFormatUtils.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * * Get the value of the data column for this Uri. This is useful for *
     * MediaStore Uris, and other file-based ContentProviders. * * @param
     * context * The context. * @param uri * The Uri to query. * @param
     * selection * (Optional) Filter used in the query. * @param selectionArgs *
     * (Optional) Selection arguments used in the query. * @return The value of
     * the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            try {
                String path = getFilePathFromURI(context, uri);
                return !TextUtils.isEmpty(path) ? path : getFilePathForN(uri, context);
            } catch (Exception e2) {

            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * * @param uri * The Uri to check. * @return Whether the Uri authority is
     * ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * * @param uri * The Uri to check. * @return Whether the Uri authority is
     * DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * * @param uri * The Uri to check. * @return Whether the Uri authority is
     * MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 根据Uri直接获取图片
     *
     * @param context 上下文
     * @param uri     图片的uri
     */
    public static String getPrivatePath(Context context, Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            File file = compressImage(context, bitmap);
            return file.getAbsolutePath();
        } catch (IOException e) {
            PLVCommonLog.e(TAG,"getPrivatePath:"+e.getMessage());
        }
        return "";
    }

    /**
     * 获取文件名
     */
    public static String getRealFileName(final Context context, final Uri uri) {
        String data = null;
        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME},
                null, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                if (index > -1) {
                    data = cursor.getString(index);
                }
            }
            cursor.close();
        }
        return data;
    }

    /**
     * 把bitmap写入app私有目录下
     *
     * @param context 上下文
     * @param bitmap  这个bitmap不能为null
     * @return File
     * 适配到4.4
     */
    private static File compressImage(Context context, Bitmap bitmap) {
        String filename;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date(System.currentTimeMillis());
            //图片名
            filename = format.format(date);
        } else {
            Date date = new Date();
            filename = date.getYear() + date.getMonth() + date.getDate() + date.getHours() + date.getMinutes() + date.getSeconds() + "";
        }

        final File primaryDir = context.getExternalFilesDir(null);
        PLVCommonLog.e("uri", COMPRESS_IMAGE + primaryDir);
        File file = new File(primaryDir.getAbsolutePath(), filename + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
        } catch (FileNotFoundException e) {
            PLVCommonLog.e(TAG, COMPRESS_IMAGE +e.getMessage());
        } catch (IOException e) {
            PLVCommonLog.e(TAG,COMPRESS_IMAGE+e.getMessage());
        } finally {
           CloseUtils.closeIO(fos);
        }

        return file;
    }
}
