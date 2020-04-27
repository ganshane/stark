package reward.entities

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import javax.persistence.AttributeConverter

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-06
  */
class ScalaEnumerationConverter[T<:Enumeration](enum:T) extends AttributeConverter[T#Value,Integer]{

  override def convertToDatabaseColumn(x: T#Value): Integer = x.id

  override def convertToEntityAttribute(y: Integer): T#Value ={
    if(y == null) null
    else enum.apply(y)
  }
}
class ScalaEnumerationSerializer extends JsonSerializer[Enumeration#Value]{
  override def serialize(value: Enumeration#Value, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeNumber(value.id)
  }
}
class ScalaEnumerationTextSerializer extends JsonSerializer[Enumeration#Value]{
  override def serialize(value: Enumeration#Value, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeString(value.toString)
  }
}
