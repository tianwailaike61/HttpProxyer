package com.http.plugin

/**
 * @author hongkui.jiang
 * @Date 2020-01-02
 */
interface IClassVisitInterceptor {
    fun intercept(chain: Chain)
    interface Chain {
        fun proceed(data: InterceptorData)
    }
}