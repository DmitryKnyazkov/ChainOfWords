package com.example.chainofwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class WordsViewModel : ViewModel() {

    private val model = Model()

    private val listModes: List<String> = listOf(
        "questionStart", // AddNewWord Создадим новую цепочку слов.Введите слово
        "inputSecondWord", // AddNewWord Введите следующее слово в цепочку
        "answerStart", // CheckWord Цепочка слов создана.Воспроизведем ее. Введите слово
        "next_word", // CheckWord Вы ответили верно. Вводите следующее слово
        "game_over", // GameOver Вы ответили неверно. Игра закончилась.
        "new_word", // AddNewWord Вы верно воспроизвели всю цепочку слов. Увеличем цепочку. Введите новое слово
        "error" // сообщение Ошибка! Такое слово уже есть в цепочке! Введите слово заново.
    )


    private val mutableModeFlow = MutableStateFlow(listModes[0])
    val modeFlow = mutableModeFlow.asStateFlow()

    private val mutableButtonFlow = MutableStateFlow(false)
    val buttonFlow = mutableButtonFlow.asStateFlow()

    private val mutableSizeWordsFlow = MutableStateFlow(0)
    val sizeWordsFlow = mutableSizeWordsFlow.asStateFlow()

    private val scope = viewModelScope

    //При изменении свойства запускаем изменение mode
    private var counterEnteredWords by Delegates.observable(0) {
            _, _, _ -> scope.launch { analysisAndCreateFlowForView() }
    }

    //Два источника одного значения - оставим одно
    //private var modelMode: Model.Modes = Model.Modes.AddNewWord


    //Название функции отражает то, что должен знать View,
    // а не внутренние алгоритмы ViewModel
    suspend fun onShow() {
        //Лучше запустить от имени viewModel
        scope.launch {
            model.modeFlowFromModel.collect {
                counterEnteredWords = 0
                analysisAndCreateFlowForView()
            }
        }
    }


    private suspend fun analysisAndCreateFlowForView() {
        mutableModeFlow.emit(
            //При такой реализации проще понять, все ли варианты перебраны
            //так же толоко один вызов emit
            mapMode(model.modeFlowFromModel.value,
                counterEnteredWords)
        )

//        if (nowInFlowModeFlowFromModel == Model.Modes.CheckWord && nowInFlowModeCounterForCheck_WordFromModel>0){}
    }


    private fun mapMode(
        modes: Model.Modes,
        counterEnteredWords: Int
    ) = when (modes) {
        Model.Modes.AddNewWord ->
            when (counterEnteredWords) {
                0 -> "questionStart"
                1 -> "inputSecondWord"
                else -> "new_word"
            }

        Model.Modes.CheckWord ->
            when (counterEnteredWords) {
                0 -> "answerStart"
                else -> "next_word"
            }

        Model.Modes.GameOver -> "game_over"
    }


    fun checkWord(word: String) {
        counterEnteredWords++
        scope.launch {
            model.checkWord(word)
        }
    }

    private fun emitCountWords() = scope.launch { mutableSizeWordsFlow.emit(model.getSizeWords()) }

    fun appNewWord(newWord: String) {
        //Не надо прибавлять заранее
        //counterEnteredWords++
        scope.launch {
            val resultRecording = model.addNewWord(newWord)

            if (!resultRecording) {
//              Если придет фолс, то имитем "error" ошибку и counterEnteredWords откати назад
//                counterEnteredWords--
                mutableModeFlow.emit("error")
            } else {
                counterEnteredWords++
                emitCountWords()
            }

        }
    }

    fun errorToinputSecondWord() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[1])
        }
    }

    fun gameOver() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
            counterEnteredWords = 0
            //Это явно костыль
            //modelMode = Model.Modes.AddNewWord
            model.restart()
            emitCountWords()
        }
    }

    fun openButton(editText: String) {

        if (editText.contains(""".*[~?!"№;%:*()+=<> @#$}{'^&+-0123456789].*""".toRegex()) ||
            editText.toString() == ""
        ) {
            viewModelScope.launch {
                mutableButtonFlow.emit(false)
            }
        } else {
            viewModelScope.launch {
                mutableButtonFlow.emit(true)
            }
        }
        if (mutableModeFlow.value in arrayOf("error","game_over")) {
            viewModelScope.launch {
                mutableButtonFlow.emit(true)
            }
        }
//        if (mutableModeFlow.value == "game_over") {
//            viewModelScope.launch {
//                MutableButtonFlow.emit(true)
//            }
//        }
    }


}