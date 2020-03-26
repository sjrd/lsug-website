package lsug
package ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import cats._
import cats.data._
import cats.implicits._
import lsug.{protocol => P}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import lsug.ui.implicits._

object common {

  val Spinner = ScalaComponent
    .builder[Unit]("Spinner")
    .render_(<.div(^.cls := "spinner"))
    .build

  val Banner = ScalaComponent
    .builder[String]("Banner")
    .render_P { asset =>
      <.div(
        ^.cls := "banner",
        <.img(^.src := s"/assets/${asset}", ^.alt := "")
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  val PersonBadge = ScalaComponent
    .builder[Option[P.Asset]]("PersonBadge")
    .render_P(pic =>
      <.div(
        ^.cls := "person-badge",
        pic
          .map(asset => <.img(^.src := asset.show))
          .getOrElse(
            MaterialIcon("person")
          )
      )
    )
    .build

  val MaterialIcon = ScalaComponent
    .builder[String]("MaterialIcon")
    .render_P(<.span(^.cls := "material-icons", _))
    .configure(Reusability.shouldComponentUpdate)
    .build

  //TODO: Need props for header
  val Markup = {

    def renderText(markup: P.Markup.Text): TagMod = {
      markup match {
        case P.Markup.Text.Plain(s) => s
        case P.Markup.Text.Styled.Italic(text) =>
          <.em(text.map(renderText).toList.toTagMod)
        case P.Markup.Text.Styled.Strong(text) =>
          <.strong(text.map(renderText).toList.toTagMod)
        case P.Markup.Text.Styled.Code(code) =>
          <.pre(<.code(code))
        case P.Markup.Text.Link(text, loc) =>
          <.a(^.href := loc, text)
      }
    }

    ScalaComponent
      .builder[P.Markup]("Markup")
      .render_P {
        case P.Markup.Paragraph(text) =>
          <.p(text.map(renderText).toList.toTagMod)
        case m =>
          println(m)
          ???
      }
      .build

  }

  object panel {

    val Summary = ScalaComponent
      .builder[(Boolean, Boolean => Callback)]("PanelSummary")
      .render_PC {
        case ((expanded, onToggle), children) =>
          <.div(
            ^.cls := ("panel-summary".cls |+| (if (expanded)
                                                 "panel-toggle-on".cls
                                               else
                                                 "panel-toggle-off".cls)).show,
            ^.onClick --> onToggle(!expanded),
            children
          )
      }
      .build

    val Details = ScalaComponent
      .builder[Boolean]("PanelDetails")
      .render_PC {
        case (expanded, children) =>
          <.div(
            ^.cls := ("panel-details".cls |+| (if (expanded)
                                                 "panel-expanded".cls
                                               else "panel-hidden".cls)).show,
            children
          )
      }
      .build

    val Panel = ScalaComponent
      .builder[Unit]("Panel")
      .render_C(cs =>
        <.div(
          ^.cls := "panel",
          cs
        )
      )
      .build
  }

  object sidesheet {

    val SideSheet = ScalaComponent
      .builder[Unit]("SideSheet")
      .render_C(cs =>
        <.div(
          ^.cls := "side-sheet",
          <.div(
            ^.cls := "side-sheet-content",
            cs
          )
        )
      )
      .build
  }

  object tabbed {

    val Tab = ScalaComponent
      .builder[(Int, Boolean, Callback)]("Tab")
      .render_P {
        case (width, selected, onSelect) =>
          <.div(
            ^.width := width.px,
            ^.cls := ("tab".cls |+| (if (selected)
                                       "tab-selected".cls
                                     else "tab-unselected".cls)).show,
            ^.onClick --> onSelect
          )
      }
      .build

    // val Tabs = ScalaComponent
    //   .builder[(Int, Int)]("Tabs")
    //   .render_PC {
    //     case ((width, selected), children) =>
    //       <.div(
    //         ^.cls := "tabs",
    //         <.div(children),
    //         <.span(
    //           ^.cls := "tab-indicator",
    //           ^.width := s"${width.show}px",
    //           ^.left := s"${selected * width}px"
    //         )
    //       )
    //   }
    //   .build

    // val TabContent = ScalaComponent
    //   .builder[Boolean]("TabContent")
    //   .render_PC {
    //     case (selected, children) =>
    //       <.div(
    //         ^.cls := ("tab-content".cls |+| if(selected) ),
    //         children
    //       )
    //   }
    //   .build

  }

  val Tabbed = {

    final class Backend(
        $ : BackendScope[NonEmptyList[String], String]
    ) {

      def render(
          current: String,
          children: PropsChildren,
          names: NonEmptyList[String]
      ): VdomNode = {

        val width = 120
        val index = names.zipWithIndex
          .collectFirst {
            case (n, i) if n === current => i
          }
          .getOrElse(0)

        <.div(
          <.div(
            ^.cls := "tab-menu",
            <.ul(
              names.map { n =>
                <.li(
                  <.button(
                    ^.width := s"${width.show}px",
                    ^.cls := (if (current === n) "tab-btn tab-current"
                              else "tab-btn"),
                    ^.onClick --> $.setState(n),
                    n
                  )
                )
              }.toList: _*
            ),
            <.span(
              ^.cls := "tab-indicator",
              ^.width := s"${width.show}px",
              ^.left := s"${index * width}px"
            )
          ),
          <.div(
            ^.cls := "tab-content",
            children.iterator
              .zip(names.toList)
              .map {
                case (child, name) => child.when(name === current)
              }
              .toTagMod
          )
        )
      }

    }

    ScalaComponent
      .builder[NonEmptyList[String]]("Tabbed")
      .initialStateFromProps(_.head)
      .renderBackendWithChildren[Backend]
      .build
  }

  val NavBar = ScalaComponent
    .builder[Unit]("nav-bar")
    .renderStatic(
      <.nav(
        <.span("lsug"),
        <.div(
          <.a("abous us")
        )
      )
    )
    .build

  val Disclaimer = {

    val format = DateTimeFormatter.ofPattern("yyyy")

    ScalaComponent
      .builder[(String, LocalDate)]("Disclaimer")
      .render_P {
        case (id, now) =>
          <.div(
            ^.cls := "disclaimer",
            <.p(
              s"© ${now.format(format)}.",
              s"London Scala User Group is a registered community interest group in England and Wales (",
              <.a(
                id,
                ^.href := s"https://beta.companieshouse.gov.uk/company/${id}"
              ),
              ")"
            )
          )
      }
      .build
  }

  val Footer = ScalaComponent
    .builder[LocalDate]("Footer")
    .render_P(now =>
      <.div(
        ^.cls := "footer",
        Disclaimer(("123240125", now))
      )
    )
    .configure(Reusability.shouldComponentUpdate)
    .build

// <iframe
//   width="450"
//   height="250"
//   frameborder="0" style="border:0"
//   src="https://www.google.com/maps/embed/v1/search?key=YOUR_API_KEY&q=record+stores+in+Seattle" allowfullscreen>
// </iframe>

}