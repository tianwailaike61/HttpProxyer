package com.http.plugin

import com.http.plugin.VisitorManager.IClassAbandonCallback
import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.*
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * @author hongkui.jiang
 * @Date 2020/6/30
 */
class JarVisitFilter(
    private val data: InterceptorData,
    private val list: List<IJarClassVisitInterceptor>,
    private val callback: IClassAbandonCallback
) {
    private var bytes = ByteArray(0)
    fun filterJar(file: File) {
        val info = getInfo(file)
        val classVisitInterceptors: MutableList<IClassVisitInterceptor> = ArrayList()
        for (interceptor in list) {
            if (interceptor.filterInfo(info)) {
                classVisitInterceptors.add(interceptor)
            }
        }
        if (classVisitInterceptors.isEmpty()) {
            return
        }
        val dest = File(file.parentFile, file.name.replace(".jar", ".cp.jar"))
        visitJar(file, dest, classVisitInterceptors)
    }

    private fun getInfo(src: File): String {
        if (!src.exists()) {
            return ""
        }
        try {
            ZipFile(src).use { inputZip ->
                val entry = inputZip.getEntry("META-INF/MANIFEST.MF") ?: return ""
                val originalFile: InputStream = BufferedInputStream(inputZip.getInputStream(entry))
                var count = originalFile.available()
                if (count > bytes.size) {
                    bytes = ByteArray(count)
                }
                count = originalFile.read(bytes)
                return String(bytes, 0, count)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun visitJar(src: File, dest: File, list: List<IClassVisitInterceptor>) {
        ZipOutputStream(
            BufferedOutputStream(
                Files.newOutputStream(dest.toPath())
            )
        ).use { outputZip ->
            ZipFile(src).use { inputZip ->
                val inEntries = inputZip.entries()
                while (inEntries.hasMoreElements()) {
                    val entry = inEntries.nextElement()
                    val originalFile: InputStream =
                        BufferedInputStream(inputZip.getInputStream(entry))
                    val outEntry = ZipEntry(entry.name)
                    var newEntryContent: ByteArray
                    // seperator of entry name is always '/', even in windows
                    newEntryContent = if (callback.isAbandon(outEntry.name.replace("/", "."))) {
                        IOUtils.toByteArray(originalFile)
                    } else {
                        weaveSingleClassToByteArray(originalFile, list)
                    }
                    val crc32 = CRC32()
                    crc32.update(newEntryContent)
                    outEntry.crc = crc32.value
                    outEntry.method = ZipEntry.STORED
                    outEntry.size = newEntryContent.size.toLong()
                    outEntry.compressedSize = newEntryContent.size.toLong()
                    outEntry.lastAccessTime = ZERO
                    outEntry.lastModifiedTime = ZERO
                    outEntry.creationTime = ZERO
                    outputZip.putNextEntry(outEntry)
                    outputZip.write(newEntryContent)
                    outputZip.closeEntry()
                }
                outputZip.flush()
            }
        }
        src.delete()
        dest.renameTo(src)
    }

    @Throws(IOException::class)
    private fun weaveSingleClassToByteArray(
        inputStream: InputStream,
        list: List<IClassVisitInterceptor>
    ): ByteArray {
        var count = inputStream.available()
        val bytes = ByteArray(count)
        count = inputStream.read(bytes)
        if (count <= 0) {
            return ByteArray(0)
        }
        val chain = ClassVisitChain(list, 0)
        val data = data.clone()
        data.bytes = bytes
        chain.proceed(data)
        return data.bytes
    }

    companion object {
        private val ZERO = FileTime.fromMillis(0)
    }
}