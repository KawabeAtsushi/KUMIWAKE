package com.pandatone.kumiwake.kumiwake;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.pandatone.kumiwake.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by atsushi_2 on 2016/05/31.
 */
public class SaveResultPic {

    public static void savePic(Context context, View view) {

        String folderPath = Environment.getExternalStorageDirectory().getPath()
                + "/KUMIWAKE";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String filepath = Environment.getExternalStorageDirectory().getPath() + "/KUMIWAKE/" + System.currentTimeMillis() + ".jpg";

        File file = new File(filepath);

        file.getParentFile().mkdir();

        view.setDrawingCacheEnabled(true);

        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());


        int w = bmp.getWidth();
        int h = bmp.getHeight();
        float scale = Math.min((float) 1000 / w, (float) 1000 / h);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

        try {

            FileOutputStream fos = new FileOutputStream(file, true);

            bmp2.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();

            fos.close();

            //Toast.makeText(context, MainActivity.getContext().getText(R.string.success_save), Toast.LENGTH_SHORT).show();

            view.setDrawingCacheEnabled(false);

        } catch (Exception e) {

            Toast.makeText(context, MainActivity.getContext().getText(R.string.error), Toast.LENGTH_SHORT).show();

        }

        try {
            // これをしないと、新規フォルダは端末をシャットダウンするまで更新されない
            showFolder(file, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void showFolder(File path, Context context) throws Exception {
        try {
            ContentValues values = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_MODIFIED,
                    System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.SIZE, path.length());
            values.put(MediaStore.Images.Media.TITLE, path.getName());
            values.put(MediaStore.Images.Media.DATA, path.getPath());
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            throw e;
        }
    }
}
