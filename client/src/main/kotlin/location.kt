class Location(
    val name: String = "Forbidden Wall",
    val city: String = "Yosemite",
    val region: String = "Yosemite",
    val country: String = "United States",
    val hazards: List<String> = listOf("Powerlines", "Trees", "Legality"),
    val history: String = "Opened by Dean Potter in 2013",
    val approach: String = "Follow the trail to Yosemite Falls",
    val images: List<Image> = listOf(Image(), Image())
)

class Image(
    val url: String = "https://www.basebeta.com/images/forbbiden1.jpg",
    val caption: String = "Trailhead parking")
