package com.example.palmreader

import kotlin.math.abs
import kotlin.math.sqrt

/** Một đường chỉ tay: danh sách điểm [y, x, dy, dx] – giữ nguyên dạng của code Python để đặc trưng khớp với các tâm cụm. */
typealias Line = List<IntArray>

/**
 * Lõi nhận diện đường chỉ tay, port từ yeonsumia/palmistry (Apache-2.0, xem assets/LICENSE-palmistry.txt).
 * Không phụ thuộc Android để chạy được unit test trên JVM. Ảnh là mảng ARGB hàng-trước, kích thước [SIZE]x[SIZE].
 */
object PalmLines {
    const val SIZE = 256
    const val MASK_THRESHOLD = 0.03f

    /** 21 điểm mốc MediaPipe của bàn tay chuẩn (chuẩn hóa 0..1) mà ảnh được nắn về – rectification.py, đã lật ngang. */
    val TARGET: List<Pair<Double, Double>> = listOf(
        0.48203104734420776 to 0.9063420295715332, 0.6043621301651001 to 0.8119394183158875, 0.6763232946395874 to 0.6790258884429932,
        0.7340714335441589 to 0.5716733932495117, 0.7896472215652466 to 0.5098430514335632, 0.5655680298805237 to 0.5117031931877136,
        0.5979393720626831 to 0.36575648188591003, 0.6135331392288208 to 0.2713503837585449, 0.6196483373641968 to 0.19251111149787903,
        0.4928809702396393 to 0.4982593059539795, 0.4899863600730896 to 0.3213786780834198, 0.4894656836986542 to 0.21283167600631714,
        0.48334982991218567 to 0.12900274991989136, 0.4258815348148346 to 0.5180916786193848, 0.4033462107181549 to 0.3581996262073517,
        0.3938145041465759 to 0.2616880536079407, 0.38608720898628235 to 0.1775170862674713, 0.36368662118911743 to 0.5642163157463074,
        0.33553171157836914 to 0.44737303256988525, 0.3209102153778076 to 0.3749568462371826, 0.31213682889938354 to 0.3026996850967407,
    ).map { (x, y) -> (1 - x) to y }

    // ---------------- 1. Nắn ảnh ----------------

    /**
     * Homography 3x3 (hàng-trước, h[8]=1) ánh xạ [src] -> [dst] theo bình phương tối thiểu.
     * shortcut: không dùng RANSAC như cv2.findHomography; đủ tốt vì 21 điểm MediaPipe ít khi sai lệch lớn – thêm RANSAC nếu gặp tay bị che.
     */
    fun homography(src: List<Pair<Double, Double>>, dst: List<Pair<Double, Double>>): DoubleArray {
        require(src.size == dst.size && src.size >= 4)
        val ata = Array(8) { DoubleArray(8) }
        val atb = DoubleArray(8)
        fun add(row: DoubleArray, b: Double) {
            for (i in 0 until 8) {
                atb[i] += row[i] * b
                for (j in 0 until 8) ata[i][j] += row[i] * row[j]
            }
        }
        for (k in src.indices) {
            val (x, y) = src[k]
            val (u, v) = dst[k]
            add(doubleArrayOf(x, y, 1.0, 0.0, 0.0, 0.0, -x * u, -y * u), u)
            add(doubleArrayOf(0.0, 0.0, 0.0, x, y, 1.0, -x * v, -y * v), v)
        }
        return solve(ata, atb) + 1.0
    }

    fun project(h: DoubleArray, x: Double, y: Double): Pair<Double, Double> {
        val w = h[6] * x + h[7] * y + h[8]
        return (h[0] * x + h[1] * y + h[2]) / w to (h[3] * x + h[4] * y + h[5]) / w
    }

    private fun solve(a: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        val n = b.size
        for (c in 0 until n) {
            val p = (c until n).maxBy { abs(a[it][c]) }
            require(abs(a[p][c]) > 1e-12) { "điểm mốc suy biến" }
            a[c] = a[p].also { a[p] = a[c] }
            b[c] = b[p].also { b[p] = b[c] }
            for (r in c + 1 until n) {
                val f = a[r][c] / a[c][c]
                for (k in c until n) a[r][k] -= f * a[c][k]
                b[r] -= f * b[c]
            }
        }
        val x = DoubleArray(n)
        for (r in n - 1 downTo 0) x[r] = (b[r] - (r + 1 until n).sumOf { a[r][it] * x[it] }) / a[r][r]
        return x
    }

