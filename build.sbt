import sbtassembly.MergeStrategy

name := "MMOP_Project"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "17.11.0",
  "com.twitter" %% "finatra-http" % "17.11.0",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.1.0",
  "com.h2database"  %  "h2"                % "1.4.196",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "org.json4s" % "json4s-jackson_2.12" % "3.5.3"
)


val defaultMergeStrategy: String => MergeStrategy = {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

assemblyMergeStrategy in assembly := {
//  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
//  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
//  case "application.conf"                            => MergeStrategy.concat
//  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    defaultMergeStrategy(x)
}