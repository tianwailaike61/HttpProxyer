package com.http.plugin
import java.io.*
import java.nio.file.attribute.FileTime
import java.util.*
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author hongkui.jiang
 * @Date 2019-06-17
 */
object FileUtils {

    private val ZERO: FileTime = FileTime.fromMillis(0)

    fun copyDir(src: File?, dest: File?) {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(src, dest)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun copeFile(src: File?, dest: File?) {
        src ?: return
        dest ?: return
        createFile(dest)
        FileInputStream(src).use { inStream ->
            FileOutputStream(dest).use { outStream ->
                inStream.channel.use {
                    val count = it.size()
                    it.transferTo(0, count, outStream.channel)
                }
            }
        }
    }

    fun getDataFromFile(file: File?): ByteArray {
        if (file == null || !file.exists()) {
            return ByteArray(0)
        }

        FileInputStream(file).use { fileInputStream ->
            val size = fileInputStream.available()
            val data = ByteArray(size)
            return if (fileInputStream.read(data) <= 0) {
                ByteArray(0)
            } else data
        }
    }

    fun readFile(file: File?): String {
        if (file == null || !file.exists()) {
            return ""
        }
        FileInputStream(file).use { fileInputStream ->
            val count = fileInputStream.available()
            val bytes = ByteArray(count)
            val length = fileInputStream.read(bytes)
            return if (length <= 0) {
                ""
            } else String(bytes)
        }
    }

    fun readFile(file: File?, callback: IReadCallback?) {
        if (file == null || !file.exists()) {
            return
        }
        FileInputStream(file).use { fileInputStream -> readFile(fileInputStream, callback) }
    }

    fun readFile(inputStream: InputStream?, callback: IReadCallback?) {
        if (inputStream == null || callback == null) {
            return
        }
        var s: String?
        InputStreamReader(inputStream).use { inputStreamReader ->
            BufferedReader(inputStreamReader).use { reader ->
                while (reader.readLine().also { s = it } != null) {
                    if (callback.onRead(s!!)) {
                        break
                    }
                }
            }
        }
    }

    private fun createFile(file: File): Boolean {
        if (file.exists()) {
            return true
        }
        val parent = file.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }

        return file.createNewFile()
    }

    private fun deleteFile(file: File) {
        if (!file.exists()) {
            return
        }
        if (file.isDirectory) {
            deleteDir(file)
        } else {
            file.delete()
        }
    }

    private fun deleteDir(file: File) {
        if (!file.exists() || !file.isDirectory) {
            return
        }
        val files = file.listFiles()
        if (files == null || files.isEmpty()) {
            file.delete()
            return
        }
        for (f in files) {
            deleteFile(f)
        }
        file.delete()
    }

    fun closeStream(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun eachFileRecurse(file: File, callback: IFileCallback) {
        if (file.isFile) {
            callback.onVisitFile(file)
        } else {
            val fs = file.listFiles()
            if (fs == null || fs.isEmpty()) {
                return
            }
            Arrays.sort(fs) { file1: File, t1: File -> file1.absoluteFile.compareTo(t1.absoluteFile) }
            for (f in fs) {
                eachFileRecurse(f, callback)
            }
        }
    }

    interface IFileCallback {
        fun onVisitFile(file: File)
    }

    interface IReadCallback {
        fun onRead(s: String): Boolean
    }

    @Throws(Exception::class)
    fun zip(files: List<File>, dest: File, keepDirStructure: Boolean): Boolean {
        FileOutputStream(dest).use { fos ->
            ZipOutputStream(fos).use { zos ->
                files.forEach {
                    compress(it, zos, it.name, keepDirStructure)
                }
                return true
            }
        }
    }

    @Throws(Exception::class)
    fun zip(src: File, dest: File, keepDirStructure: Boolean): Boolean {
        FileOutputStream(dest).use { fos ->
            ZipOutputStream(fos).use { zos ->
                compress(src, zos, src.name, keepDirStructure)
                return true
            }
        }
    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param keepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     * false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     */
    @Throws(Exception::class)
    private fun compress(sourceFile: File, zos: ZipOutputStream, name: String,
                         keepDirStructure: Boolean) {
        if (sourceFile.isFile) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            val outEntry = ZipEntry(name)
            val `in` = FileInputStream(sourceFile)
            val newEntryContent = ByteArray(`in`.available())
            val count = `in`.read(newEntryContent)
            if (count <= 0) {
                return
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
            zos.putNextEntry(outEntry)
            zos.write(newEntryContent)
            zos.closeEntry()
            `in`.close()
        } else {
            val listFiles = sourceFile.listFiles()
            if (listFiles == null || listFiles.isEmpty()) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(ZipEntry("$name/"))
                    // 没有文件，不需要文件的copy
                    zos.closeEntry()
                }
            } else {
                for (file in listFiles) {
                    compress(file, zos, name + "/" + file.name, keepDirStructure)
                    //                    // 判断是否需要保留原来的文件结构
//                    if (keepDirStructure) {
//                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
//                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
//                        compress(file, zos, name + "/" + file.getName(), true);
//                    } else {
//                        compress(file, zos, name + "/" + file.getName(), false);
//                    }
                }
            }
        }
    }
}

fun File.each(callback: FileUtils.IFileCallback) {
    FileUtils.eachFileRecurse(this, callback)
}