# CatEyeSdkTest
COMAIOT 云控慧联 Android端SDK

Step 1. Authorize JitPack and get your personal access token:
=
  jp_s5jeq1pp0brabn77ur6tpangf2<br>

Step 2. Add the token to $HOME/.gradle/gradle.properties
=
  authToken=jp_s5jeq1pp0brabn77ur6tpangf2<br>
Then use authToken as the username in your build.gradle:<br>
=
  allprojects {<br>
    repositories {<br>
        ...<br>
        maven {<br>
            url "https://jitpack.io"<br>
            credentials { username authToken }<br>
        }<br>
    }<br>
 }<br>

Step 3. (Optional) You may need to approve JitPack Application on GitHub
=
  Build artifacts (jar, aar) are also private and you can only download them if you have access to the Git repo itself.<br>
  See the documentation on how to authenticate with other providers (Bitbucket, GitLab, custom) or to use SSH key authentication.
