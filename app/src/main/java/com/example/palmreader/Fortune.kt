package com.example.palmreader

import java.text.Normalizer
import java.time.LocalDate
import kotlin.random.Random

// ---------- Báo cáo hằng ngày: cùng cung + cùng ngày luôn ra cùng kết quả ----------

data class DailyReport(val numbers: List<Int>, val colors: List<String>, val strength: Int, val tip: String)

private val COLORS = listOf(
    "Đỏ", "Cam", "Vàng", "Vàng kim", "Xanh lá", "Xanh ngọc", "Xanh biển", "Xanh da trời",
    "Tím", "Tím oải hương", "Hồng", "San hô", "Trắng ngọc trai", "Bạc", "Nâu", "Be", "Đen", "Đỏ rượu",
)

private val TIPS = listOf(
    "Một cuộc trò chuyện ngắn hôm nay có thể mở ra cơ hội lớn.",
    "Hãy hoàn thành việc nhỏ đang dang dở trước khi nhận việc mới.",
    "Tin vào trực giác khi phải chọn giữa hai phương án.",
    "Dành mười phút đi bộ ngoài trời để đầu óc thông suốt.",
    "Lời khen chân thành sẽ giúp bạn gắn kết hơn với ai đó.",
    "Đừng vội chi tiêu cho món đồ bạn chỉ thích trong khoảnh khắc.",
    "Hôm nay hợp để lên kế hoạch hơn là hành động vội.",
    "Liên lạc lại với một người bạn cũ – họ đang nghĩ đến bạn.",
    "Ngủ sớm hơn mọi ngày, năng lượng ngày mai sẽ khác hẳn.",
    "Nói ra điều bạn cần thay vì chờ người khác tự đoán.",
)

fun dailyReport(sign: Sign, date: LocalDate): DailyReport {
    val r = Random(date.toEpochDay() * 12 + sign.ordinal)
    return DailyReport(
        numbers = (1..99).shuffled(r).take(3).sorted(),
        colors = COLORS.shuffled(r).take(2),
        strength = r.nextInt(60, 100),
        tip = TIPS.random(r),
    )
}

// ---------- Test tình duyên: điểm "seeded" từ hai cái tên, giống love_score_mode=seeded của app gốc ----------

data class LoveBand(val range: IntRange, val title: String, val text: String)

val LOVE_BANDS = listOf(
    LoveBand(91..100, "Định mệnh an bài", "Hai bạn như được viết sẵn trong các vì sao – hiểu nhau gần như không cần lời."),
    LoveBand(71..90, "Một cặp đẹp", "Sự đồng điệu rất rõ. Chỉ cần vun đắp đều đặn, mối quan hệ sẽ bền lâu."),
    LoveBand(51..70, "Tiềm năng ngọt ngào", "Có nhiều điểm hợp nhau; hãy dành thời gian để khám phá thêm về đối phương."),
    LoveBand(36..50, "Đáng để thử", "Khác biệt không ít, nhưng sự tò mò có thể biến thành sức hút."),
    LoveBand(21..35, "Khác tần số", "Hai bạn nhìn thế giới theo hai cách khác nhau – cần nhiều kiên nhẫn để gặp nhau ở giữa."),
    LoveBand(6..20, "Hợp làm bạn hơn?", "Có thể đây là một tình bạn tuyệt vời hơn là một mối tình."),
    LoveBand(0..5, "Hai quỹ đạo ngược chiều", "Như hai hành tinh ở hai đầu vũ trụ – nhưng ai biết được, vũ trụ vẫn luôn bất ngờ."),
)

private fun normalizeName(s: String) =
    Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace("\\p{M}".toRegex(), "")
        .replace("đ", "d").replace("Đ", "D")
        .lowercase()
        .filter { it.isLetter() }

/** 0..100, không phụ thuộc thứ tự tên, dấu hay hoa/thường. String.hashCode được Java định nghĩa cố định nên kết quả ổn định giữa các máy. */
fun loveScore(a: String, b: String): Int {
    val key = listOf(normalizeName(a), normalizeName(b)).sorted().joinToString("|")
    return Math.floorMod(key.hashCode() * 31 + 17, 101)
}

fun loveBand(score: Int) = LOVE_BANDS.first { score in it.range }
