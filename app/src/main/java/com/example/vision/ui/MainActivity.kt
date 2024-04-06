package com.example.vision.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.vision.R
import com.example.vision.databinding.ActivityMainBinding
import android.speech.RecognizerIntent
import android.content.Intent
import android.widget.Toast
import android.content.ActivityNotFoundException
import android.view.KeyEvent
import com.example.vision.ocr.OCRActivity
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.vision.tensorflow.ObjectDetectionActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private val SPEECH_RECOGNITION_CODE = 1
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        //val tabs: TabLayout = binding.tabs
        //tabs.setupWithViewPager(viewPager)

        binding.btnVoiceCommand.setOnClickListener {
            startSpeechToText()
        }

        binding.btnRight.setOnClickListener(){
            startActivity(Intent(this, OCRActivity::class.java))
            finish()
        }

        /*binding.btnHint.setOnClickListener {
            var msg = getString(R.string.hint);
            speakOut(msg);
        }*/
    }


    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event!!.keyCode === KeyEvent.KEYCODE_POWER) {
            startSpeechToText()
            return true
        }
        return super.dispatchKeyEvent(event)
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

    /**
    * Callback for speech recognition activity
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SPEECH_RECOGNITION_CODE -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val text = result!![0]
                    //txtOutput.setText(text)
                    actionOnResult(text)
                }
            }
        }
    }

    private fun actionOnResult(text: String?) {

        if(text?.let { find(it,"read") } == true){
            speakOut("Opening camera to read text")
            startActivity(Intent(this, OCRActivity::class.java))
        }
        else if(text?.let { find(it,"object") } == true
            || text?.let { find(it,"detect") } == true
            || text?.let { find(it,"what is in front of me") } == true ){
            speakOut("Opening camera for object Detection")
            startActivity(Intent(this, ObjectDetectionActivity::class.java))
        }
        else if(text?.let { find(it,"Map") } == true
            || text?.let { find(it,"take me") } == true
            || text?.let { find(it,"navigate") } == true ){
                var stringArray = text.split("to")

            speakOut("Navigation for ${stringArray[1]} has started")
            // Open Map
            var url = "https://www.google.com/maps/dir/Your+Location/${stringArray[1]}/"
            var intent = Intent(this,MapActivity::class.java)
            intent.putExtra("url",url)
            startActivity(intent)
        }
    }
    private fun find(str:String, word: String): Boolean = str.contains(word)

    override fun onDestroy() {
// Don't forget to shutdown tts!
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
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