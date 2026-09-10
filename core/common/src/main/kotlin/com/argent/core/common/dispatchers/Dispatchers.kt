package com.argent.core.common.dispatchers

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val kind: ArgentDispatcher)

enum class ArgentDispatcher { Default, IO }
