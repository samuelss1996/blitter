package es.soutullo.blitter.model.vo.person

import android.graphics.drawable.Drawable

/**
 *
 */
abstract class Person(open val name: String) {
    // TODO UPDATE UML
    val profilePicture: Drawable by lazy {
        TODO()
    }
}