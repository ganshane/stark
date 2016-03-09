/*
 * Copyright 2016 GanShaNe network
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


object Developers {
  lazy val members = Map(
    "jcai" -> "Jun Tsai"
  )

  def toXml =
    <developers>
      {members map { m =>
      <developer>
        <id>{m._1}</id>
        <name>{m._2}</name>
        <url>http://github.com/{m._1}</url>
      </developer>
    }}
    </developers>
}