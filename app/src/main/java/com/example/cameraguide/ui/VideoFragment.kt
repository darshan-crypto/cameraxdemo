package com.example.cameraguide.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CaptureRequest
import android.media.*
import android.media.MediaCodec.BufferInfo
import android.os.Bundle
import android.util.Log
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageAnalysis.STRATEGY_BLOCK_PRODUCER
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.BitmapCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cameraguide.databinding.FragmentVideoBinding
import com.example.cameraguide.viewmodels.SharedViewModel
import com.example.cameraguide.ui.Muxer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class FrameAnalyzer(private val outputFile: File, private val mycontext: Context) :
    ImageAnalysis.Analyzer {
    private var yimages: Array<ArrayList<ByteArray>> = arrayOf(
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>(),
        ArrayList<ByteArray>()
    )
//    private var videoStates: Array<Int> = arrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    private var z: Int = 0 //frame counter
    private var currentarray: Int = 0
    public var is_recording = true
    var TAG: String = "CameraXApp"
    val executor = Executors.newFixedThreadPool(10)
    private fun toBitmap(image: Image): ByteArray {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)


        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)


        val out = ByteArrayOutputStream()


        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)


        val imageBytes: ByteArray = out.toByteArray()

        return imageBytes;
    }

    fun stop() {
        Log.d(TAG, "stopping")
//        Thread {
        val muxerConfig = MuxerConfig(
            File(
                "/storage/emulated/0/Movies/",
                "test_${System.currentTimeMillis()}.mp4"
            ), 600, 480, "video/avc", 1, 60f, 1500000
        )
        val y = FrameBuilder(mycontext,muxerConfig,null)
        y.start()
        while (yimages[currentarray].size != 0){
            val mybitmap  = BitmapFactory.decodeByteArray(yimages[currentarray][0], 0, yimages[currentarray][0].size)
            y.createFrame(mybitmap)
            yimages[currentarray].removeAt(0)
        }
//        for (imagebuffer in yimages[currentarray])
//        {
//            val mybitmap  = BitmapFactory.decodeByteArray(imagebuffer, 0, imagebuffer.size)
//            y.createFrame(mybitmap)
//
//        }
        yimages[currentarray].clear()

        y.releaseVideoCodec()
        y.releaseAudioExtractor()
        y.releaseMuxer()
//        val muxer = Muxer(mycontext, muxerConfig)
//        var mybitmaps = ArrayList<Bitmap>()
//        for (imagebuffer in yimages[currentarray]) {
//            mybitmaps.add(BitmapFactory.decodeByteArray(imagebuffer, 0, imagebuffer.size))
//        }
//        videoStates[currentarray] = 0
//
//        muxer.mux(mybitmaps);
//        yimages[currentarray].clear()
//        }.start()
        Toast.makeText(mycontext, "recording stopped", Toast.LENGTH_SHORT).show()
    }

    override
    fun analyze(imageProxy: ImageProxy) {
        val start_Time = System.currentTimeMillis();
        var End_Time: Long
        val image: Image? = imageProxy.image
        if (image != null) {
            var x: ByteArray = toBitmap(image);
            imageProxy.close()

            yimages[currentarray].add(x);
            z++;
            if (z == 600) {
                Log.d("anatime", "muxer")
              //  videoStates[currentarray] = 2;
                val priv = currentarray
                                     executor.execute(Runnable {

                                         Log.d("anatime","runnable muxe startedr ")


                                         val time1 =System.currentTimeMillis()
                                         val muxerConfig = MuxerConfig( File("/storage/emulated/0/Movies/","test_${System.currentTimeMillis()}.mp4"), 600, 480, "video/avc", 1, 60f, 1500000)

                                         val y = FrameBuilder(mycontext,muxerConfig,null)
                                        y.start()
//                                         for (imagebuffer in yimages[priv])
//                                         {
//                                              val mybitmap  = BitmapFactory.decodeByteArray(imagebuffer, 0, imagebuffer.size)
//                                              y.createFrame(mybitmap)
//
//                                         }
                                         while (yimages[priv].size != 0){
                                             val mybitmap  = BitmapFactory.decodeByteArray(yimages[priv][0], 0, yimages[priv][0].size)
                                             y.createFrame(mybitmap)
                                             yimages[priv].removeAt(0)
                                         }
//                                         yimages[priv].clear()

                                         y.releaseVideoCodec()
                                         y.releaseAudioExtractor()
                                         y.releaseMuxer()

                                         val time2 =System.currentTimeMillis()

                                         Log.d("bitc","bitmap conversion stoped ${time1-time2}")


//                                         val muxer = Muxer(mycontext ,muxerConfig)
//                                          muxer.mux(mybitmaps);
                                         Log.d("anatime","runnable muxe ended ")

                                     })

                z = 0;
                if (currentarray == 9) currentarray = 0
                else currentarray++;
                Log.d("maind", "currently entring data to ${currentarray}")
//                videoStates[currentarray] = 1
            }
        }


        End_Time = System.currentTimeMillis()
        if ((End_Time - start_Time) >= 17) {
            Log.d("ERRORX", "fps is ${End_Time - start_Time}")
        }
        Log.d("anatime", "time is ${End_Time - start_Time} ${z} $currentarray}")
    }


}

