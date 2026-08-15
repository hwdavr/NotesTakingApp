package com.example.notesapp.domain.emoji

import java.util.Locale

enum class EmojiCategory(val storageKey: String) {
    RECENT("recent"),
    SMILEYS_EMOTION("smileys_emotion"),
    PEOPLE_BODY("people_body"),
    ANIMALS_NATURE("animals_nature"),
    FOOD_DRINK("food_drink"),
    ACTIVITIES("activities"),
    TRAVEL_PLACES("travel_places"),
    OBJECTS("objects"),
    SYMBOLS("symbols"),
    FLAGS("flags");

    companion object {
        val approvedBrowseCategories: List<EmojiCategory> = listOf(
            SMILEYS_EMOTION,
            PEOPLE_BODY,
            ANIMALS_NATURE,
            FOOD_DRINK,
            ACTIVITIES,
            TRAVEL_PLACES,
            OBJECTS,
            SYMBOLS,
            FLAGS
        )
    }
}

enum class EmojiNameKey(val searchLabel: String) {
    GRINNING_FACE("grinning face"),
    FACE_WITH_TEARS_OF_JOY("face with tears of joy"),
    SMILING_FACE_WITH_HEART_EYES("smiling face with heart-eyes"),
    THINKING_FACE("thinking face"),
    SUNGLASSES_FACE("smiling face with sunglasses"),
    SLIGHTLY_SMILING_FACE("slightly smiling face"),
    BEAMING_FACE_WITH_SMILING_EYES("beaming face with smiling eyes"),
    CRYING_FACE("crying face"),
    RED_HEART("red heart"),
    THUMBS_UP("thumbs up"),
    WAVING_HAND("waving hand"),
    CLAPPING_HANDS("clapping hands"),
    FOLDED_HANDS("folded hands"),
    FLEXED_BICEPS("flexed biceps"),
    PERSON("person"),
    THUMBS_DOWN("thumbs down"),
    RAISING_HANDS("raising hands"),
    OK_HAND("OK hand"),
    DOG("dog"),
    CAT("cat"),
    FOX("fox"),
    BEAR("bear"),
    FROG("frog"),
    MONKEY_FACE("monkey face"),
    LION("lion"),
    PANDA("panda"),
    PIZZA("pizza"),
    HAMBURGER("hamburger"),
    FRENCH_FRIES("french fries"),
    APPLE("red apple"),
    CAKE("birthday cake"),
    BANANA("banana"),
    DOUGHNUT("doughnut"),
    HOT_BEVERAGE("hot beverage"),
    SOCCER_BALL("soccer ball"),
    TROPHY("trophy"),
    ARTIST_PALETTE("artist palette"),
    PARTY_POPPER("party popper"),
    MUSICAL_NOTE("musical note"),
    VIDEO_GAME("video game"),
    SPORTS_MEDAL("sports medal"),
    AUTOMOBILE("automobile"),
    AIRPLANE("airplane"),
    ROCKET("rocket"),
    GLOBE_SHOWING_EUROPE_AFRICA("globe showing Europe-Africa"),
    HOUSE("house"),
    TRAIN("train"),
    BICYCLE("bicycle"),
    BEACH_WITH_UMBRELLA("beach with umbrella"),
    LIGHT_BULB("light bulb"),
    MOBILE_PHONE("mobile phone"),
    LAPTOP("laptop"),
    GIFT("wrapped gift"),
    KEY("key"),
    BOOKS("books"),
    BELL("bell"),
    HEADPHONE("headphone"),
    STAR("star"),
    CHECK_MARK_BUTTON("check mark button"),
    HUNDRED_POINTS("hundred points"),
    QUESTION_MARK("question mark"),
    CROSS_MARK("cross mark"),
    PURPLE_HEART("purple heart"),
    HIGH_VOLTAGE("high voltage"),
    UNITED_STATES("flag: United States"),
    UNITED_KINGDOM("flag: United Kingdom"),
    SINGAPORE("flag: Singapore"),
    JAPAN("flag: Japan"),
    AUSTRALIA("flag: Australia"),
    CANADA("flag: Canada"),
    SOUTH_KOREA("flag: South Korea"),
    INDIA("flag: India"),
    FRANCE("flag: France")
}

enum class SkinTone(val storageKey: String, val modifier: String?) {
    DEFAULT("default", null),
    LIGHT("light", "🏻"),
    MEDIUM_LIGHT("medium_light", "🏼"),
    MEDIUM("medium", "🏽"),
    MEDIUM_DARK("medium_dark", "🏾"),
    DARK("dark", "🏿")
}

data class EmojiVariant(
    val tone: SkinTone,
    val unicode: String
)

data class EmojiCatalogItem(
    val id: String,
    val unicode: String,
    val nameKey: EmojiNameKey,
    val category: EmojiCategory,
    val keywords: Set<String> = emptySet(),
    val variants: List<EmojiVariant> = emptyList()
) {
    fun unicodeFor(tone: SkinTone): String = variants.firstOrNull { it.tone == tone }?.unicode ?: unicode

    fun matchesQuery(query: String): Boolean {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        if (normalizedQuery.isEmpty()) return true
        return nameKey.searchLabel.lowercase(Locale.ROOT).contains(normalizedQuery) ||
            keywords.any { keyword -> keyword.lowercase(Locale.ROOT).contains(normalizedQuery) }
    }
}

interface EmojiCatalogRepository {
    fun getCatalog(): List<EmojiCatalogItem>
}
