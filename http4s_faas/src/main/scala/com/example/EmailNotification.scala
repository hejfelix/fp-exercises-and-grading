package com.example

import cats.effect.Sync
import cats.implicits._
import org.apache.commons.mail.{Email, EmailException, HtmlEmail}

import scala.xml.Elem

case class EmailCredentials(user: String, password: String)
case class EmailConfig(hostname: String,
                       port: Int,
                       senderEmail: String,
                       senderName: String,
                       useSsl: Boolean = false,
                       credentials: Option[EmailCredentials] = None)
case class User(email: String, name: String)

class EmailNotification[F[_]](emailConfiguration: EmailConfig)(implicit F: Sync[F]) {

  private val logger = org.log4s.getLogger

  def notifyUserHtml(user: User, title: String)(html: Elem): F[Elem] =
    (F.delay(createHtmlEmail(user, title, html)) >>= sendEmail(user)) *> F.pure(html)

  private def sendEmail(user: User)(email: Email): F[Unit] =
    F.delay({
      logger.info(s"Sending e-mail: ${email.getHostName}, ${email.getSmtpPort}")
      try {
        val res = email.send()
        logger.info(s"Email sent: ${res}")
      } catch {
        case emailException: EmailException =>
          logger.error(emailException)(s"Failed to send email: ${emailException.getMessage}")
      }
    }) >>=
      (emailId => F.delay(logger.info(s"Sent email with id $emailId to user $user")))

  private def prepareEmail[T <: Email](email: T, user: User, title: String): T = {
    emailConfiguration.credentials.foreach { creds =>
      email.setAuthentication(creds.user, creds.password)
    }
    email.setHostName(emailConfiguration.hostname)
    email.setSmtpPort(emailConfiguration.port)
    email.setSSLOnConnect(emailConfiguration.useSsl)
    email.setFrom(emailConfiguration.senderEmail, emailConfiguration.senderName)
    email.setSubject(title)
    email.addTo(user.email, user.name)
    email
  }

  private def createHtmlEmail(user: User, title: String, notificationHtml: Elem): HtmlEmail = {
    val email = prepareEmail(new HtmlEmail(), user, title)
    email.setHtmlMsg(notificationHtml.toString)
  }

}
