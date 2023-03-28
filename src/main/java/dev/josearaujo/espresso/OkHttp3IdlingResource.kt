/*
 * Copyright (C) 2016 Jake Wharton
 * Copyright (C) 2023 José Araújo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.josearaujo.espresso

import androidx.annotation.CheckResult
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

/** An [IdlingResource] for [OkHttpClient] */
class OkHttp3IdlingResource private constructor(
    private val name: String,
    private val okHttpCompat: OkHttp3Compat
) : IdlingResource {

    @Volatile
    private var callback: ResourceCallback? = null

    init {
        okHttpCompat.dispatcherCompat.setIdleCallbackCompat {
            callback?.onTransitionToIdle()
        }
    }

    override fun getName(): String = name

    override fun isIdleNow(): Boolean {
        return okHttpCompat.dispatcherCompat.dispatcherDelegate.runningCallsCount() == 0
    }

    override fun registerIdleTransitionCallback(callback: ResourceCallback) {
        this.callback = callback
    }

    companion object {
        /**
         * Create a new [IdlingResource] from [client] as [name]. You must register
         * this instance using `Espresso.registerIdlingResources`
         */
        @CheckResult
        fun create(name: String, client: OkHttpClient): OkHttp3IdlingResource {
            return OkHttp3IdlingResource(name, client.compat())
        }
    }
}