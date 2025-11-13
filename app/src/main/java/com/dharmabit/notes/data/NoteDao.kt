package com.dharmabit.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note): Int

    @Query("DELETE FROM notes_table WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int): Int

    @Query("SELECT * FROM notes_table WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    // This regular notes (non-private)
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND (isPrivate = 0 OR isPrivate IS NULL) ORDER BY isPinned DESC, isFavorite DESC, lastModified DESC")
    fun getActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND (isPrivate = 0 OR isPrivate IS NULL) ORDER BY lastModified DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    // This private notes
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 AND isPrivate = 1 ORDER BY isPinned DESC, isFavorite DESC, lastModified DESC")
    fun getActivePrivateNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 AND isPrivate = 1 ORDER BY lastModified DESC")
    fun getArchivedPrivateNotes(): Flow<List<Note>>

    // All notes (for when security is disabled)
    @Query("SELECT * FROM notes_table WHERE isArchived = 0 ORDER BY isPinned DESC, isFavorite DESC, lastModified DESC")
    fun getAllActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isArchived = 1 ORDER BY lastModified DESC")
    fun getAllArchivedNotes(): Flow<List<Note>>

    // Fav notes
    @Query("SELECT * FROM notes_table WHERE isFavorite = 1 AND isArchived = 0 AND (isPrivate = 0 OR isPrivate IS NULL) ORDER BY lastModified DESC")
    fun getFavoriteNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isFavorite = 1 AND isArchived = 0 AND isPrivate = 1 ORDER BY lastModified DESC")
    fun getFavoritePrivateNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE isFavorite = 1 AND isArchived = 0 ORDER BY lastModified DESC")
    fun getAllFavoriteNotes(): Flow<List<Note>>

    // Get encrypted notes (for batch decryption if needed)
    @Query("SELECT * FROM notes_table WHERE isEncrypted = 1")
    suspend fun getEncryptedNotes(): List<Note>

    // Check if note exists
    @Query("SELECT COUNT(*) FROM notes_table WHERE id = :noteId")
    suspend fun noteExists(noteId: Int): Int

    // Search functionality
    @Query("""
        SELECT * FROM notes_table 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        AND isArchived = 0 
        AND (isPrivate = 0 OR isPrivate IS NULL)
        ORDER BY lastModified DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("""
        SELECT * FROM notes_table 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        AND isArchived = 0 
        AND isPrivate = 1
        ORDER BY lastModified DESC
    """)
    fun searchPrivateNotes(query: String): Flow<List<Note>>

    @Query("SELECT DISTINCT tags FROM notes_table WHERE tags != '[]'")
    suspend fun getAllTags(): List<String>

    @Query("SELECT COUNT(*) FROM notes_table WHERE isArchived = 0 AND (isPrivate = 0 OR isPrivate IS NULL)")
    suspend fun getActiveNotesCount(): Int

    @Query("SELECT COUNT(*) FROM notes_table WHERE isArchived = 1")
    suspend fun getArchivedNotesCount(): Int

    @Query("SELECT COUNT(*) FROM notes_table WHERE isPrivate = 1")
    suspend fun getPrivateNotesCount(): Int

    @Query("SELECT COUNT(*) FROM notes_table WHERE isFavorite = 1")
    suspend fun getFavoriteNotesCount(): Int
}