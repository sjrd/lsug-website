package lsug
package protocol

import cats._
import cats.implicits._
import cats.data.NonEmptyList
import java.time.{LocalDateTime, LocalDate, LocalTime}

import io.circe._
import io.circe.generic.semiauto._

object Twitter {

  final class Handle(val value: String) extends AnyVal

  object Handle {
    implicit val decoder: Decoder[Handle] = Decoder[String].map(new Handle(_))
    implicit val encoder: Encoder[Handle] = Encoder[String].contramap(_.value)
    implicit val eq: Eq[Handle] = Eq[String].contramap(_.value)
    implicit val show: Show[Handle] = Show[String].contramap(_.value)
  }
}

object Github {

  final class Org(val value: String) extends AnyVal

  object Org {
    implicit val decoder: Decoder[Org] = Decoder[String].map(new Org(_))
    implicit val encoder: Encoder[Org] = Encoder[String].contramap(_.value)
    implicit val eq: Eq[Org] = Eq[String].contramap(_.value)
  }

  final class User(val value: String) extends AnyVal

  object User {
    implicit val decoder: Decoder[User] = Decoder[String].map(new User(_))
    implicit val encoder: Encoder[User] = Encoder[String].contramap(_.value)
    implicit val eq: Eq[User] = Eq[String].contramap(_.value)
  }

}

final class Email(val value: String) extends AnyVal

object Email {
  implicit val decoder: Decoder[Email] = Decoder[String].map(new Email(_))
  implicit val encoder: Encoder[Email] = Encoder[String].contramap(_.value)
  implicit val eq: Eq[Email] = Eq[String].contramap(_.value)
}

final class Link(val value: String) extends AnyVal

object Link {

  implicit val decoder: Decoder[Link] = Decoder[String].map(new Link(_))
  implicit val encoder: Encoder[Link] = Encoder[String].contramap(_.value)
  implicit val show: Show[Link] = Show[String].contramap(_.value)
  implicit val eq: Eq[Link] = Eq[String].contramap(_.value)

}

final class Asset(val path: String) extends AnyVal

object Asset {
  implicit val decoder: Decoder[Asset] = Decoder[String].map(new Asset(_))
  implicit val encoder: Encoder[Asset] = Encoder[String].contramap(_.path)
  implicit val eq: Eq[Asset] = Eq[String].contramap(_.path)

  def fromPath(path: String): Either[String, Asset] =
    new Asset(path).asRight

  implicit val show: Show[Asset] = Show.show { asset =>
    s"/assets/${asset.path}"
  }

  val twitter = new Asset("twitter.svg")
}

sealed trait Markup

object Markup {

  sealed trait Text extends Markup { self =>
    def string: String = self match {
      case Text.Plain(value)        => value
      case Text.Styled.Code(text)   => text
      case Text.Styled.Strong(text) => text.map(_.string).fold
      case Text.Styled.Italic(text) => text.map(_.string).fold
      case Text.Link(text, _)       => text
    }

    def trim: Text = self match {
      case Text.Plain(value)          => Text.Plain(value.trim)
      case Text.Link(text, location)  => Text.Link(text.trim, location)
      case code @ Text.Styled.Code(_) => code
      case styled                     => styled
      //TODO: finish this off
    }
  }

  object Text {

    sealed trait Styled extends Text

    case class Link(text: String, location: String) extends Text

    object Styled {

      case class Code(text: String) extends Styled
      case class Strong(text: NonEmptyList[Text]) extends Styled
      case class Italic(text: NonEmptyList[Text]) extends Styled

      implicit val codec: Codec[Styled] = deriveCodec[Styled]

    }

    case class Plain(value: String) extends Text

    object Plain {
      implicit val codec: Codec[Plain] = deriveCodec[Plain]
    }

    implicit val codec: Codec[Text] = deriveCodec[Text]

  }

  case class Table(headings: NonEmptyList[Text], rows: List[Table.Row])
      extends Markup

  object Table {

    case class Row(columns: NonEmptyList[Text]) extends Markup

    object Row {
      implicit val codec: Codec[Row] = deriveCodec[Row]
    }

    implicit val codec: Codec[Table] = deriveCodec[Table]

  }

  case class Section(heading: Text, content: List[Markup]) extends Markup

  object Section {
    implicit val codec: Codec[Section] = deriveCodec[Section]
  }

  case class CodeBlock(lang: String, code: NonEmptyList[String]) extends Markup

  case class Paragraph(text: NonEmptyList[Text]) extends Markup

  object Paragraph {
    implicit val codec: Codec[Paragraph] = deriveCodec[Paragraph]
  }

  implicit val codec: Codec[Markup] = deriveCodec[Markup]
  implicit val eq: Eq[Markup] = Eq.fromUniversalEquals[Markup]

}

case class CodeOfConduct(
    project: List[Markup],
    meetup: List[Markup],
    contacts: NonEmptyList[Email]
)

object CodeOfConduct {
  implicit val codec: Codec[CodeOfConduct] = deriveCodec[CodeOfConduct]
}

case class Sponsor(
    id: Sponsor.Id,
    logo: Option[Either[Link, Asset]],
    description: List[Markup],
    begin: LocalDate
)

object Sponsor {

  final class Id(val value: String) extends AnyVal

  object Id {
    implicit val decoder: Decoder[Id] = Decoder[String].map(new Id(_))
    implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.value)
    implicit val eq: Eq[Speaker.Id] = Eq[String].contramap(_.value)
    implicit val show: Show[Id] = Show[String].contramap(_.value)
  }

}

case class Speaker(
    profile: Speaker.Profile,
    bio: List[Markup],
    socialMedia: Speaker.SocialMedia
)

object Speaker {

