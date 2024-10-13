package com.example.chainofwords

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase


// В этом файле описывается БД Room

// В этом классе по сути описывается вид таблицы. анатацией @Entity(tableName = "words") об этом
// сообщается руму. tableName = "words" - дает название таблице.
//@ColumnInfo(name = "word") определяет имя столбцов в таблице. по умолчанию, столбец именнуется по
// названию переменной, но можно и принудительно назвать name = "word".
// @PrimaryKey(autoGenerate = true) мы вынесли из конструктора, чтобы его каждый раз не передавать.
// autoGenerate = true будет автоматически генерировать ID строки.
// каждый экземпляр этого класса является строкой таблицы БД!
@Entity(tableName = "words")
data class Word(
    @ColumnInfo(name = "word")
    val word: String
) {@PrimaryKey(autoGenerate = true)
var uid: Int = 0 }


// Этот интерфейс хронит и определяет методы взаимодействия с БД
@Dao
interface WordsDao {
    // addNewWord
    @Insert
    suspend fun insertAll(vararg words: Word)


    // wordExist
    @Query("SELECT exists (select 1 from words Where word = :word)")
    suspend fun wordExist(word: String): Boolean


    // getSizeWords
    @Query("SELECT count (*) FROM words")
    suspend fun getCount(): Int


    // getWordByIndex
    @Query("SELECT word FROM words order by uid LIMIT 1 offset :index")
    suspend fun getWordByIndex(index: Int): String

    // clearListChainOfWords
    @Query("delete from words")
    suspend fun clear()

}

// Этот класс является основной точкой тоступа приложения к БД. как бы собирает в одну кучу таблицу
// БД и методы взаимодействия с ней.
// entities = [Word::class] - здесь информация о таблице,
// abstract fun wordsDao(): WordsDao - здесь информация о метадах.
// Экземпляр этого класса особым образом создается только там где есть контекст, т.е. в активити
@Database(entities = [Word::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao
}

// Этот класс позволяет взаимодействовать с базой данных
class RepositoryWordsRoom(private val roomDatabase: AppDatabase): RepositoryWords {

    private val wordsDao = roomDatabase.wordsDao()

    override suspend fun addNewWord(newWord: String) {
        wordsDao.insertAll(Word(newWord))
    }

    override suspend fun wordExist(newWord: String): Boolean {
        return wordsDao.wordExist(newWord)
    }

    override suspend fun getSizeWords(): Int {
        return wordsDao.getCount()
    }

    override suspend fun getWordByIndex(index: Int): String {
        return wordsDao.getWordByIndex(index)
    }

    override suspend fun clearListChainOfWords() {
        wordsDao.clear()
    }

}