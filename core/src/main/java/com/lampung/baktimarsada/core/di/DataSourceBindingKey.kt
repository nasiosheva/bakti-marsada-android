package com.lampung.baktimarsada.core.di

import com.lampung.baktimarsada.core.constants.DataSourceProvider
import dagger.MapKey

@MapKey
annotation class DataSourceBindingKey(val provider: DataSourceProvider)
