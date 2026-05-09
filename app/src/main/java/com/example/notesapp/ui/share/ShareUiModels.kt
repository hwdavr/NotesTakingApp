package com.example.notesapp.ui.share

import androidx.compose.ui.graphics.Color
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import java.util.Locale

enum class AccessRole {
    OWNER,
    FULL_ACCESS,
    READ_ONLY
}

data class SharedUserUiModel(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val accentColor: Color,
    val role: AccessRole,
    val isPending: Boolean
)

internal fun buildSharedUserUiModels(ownerEmail: String?, shares: List<NoteShare>): List<SharedUserUiModel> {
    val ownerRow = ownerEmail?.let {
        SharedUserUiModel(
            id = "owner",
            name = deriveDisplayName(it, null),
            email = it,
            initials = deriveInitials(it, null),
            accentColor = accentColorFor(it),
            role = AccessRole.OWNER,
            isPending = false
        )
    }

    val collaboratorRows = shares
        .filterNot { share -> ownerEmail != null && share.email.equals(ownerEmail, ignoreCase = true) }
        .map { share ->
            SharedUserUiModel(
                id = share.id,
                name = deriveDisplayName(share.email, share.displayName),
                email = share.email,
                initials = deriveInitials(share.email, share.displayName),
                accentColor = accentColorFor(share.email),
                role = when (share.accessRole) {
                    NoteShareAccessRole.FULL_ACCESS -> AccessRole.FULL_ACCESS
                    NoteShareAccessRole.READ_ONLY -> AccessRole.READ_ONLY
                },
                isPending = share.status == NoteShareStatus.PENDING
            )
        }

    return listOfNotNull(ownerRow) + collaboratorRows
}

internal fun isValidInviteEmail(email: String): Boolean {
    val normalized = email.trim()
    if (normalized.isBlank()) return false
    return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(normalized)
}

private fun deriveDisplayName(email: String, displayName: String?): String {
    if (!displayName.isNullOrBlank()) return displayName
    val localPart = email.substringBefore('@')
    return localPart
        .split('.', '_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
        .ifBlank { email }
    }

private fun deriveInitials(email: String, displayName: String?): String {
    val source = (displayName ?: email.substringBefore('@')).trim()
    val pieces = source.split(' ', '.', '_', '-').filter { it.isNotBlank() }
    return pieces.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }
}

private fun accentColorFor(seed: String): Color {
    val palette = listOf(
        Color(0xFF6E7BFF),
        Color(0xFF2DB7A3),
        Color(0xFFF59E0B),
        Color(0xFFF97373),
        Color(0xFF7C3AED)
    )
    return palette[kotlin.math.abs(seed.hashCode()) % palette.size]
}
