package com.maximeproulx.watchverse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object ArtworkRepository {
    private const val MAX_ARTWORK_BYTES = 25L * 1024L * 1024L
    private val memoryCache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    suspend fun load(context: Context, source: String): Bitmap? {
        if (!source.startsWith("artwork/")) {
            return null
        }

        memoryCache.get(source)?.let { bitmap -> return bitmap }

        val cacheFile = cacheFile(context, source)
        val diskBitmap = withContext(Dispatchers.IO) {
            if (cacheFile.exists()) BitmapFactory.decodeFile(cacheFile.path) else null
        }

        if (diskBitmap != null) {
            memoryCache.put(source, diskBitmap)
            return diskBitmap
        }

        val bytes = FirebaseStorage.getInstance()
            .reference
            .child(source)
            .getBytes(MAX_ARTWORK_BYTES)
            .awaitResult()

        val bitmap = withContext(Dispatchers.Default) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } ?: return null

        withContext(Dispatchers.IO) {
            cacheFile.parentFile?.mkdirs()
            cacheFile.writeBytes(bytes)
        }

        memoryCache.put(source, bitmap)
        return bitmap
    }

    suspend fun preload(context: Context, source: String) {
        try {
            load(context, source)
        } catch (error: Exception) {
            android.util.Log.w(
                "WatchVerseArtwork",
                "Artwork preload failed for $source",
                error
            )
        }
    }

    private fun cacheFile(context: Context, source: String): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }

        return File(context.cacheDir, "watchverse-artwork/$digest")
    }
}

@Composable
fun ArtworkImage(
    source: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    @DrawableRes placeholder: Int = R.drawable.placeholder_poster
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = source
    ) {
        value = try {
            ArtworkRepository.load(context.applicationContext, source)
        } catch (error: Exception) {
            android.util.Log.w(
                "WatchVerseArtwork",
                "Artwork load failed for $source",
                error
            )
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter
        )
    } else {
        Image(
            painter = painterResource(placeholder),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter
        )
    }
}

private suspend fun <T> Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener { continuation.cancel() }
    }
}
