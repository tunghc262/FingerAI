package com.example.palmreader

data class TarotCard(val name: String, val keyword: String, val meaning: String)

enum class TarotTopic(val vi: String, val focus: String) {
    LOVE("Tình yêu", "Trong chuyện tình cảm"),
    BUSINESS("Công việc", "Trong công việc và sự nghiệp"),
    FINANCIAL("Tài chính", "Về tiền bạc"),
    HEALTH("Sức khỏe", "Về sức khỏe"),
    LUCK("May mắn", "Về vận may"),
    PROTECTION("Bình an", "Về sự bình an và bảo vệ bản thân"),
}

private val MAJOR = listOf(
    TarotCard("The Fool – Kẻ Khờ", "một khởi đầu mới đầy tự do", "Bước vào hành trình mới với tâm thế cởi mở. Đừng sợ chưa biết hết mọi thứ."),
    TarotCard("The Magician – Nhà Ảo Thuật", "khả năng biến ý tưởng thành hiện thực", "Bạn có đủ công cụ trong tay. Đây là lúc hành động có chủ đích."),
    TarotCard("The High Priestess – Nữ Tư Tế", "trực giác và những điều chưa nói ra", "Lắng nghe tiếng nói bên trong; câu trả lời không nằm ở bề mặt."),
    TarotCard("The Empress – Hoàng Hậu", "sự sung túc và nuôi dưỡng", "Thời kỳ phát triển, sáng tạo và chăm sóc – cho người khác lẫn chính mình."),
    TarotCard("The Emperor – Hoàng Đế", "trật tự, kỷ luật và quyền kiểm soát", "Xây dựng cấu trúc vững chắc; quyết đoán và chịu trách nhiệm."),
    TarotCard("The Hierophant – Giáo Hoàng", "truyền thống và lời khuyên từ người đi trước", "Học hỏi từ những gì đã được kiểm chứng, hoặc tìm một người thầy."),
    TarotCard("The Lovers – Tình Nhân", "sự gắn kết và một lựa chọn quan trọng", "Một lựa chọn xuất phát từ giá trị thật của bạn sẽ dẫn đến sự hòa hợp."),
    TarotCard("The Chariot – Cỗ Xe", "ý chí và chiến thắng nhờ quyết tâm", "Giữ vững hướng đi, kiểm soát những lực kéo trái chiều để tiến lên."),
    TarotCard("Strength – Sức Mạnh", "sức mạnh nội tâm và lòng kiên nhẫn", "Sự dịu dàng và bình tĩnh mạnh hơn sự ép buộc."),
    TarotCard("The Hermit – Ẩn Sĩ", "khoảng lặng để suy ngẫm", "Lùi lại một bước, ở một mình để tìm câu trả lời của chính mình."),
    TarotCard("Wheel of Fortune – Bánh Xe Số Phận", "một bước ngoặt của vận may", "Mọi thứ đang chuyển động; hãy đón nhận thay đổi thay vì chống lại."),
    TarotCard("Justice – Công Lý", "sự công bằng và hệ quả của hành động", "Trung thực và cân nhắc kỹ; điều bạn gieo sẽ quay về."),
    TarotCard("The Hanged Man – Người Treo Ngược", "nhìn mọi việc từ góc độ khác", "Tạm dừng và buông bỏ cách nhìn cũ để thấy lối đi mới."),
    TarotCard("Death – Sự Chuyển Hóa", "sự kết thúc để mở ra điều mới", "Một giai đoạn khép lại – không đáng sợ, mà là cần thiết để đổi mới."),
    TarotCard("Temperance – Tiết Độ", "sự cân bằng và điều độ", "Kết hợp hài hòa các mặt đối lập; kiên nhẫn sẽ mang lại kết quả."),
    TarotCard("The Devil – Ác Quỷ", "những ràng buộc và cám dỗ", "Nhận diện thói quen hay mối quan hệ đang trói buộc bạn – bạn có thể bước ra."),
    TarotCard("The Tower – Tòa Tháp", "một thay đổi đột ngột", "Điều không vững chắc sẽ sụp đổ, nhường chỗ cho nền móng thật hơn."),
    TarotCard("The Star – Ngôi Sao", "hy vọng và sự chữa lành", "Sau giông bão là bình yên. Hãy tin vào tương lai."),
    TarotCard("The Moon – Mặt Trăng", "sự mơ hồ và nỗi sợ vô hình", "Không phải mọi thứ đều như vẻ ngoài; đi chậm và tin trực giác."),
    TarotCard("The Sun – Mặt Trời", "niềm vui, thành công và sự rõ ràng", "Năng lượng tích cực bao trùm; đây là thời điểm tỏa sáng."),
    TarotCard("Judgement – Phán Xét", "sự thức tỉnh và một lời kêu gọi", "Nhìn lại quá khứ, tha thứ và đưa ra quyết định cho chặng mới."),
    TarotCard("The World – Thế Giới", "sự trọn vẹn và hoàn thành", "Một chu kỳ khép lại thành công; hãy ăn mừng trước khi bắt đầu hành trình tiếp theo."),
)

private data class Suit(val en: String, val vi: String, val domain: String)
private data class Rank(val en: String, val vi: String, val keyword: String)

private val SUITS = listOf(
    Suit("Cups", "Cốc", "cảm xúc và các mối quan hệ"),
    Suit("Wands", "Gậy", "đam mê, hành động và sáng tạo"),
    Suit("Swords", "Kiếm", "suy nghĩ, lời nói và sự thật"),
    Suit("Pentacles", "Tiền", "tiền bạc, công việc và sức khỏe"),
)

private val RANKS = listOf(
    Rank("Ace", "Át", "một hạt giống mới"),
    Rank("Two", "Hai", "sự lựa chọn và cân bằng"),
    Rank("Three", "Ba", "sự phát triển và hợp tác"),
    Rank("Four", "Bốn", "sự ổn định, đôi khi là trì trệ"),
    Rank("Five", "Năm", "thử thách và mất mát tạm thời"),
    Rank("Six", "Sáu", "sự hồi phục và cho – nhận"),
    Rank("Seven", "Bảy", "sự kiên định trước trở ngại"),
    Rank("Eight", "Tám", "chuyển động và nỗ lực"),
    Rank("Nine", "Chín", "gần chạm đích và tự lực"),
    Rank("Ten", "Mười", "sự hoàn tất một chu kỳ"),
    Rank("Page", "Tiểu Đồng", "tin tức mới và sự tò mò"),
    Rank("Knight", "Hiệp Sĩ", "hành động và theo đuổi mục tiêu"),
    Rank("Queen", "Hoàng Hậu", "sự thấu hiểu và nuôi dưỡng"),
    Rank("King", "Vua", "sự làm chủ và trách nhiệm"),
)

/** Bộ bài 78 lá: 22 Ẩn Chính + 56 Ẩn Phụ (ý nghĩa Ẩn Phụ ghép từ số và chất theo cách đọc truyền thống). */
val TAROT_DECK: List<TarotCard> = MAJOR + SUITS.flatMap { s ->
    RANKS.map { r ->
        TarotCard("${r.en} of ${s.en} – ${r.vi} ${s.vi}", r.keyword, "Lá ${r.vi} ${s.vi} nói về ${r.keyword} trong lĩnh vực ${s.domain}.")
    }
}

fun tarotReading(card: TarotCard, topic: TarotTopic) = "${topic.focus}, lá bài này nhấn mạnh ${card.keyword}."
