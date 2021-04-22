package com.easefun.polyv.livecommon.module.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        } finally {
           CloseUtils.closeIO(inputStream,outputStream);
        }
        return file.getPath();
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
            String path = getFilePathFromURI(context, uri);
            return !TextUtils.isEmpty(path) ? path : getFilePathForN(uri, context);
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
