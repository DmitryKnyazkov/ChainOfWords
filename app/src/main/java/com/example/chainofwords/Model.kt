package com.example.chainofwords

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Это скорее вспомогательный абстрактный класс, чтобы не забыть какие функции нужно реализовать
// в классе который будет управлять БД
interface RepositoryWords {
    suspend fun addNewWord(newWord: String)
    suspend fun wordExist(newWord: String): Boolean
    suspend fun getSizeWords(): Int
    suspend fun getWordByIndex(index: Int): String
    suspend fun clearListChainOfWords()

}


//Этот класс использовался, когда цепочка слов хранилась в списке, т.е. по сути в этом классе.
// когда появилась БД то он уже не используется.
class RepositoryWordsList(): RepositoryWords {

    private val listChainOfWords = mutableListOf<String>()

    override suspend fun addNewWord(newWord: String) {
        listChainOfWords.add(newWord)
    }

    override suspend fun wordExist(newWord: String): Boolean {
        return newWord in listChainOfWords
    }

    override suspend fun getSizeWords(): Int {
        return listChainOfWords.size
    }

    override suspend fun getWordByIndex(index: Int): String {
        return listChainOfWords[index]
    }

    override suspend fun clearListChainOfWords() {
        listChainOfWords.clear()
    }
}

class RepositoryRecords() {
    var record = 0
}

// Этот класс определяет и отправляет состаяния во VM. в конструкторе private val repositoryWords
// определяет взаимодействие с какой БД или репозиторием он будет работать.
class Model(private val repositoryWords: RepositoryWords = RepositoryWordsList()) {

    enum class Modes{
        AddNewWord, CheckWord, GameOver
    }


    private var numberAddingWords = 2

//  по этому потоку передается состояие. либо "режим добавления нового слова", либо "режим сравнивания слов с цепочкой"
    private val mutableModeFlowFromModel = MutableStateFlow(Modes.AddNewWord)
//   В этой переменной текущий номер слова в цепи для сравнения или количество слов даваемых в начале игры
    private var counterForCheckWord = 0

    val modeFlowFromModel = mutableModeFlowFromModel.asStateFlow()



    suspend fun addNewWord(newWord: String): Boolean {

//        Здесь происходит некая проверка, утверждение о том, во соответствующее фло отправляется
        //        именно определенные состояния. если во фло отправится что-то другое, то приложение упадет.
        assert(
            mutableModeFlowFromModel.value in arrayOf(
                Modes.AddNewWord,
                Modes.GameOver
            )
        )
//      Здесь проверяем есть ли такое слово в списке. если уже есть, то функция вернет false
        if (repositoryWords.wordExist(newWord)) {
            return false
        } else {
            repositoryWords.addNewWord(newWord)
//          numberAddingWords контролирует количество вводимых слов. в начале игры это 2 слова,
//          когда увеличивается цепочка, то это 1 слово. слова вводятся до того как numberAddingWords
//          станет нулем. и произойдет имит нового состояния.
            numberAddingWords --
            if (numberAddingWords == 0) {mutableModeFlowFromModel.emit(Modes.CheckWord)}
            return true
        }

    }

//Эта функция проверяет правильноть ответа и определяет все ли мы проверили слова из списка
//    имитит нужный сигнал для VM
    suspend fun checkWord(word: String) {

        assert(
            mutableModeFlowFromModel.value in arrayOf(
                Modes.CheckWord
            )
        )
//      Проверяем пришедшее слово (word) соответствует ли оно нужному слову из сохранненых
        if (word == repositoryWords.getWordByIndex(counterForCheckWord)) {
//          Если да то добавляем к счетчику единицу. Для того чтобы в следующей раз сравнивать
//          следующее слово по списку
            counterForCheckWord += 1

//          Проверяем все ли мы проверили слова из списка. Если каунтер равен колчеству слов в списке,
//          значит мы все слова перебрали и список нужно увеличивать.
            if (counterForCheckWord == repositoryWords.getSizeWords()) {
//              Посылаем во VM сигнал, что пора присылать новое слово для списка
                mutableModeFlowFromModel.emit(Modes.AddNewWord)
//              Обнуляем каунтер для последующих проверок слов начиная с первого в списке
                counterForCheckWord = 0
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
        counterForCheckWord = 0
        repositoryWords.clearListChainOfWords()
        numberAddingWords = 2
        mutableModeFlowFromModel.emit(Modes.AddNewWord)
    }

    suspend fun getSizeWords(): Int {
        return repositoryWords.getSizeWords()
    }
}

