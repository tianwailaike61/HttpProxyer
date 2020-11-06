package com.http.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

open class BaseClassVisitor(classVisitor: ClassVisitor) :
    ClassVisitor(Opcodes.ASM7, classVisitor) {
    private var fullClassName: String? = null

    var className: String? = null
    private var packageName: String? = null

    private var isInterface = false
    fun setClassVisitor(classVisitor: ClassVisitor) {
        cv = classVisitor
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        fullClassName = name
        isInterface = access and Opcodes.ACC_INTERFACE != 0
        val lastIndex = name.lastIndexOf("/")
        packageName = name.substring(0, lastIndex).replace("/", ".")
        className = name.substring(lastIndex + 1)
    }

    protected fun isConstructor(name: String): Boolean {
        return "<init>" == name
    }

    protected fun isStaticConstructor(name: String): Boolean {
        return "<clinit>" == name
    }

    protected fun isAccess(name: String): Boolean {
        return name.startsWith("access$")
    }

    protected val isInnerClass: Boolean
        get() = className!!.contains("$")
}