package cn.com.lushunming.models

import kotlinx.serialization.Serializable

@Serializable
data class Download(val url: String, val headers: MutableMap<String, String>, val filename: String)

@Serializable
data class Downloads(val list: MutableList<Download>)
