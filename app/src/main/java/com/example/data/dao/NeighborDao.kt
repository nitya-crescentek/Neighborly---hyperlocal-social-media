package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NeighborDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getUsersByIds(ids: List<Long>): List<UserEntity>

    @Query("SELECT * FROM users WHERE neighborhoodId = :neighborhoodId")
    fun getUsersInNeighborhood(neighborhoodId: Long): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)


    // --- Neighborhoods ---
    @Query("SELECT * FROM neighborhoods")
    fun getAllNeighborhoodsFlow(): Flow<List<NeighborhoodEntity>>

    @Query("SELECT * FROM neighborhoods")
    suspend fun getAllNeighborhoods(): List<NeighborhoodEntity>

    @Query("SELECT * FROM neighborhoods WHERE id = :id LIMIT 1")
    suspend fun getNeighborhoodById(id: Long): NeighborhoodEntity?

    @Query("SELECT * FROM neighborhoods WHERE name = :name LIMIT 1")
    suspend fun getNeighborhoodByName(name: String): NeighborhoodEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNeighborhood(neighborhood: NeighborhoodEntity): Long


    // --- Posts ---
    @Query("SELECT * FROM posts WHERE neighborhoodId = :neighborhoodId AND groupId IS NULL AND reportsCount < 5 ORDER BY isPinned DESC, isUrgent DESC, createdAt DESC")
    fun getGeneralPosts(neighborhoodId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE neighborhoodId = :neighborhoodId AND groupId IS NULL AND category = :category AND reportsCount < 5 ORDER BY isPinned DESC, isUrgent DESC, createdAt DESC")
    fun getPostsByCategory(neighborhoodId: Long, category: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE groupId = :groupId AND reportsCount < 5 ORDER BY createdAt DESC")
    fun getGroupPosts(groupId: Long): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Query("UPDATE posts SET reportsCount = reportsCount + 1 WHERE id = :postId")
    suspend fun incrementPostReport(postId: Long)

    @Query("UPDATE posts SET isPinned = :isPinned WHERE id = :postId")
    suspend fun updatePostPinnedState(postId: Long, isPinned: Boolean)


    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long


    // --- Reactions ---
    @Query("SELECT * FROM reactions WHERE postId = :postId")
    fun getReactionsForPost(postId: Long): Flow<List<ReactionEntity>>

    @Query("SELECT * FROM reactions WHERE postId = :postId AND userId = :userId LIMIT 1")
    suspend fun getUserReactionForPost(postId: Long, userId: Long): ReactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: ReactionEntity): Long

    @Query("DELETE FROM reactions WHERE postId = :postId AND userId = :userId")
    suspend fun deleteReaction(postId: Long, userId: Long)


    // --- Marketplace Listings ---
    @Query("SELECT * FROM marketplace_listings WHERE neighborhoodId = :neighborhoodId ORDER BY createdAt DESC")
    fun getMarketplaceListings(neighborhoodId: Long): Flow<List<MarketplaceListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceListingEntity): Long

    @Query("UPDATE marketplace_listings SET status = :status WHERE id = :listingId")
    suspend fun updateListingStatus(listingId: Long, status: String)


    // --- Business Pages & Reviews ---
    @Query("SELECT * FROM business_pages WHERE neighborhoodId = :neighborhoodId")
    fun getBusinessesInNeighborhood(neighborhoodId: Long): Flow<List<BusinessPageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessPage(business: BusinessPageEntity): Long

    @Query("SELECT * FROM reviews WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getReviewsForBusiness(businessId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("UPDATE business_pages SET averageRating = :avgRating WHERE id = :businessId")
    suspend fun updateBusinessAverageRating(businessId: Long, avgRating: Double)


    // --- Alerts ---
    @Query("SELECT * FROM alerts WHERE neighborhoodId = :neighborhoodId AND expiresAt > :now ORDER BY createdAt DESC")
    fun getActiveAlerts(neighborhoodId: Long, now: Long): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE neighborhoodId = :neighborhoodId ORDER BY createdAt DESC")
    fun getAllAlertsHistory(neighborhoodId: Long): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long


    // --- Groups ---
    @Query("SELECT * FROM groups WHERE neighborhoodId = :neighborhoodId")
    fun getGroupsInNeighborhood(neighborhoodId: Long): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getGroupMembers(groupId: Long): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId LIMIT 1")
    suspend fun getGroupMembership(groupId: Long, userId: Long): GroupMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity): Long

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeGroupMember(groupId: Long, userId: Long)


    // --- Direct Messaging & User Blocking ---
    @Query("SELECT * FROM direct_messages WHERE (senderId = :userA AND recipientId = :userB) OR (senderId = :userB AND recipientId = :userA) ORDER BY createdAt ASC")
    fun getDirectMessages(userA: Long, userB: Long): Flow<List<DirectMessageEntity>>

    @Query("SELECT DISTINCT CASE WHEN senderId = :userId THEN recipientId ELSE senderId END FROM direct_messages WHERE senderId = :userId OR recipientId = :userId")
    fun getMessageContactsIds(userId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirectMessage(message: DirectMessageEntity): Long

    @Query("UPDATE direct_messages SET readAt = :now WHERE recipientId = :userId AND senderId = :senderId AND readAt IS NULL")
    suspend fun markMessagesAsRead(senderId: Long, userId: Long, now: Long)

    @Query("SELECT * FROM blocked_users WHERE userId = :userId")
    fun getBlockedUsersFlow(userId: Long): Flow<List<BlockedUserEntity>>

    @Query("SELECT * FROM blocked_users WHERE userId = :userId")
    suspend fun getBlockedUsers(userId: Long): List<BlockedUserEntity>

    @Query("SELECT * FROM blocked_users WHERE userId = :userId AND blockedUserId = :blockedUserId LIMIT 1")
    suspend fun getBlockedUserRelation(userId: Long, blockedUserId: Long): BlockedUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockUser(block: BlockedUserEntity)

    @Query("DELETE FROM blocked_users WHERE userId = :userId AND blockedUserId = :blockedUserId")
    suspend fun unblockUser(userId: Long, blockedUserId: Long)
}
