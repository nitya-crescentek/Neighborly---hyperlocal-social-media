package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val avatarUrl: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val neighborhoodId: Long,
    val isVerified: Boolean = false,
    val role: String = "resident" // resident, business_owner, moderator
)

@Entity(tableName = "neighborhoods")
data class NeighborhoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,
    val state: String
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val neighborhoodId: Long,
    val category: String, // general, lost_pet, safety, recommendation, event, free_item, for_sale, alert
    val title: String,
    val body: String,
    val imageUrls: String, // comma-separated strings
    val isPinned: Boolean = false,
    val isUrgent: Boolean = false,
    val reportsCount: Int = 0,
    val groupId: Long? = null, // null means general feed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val userId: Long,
    val body: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reactions")
data class ReactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val userId: Long,
    val type: String // helpful, thanks, agree
)

@Entity(tableName = "marketplace_listings")
data class MarketplaceListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val neighborhoodId: Long,
    val title: String,
    val description: String,
    val price: Double?, // null means free
    val condition: String, // New, Like New, Good, Fair
    val imageUrls: String, // comma-separated
    val status: String, // available, sold, reserved
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_pages")
data class BusinessPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val neighborhoodId: Long,
    val name: String,
    val category: String,
    val description: String,
    val address: String,
    val phone: String,
    val website: String,
    val hours: String,
    val averageRating: Double = 0.0
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val userId: Long,
    val rating: Int, // 1 to 5
    val body: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdBy: Long,
    val neighborhoodId: Long,
    val type: String, // weather, crime, outage, emergency, other
    val title: String,
    val body: String,
    val severity: String, // low, medium, high, critical
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 3600 * 1000)
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val neighborhoodId: Long,
    val creatorId: Long,
    val name: String,
    val description: String,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val userId: Long,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val recipientId: Long,
    val body: String,
    val readAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_users")
data class BlockedUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val blockedUserId: Long
)
