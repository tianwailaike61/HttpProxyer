package com.http.plugin

/**
 * @author hongkui.jiang
 * @Date 2020/6/30
 */
interface IJarClassVisitInterceptor : IClassVisitInterceptor {
    fun filterInfo(s: String?): Boolean
}