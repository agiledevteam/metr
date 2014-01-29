package com.lge.metr

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.{ ProvenShape, ForeignKeyQuery }

class Projects(tag: Tag) extends Table[(Int, String, String, String)](tag, "PROJECTS") {
  def id: Column[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name: Column[String] = column[String]("NAME")
  def gitdir: Column[String] = column[String]("GITDIR")
  def branch: Column[String] = column[String]("BRANCH")
  def * = (id, name, gitdir, branch)
}

class Commits(tag: Tag) extends Table[(String, Int, String, Long)](tag, "COMMITS") {
  def sha1: Column[String] = column[String]("SHA1", O.PrimaryKey)
  def projectId: Column[Int] = column[Int]("PROJECT_ID")
  def author: Column[String] = column[String]("AUTHOR")
  def timestamp: Column[Long] = column[Long]("TIMESTAMP")
  def * = (sha1, projectId, author, timestamp)

  // A reified foreign key relation that can be navigated to create a join
  def project: ForeignKeyQuery[Projects, (Int, String, String, String)] =
    foreignKey("PROJECT_FK", projectId, TableQuery[Projects])(_.id)
}

class Objects(tag: Tag) extends Table[(String, Int, Double)](tag, "OBJECTS") {
  def sha1: Column[String] = column[String]("SHA1", O.PrimaryKey)
  def sloc: Column[Int] = column[Int]("SLOC")
  def dloc: Column[Double] = column[Double]("DLOC")
  def * = (sha1, sloc, dloc)
}

// A Suppliers table with 6 columns: id, name, street, city, state, zip
class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
  def id: Column[Int] = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
  def name: Column[String] = column[String]("SUP_NAME")
  def street: Column[String] = column[String]("STREET")
  def city: Column[String] = column[String]("CITY")
  def state: Column[String] = column[String]("STATE")
  def zip: Column[String] = column[String]("ZIP")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, String, String, String, String, String)] = (id, name, street, city, state, zip)
}

// A Coffees table with 5 columns: name, supplier id, price, sales, total
class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
  def name: Column[String] = column[String]("COF_NAME", O.PrimaryKey)
  def supID: Column[Int] = column[Int]("SUP_ID")
  def price: Column[Double] = column[Double]("PRICE")
  def sales: Column[Int] = column[Int]("SALES")
  def total: Column[Int] = column[Int]("TOTAL")

  def * : ProvenShape[(String, Int, Double, Int, Int)] = (name, supID, price, sales, total)

  // A reified foreign key relation that can be navigated to create a join
  def supplier: ForeignKeyQuery[Suppliers, (Int, String, String, String, String, String)] =
    foreignKey("SUP_FK", supID, TableQuery[Suppliers])(_.id)
}