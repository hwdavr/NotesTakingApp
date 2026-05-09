package com.example.notesapp.ui.share.model

import androidx.compose.ui.graphics.Color
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import java.util.Locale

enum class AccessRole {
    OWNER,
    EDITOR,
    VIEWER
}

enum class ManageAccessPermission {
    VIEWER,
    EDITOR,
    DELETE
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

data class ManageAccessUserUiModel(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val accentColor: Color,
    val currentPermission: ManageAccessPermission,
    val selectedPermission: ManageAccessPermission,
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
                    NoteShareAccessRole.EDITOR -> AccessRole.EDITOR
                    NoteShareAccessRole.VIEWER -> AccessRole.VIEWER
                },
                isPending = share.status == NoteShareStatus.PENDING
            )
        }
    return listOfNotNull(ownerRow) + collaboratorRows
}

internal fun buildManageAccessUserUiModels(
    shares: List<NoteShare>,
    selectedPermissions: Map<String, ManageAccessPermission> = emptyMap()
): List<ManageAccessUserUiModel> = shares.map { share ->
    val currentPermission = share.accessRole.toManageAccessPermission()
    ManageAccessUserUiModel(
        id = share.id,
        name = deriveDisplayName(share.email, share.displayName),
        email = share.email,
        initials = deriveInitials(share.email, share.displayName),
        accentColor = accentColorFor(share.email),
        currentPermission = currentPermission,
        selectedPermission = selectedPermissions[share.id] ?: currentPermission,
        isPending = share.status == NoteShareStatus.PENDING
    )
}

internal fun ManageAccessPermission.toNoteShareAccessRole(): NoteShareAccessRole = when (this) {
    ManageAccessPermission.VIEWER -> NoteShareAccessRole.VIEWER
    ManageAccessPermission.EDITOR -> NoteShareAccessRole.EDITOR
    ManageAccessPermission.DELETE -> NoteShareAccessRole.VIEWER
}

private fun NoteShareAccessRole.toManageAccessPermission(): ManageAccessPermission = when (this) {
    NoteShareAccessRole.VIEWER -> ManageAccessPermission.VIEWER
    NoteShareAccessRole.EDITOR -> ManageAccessPermission.EDITOR
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
