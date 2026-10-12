package com.leon.be_nobat.data

import org.json.JSONObject
import java.io.IOException

class ApiFailure(val code: String, val status: Int = 0) : IOException(code)

fun JSONObject.objects(key: String = "items"): List<JSONObject> = optJSONArray(key)?.let { array ->
    (0 until array.length()).mapNotNull { array.optJSONObject(it) }
}.orEmpty()

fun JSONObject.strings(key: String): List<String> = optJSONArray(key)?.let { array ->
    (0 until array.length()).map { array.optString(it) }
}.orEmpty()

fun JSONObject.text(key: String): String = optString(key).takeUnless { it == "null" }.orEmpty()