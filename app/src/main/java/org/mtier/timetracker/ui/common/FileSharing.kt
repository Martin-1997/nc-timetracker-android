package org.mtier.timetracker.ui.common

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Mobile has no browser-style "download" — sharing a generated file via the
 * system share sheet (so the user can save it to Files, send it, etc.) is
 * the closest equivalent to the web app's client-side CSV/JSON blob
 * download and the server's download-timeline CSV endpoint.
 */
fun shareFile(
    context: Context,
    fileName: String,
    content: ByteArray,
    mimeType: String,
) {
    val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(sharedDir, fileName)
    file.writeBytes(content)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // The system Sharesheet reads the URI via ClipData (not just the
            // EXTRA_STREAM grant flag) to generate its preview/icon before
            // the user picks a target — without this it fails with
            // "requires the provider be exported, or grantUriPermission()"
            // for uid=1000 (system), even though the flag above is set.
            clipData = ClipData.newRawUri("", uri)
        }
    context.startActivity(Intent.createChooser(intent, fileName))
}
