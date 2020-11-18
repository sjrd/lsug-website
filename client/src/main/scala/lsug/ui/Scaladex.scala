package lsug.ui
package event

import lsug.protocol.Github.{Org, Repo}
import lsug.protocol.Scaladex.Project
import cats.implicits._
import monocle.Iso
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Scaladex {

  val Badge = {
    final class Backend(
        $ : BackendScope[(Org, Repo), Option[Project]]
    ) {

      def render(props: (Org, Repo), s: Option[Project]): VdomNode = {
        val (org, repo) = props
        <.div(
          ^.cls := "scaladex-badge",
          <.p(
            ^.cls := "scaladex-repo",
            <.span(org.show),
            <.span("/"),
            <.span(repo.show)
          ),
          s.map(p => <.img(^.src := p.logo.show))
            .getOrElse(<.div(^.cls := "loading"))
        )
      }

      def load: Callback = {
        (for {
          (org, repo) <- $.props.async
          _ <- Resource.load($.modState)(
            Iso.id.asLens,
            s"scaladex/${org.show}/${repo.show}"
          )
        } yield ()).toCallback
      }
    }

    ScalaComponent
      .builder[(Org, Repo)]("ScaladexBadge")
      .initialState[Option[Project]](
        None
      )
      .renderBackend[Backend]
      .componentDidMount(_.backend.load)
      .build
  }

}
