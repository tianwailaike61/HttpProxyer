package com.http.plugin

/**
 * @author hongkui.jiang
 * @Date 2020-01-02
 */
class InterceptorData : Cloneable {
    var bytes: ByteArray = ByteArray(0)
    var filePath: String? = null
    public override fun clone(): InterceptorData {
        val data = InterceptorData()
        data.bytes = bytes
        return data
    }
}