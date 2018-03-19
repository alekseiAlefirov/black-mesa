package com.virtuslab.blackmesa.visualization

import com.virtuslab.blackmesa.model.{ BaseScheduler, Grid, Model }

object SampleApp extends App {
  val configuration = Configuration(
    (_: ModelParameters) => new Model {
      override def grid: Grid[_] = ???

      override def schedule: BaseScheduler = ???
    },
    IndexedSeq.empty[VisualizationElement],
    "test",
    Map.empty)

  new BlackMesaServer(configuration).start()
}
