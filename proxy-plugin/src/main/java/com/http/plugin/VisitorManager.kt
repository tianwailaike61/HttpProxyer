package com.http.plugin

import com.http.plugin.FileCollector.FileTraverseListener
import com.http.plugin.FileUtils.getDataFromFile
import java.io.File

class VisitorManager(private val collector: FileCollector?) : FileTraverseListener {
    private lateinit var data: InterceptorData
    private var srcInterceptors: List<IClassVisitInterceptor>? = null
    private var jarInterceptors: List<IJarClassVisitInterceptor>? = null
    private var jarFilter: JarVisitFilter? = null
    fun setData(data: InterceptorData) {
        this.data = data
    }

    fun start(
        srcInterceptors: List<IClassVisitInterceptor>?,
        jarInterceptors: List<IJarClassVisitInterceptor>?
    ) {
        if (srcInterceptors == null || srcInterceptors.isEmpty()) {
            this.srcInterceptors = emptyList()
        } else {
            this.srcInterceptors = srcInterceptors
        }
        if (jarInterceptors == null || jarInterceptors.isEmpty()) {
            this.jarInterceptors = emptyList()
        } else {
            this.jarInterceptors = jarInterceptors
        }
        collector!!.traverseAllFile()
        finish()
    }

    private fun finish() {}

    override fun traverseSrcFile(file: File) {
        srcInterceptors?.let {
            if (it.isEmpty()) {
                return@let
            }
            val name = file.name
            if (!isAbandonClass(name)) {
                val chain = ClassVisitChain(it, 0)
                val data = data.clone()
                data.filePath = file.path
                data.bytes = getDataFromFile(file)
                chain.proceed(data)
                file.writeBytes(data.bytes)
            }
        }

    }

    override fun traverseJarFile(src: File) {
        jarInterceptors?.let {
            if (it.isEmpty()) {
                return@let
            }
            if (jarFilter == null) {
                jarFilter = JarVisitFilter(data, it,
                    object : IClassAbandonCallback {
                        override fun isAbandon(name: String): Boolean {
                            return isAbandonClass(name)
                        }
                    })
            }
            jarFilter!!.filterJar(src)
        }

    }

    private fun isAbandonClass(name: String): Boolean {
        return (!name.endsWith(".class") || name.startsWith("R$")
                || "R.class" == name || "BuildConfig.class" == name)
    }

    interface IClassAbandonCallback {
        fun isAbandon(name: String): Boolean
    }

    init {
        collector?.setListener(this)
    }
}