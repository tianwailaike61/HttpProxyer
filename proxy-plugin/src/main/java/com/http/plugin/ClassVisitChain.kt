package com.http.plugin

/**
 * @author hongkui.jiang
 * @Date 2020-01-02
 */
class ClassVisitChain(
    private val interceptorList: List<IClassVisitInterceptor>,
    private val index: Int
) : IClassVisitInterceptor.Chain, Cloneable {
    lateinit var data: InterceptorData

    override fun clone(): ClassVisitChain {
        return ClassVisitChain(interceptorList, 0)
    }

    override fun proceed(data: InterceptorData) {
        val next = ClassVisitChain(interceptorList, index + 1)
        next.data = data
        if (index >= interceptorList.size) {
            return
        }
        val interceptor = interceptorList[index]
        interceptor.intercept(next)
    }
}