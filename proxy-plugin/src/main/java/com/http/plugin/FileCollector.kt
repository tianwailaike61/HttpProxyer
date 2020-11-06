package com.http.plugin

import com.android.build.api.transform.*
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

abstract class FileCollector private constructor() {
    var srcFiles: MutableList<File> = ArrayList()
    var jarFiles: MutableList<File> = ArrayList()
    var removeFilePaths: MutableList<String> = ArrayList()
    private var listener: FileTraverseListener? = null

    @Throws(IOException::class)
    abstract fun traverseJar(
        jarInputs: Collection<JarInput>,
        outputProvider: TransformOutputProvider
    )

    @Throws(IOException::class)
    abstract fun traverseDirectory(
        directoryInputs: Collection<DirectoryInput>,
        outputProvider: TransformOutputProvider
    )

    private fun traverseSrcFiles(src: File) {
        listener ?: return

        if (src.isDirectory) {
            src.each(object : com.http.plugin.FileUtils.IFileCallback {
                override fun onVisitFile(file: File) {
                    listener!!.traverseSrcFile(file)
                }
            })
        } else {
            listener!!.traverseSrcFile(src)
        }

    }

    private fun traverseJarFiles(src: File) {
        listener ?: return
        listener!!.traverseJarFile(src)
    }

    fun traverseAllFile() {
        for (file in srcFiles) {
            traverseSrcFiles(file)
        }
        for (file in jarFiles) {
            traverseJarFiles(file)
        }
    }

    fun setListener(listener: FileTraverseListener?) {
        this.listener = listener
    }

    override fun toString(): String {
        return "FileCollector${hashCode()}{srcFiles:$srcFiles,jarFiles:$jarFiles}"
    }

    interface FileTraverseListener {
        fun traverseJarFile(file: File)
        fun traverseSrcFile(file: File)
    }

    private class NormalFileCollector : FileCollector() {
        @Throws(IOException::class)
        override fun traverseJar(
            jarInputs: Collection<JarInput>,
            outputProvider: TransformOutputProvider
        ) {
            for (jarInput in jarInputs) {
                val dest = outputProvider.getContentLocation(
                    jarInput.file.absolutePath,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                val src = jarInput.file
                FileUtils.copyFile(src, dest)
                jarFiles.add(dest)
            }
        }

        @Throws(IOException::class)
        override fun traverseDirectory(
            directoryInputs: Collection<DirectoryInput>,
            outputProvider: TransformOutputProvider
        ) {
            for (directoryInput in directoryInputs) {
                val dest = outputProvider.getContentLocation(
                    directoryInput.file.absolutePath,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                val src = directoryInput.file
                if (src.isDirectory) {
                    FileUtils.copyDirectory(src, dest)
                } else {
                    FileUtils.copyFile(src, dest)
                }
                srcFiles.add(dest)
            }
        }
    }

    private class IncrementalFileCollector : FileCollector() {
        @Throws(IOException::class)
        override fun traverseJar(
            jarInputs: Collection<JarInput>,
            outputProvider: TransformOutputProvider
        ) {
            for (jarInput in jarInputs) {
                val dest = outputProvider.getContentLocation(
                    jarInput.file.absolutePath,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                val src = jarInput.file
                filterByStatus(src, jarInput.status, src.absolutePath, dest.absolutePath)
                    ?: continue
                FileUtils.copyFile(src, dest)
                jarFiles.add(dest)
            }
        }

        @Throws(IOException::class)
        override fun traverseDirectory(
            directoryInputs: Collection<DirectoryInput>,
            outputProvider: TransformOutputProvider
        ) {
            for (directoryInput in directoryInputs) {
                val dest = outputProvider.getContentLocation(
                    directoryInput.file.absolutePath,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                val srcDirPath = directoryInput.file.absolutePath
                val destDirPath = dest.absolutePath
                val map = directoryInput.changedFiles
                for ((key, value) in map) {
                    val src = filterByStatus(key, value, srcDirPath, destDirPath)
                    if (src == null) {
                        if (value == Status.REMOVED) {
                            val destFile = File(key.absolutePath.replace(srcDirPath, destDirPath))
                            FileUtils.forceDeleteOnExit(destFile)
                        }
                        continue
                    }
                    val destFilePath = src.absolutePath.replace(srcDirPath, destDirPath)
                    val destFile = File(destFilePath)
                    if (src.isDirectory) {
                        FileUtils.copyDirectory(src, destFile)
                    } else {
                        FileUtils.copyFile(src, destFile)
                    }
                    srcFiles.add(destFile)
                }
            }
        }

        @Throws(IOException::class)
        fun filterByStatus(
            file: File,
            status: Status,
            srcDirPath: String?,
            destDirPath: String?
        ): File? {
            when (status) {
                Status.NOTCHANGED -> {
                }
                Status.REMOVED -> {
                    removeFilePaths.add(file.absolutePath.replace(".class", ""))
                    val dest = File(file.absolutePath.replace(srcDirPath!!, destDirPath!!))
                    if (dest.exists()) {
                        FileUtils.forceDelete(dest)
                    }
                    return null
                }
                Status.ADDED, Status.CHANGED -> {
                    try {
                        FileUtils.touch(file)
                    } catch (e: IOException) {
                        //maybe mkdirs fail for some strange reason, try again.
                        Files.createParentDirs(file)
                    }
                    return file
                }
            }
            return null
        }
    }

    companion object {
        @JvmStatic
        operator fun get(isIncremental: Boolean): FileCollector {
            return if (isIncremental) {
                IncrementalFileCollector()
            } else NormalFileCollector()
        }
    }
}