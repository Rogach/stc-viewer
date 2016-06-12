Revolver.settings

javacOptions ++= Seq("-source", "1.8")

javaOptions += "-Dprism.order=sw"

libraryDependencies := Seq("org.controlsfx" % "controlsfx" % "8.40.10")

packageOptions in (Compile, packageBin) := Seq(Package.ManifestAttributes("Main-Class" -> "Main"))

assemblyJarName in assembly := "stc-viewer.jar"
