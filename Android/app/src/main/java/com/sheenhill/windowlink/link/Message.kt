package com.sheenhill.windowlink.link

sealed class Message(
    open val key: Int, open val done: Boolean
)

data class FileMessage(
    override val key: Int,
    val fileName: String,
    private val fileSize: Int,
    private val from: Int = 0,
    override val done: Boolean = false
) : Message(key, done) {
    val fromInfo: String
        get() = when (from) {
            0 -> "Android端"
            else -> "电脑端"
        }

    val fileSizeInfo: String
        get() = "${fileSize}K"
}

data class TextMessage(
    override val key: Int, val text: String, override val done: Boolean = false
) : Message(key, done)