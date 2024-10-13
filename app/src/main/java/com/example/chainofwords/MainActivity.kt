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
import androidx.room.Room
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wordsViewModel: WordsViewModel by viewModels()

        //Это костыль - комментируем
        //var counterForStart = 0

//        создание экземпляра базы данных приложения. может быть сделан, только там где есть
//        контекст, т.е. в MainActivity
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "words"
        ).build()

//По скольку экземпляр БД приложения в первую очередь нужен во Модуле, через VM передаем его туда.
        wordsViewModel.setDB(db)

        var modes: String = "questionStart"

        val textComand: TextView = findViewById(R.id.textComand)
        val editText = findViewById<EditText>(R.id.editText)
        val btn = findViewById<Button>(R.id.btn)
        val textCounter: TextView = findViewById<TextView>(R.id.textCounter)

        fun setButtonTitle(modes: String) {
            btn.text = when (modes) {
                "game_over" -> "Начать игру заново?"
                "error" -> "Ok"
                else -> "Ввод"
            }
        }

        fun questionStart() {
            textComand.text = getString(R.string.questionStart)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun inputSecondWord() {
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

        fun nextWord() {
            textComand.text = getString(R.string.next_word)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.setBackgroundColor(getColor(R.color.blue1))
        }

        fun gameOver() {
            textComand.text = getString(R.string.game_over)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.isInvisible = true
            btn.setBackgroundColor(getColor(R.color.ping))

        }

        fun newWord() {
            textComand.text = getString(R.string.new_word)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            editText.setText("")
            editText.isInvisible = false
            btn.isEnabled = true

            btn.setBackgroundColor(getColor(R.color.green))

        }

        fun error() {
            textComand.text = getString(R.string.error)
            //Это костыль, тут не нужно
            //wordsViewModel.getSizeWords()
            btn.setBackgroundColor(getColor(R.color.red1))
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

                        setButtonTitle(modes)


//                        if (counterForStart == 0) {modes = "questionStart"}
                        when (modes) {
                            "questionStart" -> questionStart()
                            "inputSecondWord" -> inputSecondWord()
                            "answerStart" -> answerStart()
                            "next_word" -> nextWord()
                            "game_over" -> gameOver()
                            "new_word" -> newWord()
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
                "game_over" -> wordsViewModel.gameOver()
                "new_word" -> wordsViewModel.appNewWord(editText.getText().toString())
                "error" -> wordsViewModel.errorToinputSecondWord()
            }
            editText.setText("")
        }
    }
//    override fun onDestroy() {
//        super.onDestroy()
//        wordsViewModel.gameOver()
//        // Освободить ресурсы, которые больше не нужны активности
//    }


}