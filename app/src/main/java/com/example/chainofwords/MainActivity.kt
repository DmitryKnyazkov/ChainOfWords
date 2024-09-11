package com.example.chainofwords

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
            btn.setText("Ввод")
            textComand.setText(getString(R.string.questionStart))
            textCounter.setText(wordsViewModel.counterWords.toString())
            editText.setText("")
            btn.isEnabled = false
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun inputSecondWord() {
            btn.setText("Ввод")
            textComand.setText(getString(R.string.inputSecondWord))
            textCounter.setText(wordsViewModel.counterWords.toString())
            editText.setText("")
            btn.isEnabled = false
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun answerStart() {
            textComand.setText(getString(R.string.answerStart))
            textCounter.setText(wordsViewModel.counterWords.toString())
            btn.isEnabled = false
            editText.isInvisible = false
            editText.setText("")
            btn.setBackgroundColor(getColor(R.color.blue1))

        }

        fun next_word() {
            textComand.setText(getString(R.string.next_word))
            textCounter.setText(wordsViewModel.counterWords.toString())
            editText.setText("")
            btn.isEnabled = false
            editText.isInvisible = false

            btn.setBackgroundColor(getColor(R.color.blue1))

        }

        fun game_over() {
            btn.setText("Начать игру заново?")
            textComand.setText(getString(R.string.game_over))
            textCounter.setText(wordsViewModel.counterWords.toString())
            btn.isEnabled = true
            editText.isInvisible = true
            btn.setBackgroundColor(getColor(R.color.ping))

        }

        fun new_word() {
            btn.setText("Ввод")
            textComand.setText(getString(R.string.new_word))
            textCounter.setText(wordsViewModel.counterWords.toString())
            editText.setText("")
            btn.isEnabled = false
            editText.isInvisible = false

            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun error(){
            textComand.setText(getString(R.string.error))
            textCounter.setText(wordsViewModel.counterWords.toString())
            btn.setBackgroundColor(getColor(R.color.red1))
            btn.isEnabled = true
            btn.setText("Ok")
            editText.isInvisible = true


        }

        lifecycleScope.launch {

            //для того чтобы работал нужно специальный implementation длбавлять в Билд
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    wordsViewModel.getModesFlow().collect {
                        Log.d("chainofworld","getting $it")
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

        fun openButton() {

            if (editText.getText()
                    .contains(""".*[~?!"№;%:?*())+=<>? !@#$}{'$'%^&*+-0123456789].*""".toRegex()) ||
//                editText.getText().contains(""".*\s.*""".toRegex()) ||
                editText.getText()
                    .toString() == ""
            ) {
                btn.isEnabled = false
            } else btn.isEnabled = true
        }

        editText.addTextChangedListener { openButton() }

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
        }
    }
}