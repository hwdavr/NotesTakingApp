package com.example.notesapp.data.emoji

import com.example.notesapp.domain.emoji.EmojiCatalogItem
import com.example.notesapp.domain.emoji.EmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.EmojiNameKey
import com.example.notesapp.domain.emoji.EmojiVariant
import com.example.notesapp.domain.emoji.SkinTone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledEmojiCatalogRepository @Inject constructor() : EmojiCatalogRepository {
    override fun getCatalog(): List<EmojiCatalogItem> = CATALOG

    private companion object {
        val CATALOG = listOf(
            item(
                id = "grinning_face",
                unicode = "😀",
                nameKey = EmojiNameKey.GRINNING_FACE,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("happy", "smile")
            ),
            item(
                id = "face_with_tears_of_joy",
                unicode = "😂",
                nameKey = EmojiNameKey.FACE_WITH_TEARS_OF_JOY,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("laugh", "funny", "joy")
            ),
            item(
                id = "smiling_face_with_heart_eyes",
                unicode = "😍",
                nameKey = EmojiNameKey.SMILING_FACE_WITH_HEART_EYES,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("love", "hearts")
            ),
            item(
                id = "thinking_face",
                unicode = "🤔",
                nameKey = EmojiNameKey.THINKING_FACE,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("hmm", "consider")
            ),
            item(
                id = "sunglasses_face",
                unicode = "😎",
                nameKey = EmojiNameKey.SUNGLASSES_FACE,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("cool")
            ),
            item(
                id = "slightly_smiling_face",
                unicode = "🙂",
                nameKey = EmojiNameKey.SLIGHTLY_SMILING_FACE,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("happy", "smile", "positive")
            ),
            item(
                id = "beaming_face_with_smiling_eyes",
                unicode = "😁",
                nameKey = EmojiNameKey.BEAMING_FACE_WITH_SMILING_EYES,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("grin", "happy", "smile")
            ),
            item(
                id = "crying_face",
                unicode = "😢",
                nameKey = EmojiNameKey.CRYING_FACE,
                category = EmojiCategory.SMILEYS_EMOTION,
                keywords = setOf("sad", "tear")
            ),
            item(
                id = "red_heart",
                unicode = "❤️",
                nameKey = EmojiNameKey.RED_HEART,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("love", "heart")
            ),
            itemWithSkinTones(
                id = "thumbs_up",
                unicode = "👍",
                nameKey = EmojiNameKey.THUMBS_UP,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("like", "approve", "hand")
            ),
            itemWithSkinTones(
                id = "waving_hand",
                unicode = "👋",
                nameKey = EmojiNameKey.WAVING_HAND,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("hello", "goodbye", "hand")
            ),
            itemWithSkinTones(
                id = "clapping_hands",
                unicode = "👏",
                nameKey = EmojiNameKey.CLAPPING_HANDS,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("applause", "celebrate", "hand")
            ),
            itemWithSkinTones(
                id = "folded_hands",
                unicode = "🙏",
                nameKey = EmojiNameKey.FOLDED_HANDS,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("pray", "please", "thanks", "hand")
            ),
            itemWithSkinTones(
                id = "flexed_biceps",
                unicode = "💪",
                nameKey = EmojiNameKey.FLEXED_BICEPS,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("strong", "muscle", "workout")
            ),
            itemWithSkinTones(
                id = "person",
                unicode = "🧑",
                nameKey = EmojiNameKey.PERSON,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("human", "adult")
            ),
            itemWithSkinTones(
                id = "thumbs_down",
                unicode = "👎",
                nameKey = EmojiNameKey.THUMBS_DOWN,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("dislike", "disapprove", "hand")
            ),
            itemWithSkinTones(
                id = "raising_hands",
                unicode = "🙌",
                nameKey = EmojiNameKey.RAISING_HANDS,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("celebrate", "hooray", "hand")
            ),
            itemWithSkinTones(
                id = "ok_hand",
                unicode = "👌",
                nameKey = EmojiNameKey.OK_HAND,
                category = EmojiCategory.PEOPLE_BODY,
                keywords = setOf("okay", "approve", "hand")
            ),
            item(
                id = "dog",
                unicode = "🐶",
                nameKey = EmojiNameKey.DOG,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("puppy", "pet")
            ),
            item(
                id = "cat",
                unicode = "🐱",
                nameKey = EmojiNameKey.CAT,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("kitten", "pet")
            ),
            item(
                id = "fox",
                unicode = "🦊",
                nameKey = EmojiNameKey.FOX,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("animal")
            ),
            item(
                id = "bear",
                unicode = "🐻",
                nameKey = EmojiNameKey.BEAR,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("animal", "wildlife")
            ),
            item(
                id = "frog",
                unicode = "🐸",
                nameKey = EmojiNameKey.FROG,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("animal", "nature")
            ),
            item(
                id = "monkey_face",
                unicode = "🐵",
                nameKey = EmojiNameKey.MONKEY_FACE,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("animal", "ape")
            ),
            item(
                id = "lion",
                unicode = "🦁",
                nameKey = EmojiNameKey.LION,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("animal", "wildlife")
            ),
            item(
                id = "panda",
                unicode = "🐼",
                nameKey = EmojiNameKey.PANDA,
                category = EmojiCategory.ANIMALS_NATURE,
                keywords = setOf("animal", "bear", "wildlife")
            ),
            item(
                id = "pizza",
                unicode = "🍕",
                nameKey = EmojiNameKey.PIZZA,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("food", "slice", "italian")
            ),
            item(
                id = "hamburger",
                unicode = "🍔",
                nameKey = EmojiNameKey.HAMBURGER,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("burger", "food")
            ),
            item(
                id = "french_fries",
                unicode = "🍟",
                nameKey = EmojiNameKey.FRENCH_FRIES,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("food", "potato")
            ),
            item(
                id = "red_apple",
                unicode = "🍎",
                nameKey = EmojiNameKey.APPLE,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("fruit", "food")
            ),
            item(
                id = "birthday_cake",
                unicode = "🎂",
                nameKey = EmojiNameKey.CAKE,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("dessert", "birthday", "food")
            ),
            item(
                id = "banana",
                unicode = "🍌",
                nameKey = EmojiNameKey.BANANA,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("fruit", "food")
            ),
            item(
                id = "doughnut",
                unicode = "🍩",
                nameKey = EmojiNameKey.DOUGHNUT,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("dessert", "sweet", "food")
            ),
            item(
                id = "hot_beverage",
                unicode = "☕",
                nameKey = EmojiNameKey.HOT_BEVERAGE,
                category = EmojiCategory.FOOD_DRINK,
                keywords = setOf("coffee", "tea", "drink")
            ),
            item(
                id = "soccer_ball",
                unicode = "⚽",
                nameKey = EmojiNameKey.SOCCER_BALL,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("football", "sport", "game")
            ),
            item(
                id = "trophy",
                unicode = "🏆",
                nameKey = EmojiNameKey.TROPHY,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("award", "winner", "sport")
            ),
            item(
                id = "artist_palette",
                unicode = "🎨",
                nameKey = EmojiNameKey.ARTIST_PALETTE,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("art", "paint", "creative")
            ),
            item(
                id = "party_popper",
                unicode = "🎉",
                nameKey = EmojiNameKey.PARTY_POPPER,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("celebrate", "party")
            ),
            item(
                id = "musical_note",
                unicode = "🎵",
                nameKey = EmojiNameKey.MUSICAL_NOTE,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("music", "song", "sound")
            ),
            item(
                id = "video_game",
                unicode = "🎮",
                nameKey = EmojiNameKey.VIDEO_GAME,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("game", "play", "controller")
            ),
            item(
                id = "sports_medal",
                unicode = "🏅",
                nameKey = EmojiNameKey.SPORTS_MEDAL,
                category = EmojiCategory.ACTIVITIES,
                keywords = setOf("award", "medal", "sport")
            ),
            item(
                id = "automobile",
                unicode = "🚗",
                nameKey = EmojiNameKey.AUTOMOBILE,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("car", "drive", "travel")
            ),
            item(
                id = "airplane",
                unicode = "✈️",
                nameKey = EmojiNameKey.AIRPLANE,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("flight", "travel", "trip")
            ),
            item(
                id = "rocket",
                unicode = "🚀",
                nameKey = EmojiNameKey.ROCKET,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("launch", "space", "ship", "travel")
            ),
            item(
                id = "globe_showing_europe_africa",
                unicode = "🌍",
                nameKey = EmojiNameKey.GLOBE_SHOWING_EUROPE_AFRICA,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("world", "earth", "travel")
            ),
            item(
                id = "house",
                unicode = "🏠",
                nameKey = EmojiNameKey.HOUSE,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("home", "place")
            ),
            item(
                id = "train",
                unicode = "🚆",
                nameKey = EmojiNameKey.TRAIN,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("rail", "transport", "travel")
            ),
            item(
                id = "bicycle",
                unicode = "🚲",
                nameKey = EmojiNameKey.BICYCLE,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("bike", "cycle", "transport")
            ),
            item(
                id = "beach_with_umbrella",
                unicode = "🏖️",
                nameKey = EmojiNameKey.BEACH_WITH_UMBRELLA,
                category = EmojiCategory.TRAVEL_PLACES,
                keywords = setOf("vacation", "summer", "place")
            ),
            item(
                id = "light_bulb",
                unicode = "💡",
                nameKey = EmojiNameKey.LIGHT_BULB,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("idea", "bright")
            ),
            item(
                id = "mobile_phone",
                unicode = "📱",
                nameKey = EmojiNameKey.MOBILE_PHONE,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("phone", "technology")
            ),
            item(
                id = "laptop",
                unicode = "💻",
                nameKey = EmojiNameKey.LAPTOP,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("computer", "technology", "work")
            ),
            item(
                id = "wrapped_gift",
                unicode = "🎁",
                nameKey = EmojiNameKey.GIFT,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("present", "birthday")
            ),
            item(
                id = "key",
                unicode = "🔑",
                nameKey = EmojiNameKey.KEY,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("lock", "security")
            ),
            item(
                id = "books",
                unicode = "📚",
                nameKey = EmojiNameKey.BOOKS,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("read", "library", "study")
            ),
            item(
                id = "bell",
                unicode = "🔔",
                nameKey = EmojiNameKey.BELL,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("alert", "notification", "sound")
            ),
            item(
                id = "headphone",
                unicode = "🎧",
                nameKey = EmojiNameKey.HEADPHONE,
                category = EmojiCategory.OBJECTS,
                keywords = setOf("music", "audio", "listen")
            ),
            item(
                id = "star",
                unicode = "⭐",
                nameKey = EmojiNameKey.STAR,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("favorite", "rating")
            ),
            item(
                id = "check_mark_button",
                unicode = "✅",
                nameKey = EmojiNameKey.CHECK_MARK_BUTTON,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("done", "complete", "yes")
            ),
            item(
                id = "hundred_points",
                unicode = "💯",
                nameKey = EmojiNameKey.HUNDRED_POINTS,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("perfect", "score", "number")
            ),
            item(
                id = "question_mark",
                unicode = "❓",
                nameKey = EmojiNameKey.QUESTION_MARK,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("question", "confused")
            ),
            item(
                id = "cross_mark",
                unicode = "❌",
                nameKey = EmojiNameKey.CROSS_MARK,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("no", "wrong", "cancel")
            ),
            item(
                id = "purple_heart",
                unicode = "💜",
                nameKey = EmojiNameKey.PURPLE_HEART,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("love", "heart")
            ),
            item(
                id = "high_voltage",
                unicode = "⚡",
                nameKey = EmojiNameKey.HIGH_VOLTAGE,
                category = EmojiCategory.SYMBOLS,
                keywords = setOf("lightning", "electricity", "energy")
            ),
            item(
                id = "flag_united_states",
                unicode = "🇺🇸",
                nameKey = EmojiNameKey.UNITED_STATES,
                category = EmojiCategory.FLAGS,
                keywords = setOf("usa", "america", "country")
            ),
            item(
                id = "flag_united_kingdom",
                unicode = "🇬🇧",
                nameKey = EmojiNameKey.UNITED_KINGDOM,
                category = EmojiCategory.FLAGS,
                keywords = setOf("uk", "britain", "country")
            ),
            item(
                id = "flag_singapore",
                unicode = "🇸🇬",
                nameKey = EmojiNameKey.SINGAPORE,
                category = EmojiCategory.FLAGS,
                keywords = setOf("sg", "country")
            ),
            item(
                id = "flag_japan",
                unicode = "🇯🇵",
                nameKey = EmojiNameKey.JAPAN,
                category = EmojiCategory.FLAGS,
                keywords = setOf("country")
            ),
            item(
                id = "flag_australia",
                unicode = "🇦🇺",
                nameKey = EmojiNameKey.AUSTRALIA,
                category = EmojiCategory.FLAGS,
                keywords = setOf("country")
            ),
            item(
                id = "flag_canada",
                unicode = "🇨🇦",
                nameKey = EmojiNameKey.CANADA,
                category = EmojiCategory.FLAGS,
                keywords = setOf("country")
            ),
            item(
                id = "flag_south_korea",
                unicode = "🇰🇷",
                nameKey = EmojiNameKey.SOUTH_KOREA,
                category = EmojiCategory.FLAGS,
                keywords = setOf("korea", "country")
            ),
            item(
                id = "flag_india",
                unicode = "🇮🇳",
                nameKey = EmojiNameKey.INDIA,
                category = EmojiCategory.FLAGS,
                keywords = setOf("country")
            ),
            item(
                id = "flag_france",
                unicode = "🇫🇷",
                nameKey = EmojiNameKey.FRANCE,
                category = EmojiCategory.FLAGS,
                keywords = setOf("country")
            )
        )

        fun item(
            id: String,
            unicode: String,
            nameKey: EmojiNameKey,
            category: EmojiCategory,
            keywords: Set<String>
        ): EmojiCatalogItem = EmojiCatalogItem(
            id = id,
            unicode = unicode,
            nameKey = nameKey,
            category = category,
            keywords = keywords
        )

        fun itemWithSkinTones(
            id: String,
            unicode: String,
            nameKey: EmojiNameKey,
            category: EmojiCategory,
            keywords: Set<String>
        ): EmojiCatalogItem = item(
            id = id,
            unicode = unicode,
            nameKey = nameKey,
            category = category,
            keywords = keywords
        ).copy(
            variants = SkinTone.entries.map { tone ->
                EmojiVariant(tone = tone, unicode = tone.modifier?.let { unicode + it } ?: unicode)
            }
        )
    }
}
