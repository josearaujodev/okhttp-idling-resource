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

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import com.google.common.truth.Truth.assertThat
import okhttp3.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class OkHttp3IdlingResourceTest {
    @get:Rule val server = MockWebServer()

    @Test
    fun name() {
        val client = OkHttpClient()
        val idlingResource: IdlingResource = OkHttp3IdlingResource.create("Ok!", client)
        assertThat(idlingResource.name).isEqualTo("Ok!")
    }

    @Test
    @Throws(InterruptedException::class)
    fun idleNow() {
        server.enqueue(MockResponse())
        val requestReady = CountDownLatch(1)
        val requestProceed = CountDownLatch(1)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                requestReady.countDown()
                try {
                    requestProceed.await(10, TimeUnit.SECONDS)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
                chain.proceed(chain.request())
            })
            .build()
        val idlingResource: IdlingResource = OkHttp3IdlingResource.create("Ok!", client)
        assertThat(idlingResource.isIdleNow).isTrue()
        val call = client.newCall(Request.Builder().url(server.url("/")).build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                throw AssertionError()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })

        // Wait until the interceptor is called signifying we are not idle.
        requestReady.await(10, TimeUnit.SECONDS)
        assertThat(idlingResource.isIdleNow).isFalse()

        // Allow the request to proceed and wait for the executor to stop to signify we became idle.
        requestProceed.countDown()
        client.dispatcher.executorService.shutdown()
        client.dispatcher.executorService.awaitTermination(10, TimeUnit.SECONDS)
        assertThat(idlingResource.isIdleNow).isTrue()
    }

    @Test
    @Throws(InterruptedException::class, IOException::class)
    fun idleCallback() {
        server.enqueue(MockResponse())
        val client = OkHttpClient()
        val idlingResource: IdlingResource = OkHttp3IdlingResource.create("Ok!", client)
        val count = AtomicInteger()
        val callback = ResourceCallback { count.getAndIncrement() }
        idlingResource.registerIdleTransitionCallback(callback)
        assertThat(count.get()).isEqualTo(0)

        // Use a synchronous call as a quick way to transition from busy to idle in a blocking way.
        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        assertThat(count.get()).isEqualTo(1)
    }
}