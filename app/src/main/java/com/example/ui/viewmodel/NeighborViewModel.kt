package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.NeighborDatabase
import com.example.data.model.*
import com.example.data.repository.NeighborRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NeighborViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NeighborDatabase.getDatabase(application, viewModelScope)
    val repository = NeighborRepository(database.neighborDao())

    // --- Active Auth State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentNeighborhood = MutableStateFlow<NeighborhoodEntity?>(null)
    val currentNeighborhood: StateFlow<NeighborhoodEntity?> = _currentNeighborhood.asStateFlow()

    // Address Verification simulation state
    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()

    private val _verificationError = MutableStateFlow("")
    val verificationError: StateFlow<String> = _verificationError.asStateFlow()

    private val _authStateError = MutableStateFlow("")
    val authStateError: StateFlow<String> = _authStateError.asStateFlow()

    // Loaded map / directory users
    private val _allUsersList = MutableStateFlow<List<UserEntity>>(emptyList())
    val allUsersList: StateFlow<List<UserEntity>> = _allUsersList.asStateFlow()

    // --- Active Feed & Filters ---
    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    val posts: StateFlow<List<PostEntity>> = combine(
        _currentUser.filterNotNull(),
        _selectedCategoryFilter
    ) { user, filter ->
        if (filter == "All") {
            repository.getGeneralPosts(user.neighborhoodId)
        } else {
            repository.getPostsByCategory(user.neighborhoodId, filter.lowercase())
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Alerts ---
    val activeAlerts: StateFlow<List<AlertEntity>> = _currentUser.filterNotNull().flatMapLatest { user ->
        repository.getActiveAlerts(user.neighborhoodId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertsHistory: StateFlow<List<AlertEntity>> = _currentUser.filterNotNull().flatMapLatest { user ->
        repository.getAllAlertsHistory(user.neighborhoodId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Groups ---
    val groups: StateFlow<List<GroupEntity>> = _currentUser.filterNotNull().flatMapLatest { user ->
        repository.getGroupsInNeighborhood(user.neighborhoodId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGroup = MutableStateFlow<GroupEntity?>(null)
    val selectedGroup: StateFlow<GroupEntity?> = _selectedGroup.asStateFlow()

    val groupPosts: StateFlow<List<PostEntity>> = _selectedGroup.filterNotNull().flatMapLatest { group ->
        repository.getGroupPosts(group.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupMembers: StateFlow<List<Long>> = _selectedGroup.filterNotNull().flatMapLatest { group ->
        repository.getGroupMembers(group.id).map { members -> members.map { it.userId } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Marketplace ---
    private val _marketplaceMaxPrice = MutableStateFlow<Double?>(null)
    val marketplaceMaxPrice: StateFlow<Double?> = _marketplaceMaxPrice.asStateFlow()

    private val _marketplaceConditionFilter = MutableStateFlow("All")
    val marketplaceConditionFilter: StateFlow<String> = _marketplaceConditionFilter.asStateFlow()

    private val _marketplaceFreeOnly = MutableStateFlow(false)
    val marketplaceFreeOnly: StateFlow<Boolean> = _marketplaceFreeOnly.asStateFlow()

    val marketplaceListings: StateFlow<List<MarketplaceListingEntity>> = combine(
        _currentUser.filterNotNull().flatMapLatest { user -> repository.getMarketplaceListings(user.neighborhoodId) },
        _marketplaceMaxPrice,
        _marketplaceConditionFilter,
        _marketplaceFreeOnly
    ) { listings, maxPrice, condition, freeOnly ->
        listings.filter { listing ->
            // Auto expire after 30 days rule
            val isExpired = System.currentTimeMillis() - listing.createdAt > 30L * 24 * 3600 * 1000
            if (isExpired) return@filter false

            val matchesPrice = if (freeOnly) {
                listing.price == null || listing.price == 0.0
            } else if (maxPrice != null) {
                (listing.price ?: 0.0) <= maxPrice
            } else {
                true
            }

            val matchesCondition = condition == "All" || listing.condition.lowercase() == condition.lowercase()

            matchesPrice && matchesCondition
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Business Directory ---
    val businessDirectory: StateFlow<List<BusinessPageEntity>> = _currentUser.filterNotNull().flatMapLatest { user ->
        repository.getBusinessesInNeighborhood(user.neighborhoodId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBusiness = MutableStateFlow<BusinessPageEntity?>(null)
    val selectedBusiness: StateFlow<BusinessPageEntity?> = _selectedBusiness.asStateFlow()

    val businessReviews: StateFlow<List<ReviewEntity>> = _selectedBusiness.filterNotNull().flatMapLatest { biz ->
        repository.getReviewsForBusiness(biz.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Direct Messages & Contacts ---
    private val _activeChatUser = MutableStateFlow<UserEntity?>(null)
    val activeChatUser: StateFlow<UserEntity?> = _activeChatUser.asStateFlow()

    val directMessages: StateFlow<List<DirectMessageEntity>> = combine(
        _currentUser.filterNotNull(),
        _activeChatUser.filterNotNull()
    ) { user, contact ->
        repository.getDirectMessages(user.id, contact.id)
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatContacts: StateFlow<List<UserEntity>> = _currentUser.filterNotNull().flatMapLatest { user ->
        repository.getMessageContactsIds(user.id).map { ids ->
            repository.getUsersByIds(ids)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Initial Login helper (seeds verified user so emulator doesn't start blank)
    init {
        viewModelScope.launch {
            // Attempt to login ka0967711@gmail.com by default
            login("ka0967711@gmail.com", "password")
            
            // If database not loaded yet or seed delayed, load John as fallback
            if (_currentUser.value == null) {
                login("john@example.com", "password")
            }

            // Sync all users lists
            currentUser.collect { user ->
                if (user != null) {
                    val nbh = repository.getNeighborhoodById(user.neighborhoodId)
                    _currentNeighborhood.value = nbh
                    repository.getUsersInNeighborhood(user.neighborhoodId).collect {
                        _allUsersList.value = it
                    }
                }
            }
        }
    }

    // --- ACTIONS ---

    // Auth Actions
    suspend fun login(email: String, passwordHash: String): Boolean {
        _authStateError.value = ""
        val user = repository.getUserByEmail(email)
        if (user != null && user.passwordHash == passwordHash) {
            _currentUser.value = user
            val nbh = repository.getNeighborhoodById(user.neighborhoodId)
            _currentNeighborhood.value = nbh
            
            // Generate a random 4-digit code if user is not verified yet
            if (!user.isVerified) {
                _verificationCode.value = (1000..9999).random().toString()
            } else {
                _verificationCode.value = ""
            }
            return true
        } else {
            _authStateError.value = "Invalid email or password."
            return false
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentNeighborhood.value = null
        _verificationCode.value = ""
    }

    suspend fun signUp(
        name: String,
        email: String,
        passwordHash: String,
        street: String,
        city: String,
        state: String,
        zipCode: String,
        role: String = "resident"
    ) {
        _authStateError.value = ""
        if (name.isBlank() || email.isBlank() || passwordHash.isBlank()) {
            _authStateError.value = "All fields are required"
            return
        }

        // Check duplicate
        if (repository.getUserByEmail(email) != null) {
            _authStateError.value = "Email is already registered"
            return
        }

        // Map zip to neighborhood
        val neighborhoods = repository.getAllNeighborhoods()
        var matchedNbh = neighborhoods.firstOrNull { it.name.lowercase().contains(city.lowercase()) }
        if (matchedNbh == null) {
            // Zip mapping simulation
            val nbhName = when (zipCode) {
                "98101", "98102" -> "Maple Hills"
                "97201", "97202" -> "Oakwood Heights"
                "78701", "78702" -> "Greenwood Valley"
                "94086", "94087" -> "Sunnyvale Commons"
                else -> "${city.ifBlank { "Local" }} Heights"
            }
            val existing = repository.getNeighborhoodByName(nbhName)
            if (existing != null) {
                matchedNbh = existing
            } else {
                val newId = repository.addNeighborhood(
                    NeighborhoodEntity(name = nbhName, city = city.ifBlank { "Seattle" }, state = state.ifBlank { "WA" })
                )
                matchedNbh = repository.getNeighborhoodById(newId)
            }
        }

        val newUser = UserEntity(
            name = name,
            email = email,
            passwordHash = passwordHash,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
            street = street,
            city = city,
            state = state,
            zipCode = zipCode,
            neighborhoodId = matchedNbh?.id ?: 1L,
            isVerified = false, // Must enter address verification code first
            role = role
        )

        val id = repository.registerUser(newUser)
        login(email, passwordHash)
    }

    suspend fun verifyAddress(enteredCode: String): Boolean {
        val user = _currentUser.value ?: return false
        _verificationError.value = ""
        if (enteredCode == _verificationCode.value && enteredCode.isNotBlank()) {
            val verifiedUser = user.copy(isVerified = true)
            repository.updateUser(verifiedUser)
            _currentUser.value = verifiedUser
            _verificationCode.value = ""
            return true
        } else {
            _verificationError.value = "Incorrect code. Please try again."
            return false
        }
    }

    // Community Feed Actions
    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    suspend fun createPost(title: String, body: String, category: String, imageUrl: String = "", isUrgent: Boolean = false) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return // rule 2 check

        val isMod = user.role == "moderator"

        val post = PostEntity(
            userId = user.id,
            neighborhoodId = user.neighborhoodId,
            category = category,
            title = title,
            body = body,
            imageUrls = imageUrl,
            isPinned = isUrgent && isMod, // Pin immediately if moderator creates urgent safety warnings
            isUrgent = isUrgent
        )
        repository.createPost(post)
    }

    suspend fun createGroupPost(groupId: Long, title: String, body: String) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return

        val post = PostEntity(
            userId = user.id,
            neighborhoodId = user.neighborhoodId,
            category = "general",
            title = title,
            body = body,
            imageUrls = "",
            groupId = groupId
        )
        repository.createPost(post)
    }

    suspend fun toggleReaction(postId: Long, reactionType: String) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return

        val existing = repository.getUserReactionForPost(postId, user.id)
        if (existing != null) {
            repository.removeReaction(postId, user.id)
            if (existing.type != reactionType) {
                // switch reaction (Rule 7: One reaction type per user per post, no duplicates)
                repository.addReaction(ReactionEntity(postId = postId, userId = user.id, type = reactionType))
            }
        } else {
            repository.addReaction(ReactionEntity(postId = postId, userId = user.id, type = reactionType))
        }
    }

    suspend fun addComment(postId: Long, body: String) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return

        val comment = CommentEntity(
            postId = postId,
            userId = user.id,
            body = body
        )
        repository.addComment(comment)
    }

    suspend fun reportPost(postId: Long) {
        // Enforces rule 5: Posts with 5+ reports are auto-hidden pending moderator review (done inside DAO Query)
        repository.reportPost(postId)
    }

    suspend fun pinPost(postId: Long, isPinned: Boolean) {
        val user = _currentUser.value ?: return
        if (user.role == "moderator") {
            repository.pinPost(postId, isPinned)
        }
    }

    // Safety Alert Actions
    suspend fun createAlert(title: String, body: String, type: String, severity: String) {
        val user = _currentUser.value ?: return
        if (user.role != "moderator") return // Rule 6: Only moderators can create Alerts.

        val alert = AlertEntity(
            createdBy = user.id,
            neighborhoodId = user.neighborhoodId,
            type = type,
            title = title,
            body = body,
            severity = severity
        )
        repository.createAlert(alert)
    }

    // Marketplace Actions
    fun setMarketplaceCondition(condition: String) {
        _marketplaceConditionFilter.value = condition
    }

    fun setMarketplaceMaxPrice(price: Double?) {
        _marketplaceMaxPrice.value = price
    }

    fun setMarketplaceFreeOnly(freeOnly: Boolean) {
        _marketplaceFreeOnly.value = freeOnly
    }

    suspend fun createListing(title: String, desc: String, price: Double?, condition: String, imageUrl: String) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return

        val listing = MarketplaceListingEntity(
            userId = user.id,
            neighborhoodId = user.neighborhoodId,
            title = title,
            description = desc,
            price = price,
            condition = condition,
            imageUrls = imageUrl,
            status = "available"
        )
        repository.createListing(listing)
    }

    suspend fun updateListingStatus(listingId: Long, status: String) {
        repository.updateListingStatus(listingId, status)
    }

    // Groups Actions
    fun selectGroup(group: GroupEntity?) {
        _selectedGroup.value = group
    }

    suspend fun createGroup(name: String, desc: String, isPrivate: Boolean) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return

        val group = GroupEntity(
            neighborhoodId = user.neighborhoodId,
            creatorId = user.id,
            name = name,
            description = desc,
            isPrivate = isPrivate
        )
        val groupId = repository.createGroup(group)
        repository.joinGroup(groupId, user.id)
    }

    suspend fun joinGroup(groupId: Long) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return
        repository.joinGroup(groupId, user.id)
        _selectedGroup.value?.let { current ->
            if (current.id == groupId) {
                // refresh selected group state triggers
                _selectedGroup.value = current
            }
        }
    }

    suspend fun leaveGroup(groupId: Long) {
        val user = _currentUser.value ?: return
        repository.leaveGroup(groupId, user.id)
        _selectedGroup.value?.let { current ->
            if (current.id == groupId) {
                _selectedGroup.value = current
            }
        }
    }

    // Business Directory Actions
    fun selectBusiness(business: BusinessPageEntity?) {
        _selectedBusiness.value = business
    }

    suspend fun registerBusinessPage(name: String, cat: String, desc: String, address: String, phone: String, web: String, hours: String) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return
        // Rule 4: Business owners must belong to same neighborhood as their business page
        if (user.role != "business_owner") return

        val biz = BusinessPageEntity(
            ownerId = user.id,
            neighborhoodId = user.neighborhoodId,
            name = name,
            category = cat,
            description = desc,
            address = address,
            phone = phone,
            website = web,
            hours = hours,
            averageRating = 0.0
        )
        repository.registerBusinessPage(biz)
    }

    suspend fun addReview(businessId: Long, rating: Int, body: String) {
        val user = _currentUser.value ?: return
        if (!user.isVerified) return

        val review = ReviewEntity(
            businessId = businessId,
            userId = user.id,
            rating = rating,
            body = body
        )
        repository.addReview(review)
    }

    // Messages Actions
    fun startChat(contact: UserEntity) {
        _activeChatUser.value = contact
    }

    fun endChat() {
        _activeChatUser.value = null
    }

    suspend fun sendDirectMessage(text: String) {
        val user = _currentUser.value ?: return
        val chatUser = _activeChatUser.value ?: return
        // Rule 3: Users can only message other verified residents / neighbors
        if (!user.isVerified || !chatUser.isVerified) return

        // Check if either has blocked each other
        if (repository.isUserBlocked(user.id, chatUser.id)) return

        val msg = DirectMessageEntity(
            senderId = user.id,
            recipientId = chatUser.id,
            body = text
        )
        repository.sendDirectMessage(msg)
    }

    suspend fun markActiveChatMessagesAsRead() {
        val user = _currentUser.value ?: return
        val chatUser = _activeChatUser.value ?: return
        repository.markMessagesAsRead(chatUser.id, user.id)
    }

    suspend fun toggleBlockUser(targetUserId: Long) {
        val user = _currentUser.value ?: return
        val isBlocked = repository.isUserBlocked(user.id, targetUserId)
        if (isBlocked) {
            repository.unblockUser(user.id, targetUserId)
        } else {
            repository.blockUser(user.id, targetUserId)
        }
    }
}
