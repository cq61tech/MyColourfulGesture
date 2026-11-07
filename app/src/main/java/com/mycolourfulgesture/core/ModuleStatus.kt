@file:JvmName("ModuleStatus")

package com.mycolourfulgesture.core

@Volatile private var stub = false

fun isModuleActive(): Boolean = stub

fun moduleFramework(): String = if (stub) "?" else ""
