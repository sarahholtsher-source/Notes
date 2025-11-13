package com.dharmabit.notes.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dharmabit.notes.data.*
import com.dharmabit.notes.security.SecurityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NoteStats(
    val totalNotes: Int = 0,
    val archivedNotes: Int = 0,
    val privateNotes: Int = 0,
    val favoriteNotes: Int = 0
)

enum class SortBy {
    DATE_MODIFIED, DATE_CREATED, TITLE, COLOR
}

class NoteViewModel(application: Application) : ViewModel() {
    private val repository: NoteRepository
    private val securityManager = SecurityManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isPrivateMode = MutableStateFlow(false)
    val isPrivateMode = _isPrivateMode.asStateFlow()

    private val _isUnlocked = MutableStateFlow(!securityManager.isSecurityEnabled())
    val isUnlocked = _isUnlocked.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.DATE_MODIFIED)
    val sortBy = _sortBy.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()

    private val _noteStats = MutableStateFlow(NoteStats())
    val noteStats = _noteStats.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val activeNotes: StateFlow<List<Note>>
    val archivedNotes: StateFlow<List<Note>>
    val favoriteNotes: StateFlow<List<Note>>

    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)

        activeNotes = combine(
            _isPrivateMode,
            _searchQuery,
            _sortBy,
            _selectedTags,
            repository.activeNotes,
            repository.activePrivateNotes,
            repository.allActiveNotes
        ) { values: Array<Any?> ->
            val isPrivate = values[0] as Boolean
            val query = values[1] as String
            val sort = values[2] as SortBy
            val tags = values[3] as Set<*>
            val regularNotes = values[4] as List<*>
            val privateNotes = values[5] as List<*>
            val allNotes = values[6] as List<*>

            val notesToShow = when {
                !securityManager.isSecurityEnabled() -> allNotes as List<Note>
                isPrivate -> privateNotes as List<Note>
                else -> regularNotes as List<Note>
            }

            @Suppress("UNCHECKED_CAST")
            processNotes(notesToShow, query, sort, tags as Set<String>)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        archivedNotes = combine(
            _isPrivateMode,
            _searchQuery,
            _sortBy,
            repository.archivedNotes,
            repository.archivedPrivateNotes,
            repository.allArchivedNotes
        ) { values: Array<Any?> ->
            val isPrivate = values[0] as Boolean
            val query = values[1] as String
            val sort = values[2] as SortBy
            val regularNotes = values[3] as List<*>
            val privateNotes = values[4] as List<*>
            val allNotes = values[5] as List<*>

            val notesToShow = when {
                !securityManager.isSecurityEnabled() -> allNotes as List<Note>
                isPrivate -> privateNotes as List<Note>
                else -> regularNotes as List<Note>
            }

            processNotes(notesToShow, query, sort, emptySet())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        favoriteNotes = combine(
            _isPrivateMode,
            _searchQuery,
            repository.favoriteNotes,
            repository.favoritePrivateNotes,
            repository.allFavoriteNotes
        ) { values: Array<Any?> ->
            val isPrivate = values[0] as Boolean
            val query = values[1] as String
            val regularFav = values[2] as List<*>
            val privateFav = values[3] as List<*>
            val allFav = values[4] as List<*>

            val notesToShow = when {
                !securityManager.isSecurityEnabled() -> allFav as List<Note>
                isPrivate -> privateFav as List<Note>
                else -> regularFav as List<Note>
            }

            processNotes(notesToShow, query, SortBy.DATE_MODIFIED, emptySet())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        loadStatistics()
    }

    private fun processNotes(
        notes: List<Note>,
        query: String,
        sort: SortBy,
        tags: Set<String>
    ): List<Note> {
        var processed = notes.map { note ->
            if (note.isEncrypted && securityManager.isSecurityEnabled()) {
                note.copy(
                    title = note.getDecryptedTitle(securityManager),
                    content = note.getDecryptedContent(securityManager)
                )
            } else note
        }

        if (query.isNotBlank()) {
            processed = processed.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }

        if (tags.isNotEmpty()) {
            processed = processed.filter { note ->
                note.tags.any { it in tags }
            }
        }

        processed = when (sort) {
            SortBy.DATE_MODIFIED -> processed.sortedByDescending { it.lastModified }
            SortBy.DATE_CREATED -> processed.sortedByDescending { it.timestamp }
            SortBy.TITLE -> processed.sortedBy { it.title.lowercase() }
            SortBy.COLOR -> processed.sortedBy { it.color.ordinal }
        }

        return processed
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = NoteStats(
                    totalNotes = repository.getActiveNotesCount(),
                    archivedNotes = repository.getArchivedNotesCount(),
                    privateNotes = repository.getPrivateNotesCount(),
                    favoriteNotes = repository.getFavoriteNotesCount()
                )
                _noteStats.value = stats
            } catch (e: Exception) {
                android.util.Log.e("NoteViewModel", "Error loading statistics", e)
            }
        }
    }

    // Security methods
    fun getSecurityManager() = securityManager

    fun unlock() {
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun togglePrivateMode() {
        if (securityManager.isPrivateNotesEnabled()) {
            _isPrivateMode.value = !_isPrivateMode.value
        }
    }

    fun disableSecurity() {
        viewModelScope.launch {
            try {
                val encryptedNotes = repository.getEncryptedNotes()
                encryptedNotes.forEach { note ->
                    val decryptedNote = note.decrypt(securityManager)
                    repository.insertOrUpdate(decryptedNote)
                }

                securityManager.disableSecurity()
                _isUnlocked.value = true
                _isPrivateMode.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to disable security: ${e.message}"
                android.util.Log.e("NoteViewModel", "Error disabling security", e)
            }
        }
    }

    // Note operations
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun toggleTag(tag: String) {
        _selectedTags.value = if (tag in _selectedTags.value) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun clearTagFilters() {
        _selectedTags.value = emptySet()
    }

    fun insertOrUpdate(note: Note) = viewModelScope.launch {
        try {
            val noteToSave = note.copy(
                lastModified = System.currentTimeMillis(),
                isPrivate = if (securityManager.isSecurityEnabled()) _isPrivateMode.value else false
            )

            val finalNote = if (securityManager.isSecurityEnabled() && _isPrivateMode.value) {
                noteToSave.encrypt(securityManager)
            } else {
                noteToSave
            }

            repository.insertOrUpdate(finalNote)
            loadStatistics()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to save note: ${e.message}"
            android.util.Log.e("NoteViewModel", "Error saving note", e)
        }
    }

    fun deleteById(noteId: Int) = viewModelScope.launch {
        try {
            repository.deleteById(noteId)
            loadStatistics()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete note: ${e.message}"
            android.util.Log.e("NoteViewModel", "Error deleting note", e)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return try {
            val note = repository.getNoteById(id)
            if (note?.isEncrypted == true && securityManager.isSecurityEnabled()) {
                note.copy(
                    title = note.getDecryptedTitle(securityManager),
                    content = note.getDecryptedContent(securityManager)
                )
            } else note
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load note: ${e.message}"
            android.util.Log.e("NoteViewModel", "Error getting note by ID", e)
            null
        }
    }

    fun archiveNote(note: Note) = insertOrUpdate(note.copy(isArchived = true, isPinned = false))
    fun unarchiveNote(note: Note) = insertOrUpdate(note.copy(isArchived = false))
    fun toggleFavorite(note: Note) = insertOrUpdate(note.copy(isFavorite = !note.isFavorite))
    fun togglePin(note: Note) = insertOrUpdate(note.copy(isPinned = !note.isPinned))

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun getAllTags(): List<String> {
        return try {
            val tagJsonList = repository.getAllTags()
            tagJsonList.flatMap { tagListJson ->
                try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                    val tags: List<String> = gson.fromJson(tagListJson, type)
                    tags
                } catch (e: Exception) {
                    emptyList()
                }
            }.distinct().sorted()
        } catch (e: Exception) {
            android.util.Log.e("NoteViewModel", "Error getting tags", e)
            emptyList()
        }
    }
}

class NoteViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}