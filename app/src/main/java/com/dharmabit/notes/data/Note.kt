package com.dharmabit.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.dharmabit.notes.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class NoteType {
    TEXT, CHECKLIST
}

enum class NoteColor {
    DEFAULT, RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE, PINK
}

data class ChecklistItem(
    val id: Long = System.currentTimeMillis(),
    var text: String,
    var isChecked: Boolean
)

@Entity(tableName = "notes_table")
@TypeConverters(ChecklistConverter::class, StringListConverter::class, NoteColorConverter::class)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val noteType: NoteType = NoteType.TEXT,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val imageUri: String? = null,
    val isArchived: Boolean = false,
    val audioPath: String? = null,
    val isPrivate: Boolean = false,
    val isEncrypted: Boolean = false,
    val encryptedContent: String? = null,
    val encryptedTitle: String? = null,
    val hasCustomPassword: Boolean = false,
    val customPasswordHash: String? = null,
    val tags: List<String> = emptyList(),
    val color: NoteColor = NoteColor.DEFAULT,
    val isFavorite: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

class ChecklistConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromChecklist(checklist: List<ChecklistItem>): String = gson.toJson(checklist)
    @TypeConverter
    fun toChecklist(json: String): List<ChecklistItem> {
        return try {
            val type = object : TypeToken<List<ChecklistItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class StringListConverter {
    private val gson = Gson()
    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)
    @TypeConverter
    fun toStringList(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class NoteColorConverter {
    @TypeConverter
    fun fromNoteColor(color: NoteColor): String = color.name
    @TypeConverter
    fun toNoteColor(name: String): NoteColor {
        return try {
            NoteColor.valueOf(name)
        } catch (e: Exception) {
            NoteColor.DEFAULT
        }
    }
}

// Extension functions for encryption/decryption with better error handling
fun Note.getDecryptedTitle(securityManager: SecurityManager): String {
    return if (isEncrypted && encryptedTitle != null) {
        try {
            securityManager.decryptText(encryptedTitle) ?: title
        } catch (e: Exception) {
            android.util.Log.e("Note", "Failed to decrypt title", e)
            title
        }
    } else title
}

fun Note.getDecryptedContent(securityManager: SecurityManager): String {
    return if (isEncrypted && encryptedContent != null) {
        try {
            securityManager.decryptText(encryptedContent) ?: content
        } catch (e: Exception) {
            android.util.Log.e("Note", "Failed to decrypt content", e)
            content
        }
    } else content
}

fun Note.encrypt(securityManager: SecurityManager): Note {
    return try {
        val encTitle = securityManager.encryptText(title)
        val encContent = securityManager.encryptText(content)

        if (encTitle != null && encContent != null) {
            copy(
                isEncrypted = true,
                encryptedTitle = encTitle,
                encryptedContent = encContent,
                title = "",
                content = ""
            )
        } else {
            android.util.Log.e("Note", "Encryption failed, keeping original note")
            this
        }
    } catch (e: Exception) {
        android.util.Log.e("Note", "Encryption error", e)
        this
    }
}

fun Note.decrypt(securityManager: SecurityManager): Note {
    return try {
        copy(
            title = getDecryptedTitle(securityManager),
            content = getDecryptedContent(securityManager),
            isEncrypted = false,
            encryptedTitle = null,
            encryptedContent = null
        )
    } catch (e: Exception) {
        android.util.Log.e("Note", "Decryption error", e)
        this
    }
}

// Helper function to check if note has any content
fun Note.hasContent(): Boolean {
    return title.isNotBlank() ||
            content.isNotBlank() ||
            checklistItems.any { it.text.isNotBlank() } ||
            imageUri != null ||
            audioPath != null
}