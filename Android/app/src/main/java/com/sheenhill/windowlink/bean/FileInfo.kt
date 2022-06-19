package com.sheenhill.windowlink.bean

data class FileInfo(
    val path: String = "",
    val size: Int = 0
) {
    val name: String
        get() = if (path.isNotEmpty()) {
            path.substring(startIndex = 1 + path.indexOfLast { it == '/' })
        } else "未识别"
}