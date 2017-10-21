version := "1.0.0"

Revolver.settings

javacOptions ++= Seq("-source", "1.8", "-Xlint:unchecked")

javaOptions ++= Seq(
  "-Dprism.order=sw",
  "-Xmx1000m"
)

libraryDependencies := Seq("org.controlsfx" % "controlsfx" % "8.40.14")

packageOptions in (Compile, packageBin) := Seq(Package.ManifestAttributes("Main-Class" -> "Main"))

assemblyJarName in assembly := "stc-viewer.jar"

mainClass in reStart := Some("Main")
