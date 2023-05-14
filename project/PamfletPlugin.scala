import com.typesafe.sbt.site.SitePlugin.autoImport.siteSubdirName
import com.typesafe.sbt.site.SitePlugin
import com.typesafe.sbt.site.util.SiteHelpers
import sbt.Keys._
import sbt._
import pamflet.FileStorage
import pamflet.Produce

object PamfletPlugin extends AutoPlugin {
  override def requires = SitePlugin
  override def trigger = noTrigger
  object autoImport {
    val Pamflet = config("pamflet")
  }

  import autoImport._

  override def projectSettings = pamfletSettings(Pamflet)

  private def pamfletSettings(config: Configuration): Seq[Setting[_]] =
    inConfig(config)(
      Def.settings(
        includeFilter := AllPassFilter,
        mappings := {
          val output = target.value
          val storage = FileStorage(sourceDirectory.value, Nil)
          Produce(storage.globalized, output)
          output ** includeFilter.value --- output pair Path.relativeTo(output)
        },
        siteSubdirName := "",
        SiteHelpers.directorySettings(config),
        SiteHelpers.watchSettings(config),
        SiteHelpers.addMappingsToSiteDir(config / mappings, config / siteSubdirName),
      )
    )
}
