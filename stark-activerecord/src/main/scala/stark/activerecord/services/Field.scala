package stark.activerecord.services


import stark.activerecord.services.Condition.ExpressionBuilder
import stark.activerecord.services.DSL.UpdateField

import javax.persistence.criteria.Selection
import scala.language.implicitConversions
import scala.reflect.runtime.universe._

/**
 * ActiveRecord field
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object Field {
  def countField = new SelectionField {
    override def toSelection[X]: Selection[X] = {
      val context = DSL.dslContext.value
      context.builder.count(context.root).asInstanceOf[Selection[X]]
    }
  }

  implicit def wrapNumericField[T <: Number](field: Field[T]): NumericField[T] = new NumericField(field)

  implicit def wrapNumericField[T](field: Field[T])(implicit num: Numeric[T]) = new NumericField(field)

  implicit def wrapStringField(field: Field[String]): StringField = new StringField(field)
}

trait Field[+A] extends SelectionField {
  val fieldName: String

  def ===[B >: A](value: B): Condition

  def ===(value: Field[Any]): Condition

  def +(value: Field[Any]): CompositeField[java.lang.Number]
  def -(value: Field[Any]): CompositeField[java.lang.Number]

  private [activerecord] def getExpression[T]:ExpressionBuilder[T]

    def <[B >: A](value: B): Condition

  def <=[B >: A](value: B): Condition

  def >[B >: A](value: B): Condition

  def >=[B >: A](value: B): Condition

  def <(value: Field[Any]): Condition

  def <=(value: Field[Any]): Condition

  def >(value: Field[Any]): Condition

  def >=(value: Field[Any]): Condition

  def !==[B >: A](value: B): Condition

  def !==[B >: A](value: Field[B]): Condition

  def <>[B >: A](value: B) = !==(value)

  def <>[B >: A](value: Field[B]) = !==(value)

  def isNull: Condition

  def notNull: Condition

  def desc[B >: A]: SortField[B]

  def asc[B >: A]: SortField[B]

  def count: SelectionField

  def sum: SelectionField

  def distinct: SelectionField

  def max: SelectionField

  //用来更新操作
  def ~=?[B >: A](value: B): UpdateField

  def ~=[B >: A](value: B): UpdateField
}

trait SelectionField {
  def toSelection[X]: Selection[X]
}

trait DistinctSelectionField extends SelectionField {
}

case class SortField[T](field: Field[T], isAsc: Boolean = true)

private[activerecord] class CompositeField[A](expressionBuilder: ExpressionBuilder[A]) extends JPAField(null) {
  override private[activerecord] def getExpression[T] = expressionBuilder.asInstanceOf[ExpressionBuilder[T]]
}
private[activerecord] class JPAField[+T: TypeTag](val fieldName: String) extends Field[T] {
  override def ===[B >: T](value: B): Condition = {
    Condition.eq(this, value)
  }

  override def ===(value: Field[Any]): Condition = {
    Condition.eq(this, value)
  }


  override private[activerecord] def getExpression[A]:ExpressionBuilder[A] = {
    builder => Condition.findFieldPath(fieldName)
  }

  override def +(value: Field[Any]): CompositeField[java.lang.Number] = {
    new CompositeField(Condition.plus(this, value))
  }


  override def -(value: Field[Any]): CompositeField[Number] = {
    new CompositeField(Condition.minus(this, value))
  }

  override def <[B >: T](value: B): Condition = Condition.lt(this, value)

  override def <=[B >: T](value: B): Condition = Condition.le(this, value)

  override def >[B >: T](value: B): Condition = Condition.gt(this, value)

  override def >=[B >: T](value: B): Condition = Condition.ge(this, value)

  override def <(value: Field[Any]): Condition = Condition.lt(this, value)

  override def <=(value: Field[Any]): Condition = Condition.le(this, value)

  override def >(value: Field[Any]): Condition = Condition.gt(this, value)

  override def >=(value: Field[Any]): Condition = Condition.ge(this, value)

  override def !==[B >: T](value: B): Condition = Condition.notEq(this, value)

  override def !==[B >: T](value: Field[B]): Condition = Condition.notEq(this, value)

  override def isNull: Condition = Condition.isNull(this)

  override def notNull: Condition = Condition.notNull(this)

  override def desc[B >: T]: SortField[B] = SortField(this, false)

  override def asc[B >: T]: SortField[B] = SortField(this, isAsc = true)

  override def count: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        DSL.dslContext.value.builder.count(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  private def expression[X] = {
    DSL.dslContext.value.root.get[X](fieldName)
  }

  override def sum: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        DSL.dslContext.value.builder.sum(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  override def max: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        DSL.dslContext.value.builder.max(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  override def distinct: SelectionField = {
    new DistinctSelectionField {
      override def toSelection[X]: Selection[X] = {
        JPAField.this.toSelection
      }
    }
  }

  override def toSelection[X]: Selection[X] = {
    DSL.dslContext.value.root.get[X](fieldName)
  }

  override def ~=?[B >: T](value: B): UpdateField = {
    updater => {
      if (value != null) updater.set(this.fieldName, value)
      else updater
    }
  }

  override def ~=[B >: T](value: B): UpdateField = {
    updater => {
      updater.set(this.fieldName, value)
    }
  }
}

class NumericField[T](field: Field[T]) {
  def avg: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.avg(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  def max: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.max(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  def min: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.min(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  def sum: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.sum(expression).asInstanceOf[Selection[X]]
      }
    }
  }
}

class StringField(field: Field[String]) {
  def like(value: String): Condition = Condition.like(field, value)

  def notLike(value: String): Condition = Condition.notLike(field, value)
}
