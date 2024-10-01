package com.example.chainofwords

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

        //Это костыль - комментируем
        //var counterForStart = 0



        var modes: String = "questionStart"

        val textComand: TextView = findViewById(R.id.textComand)
        val editText = findViewById<EditText>(R.id.editText)
        val btn = findViewById<Button>(R.id.btn)
        val textCounter: TextView = findViewById<TextView>(R.id.textCounter)


        fun questionStart() {
            btn.text = "Ввод"
            textComand.text = getString(R.string.questionStart)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun inputSecondWord() {
            btn.text = "Ввод"
            textComand.text = getString(R.string.inputSecondWord)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun answerStart() {
            textComand.text = getString(R.string.answerStart)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.blue1))

        }

        fun next_word() {
            textComand.text = getString(R.string.next_word)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.blue1))
        }

        fun game_over() {
            btn.text = "Начать игру заново?"
            textComand.text = getString(R.string.game_over)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.isInvisible = true
            btn.setBackgroundColor(getColor(R.color.ping))

        }

        fun new_word() {
            btn.text = "Ввод"
            textComand.text = getString(R.string.new_word)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false

            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun error() {
            textComand.text = getString(R.string.error)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
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
                        btn.isEnabled = it
                    }
                }
                launch { wordsViewModel.onShow() }
                launch { wordsViewModel.sizeWordsFlow.collect {textCounter.text = it.toString()} }
                launch {
                    wordsViewModel.modeFlow.collect {
                        modes = it
//                        if (counterForStart == 0) {modes = "questionStart"}
                        when (modes) {
                            "questionStart" -> questionStart()
                            "inputSecondWord" -> inputSecondWord()
                            "answerStart" -> answerStart()
                            "next_word" -> next_word()
                            "game_over" -> game_over()
                            "new_word" -> new_word()
                            "error" -> error()
                        }
  //                      counterForStart++
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
                "questionStart" -> wordsViewModel.appNewWord(editText.getText().toString())
                "inputSecondWord" -> wordsViewModel.appNewWord(editText.getText().toString())
                "answerStart" -> wordsViewModel.checkWord(editText.getText().toString())
                "next_word" ->wordsViewModel.checkWord(editText.getText().toString())
                "game_over" -> wordsViewModel.game_over()
                "new_word" -> wordsViewModel.appNewWord(editText.getText().toString())
                "error" -> wordsViewModel.errorToinputSecondWord()
            }
            editText.setText("")
        }
    }
}