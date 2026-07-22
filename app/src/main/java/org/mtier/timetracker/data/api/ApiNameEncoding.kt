package org.mtier.timetracker.data.api

import java.net.URLEncoder

/**
 * The PHP backend's startTimer/stopTimer/addWorkInterval methods each call
 * urldecode() on the `name` path segment themselves, on top of whatever
 * decoding Nextcloud's router already does — a legacy quirk carried over
 * unchanged from the web frontend, which double-encodes with
 * encodeURIComponent(encodeURIComponent(name)) for exactly these three
 * endpoints (and only these three; add-tag/add-client/add-project take a
 * single encoding). See timetracker's js/src/app/views/TimerView.vue.
 */
fun doubleEncodeName(name: String): String {
    val once = URLEncoder.encode(name, "UTF-8")
    return URLEncoder.encode(once, "UTF-8")
}
