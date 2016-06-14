package app

import client.HttpClient

/**
  * Created by Hierro on 6/13/16.
  */
object Main extends App {
  val client = new HttpClient()
  client.streamByHashTag("newyork")
}
