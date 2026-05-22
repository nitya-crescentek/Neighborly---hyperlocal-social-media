package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.NeighborDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        NeighborhoodEntity::class,
        PostEntity::class,
        CommentEntity::class,
        ReactionEntity::class,
        MarketplaceListingEntity::class,
        BusinessPageEntity::class,
        ReviewEntity::class,
        AlertEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        DirectMessageEntity::class,
        BlockedUserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NeighborDatabase : RoomDatabase() {

    abstract fun neighborDao(): NeighborDao

    companion object {
        @Volatile
        private var INSTANCE: NeighborDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NeighborDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NeighborDatabase::class.java,
                    "neighborly_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(NeighborDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class NeighborDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.neighborDao())
                }
            }
        }

        suspend fun populateDatabase(dao: NeighborDao) {
            // 1. Seed Neighborhoods
            val nh1 = dao.insertNeighborhood(NeighborhoodEntity(name = "Maple Hills", city = "Seattle", state = "WA"))
            val nh2 = dao.insertNeighborhood(NeighborhoodEntity(name = "Oakwood Heights", city = "Portland", state = "OR"))
            val nh3 = dao.insertNeighborhood(NeighborhoodEntity(name = "Greenwood Valley", city = "Austin", state = "TX"))
            val nh4 = dao.insertNeighborhood(NeighborhoodEntity(name = "Sunnyvale Commons", city = "Silicon Valley", state = "CA"))

            // 2. Seed Users (passwords are simply "password")
            val modMike = dao.insertUser(UserEntity(
                name = "Mod Mike", email = "mike@example.com", passwordHash = "password",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                street = "123 Elm St", city = "Seattle", state = "WA", zipCode = "98101",
                neighborhoodId = nh1, isVerified = true, role = "moderator"
            ))

            val sarahSweet = dao.insertUser(UserEntity(
                name = "Sarah Baker", email = "sarah@example.com", passwordHash = "password",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
                street = "456 Maple Way", city = "Seattle", state = "WA", zipCode = "98101",
                neighborhoodId = nh1, isVerified = true, role = "business_owner"
            ))

            val johnRes = dao.insertUser(UserEntity(
                name = "John Resident", email = "john@example.com", passwordHash = "password",
                avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=crop&w=150&q=80",
                street = "789 Pine Ave", city = "Seattle", state = "WA", zipCode = "98101",
                neighborhoodId = nh1, isVerified = true, role = "resident"
            ))

            val elenaGreen = dao.insertUser(UserEntity(
                name = "Elena Green", email = "elena@example.com", passwordHash = "password",
                avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80",
                street = "101 Cedar Ln", city = "Seattle", state = "WA", zipCode = "98101",
                neighborhoodId = nh1, isVerified = true, role = "resident"
            ))

            // Seed user's actual log-in account
            dao.insertUser(UserEntity(
                name = "Developer Neighbor", email = "ka0967711@gmail.com", passwordHash = "password",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                street = "404 Main Dr", city = "Seattle", state = "WA", zipCode = "98101",
                neighborhoodId = nh1, isVerified = true, role = "resident"
            ))

            // 3. Seed Posts
            val post1 = dao.insertPost(PostEntity(
                userId = modMike, neighborhoodId = nh1, category = "safety",
                title = "🚨 Cougar sighting near Maple Hills Trail",
                body = "Just saw a young cougar crossing the main trail head near 14th Ave exit. Keep your pets indoors and stay extra vigilant!",
                imageUrls = "https://images.unsplash.com/photo-1547407139-3c921a66005c?auto=format&fit=crop&w=600&q=80",
                isPinned = true, isUrgent = true, reportsCount = 0
            ))

            val post2 = dao.insertPost(PostEntity(
                userId = johnRes, neighborhoodId = nh1, category = "event",
                title = "🍔 Annual Neighborhood Block Party next Saturday!",
                body = "Hey everyone! It's that time of the year again. We're hosting the block party on Pine Ave starting at 2:00 PM. Please bring a side dish or drinks. We'll provide burgers & dogs. Kids welcome!",
                imageUrls = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=600&q=80",
                isPinned = false, isUrgent = false
            ))

            val post3 = dao.insertPost(PostEntity(
                userId = elenaGreen, neighborhoodId = nh1, category = "lost_pet",
                title = "🐱 Lost Cat - Fluffy, missing since Thursday",
                body = "Our orange tabby, Fluffy, slipped out of the backdoor on Thursday night. He is friendly but might be skittish. Wearing a blue collar with a small bell. Please text if you spot him!",
                imageUrls = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80",
                isPinned = false, isUrgent = false
            ))

            val post4 = dao.insertPost(PostEntity(
                userId = johnRes, neighborhoodId = nh1, category = "recommendation",
                title = "🛠️ Need a reliable plumber recommendation?",
                body = "Is there anyone local who does good repairs on water heaters? Ours is leaking slightly. Thanks!",
                imageUrls = ""
            ))

            val post5 = dao.insertPost(PostEntity(
                userId = sarahSweet, neighborhoodId = nh1, category = "free_item",
                title = "📦 Free solid oak coffee table",
                body = "Upgrading our furniture. Table has a few scratches but is extremely solid. Pick up anytime from our porch on Maple Way.",
                imageUrls = "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&w=600&q=80"
            ))

            // 4. Seed Comments
            dao.insertComment(CommentEntity(postId = post2, userId = elenaGreen, body = "Can't wait! I will bring my famous guacamole."))
            dao.insertComment(CommentEntity(postId = post2, userId = modMike, body = "I'll be there to help run the grill!"))
            dao.insertComment(CommentEntity(postId = post1, userId = johnRes, body = "Thanks for the heads up, Mike! Shared with my wife."))

            // 5. Seed Reactions (One per user per post)
            dao.insertReaction(ReactionEntity(postId = post1, userId = johnRes, type = "helpful"))
            dao.insertReaction(ReactionEntity(postId = post1, userId = elenaGreen, type = "thanks"))
            dao.insertReaction(ReactionEntity(postId = post2, userId = elenaGreen, type = "agree"))

            // 6. Seed Marketplace_Listings
            dao.insertListing(MarketplaceListingEntity(
                userId = johnRes, neighborhoodId = nh1, title = "Specialized Mountain Bike (Red)",
                description = "21 gears, great suspension. Ridden only for one season, in like new condition.",
                price = 150.00, condition = "Like New",
                imageUrls = "https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=400&q=80",
                status = "available"
            ))
            dao.insertListing(MarketplaceListingEntity(
                userId = elenaGreen, neighborhoodId = nh1, title = "Free moving boxes & bubble wrap",
                description = "Around 25 clean boxes of various sizes, free to whoever can pick them up today.",
                price = null, condition = "Good",
                imageUrls = "",
                status = "available"
            ))

            // 7. Seed Business Pages & Reviews
            val sweetBakery = dao.insertBusinessPage(BusinessPageEntity(
                ownerId = sarahSweet, neighborhoodId = nh1, name = "Sarah's Sweet Cupcakes & Coffee",
                category = "Food & Drink", description = "Artisanal cupcakes, freshly baked everyday, paired with rich local roasted espresso.",
                address = "456 Maple Way, Seattle, WA", phone = "206-555-0199", website = "www.sarahbakesseattle.com",
                hours = "Tue - Sun: 7:00 AM - 4:00 PM", averageRating = 4.8
            ))

            dao.insertReview(ReviewEntity(businessId = sweetBakery, userId = johnRes, rating = 5, body = "Best cinnamon rolls and chocolate muffins in town! Highly recommend."))
            dao.insertReview(ReviewEntity(businessId = sweetBakery, userId = modMike, rating = 4, body = "Super friendly staff, chocolate croissants are incredible."))

            // 8. Seed Safety/Emergency Alerts
            dao.insertAlert(AlertEntity(
                createdBy = modMike, neighborhoodId = nh1, type = "weather",
                title = "⛈️ Severe Summer Storm Warning",
                body = "The National Weather Service has issued a Severe Storm warning for Puget Sound until 11:00 PM tonight. High winds and localized power outages are expected.",
                severity = "critical",
                createdAt = System.currentTimeMillis() - 10 * 60 * 1000,
                expiresAt = System.currentTimeMillis() + 4 * 3600 * 1000 // expires in 4 hrs
            ))

            dao.insertAlert(AlertEntity(
                createdBy = modMike, neighborhoodId = nh1, type = "crime",
                title = "🚗 Package Theft Reported On 10th Ave",
                body = "A porch pirate was caught on camera stealing Amazon boxes from porches on 10th Ave yesterday at 2PM. Driving a white sedan.",
                severity = "medium",
                createdAt = System.currentTimeMillis() - 22 * 3600 * 1000,
                expiresAt = System.currentTimeMillis() + 2 * 24 * 3600 * 1000
            ))

            // 9. Seed Groups & Members
            val bookClub = dao.insertGroup(GroupEntity(
                neighborhoodId = nh1, creatorId = johnRes,
                name = "📖 Maple Hills Book Club",
                description = "An informal monthly gathering to discuss fiction, thrillers, and award-winning novels. Everyone welcome!",
                isPrivate = false
            ))

            dao.insertGroupMember(GroupMemberEntity(groupId = bookClub, userId = johnRes))
            dao.insertGroupMember(GroupMemberEntity(groupId = bookClub, userId = elenaGreen))

            // 10. Seed DMs
            dao.insertDirectMessage(DirectMessageEntity(
                senderId = johnRes, recipientId = elenaGreen,
                body = "Hi Elena, is your tabby cat still missing? I saw an orange cat near Pine St earlier.",
                createdAt = System.currentTimeMillis() - 2 * 3600 * 1000
            ))
            dao.insertDirectMessage(DirectMessageEntity(
                senderId = elenaGreen, recipientId = johnRes,
                body = "Yes he is! Oh my goodness, did he have a blue collar on?",
                createdAt = System.currentTimeMillis() - 1 * 3600 * 1000
            ))
        }
    }
}
