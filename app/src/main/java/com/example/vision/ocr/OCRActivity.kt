package com.example.vision.ocr

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.util.Linkify
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.vision.R
import com.example.vision.databinding.ActivityMainBinding
import com.example.vision.databinding.ActivityTextDetectionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OCRActivity : AppCompatActivity(),TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityTextDetectionBinding
    private lateinit var recognizer: TextRecognizer
    private val TAG = "Testing"
    private val SAVED_TEXT_TAG = "SavedText"
    private var tts: TextToSpeech? = null
    private val SPEECH_RECOGNITION_CODE = 1

    //private val SAVED_IMAGE_BITMAP = "SavedImage"
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    lateinit var camera: Camera
    var savedBitmap: Bitmap? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_detection)
        binding = ActivityTextDetectionBinding.inflate(layoutInflater)
        tts = TextToSpeech(this,this)

        if (savedInstanceState != null) {
            val savedText = savedInstanceState.getString(SAVED_TEXT_TAG)
            binding.apply {
                if (isTextValid(savedText)) {
                    showTxtNavBar.visibility = View.VISIBLE
                    textInImage.text = savedInstanceState.getString(SAVED_TEXT_TAG)
                }
                if (savedBitmap != null) {
                    previewImage.visibility = View.VISIBLE
                    previewImage.setImageBitmap(savedBitmap)
                }
                //previewImage.setImageBitmap(savedInstanceState.getParcelable(SAVED_IMAGE_BITMAP))
            }
        }
        init()
        setContentView(binding.root)
    }

    private fun init() {

        cameraExecutor = Executors.newSingleThreadExecutor()

        recognizer = TextRecognition.getClient()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Extract Button Click
        binding.apply {
            btnVoiceCommand.setOnClickListener {
                when {
                    previewImage.visibility == View.VISIBLE -> {
                        savedBitmap = previewImage.drawable.toBitmap()
                        runTextRecognition(savedBitmap!!)
                    }
                    preView.bitmap != null -> {
                        previewImage.visibility = View.VISIBLE
                        savedBitmap = preView.bitmap
                       previewImage.setImageBitmap(preView.bitmap!!)
                        runTextRecognition(savedBitmap!!)
                    }
                    else -> {
                        showToast(getString(R.string.camera_error_default_msg))
                    }
                }
            }


            //Copy to Clipboard
            /*copyToClipboard.setOnClickListener {
                val textToCopy = textInImage.text
                if (isTextValid(textToCopy.toString())) {
                    copyToClipboard(textToCopy)
                } else {
                    showToast(getString(R.string.no_text_found))
                }
            }*/

            btnShare.setOnClickListener {
                val textToCopy = textInImage.text.toString()
                if (isTextValid(textToCopy)) {
                    shareText(textToCopy)
                } else {
                    showToast(getString(R.string.no_text_found))
                }
            }

            btnCamera.setOnClickListener {
                if(!allPermissionsGranted()){
                    requestPermissions()
                } else {
                    binding.previewImage.visibility = View.GONE
                    binding.showTxtNavBar.visibility = View.GONE
                    savedBitmap = null
                }
            }
            /*close.setOnClickListener {
                textInImageLayout.visibility = View.GONE
            }*/

        }

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showToast(
                    getString(R.string.permission_denied_msg)
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.gallery) {
            /*binding.textInImageLayout.visibility = View.GONE
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()*/

            return true
        } else if (item.itemId == R.id.camera) {
            if(!allPermissionsGranted()){
                requestPermissions()
            } else {
                binding.previewImage.visibility = View.GONE
                savedBitmap = null
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!

                // Use Uri object instead of File to avoid storage permissions
                binding.previewImage.apply {
                    visibility = View.VISIBLE
                    setImageURI(uri)
                }
                //runTextRecognition(binding.previewImage.drawable.toBitmap())
            }

            /*ImagePicker.RESULT_ERROR -> {
                showToast(ImagePicker.getError(data))
            }*/
            else -> {
                showToast("No Image Selected")
            }
        }


    }

    private fun runTextRecognition(bitmap: Bitmap) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        recognizer
            .process(inputImage)
            .addOnSuccessListener { text ->
                binding.showTxtNavBar.visibility = View.VISIBLE
                processTextRecognitionResult(text)
            }.addOnFailureListener { e ->
                e.printStackTrace()
                showToast(e.localizedMessage ?: getString(R.string.error_default_msg))
            }
    }

    private fun processTextRecognitionResult(result: Text) {
        var finalText = ""
        for (block in result.textBlocks) {
            for (line in block.lines) {
                finalText += line.text + " \n"
            }
            finalText += "\n"
        }

        Log.d(TAG, finalText)
        Log.d(TAG, result.text)

        binding.textInImage.text = if (finalText.isNotEmpty()) {
            finalText
        } else {
            getString(R.string.no_text_found)
        }
        speakOut(binding.textInImage.text.toString())
        Linkify.addLinks(binding.textInImage, Linkify.ALL)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )


                binding.apply {

                    /*camera.apply {
                        torchImage.setBackgroundColor(resources.getColor(R.color.purple_200))
                        if (cameraInfo.hasFlashUnit()) {
                            torchButton.setOnClickListener {
                                cameraControl.enableTorch(cameraInfo.torchState.value == TorchState.OFF)
                            }
                        } else {
                            torchButton.setOnClickListener {
                                showToast(getString(R.string.torch_not_available_msg))
                            }
                        }

                        cameraInfo.torchState.observe(this@OCRActivity) { torchState ->
                            if (torchState == TorchState.OFF) {
                                torchImage.setImageResource(R.drawable.ic_obj_detection)
                            } else {
                                torchImage.setImageResource(R.drawable.ic_obj_detection)
                            }
                        }

                    }*/

                }
            } catch (exc: Exception) {
                showToast(getString(R.string.error_default_msg))
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isTextValid(text: String?): Boolean {
        if (text == null)
            return false

        return text.isNotEmpty() and !text.equals(getString(R.string.no_text_found))
    }

    private fun shareText(text: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_text_title)))
    }

    private fun copyToClipboard(text: CharSequence) {
        val clipboard =
            ContextCompat.getSystemService(applicationContext, ClipboardManager::class.java)
        val clip = ClipData.newPlainText("label", text)
        clipboard?.setPrimaryClip(clip)
        showToast(getString(R.string.clipboard_text))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        cameraExecutor.shutdown()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
        val textInImage = (binding.textInImage.text).toString()
        if (isTextValid(textInImage)) {
            outState.putString(SAVED_TEXT_TAG, textInImage)
        }
        /*if (binding.previewImage.visibility == View.VISIBLE) {
            outState.putParcelable(SAVED_IMAGE_BITMAP, binding.previewImage.drawable.toBitmap())
        }*/
    }

    //***********TTS*******************
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak something..."
        )
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext,
                "Sorry! Speech recognition is not supported in this device.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    private fun actionOnResult(text: String?) {
        when(text){
            "click picture"-> {
                speakOut("Click Picture command is initiated")
                when {
                    binding.previewImage.visibility == View.VISIBLE -> {
                        savedBitmap = binding.previewImage.drawable.toBitmap()
                        runTextRecognition(savedBitmap!!)
                    }
                    binding.preView.bitmap != null -> {
                        binding.previewImage.visibility = View.VISIBLE
                        savedBitmap = binding.preView.bitmap
                        binding.previewImage.setImageBitmap(binding.preView.bitmap!!)
                        runTextRecognition(savedBitmap!!)
                    }
                    else -> {
                        showToast(getString(R.string.camera_error_default_msg))
                    }
                }
            }
            "take a new one"->{
                speakOut("Camera is prepared for the new image")
                if(!allPermissionsGranted()){
                    requestPermissions()
                } else {
                    binding.previewImage.visibility = View.GONE
                    binding.showTxtNavBar.visibility = View.GONE
                    savedBitmap = null
                }
            }
        }
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts!!.setSpeechRate(0.7f)
            val result = tts!!.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.e("TTS", "This Language is not supported")
            } else {
                speakOut("")
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    private fun speakOut(msg: String) {
        tts!!.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
    }
}