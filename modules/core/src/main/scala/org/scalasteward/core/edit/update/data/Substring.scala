/*
 * Copyright 2018-2022 Scala Steward contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalasteward.core.edit.update.data

import scala.util.matching.Regex.Match

object Substring {
  final case class Position(start: Int, value: String) {
    def replaceWith(replacement: String): Replacement =
      Replacement(this, replacement)
  }

  object Position {
    def fromMatch(m: Match, value: String): Position = {
      val start = m.start + m.matched.indexOf(value)
      Position(start, value)
    }
  }

  final case class Replacement(position: Position, replacement: String) {
    private def replaceIn(source: String): String = {
      val before = source.substring(0, position.start)
      val after = source.substring(position.start + position.value.length)
      before + replacement + after
    }
  }

  object Replacement {
    def applyAll(replacements: List[Replacement])(source: String): String =
      replacements
        .sortBy(_.position.start)(Ordering.Int.reverse)
        .foldLeft(source)((s, r) => r.replaceIn(s))
  }
}
