package com.example.chainofwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val scope = viewModelScope

    var counterEnteredWords = 0

    fun getcounterEnteredWords(): Int {return counterEnteredWords}

    var modelMode: Model.Modes = Model.Modes.AddNewWord

    suspend fun giveFlowsMode(){
        model.modeFlowFromModel.collect{
            counterEnteredWords = 0
            modelMode = it
            analysisAndCreateFlowForView()
        }
    }


    suspend fun analysisAndCreateFlowForView(){
        if (modelMode == Model.Modes.AddNewWord && counterEnteredWords == 1){mutableModeFlow.emit("inputSecondWord")}
        if (modelMode == Model.Modes.AddNewWord && counterEnteredWords > 1){mutableModeFlow.emit("new_word")}
        if (modelMode == Model.Modes.CheckWord && counterEnteredWords == 0) {mutableModeFlow.emit("answerStart")}
        if (modelMode == Model.Modes.CheckWord && counterEnteredWords > 0) {mutableModeFlow.emit("next_word")}
        if (modelMode == Model.Modes.AddNewWord && counterEnteredWords == 0){mutableModeFlow.emit("questionStart")}
        if (modelMode == Model.Modes.GameOver){mutableModeFlow.emit("game_over")}

//        if (nowInFlowModeFlowFromModel == Model.Modes.CheckWord && nowInFlowModeCounterForCheck_WordFromModel>0){}
    }





    fun check_word(word: String) {
        counterEnteredWords++
        scope.launch { model.check_word(word)
            analysisAndCreateFlowForView()}
    }

    fun getSizeWords() = scope.launch {MutableSizeWordsFlow.emit(model.getSizeWords())}

    fun app_new_word(new_word: String) {
        counterEnteredWords++
        scope.launch {
            var resultRecording = model.add_new_word(new_word)

            if (resultRecording == false) {
//              Если придет фолс, то имитем "error" ошибку и counterEnteredWords откати назад
                counterEnteredWords--
                mutableModeFlow.emit("error")
            }
            else analysisAndCreateFlowForView()

        }
    }

    fun errorToinputSecondWord() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[1])
        }
    }

    fun game_over(){
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
            counterEnteredWords = 0
            modelMode = Model.Modes.AddNewWord
            model.restart()
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
        if (mutableModeFlow.value == "error") {
            viewModelScope.launch {
                MutableButtonFlow.emit(true)
            }
        }
        if (mutableModeFlow.value == "game_over") {
            viewModelScope.launch {
                MutableButtonFlow.emit(true)
            }
        }
    }


    fun getModesFlow(): StateFlow<String> = modeFlow
//    fun getButtonFlow(): StateFlow<Boolean> = buttonFlow


}