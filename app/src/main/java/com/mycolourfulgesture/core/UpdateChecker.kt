package com.mycolourfulgesture.core

import android.os.Handler
import android.os.Looper
import com.mycolourfulgesture.Constants
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {

    const val CODE_OFFSET = 100

    data class Result(
        val ok: Boolean,
        val latestBuild: Int = 0,
        val tag: String = "",
        val url: String = "",
        val error: String? = null,
    )

    fun check(onDone: (Result) -> Unit) {
        Thread {
            val r = try {
                val conn = URL(Constants.RELEASES_API).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = JSONArray(text)
                if (arr.length() == 0) {
                    Result(false, error = "no releases")
                } else {
                    val o = arr.getJSONObject(0)
                    val tag = o.getString("tag_name")
                    Result(true, tag.removePrefix("build-").toIntOrNull() ?: 0, tag, o.getString("html_url"))
                }
            } catch (t: Throwable) {
                Result(false, error = t.javaClass.simpleName)
            }
            Handler(Looper.getMainLooper()).post { onDone(r) }
        }.start()
    }
}
