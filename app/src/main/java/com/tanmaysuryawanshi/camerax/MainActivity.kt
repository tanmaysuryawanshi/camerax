package com.tanmaysuryawanshi.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tanmaysuryawanshi.camerax.ui.theme.CameraxTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private var recording:Recording?=null
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasRequiredPermissions()){
            ActivityCompat.requestPermissions(
                this,
                permissions,0
            )
        }
        setContent {
            CameraxTheme {
                val scaffoldState= rememberBottomSheetScaffoldState();
                val controller= remember {
LifecycleCameraController(applicationContext).apply {
setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
}
                }
                val scope= rememberCoroutineScope()
                val viewModel= viewModel<MainViewModel>();
                val bitmaps by viewModel.bitmaps.collectAsState()
                BottomSheetScaffold(scaffoldState=scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                    PhotoBottomSheetContent(bitmaps = bitmaps,
                        modifier=Modifier.fillMaxSize())
                }) {
                   padding->
                    Box(modifier= Modifier
                        .fillMaxSize()
                        .padding(padding)
                    ){
                        CameraPreview(controller =controller , modifier = Modifier.fillMaxSize())
                       // Image(painter = painterResource(id = R.drawable.sukuna), contentDescription ="sukuna" , modifier = Modifier.fillMaxSize())
                        IconButton(
                            onClick = {
                                controller.cameraSelector=if(controller.cameraSelector== CameraSelector.DEFAULT_BACK_CAMERA){
                                    CameraSelector.DEFAULT_FRONT_CAMERA;
                                }
                                else CameraSelector.DEFAULT_BACK_CAMERA
                            },modifier=Modifier.offset(16.dp,16.dp)

                        ){
                            Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "switch Camera")
                        }
                        Row(modifier= Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                            ){
                            IconButton(
                                onClick = { scope.launch { scaffoldState.bottomSheetState.expand() }
                                },modifier=Modifier.offset(16.dp,16.dp)

                            ){
                                Icon(imageVector = Icons.Default.Photo, contentDescription = "open gallery")
                            }
                            IconButton(
                                onClick = {
takePhoto(controller,viewModel::onTakePhoto)
                                },modifier=Modifier.offset(16.dp,16.dp)

                            ){
                                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "take Photo")
                            }
                            IconButton(
                                onClick = {
                                    recordVideo(controller)
                                },modifier=Modifier.offset(16.dp,16.dp)

                            ){
                                Icon(imageVector = Icons.Default.VideoCameraBack, contentDescription = "take Video")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun takePhoto(
        controller:LifecycleCameraController,
        onPhotoTaken: (Bitmap) ->Unit
    ){
        if (!hasRequiredPermissions()){
            return}
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object:OnImageCapturedCallback(){

                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        onPhotoTaken(image.toBitmap())
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.e("Camera","Couldn't take photo: "+exception)
                    }

            }
        )
    }
@SuppressLint("MissingPermission")
private fun recordVideo(controller:LifecycleCameraController){
    if (recording!=null){
        recording?.stop()
        recording=null
        return
    }
    if (!hasRequiredPermissions()){
return}
    var outputFile= File(filesDir,"my-recording.mp4")
recording=controller.startRecording(
FileOutputOptions.Builder(outputFile).build(),
    AudioConfig.create(true),
    ContextCompat.getMainExecutor(applicationContext)
){videoRecordEvent->
    when(videoRecordEvent){
is VideoRecordEvent.Finalize->{
    if(videoRecordEvent.hasError()){
        recording?.close()
        recording=null
        Toast.makeText(applicationContext,"Video Capture Failed",Toast.LENGTH_LONG).show()
    }
    else {
        Toast.makeText(applicationContext,"Video Capture Success",Toast.LENGTH_LONG).show()
    }
}
    }
}

}
    private fun hasRequiredPermissions():Boolean{
        return permissions.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            )==PackageManager.PERMISSION_GRANTED
        }
    }
    companion object{
        private  val permissions= arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
