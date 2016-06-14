package util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent.Future, concurrent.ExecutionContext.Implicits.global

/**
  * Created by Hierro on 5/31/16.
  */
object Oauth {
  //Get your credentials from https://apps.twitter.com and replace the values below
  private val url = "https://stream.twitter.com/1.1/statuses/filter.json"

  //Create Oauth 1a header
  def getOauthHeader(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String, url: String, body: String)
                    (implicit system: ActorSystem, mat: ActorMaterializer): Future[String] = {

  val consumer = new DefaultConsumerService(system.dispatcher)

    consumer.createOauthenticatedRequest(
      KoauthRequest(
        method = "POST",
        url = url,
        authorizationHeader = None,
        body = Some(body)
      ),
      consumerKey,
      consumerSecret,
      accessToken,
      accessTokenSecret
    ) map (_.header)
  }

}
