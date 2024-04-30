/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandatone.kumiwake

import androidx.annotation.ColorRes

/**
 * Usually this should be done via attributes and [android.view.ContextThemeWrapper]s.
 * In one case in Topeka it is more perform to work like this.
 * This case involves a trade-off between statically loading these themes versus inflation
 * in an adapter backed view without recycling.
 */
enum class Theme(
    @get:ColorRes
    val primaryColor: Int,
    @get:ColorRes
    val backgroundColor: Int,
) {
    Default(R.color.top_primary, android.R.color.white),
    Kumiwake(R.color.red_title, R.color.red_background),
    Sekigime(R.color.green_title, R.color.green_background),
    Others(R.color.gray, R.color.background_gray),
    Member(R.color.blue_title, R.color.blue_background),
    Setting(R.color.orange_title, R.color.orange_background)
}
