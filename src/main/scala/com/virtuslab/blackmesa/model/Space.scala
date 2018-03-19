package com.virtuslab.blackmesa.model

import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * Base class for a square grid.
 * *
 * Grid cells are indexed by [x][y], where [0][0] is assumed to be the
 * bottom-left and [width-1][height-1] is the top-right. If a grid is
 * toroidal, the top and bottom, and left and right, edges wrap to each other
 * *
 * Properties:
 * width, height: The grid's width and height.
 * torus: Boolean which determines whether to treat the grid as a torus.
 * grid: Internal list-of-lists which holds the grid cells themselves.
 * *
 * Methods:
 * get_neighbors: Returns the objects surrounding a given cell.
 * get_neighborhood: Returns the cells surrounding a given cell.
 * get_cell_list_contents: Returns the contents of a list of cells
 * ((x,y) tuples)
 * neighbor_iter: Iterates over position neightbors.
 * coord_iter: Returns coordinates as well as cell contents.
 * place_agent: Positions an agent on the grid, and set its pos variable.
 * move_agent: Moves an agent from its current position to a new position.
 * iter_neighborhood: Returns an iterator over cell coordinates that are
 * in the neighborhood of a certain point.
 * torus_adj: Converts coordinate, handles torus looping.
 * out_of_bounds: Determines whether position is off the grid, returns
 * the out of bounds coordinate.
 * iter_cell_list_contents: Returns an iterator of the contents of the
 * cells identified in cell_list.
 * get_cell_list_contents: Returns a list of the contents of the cells
 * identified in cell_list.
 * remove_agent: Removes an agent from the grid.
 * is_cell_empty: Returns a bool of the contents of a cell.
 *
 */
abstract class Grid[AgentValue >: Null <: Agent: ClassTag](width: Int, height: Int, isTorus: Boolean) {

  var grid: Array[Array[AgentValue]] = Array.fill(width) {
    Array.fill(height)(defaultValue())
  }

  var empties: mutable.TreeSet[(Int, Int)] = (for {
    w <- 0.until(width)
    h <- 0.until(height)
  } yield {
    w -> h
  })(collection.breakOut)

  def defaultValue(): AgentValue

  def apply(index: Int): Array[AgentValue] = grid(index)

  /**
   * An iterator that returns coordinates as well as cell contents.
   */
  def coordinatesIter: Iterator[(AgentValue, Int, Int)] = {
    (for {
      w <- 0.until(width)
      h <- 0.until(height)
    } yield {
      (grid(w)(h), w, h)
    })(collection.breakOut)
  }

  /**
   * Iterate over position neighbors.
   * *
   * Args:
   * pos: (x,y) coords tuple for the position to get the neighbors of.
   * moore: Boolean for whether to use Moore neighborhood (including
   * diagonals) or Von Neumann (only up/down/left/right).
   */
  def neighborsIter(pos: (Int, Int), moore: Boolean = true): Iterator[AgentValue] = {
    val neighborhood: Iterator[(Int, Int)] = iterNeighborhood(pos, moore = moore)
    iterCellListContents(neighborhood)
  }

  /**
   * Return a list of cells that are in the neighborhood of a
   * certain point.
   * *
   * Args:
   * pos: Coordinate tuple for the neighborhood to get.
   * moore: If True, return Moore neighborhood
   * (including diagonals)
   * If False, return Von Neumann neighborhood
   * (exclude diagonals)
   * include_center: If True, return the (x, y) cell as well.
   * Otherwise, return surrounding cells only.
   * radius: radius, in cells, of neighborhood to get.
   * *
   * Returns:
   * A list of coordinate tuples representing the neighborhood;
   * With radius 1, at most 9 if Moore, 5 if Von Neumann (8 and 4
   * if not including the center).
   */
  def getNeighborhood(pos: (Int, Int), moore: Boolean, includeCenter: Boolean = false, radius: Int = 1): List[(Int, Int)] = {
    iterNeighborhood(pos, moore, includeCenter, radius).toList
  }

  def getNeighbors(pos: (Int, Int), moore: Boolean, includeCenter: Boolean = false, radius: Int = 1): List[AgentValue] = {
    iterNeighbors(pos, moore, includeCenter, radius).toList
  }

  /**
   * Return an iterator over neighbors to a certain point.
   * *
   * Args:
   * pos: Coordinates for the neighborhood to get.
   * moore: If True, return Moore neighborhood
   * (including diagonals)
   * If False, return Von Neumann neighborhood
   * (exclude diagonals)
   * include_center: If True, return the (x, y) cell as well.
   * Otherwise,
   * return surrounding cells only.
   * radius: radius, in cells, of neighborhood to get.
   * *
   * Returns:
   * An iterator of non-None objects in the given neighborhood;
   * at most 9 if Moore, 5 if Von-Neumann
   * (8 and 4 if not including the center).
   */
  def iterNeighbors(pos: (Int, Int), moore: Boolean, includeCenters: Boolean = false, radius: Int = 1): Iterator[AgentValue] = {
    val neighborhood = iterNeighborhood(pos, moore, includeCenters, radius)
    iterCellListContents(neighborhood)
  }

