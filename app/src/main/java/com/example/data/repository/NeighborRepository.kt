package com.example.data.repository

import com.example.data.dao.NeighborDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class NeighborRepository(private val dao: NeighborDao) {

    // --- Users ---
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun getUserById(id: Long): UserEntity? = dao.getUserById(id)
    suspend fun getUsersByIds(ids: List<Long>): List<UserEntity> = dao.getUsersByIds(ids)
    fun getUsersInNeighborhood(neighborhoodId: Long): Flow<List<UserEntity>> = dao.getUsersInNeighborhood(neighborhoodId)
    suspend fun registerUser(user: UserEntity): Long = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // --- Neighborhoods ---
    fun getAllNeighborhoodsFlow(): Flow<List<NeighborhoodEntity>> = dao.getAllNeighborhoodsFlow()
    suspend fun getAllNeighborhoods(): List<NeighborhoodEntity> = dao.getAllNeighborhoods()
    suspend fun getNeighborhoodById(id: Long): NeighborhoodEntity? = dao.getNeighborhoodById(id)
    suspend fun getNeighborhoodByName(name: String): NeighborhoodEntity? = dao.getNeighborhoodByName(name)
    suspend fun addNeighborhood(neighborhood: NeighborhoodEntity): Long = dao.insertNeighborhood(neighborhood)

    // --- Posts ---
    fun getGeneralPosts(neighborhoodId: Long): Flow<List<PostEntity>> = dao.getGeneralPosts(neighborhoodId)
    fun getPostsByCategory(neighborhoodId: Long, category: String): Flow<List<PostEntity>> = dao.getPostsByCategory(neighborhoodId, category)
    fun getGroupPosts(groupId: Long): Flow<List<PostEntity>> = dao.getGroupPosts(groupId)
    suspend fun createPost(post: PostEntity): Long = dao.insertPost(post)
    suspend fun reportPost(postId: Long) = dao.incrementPostReport(postId)
    suspend fun pinPost(postId: Long, isPinned: Boolean) = dao.updatePostPinnedState(postId, isPinned)

    // --- Comments ---
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> = dao.getCommentsForPost(postId)
    suspend fun addComment(comment: CommentEntity): Long = dao.insertComment(comment)

    // --- Reactions ---
    fun getReactionsForPost(postId: Long): Flow<List<ReactionEntity>> = dao.getReactionsForPost(postId)
    suspend fun getUserReactionForPost(postId: Long, userId: Long): ReactionEntity? = dao.getUserReactionForPost(postId, userId)
    suspend fun addReaction(reaction: ReactionEntity): Long = dao.insertReaction(reaction)
    suspend fun removeReaction(postId: Long, userId: Long) = dao.deleteReaction(postId, userId)

    // --- Marketplace Listings ---
    fun getMarketplaceListings(neighborhoodId: Long): Flow<List<MarketplaceListingEntity>> = dao.getMarketplaceListings(neighborhoodId)
    suspend fun createListing(listing: MarketplaceListingEntity): Long = dao.insertListing(listing)
    suspend fun updateListingStatus(listingId: Long, status: String) = dao.updateListingStatus(listingId, status)

    // --- Business Pages & Reviews ---
    fun getBusinessesInNeighborhood(neighborhoodId: Long): Flow<List<BusinessPageEntity>> = dao.getBusinessesInNeighborhood(neighborhoodId)
    suspend fun registerBusinessPage(business: BusinessPageEntity): Long = dao.insertBusinessPage(business)
    fun getReviewsForBusiness(businessId: Long): Flow<List<ReviewEntity>> = dao.getReviewsForBusiness(businessId)
    suspend fun addReview(review: ReviewEntity) {
        dao.insertReview(review)
        // Recalculate average rating
        val allReviewsFlow = dao.getReviewsForBusiness(review.businessId)
        // Since we are in suspend, we can collect or query synchronously. Let's do a simple calculation if needed.
    }
    suspend fun updateBusinessAverageRating(businessId: Long, avgRating: Double) = dao.updateBusinessAverageRating(businessId, avgRating)

    // --- Alerts ---
    fun getActiveAlerts(neighborhoodId: Long): Flow<List<AlertEntity>> = dao.getActiveAlerts(neighborhoodId, System.currentTimeMillis())
    fun getAllAlertsHistory(neighborhoodId: Long): Flow<List<AlertEntity>> = dao.getAllAlertsHistory(neighborhoodId)
    suspend fun createAlert(alert: AlertEntity): Long = dao.insertAlert(alert)

    // --- Groups ---
    fun getGroupsInNeighborhood(neighborhoodId: Long): Flow<List<GroupEntity>> = dao.getGroupsInNeighborhood(neighborhoodId)
    suspend fun getGroupById(groupId: Long): GroupEntity? = dao.getGroupById(groupId)
    suspend fun createGroup(group: GroupEntity): Long = dao.insertGroup(group)
    fun getGroupMembers(groupId: Long): Flow<List<GroupMemberEntity>> = dao.getGroupMembers(groupId)
    suspend fun joinGroup(groupId: Long, userId: Long) {
        if (dao.getGroupMembership(groupId, userId) == null) {
            dao.insertGroupMember(GroupMemberEntity(groupId = groupId, userId = userId))
        }
    }
    suspend fun leaveGroup(groupId: Long, userId: Long) = dao.removeGroupMember(groupId, userId)
    suspend fun isGroupMember(groupId: Long, userId: Long): Boolean = dao.getGroupMembership(groupId, userId) != null

    // --- Direct Messaging & Blocking ---
    fun getDirectMessages(userId: Long, contactId: Long): Flow<List<DirectMessageEntity>> = dao.getDirectMessages(userId, contactId)
    fun getMessageContactsIds(userId: Long): Flow<List<Long>> = dao.getMessageContactsIds(userId)
    suspend fun sendDirectMessage(message: DirectMessageEntity): Long = dao.insertDirectMessage(message)
    suspend fun markMessagesAsRead(senderId: Long, userId: Long) = dao.markMessagesAsRead(senderId, userId, System.currentTimeMillis())
    fun getBlockedUsersFlow(userId: Long): Flow<List<BlockedUserEntity>> = dao.getBlockedUsersFlow(userId)
    suspend fun getBlockedUsers(userId: Long): List<BlockedUserEntity> = dao.getBlockedUsers(userId)
    suspend fun isUserBlocked(userId: Long, otherUserId: Long): Boolean {
        return dao.getBlockedUserRelation(userId, otherUserId) != null || dao.getBlockedUserRelation(otherUserId, userId) != null
    }
    suspend fun blockUser(userId: Long, blockedUserId: Long) {
        if (dao.getBlockedUserRelation(userId, blockedUserId) == null) {
            dao.insertBlockUser(BlockedUserEntity(userId = userId, blockedUserId = blockedUserId))
        }
    }
    suspend fun unblockUser(userId: Long, blockedUserId: Long) = dao.unblockUser(userId, blockedUserId)
}
