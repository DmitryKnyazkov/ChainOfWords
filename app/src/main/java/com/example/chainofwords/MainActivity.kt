package com.example.chainofwords

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wordsViewModel: WordsViewModel by viewModels()


        var modes: String = "questionStart"

        val image: ImageView = findViewById(R.id.image)
        val textComand: TextView = findViewById(R.id.textComand)
        val editText = findViewById<EditText>(R.id.editText)
        val btn = findViewById<Button>(R.id.btn)
        val text2 = findViewById<TextView>(R.id.text2)
        val textCounter: TextView = findViewById<TextView>(R.id.textCounter)


        fun questionStart() {
            btn.text = "Ввод"
            textComand.text = getString(R.string.questionStart)
            textCounter.text = wordsViewModel.counterEnteredWords.toString()
            editText.setText("")
//            btn.isEnabled = false
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun inputSecondWord() {
            btn.text = "Ввод"
            textComand.text = getString(R.string.inputSecondWord)
            textCounter.text = wordsViewModel.getcounterEnteredWords().toString()
            editText.setText("")
//            btn.isEnabled = false
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun answerStart() {
            textComand.text = getString(R.string.answerStart)
            textCounter.text = wordsViewModel.getcounterEnteredWords().toString()
            editText.setText("")
//            btn.isEnabled = false
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.blue1))

        }

        fun next_word() {
            textComand.text = getString(R.string.next_word)
            textCounter.text = wordsViewModel.getcounterEnteredWords().toString()
            editText.setText("")
//            btn.isEnabled = false
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.blue1))
        }

        fun game_over() {
            btn.text = "Начать игру заново?"
            textComand.text = getString(R.string.game_over)
            textCounter.text = wordsViewModel.getcounterEnteredWords().toString()
//            btn.isEnabled = true
            editText.isInvisible = true
            btn.setBackgroundColor(getColor(R.color.ping))

        }

        fun new_word() {
            btn.text = "Ввод"
            textComand.text = getString(R.string.new_word)
            textCounter.text = wordsViewModel.getcounterEnteredWords().toString()
            editText.setText("")
//            btn.isEnabled = false
            editText.isInvisible = false

            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun error() {
            textComand.text = getString(R.string.error)
            textCounter.text = wordsViewModel.getcounterEnteredWords().toString()
            btn.setBackgroundColor(getColor(R.color.red1))
            btn.isEnabled = true
            btn.text = "Ok"
            editText.isInvisible = true


        }


        lifecycleScope.launch {

            //для того чтобы работал нужно специальный implementation длбавлять в Билд
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch { wordsViewModel.analysisAndCreateFlowForView() }
                launch {
                    wordsViewModel.buttonFlow.collect {
                        if (it == false){btn.isEnabled = false}
                        else{btn.isEnabled = true}
                    }
                }
                launch { wordsViewModel.giveFlowsMode() }
                launch {
                    wordsViewModel.modeFlow.collect {
                        modes = it

                        when (modes) {
                            "questionStart" -> questionStart()
                            "inputSecondWord" -> inputSecondWord()
                            "answerStart" -> answerStart()
                            "next_word" -> next_word()
                            "game_over" -> game_over()
                            "new_word" -> new_word()
                            "error" -> error()
                        }
                    }
                }
            }
        }


//        fun openButton() {
//
//            if (editText.getText()
//                    .contains(""".*[~?!"№;%:?*())+=<>? !@#$}{'$'%^&*+-0123456789].*""".toRegex()) ||
//                editText.getText()
//                    .toString() == ""
//            ) {
//                btn.isEnabled = false
//            } else btn.isEnabled = true
//        }

        editText.addTextChangedListener { wordsViewModel.openButton(editText.getText().toString()) }

        btn.setOnClickListener {
            when (modes) {
                "questionStart" -> wordsViewModel.app_new_word(editText.getText().toString())
                "inputSecondWord" -> wordsViewModel.app_new_word(editText.getText().toString())
                "answerStart" -> wordsViewModel.check_word(editText.getText().toString())
                "next_word" ->wordsViewModel.check_word(editText.getText().toString())
                "game_over" -> wordsViewModel.game_over()
                "new_word" -> wordsViewModel.app_new_word(editText.getText().toString())
                "error" -> wordsViewModel.errorToinputSecondWord()
            }
            editText.setText("")
        }
    }
}