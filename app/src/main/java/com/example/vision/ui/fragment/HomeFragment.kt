package com.example.vision.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.vision.R
import com.example.vision.databinding.FragmentHomeBinding
import com.example.vision.ocr.OCRActivity
import com.example.vision.tensorflow.ObjectDetectionActivity
import com.example.vision.ui.FeatureListAdapter
import com.example.vision.ui.MainActivity
import com.example.vision.ui.MapActivity
import com.example.vision.viewModel.PageViewModel
import java.util.*

class HomeFragment: Fragment(), FeatureListAdapter.OnItemClickListner,TextToSpeech.OnInitListener {
    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var adaper: FeatureListAdapter
    private val SPEECH_RECOGNITION_CODE = 1
    private var tts: TextToSpeech? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(activity,this)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        adaper = FeatureListAdapter(this)
        binding.recyclerView.adapter = adaper

        binding.btnHint.setOnClickListener {
            var msg = getString(R.string.hint)
            speakOut(msg)
        }
        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        when(position){
            0->{
                //OCR
                speakOut("Starting Camera")
                startActivity(Intent(activity,OCRActivity::class.java))
            }
            1->{
                //Object Detection
                speakOut("Starting Camera")
                startActivity(Intent(activity,ObjectDetectionActivity::class.java))
            }
            2->{
                //Map
                startSpeechToText()
            }
        }
    }


    //***********TTS*******************
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Where Do you want to go"
        )
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE)
        } catch (a: ActivityNotFoundException) {
            Log.d("Error","Sorry! Speech recognition is not supported in this device.")
        }
    }

    /**
     * Callback for speech recognition activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SPEECH_RECOGNITION_CODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val text = result!![0]
                    //txtOutput.setText(text)
                    actionOnResult(text)
                }
            }
        }
    }

    private fun actionOnResult(destination: String?) {
        speakOut("Got it!, Map is opening.")
        speakOut("Navigation for $destination has started")
        // Open Map
        var url = "https://www.google.com/maps/dir/Your+Location/$destination/"
        var intent = Intent(activity,MapActivity::class.java)
        intent.putExtra("url",url)
        startActivity(intent)
    }


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