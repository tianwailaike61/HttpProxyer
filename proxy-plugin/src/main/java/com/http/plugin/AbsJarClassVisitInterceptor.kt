package com.http.plugin

import org.objectweb.asm.ClassVisitor

/**
 * @author hongkui.jiang
 * @Date 2020/6/30
 */
abstract class AbsJarClassVisitInterceptor<T : ClassVisitor> : AbsClassVisitInterceptor<T>(),
    IJarClassVisitInterceptor