package com.example.chainofwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


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

    private val MutableButtonFlow = MutableStateFlow(false)
    val buttonFlow = MutableButtonFlow.asStateFlow()

    private val MutableSizeWordsFlow = MutableStateFlow(0)
    val sizeWordsFlow = MutableSizeWordsFlow.asStateFlow()

    private val scope = viewModelScope

    private var counterEnteredWords = 0

    private var modelMode: Model.Modes = Model.Modes.AddNewWord


    //Название функции отражает то, что должен знать View,
    // а не внутренние алгоритмы ViewModel
    suspend fun onShow() {
        model.modeFlowFromModel.collect {
            counterEnteredWords = 0
            modelMode = it
            analysisAndCreateFlowForView()
        }
    }


    private suspend fun analysisAndCreateFlowForView() {
        mutableModeFlow.emit(
            //При такой реализации проще понять, все ли варианты перебраны
            //так же толоко один вызов emit
            when (modelMode) {
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
        )

//        if (nowInFlowModeFlowFromModel == Model.Modes.CheckWord && nowInFlowModeCounterForCheck_WordFromModel>0){}
    }


    fun checkWord(word: String) {
        counterEnteredWords++
        scope.launch {
            model.checkWord(word)
            analysisAndCreateFlowForView()
        }
    }

    private fun getSizeWords() = scope.launch { MutableSizeWordsFlow.emit(model.getSizeWords()) }

    fun appNewWord(new_word: String) {
        //Не надо прибавлять заранее
        //counterEnteredWords++
        scope.launch {
            val resultRecording = model.addNewWord(new_word)

            if (!resultRecording) {
//              Если придет фолс, то имитем "error" ошибку и counterEnteredWords откати назад
//                counterEnteredWords--
                mutableModeFlow.emit("error")
            } else {
                counterEnteredWords++
                analysisAndCreateFlowForView()
                getSizeWords()
            }

        }
    }

    fun errorToinputSecondWord() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[1])
        }
    }

    fun game_over() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
            counterEnteredWords = 0
            modelMode = Model.Modes.AddNewWord
            model.restart()
            getSizeWords()
        }
    }

    fun openButton(editText: String) {

        if (editText.contains(""".*[~?!"№;%:*()+=<> @#$}{'^&+-0123456789].*""".toRegex()) ||
            editText.toString() == ""
        ) {
            viewModelScope.launch {
                MutableButtonFlow.emit(false)
            }
        } else {
            viewModelScope.launch {
                MutableButtonFlow.emit(true)
            }
        }
        if (mutableModeFlow.value in arrayOf("error","game_over")) {
            viewModelScope.launch {
                MutableButtonFlow.emit(true)
            }
        }
//        if (mutableModeFlow.value == "game_over") {
//            viewModelScope.launch {
//                MutableButtonFlow.emit(true)
//            }
//        }
    }


}