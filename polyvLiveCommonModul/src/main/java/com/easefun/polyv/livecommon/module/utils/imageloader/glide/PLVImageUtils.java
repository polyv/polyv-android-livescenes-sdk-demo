package com.easefun.polyv.livecommon.module.utils.imageloader.glide;

import static com.plv.foundationsdk.utils.PLVSugarUtil.clamp;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.chatroom.send.img.PLVSendChatImageHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import okio.Okio;

public class PLVImageUtils {

    private static final String TAG = PLVImageUtils.class.getSimpleName();

    @Nullable
    public static String transformUriToFilePath(Context context, Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            return uri.getPath();
        }
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final ContentResolver contentResolver = context.getContentResolver();
            final String fileName = "Atemp_" + System.currentTimeMillis() + "_" + Math.abs(new Random().nextInt())
                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

            InputStream input = null;
            try {
                input = contentResolver.openInputStream(uri);
                File outFile = new File(context.getCacheDir(), fileName);
                if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                outFile.createNewFile();
                Okio.buffer(Okio.sink(outFile)).writeAll(Okio.source(input));
                return outFile.getAbsolutePath();
            } catch (IOException e) {
                PLVCommonLog.exception(e);
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                }
            }
        }
        return null;
    }

    public static String compressImage(Context context, String sourcePath) {
        final File sourceFile = new File(sourcePath);
        if (!sourceFile.canRead()) {
            return sourcePath;
        }
        if (sourceFile.length() < PLVSendChatImageHelper.ALLOW_LENGTH) {
            try {
                final String imageType = PLVSendChatImageHelper.getImageType(sourceFile);
                if ("jpeg".equalsIgnoreCase(imageType) || "jpg".equalsIgnoreCase(imageType)) {
                    return sourcePath;
                }
            } catch (FileNotFoundException e) {
                // ignore
            }
        }

        String compressOutput = compressOnce(context, sourcePath, 100);
        if (compressOutput == null) {
            return sourcePath;
        }
        if (new File(compressOutput).length() < PLVSendChatImageHelper.ALLOW_LENGTH) {
            return compressOutput;
        }
        compressOutput = compressOnce(context, sourcePath, calculateNextQuality(new File(compressOutput).length()));
        if (compressOutput == null) {
            return sourcePath;
        }
        return compressOutput;
    }

    public static String fixImageUrl(String origin) {
        if (TextUtils.isEmpty(origin)) {
            return origin;
        }
        if (origin.startsWith("//")) {
            return "http:" + origin;
        } else if (origin.startsWith("/")) {
            return "http://livestatic.videocc.net" + origin;
        } else {
            return origin;
        }
    }

    private static String compressOnce(Context context, String sourcePath, int quality) {
        final Bitmap sourceBitmap = BitmapFactory.decodeFile(sourcePath);
        final String compressOutputFileName = "Atemp_" + System.currentTimeMillis() + "_" + Math.abs(new Random().nextInt()) + ".jpg";
        final File compressOutputFile = new File(context.getCacheDir(), compressOutputFileName);
        OutputStream outputStream = null;
        try {
            if (compressOutputFile.getParentFile() != null && !compressOutputFile.getParentFile().exists()) {
                compressOutputFile.getParentFile().mkdirs();
            }
            compressOutputFile.createNewFile();
            outputStream = new FileOutputStream(compressOutputFile);
            boolean res = sourceBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            if (!res) {
                PLVCommonLog.w(TAG, "compress failed, will return sourcePath");
                return sourcePath;
            } else {
                return compressOutputFile.getAbsolutePath();
            }
        } catch (IOException e) {
            PLVCommonLog.exception(e);
        } finally {
            sourceBitmap.recycle();
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                PLVCommonLog.exception(e);
            }
        }
        return null;
    }

    private static final long COMPRESS_TARGET_SIZE_IN_KB = 1800;

    private static int calculateNextQuality(long fileSizeCompressed) {
        long fileLengthInKB = fileSizeCompressed / 1024;
        if (fileLengthInKB >= 19000) {
            return 20;
        }
        return clamp((int) (106.847 - (6.188 * fileLengthInKB) / (COMPRESS_TARGET_SIZE_IN_KB - 0.091 * fileLengthInKB)), 20, 100);
    }

}