  /**
   * Return an iterator over cell coordinates that are in the
   * neighborhood of a certain point.
   * *
   * Args:
   * pos: Coordinate tuple for the neighborhood to get.
   * moore: If True, return Moore neighborhood
   * (including diagonals)
   * If False, return Von Neumann neighborhood
   * (exclude diagonals)
   * include_center: If True, return the (x, y) cell as well.
   * Otherwise, return surrounding cells only.
   * radius: radius, in cells, of neighborhood to get.
   * *
   * Returns:
   * A list of coordinate tuples representing the neighborhood. For
   * example with radius 1, it will return list with number of elements
   * equals at most 9 (8) if Moore, 5 (4) if Von Neumann (if not
   * including the center).
   */
  def iterNeighborhood(

    pos: (Int, Int),
    moore: Boolean,
    includeCenter: Boolean = false,
    radius: Int = 1): Iterator[(Int, Int)] = {

    val vonNeumann = !moore

    val (x, y) = pos
    val coordinatesVisited = mutable.Set.empty[(Int, Int)]

    (for {
      dy <- (-radius).to(radius)
      dx <- (-radius).to(radius)
      if !(dx == 0 && dy == 0 && !includeCenter)
      // Skip diagonals in Von Neumann neighborhood.
      if !(vonNeumann && dy != 0 && dx != 0)
      // Skip diagonals in Moore neighborhood when distance > radius
      if !(moore && Math.pow(dy * dy + dx * dx, 0.5) > radius && radius > 1)
      // Skip if not a torus and new coords out of bounds.
      if !(isTorus && !(0 <= x + dx && x + dx < width) && !(0 <= y + dy && y + dy < width))

      pxy @ (px, py) = torusAjd((x + dx, y + dy))

      if !isOutOfBounds(pxy)
      if !coordinatesVisited.contains(pxy)
    } yield {
      coordinatesVisited.add(pxy)
      pxy
    })(collection.breakOut)
  }

  /**
   * Convert coordinate, handling torus looping.
   */
  def torusAjd(pos: (Int, Int)): (Int, Int) = {
    if (!isOutOfBounds(pos)) pos
    else if (!isTorus) throw new RuntimeException(s"Point $pos out of bounds, and space non-toroidal.")
    else (pos._1 % width, pos._2 % height)
  }

  /**
   * Determines whether position is off the grid, returns the out of
   * bounds coordinate.
   */
  def isOutOfBounds(pos: (Int, Int)): Boolean = {
    val (x, y) = pos
    x < 0 || x >= width || y < 0 || y >= height
  }

  /**
   * Args:
   * cell_list: Array-like of (x, y) tuples, or single tuple.
   * *
   * Returns:
   * An iterator of the contents of the cells identified in cell_list
   */
  def iterCellListContents(cellList: Iterator[(Int, Int)]): Iterator[AgentValue] = {
    cellList.filter(isCellEmpty).map { case (x, y) => grid(x)(y) }
  }

  def isCellEmpty(pos: (Int, Int)): Boolean = {
    val (x, y) = pos
    if (grid(x)(y) == defaultValue()) true
    else false
  }

  def iterCellListContents(cell: (Int, Int)): Iterator[AgentValue] = {
    iterCellListContents(List(cell).iterator)
  }

  def getCellListContents(cellList: (Int, Int)): List[AgentValue] = {
    getCellListContents(List(cellList).iterator)
  }

  /**
   * Args:
   * cell_list: Array-like of (x, y) tuples, or single tuple.
   * *
   * Returns:
   * A list of the contents of the cells identified in cell_list
   */
  def getCellListContents(cellList: Iterator[(Int, Int)]): List[AgentValue] = {
    iterCellListContents(cellList).toList
  }

  /**
   * Move an agent from its current position to a new position.
   * *
   * Args:
   * agent: Agent object to move. Assumed to have its current location
   * stored in a 'pos' tuple.
   * pos: Tuple of new position to move the agent to.
   */
  def moveAgent(agent: AgentValue, pos: (Int, Int)): Unit = {
    val pos2 = torusAjd(pos)
    _removeAgent(agent.pos, agent)
    _placeAgent(pos, agent)
    agent.pos = pos
  }

  def placeAgent(agent: AgentValue, pos: (Int, Int)): Unit = {
    _placeAgent(pos, agent)
    agent.pos = pos
  }

  private def _placeAgent(pos: (Int, Int), agent: AgentValue): Unit = {
    val (x, y) = pos
    grid(x)(y) = agent
    if (empties.contains(pos)) empties.remove(pos)
  }

  def removeAgent(agent: AgentValue): Unit = {
    _removeAgent(agent.pos, agent)
    agent.pos = null
  }

  private def _removeAgent(pos: (Int, Int), agent: AgentValue): Unit = {
    val (x, y) = pos
    grid(x)(y) = null // this needed Value >: Null
    empties += pos
  }

  /**
   * Moves agent to a random empty cell, vacating agent's old cell.
   */
  def moveToEmpty(agent: AgentValue): Unit = {
    val pos = agent.pos
    val newPos = findEmpty()
    if (newPos.isEmpty) throw new RuntimeException("ERROR: No empty cells")
    else {
      _placeAgent(newPos.get, agent)
      agent.pos = newPos.get
      _removeAgent(pos, agent)
    }
  }

  /**
   * Pick a random empty cell.
   */
  def findEmpty(): Option[(Int, Int)] = {
    if (existsEmptyCells()) {
      // TODO make reuse random from Model
      val idx = new scala.util.Random().nextInt(empties.size)
      Some(empties.toList(idx))
    } else None
  }

  def existsEmptyCells(): Boolean = {
    empties.nonEmpty
  }
}
