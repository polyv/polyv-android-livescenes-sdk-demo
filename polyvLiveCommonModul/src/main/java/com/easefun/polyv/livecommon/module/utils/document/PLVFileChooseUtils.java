package com.easefun.polyv.livecommon.module.utils.document;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.webkit.MimeTypeMap;

public class PLVFileChooseUtils {

    public static final int REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT = 0x25;
    public static final String[] SUPPORT_FILE_MIME_TYPES = {MimeType.PPT, MimeType.PPTX, MimeType.PDF, MimeType.DOC, MimeType.DOCX, MimeType.XLS, MimeType.XLSX, MimeType.WPS, MimeType.JPG, MimeType.JPEG, MimeType.PNG};

    public static void openDirChooseFile(Activity activity, int requestCode, String[] mimeTypes) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (mimeTypes != null) {
            if (Build.VERSION.SDK_INT >= 19) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        }
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//多选
        activity.startActivityForResult(intent, requestCode);
    }

    public static void chooseFile(Activity activity, int requestCode) {
        openDirChooseFile(activity, requestCode, SUPPORT_FILE_MIME_TYPES);
    }

    public static boolean isSupportMimeType(String mimeType) {
        for (String supportType : SUPPORT_FILE_MIME_TYPES) {
            if (supportType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static class MimeType {
        public static final String PPT = getMimeTypeFromExtension("ppt");
        public static final String PPTX = getMimeTypeFromExtension("pptx");
        public static final String PDF = getMimeTypeFromExtension("pdf");
        public static final String DOC = getMimeTypeFromExtension("doc");
        public static final String DOCX = getMimeTypeFromExtension("docx");
        public static final String XLS = getMimeTypeFromExtension("xls");//application/x-excel
        public static final String XLSX = getMimeTypeFromExtension("xlsx");
        public static final String WPS = "application/vnd.ms-works";
        public static final String JPG = getMimeTypeFromExtension("jpg");
        public static final String JPEG = getMimeTypeFromExtension("jpeg");
        public static final String PNG = getMimeTypeFromExtension("png");

        private static String getMimeTypeFromExtension(String extension) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
    }
}