    // ---------------- 2. Tách nền + chuẩn bị đầu vào U-Net ----------------

    /** tools.remove_background: giữ pixel màu da (HSV kiểu OpenCV 8-bit: H<=50, S>=20, V>=80) có G>10, còn lại tô trắng. */
    fun removeBackground(argb: IntArray): IntArray = IntArray(argb.size) { i ->
        val p = argb[i]
        val r = p shr 16 and 255
        val g = p shr 8 and 255
        val b = p and 255
        val v = maxOf(r, g, b)
        val diff = v - minOf(r, g, b)
        val s = if (v == 0) 0 else (255 * diff + v / 2) / v
        val hue = when {
            diff == 0 -> 0.0
            v == r -> 60.0 * (g - b) / diff
            v == g -> 120.0 + 60.0 * (b - r) / diff
            else -> 240.0 + 60.0 * (r - g) / diff
        }.let { if (it < 0) it + 360 else it } / 2
        val skin = Math.round(hue) <= 50 && s >= 20 && v >= 80
        if (skin && g > 10) p else -1 // -1 = 0xFFFFFFFF trắng
    }

    /** Tensor NCHW float [1,3,SIZE,SIZE], RGB / 255. */
    fun toInput(argb: IntArray): FloatArray {
        val n = SIZE * SIZE
        val out = FloatArray(3 * n)
        for (i in 0 until n) {
            val p = argb[i]
            out[i] = (p shr 16 and 255) / 255f
            out[n + i] = (p shr 8 and 255) / 255f
            out[2 * n + i] = (p and 255) / 255f
        }
        return out
    }

    fun mask(logits: FloatArray) = BooleanArray(logits.size) { logits[it] > MASK_THRESHOLD }

    // ---------------- 3. Làm mảnh (skimage _fast_skeletonize, Zhang-Suen dùng bảng tra) ----------------

    private val LUT = intArrayOf(
        0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 0, 3, 3,
        0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0,
        0, 0, 3, 1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
        3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        2, 3, 1, 3, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 1, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0,
    )

    fun skeletonize(img: BooleanArray, w: Int, h: Int): BooleanArray {
        val W = w + 2
        val skel = IntArray(W * (h + 2))
        for (y in 0 until h) for (x in 0 until w) if (img[y * w + x]) skel[(y + 1) * W + x + 1] = 1
        val clean = skel.copyOf()
        var removed = true
        while (removed) {
            removed = false
            for (pass in 0..1) {
                for (r in 1..h) for (c in 1..w) {
                    val i = r * W + c
                    if (skel[i] == 0) continue
                    val n = LUT[skel[i - W - 1] + 2 * skel[i - W] + 4 * skel[i - W + 1] + 8 * skel[i + 1] +
                        16 * skel[i + W + 1] + 32 * skel[i + W] + 64 * skel[i + W - 1] + 128 * skel[i - 1]]
                    if (n == 3 || (n == 1 && pass == 0) || (n == 2 && pass == 1)) {
                        clean[i] = 0
                        removed = true
                    }
                }
                clean.copyInto(skel)
            }
        }
        return BooleanArray(w * h) { skel[(it / w + 1) * W + it % w + 1] == 1 }
    }

    // ---------------- 4. Gom pixel thành các đường (classification.group) ----------------

