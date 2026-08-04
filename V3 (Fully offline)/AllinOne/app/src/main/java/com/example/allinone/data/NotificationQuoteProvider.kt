package com.example.allinone.data

import kotlin.random.Random

object NotificationQuoteProvider {

    private val morningQuotes = listOf(
        "The best way to predict the future is to create it.",
        "Your only limit is your mind.",
        "Dream big, work hard, stay focused.",
        "Success is not final, failure is not fatal: it is the courage to continue that counts.",
        "Believe you can and you're halfway there.",
        "Do something today that your future self will thank you for.",
        "The secret of getting ahead is getting started.",
        "It always seems impossible until it's done.",
        "Don't stop until you're proud.",
        "Great things never come from comfort zones."
    )

    private val greatProgressQuotes = listOf(
        "Incredible job today! You're crushing your goals.",
        "Productivity level: Expert. Enjoy your well-deserved rest.",
        "You proved that consistency is key. Sleep well, champion!",
        "A successful day leads to a peaceful night. Great work!"
    )

    private val moderateProgressQuotes = listOf(
        "Solid effort today. You're moving in the right direction.",
        "Every step counts. You did good work today.",
        "Progress is progress, no matter how small. Rest up for tomorrow.",
        "You handled the day well. Tomorrow is a new chance to excel."
    )

    private val lowProgressQuotes = listOf(
        "Tomorrow is a fresh start. Don't let today hold you back.",
        "It's okay to have slow days. Recharge and come back stronger.",
        "Rest is also part of the process. Reflect and reset tonight.",
        "Believe in your ability to turn things around tomorrow."
    )

    fun getRandomMorningQuote(): String {
        return morningQuotes[Random.nextInt(morningQuotes.size)]
    }

    fun getClosingQuote(completionRate: Int): String {
        return when {
            completionRate >= 80 -> greatProgressQuotes[Random.nextInt(greatProgressQuotes.size)]
            completionRate >= 40 -> moderateProgressQuotes[Random.nextInt(moderateProgressQuotes.size)]
            else -> lowProgressQuotes[Random.nextInt(lowProgressQuotes.size)]
        }
    }
}
