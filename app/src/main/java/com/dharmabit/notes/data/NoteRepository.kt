package com.dharmabit.notes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    val activePrivateNotes: Flow<List<Note>> = noteDao.getActivePrivateNotes()
    val archivedPrivateNotes: Flow<List<Note>> = noteDao.getArchivedPrivateNotes()
    val allActiveNotes: Flow<List<Note>> = noteDao.getAllActiveNotes()
    val allArchivedNotes: Flow<List<Note>> = noteDao.getAllArchivedNotes()
    val favoriteNotes: Flow<List<Note>> = noteDao.getFavoriteNotes()
    val favoritePrivateNotes: Flow<List<Note>> = noteDao.getFavoritePrivateNotes()
    val allFavoriteNotes: Flow<List<Note>> = noteDao.getAllFavoriteNotes()

    suspend fun insertOrUpdate(note: Note) {
        try {
            noteDao.insertOrUpdateNote(note)
            android.util.Log.d("NoteRepository", "Note saved: ${note.id} - ${note.title}")
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error saving note", e)
            throw e
        }
    }

    suspend fun deleteById(noteId: Int) {
        try {
            val deletedRows = noteDao.deleteNoteById(noteId)
            android.util.Log.d("NoteRepository", "Deleted rows: $deletedRows for ID: $noteId")
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error deleting note by ID", e)
            throw e
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return try {
            noteDao.getNoteById(noteId)
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting note by ID", e)
            null
        }
    }

    suspend fun getEncryptedNotes(): List<Note> {
        return try {
            noteDao.getEncryptedNotes()
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting encrypted notes", e)
            emptyList()
        }
    }

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun searchPrivateNotes(query: String): Flow<List<Note>> = noteDao.searchPrivateNotes(query)

    suspend fun getAllTags(): List<String> {
        return try {
            noteDao.getAllTags()
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting tags", e)
            emptyList()
        }
    }

    suspend fun getActiveNotesCount(): Int {
        return try {
            noteDao.getActiveNotesCount()
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting active notes count", e)
            0
        }
    }

    suspend fun getArchivedNotesCount(): Int {
        return try {
            noteDao.getArchivedNotesCount()
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting archived notes count", e)
            0
        }
    }

    suspend fun getPrivateNotesCount(): Int {
        return try {
            noteDao.getPrivateNotesCount()
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting private notes count", e)
            0
        }
    }

    suspend fun getFavoriteNotesCount(): Int {
        return try {
            noteDao.getFavoriteNotesCount()
        } catch (e: Exception) {
            android.util.Log.e("NoteRepository", "Error getting favorite notes count", e)
            0
        }
    }
}