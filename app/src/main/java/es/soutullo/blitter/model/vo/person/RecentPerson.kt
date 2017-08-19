package es.soutullo.blitter.model.vo.person

import java.util.*

/**
 *
 */
data class RecentPerson(val name: String, val lastDate: Date) : Person(name) {
}