    fun group(img: BooleanArray, w: Int, h: Int): List<Line> {
        val count = IntArray(w * h)
        val nodes = ArrayList<Int>()
        for (j in 1 until h - 1) for (i in 1 until w - 1) {
            if (!img[j * w + i]) continue
            var c = -1
            for (dy in -1..1) for (dx in -1..1) if (img[(j + dy) * w + i + dx]) c++
            count[j * w + i] = c
            if (c == 1 || c >= 3) nodes += j * w + i
        }
        nodes.sortBy { it / w + it % w } // sort ổn định như Python
        fun isNode(p: Int) = count[p] == 1 || count[p] >= 3

        val graph = LinkedHashMap<Int, LinkedHashMap<Int, List<IntArray>>>()
        for (n in nodes) graph[n] = LinkedHashMap()
        val notVisited = BooleanArray(w * h) { true }
        fun around(p: Int): List<Pair<Int, Int>> { // vị trí (dy,dx) 0..2 theo thứ tự hàng-trước, như np.nonzero
            val y = p / w
            val x = p % w
            val out = ArrayList<Pair<Int, Int>>()
            for (dy in 0..2) for (dx in 0..2) {
                val q = (y + dy - 1) * w + x + dx - 1
                if (count[q] != 0 && notVisited[q]) out += dy to dx
            }
            return out
        }
        fun link(line: List<IntArray>) {
            val a = line.first()[0] * w + line.first()[1]
            val b = line.last()[0] * w + line.last()[1]
            graph[a]!![b] = line
            graph[b]!![a] = line.reversed()
        }

        for (node in nodes) {
            notVisited[node] = false
            val y0 = node / w
            val x0 = node % w
            for ((dy0, dx0) in around(node)) {
                var dy = dy0
                var dx = dx0
                val ny = y0 + dy - 1
                val nx = x0 + dx - 1
                if (dx == 0 || (dy == 0 && dx == 1)) { dy = 2 - dy; dx = 2 - dx }
                val line = arrayListOf(intArrayOf(y0, x0, 0, 0), intArrayOf(ny, nx, dy - 1, dx - 1))
                if (isNode(ny * w + nx)) {
                    notVisited[ny * w + nx] = true
                    link(line)
                    continue
                }
                while (true) {
                    val cur = line.last()[0] * w + line.last()[1]
                    notVisited[cur] = false
                    val next = around(cur).firstOrNull() ?: break
                    val y = cur / w
                    val x = cur % w
                    val ty = y + next.first - 1
                    val tx = x + next.second - 1
                    var sy = ty - y
                    var sx = tx - x
                    if (sx == -1 || (sy == -1 && sx == 0)) { sy = -sy; sx = -sx }
                    line += intArrayOf(ty, tx, sy, sx)
                    notVisited[ty * w + tx] = false
                    if (isNode(ty * w + tx)) {
                        link(line)
                        notVisited[ty * w + tx] = true
                        break
                    }
                }
            }
            notVisited[node] = true
        }

        // tìm mọi đường đi bằng quay lui
        val linesNode = ArrayList<List<Int>>()
        val visited = HashMap<Int, Boolean>()
        val finished = HashMap<Int, Boolean>()
        for (n in nodes) { visited[n] = false; finished[n] = false }
        val temp = ArrayList<Int>()
        fun backtrack(node: Int) {
            var end = true
            for (next in graph[node]!!.keys) {
                if (visited[next]!!) continue
                end = false
                temp += next
                visited[next] = true
                finished[next] = true
                backtrack(next)
                temp.removeAt(temp.size - 1)
                visited[next] = false
            }
            if (end) linesNode += ArrayList(temp)
        }
        for (n in nodes) if (!finished[n]!!) {
            temp.clear(); temp += n
            visited[n] = true
            finished[n] = true
            backtrack(n)
        }

        // lọc: bỏ đường quay ngược hướng hoặc ngắn hơn 10 điểm
        val lines = ArrayList<Line>()
        for (ln in linesNode) {
            if (ln.size == 1) continue
            val line = ArrayList<IntArray>()
            var wrong = false
            for (i in 1 until ln.size) {
                val cur = ln[i - 1]
                val nxt = ln[i]
                if (i > 1) {
                    val prev = ln[i - 2]
                    val dot = (cur / w - prev / w) * (nxt / w - cur / w) + (cur % w - prev % w) * (nxt % w - cur % w)
                    if (dot < 0) { wrong = true; break }
                }
                line += graph[cur]!![nxt]!!
            }
            if (!wrong && line.size >= 10) lines += line
        }
        return lines
    }