  implicit val codec: Codec[Speaker] = deriveCodec[Speaker]

  final class Id(val value: String) extends AnyVal

  object Id {
    implicit val decoder: Decoder[Id] = Decoder[String].map(new Id(_))
    implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.value)
    implicit val eq: Eq[Speaker.Id] = Eq[String].contramap(_.value)
    implicit val show: Show[Id] = Show[String].contramap(_.value)
  }

  case class SocialMedia(
      blog: Option[Link],
      twitter: Option[Twitter.Handle],
      github: Option[Github.User]
  )

  object SocialMedia {

    implicit val eq: Eq[SocialMedia] = Eq.instance {
      case (SocialMedia(b, t, g), SocialMedia(bb, tt, gg)) =>
        b === bb && tt === t && g === gg
    }

    implicit val codec: Codec[SocialMedia] = deriveCodec[SocialMedia]
  }

  case class Profile(
      id: Id,
      name: String,
      photo: Option[Asset]
  )

  object Profile {
    implicit val codec: Codec[Profile] = deriveCodec[Profile]
    implicit val eq: Eq[Profile] = Eq.instance {
      case (Profile(i, n, p), Profile(ii, nn, pp)) =>
        i === ii && n === nn && p === pp
    }
  }
}

object Venue {

  final class Id(val value: String) extends AnyVal

  object Id {

    implicit val decoder: Decoder[Id] = Decoder[String].map(new Id(_))
    implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.value)
    implicit val show: Show[Id] = Show[String].contramap(_.value)
    implicit val eq: Eq[Id] = Eq[String].contramap(_.value)
  }

  case class Summary(id: Id, name: String, address: NonEmptyList[String])

  object Summary {
    implicit val codec: Codec[Summary] = deriveCodec[Summary]
    implicit val eq: Eq[Summary] = Eq.instance {
      case (Summary(i, n, a), Summary(ii, nn, aa)) =>
        i === ii && n === nn && a === aa
    }
  }
}

case class Event[A](
    host: NonEmptyList[Speaker.Id],
    welcome: List[Markup],
    virtual: Option[Event.Virtual],
    summary: Event.Summary[A],
    schedule: Event.Schedule
)

object Event {

  final class Id(val value: String) extends AnyVal

  object Id {

    implicit val decoder: Decoder[Id] = Decoder[String].map(new Id(_))
    implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.value)
    implicit val show: Show[Id] = Show[String].contramap(_.value)
    implicit val eq: Eq[Id] = Eq[String].contramap(_.value)

  }

  case class Schedule(items: NonEmptyList[Schedule.Item])

  object Schedule {

    case class Item(event: String, start: LocalTime, end: LocalTime)

    object Item {
      implicit val codec: Codec[Item] = deriveCodec[Item]
    }

    implicit val codec: Codec[Schedule] = deriveCodec[Schedule]
  }

  case class Virtual(
      open: LocalTime,
      closed: LocalTime,
      providers: NonEmptyList[Virtual.Provider]
  )

  object Virtual {

    sealed trait Provider

    object Provider {

      case class Blackboard(link: Link) extends Provider
      case class Gitter(link: Link) extends Provider

      implicit val codec: Codec[Provider] = deriveCodec

    }

    implicit val codec: Codec[Virtual] = deriveCodec

  }

  sealed trait Location

  object Location {

    case object Virtual extends Location
    case class Physical(id: Venue.Id) extends Location

    implicit val codec: Codec[Location] = deriveCodec[Location]
    implicit val eq: Eq[Location] = Eq.fromUniversalEquals[Location]
  }

  case class Time(start: LocalDateTime, end: LocalDateTime)

  object Time {
    implicit val codec: Codec[Time] = deriveCodec[Time]
    implicit val eq: Eq[Time] = Eq.fromUniversalEquals[Time]
  }

  case class Summary[A](
      id: Id,
      time: Time,
      location: Location,
      events: List[A]
  )

  object Summary {
    implicit def codec[A: Codec]: Codec[Summary[A]] = deriveCodec[Summary[A]]
  }

  case class Blurb(
      event: String,
      description: List[Markup],
      speakers: List[Speaker.Id],
      tags: List[String]
  )

  object Blurb {
    implicit val codec: Codec[Blurb] = deriveCodec[Blurb]
  }

  case class Item(
      blurb: Blurb,
      setup: List[Markup],
      slides: Option[Link],
      recording: Option[Link],
      photos: List[Asset]
  )

  object Item {
    implicit val codec: Codec[Item] = deriveCodec[Item]
  }

  implicit val codec: Codec[Event[Item]] = deriveCodec[Event[Item]]

  object Meetup {

    case class Event(link: Link, attendees: Int)

    implicit val codec: Codec[Meetup.Event] = deriveCodec[Meetup.Event]
    implicit val eq: Eq[Event] = Eq.instance {
      case (Event(l, a), Event(ll, aa)) =>
        l === ll && a === aa
    }

    object Group {

      final class Id(val value: String) extends AnyVal

      object Id {
        implicit val show: Show[Id] = Show[String].contramap(_.value)
        implicit val decoder: Decoder[Id] = Decoder[String].map(new Id(_))
        implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.value)
      }
    }

    object Event {

      final class Id(val value: String) extends AnyVal

      object Id {
        implicit val show: Show[Id] = Show[String].contramap(_.value)
        implicit val decoder: Decoder[Id] = Decoder[String].map(new Id(_))
        implicit val encoder: Encoder[Id] = Encoder[String].contramap(_.value)
        implicit val eq: Eq[Id] = Eq[String].contramap(_.value)
      }

    }

  }

}

case class Contact(
    email: Email,
    twitter: Twitter.Handle,
    github: Github.Org
)