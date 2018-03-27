package org.virtuslab.example.shop

import org.virtuslab.blackmesa.model.space.Grid
import org.virtuslab.blackmesa.model.Agent
import org.virtuslab.blackmesa.model.BaseScheduler
import org.virtuslab.blackmesa.model.{ BaseScheduler, Model }

case class ShopModel(grid: Grid[Agent]) extends Model {

  def withAgents(agents: List[Agent]): ShopModel = {
    agents.foreach(agent => grid.placeAgent(agent, agent.pos))
    ShopModel(grid)
  }

  override def schedule: BaseScheduler = new BaseScheduler(this)

  override def step(): Unit = {
    grid.getAllAgents.foreach(a => a.step(this))
  }

}
