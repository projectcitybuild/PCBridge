package com.projectcitybuild

import org.mockito.Mockito

/**
 * Mocks a class or interface that has generic types
 *
 * Usage:
 * ```
 * val myClass = mock<MyClass<String>>()
 * ```
 */
inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)!!