package com.easefun.polyv.livecommon.module.utils.water;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.easefun.polyv.livecommon.module.utils.imageloader.glide.PLVImageUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;

import java.util.ArrayList;

public class PLVImagePickerUtil {
    private static final int REQUEST_CODE_PICK_IMAGES = 10011;
    private static final int MAX_SELECTION = 10;

    public static void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_CODE_PICK_IMAGES);
    }

    public static void handleActivityResult(Context context, int requestCode, int resultCode, Intent data, ImagePickerCallback callback) {
        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> imagePaths = new ArrayList<>();

            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), MAX_SELECTION);
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    String imagePath = getPathFromUri(context, imageUri);
                    if (imagePath != null) {
                        imagePaths.add(imagePath);
                    }
                }
            } else if (data.getData() != null) { // 单选
                String imagePath = getPathFromUri(context, data.getData());
                if (imagePath != null) {
                    imagePaths.add(imagePath);
                }
            }

            if (callback != null) {
                callback.onImagesSelected(imagePaths);
            }
        }
    }

    private static String getPathFromUri(Context context, Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = PLVImageUtils.transformUriToFilePath(context, uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        } else if (DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if (uri.getAuthority() != null && uri.getAuthority().equals("com.android.providers.media.documents")) {
                String[] split = docId.split(":");
                String selection = MediaStore.Images.Media._ID + "=" + split[1];
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if (uri.getAuthority() != null && uri.getAuthority().equals("com.android.providers.downloads.documents")) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), PLVFormatUtils.parseLong(docId));
                filePath = getDataColumn(context, contentUri, null);
            }
        }
        return filePath;
    }

    private static String getDataColumn(Context context, Uri uri, String selection) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public interface ImagePickerCallback {
        void onImagesSelected(ArrayList<String> imagePaths);
    }
}