class VideoFragment : Fragment() {
    companion object {
        fun newInstance() = VideoFragment()
        private const val TAG = "CameraXApp"
        private lateinit var imageAnalyzer: ImageAnalysis;
        private lateinit var frameAnalyzer: FrameAnalyzer;

    }


    private var is_recording_on = false;
    private lateinit var _binding: FragmentVideoBinding
    private lateinit var sharedViewModel: SharedViewModel


    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            sharedViewModel = ViewModelProvider(it).get(SharedViewModel::class.java)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.isPermissionGranted.observe(viewLifecycleOwner) {
            if (it) startCamera()
        }
        _binding.videoCaptureButton.setOnClickListener {
            if (is_recording_on == false) {

                is_recording_on = true
                frameAnalyzer = FrameAnalyzer(File("j"), requireContext())

                imageAnalyzer.setAnalyzer(
                    ContextCompat.getMainExecutor(requireContext()),
                    frameAnalyzer
                )
                _binding.videoCaptureButton.apply {
                    text = "stop capture"
                }
            } else {
                _binding.videoCaptureButton.apply {
                    text = "stopping..."
                    isEnabled = false
                }
                _binding.videoCaptureButton.setText("stopping")

                is_recording_on = false
                frameAnalyzer.is_recording = false
                imageAnalyzer.clearAnalyzer()

                frameAnalyzer.stop()

                _binding.videoCaptureButton.apply {
                    text = "start capture"
                    isEnabled = true

                }
            }

        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("RestrictedApi")
    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val v = FileOutputOptions.Builder(File("f")).build()
//            cameraProvider.
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(_binding.videoPreviewView.surfaceProvider)
                }
            val qualitySelector = QualitySelector.from(Quality.HD)

            val recorder = Recorder.Builder()
                .setExecutor(cameraExecutor).setQualitySelector(qualitySelector)
                .build()

            val videocap =
                VideoCapture.Builder(recorder).setTargetFrameRate(Range<Int>(60, 60)).build()

            val builder = ImageAnalysis.Builder()
            val ext: Camera2Interop.Extender<*> = Camera2Interop.Extender(builder)
            ext.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                Range<Int>(60, 60)

            )
            imageAnalyzer = builder
                .setBackpressureStrategy(STRATEGY_BLOCK_PRODUCER)
                .setImageQueueDepth(52)

                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer,
                    videocap
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroy() {
        super.onDestroy()
        frameAnalyzer.stop()
        cameraExecutor.shutdown()
    }
}
