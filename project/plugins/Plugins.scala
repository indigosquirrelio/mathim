import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val untypedRepo = "Untyped Repo" at "http://repo.untyped.com"
  val closureCompiler = "com.untyped" % "sbt-closure" % "0.1"
}
