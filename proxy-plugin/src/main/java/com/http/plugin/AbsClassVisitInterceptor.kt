package com.http.plugin

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

/**
 * @author hongkui.jiang
 * @Date 2020-01-02
 */
abstract class AbsClassVisitInterceptor<T : ClassVisitor> : IClassVisitInterceptor {
    override fun intercept(chain: IClassVisitInterceptor.Chain) {
        val classVisitChain = chain as ClassVisitChain
        val data = classVisitChain.data
        val cv = doVisit(data, classVisitChain)
        interceptNext(chain, data, cv)
    }

    private fun doVisit(data: InterceptorData, classVisitChain: ClassVisitChain): T {
        val cr = ClassReader(data.bytes)
        val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        val cv = createClassVisitor(cw, data)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        updateData(data, cw.toByteArray(), cv)
        return cv
    }

    private fun interceptNext(
        chain: IClassVisitInterceptor.Chain,
        data: InterceptorData,
        t: T
    ) {
        chain.proceed(data)
    }

    private fun updateData(data: InterceptorData, bytes: ByteArray, t: T) {
        data.bytes = bytes
    }

    protected abstract fun createClassVisitor(
        classVisitor: ClassVisitor,
        data: InterceptorData
    ): T
}