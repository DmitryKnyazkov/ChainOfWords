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


@Entity(tableName = "words")
data class Word(
    @ColumnInfo(name = "word") val word: String

) {@PrimaryKey(autoGenerate = true)
var uid: Int = 0 }

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

@Database(entities = [Word::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao
}


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