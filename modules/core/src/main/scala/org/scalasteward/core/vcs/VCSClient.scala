package org.scalasteward.core.vcs

import cats.effect.MonadCancelThrow
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{BasicCredentials, Header, Request, Uri}
import org.scalasteward.core.application.Config.VCSCfg
import org.scalasteward.core.vcs.VCSType.{Bitbucket, BitbucketServer, GitHub, GitLab}
import org.scalasteward.core.vcs.data.AuthenticatedUser
import org.typelevel.ci._

object VCSClient {
  def apply[F[_]: MonadCancelThrow](
      vcsCfg: VCSCfg,
      user: AuthenticatedUser
  )(underlying: Client[F]): Client[F] = {
    val modify = addHeaders[F](vcsType, user)
    // also check scheme
    vcsApiHost.host.fold(underlying) { host =>
      Client.apply { req =>
        val modified = if (req.uri.host.contains(host)) modify(req) else req
        println(modified)
        underlying.run(modified)
      }
    }
  }

  private def addHeaders[F[_]](
      vcsType: VCSType,
      user: AuthenticatedUser
  ): Request[F] => Request[F] =
    vcsType match {
      case Bitbucket       => _.putHeaders(basicAuth(user))
      case BitbucketServer => _.putHeaders(basicAuth(user), xAtlassianToken)
      case GitHub          => _.putHeaders(basicAuth(user))
      case GitLab          => _.putHeaders(Header.Raw(ci"Private-Token", user.accessToken))
    }

  private def basicAuth(user: AuthenticatedUser): Authorization =
    Authorization(BasicCredentials(user.login, user.accessToken))

  // Bypass the server-side XSRF check, see
  // https://github.com/scala-steward-org/scala-steward/pull/1863#issuecomment-754538364
  private val xAtlassianToken = Header.Raw(ci"X-Atlassian-Token", "no-check")
}
