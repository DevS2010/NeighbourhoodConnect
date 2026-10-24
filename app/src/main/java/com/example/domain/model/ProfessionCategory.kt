package com.example.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PestControl
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.ui.graphics.vector.ImageVector

enum class ProfessionGroup(val titleEn: String, val titleHi: String) {
    ALL("All Trades", "सभी सेवाएं"),
    HOME_REPAIR("Home & Utilities", "घर और मरम्मत"),
    TECH_REPAIR("Tech & Appliances", "टेक और उपकरण"),
    HEALTH_LEGAL("Medical & Legal", "स्वास्थ्य और कानूनी"),
    EDUCATION_MEDIA("Education & Media", "शिक्षा और मीडिया"),
    DOMESTIC_DELIVERY("Domestic & Essentials", "घरेलू और डिलीवरी"),
    AUTOMOTIVE_EMERGENCY("Auto & Emergency", "वाहन और आपातकालीन")
}

enum class ProfessionCategory(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val titleTa: String,
    val group: ProfessionGroup,
    val icon: ImageVector,
    val baseVisitingFeeInr: Int,
    val supportsEmergency: Boolean,
    val keywords: List<String>
) {
    ELECTRICIAN(
        id = "electrician",
        titleEn = "Electrician",
        titleHi = "इलेक्ट्रीशियन",
        titleMr = "इलेक्ट्रिशियन",
        titleTa = "எலக்ட்ரீஷியன்",
        group = ProfessionGroup.HOME_REPAIR,
        icon = Icons.Filled.ElectricalServices,
        baseVisitingFeeInr = 199,
        supportsEmergency = true,
        keywords = listOf("wire", "switch", "mcb", "short circuit", "spark", "fan", "light", "inverter", "power", "current", "socket")
    ),
    PLUMBER(
        id = "plumber",
        titleEn = "Plumber",
        titleHi = "प्लंबर",
        titleMr = "प्लंबर",
        titleTa = "பிளம்பர்",
        group = ProfessionGroup.HOME_REPAIR,
        icon = Icons.Filled.Plumbing,
        baseVisitingFeeInr = 199,
        supportsEmergency = true,
        keywords = listOf("pipe", "leak", "tap", "sink", "drain", "clog", "water tank", "flush", "bathroom", "geyser", "valve")
    ),
    AC_REPAIR(
        id = "ac_repair",
        titleEn = "AC Repair",
        titleHi = "एसी रिपेयर",
        titleMr = "एसी दुरुस्ती",
        titleTa = "ஏசி பழுதுபார்ப்பு",
        group = ProfessionGroup.TECH_REPAIR,
        icon = Icons.Filled.AcUnit,
        baseVisitingFeeInr = 299,
        supportsEmergency = true,
        keywords = listOf("ac", "cooling", "gas refill", "compressor", "split ac", "window ac", "thermostat", "filter", "water dripping")
    ),
    DOCTOR(
        id = "doctor",
        titleEn = "Doctor",
        titleHi = "डॉक्टर",
        titleMr = "डॉक्टर",
        titleTa = "மருத்துவர்",
        group = ProfessionGroup.HEALTH_LEGAL,
        icon = Icons.Filled.LocalHospital,
        baseVisitingFeeInr = 499,
        supportsEmergency = true,
        keywords = listOf("fever", "doctor", "physician", "clinic", "checkup", "pediatrician", "health", "medicine", "consultation", "pain")
    ),
    MECHANIC(
        id = "mechanic",
        titleEn = "Mechanic",
        titleHi = "मैकेनिक",
        titleMr = "मेकॅनिक",
        titleTa = "மெக்கானிக்",
        group = ProfessionGroup.AUTOMOTIVE_EMERGENCY,
        icon = Icons.Filled.CarRepair,
        baseVisitingFeeInr = 249,
        supportsEmergency = true,
        keywords = listOf("car", "bike", "engine", "battery", "puncture", "brake", "clutch", "breakdown", "start", "oil", "tyre")
    ),
    CARPENTER(
        id = "carpenter",
        titleEn = "Carpenter",
        titleHi = "बढ़ई (कारपेंटर)",
        titleMr = "सुतार",
        titleTa = "தச்சர்",
        group = ProfessionGroup.HOME_REPAIR,
        icon = Icons.Filled.Handyman,
        baseVisitingFeeInr = 249,
        supportsEmergency = false,
        keywords = listOf("wood", "door", "lock", "hinge", "cupboard", "wardrobe", "bed", "furniture", "table", "drill", "window")
    ),
    PAINTER(
        id = "painter",
        titleEn = "Painter",
        titleHi = "पेंटर",
        titleMr = "रंगारी",
        titleTa = "பெயிண்டர்",
        group = ProfessionGroup.HOME_REPAIR,
        icon = Icons.Filled.FormatPaint,
        baseVisitingFeeInr = 149,
        supportsEmergency = false,
        keywords = listOf("paint", "wall", "waterproofing", "putty", "texture", "seepage", "damp", "primer", "polish")
    ),
    TEACHER(
        id = "teacher",
        titleEn = "Teacher",
        titleHi = "शिक्षक",
        titleMr = "शिक्षक",
        titleTa = "ஆசிரியர்",
        group = ProfessionGroup.EDUCATION_MEDIA,
        icon = Icons.Filled.School,
        baseVisitingFeeInr = 399,
        supportsEmergency = false,
        keywords = listOf("school", "music", "language", "spoken english", "piano", "guitar", "yoga", "class", "syllabus")
    ),
    TUTOR(
        id = "tutor",
        titleEn = "Home Tutor",
        titleHi = "होम ट्यूटर",
        titleMr = "होम ट्यूटर",
        titleTa = "வீட்டு ஆசிரியர்",
        group = ProfessionGroup.EDUCATION_MEDIA,
        icon = Icons.Filled.MenuBook,
        baseVisitingFeeInr = 349,
        supportsEmergency = false,
        keywords = listOf("maths", "science", "physics", "coding", "neet", "jee", "board exam", "tuition", "homework")
    ),
    DRIVER(
        id = "driver",
        titleEn = "Driver on Demand",
        titleHi = "ड्राइवर",
        titleMr = "ड्रायव्हर",
        titleTa = "ஓட்டுநர்",
        group = ProfessionGroup.AUTOMOTIVE_EMERGENCY,
        icon = Icons.Filled.DirectionsCar,
        baseVisitingFeeInr = 399,
        supportsEmergency = true,
        keywords = listOf("driver", "chauffeur", "outstation", "hourly driver", "automatic car", "highway")
    ),
    LAWYER(
        id = "lawyer",
        titleEn = "Lawyer & Notary",
        titleHi = "वकील",
        titleMr = "वकील",
        titleTa = "வழக்கறிஞர்",
        group = ProfessionGroup.HEALTH_LEGAL,
        icon = Icons.Filled.Gavel,
        baseVisitingFeeInr = 599,
        supportsEmergency = false,
        keywords = listOf("legal", "agreement", "notary", "property", "rent", "affidavit", "court", "contract", "tax", "dispute")
    ),
    NURSE(
        id = "nurse",
        titleEn = "Home Nurse",
        titleHi = "होम नर्स",
        titleMr = "परिचारिका",
        titleTa = "செவிலியர்",
        group = ProfessionGroup.HEALTH_LEGAL,
        icon = Icons.Filled.Healing,
        baseVisitingFeeInr = 399,
        supportsEmergency = true,
        keywords = listOf("injection", "dressing", "wound", "elderly care", "iv drip", "bp check", "physiotherapy", "patient")
    ),
    PHOTOGRAPHER(
        id = "photographer",
        titleEn = "Photographer",
        titleHi = "फोटोग्राफर",
        titleMr = "फोटोग्राफर",
        titleTa = "புகைப்படக்காரர்",
        group = ProfessionGroup.EDUCATION_MEDIA,
        icon = Icons.Filled.CameraAlt,
        baseVisitingFeeInr = 999,
        supportsEmergency = false,
        keywords = listOf("photo", "wedding", "event", "birthday", "portrait", "videography", "drone", "shoot", "album")
    ),
    MOBILE_REPAIR(
        id = "mobile_repair",
        titleEn = "Mobile Repair",
        titleHi = "मोबाइल रिपेयर",
        titleMr = "मोबाईल दुरुस्ती",
        titleTa = "மொபைல் பழுது",
        group = ProfessionGroup.TECH_REPAIR,
        icon = Icons.Filled.PhoneAndroid,
        baseVisitingFeeInr = 199,
        supportsEmergency = false,
        keywords = listOf("phone", "screen", "display", "charging", "battery replacement", "iphone", "android", "water damage", "speaker")
    ),
    LAPTOP_REPAIR(
        id = "laptop_repair",
        titleEn = "Laptop & PC Repair",
        titleHi = "लैपटॉप रिपेयर",
        titleMr = "लॅपटॉप दुरुस्ती",
        titleTa = "மடிக்கணினி பழுது",
        group = ProfessionGroup.TECH_REPAIR,
        icon = Icons.Filled.Computer,
        baseVisitingFeeInr = 299,
        supportsEmergency = false,
        keywords = listOf("laptop", "ssd", "ram", "windows", "macbook", "keyboard", "motherboard", "blue screen", "slow", "virus")
    ),
    INTERNET_TECH(
        id = "internet_tech",
        titleEn = "Internet & Wi-Fi Tech",
        titleHi = "इंटरनेट तकनीशियन",
        titleMr = "इंटरनेट तंत्रज्ञ",
        titleTa = "இணைய தொழில்நுட்பர்",
        group = ProfessionGroup.TECH_REPAIR,
        icon = Icons.Filled.Router,
        baseVisitingFeeInr = 199,
        supportsEmergency = false,
        keywords = listOf("wifi", "router", "fiber", "broadband", "lan", "cctv", "network", "mesh", "signal", "modem")
    ),
    CLEANING(
        id = "cleaning",
        titleEn = "Deep Cleaning",
        titleHi = "क्लीनिंग सर्विस",
        titleMr = "स्वच्छता सेवा",
        titleTa = "துப்புரவு சேவை",
        group = ProfessionGroup.DOMESTIC_DELIVERY,
        icon = Icons.Filled.CleaningServices,
        baseVisitingFeeInr = 499,
        supportsEmergency = false,
        keywords = listOf("sofa cleaning", "bathroom cleaning", "kitchen cleaning", "full home", "sanitization", "carpet")
    ),
    MAID(
        id = "maid",
        titleEn = "Maid & Cook",
        titleHi = "घरेलू सहायिका / कुक",
        titleMr = "घरकाम / स्वयंपाकी",
        titleTa = "வீட்டு உதவி / சமையல்",
        group = ProfessionGroup.DOMESTIC_DELIVERY,
        icon = Icons.Filled.Person,
        baseVisitingFeeInr = 199,
        supportsEmergency = false,
        keywords = listOf("maid", "cook", "utensils", "sweeping", "mopping", "babysitter", "househelp", "roti")
    ),
    GARDENER(
        id = "gardener",
        titleEn = "Gardener (Mali)",
        titleHi = "माली (गार्डनर)",
        titleMr = "माळी",
        titleTa = "தோட்டக்காரர்",
        group = ProfessionGroup.DOMESTIC_DELIVERY,
        icon = Icons.Filled.Yard,
        baseVisitingFeeInr = 249,
        supportsEmergency = false,
        keywords = listOf("plants", "garden", "lawn", "pruning", "pots", "fertilizer", "balcony garden", "soil")
    ),
    WATER_SUPPLIER(
        id = "water_supplier",
        titleEn = "Water Supplier",
        titleHi = "वाटर सप्लायर",
        titleMr = "पाणी पुरवठादार",
        titleTa = "தண்ணீர் விநியோகம்",
        group = ProfessionGroup.DOMESTIC_DELIVERY,
        icon = Icons.Filled.WaterDrop,
        baseVisitingFeeInr = 90,
        supportsEmergency = true,
        keywords = listOf("water can", "20l jar", "water tanker", "drinking water", "mineral water", "bisleri")
    ),
    GAS_DELIVERY(
        id = "gas_delivery",
        titleEn = "LPG & Gas Service",
        titleHi = "गैस डिलीवरी / रिपेयर",
        titleMr = "गॅस सेवा",
        titleTa = "எரிவாயு சேவை",
        group = ProfessionGroup.DOMESTIC_DELIVERY,
        icon = Icons.Filled.LocalGasStation,
        baseVisitingFeeInr = 149,
        supportsEmergency = true,
        keywords = listOf("gas leak", "stove", "burner", "cylinder", "lpg", "pipeline", "regulator", "hob")
    ),
    TAXI(
        id = "taxi",
        titleEn = "Local Taxi & Auto",
        titleHi = "लोकल टैक्सी",
        titleMr = "लोकल टॅक्सी",
        titleTa = "உள்ளூர் டாக்ஸி",
        group = ProfessionGroup.AUTOMOTIVE_EMERGENCY,
        icon = Icons.Filled.LocalTaxi,
        baseVisitingFeeInr = 149,
        supportsEmergency = true,
        keywords = listOf("cab", "taxi", "airport drop", "station", "sedan", "suv", "tempo traveller")
    ),
    EMERGENCY_SERVICES(
        id = "emergency_services",
        titleEn = "Emergency Response",
        titleHi = "आपातकालीन सेवा",
        titleMr = "आपत्कालीन सेवा",
        titleTa = "அவசர சேவை",
        group = ProfessionGroup.AUTOMOTIVE_EMERGENCY,
        icon = Icons.Filled.Emergency,
        baseVisitingFeeInr = 299,
        supportsEmergency = true,
        keywords = listOf("ambulance", "locksmith", "key lost", "fire", "flood", "sos", "urgent", "24x7")
    ),
    APPLIANCE_REPAIR(
        id = "appliance_repair",
        titleEn = "Appliance & RO Repair",
        titleHi = "उपकरण और आरओ रिपेयर",
        titleMr = "उपकरण दुरुस्ती",
        titleTa = "சாதன பழுதுபார்ப்பு",
        group = ProfessionGroup.TECH_REPAIR,
        icon = Icons.Filled.Kitchen,
        baseVisitingFeeInr = 249,
        supportsEmergency = false,
        keywords = listOf("fridge", "refrigerator", "washing machine", "microwave", "ro purifier", "chimney", "dishwasher")
    ),
    PEST_CONTROL(
        id = "pest_control",
        titleEn = "Pest Control",
        titleHi = "पेस्ट कंट्रोल",
        titleMr = "कीटक नियंत्रण",
        titleTa = "பூச்சி கட்டுப்பாடு",
        group = ProfessionGroup.HOME_REPAIR,
        icon = Icons.Filled.PestControl,
        baseVisitingFeeInr = 599,
        supportsEmergency = false,
        keywords = listOf("cockroach", "termite", "bed bugs", "mosquito", "rat", "ant", "fumigation")
    );

    fun localizedTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.HINDI -> titleHi
        AppLanguage.MARATHI -> titleMr
        AppLanguage.TAMIL -> titleTa
    }
}
