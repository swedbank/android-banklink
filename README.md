Swedbank Banklink Android SDK
==============================

The Swedbank Banklink Android SDK lets you quickly integrate Swedbank Banklink payment solution
in native Android applications and establish communication between Swedbank’s Android app and merchants Android app.

Getting Started
---------------

#### Add the Swedbank Banklink Android SDK to your project

```groovy
dependencies {
  compile 'com.swedbank.sdk:banklink-sdk:1.0.0'
}
```

#### Install the latest version of Swedbank app on your Android device

Swedbank app is available on the [Google Play Store](https://play.google.com/store/search?q=swedbank&c=apps).

- Banklink is supported in Estonian, Latvian and Lithuanian apps.
- Banklink API is supported in versions **6.8** and later.

#### Initiating a banklink transaction from your app

**First, create an instance of BanklinkClient:**

```java
BanklinkClient eeClient = BanklinkSdk.createClient(app, BanklinkApi.Country.EE)
```
_Note: if you want to use banklink SDK for multiple countries, you must create new client for
each country._

**Then, start the Swedbank app with created banklink intent**

The _packetMap_ of type _Map<String, String>_ represents a signed Banklink packet parameters which are
usually sent to Swedbank banklink server over HTTP protocol using POST method, but here it should be
sent to Swedbank native app with _Intent_ object. Merchant app should prepare packet
parameters(_VK_SERVICE, VK_VERSION, VK_SND_ID etc._) and calculate MAC value(_VK_MAC_).

```java
// obtain signed packet map
try {
  Intent intent = eeClient.createBanklinkIntent(packetMap);
  startActivityForResult(intent, BANKLINK_REQUEST_CODE);
} catch (ActivityNotFoundException e) {
  // Swedbank app not installed. Show Swedbank app in Google Play Store.
  eeClient.openSwedbankAppPlayStoreListing();
}
```

When the user's Android device switches over to Swedbank app, the details of the transaction are prepopulated.
The user can either execute the transaction(confirming with selected authentication method, if required) or cancel it.
Either way, when the action completes, the device returns to your app and provides details of the result.


**Finally, handle transaction results**

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  if (requestCode == BANKLINK_REQUEST_CODE) {
    BanklinkResult result = eeClient.parseResult(resultCode, data);
    if (result.success()) {
      Map<String, String> responsePacket = result.responsePacket();
      // handle success
    } else {
      if (result.canceled()) {
        // handle user canceled transaction
      } else {
        // handle errors
      }
    }
  } else {
    super.onActivityResult(requestCode, resultCode, data);
  }
}
```

Documentation
-------------

- [Javadoc](https://swedbank.github.io/android-banklink/javadoc/)

Updates
-------

All updates can be found in the [CHANGELOG](CHANGELOG.md).

Bugs and Feedback
-----------------

**For bugs, questions and discussions please use the [Github Issues](https://github.com/swedbank/android-banklink/wiki).**


License
--------

    Copyright 2017 Swedbank

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
