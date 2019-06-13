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

package com.pandatone.kumiwake.kumiwake

import android.support.annotation.ColorRes

import com.pandatone.kumiwake.R

/**
 * A way to make simple changes to the application's appearance at runtime in correlation to its
 * [Category].
 *
 * Usually this should be done via attributes and [android.view.ContextThemeWrapper]s.
 * In one case in Topeka it is more performant to work like this.
 * This case involves a trade-off between statically loading these themes versus inflation
 * in an adapter backed view without recycling.
 */
enum class Theme(@get:ColorRes
                                     val primaryColor: Int,
                 @get:ColorRes
                                     val windowBackgroundColor: Int, @get:ColorRes
                                     val textPrimaryColor: Int) {
    blue(R.color.blue_title, R.color.blue_background, R.color.theme_blue_text),
    green(R.color.green_title, R.color.green_background_title, R.color.theme_green_text),
    red(R.color.red_title, R.color.red_background_title, R.color.theme_red_text),
    yellow(R.color.yellow_title, R.color.yellow_background_title, R.color.theme_yellow_text)
}
