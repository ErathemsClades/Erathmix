package com.example.erathmix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;

public class FileUtil {
    @SuppressLint("NewApi")
    public static String getFullPathFromTreeUri(final Uri uri, Context context) {
        if (uri == null) return null;
        String docId = DocumentsContract.getTreeDocumentId(uri);
        return "/storage/" + docId.replace(":", "/");
    }
}
