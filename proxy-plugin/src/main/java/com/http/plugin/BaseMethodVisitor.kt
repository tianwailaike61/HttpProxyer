package com.http.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

open class BaseMethodVisitor protected constructor(
    access: Int,
    desc: String,
    private var methodName: String,
    mv: MethodVisitor
) : AdviceAdapter(
    ASM7, mv, access, methodName, desc
) {
    protected val isConstructor: Boolean
        protected get() = "<init>" == methodName
    private val isAccess: Boolean
        private get() = methodName.startsWith("access$")

    override fun onMethodEnter() {
        super.onMethodEnter()
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
    }

    init {
        methodDesc = desc
        methodAccess = access
    }
}