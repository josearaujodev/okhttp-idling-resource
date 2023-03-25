OkHttp Idling Resource
======================

An Espresso `IdlingResource` for OkHttp.



Usage
-----

With your `OkHttpClient` instance, create an idling resource:

<details>
  <summary>Java</summary>
```java
OkHttpClient client = // ...
IdlingResource resource = OkHttp3IdlingResource.create("OkHttp", client);
```
</details>

<details>
  <summary>Kotlin</summary>
```java
val client: OkHttpClient = // ...
val resource: IdlingResource = OkHttp3IdlingResource.create("OkHttp", client);
```
</details>

Register the idling resource with `Espresso` before any of your tests.
```java
Espresso.registerIdlingResources(resource);
```



Download
--------

<details>
  <summary>Stable release</summary>
```groovy
androidTestImplementation 'dev.josearaujo.espresso:okhttp3-idling-resource:2.0.0'
```
</details>

<details>
  <summary>Snapshot</summary>
```groovy
androidTestImplementation 'dev.josearaujo.espresso:okhttp3-idling-resource:2.0.0-SNAPSHOT'
```
</details>

License
-------

    Copyright 2016 Jake Wharton
    Copyright 2023 José Araújo

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
