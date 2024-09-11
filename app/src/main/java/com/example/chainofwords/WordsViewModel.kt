package com.example.chainofwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordsViewModel : ViewModel() {

    private val model = Model()

    private val listModes: List<String> = listOf(
        "questionStart",
        "inputSecondWord",
        "answerStart",
        "next_word",
        "game_over",
        "new_word",
        "error"
    )

    private val mutableModeFlow = MutableStateFlow(listModes[0])
    private val modeFlow = mutableModeFlow.asStateFlow()


//    enum class Modes {
//        Question, NextWord, GameOver, NewWord
//    }


    fun change_modes() {

    }

    private var listWords: MutableList<String> = model.listChainOfWords
    private var counterForCheck_Word = 0
    val counterWords: Int get() = listWords.size


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

        viewModelScope.launch {
            if (new_word in model.listChainOfWords) {
                mutableModeFlow.emit(listModes[6])
            } else {
                model.listChainOfWords.add(new_word)
                if (model.listChainOfWords.size == 1) {
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
        model.listChainOfWords.clear()
        viewModelScope.launch {
            mutableModeFlow.emit(listModes[0])
        }
    }


    fun getModesFlow(): StateFlow<String> = modeFlow
}