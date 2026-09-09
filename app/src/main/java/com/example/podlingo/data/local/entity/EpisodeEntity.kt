package com.example.podlingo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = PodcastEntity::class,
            parentColumns = ["id"],
            childColumns = ["podcastId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("podcastId")],
)
data class EpisodeEntity(
    @PrimaryKey val id: String,
    val podcastId: String,
    val title: String,
    val audioUrl: String,
    val localFilePath: String? = null,
    val pubDateEpochMs: Long? = null,
    val durationSec: Long? = null,
    val transcriptStatus: TranscriptStatus = TranscriptStatus.NONE,
    val transcriptError: String? = null,
    val lastPlayedEpochMs: Long? = null,
    /** No longer read or written - kept only so the column stays in the existing schema without a migration. */
    val vocabCalibrated: Boolean = false,
    /** No longer read or written - kept only so the column stays in the existing schema without a migration. Word Check's outstanding-words check is always re-derived from live word-knowledge status instead (see PlayerViewModel.computeOutstandingWordTiers) - a cached "already assessed" flag went stale as soon as word-knowledge data changed afterwards (e.g. deleting words in Settings). */
    val startQuizCompleted: Boolean = false,
)
