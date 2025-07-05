package com.example.gitcommai.ViewModels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognizer:ViewModel() {
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)}
    fun getText(bitmap: Bitmap,onTextDetected:(String)->Unit){
        val image=InputImage.fromBitmap(bitmap,0)
        recognizer.process(image).addOnSuccessListener { text->
            if (text.text.isNotBlank()){
                onTextDetected(text.text)
            }
        }
    }
    fun getText(uri: Uri,context: Context,onTextDetected:(String)->Unit){
        val image=InputImage.fromFilePath(context,uri)
        recognizer.process(image).addOnSuccessListener { text->
            if (text.text.isNotBlank()){
                  onTextDetected(text.text)
            }
        }
    }
}