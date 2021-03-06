import com.solarmosaic.client.mail.EnvelopeWrappers
import com.solarmosaic.client.mail.content.ContentType.MultipartTypes
import javax.mail.{Authenticator, PasswordAuthentication}

/**
  * Created by guoning on 2017/6/6.
  *
  * 发送 DingDing
  *
  *
  */
package object notice {

  import org.fire.spark.streaming.core.kit.Utils

  case class Ding(api: String, to: String, message: String)
  case class EMail(to:String,
                   subject:String,
                   message:String,
                   user:String,
                   password:String,
                   addr:String="",
                   port:Int = 587,
                   tls:Boolean=false
                  ) extends Authenticator {
    override def getPasswordAuthentication(): PasswordAuthentication = {
      new PasswordAuthentication(user, password)
    }
  }

  object send extends EnvelopeWrappers {

    def a(ding: Ding): Unit = {

      val body =
        s"""
           |{
           |  "msgtype": "text",
           |  "text": {
           |    "content": "${ding.message}"
           |  },
           |  "at": {
           |    "atMobiles": [
           |      "${ding.to}"
           |    ],
           |    "isAtAll": false
           |  }
           |}
        """.stripMargin

      val headers = Map("content-type" -> "application/json")
      val (code,res) = Utils.httpPost(ding.api,body,headers)

      println(s"result code : $code , body : $res")
    }


    def a(email: EMail): Unit = {

      import com.solarmosaic.client.mail._
      import com.solarmosaic.client.mail.configuration._
      import com.solarmosaic.client.mail.content._


      val config = SmtpConfiguration(host = email.addr,
        port = email.port,
        tls = email.tls,
        debug = true,
        authenticator = Some(email)
      )
      val mailer = Mailer(config)
      val content = Multipart(
        parts = Seq(Text("text"), Html(s"<p>${email.message}</p>")),
        subType = MultipartTypes.alternative
      )

      val envelope = Envelope(
        from = email.user,
        to = Seq(email.to),
        subject = email.subject,
        content = content
      )
      mailer.send(envelope)
    }
  }

}