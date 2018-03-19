package com.virtuslab.blackmesa.visualization.modules

import com.virtuslab.blackmesa.model.{ Agent, Grid }

trait GridAware[AgentValue >: Null <: Agent] {
  def grid: Grid[AgentValue]
}
