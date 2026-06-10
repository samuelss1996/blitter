package es.soutullo.blitter.view.util

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EdgeToEdge(
    val mode: EdgeToEdgeMode = EdgeToEdgeMode.CONTENT,
    val statusBarColor: EdgeToEdgeStatusBarColor = EdgeToEdgeStatusBarColor.DEFAULT
)

enum class EdgeToEdgeMode {
    CONTENT,
    STATUS_BAR_ONLY,
    APP_INTRO,
    NONE
}

enum class EdgeToEdgeStatusBarColor {
    DEFAULT,
    CAMERA_INTRO,
    ASSIGNATION_INTRO
}
