package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import model.Tweet
import org.json4s._
import org.json4s.native.JsonMethods._
import util.Oauth

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by Hierro on 5/31/16.
  */
class HttpClient {

  private val config = com.typesafe.config.ConfigFactory.load()
  private val oauthKeys = config.getConfig("oauth")
  private val consumerKey = oauthKeys.getString("consumerKey")
  private val consumerSecret = oauthKeys.getString("consumerSecret")
  private val accessToken = oauthKeys.getString("accessToken")
  private val accessTokenSecret = oauthKeys.getString("accessTokenSecret")

  def streamByHashTag(body: String) = streamData(s"track=$body")

  def streamByUsers(body: String) = streamData(s"follow=$body") //needs call to api to get user data (id)  -> https://api.twitter.com/1.1/users/lookup.json?screen_name=twitterapi,twitter


  private[this] def streamData(body: String) = {
    implicit val actorSystem = ActorSystem("system")
    implicit val mat = ActorMaterializer()
    val protocol = "https://"
    val baseUrl = "stream.twitter.com"
    val path = "/1.1/statuses/filter.json"
    val fullUrl = protocol + baseUrl + path

    val oAuthHeaders: Future[String] = Oauth.getOauthHeader(consumerKey, consumerSecret, accessToken, accessTokenSecret, fullUrl, body)

    oAuthHeaders.onComplete {
      case Success(header) =>
        val httpHeaders: List[HttpHeader] = List(
          HttpHeader.parse("Authorization", header) match {
            case ParsingResult.Ok(h, _) => Some(h)
            case _ => None
          },
          HttpHeader.parse("Accept", "*/*") match {
            //not sure if I need this
            case ParsingResult.Ok(h, _) => Some(h)
            case _ => None
          }
        ).flatten

        implicit val formats = DefaultFormats
        val connectionFlow =
          Http().outgoingConnectionHttps(baseUrl)
            .map { response =>
              if (response.status != StatusCodes.OK) {
                println(response.entity.dataBytes.runForeach(bs => println("Non OK Problem: " + bs.utf8String)))
                Future(Unit)
              } else {
                response.entity.dataBytes
                  .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
                  .filter(_.contains("\r\n"))
                  .map(json => Try(parse(json).extract[Tweet]))
                  .runForeach {
                    case Success(tweet) =>
                      println("-----")
                      println(s"${tweet.user.name} -> ${tweet.text}")
                    case Failure(e) =>
                      println("-----")
                      println(response.entity.dataBytes.runForeach(db => println(db.utf8String)))
                  }
              }
            }

        val httpRequest = HttpRequest(
          entity = HttpEntity(contentType = MediaTypes.`application/x-www-form-urlencoded`.withCharset(HttpCharsets.`UTF-8`), data = ByteString(body)),
          method = HttpMethods.POST,
          headers = httpHeaders,
          uri = Uri(path)
        )


        Source.single(httpRequest)
          .via(connectionFlow)
          .runForeach(println)

      case Failure(t) =>
        println("Failure: " + t)
    }
  }
}