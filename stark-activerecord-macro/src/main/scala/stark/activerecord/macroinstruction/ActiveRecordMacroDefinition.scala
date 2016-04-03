package stark.activerecord.macroinstruction

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * hall orm macro definition
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-01
 */
object ActiveRecordMacroDefinition {
  private val find_by_pattern = "(?i)find_by_([_a-zA-Z0-9]*)".r

  /**
   * find method
   */
  def findByMethodImpl[E: c.WeakTypeTag,R](c: whitebox.Context)
                                          (name: c.Expr[String])
                                          (params:c.Expr[Any]*) : c.Expr[R] = {
    import c.universe._
    val Literal(Constant(findMethod)) = name.tree
    val paramsTree = params.map(_.tree).toList
    findMethod.toString.trim match{
      case find_by_pattern(attributes) =>
        //gather class field
        val expectedNames =  findMembers[E](c).map(_.name.toString.trim)
        val attrs =  attributes.split("_and_")
        if(attrs.length != params.length){
          c.error(c.enclosingPosition, s"name's length ${attrs.length} !=  parameter's length ${params.length}.")
        }
        //validate parameter name
        attrs.zipWithIndex.foreach{case (attr,index)=>
          if(!expectedNames.contains(attr)){
            c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#$attr not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
          }
        }
        val tupleParameters = attrs.zip(paramsTree).map{
          case (attr,parameterTree) =>
            val field = findField(c)(c.Expr[String](Literal(Constant(attr))))
            q"($field === $parameterTree)"
        }.toList


        executeInternalWhere[c.type,R](c)(tupleParameters)
      case other=>
        c.error(c.enclosingPosition, s"unsupport operation")
        c.Expr[R](Literal(Constant(Nil)))
    }

  }
  private def findMembers[E:c.WeakTypeTag](c:whitebox.Context):Seq[c.universe.TermSymbol]={
    /**
      * find all members included inherited members
      *
      */
    c.weakTypeOf[E].members.filter(_.isTerm).filter(_.asTerm.isAccessor).map(_.asTerm.accessed.asTerm).toStream.distinct.toSeq
  }
  /**
   * find method
   */
  def findField[E: c.WeakTypeTag,R](c: whitebox.Context)
                                       (fieldName: c.Expr[String]):c.Expr[R]  = {
    import c.universe._
    val Literal(Constant(field:String)) = fieldName.tree
    //gather class field

    val expectedNames =  findMembers[E](c)

    //validate field name
//    c.error(c.enclosingPosition,s"field: $field")
    val termOpt =  expectedNames.find(_.name.decodedName.toString.trim == field)
    termOpt match{
      case Some(term) =>
        val termType = term.typeSignature
        c.Expr[R](q"stark.activerecord.services.DSL.column[$termType]($field)")
      case None=>
        c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#$field not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
        c.Expr[R](Literal(Constant(Nil)))
    }
  }
  private def executeInternalWhere[C <: whitebox.Context,R](c:C)(paramsTree:List[c.universe.Tree]): c.Expr[R]={
    import c.universe._

    val clazzTree = Select(c.prefix.tree,TermName("clazz"))
    val primaryKeyTree = Select(c.prefix.tree,TermName("primaryKey"))

    c.Expr[R](q"stark.activerecord.services.ActiveRecord.createCriteriaRelation($clazzTree,$primaryKeyTree,..$paramsTree)")

    //c.Expr[R](Apply(Apply(Select(objectTree, TermName("internalWhere")), List(clazzTree,primaryKeyTree,qlTree)),paramsTree))
  }

  /**
   * implement update(name=xxx,sex=xxx)
   */
  def updateMethodImpl[E: c.WeakTypeTag,R](c: whitebox.Context)
                                           (name: c.Expr[String])
                                           (params:c.Expr[(String,Any)]*):c.Expr[R] = {
    import c.universe._
    val Literal(Constant(methodName:String)) = name.tree
    //find class field
    val expectedNames =  findMembers[E](c)

    val trees = params.map(_.tree).toList
    trees.foreach{
      case Apply(_,Literal(Constant(_name: String))::value::_) =>
        if(_name.isEmpty)
          c.error(c.enclosingPosition, s"name parameter is empty.")
        val termOpt = expectedNames.find(_.name.decodedName.toString.trim == _name)
        termOpt match{
          case Some(term) =>
            val termName = _name
            val termType = term.typeSignature
            val valueType = value.tpe
            //if(!(termType weak_<:< valueType))
            if(!(valueType <:< termType))
              c.error(value.pos, s"$termType expected,but $valueType occur.")
            q"($termName,$value.asInstanceOf[$termType])"
          case None =>
            c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${_name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
        }
      case other =>
        c.error(c.enclosingPosition, s"$other unsupported.")
     }
    //c.error(c.enclosingPosition,"asdf")

    methodName match{
      case "update" | "set"=>
        c.Expr[R](Apply(Select(c.prefix.tree, TermName("internalUpdate")), trees))
      case other=>
        c.error(c.enclosingPosition, s"${other} unsupported.")
        c.Expr[R](Literal(Constant(Nil)))
    }
  }

  /**
   * implement find_by(name="xxx")
   */
  def findByNamedParameterImpl[E: c.WeakTypeTag,R](c: whitebox.Context)
                                            (name: c.Expr[String])
                                            (params:c.Expr[(String,Any)]*): c.Expr[R] = {
    import c.universe._

    val Literal(Constant(methodName:String)) = name.tree
    //find class field
    val expectedNames =  findMembers[E](c).map(_.name.toString.trim)

    //validate params
    val trees = params.map(_.tree).toList
    val finalTrees = trees.map{
      case Apply(_,Literal(Constant(_name: String))::value::Nil) =>
        if(_name.isEmpty)
          c.error(c.enclosingPosition, s"name parameter is empty.")
        else if(!expectedNames.contains(_name))
          c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${_name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")

        val field = findField(c)(c.Expr[String](Literal(Constant(_name))))
        q"($field === $value)"
      case other =>
          c.error(c.enclosingPosition, s"$other unsupported.")

    }.asInstanceOf[List[c.universe.Tree]]

    methodName match{
      case "find_by" =>
        executeInternalWhere[c.type,R](c)(finalTrees.toList)
      case other=>
        c.error(c.enclosingPosition, s"$other unsupported.")
        c.Expr[R](Literal(Constant(Nil)))
    }
  }
}
