package com.example.palmreader

import java.time.LocalDate
import java.time.MonthDay

/** 12 cung theo thứ tự hoàng đạo (Bạch Dương = 0) – thứ tự này cũng là chỉ số rasi trong chiêm tinh Vệ Đà. */
enum class Sign(
    val vi: String,
    val api: String,
    val symbol: String,
    val start: MonthDay,
    val element: String,
    val ruler: String,
    val core: String,
    val strengths: String,
    val challenges: String,
    val love: String,
    val career: String,
    val mind: String,
    val balance: String,
    val moon: String,
) {
    ARIES(
        "Bạch Dương", "aries", "♈", MonthDay.of(3, 21), "Lửa", "Sao Hỏa",
        "Người tiên phong, thẳng thắn và luôn muốn là người bắt đầu.",
        "Dũng cảm, quyết đoán, tràn đầy năng lượng.",
        "Nóng vội, dễ mất kiên nhẫn khi mọi việc chậm lại.",
        "Yêu nồng nhiệt, thích chủ động theo đuổi và cần cảm giác mới mẻ.",
        "Hợp vai trò dẫn dắt, khởi nghiệp, công việc cạnh tranh.",
        "Cảm xúc đến nhanh và đi cũng nhanh, ít để bụng.",
        "Đếm đến mười trước khi phản ứng; vận động thể chất để xả năng lượng.",
        "Mặt Trăng Bạch Dương: cảm xúc bộc phát, cần được hành động ngay để thấy an toàn.",
    ),
    TAURUS(
        "Kim Ngưu", "taurus", "♉", MonthDay.of(4, 20), "Đất", "Sao Kim",
        "Điềm tĩnh, thực tế và trân trọng sự ổn định.",
        "Kiên trì, đáng tin cậy, có gu thẩm mỹ.",
        "Bảo thủ, ngại thay đổi, đôi khi cố chấp.",
        "Chung thủy, thể hiện tình cảm qua sự chăm sóc và những điều cụ thể.",
        "Hợp tài chính, nghệ thuật, ẩm thực, công việc cần sự bền bỉ.",
        "Cảm xúc sâu nhưng chậm bộc lộ; cần thời gian để tin tưởng.",
        "Thử một thói quen nhỏ mới mỗi tuần để mềm dẻo hơn.",
        "Mặt Trăng Kim Ngưu: tìm bình yên trong sự quen thuộc, đồ ăn ngon và không gian dễ chịu.",
    ),
    GEMINI(
        "Song Tử", "gemini", "♊", MonthDay.of(5, 21), "Khí", "Sao Thủy",
        "Tò mò, linh hoạt và giỏi giao tiếp.",
        "Nhanh trí, hài hước, học hỏi rất nhanh.",
        "Thiếu tập trung, dễ chán, hay đổi ý.",
        "Cần một người bạn đồng hành để trò chuyện và cùng khám phá.",
        "Hợp truyền thông, bán hàng, giảng dạy, viết lách.",
        "Lý trí dẫn dắt cảm xúc; hay nghĩ nhiều.",
        "Hoàn thành một việc trước khi bắt đầu việc mới.",
        "Mặt Trăng Song Tử: xử lý cảm xúc bằng cách nói ra hoặc viết ra.",
    ),
    CANCER(
        "Cự Giải", "cancer", "♋", MonthDay.of(6, 21), "Nước", "Mặt Trăng",
        "Giàu cảm xúc, chu đáo và gắn bó với gia đình.",
        "Biết quan tâm, trực giác tốt, trung thành.",
        "Nhạy cảm quá mức, dễ tổn thương, hay hoài niệm.",
        "Yêu bằng cả trái tim, cần cảm giác được che chở và thuộc về.",
        "Hợp chăm sóc, giáo dục, nhân sự, nhà hàng – khách sạn.",
        "Cảm xúc lên xuống theo môi trường xung quanh.",
        "Đặt ranh giới rõ ràng để không gánh cảm xúc của người khác.",
        "Mặt Trăng Cự Giải: cần một 'tổ ấm' – nơi chốn hoặc con người – để thấy an toàn.",
    ),
    LEO(
        "Sư Tử", "leo", "♌", MonthDay.of(7, 23), "Lửa", "Mặt Trời",
        "Tự tin, hào phóng và thích tỏa sáng.",
        "Lôi cuốn, ấm áp, có tinh thần lãnh đạo.",
        "Kiêu hãnh, cần được công nhận, đôi khi phô trương.",
        "Lãng mạn, rộng lượng, muốn được ngưỡng mộ và trân trọng.",
        "Hợp sân khấu, quản lý, sáng tạo, thương hiệu cá nhân.",
        "Tình cảm mạnh mẽ, dễ tổn thương khi bị phớt lờ.",
        "Ghi nhận công sức của người khác cũng nhiều như của mình.",
        "Mặt Trăng Sư Tử: cần được nhìn thấy và khen ngợi để thấy mình có giá trị.",
    ),
    VIRGO(
        "Xử Nữ", "virgo", "♍", MonthDay.of(8, 23), "Đất", "Sao Thủy",
        "Tỉ mỉ, chu toàn và luôn muốn cải thiện mọi thứ.",
        "Phân tích tốt, chăm chỉ, có trách nhiệm.",
        "Cầu toàn, hay lo lắng và tự trách.",
        "Thể hiện tình yêu qua hành động giúp đỡ thiết thực.",
        "Hợp y tế, kế toán, kỹ thuật, biên tập, nghiên cứu.",
        "Đầu óc bận rộn, dễ căng thẳng vì chi tiết.",
        "Chấp nhận 'đủ tốt' thay vì hoàn hảo.",
        "Mặt Trăng Xử Nữ: thấy yên lòng khi mọi việc được sắp xếp gọn gàng.",
    ),
    LIBRA(
        "Thiên Bình", "libra", "♎", MonthDay.of(9, 23), "Khí", "Sao Kim",
        "Hòa nhã, công bằng và yêu cái đẹp.",
        "Khéo léo, biết lắng nghe, giỏi hòa giải.",
        "Do dự, ngại xung đột, dễ chiều lòng người khác.",
        "Coi trọng sự đồng điệu và tôn trọng lẫn nhau trong tình yêu.",
        "Hợp luật, ngoại giao, thiết kế, tư vấn.",
        "Cần sự cân bằng; bất hòa khiến họ mệt mỏi.",
        "Tập nói 'không' một cách nhẹ nhàng nhưng rõ ràng.",
        "Mặt Trăng Thiên Bình: cảm thấy ổn khi các mối quan hệ hài hòa.",
    ),
    SCORPIO(
        "Bọ Cạp", "scorpio", "♏", MonthDay.of(10, 23), "Nước", "Sao Diêm Vương",
        "Sâu sắc, bí ẩn và có ý chí mạnh mẽ.",
        "Kiên định, nhạy bén, trung thành tuyệt đối.",
        "Đa nghi, chiếm hữu, khó tha thứ.",
        "Yêu sâu và trọn vẹn, cần sự thật lòng hoàn toàn.",
        "Hợp điều tra, tâm lý, tài chính, nghiên cứu chuyên sâu.",
        "Cảm xúc mãnh liệt nhưng giấu kín.",
        "Buông bỏ điều không thể kiểm soát.",
        "Mặt Trăng Bọ Cạp: cảm xúc sâu, cần sự tin tưởng tuyệt đối trước khi mở lòng.",
    ),
    SAGITTARIUS(
        "Nhân Mã", "sagittarius", "♐", MonthDay.of(11, 22), "Lửa", "Sao Mộc",
        "Lạc quan, yêu tự do và ham khám phá.",
        "Cởi mở, vui vẻ, tầm nhìn rộng.",
        "Thiếu kiên nhẫn, nói thẳng đến vô tình, ngại ràng buộc.",
        "Cần một người bạn đời cũng là bạn đồng hành phiêu lưu.",
        "Hợp du lịch, giáo dục, xuất bản, công việc quốc tế.",
        "Tìm ý nghĩa trong mọi trải nghiệm.",
        "Cam kết với một mục tiêu đủ lâu để thấy kết quả.",
        "Mặt Trăng Nhân Mã: cần không gian và tự do để cảm thấy dễ chịu.",
    ),
    CAPRICORN(
        "Ma Kết", "capricorn", "♑", MonthDay.of(12, 22), "Đất", "Sao Thổ",
        "Nghiêm túc, kỷ luật và có tham vọng dài hạn.",
        "Bền bỉ, có tổ chức, đáng tin cậy.",
        "Khắt khe, ít bộc lộ cảm xúc, dễ làm việc quá sức.",
        "Yêu chậm mà chắc, thể hiện qua sự cam kết lâu dài.",
        "Hợp quản lý, tài chính, kỹ thuật, xây dựng sự nghiệp từng bước.",
        "Kiềm chế cảm xúc để giữ hình ảnh vững vàng.",
        "Dành thời gian nghỉ ngơi như một phần của kế hoạch.",
        "Mặt Trăng Ma Kết: thấy an toàn khi kiểm soát được trách nhiệm của mình.",
    ),
    AQUARIUS(
        "Bảo Bình", "aquarius", "♒", MonthDay.of(1, 20), "Khí", "Sao Thiên Vương",
        "Độc lập, sáng tạo và nghĩ khác người.",
        "Đổi mới, nhân ái, có tư duy tiến bộ.",
        "Xa cách, bướng bỉnh với quan điểm của mình.",
        "Coi trọng tình bạn trong tình yêu và cần không gian riêng.",
        "Hợp công nghệ, khoa học, hoạt động cộng đồng.",
        "Tiếp cận cảm xúc bằng lý trí.",
        "Cho phép bản thân cảm nhận thay vì chỉ phân tích.",
        "Mặt Trăng Bảo Bình: cần tự do cảm xúc và những người bạn hiểu mình.",
    ),
    PISCES(
        "Song Ngư", "pisces", "♓", MonthDay.of(2, 19), "Nước", "Sao Hải Vương",
        "Mơ mộng, giàu lòng trắc ẩn và nhạy cảm.",
        "Trực giác mạnh, sáng tạo, biết cảm thông.",
        "Dễ trốn tránh thực tế, thiếu ranh giới.",
        "Yêu lãng mạn và hết lòng, đôi khi lý tưởng hóa người yêu.",
        "Hợp nghệ thuật, âm nhạc, chữa lành, công việc thiện nguyện.",
        "Hấp thụ cảm xúc của người xung quanh như miếng bọt biển.",
        "Viết nhật ký hoặc thiền để tách cảm xúc của mình khỏi của người khác.",
        "Mặt Trăng Song Ngư: cảm xúc bao la, cần thời gian một mình để nạp lại năng lượng.",
    );

    fun sections() = listOf(
        "Tính cách cốt lõi" to core,
        "Điểm mạnh" to strengths,
        "Thử thách" to challenges,
        "Tình yêu & quan hệ" to love,
        "Phong cách sự nghiệp" to career,
        "Tâm trí & cảm xúc" to mind,
        "Lời khuyên cân bằng" to balance,
    )

    val label get() = "$symbol $vi"

    companion object {
        /** Cung Mặt Trời (chiêm tinh phương Tây) theo ngày sinh. */
        fun of(date: LocalDate): Sign {
            val md = MonthDay.from(date)
            return entries.filter { it.start <= md }.maxByOrNull { it.start } ?: CAPRICORN
        }
    }
}
