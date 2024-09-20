package com.example.chainofwords

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RepositoryWords() {
    private val listChainOfWords = mutableListOf<String>()

    suspend fun addNewWord(new_word: String) {
        listChainOfWords.add(new_word)
    }

    suspend fun wordExist(new_word: String): Boolean {
        return new_word in listChainOfWords
    }

    suspend fun getSizeWords(): Int {
        return listChainOfWords.size
    }

    suspend fun getWordByIndex(index: Int): String {
        return listChainOfWords[index]
    }

    suspend fun clearlistChainOfWords() {
        listChainOfWords.clear()
    }
}

class RepositoryRecords() {
    var record = 0
}

class Model() {

    enum class Modes{
        AddNewWord, CheckWord, GameOver
    }

    private val repositoryWords = RepositoryWords()

    private var numberAddingWords = 2

//  по этому потоку передается состояие. либо "режим добавления нового слова", либо "режим сравнивания слов с цепочкой"
    private val mutableModeFlowFromModel = MutableStateFlow(Modes.AddNewWord)
//   В этой переменной текущий номер слова в цепи для сравнения или количество слов даваемых в начале игры
    private var counterForCheck_Word = 0

    val modeFlowFromModel = mutableModeFlowFromModel.asStateFlow()



    suspend fun add_new_word(new_word: String): Boolean {
        assert(
            mutableModeFlowFromModel.value in arrayOf(
                Modes.AddNewWord,
                Modes.GameOver
            )
        )
//      Здесь проверяем есть ли такое слово в списке. если уже есть, то функция вернет false
        if (repositoryWords.wordExist(new_word)) {
            return false
        } else {
            repositoryWords.addNewWord(new_word)
//          numberAddingWords контролирует количество вводимых слов. в начале игры это 2 слова,
//          когда увеличивается цепочка, то это 1 слово. слова вводятся до того как numberAddingWords
//          станет нулем. и произойдет имит нового состояния.
            numberAddingWords --
            if (numberAddingWords == 0) {mutableModeFlowFromModel.emit(Modes.CheckWord)}
            return true
        }

    }


    suspend fun check_word(word: String) {

        assert(
            mutableModeFlowFromModel.value in arrayOf(
                Modes.CheckWord
            )
        )
//      Проверяем пришедшее слово (word) соответствует ли оно нужному слову из сохранненых
        if (word == repositoryWords.getWordByIndex(counterForCheck_Word)) {
//          Если да то добавляем к счетчику единицу. Для того чтобы в следующей раз сравнивать
//          следующее слово по списку
            counterForCheck_Word += 1

//          Проверяем все ли мы проверили слова из списка. Если каунтер равен колчеству слов в списке,
//          значит мы все слова перебрали и список нужно увеличивать.
            if (counterForCheck_Word == repositoryWords.getSizeWords()) {
//              Посылаем во VM сигнал, что пора присылать новое слово для списка
                mutableModeFlowFromModel.emit(Modes.AddNewWord)
//              Обнуляем каунтер для последующих проверок слов начиная с первого в списке
                counterForCheck_Word = 0
//              numberAddingWords = 1 для того чтобы функции add_new_word сообщть, что добавить нужно
//              только одно слово к списку. add_new_word в 65 строке вычтет 1 и в следующей строке
//              поймет что слова для ввода закончились
                numberAddingWords = 1
            }
        } else {
//          Если было введено неправильное слово, то передаем VM конец игры
            mutableModeFlowFromModel.emit(Modes.GameOver)
        }


    }

    suspend fun restart(){
        counterForCheck_Word = 0
        repositoryWords.clearlistChainOfWords()
        numberAddingWords = 2
//        mutableModeFlowFromModel.emit(Modes.AddNewWord)
    }
}