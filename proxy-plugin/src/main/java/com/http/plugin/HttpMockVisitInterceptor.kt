package com.http.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * @author hongkui.jiang
 * @Date 2020/6/30
 */
class HttpMockVisitInterceptor : AbsJarClassVisitInterceptor<OkHttpClassVisitor>() {
    override fun createClassVisitor(
        classVisitor: ClassVisitor,
        data: InterceptorData
    ): OkHttpClassVisitor {
        return OkHttpClassVisitor(classVisitor)
    }

    override fun filterInfo(s: String?): Boolean {
        return s!!.contains("okhttp3")
    }
}

class OkHttpClassVisitor(classVisitor: ClassVisitor) : BaseClassVisitor(classVisitor) {
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if (className == "OkHttpClient\$Builder" && isConstructor(name)) {
            OkHttpMethodVisitor(access, descriptor, name, mv)
        } else mv
    }

    internal class OkHttpMethodVisitor(
        access: Int,
        desc: String,
        name: String,
        mv: MethodVisitor
    ) : BaseMethodVisitor(access, desc, name, mv) {
        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            mv.visitVarInsn(ALOAD, 0)
            var desc = "(Ljava/lang/Object;)V"
            if (methodDesc.split(";".toRegex()).toTypedArray().size > 1) {
                mv.visitVarInsn(ALOAD, 1)
                desc = "(Ljava/lang/Object;Ljava/lang/Object;)V"
            }
            mv.visitMethodInsn(
                INVOKESTATIC,
                "com/http/proxy/RequestHook",
                "updateOkhttpClientBuilder",
                desc,
                false
            )
        }
    }
}