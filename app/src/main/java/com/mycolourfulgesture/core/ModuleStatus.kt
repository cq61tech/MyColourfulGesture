@file:JvmName("ModuleStatus")

package com.mycolourfulgesture.core

private var stub = false

/** Modül kendi uygulamasında kapsama alınırsa hook bunu true döndürür. */
fun isModuleActive(): Boolean = stub

fun moduleFramework(): String = if (stub) "?" else ""
