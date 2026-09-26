//[app](../../../index.md)/[com.resistine.android.ui.theme](../index.md)/[AppThemeManager](index.md)

# AppThemeManager

[androidJvm]\
object [AppThemeManager](index.md)

Manager responsible for storing, retrieving, and applying application night/light theme modes.

## Functions

| Name | Summary |
|---|---|
| [applyStoredMode](apply-stored-mode.md) | [androidJvm]<br>fun [applyStoredMode](apply-stored-mode.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Applies the stored theme mode. |
| [current](current.md) | [androidJvm]<br>fun [current](current.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [AppThemeMode](../-app-theme-mode/index.md)<br>Retrieves the currently selected [AppThemeMode](../-app-theme-mode/index.md). |
| [set](set.md) | [androidJvm]<br>fun [set](set.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), mode: [AppThemeMode](../-app-theme-mode/index.md))<br>Sets and persists a new theme mode, applying it immediately. |