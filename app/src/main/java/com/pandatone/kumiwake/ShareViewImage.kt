package com.pandatone.kumiwake

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object ShareViewImage {

    fun shareView(context: Context, view: View, message: String) {
        writeBitmapExtarnalStorage(context, view)

        // ファイル保存先 (SDカード)
        val fileFullPath = getShareImageFilePath(context)

        // ファイルを示すインテントを作成する
        val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                File(fileFullPath))
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png" // PNG image
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        // 外部アプリを起動する
        try {
            context.startActivity(Intent.createChooser(intent, null)) // 常に送信先を選択させる
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun getShareImageFilePath(context: Context): String {
        val fname = "/result.png"
        // ファイル保存先 (SDカード)
        return (context.getExternalFilesDir(null).toString() + fname)
    }

    /**
     * SDカードにbitmapを保存する
     */
    private fun writeBitmapExtarnalStorage(context: Context, view: View) {
        // ファイル保存先 (SD)
        val fileFullPath = getShareImageFilePath(context)
        val out = FileOutputStream(fileFullPath)
        try {
            saveViewCapture(view, out)
        } catch (ex: FileNotFoundException) {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
        } catch (ex: IOException) {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
        } finally {
            try {
                out.close()
            } catch (ex: IOException) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * キャプチャを撮る
     * @param　撮りたいView
     * @return 撮ったキャプチャ(Bitmap)
     */
    private fun saveViewCapture(view: View, output: FileOutputStream) {
        // Bitmap生成
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
    }
}