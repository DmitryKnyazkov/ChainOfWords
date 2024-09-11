package com.example.chainofwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class WordsViewModel : ViewModel() {

    val model = Model()

    val listModes: List<String> = listOf(
        "questionStart",           //Добавление слов в "Model"
        "inputSecondWord",         //Добавление слов в "Model"
        "answerStart",             //получение ответа в Model
        "next_word",               //получение ответа в Model
        "game_over",               //состояние из Model
        "new_word",                //Добавление слов в "Model"
        "error"                    //- это не состояние Model,
                                   // а результат операции в Model
    )

    private val mutableModeFlow = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 10)
    val modeFlow = mutableModeFlow.asSharedFlow()


//    enum class Modes {
//        Question, NextWord, GameOver, NewWord
//    }


    fun change_modes() {

    }

   // var listWords: MutableList<String> = model.listChainOfWords
    var counterForCheck_Word = 0
    val counterWords get() = model.countWords()

    init {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
        }
    }

    fun check_word(word: String) {
        val job = viewModelScope.launch {
            if (word == listWords[counterForCheck_Word]) {
                mutableModeFlow.emit("X")
                mutableModeFlow.emit(listModes[3])
                counterForCheck_Word += 1
                if (counterForCheck_Word == listWords.size) {
                    mutableModeFlow.emit(listModes[5])
                    counterForCheck_Word = 0
                }
            } else {
                mutableModeFlow.emit(listModes[4])
                counterForCheck_Word = 0
            }

        }
        job.cancel()
    }

    fun app_new_word(new_word: String) {
//        if (new_word in model.listChainOfWords) {
//            viewModelScope.launch { mutableModeFlow.emit(listModes[6]) }
//        }
//        else {
//            model.listChainOfWords.add(new_word)
//            viewModelScope.launch {
//                if (model.listChainOfWords.size == 1) {
//                    mutableModeFlow.emit(listModes[1])
//                } else {
//                    mutableModeFlow.emit(listModes[2])
//                }
//            }
//        }
        viewModelScope.launch {
            if (!model.addWord(new_word))
            {
                mutableModeFlow.emit(listModes[6])
            } else {
                if (model.countWords() == 1) {
                    mutableModeFlow.emit(listModes[1])
                } else {
                    mutableModeFlow.emit(listModes[2])
                }
            }
        }
    }

    fun errorToinputSecondWord() {
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[1])
        }
    }

    fun game_over(){
        model.clear()
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
        }
    }


    fun getModesFlow(): SharedFlow<String> = modeFlow
}