    // ---------------- 5. Chọn 3 đường: tim, trí, sinh đạo ----------------

    /** Tâm cụm K-means đã huấn luyện sẵn trong repo, theo thứ tự [tim, trí, sinh đạo]. */
    private val CENTERS = arrayOf(
        doubleArrayOf(5.232849, 4.881592, 6.3223267, 6.64093, 0.8113839, 0.655735, 0.82874316, 0.74796075, 0.7993417, 0.8345605, 0.68143266, 0.90320605,
            0.5769709, 0.9721149, 0.53258324, 0.98307294, 0.4804058, 0.9829783, 0.36796156, 0.99141085, 0.24345541, 0.99082345, 0.30017138, 0.9736235),
        doubleArrayOf(5.645419, 4.169626, 7.126243, 6.0026045, 0.3532842, 0.928315, 0.4692493, 0.9680717, 0.578683, 0.9680221, 0.7227269, 0.9454175,
            0.7741767, 0.9495983, 0.7802345, 0.89685285, 0.8743354, 0.8478447, 0.85625464, 0.82669544, 0.88459945, 0.8000444, 0.8956431, 0.74734426),
        doubleArrayOf(5.755994, 3.8910964, 8.680631, 5.3926454, 0.4247846, 0.93111324, 0.6940754, 0.9203782, 0.8567455, 0.767301, 0.9177662, 0.6054738,
            0.9801044, 0.47111732, 0.9812451, 0.34593108, 0.97122467, 0.28715244, 0.9036454, 0.26124895, 0.8069528, 0.25324377, 0.59989274, 0.32016128),
    )

    fun feature(line: Line, h: Int, w: Int): DoubleArray {
        val f = DoubleArray(24)
        f[0] = line.minOf { it[0] } * 10.0 / h
        f[1] = line.minOf { it[1] } * 10.0 / w
        f[2] = line.maxOf { it[0] } * 10.0 / h
        f[3] = line.maxOf { it[1] } * 10.0 / w
        val step = line.size / 10
        for (i in 0 until 10) {
            val seg = line.subList(i * step, (i + 1) * step)
            f[4 + 2 * i] = seg.sumOf { it[2] }.toDouble() / seg.size
            f[5 + 2 * i] = seg.sumOf { it[3] }.toDouble() / seg.size
        }
        return f
    }

    /** Mỗi tâm cụm lấy đường gần nhất chưa bị chọn (repo gốc kiểm tra `range(i-1)` nên có thể chọn trùng – đã sửa). */
    fun classify(lines: List<Line>, h: Int = SIZE, w: Int = SIZE): List<Line?> {
        val feats = lines.map { feature(it, h, w) }
        val picked = HashSet<Int>()
        return CENTERS.map { c ->
            val best = lines.indices.filter { it !in picked }.minByOrNull { j -> sqrt(feats[j].indices.sumOf { (feats[j][it] - c[it]).let { d -> d * d } }) }
            best?.let { picked += it; lines[it] }
        }
    }

    // ---------------- 6. Đo độ dài (measurement.py) ----------------

    data class Lengths(val heartLong: Boolean, val headLong: Boolean, val lifeLong: Boolean)

    /** [lm] là 21 điểm mốc chuẩn hóa trong hệ tọa độ ảnh đã nắn (chưa lật). Trả null nếu thiếu đường. */
    fun measure(lines: List<Line?>, lm: List<Pair<Double, Double>>, size: Int = SIZE): Lengths? {
        val (heart, head, life) = lines.takeIf { it.size == 3 && it.all { l -> l != null } }?.map { it!! } ?: return null
        val heartThresX = size * (lm[9].first + (lm[5].first - lm[9].first) * 2 / 5)
        val headThresX = size * (lm[13].first + (lm[9].first - lm[13].first) / 3)
        val lifeThresY = size * (lm[1].second + (lm[0].second - lm[1].second) / 3)
        return Lengths(
            heartLong = heart.first()[1] < heartThresX,
            headLong = head.last()[1] > headThresX,
            lifeLong = life.last()[0] > lifeThresY,
        )
    }
}
