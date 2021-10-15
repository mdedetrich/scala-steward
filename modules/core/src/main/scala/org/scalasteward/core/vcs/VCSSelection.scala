/*
 * Copyright 2018-2021 Scala Steward contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalasteward.core.vcs

import cats.MonadThrow
import cats.syntax.all._
import org.scalasteward.core.application.Config
import org.scalasteward.core.util.HttpJsonClient
import org.scalasteward.core.vcs.VCSType.{Bitbucket, BitbucketServer, GitHub, GitLab}
import org.scalasteward.core.vcs.bitbucket.BitbucketApiAlg
import org.scalasteward.core.vcs.bitbucketserver.BitbucketServerApiAlg
import org.scalasteward.core.vcs.github.GitHubApiAlg
import org.scalasteward.core.vcs.gitlab.GitLabApiAlg
import org.typelevel.log4cats.Logger

final class VCSSelection[F[_]](config: Config)(implicit
    client: HttpJsonClient[F],
    logger: Logger[F],
    F: MonadThrow[F]
) {
  private def gitHubApiAlg: GitHubApiAlg[F] =
    new GitHubApiAlg[F](config.vcsCfg.apiHost, _ => _.pure[F])

  private def gitLabApiAlg: GitLabApiAlg[F] =
    new GitLabApiAlg[F](config.vcsCfg, config.gitLabCfg, _ => _.pure[F])

  private def bitbucketApiAlg: BitbucketApiAlg[F] =
    new BitbucketApiAlg(config.vcsCfg, _ => _.pure[F])

  private def bitbucketServerApiAlg: BitbucketServerApiAlg[F] =
    new BitbucketServerApiAlg[F](config.vcsCfg.apiHost, config.bitbucketServerCfg, _ => _.pure[F])

  def vcsApiAlg: VCSApiAlg[F] =
    config.vcsCfg.tpe match {
      case GitHub          => gitHubApiAlg
      case GitLab          => gitLabApiAlg
      case Bitbucket       => bitbucketApiAlg
      case BitbucketServer => bitbucketServerApiAlg
    }
}
