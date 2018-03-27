package org.virtuslab.blackmesa.model

import scala.collection.mutable

/**
 * Mesa Time Module
 * ================
 * *
 * Objects for handling the time component of a model. In particular, this module
 * contains Schedulers, which handle agent activation. A Scheduler is an object
 * which controls when agents are called upon to act, and when.
 * *
 * The activation order can have a serious impact on model behavior, so it's
 * important to specify it explicitly. Example simple activation regimes include
 * activating all agents in the same order every step, shuffling the activation
 * order every time, activating each agent *on average* once per step, and more.
 * *
 * Key concepts:
 * Step: Many models advance in 'steps'. A step may involve the activation of
 * all agents, or a random (or selected) subset of them. Each agent in turn
 * may have their own step() method.
 * *
 * Time: Some models may simulate a continuous 'clock' instead of discrete
 * steps. However, by default, the Time is equal to the number of steps the
 * model has taken.
 * *
 *
 * TODO: Have the schedulers use the model's randomizer, to keep random number
 * seeds consistent and allow for replication.
 *
 */

/**
 * Simplest scheduler; activates agents one at a time, in the order
 * they were added.
 * *
 * Assumes that each agent added has a *step* method which takes no arguments.
 * *
 * (This is explicitly meant to replicate the scheduler in MASON).
 *
 */
class BaseScheduler(var model: Model) {

  var steps = 0
  var time: Double = 0
  var agents = mutable.ArrayBuffer.empty[Agent]

  def add(agent: Agent): Unit = {
    agents += agent
  }

  def remove(agent: Agent): Unit = {
    while (agents.contains(agent))
      agents -= agent
  }

  def step(): Unit = {
    agents.foreach(_.step(model))
    steps += 1
    time += 1
  }

  def agentCount(): Int = {
    agents.size
  }

  def shuffleAgents(): Unit = {
    val shuffled = model.random.shuffle(agents)
    agents.clear()
    agents ++= shuffled
  }

}

/**
 * A scheduler which activates each agent once per step, in random order,
 * with the order reshuffled every step.
 * *
 * This is equivalent to the NetLogo 'ask agents...' and is generally the
 * default behavior for an ABM.
 * *
 * Assumes that all agents have a step(model) method.
 *
 */
class RandomActivation(model: Model) extends BaseScheduler(model) {

  override def step(): Unit = {
    shuffleAgents()
    super.step()
  }

}

/**
 * A scheduler to simulate the simultaneous activation of all the agents.
 * *
 * This scheduler requires that each agent have two methods: step and advance.
 * step() activates the agent and stages any necessary changes, but does not
 * apply them yet. advance() then applies the changes.
 *
 */
class SimultaneousActivation(model: Model) extends BaseScheduler(model) {

  override def step(): Unit = {
    agents.foreach(_.step(model))
    agents.foreach(_.advance())
    steps += 1
    time += 1
  }

}

/**
 * A scheduler which allows agent activation to be divided into several
 * stages instead of a single `step` method. All agents execute one stage
 * before moving on to the next.
 * *
 * Agents must have all the stage methods implemented. Stage methods take a
 * model object as their only argument.
 * *
 * This schedule tracks steps and time separately. Time advances in fractional
 * increments of 1 / (# of stages), meaning that 1 step = 1 unit of time.
 *
 */
class StagedActivation(
  model: Model,
  stages: List[String] = "step" :: Nil,
  shuffle: Boolean = false,
  shuffleBetweenStages: Boolean = false) extends BaseScheduler(model) {

  var stageTime = 1.0 / stages.size

  override def step(): Unit = {
    if (shuffle) {
      shuffleAgents()
    }

    stages.foreach { stage: String =>

      agents.foreach(_.callStage(stage, model))

      if (shuffleBetweenStages) shuffleAgents()

      time += stageTime

    }

    steps += 1

  }
}
