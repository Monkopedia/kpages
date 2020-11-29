# Lanterna Kotlin Extensions

This library aims to make it easy to develop lanterna UI from kotlin, however it has
grown in scope a little as I needed random things that weren't supported. In general,
it has the following major components:

 - Coroutines + DSLs for better kotlin integration
 - Separate layout management system (Dynamic Layouts)
   - This allows for layouts that can wrap content in more flexible ways, and is very similar to
     android's view layout. Maybe in the future I'll do a constraint based layout.
 - Advanced text features
   - Spannable CharSequences for UI
   - Ascii-art fonts
 - Screen management/navigation
 - Some key handling code
