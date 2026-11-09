package com.minodanger.earnmoney

data class DemoOffer(
    val id: String,
    val title: String,
    val description: String,
    val rewardLabel: String
)

val demoOffers = listOf(
    DemoOffer(
        id = "demo-survey",
        title = "استبيان تجريبي",
        description = "بطاقة توضيحية لشكل الاستبيانات التي قد يوفرها مزود معتمد.",
        rewardLabel = "مثال فقط"
    ),
    DemoOffer(
        id = "demo-app",
        title = "تجربة تطبيق تجريبية",
        description = "بطاقة توضيحية لمهمة تثبيت أو تجربة، إذا سمحت شروط المزود بذلك.",
        rewardLabel = "مثال فقط"
    ),
    DemoOffer(
        id = "demo-game",
        title = "مهمة ألعاب تجريبية",
        description = "لن تظهر مهام حقيقية إلا بعد التعاقد والتحقق من توفرها في مصر.",
        rewardLabel = "مثال فقط"
    )
)
