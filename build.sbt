Revolver.settings

javacOptions ++= Seq("-source", "1.8")

javaHome := Some(file("/home/platon/PrF/jdk1.8.0_45/"))

javaOptions += "-Dprism.order=sw"

libraryDependencies += "org.controlsfx" % "controlsfx" % "8.20.8"
