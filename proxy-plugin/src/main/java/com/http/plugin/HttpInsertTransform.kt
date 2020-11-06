package com.http.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import java.util.*

class HttpInsertTransform : Transform() {
    override fun getName(): String = "Http"

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return ImmutableSet.of(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        val isIncremental = transformInvocation.isIncremental
        val outputProvider = transformInvocation.outputProvider

        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        val collector = FileCollector[isIncremental]
        for (input in transformInvocation.inputs) {
            collector.traverseDirectory(input.directoryInputs, outputProvider)
            collector.traverseJar(input.jarInputs, outputProvider)
        }
        val manager = VisitorManager(collector)
        manager.setData(InterceptorData())
        manager.start(getSrcInterceptors(), getJarInterceptors())
    }

    private fun getJarInterceptors(): List<IJarClassVisitInterceptor> = Collections.singletonList(
        HttpMockVisitInterceptor()
    )

    private fun getSrcInterceptors(): List<IClassVisitInterceptor> = Collections.emptyList()

}