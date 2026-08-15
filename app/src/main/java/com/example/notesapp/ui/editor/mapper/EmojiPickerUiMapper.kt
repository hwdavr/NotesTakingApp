package com.example.notesapp.ui.editor.mapper

import com.example.notesapp.R
import com.example.notesapp.domain.emoji.EmojiCatalogItem
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.EmojiNameKey
import com.example.notesapp.domain.emoji.SkinTone
import com.example.notesapp.ui.editor.model.EmojiCategoryUiModel
import com.example.notesapp.ui.editor.model.EmojiPickerItemUiModel
import com.example.notesapp.ui.editor.model.EmojiVariantUiModel

object EmojiPickerUiMapper {
    fun categoryModels(): List<EmojiCategoryUiModel> =
        (listOf(EmojiCategory.RECENT) + EmojiCategory.approvedBrowseCategories)
            .map { category -> EmojiCategoryUiModel(category, category.toLabelRes()) }

    fun mapItems(items: List<EmojiCatalogItem>): List<EmojiPickerItemUiModel> = items.map { item ->
        EmojiPickerItemUiModel(
            id = item.id,
            unicode = item.unicode,
            nameRes = item.nameKey.toNameRes(),
            variants = item.variants.map { variant ->
                EmojiVariantUiModel(
                    tone = variant.tone,
                    unicode = variant.unicode,
                    labelRes = variant.tone.toLabelRes()
                )
            }
        )
    }

    private fun EmojiCategory.toLabelRes(): Int = when (this) {
        EmojiCategory.RECENT -> R.string.emoji_picker_recent_category
        EmojiCategory.SMILEYS_EMOTION -> R.string.emoji_picker_category_smileys_emotion
        EmojiCategory.PEOPLE_BODY -> R.string.emoji_picker_category_people_body
        EmojiCategory.ANIMALS_NATURE -> R.string.emoji_picker_category_animals_nature
        EmojiCategory.FOOD_DRINK -> R.string.emoji_picker_category_food_drink
        EmojiCategory.ACTIVITIES -> R.string.emoji_picker_category_activities
        EmojiCategory.TRAVEL_PLACES -> R.string.emoji_picker_category_travel_places
        EmojiCategory.OBJECTS -> R.string.emoji_picker_category_objects
        EmojiCategory.SYMBOLS -> R.string.emoji_picker_category_symbols
        EmojiCategory.FLAGS -> R.string.emoji_picker_category_flags
    }

    private fun EmojiNameKey.toNameRes(): Int = nameResourceByKey.getValue(this)

    private val nameResourceByKey = mapOf(
        EmojiNameKey.GRINNING_FACE to R.string.emoji_name_grinning_face,
        EmojiNameKey.FACE_WITH_TEARS_OF_JOY to R.string.emoji_name_face_with_tears_of_joy,
        EmojiNameKey.SMILING_FACE_WITH_HEART_EYES to R.string.emoji_name_smiling_face_with_heart_eyes,
        EmojiNameKey.THINKING_FACE to R.string.emoji_name_thinking_face,
        EmojiNameKey.SUNGLASSES_FACE to R.string.emoji_name_sunglasses_face,
        EmojiNameKey.RED_HEART to R.string.emoji_name_red_heart,
        EmojiNameKey.THUMBS_UP to R.string.emoji_name_thumbs_up,
        EmojiNameKey.WAVING_HAND to R.string.emoji_name_waving_hand,
        EmojiNameKey.CLAPPING_HANDS to R.string.emoji_name_clapping_hands,
        EmojiNameKey.FOLDED_HANDS to R.string.emoji_name_folded_hands,
        EmojiNameKey.FLEXED_BICEPS to R.string.emoji_name_flexed_biceps,
        EmojiNameKey.PERSON to R.string.emoji_name_person,
        EmojiNameKey.DOG to R.string.emoji_name_dog,
        EmojiNameKey.CAT to R.string.emoji_name_cat,
        EmojiNameKey.FOX to R.string.emoji_name_fox,
        EmojiNameKey.BEAR to R.string.emoji_name_bear,
        EmojiNameKey.FROG to R.string.emoji_name_frog,
        EmojiNameKey.PIZZA to R.string.emoji_name_pizza,
        EmojiNameKey.HAMBURGER to R.string.emoji_name_hamburger,
        EmojiNameKey.FRENCH_FRIES to R.string.emoji_name_french_fries,
        EmojiNameKey.APPLE to R.string.emoji_name_red_apple,
        EmojiNameKey.CAKE to R.string.emoji_name_birthday_cake,
        EmojiNameKey.SOCCER_BALL to R.string.emoji_name_soccer_ball,
        EmojiNameKey.TROPHY to R.string.emoji_name_trophy,
        EmojiNameKey.ARTIST_PALETTE to R.string.emoji_name_artist_palette,
        EmojiNameKey.PARTY_POPPER to R.string.emoji_name_party_popper,
        EmojiNameKey.AUTOMOBILE to R.string.emoji_name_automobile,
        EmojiNameKey.AIRPLANE to R.string.emoji_name_airplane,
        EmojiNameKey.ROCKET to R.string.emoji_name_rocket,
        EmojiNameKey.GLOBE_SHOWING_EUROPE_AFRICA to R.string.emoji_name_globe_showing_europe_africa,
        EmojiNameKey.HOUSE to R.string.emoji_name_house,
        EmojiNameKey.LIGHT_BULB to R.string.emoji_name_light_bulb,
        EmojiNameKey.MOBILE_PHONE to R.string.emoji_name_mobile_phone,
        EmojiNameKey.LAPTOP to R.string.emoji_name_laptop,
        EmojiNameKey.GIFT to R.string.emoji_name_wrapped_gift,
        EmojiNameKey.KEY to R.string.emoji_name_key,
        EmojiNameKey.STAR to R.string.emoji_name_star,
        EmojiNameKey.CHECK_MARK_BUTTON to R.string.emoji_name_check_mark_button,
        EmojiNameKey.HUNDRED_POINTS to R.string.emoji_name_hundred_points,
        EmojiNameKey.QUESTION_MARK to R.string.emoji_name_question_mark,
        EmojiNameKey.UNITED_STATES to R.string.emoji_name_flag_united_states,
        EmojiNameKey.UNITED_KINGDOM to R.string.emoji_name_flag_united_kingdom,
        EmojiNameKey.SINGAPORE to R.string.emoji_name_flag_singapore,
        EmojiNameKey.JAPAN to R.string.emoji_name_flag_japan,
        EmojiNameKey.AUSTRALIA to R.string.emoji_name_flag_australia,
        EmojiNameKey.CANADA to R.string.emoji_name_flag_canada
    )

    private fun SkinTone.toLabelRes(): Int = when (this) {
        SkinTone.DEFAULT -> R.string.emoji_picker_skin_tone_default
        SkinTone.LIGHT -> R.string.emoji_picker_skin_tone_light
        SkinTone.MEDIUM_LIGHT -> R.string.emoji_picker_skin_tone_medium_light
        SkinTone.MEDIUM -> R.string.emoji_picker_skin_tone_medium
        SkinTone.MEDIUM_DARK -> R.string.emoji_picker_skin_tone_medium_dark
        SkinTone.DARK -> R.string.emoji_picker_skin_tone_dark
    }
}
