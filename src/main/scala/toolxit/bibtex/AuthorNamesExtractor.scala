/*
* This file is part of the ToolXiT project.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package toolxit.bibtex

import scala.util.parsing.combinator.RegexParsers

/**
 * Utilities to extract the name of the different authors defined in a string.
 * @author Lucas Satabin
 *
 */
object AuthorNamesExtractor extends RegexParsers {

  override def skipWhitespace = false

  lazy val nameSep = """(?i)\s+and\s+""".r

  lazy val names =
    rep1sep(uptoNameSep, nameSep) ^^ (_.map(_.toString))

  lazy val uptoNameSep =
    guard(nameSep) ~> "" ^^^ Word("") |
      rep1(block | special | not(nameSep) ~> ".".r) ^^ (list => Sentence(list.mkString))

  lazy val block: Parser[Block] =
    "{" ~> rep("[^\\{}]+".r ^^ Sentence | special | block) <~ "}" ^^ Block

  lazy val special: Parser[Special] =
    "\\" ~> "[^\\s{]+".r ~ opt(rep("{") ~> "[^}]*".r <~ rep("}")) ^^ {
      case spec ~ char => Special(spec, char)
    }

  def toList(authors: String) = {
    parseAll(names, authors).getOrElse(Nil).map { author =>
      AuthorNameExtractor.parseAll(AuthorNameExtractor.author, author).getOrElse {
        println("Wrong author format: " + author)
        println("This author is omitted")
        EmptyAuthor
      }
    }
  }

}