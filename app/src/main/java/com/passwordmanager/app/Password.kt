package com.passwordmanager.app

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val username: String,
    val password: String, // Mã hóa
    val website: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY createdAt DESC")
    fun getAllPasswords(): Flow<List<Password>>
    
    @Insert
    suspend fun insert(password: Password)
    
    @Update
    suspend fun update(password: Password)
    
    @Delete
    suspend fun delete(password: Password)
    
    @Query("DELETE FROM passwords")
    suspend fun deleteAll()
}

@Database(entities = [Password::class], version = 1, exportSchema = false)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
    
    companion object {
        @Volatile
        private var INSTANCE: PasswordDatabase? = null
        
        fun getDatabase(context: Context): PasswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PasswordDatabase::class.java,
                    "password_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
