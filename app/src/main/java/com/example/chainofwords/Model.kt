package com.example.chainofwords

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

//Модель - она должна отвечать только за одну вещь
//         только за логику игры
class Model {
    //что-то другое должно отвечать за хранение слов - репозиторий
    private val listChainOfWords = mutableListOf<String>()

    private val scope = CoroutineScope(Dispatchers.Default)

    private var numberCurrentWord = 0

    //Добавление слов(а) -> проверка слова (несколько раз) -> конец игры
    //                                                     -> добавление слов(а)

    fun checkWord(word: String) =
        if (word == listChainOfWords[numberCurrentWord]) {
            numberCurrentWord++
            true
        } else {
            clear()
            false
        }


    fun addWord(word: String) =
        if (word in listChainOfWords) false
        else {
            listChainOfWords.add(word)
            true
        }

    fun countWords() = listChainOfWords.size

    fun clear() {
        listChainOfWords.clear()
    }


}
