#Dependencies

val cameraxVersion = "1.3.0-rc01"

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")


implementation("androidx.compose.material:material-icons-extended:1.5.1")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

# permissions in manifestfile
<uses-feature
android:name="android.hardware.camera"
android:required="false" />

    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
