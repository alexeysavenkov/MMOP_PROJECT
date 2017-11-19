package com.mmop.utils

object RandomString {
  def apply(possibleCharacters : String, length: Int): String = {
    (1 to length).map(_ => possibleCharacters((Math.random() * possibleCharacters.length).toInt)).mkString
  }
}
