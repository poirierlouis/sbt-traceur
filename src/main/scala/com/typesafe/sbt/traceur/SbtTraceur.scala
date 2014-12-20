package com.typesafe.sbt.traceur

import com.typesafe.sbt.jse.{SbtJsEngine, SbtJsTask}
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web._

import sbt.Keys._
import sbt._
import spray.json._

object Import {

  val traceur = TaskKey[Seq[File]]("traceur", "Run Traceur compiler")

  object TraceurKeys {

    val sourceFileNames = SettingKey[Seq[String]]("traceur-sources", "Files to compile. Should just be the 'root' modules, traceur will pull the rest. So for example if A.js requires B.js requires C.js, only list A.js here. Default main.js")
    val outputFileName = SettingKey[String]("traceur-output", "Name of the output file. Default main.js")
    val experimental = SettingKey[Boolean]("traceur-experimental", "Turns on all experimental features. Default false")
    val sourceMaps = SettingKey[Boolean]("traceur-source-maps", "Enable source maps generation")
    val includeRuntime = SettingKey[Boolean]("traceur-include-runtime", "If traceur-runtime.js code should be included in the output file. Default true")
    val extraOptions = SettingKey[Seq[String]]("traceur-extra-options", "Extra options to pass to traceur command line")

  }

}

object SbtTraceur extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import Import._
  import Import.TraceurKeys._
  import com.typesafe.sbt.jse.SbtJsTask.autoImport.JsTaskKeys._
  import com.typesafe.sbt.web.Import.WebKeys._
  import com.typesafe.sbt.web.SbtWeb.autoImport._
  import SbtJsEngine.autoImport.JsEngineKeys._

  override def projectSettings = Seq(
    includeFilter := GlobFilter("*.js"),
    sourceFileNames := Seq("main.js"),
    outputFileName := "main.js",
    experimental := false,
    sourceMaps := true,
    includeRuntime := true,
    extraOptions := Seq(),
    traceur in Assets := runTraceur(Assets).dependsOn(webJarsNodeModules in Plugin).value,
    traceur in TestAssets := runTraceur(TestAssets).dependsOn(webJarsNodeModules in Plugin).value,
    sourceGenerators in Assets <+= traceur in Assets,
    sourceGenerators in TestAssets <+= traceur in TestAssets
  )

  def boolToParam(condition:Boolean, param:String): Seq[String] = {
    if (condition) Seq(param) else Seq()
  }

  private def runTraceur(config:Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
  
      val sourceDir = (sourceDirectory in config).value
      val outputDir = (resourceManaged in config).value
      val files = (sourceDir ** includeFilter.value).get

	  val commandlineParameters = (
		boolToParam(experimental.value, "--experimental")
		++ boolToParam(sourceMaps.value, "--source-maps=file")
		++ extraOptions.value
		++ Seq("--out", (outputDir / outputFileName.value).toString)
		++ boolToParam(includeRuntime.value,
		  ((webJarsNodeModulesDirectory in Plugin).value / "traceur" / "bin" / "traceur-runtime.js").toString
		)
		++ (sourceFileNames.value.map(file => (sourceDir / file).toString))
	  )

	  streams.value.log.info("Compiling with Traceur")

	  SbtJsTask.executeJs(
		state.value,
		// For now traceur only works with node
		EngineType.Node,
		None,
		Nil,
		(webJarsNodeModulesDirectory in Plugin).value / "traceur" / "src" / "node" / "command.js",
		commandlineParameters,
		(timeoutPerSource in traceur).value * files.size
	  )

	  files.toSeq
  }
}