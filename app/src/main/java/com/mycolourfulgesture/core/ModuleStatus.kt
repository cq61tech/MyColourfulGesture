@file:JvmName("ModuleStatus")

package com.mycolourfulgesture.core

private var stub = false

fun isModuleActive(): Boolean = stub

fun moduleFramework(): String = if (stub) "?" else ""
